package dev.cuervolu.loredesigner

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.ui.launcher.LauncherDialog
import dev.cuervolu.loredesigner.ui.launcher.LauncherError
import dev.cuervolu.loredesigner.ui.launcher.LauncherNotice
import dev.cuervolu.loredesigner.ui.launcher.LauncherProject
import dev.cuervolu.loredesigner.ui.launcher.LauncherUiState
import dev.cuervolu.loredesigner.ui.launcher.LauncherViewModel
import dev.cuervolu.loredesigner.ui.launcher.ProjectAvailability
import dev.cuervolu.loredesigner.ui.launcher.ProjectSystemActions
import dev.cuervolu.loredesigner.workspace.UpdateProjectConfig
import dev.cuervolu.loredesigner.workspace.Workspace
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import dev.cuervolu.loredesigner.workspace.WorkspaceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okio.Path.Companion.toOkioPath
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

/**
 * The project lifecycle through the launcher, on the production Koin graph and real files.
 *
 * Every "session" starts the graph from scratch over the same application data folder and stops it again,
 * so only what reached the disk can carry a project from one session to the next.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProjectLifecycleTest {
    @BeforeTest
    fun useTestMainDispatcher() {
        // coroutines-swing makes Main the AWT event queue, which the test body does not run on.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun restore() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a project created in the launcher is stored and can be reopened in a later session`() =
        withScratch { scratch ->
            val created = session(scratch) { createProject("Embercourt", scratch.projects, ProjectColor.GREEN) }

            val projectFile = scratch.projects.resolve("Embercourt/project.lore")
            assertTrue(projectFile.exists(), "Creating a project must write project.lore")
            assertTrue("Embercourt" in projectFile.readText(), "project.lore must hold the project name")

            session(scratch) {
                val listed = awaitProject(created.config.id, ProjectAvailability.Available)
                assertEquals("Embercourt", listed.name)
                assertEquals(ProjectColor.GREEN, listed.color)
                assertEquals(scratch.projects.resolve("Embercourt").toString(), listed.path)

                viewModel.openProject(created.config.id)
                val reopened = await { it.openedWorkspace }
                assertEquals(created, reopened)
            }
        }

    @Test
    fun `a rename made in a session reaches the launcher list without reopening the project`() =
        withScratch { scratch ->
            session(scratch) {
                val workspace = createProject("Embercourt", scratch.projects, ProjectColor.GREEN)
                val renamed = assertIs<WorkspaceResult.Success<Workspace>>(
                    koin.get<UpdateProjectConfig>()(workspace, "Embercourt Reborn", ProjectColor.RED),
                ).value
                assertEquals("Embercourt Reborn", renamed.config.name)
                awaitStored("Embercourt Reborn")
            }

            assertTrue("Embercourt Reborn" in scratch.projects.resolve("Embercourt/project.lore").readText())
            session(scratch) {
                val listed = awaitProject(onlyProjectId(), ProjectAvailability.Available)
                assertEquals("Embercourt Reborn", listed.name)
                assertEquals(ProjectColor.RED, listed.color)
                // Renaming a project does not rename its folder.
                assertEquals(scratch.projects.resolve("Embercourt").toString(), listed.path)
            }
        }

    @Test
    fun `a deleted project folder is listed as missing but remembered, and cannot be opened`() =
        withScratch { scratch ->
            val created = session(scratch) { createProject("Embercourt", scratch.projects) }
            scratch.projects.resolve("Embercourt").toFile().deleteRecursively()

            session(scratch) {
                val missing = awaitProject(created.config.id, ProjectAvailability.Missing)
                assertEquals("Embercourt", missing.name, "A missing project keeps the name it was last seen with")
                assertTrue(viewModel.state.value.projects.missing.any { it.id == created.config.id })

                viewModel.openProject(created.config.id)
                val notice = await { it.notices.firstOrNull()?.notice }
                assertIs<WorkspaceError.NotAWorkspace>(failure(notice))
                assertNull(viewModel.state.value.openedWorkspace)
            }

            // Missing is a state of the launcher, not a reason to forget the project.
            session(scratch) { awaitProject(created.config.id, ProjectAvailability.Missing) }
        }

    @Test
    fun `a moved project is recovered by locating its new folder and stays there in later sessions`() =
        withScratch { scratch ->
            val created = session(scratch) { createProject("Embercourt", scratch.projects, ProjectColor.GREEN) }
            val newHome = scratch.root.resolve("archive").createDirectories().resolve("Embercourt")
            Files.move(scratch.projects.resolve("Embercourt"), newHome)

            session(scratch) {
                awaitProject(created.config.id, ProjectAvailability.Missing)

                viewModel.relocateProject(created.config.id, newHome.toOkioPath())

                val notice = await { it.notices.firstOrNull()?.notice }
                assertEquals(LauncherNotice.ProjectLocated("Embercourt"), notice)
                val found = awaitProject(created.config.id, ProjectAvailability.Available)
                assertEquals(newHome.toString(), found.path)
                assertTrue(viewModel.state.value.projects.missing.isEmpty())
                awaitStored("archive")
            }

            session(scratch) {
                val listed = awaitProject(created.config.id, ProjectAvailability.Available)
                assertEquals(newHome.toString(), listed.path, "The new location must have been persisted")
                viewModel.openProject(created.config.id)
                assertEquals(newHome.toOkioPath(), await { it.openedWorkspace }.location)
            }
        }

    @Test
    fun `locating a folder that holds another project is refused and the project stays missing`() =
        withScratch { scratch ->
            val (lost, other) = session(scratch) {
                createProject("Embercourt", scratch.projects) to createProject("Other", scratch.projects)
            }
            scratch.projects.resolve("Embercourt").toFile().deleteRecursively()

            session(scratch) {
                awaitProject(lost.config.id, ProjectAvailability.Missing)

                viewModel.relocateProject(lost.config.id, other.location)

                val error = failure(await { it.notices.firstOrNull()?.notice })
                assertIs<WorkspaceError.DifferentWorkspace>(error)
                assertEquals(lost.config.id, error.expected)
                assertEquals(other.config.id, error.found.id)
            }

            session(scratch) {
                val listed = awaitProject(lost.config.id, ProjectAvailability.Missing)
                assertEquals(scratch.projects.resolve("Embercourt").toString(), listed.path)
                awaitProject(other.config.id, ProjectAvailability.Available)
            }
        }

    @Test
    fun `locating a folder without a project is refused and nothing changes`() = withScratch { scratch ->
        val created = session(scratch) { createProject("Embercourt", scratch.projects) }
        scratch.projects.resolve("Embercourt").toFile().deleteRecursively()
        val empty = scratch.root.resolve("empty").createDirectories()

        session(scratch) {
            awaitProject(created.config.id, ProjectAvailability.Missing)

            viewModel.relocateProject(created.config.id, empty.toOkioPath())

            assertIs<WorkspaceError.NotAWorkspace>(failure(await { it.notices.firstOrNull()?.notice }))
            assertEquals(
                scratch.projects.resolve("Embercourt").toString(),
                viewModel.state.value.projects.missing.single().path,
            )
        }
    }

    private fun failure(notice: LauncherNotice?): WorkspaceError {
        val failed = assertIs<LauncherNotice.Failed>(notice)
        return assertIs<LauncherError.Workspace>(failed.error).error
    }

    private fun <T> withScratch(block: suspend (Scratch) -> T) {
        ScratchDirectory().use { directory ->
            val projects = directory.root.resolve("projects").createDirectories()
            runBlocking { block(Scratch(directory.root.resolve("appdata"), directory.root, projects)) }
        }
    }

    /** Runs [block] against a freshly started application, then stops it as quitting the app would. */
    private suspend fun <T> session(scratch: Scratch, block: suspend LauncherSession.() -> T): T =
        AssembledApp.start(scratch.appData).use { app -> LauncherSession(app).block() }

    private class Scratch(val appData: Path, val root: Path, val projects: Path)

    private class LauncherSession(private val app: AssembledApp) {
        val koin = app.koin
        val viewModel = LauncherViewModel(
            koin.get(),
            koin.get(),
            koin.get(),
            NoSystemActions,
        )

        suspend fun createProject(name: String, parent: Path, color: ProjectColor = ProjectColor.VIOLET): Workspace {
            viewModel.showNewProject()
            val dialog = assertIs<LauncherDialog.NewProject>(viewModel.state.value.dialog)
            dialog.name.setTextAndPlaceCursorAtEnd(name)
            dialog.location.setTextAndPlaceCursorAtEnd(parent.toString())
            viewModel.onColorChange(color)
            viewModel.submit()
            val workspace = await { it.openedWorkspace }
            viewModel.onWorkspaceHandled()
            // Opening shows the workspace first and remembers it afterwards.
            awaitRemembered(workspace.config.id)
            awaitStored(workspace.config.id.toString())
            return workspace
        }

        suspend fun onlyProjectId(): WorkspaceId = awaitLoaded().all.single().id

        suspend fun awaitProject(id: WorkspaceId, availability: ProjectAvailability): LauncherProject =
            awaitState { state ->
                state.projects.all.plus(state.projects.missing).firstOrNull { it.id == id }
                    ?.takeIf { it.availability == availability }
            }

        suspend fun <T : Any> await(select: (LauncherUiState) -> T?): T = awaitState(select)

        /** The registry updates its list before the file, and quitting must not outrun the write. */
        suspend fun awaitStored(text: String) {
            val file = app.appDataDirectory.resolve("workspaces.json")
            try {
                withTimeout(TIMEOUT) {
                    while (!(file.exists() && text in file.readText())) delay(POLL)
                }
            } catch (timeout: TimeoutCancellationException) {
                throw AssertionError("workspaces.json never contained $text", timeout)
            }
        }

        private suspend fun awaitLoaded() = awaitState { state -> state.projects.takeIf { state.projectsLoaded } }

        private suspend fun awaitRemembered(id: WorkspaceId) {
            awaitState { state -> state.projects.all.firstOrNull { it.id == id } }
        }

        private suspend fun <T : Any> awaitState(select: (LauncherUiState) -> T?): T = try {
            withTimeout(TIMEOUT) {
                var selected = select(viewModel.state.value)
                while (selected == null) {
                    delay(POLL)
                    selected = select(viewModel.state.value)
                }
                selected
            }
        } catch (timeout: TimeoutCancellationException) {
            throw AssertionError(
                "The launcher never reached the expected state; it ended as ${viewModel.state.value}",
                timeout,
            )
        }
    }

    private object NoSystemActions : ProjectSystemActions {
        override suspend fun showInFolder(projectLocation: String) = false

        override suspend fun copyText(text: String) = false
    }

    private companion object {
        val TIMEOUT = 10_000.milliseconds
        val POLL = 20.milliseconds
    }
}
