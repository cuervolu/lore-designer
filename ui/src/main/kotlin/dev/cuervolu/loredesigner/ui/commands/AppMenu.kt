package dev.cuervolu.loredesigner.ui.commands

/**
 * Platform-neutral description of the application menu. macOS renders it natively and the other
 * platforms draw it inside the title bar, so both always offer the same commands in the same state.
 */
data class AppMenu(val id: String, val title: String, val entries: List<MenuEntry>)

sealed interface MenuEntry {
    /** [checked] is null for a plain command; otherwise the item is a toggle showing that state. */
    data class Item(
        val id: String,
        val title: String,
        val shortcut: CommandShortcut? = null,
        val enabled: Boolean = true,
        val checked: Boolean? = null,
        val onClick: () -> Unit,
    ) : MenuEntry

    data object Separator : MenuEntry
}

/** A shortcut on the platform's primary modifier (Cmd on macOS, Ctrl elsewhere). */
data class CommandShortcut(val key: Char, val shift: Boolean = false) {
    fun display(macOS: Boolean): String {
        val label = key.uppercaseChar()
        return if (macOS) "${if (shift) "⇧" else ""}⌘$label" else "Ctrl+${if (shift) "Shift+" else ""}$label"
    }
}
