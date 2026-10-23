package dev.cuervolu.loredesigner.ui.commands

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppMenusTest {
    private val labels = AppMenuLabels(
        file = "File",
        closeProject = "Close project",
        settings = "Settings",
        exit = "Exit",
        window = "Window",
        view = "View",
        explorer = "Explorer",
        properties = "Properties",
        log = "Log",
    )
    private val calls = mutableListOf<String>()
    private val actions = AppMenuActions(
        onCloseWorkspace = { calls += "close" },
        onOpenSettings = { calls += "settings" },
        onToggleExplorer = { calls += "explorer" },
        onToggleProperties = { calls += "properties" },
        onToggleBottomPanel = { calls += "bottom" },
        onQuit = { calls += "quit" },
    )

    private fun menus(workspace: WorkspaceMenuState?, macOS: Boolean = false) =
        appMenus(labels, workspace, actions, macOS)

    private fun List<AppMenu>.item(id: String) =
        flatMap { it.entries }.filterIsInstance<MenuEntry.Item>().first { it.id == id }

    @Test
    fun `workspace commands are disabled without an open workspace`() {
        val menus = menus(workspace = null)

        assertFalse(menus.item("file.close-project").enabled)
        listOf("view.explorer", "view.properties", "view.log").forEach {
            assertFalse(menus.item(it).enabled, it)
        }
        assertTrue(menus.item(AppCommandIds.SETTINGS).enabled)
        assertTrue(menus.item(AppCommandIds.EXIT).enabled)
    }

    @Test
    fun `view toggles mirror which regions are visible`() {
        val menus =
            menus(WorkspaceMenuState(explorerVisible = true, propertiesVisible = false, bottomPanelVisible = true))

        assertEquals(true, menus.item("view.explorer").checked)
        assertEquals(false, menus.item("view.properties").checked)
        assertEquals(true, menus.item("view.log").checked)
        assertTrue(menus.item("view.explorer").enabled)
    }

    @Test
    fun `commands forward to their actions`() {
        val menus = menus(WorkspaceMenuState(true, true, true))

        listOf("file.close-project", AppCommandIds.SETTINGS, "view.explorer", "view.properties", "view.log")
            .forEach { menus.item(it).onClick() }
        menus.item(AppCommandIds.EXIT).onClick()

        assertEquals(listOf("close", "settings", "explorer", "properties", "bottom", "quit"), calls)
    }

    @Test
    fun `only macOS gets a window menu`() {
        assertNull(menus(null).firstOrNull { it.id == AppCommandIds.WINDOW_MENU })
        assertTrue(menus(null, macOS = true).any { it.id == AppCommandIds.WINDOW_MENU })
    }

    @Test
    fun `shortcut bindings skip disabled commands`() {
        val withoutWorkspace = menus(null).shortcutBindings()
        val withWorkspace = menus(WorkspaceMenuState(true, true, true)).shortcutBindings()

        assertFalse(AppShortcuts.ToggleExplorer in withoutWorkspace)
        assertTrue(AppShortcuts.Settings in withoutWorkspace)
        assertTrue(AppShortcuts.ToggleExplorer in withWorkspace)
    }

    @Test
    fun `shortcuts display per platform conventions`() {
        assertEquals("Ctrl+Shift+P", AppShortcuts.ToggleProperties.display(macOS = false))
        assertEquals("⇧⌘P", AppShortcuts.ToggleProperties.display(macOS = true))
        assertEquals("Ctrl+,", AppShortcuts.Settings.display(macOS = false))
    }
}
