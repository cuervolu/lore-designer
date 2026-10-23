package dev.cuervolu.loredesigner.ui.workspace.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_back_to_launcher
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_center_empty_title
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_explorer_empty_title
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_log_empty
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_not_open_title
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_properties_empty_title
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_toggle_explorer
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_toggle_log
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_toggle_properties
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import dev.cuervolu.loredesigner.workspace.Workspace
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

@OptIn(ExperimentalTestApi::class)
class WorkspaceShellTest {
    private val workspace = Workspace(
        location = "/worlds/Embercourt".toPath(),
        config = ProjectConfig(version = 1, id = WorkspaceId(Uuid.random()), name = "Embercourt"),
    )

    private fun androidx.compose.ui.test.ComposeUiTest.show(
        workspace: Workspace? = this@WorkspaceShellTest.workspace,
        state: WorkspaceShellState = WorkspaceShellState(),
        onClose: () -> Unit = {},
    ) = setContent {
        LoreDesignerTheme(darkTheme = false) {
            Box(Modifier.size(1280.dp, 800.dp)) { WorkspaceShell(workspace, state, onClose) }
        }
    }

    @Test
    fun `every region shows its empty state`() = runComposeUiTest {
        show(state = WorkspaceShellState().apply { toggleBottomPanel() })

        onNodeWithText("Embercourt").assertExists()
        onNodeWithText(getString(Res.string.workspace_shell_explorer_empty_title)).assertExists()
        onNodeWithText(getString(Res.string.workspace_shell_center_empty_title)).assertExists()
        onNodeWithText(getString(Res.string.workspace_shell_properties_empty_title)).assertExists()
        onNodeWithText(getString(Res.string.workspace_shell_log_empty)).assertExists()
    }

    @Test
    fun `status bar buttons hide and show their regions`() = runComposeUiTest {
        val state = WorkspaceShellState()
        show(state = state)
        val explorerEmpty = getString(Res.string.workspace_shell_explorer_empty_title)

        onNodeWithContentDescription(getString(Res.string.workspace_shell_toggle_explorer)).performClick()
        waitForIdle()
        onNodeWithText(explorerEmpty).assertDoesNotExist()
        assertEquals(false, state.explorerVisible)

        onNodeWithContentDescription(getString(Res.string.workspace_shell_toggle_properties)).performClick()
        waitForIdle()
        onNodeWithText(getString(Res.string.workspace_shell_properties_empty_title)).assertDoesNotExist()

        onNodeWithContentDescription(getString(Res.string.workspace_shell_toggle_explorer)).performClick()
        waitForIdle()
        onNodeWithText(explorerEmpty).assertExists()
    }

    @Test
    fun `the log starts hidden and the status bar button reveals it`() = runComposeUiTest {
        val state = WorkspaceShellState()
        show(state = state)
        val logEmpty = getString(Res.string.workspace_shell_log_empty)

        onNodeWithText(logEmpty).assertDoesNotExist()
        assertEquals(false, state.bottomPanelVisible)

        onNodeWithContentDescription(getString(Res.string.workspace_shell_toggle_log)).performClick()
        waitForIdle()
        onNodeWithText(logEmpty).assertExists()
    }

    @Test
    fun `a missing workspace offers a way back`() = runComposeUiTest {
        var closed = 0
        show(workspace = null, onClose = { closed++ })

        onNodeWithText(getString(Res.string.workspace_shell_not_open_title)).assertExists()
        onNodeWithText(getString(Res.string.workspace_shell_back_to_launcher))
            .performClick()

        assertEquals(1, closed)
    }
}
