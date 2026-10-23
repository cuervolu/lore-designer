package dev.cuervolu.loredesigner.ui.chrome

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.cuervolu.loredesigner.ui.commands.AppMenu
import dev.cuervolu.loredesigner.ui.commands.CommandShortcut
import dev.cuervolu.loredesigner.ui.commands.MenuEntry
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AppMenuBarTest {
    private val calls = mutableListOf<String>()

    private val menus = listOf(
        AppMenu(
            "file",
            "File",
            listOf(
                MenuEntry.Item("open", "Open thing", CommandShortcut('o'), onClick = { calls += "open" }),
                MenuEntry.Separator,
                MenuEntry.Item("locked", "Locked thing", enabled = false, onClick = { calls += "locked" }),
            ),
        ),
        AppMenu(
            "view",
            "View",
            listOf(MenuEntry.Item("toggle", "Show panel", checked = true, onClick = { calls += "toggle" })),
        ),
    )

    private fun androidx.compose.ui.test.ComposeUiTest.show() = setContent {
        LoreDesignerTheme(darkTheme = false) { AppMenuBar(menus) }
    }

    @Test
    fun `items stay hidden until their menu is opened`() = runComposeUiTest {
        show()

        onAllNodesWithText("Open thing").assertCountEquals(0)
        onNodeWithText("File").performClick()

        onNodeWithText("Open thing").assertExists()
        onNodeWithText("Ctrl+O").assertExists()
    }

    @Test
    fun `choosing an item runs it and closes the menu`() = runComposeUiTest {
        show()

        onNodeWithText("File").performClick()
        onNodeWithText("Open thing").performClick()
        waitForIdle()

        assertEquals(listOf("open"), calls)
        waitUntil { onAllNodesWithText("Open thing").fetchSemanticsNodes().isEmpty() }
    }

    @Test
    fun `a disabled item does nothing`() = runComposeUiTest {
        show()

        onNodeWithText("File").performClick()
        onNodeWithText("Locked thing").performClick()

        assertEquals(emptyList(), calls)
    }

    @Test
    fun `hovering a neighbouring title switches the open menu`() = runComposeUiTest {
        show()

        onNodeWithText("File").performClick()
        onNodeWithText("View").performMouseInput { moveTo(center) }

        waitUntil { onAllNodesWithText("Show panel").fetchSemanticsNodes().isNotEmpty() }
        waitUntil { onAllNodesWithText("Open thing").fetchSemanticsNodes().isEmpty() }
    }

    @Test
    fun `hovering does not open a menu while none is open`() = runComposeUiTest {
        show()

        onNodeWithText("View").performMouseInput { moveTo(center) }
        waitForIdle()

        onAllNodesWithText("Show panel").assertCountEquals(0)
    }
}
