package dev.cuervolu.loredesigner.ui.launcher

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspace
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class LauncherViewModelTest {
    private val store = FakeWorkspaceStore()
    private var registry = FakeRecentWorkspacesRegistry()
    private val viewModel by lazy { newViewModel() }

    private fun newViewModel() = LauncherViewModel(
        store.createWorkspace(),
        store.opener(registry),
        registry,
        homeDirectory = { "/home/writer" },
    )

    private fun idOf(n: Int) = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-%012d".format(n))

    private fun recent(n: Int, minute: Int, pinned: Boolean = false) = RecentWorkspace(
        id = idOf(n),
        location = "/worlds/w$n".toPath(),
        lastKnownName = "World $n",
        lastKnownColor = ProjectColor.entries[n % ProjectColor.entries.size],
        lastOpenedAt = Instant.parse("2026-01-01T00:00:00Z") + minute.minutes,
        pinned = pinned,
    )

    private fun names(projects: List<LauncherProject>) = projects.map { it.name }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful create closes the dialog and exposes the workspace`() {
        viewModel.showNewProject()
        val dialog = assertIs<LauncherDialog.NewProject>(viewModel.state.value.dialog)
        dialog.name.setTextAndPlaceCursorAtEnd("Embercourt")
        dialog.location.setTextAndPlaceCursorAtEnd("/worlds")
        viewModel.onColorChange(ProjectColor.GREEN)

        viewModel.submit()

        val state = viewModel.state.value
        assertNull(state.dialog)
        assertNull(state.error)
        val workspace = assertNotNull(state.openedWorkspace)
        assertEquals("/worlds/Embercourt".toPath(), workspace.location)
        assertEquals(ProjectColor.GREEN, workspace.config.color)
    }

    @Test
    fun `failed create keeps the dialog open with the error`() {
        store.createError = WorkspaceError.DestinationNotEmpty("/worlds/Embercourt".toPath())
        viewModel.showNewProject()
        val dialog = assertIs<LauncherDialog.NewProject>(viewModel.state.value.dialog)
        dialog.name.setTextAndPlaceCursorAtEnd("Embercourt")
        dialog.location.setTextAndPlaceCursorAtEnd("/worlds")

        viewModel.submit()

        val state = viewModel.state.value
        assertIs<LauncherDialog.NewProject>(state.dialog)
        assertEquals(LauncherError.Workspace(store.createError!!), state.error)
        assertNull(state.openedWorkspace)
    }

    @Test
    fun `invalid name is reported by workspace creation, not pre-validated`() {
        viewModel.showNewProject()
        val dialog = assertIs<LauncherDialog.NewProject>(viewModel.state.value.dialog)
        dialog.name.setTextAndPlaceCursorAtEnd("a/b")
        dialog.location.setTextAndPlaceCursorAtEnd("/worlds")

        viewModel.submit()

        val error = assertIs<LauncherError.Workspace>(viewModel.state.value.error)
        assertIs<WorkspaceError.InvalidWorkspaceName>(error.error)
        assertTrue(store.createdLocations.isEmpty())
    }

    @Test
    fun `blank location is reported without touching storage`() {
        viewModel.showOpenProject()

        viewModel.submit()

        assertEquals(LauncherError.MissingLocation, viewModel.state.value.error)
        assertTrue(store.openedLocations.isEmpty())
    }

    @Test
    fun `tilde expands to the home directory`() {
        viewModel.showOpenProject()
        assertIs<LauncherDialog.OpenProject>(
            viewModel.state.value.dialog,
        ).path.setTextAndPlaceCursorAtEnd("~/Worlds/Ceili")

        viewModel.submit()

        assertEquals("/home/writer/Worlds/Ceili".toPath(), store.openedLocations.single())
    }

    @Test
    fun `location with control characters is invalid without touching storage`() {
        viewModel.showOpenProject()
        assertIs<LauncherDialog.OpenProject>(
            viewModel.state.value.dialog,
        ).path.setTextAndPlaceCursorAtEnd("/worlds\u0000")

        viewModel.submit()

        assertEquals(LauncherError.InvalidLocation("/worlds\u0000"), viewModel.state.value.error)
        assertTrue(store.openedLocations.isEmpty())
    }

    @Test
    fun `successful open exposes the workspace until handled`() {
        viewModel.showOpenProject()
        viewModel.onDirectoryChosen("/worlds/Embercourt".toPath())

        viewModel.submit()
        assertEquals("Embercourt", viewModel.state.value.openedWorkspace?.config?.name)

        viewModel.onWorkspaceHandled()
        assertNull(viewModel.state.value.openedWorkspace)
    }

    @Test
    fun `failed open does not expose a workspace`() {
        store.openError = WorkspaceError.NotAWorkspace("/tmp".toPath())
        viewModel.showOpenProject()
        viewModel.onDirectoryChosen("/tmp".toPath())

        viewModel.submit()

        assertNull(viewModel.state.value.openedWorkspace)
        assertEquals(LauncherError.Workspace(store.openError!!), viewModel.state.value.error)
    }

    @Test
    fun `chosen directory fills the active dialog field`() {
        viewModel.showNewProject()

        viewModel.onDirectoryChosen("/worlds".toPath())

        val dialog = assertIs<LauncherDialog.NewProject>(viewModel.state.value.dialog)
        assertEquals("/worlds".toPath().toString(), dialog.location.text.toString())
    }

    @Test
    fun `operation in flight is busy and cannot be dismissed or resubmitted`() {
        val gate = CompletableDeferred<Unit>()
        store.gate = gate
        viewModel.showOpenProject()
        viewModel.onDirectoryChosen("/worlds/Embercourt".toPath())

        viewModel.submit()
        assertTrue(viewModel.state.value.busy)
        viewModel.submit()
        viewModel.dismissDialog()
        assertIs<LauncherDialog.OpenProject>(viewModel.state.value.dialog)

        gate.complete(Unit)
        assertEquals(false, viewModel.state.value.busy)
        assertEquals(1, store.openedLocations.size)
    }

    @Test
    fun `dismissing clears the dialog and its error`() {
        viewModel.showOpenProject()
        viewModel.submit()

        viewModel.dismissDialog()

        assertNull(viewModel.state.value.dialog)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `remembered projects load into the sections with real availability`() {
        registry = FakeRecentWorkspacesRegistry(listOf(recent(1, 1), recent(2, 3, pinned = true), recent(3, 2)))
        store.missingLocations += "/worlds/w3".toPath()

        val state = viewModel.state.value

        assertTrue(state.projectsLoaded)
        assertEquals(listOf("World 2", "World 1"), names(state.projects.all))
        assertEquals(listOf("World 2"), names(state.projects.pinned))
        assertEquals(listOf("World 2", "World 1"), names(state.projects.recent))
        assertEquals(listOf("World 3"), names(state.projects.missing))
        assertEquals(ProjectAvailability.Missing, state.projects.missing.single().availability)
        assertTrue(state.projects.all.all { it.availability == ProjectAvailability.Available })
    }

    @Test
    fun `recent keeps only the most recently opened projects`() {
        registry = FakeRecentWorkspacesRegistry((1..RECENT_PROJECT_LIMIT + 2).map { recent(it, minute = it) })

        val projects = viewModel.state.value.projects

        assertEquals(RECENT_PROJECT_LIMIT + 2, projects.all.size)
        assertEquals(RECENT_PROJECT_LIMIT, projects.recent.size)
        assertEquals("World ${RECENT_PROJECT_LIMIT + 2}", projects.recent.first().name)
        assertEquals(projects.all.take(RECENT_PROJECT_LIMIT), projects.recent)
    }

    @Test
    fun `successful create registers the project`() {
        viewModel.showNewProject()
        val dialog = assertIs<LauncherDialog.NewProject>(viewModel.state.value.dialog)
        dialog.name.setTextAndPlaceCursorAtEnd("Embercourt")
        dialog.location.setTextAndPlaceCursorAtEnd("/worlds")

        viewModel.submit()

        val project = viewModel.state.value.projects.all.single()
        assertEquals(TestWorkspaceId, project.id)
        assertEquals("/worlds/Embercourt", project.path)
        assertEquals(listOf(project), viewModel.state.value.projects.recent)
    }

    @Test
    fun `successful manual open registers the project and survives handling the navigation`() {
        viewModel.showOpenProject()
        assertIs<LauncherDialog.OpenProject>(viewModel.state.value.dialog).path.setTextAndPlaceCursorAtEnd("/worlds/E")

        viewModel.submit()
        viewModel.onWorkspaceHandled()

        assertEquals(listOf("Embercourt"), names(viewModel.state.value.projects.all))
    }

    @Test
    fun `opening a remembered project validates it and refreshes the cached name`() {
        registry = FakeRecentWorkspacesRegistry(listOf(recent(1, 1)))
        store.openedId = idOf(1)
        store.openedName = "Renamed on disk"

        viewModel.openProject(idOf(1))

        assertEquals(listOf("/worlds/w1".toPath()), store.openedLocations)
        assertEquals(idOf(1), assertNotNull(viewModel.state.value.openedWorkspace).config.id)
        assertEquals(listOf("Renamed on disk"), names(viewModel.state.value.projects.all))
    }

    @Test
    fun `a remembered folder now holding another project is refused`() {
        registry = FakeRecentWorkspacesRegistry(listOf(recent(1, 1)))
        store.openedId = idOf(9)
        store.openedName = "Impostor"

        viewModel.openProject(idOf(1))

        val state = viewModel.state.value
        assertNull(state.openedWorkspace)
        val error = assertIs<WorkspaceError.DifferentWorkspace>(assertIs<LauncherError.Workspace>(state.error).error)
        assertEquals("Impostor", error.found.name)
        assertEquals(listOf("World 1"), names(state.projects.missing), "its folder holds another project now")
        assertFalse(state.busy)

        viewModel.refreshAvailability()
        assertEquals(listOf("World 1"), names(viewModel.state.value.projects.missing), "the cheap probe cannot undo it")
    }

    @Test
    fun `manually opening a remembered folder that holds another project moves the old entry to missing`() {
        registry = FakeRecentWorkspacesRegistry(listOf(recent(1, 1)))
        store.openedId = idOf(9)
        store.openedName = "Newcomer"
        viewModel.showOpenProject()
        assertIs<LauncherDialog.OpenProject>(viewModel.state.value.dialog).path.setTextAndPlaceCursorAtEnd("/worlds/w1")

        viewModel.submit()

        val projects = viewModel.state.value.projects
        assertEquals(idOf(9), assertNotNull(viewModel.state.value.openedWorkspace).config.id)
        assertEquals(listOf("Newcomer"), names(projects.all))
        assertEquals(listOf("World 1"), names(projects.missing))
    }

    @Test
    fun `a project whose folder vanished moves to missing when opened`() {
        registry = FakeRecentWorkspacesRegistry(listOf(recent(1, 1)))
        store.openError = WorkspaceError.NotAWorkspace("/worlds/w1".toPath())

        viewModel.openProject(idOf(1))

        val state = viewModel.state.value
        assertNull(state.openedWorkspace)
        assertEquals(LauncherError.Workspace(store.openError!!), state.error)
        assertEquals(listOf("World 1"), names(state.projects.missing))
        assertTrue(state.projects.all.isEmpty())
    }

    @Test
    fun `relocating to the same project updates its folder and leaves missing`() {
        registry = FakeRecentWorkspacesRegistry(listOf(recent(1, 1)))
        store.missingLocations += "/worlds/w1".toPath()
        store.openedId = idOf(1)
        assertEquals(1, viewModel.state.value.projects.missing.size)

        viewModel.relocateProject(idOf(1), "/archive/w1".toPath())

        val state = viewModel.state.value
        assertNull(state.openedWorkspace, "relocating does not open the project")
        assertTrue(state.projects.missing.isEmpty())
        assertEquals("/archive/w1", state.projects.all.single().path)
        assertEquals(recent(1, 1).lastOpenedAt, registry.workspaces.value.single().lastOpenedAt, "nor make it recent")
    }

    @Test
    fun `relocating to a different project is refused`() {
        registry = FakeRecentWorkspacesRegistry(listOf(recent(1, 1)))
        store.missingLocations += "/worlds/w1".toPath()
        store.openedId = idOf(2)

        viewModel.relocateProject(idOf(1), "/elsewhere".toPath())

        val state = viewModel.state.value
        assertIs<WorkspaceError.DifferentWorkspace>(assertIs<LauncherError.Workspace>(state.error).error)
        assertEquals("/worlds/w1", state.projects.missing.single().path)
    }

    @Test
    fun `pinning and forgetting update the sections`() {
        registry = FakeRecentWorkspacesRegistry(listOf(recent(1, 1), recent(2, 2)))

        viewModel.setPinned(idOf(1), true)
        assertEquals(listOf("World 1"), names(viewModel.state.value.projects.pinned))

        viewModel.forgetProject(idOf(1))
        assertEquals(listOf("World 2"), names(viewModel.state.value.projects.all))
        assertTrue(viewModel.state.value.projects.pinned.isEmpty())
    }

    @Test
    fun `a pinned project stays pinned while missing and can be unpinned`() {
        registry = FakeRecentWorkspacesRegistry(listOf(recent(1, 1, pinned = true)))
        store.missingLocations += "/worlds/w1".toPath()

        val pinned = viewModel.state.value.projects.pinned.single()
        assertEquals(ProjectAvailability.Missing, pinned.availability)
        assertEquals(listOf("World 1"), names(viewModel.state.value.projects.missing))

        viewModel.setPinned(idOf(1), false)
        assertTrue(viewModel.state.value.projects.pinned.isEmpty())
    }

    @Test
    fun `missing projects are checked again when availability is refreshed`() {
        registry = FakeRecentWorkspacesRegistry(listOf(recent(1, 1)))
        store.missingLocations += "/worlds/w1".toPath()
        assertEquals(listOf("World 1"), names(viewModel.state.value.projects.missing))

        store.missingLocations.clear()
        viewModel.refreshAvailability()

        assertTrue(viewModel.state.value.projects.missing.isEmpty())
        assertEquals(ProjectAvailability.Available, viewModel.state.value.projects.all.single().availability)
    }

    @Test
    fun `a failure to remember the project never blocks opening it`() {
        registry = FakeRecentWorkspacesRegistry(listOf(recent(1, 1))).apply {
            recordFailure = IllegalStateException("disk full")
        }
        store.openedId = idOf(1)

        viewModel.openProject(idOf(1))
        assertEquals(idOf(1), assertNotNull(viewModel.state.value.openedWorkspace).config.id)
        viewModel.onWorkspaceHandled()

        viewModel.showOpenProject()
        assertIs<LauncherDialog.OpenProject>(viewModel.state.value.dialog).path.setTextAndPlaceCursorAtEnd("/worlds/w1")
        viewModel.submit()

        val state = viewModel.state.value
        assertNotNull(state.openedWorkspace)
        assertFalse(state.busy)
        assertNull(state.error)
        assertNull(state.dialog)
    }

    @Test
    fun `launcher errors without a dialog can be dismissed`() {
        registry = FakeRecentWorkspacesRegistry(listOf(recent(1, 1)))
        store.openError = WorkspaceError.NotAWorkspace("/worlds/w1".toPath())
        viewModel.openProject(idOf(1))

        viewModel.dismissError()

        assertNull(viewModel.state.value.error)
    }
}
