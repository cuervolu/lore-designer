package dev.cuervolu.loredesigner.workspace

import dev.cuervolu.loredesigner.core.workspace.CURRENT_PROJECT_FORMAT_VERSION
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import kotlinx.coroutines.test.runTest
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class WorkspaceUseCasesTest {
    private val workspaceId = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb380")

    @Test
    fun `create builds current project configuration with generated id`() = runTest {
        val store = RecordingWorkspaceStore()
        val createWorkspace = CreateWorkspace(store, WorkspaceIdGenerator { workspaceId })
        val parent = Path.of("worlds")

        val result = createWorkspace(parent, "My World", ProjectColor.VIOLET)

        val workspace = assertIs<WorkspaceResult.Success<Workspace>>(result).value
        assertEquals(parent.toAbsolutePath().normalize().resolve("My World"), workspace.location)
        assertEquals(
            ProjectConfig(
                version = CURRENT_PROJECT_FORMAT_VERSION,
                id = workspaceId,
                name = "My World",
                color = ProjectColor.VIOLET,
            ),
            workspace.config,
        )
    }

    @Test
    fun `create trims the name used for the folder and the project`() = runTest {
        val createWorkspace = CreateWorkspace(RecordingWorkspaceStore(), WorkspaceIdGenerator { workspaceId })

        val result = createWorkspace(Path.of("folder", "..", "worlds"), "  Embercourt  ")

        val workspace = assertIs<WorkspaceResult.Success<Workspace>>(result).value
        assertEquals(Path.of("worlds", "Embercourt").toAbsolutePath().normalize(), workspace.location)
        assertEquals("Embercourt", workspace.config.name)
    }

    @Test
    fun `create rejects names that are not portable folder names without accessing storage`() = runTest {
        val invalidNames =
            listOf(
                "a/b",
                "a\\b",
                "..",
                ".",
                "a:b",
                "what?",
                "trailing.",
                "CON",
                "nul.txt",
                "Com1",
                "lpt9.md",
                "tab\tname",
            )
        val createWorkspace = CreateWorkspace(FailingWorkspaceStore(), WorkspaceIdGenerator { workspaceId })

        for (name in invalidNames) {
            val result = createWorkspace(Path.of("worlds"), name)

            val error = assertIs<WorkspaceError.InvalidWorkspaceName>(assertIs<WorkspaceResult.Failure>(result).error)
            assertEquals(name, error.name)
        }
    }

    @Test
    fun `create accepts names that only resemble reserved device names`() = runTest {
        val createWorkspace = CreateWorkspace(RecordingWorkspaceStore(), WorkspaceIdGenerator { workspaceId })

        for (name in listOf("Console", "COM10", "Aux Realm", "Nullspace")) {
            assertIs<WorkspaceResult.Success<Workspace>>(createWorkspace(Path.of("worlds"), name))
        }
    }

    @Test
    fun `create rejects blank name before generating an id or accessing storage`() = runTest {
        var generated = false
        val createWorkspace =
            CreateWorkspace(
                FailingWorkspaceStore(),
                WorkspaceIdGenerator {
                    generated = true
                    workspaceId
                },
            )

        val result = createWorkspace(Path.of("world"), "  ")

        assertIs<WorkspaceError.InvalidWorkspaceName>(assertIs<WorkspaceResult.Failure>(result).error)
        assertEquals(false, generated)
    }

    @Test
    fun `open normalizes the workspace location`() = runTest {
        val store = RecordingWorkspaceStore()

        OpenWorkspace(store)(Path.of("folder", "..", "world"))

        assertEquals(Path.of("world").toAbsolutePath().normalize(), store.openedLocation)
    }

    private class RecordingWorkspaceStore : WorkspaceStore {
        var openedLocation: Path? = null

        override suspend fun create(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> =
            WorkspaceResult.Success(Workspace(location, config))

        override suspend fun open(location: Path): WorkspaceResult<Workspace> {
            openedLocation = location
            return WorkspaceResult.Failure(WorkspaceError.NotAWorkspace(location))
        }
    }

    private class FailingWorkspaceStore : WorkspaceStore {
        override suspend fun create(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> =
            error("storage must not be called")

        override suspend fun open(location: Path): WorkspaceResult<Workspace> = error("storage must not be called")
    }
}
