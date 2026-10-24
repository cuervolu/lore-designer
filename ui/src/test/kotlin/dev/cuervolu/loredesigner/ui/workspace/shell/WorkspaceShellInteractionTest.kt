@file:OptIn(ExperimentalTestApi::class)

package dev.cuervolu.loredesigner.ui.workspace.shell

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.cuervolu.loredesigner.ui.AppTestHarness
import dev.cuervolu.loredesigner.ui.assertOnLauncher
import dev.cuervolu.loredesigner.ui.closeProjectFromMenu
import dev.cuervolu.loredesigner.ui.openProjectFromLauncher
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.menu_explorer
import dev.cuervolu.loredesigner.ui.resources.menu_log
import dev.cuervolu.loredesigner.ui.resources.menu_properties
import dev.cuervolu.loredesigner.ui.resources.menu_view
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_explorer_empty_title
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_log_empty
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_properties_empty_title
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_toggle_explorer
import dev.cuervolu.loredesigner.ui.waitForText
import dev.cuervolu.loredesigner.ui.workspace.shell.dock.boundsPx
import dev.cuervolu.loredesigner.ui.workspace.shell.dock.moveOver
import dev.cuervolu.loredesigner.ui.workspace.shell.dock.pressAndMove
import dev.cuervolu.loredesigner.ui.workspace.shell.dock.release
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Keyboard shortcuts, menus, status bar and docking acting on the one workspace state, through the production
 * window content. Visibility is observed through each region's empty-state text.
 */
class WorkspaceShellInteractionTest {
    private val app = AppTestHarness()

    @AfterTest
    fun tearDown() = app.close()

    private fun ComposeUiTest.openWorkspace() {
        with(app) { launch() }
        runBlocking { openProjectFromLauncher("/worlds/Embercourt") }
        waitForText("Embercourt")
    }

    private suspend fun ComposeUiTest.explorerShown() =
        shown(getString(Res.string.workspace_shell_explorer_empty_title))

    private suspend fun ComposeUiTest.propertiesShown() =
        shown(getString(Res.string.workspace_shell_properties_empty_title))

    private suspend fun ComposeUiTest.logShown() = shown(getString(Res.string.workspace_shell_log_empty))

    private fun ComposeUiTest.shown(text: String) = onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    private fun ComposeUiTest.shortcut(key: Key, shift: Boolean = false) {
        onRoot().performKeyInput {
            keyDown(Key.CtrlLeft)
            if (shift) keyDown(Key.ShiftLeft)
            keyDown(key)
            keyUp(key)
            if (shift) keyUp(Key.ShiftLeft)
            keyUp(Key.CtrlLeft)
        }
        waitForIdle()
    }

    private suspend fun ComposeUiTest.chooseFromViewMenu(item: StringResource) {
        onNodeWithText(getString(Res.string.menu_view)).performClick()
        onNodeWithText(getString(item)).performClick()
        waitForIdle()
    }

    @Test
    fun `each region has a shortcut that hides it and shows it again`() = runComposeUiTest {
        openWorkspace()
        assertEquals(listOf(true, true, false), listOf(explorerShown(), propertiesShown(), logShown()))

        shortcut(Key.B)
        shortcut(Key.P, shift = true)
        shortcut(Key.J)
        assertEquals(listOf(false, false, true), listOf(explorerShown(), propertiesShown(), logShown()))

        shortcut(Key.B)
        shortcut(Key.P, shift = true)
        shortcut(Key.J)
        assertEquals(listOf(true, true, false), listOf(explorerShown(), propertiesShown(), logShown()))
    }

    @Test
    fun `the View menu, the status bar and the shortcut all drive the same visibility`() = runComposeUiTest {
        openWorkspace()

        chooseFromViewMenu(Res.string.menu_explorer)
        assertEquals(false, explorerShown(), "hidden from the View menu")

        onNodeWithContentDescription(getString(Res.string.workspace_shell_toggle_explorer)).performClick()
        waitForIdle()
        assertEquals(true, explorerShown(), "shown again from the status bar")

        shortcut(Key.B)
        assertEquals(false, explorerShown(), "hidden from the shortcut")

        chooseFromViewMenu(Res.string.menu_explorer)
        assertEquals(true, explorerShown(), "the menu acts on the state the shortcut left")
    }

    @Test
    fun `the View menu hides and shows the other regions`() = runComposeUiTest {
        openWorkspace()

        chooseFromViewMenu(Res.string.menu_properties)
        chooseFromViewMenu(Res.string.menu_log)

        assertEquals(listOf(true, false, true), listOf(explorerShown(), propertiesShown(), logShown()))
    }

    @Test
    fun `view shortcuts and menu items do nothing before a project is open`() = runComposeUiTest {
        with(app) { launch() }
        waitForIdle()

        shortcut(Key.B)
        shortcut(Key.J)
        chooseFromViewMenu(Res.string.menu_explorer)
        assertOnLauncher()

        openProjectFromLauncher("/worlds/Embercourt")
        waitForText("Embercourt")
        assertEquals(listOf(true, true, false), listOf(explorerShown(), propertiesShown(), logShown()))
    }

    @Test
    fun `a project reopened after closing starts from the default layout`() = runComposeUiTest {
        openWorkspace()
        shortcut(Key.B)
        shortcut(Key.J)

        closeProjectFromMenu()
        waitForText("/worlds/Embercourt")
        openProjectFromLauncher("/worlds/Embercourt")
        waitForText("Embercourt")

        assertEquals(listOf(true, true, false), listOf(explorerShown(), propertiesShown(), logShown()))
    }

    @Test
    fun `shortcuts keep working after a tab drag is dropped`() = runComposeUiTest {
        openWorkspace()

        pressAndMove("dock-tab-explorer", dp = DRAG_DP, density = 1f)
        moveOver("dock-tab-explorer", boundsPx("dock-tab-properties").center)
        release("dock-tab-explorer")
        assertEquals(true, explorerShown(), "the explorer moved into the properties group and is still shown")

        shortcut(Key.B)

        assertEquals(false, explorerShown())
        assertEquals(true, propertiesShown())
    }

    @Test
    fun `shortcuts keep working after a drag is cancelled with escape`() = runComposeUiTest {
        openWorkspace()

        pressAndMove("dock-tab-explorer", dp = DRAG_DP, density = 1f)
        onRoot().performKeyInput { pressKey(Key.Escape) }
        waitForIdle()
        release("dock-tab-explorer")
        shortcut(Key.J)

        assertEquals(true, logShown())
        assertEquals(true, explorerShown(), "a cancelled drag leaves the layout alone")
    }

    private companion object {
        const val DRAG_DP = 8f
    }
}
