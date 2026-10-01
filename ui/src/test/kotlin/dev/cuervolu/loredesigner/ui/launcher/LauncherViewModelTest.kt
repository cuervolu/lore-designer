package dev.cuervolu.loredesigner.ui.launcher

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.workspace.WorkspaceError
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LauncherViewModelTest {
    private val store = FakeWorkspaceStore()
    private val viewModel =
        LauncherViewModel(store.createWorkspace(), store.openWorkspace(), homeDirectory = { "/home/writer" })

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
    fun `invalid name is reported by the use case, not pre-validated`() {
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
}
