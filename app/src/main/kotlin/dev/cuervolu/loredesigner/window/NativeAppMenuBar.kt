package dev.cuervolu.loredesigner.window

import androidx.compose.runtime.Composable
import dev.cuervolu.loredesigner.ui.commands.AppCommandIds
import dev.cuervolu.loredesigner.ui.commands.AppMenu
import dev.cuervolu.loredesigner.ui.commands.CommandShortcut
import dev.cuervolu.loredesigner.ui.commands.MenuEntry
import dev.nucleusframework.menu.macos.NativeKeyShortcut
import dev.nucleusframework.menu.macos.NativeMenuBar
import dev.nucleusframework.menu.macos.NativeMenuScope

/**
 * Renders [menus] as the macOS system menu bar. Settings and Exit move into the application menu,
 * where macOS users expect them, and the Window menu is handed to the system to populate.
 *
 * Item callbacks reach us on the Compose UI thread (Nucleus dispatches them through
 * `Dispatchers.Main`, which is the Tao main thread under the Tao backend), so they can update
 * Compose state directly.
 */
@Composable
internal fun NativeAppMenuBar(menus: List<AppMenu>, appName: String) {
    val items = menus.flatMap { it.entries }.filterIsInstance<MenuEntry.Item>()
    val appMenuItems = listOfNotNull(
        items.firstOrNull { it.id == AppCommandIds.SETTINGS },
        items.firstOrNull { it.id == AppCommandIds.EXIT },
    )
    NativeMenuBar {
        Menu(appName) {
            appMenuItems.firstOrNull { it.id == AppCommandIds.SETTINGS }?.let { item(it) }
            Separator()
            appMenuItems.firstOrNull { it.id == AppCommandIds.EXIT }?.let { item(it) }
        }
        menus.forEach { menu ->
            if (menu.id == AppCommandIds.WINDOW_MENU) {
                MenuWindow(menu.title)
            } else {
                val entries = tidy(menu.entries.filterNot { it is MenuEntry.Item && it in appMenuItems })
                Menu(menu.title) { entries.forEach { entry -> render(entry) } }
            }
        }
    }
}

private fun NativeMenuScope.render(entry: MenuEntry) = when (entry) {
    MenuEntry.Separator -> Separator()
    is MenuEntry.Item -> item(entry)
}

private fun NativeMenuScope.item(item: MenuEntry.Item) {
    val shortcut = item.shortcut?.toNative()
    val checked = item.checked
    if (checked == null) {
        Item(item.title, enabled = item.enabled, shortcut = shortcut, onClick = item.onClick)
    } else {
        CheckboxItem(
            text = item.title,
            checked = checked,
            onCheckedChange = { item.onClick() },
            enabled = item.enabled,
            shortcut = shortcut,
        )
    }
}

private fun CommandShortcut.toNative() = NativeKeyShortcut(key.toString(), shift = shift)

/** Drops separators left leading, trailing or doubled after relocating items out of a menu. */
private fun tidy(entries: List<MenuEntry>): List<MenuEntry> = entries.fold(emptyList<MenuEntry>()) { acc, entry ->
    if (entry == MenuEntry.Separator && (acc.isEmpty() || acc.last() == MenuEntry.Separator)) acc else acc + entry
}.dropLastWhile { it == MenuEntry.Separator }
