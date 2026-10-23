package dev.cuervolu.loredesigner.ui.commands

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

private fun CommandShortcut.composeKey(): Key = when (key) {
    ',' -> Key.Comma
    in 'a'..'z' -> Key(Key.A.keyCode + (key - 'a'))
    else -> Key.Unknown
}

/**
 * Runs the enabled menu commands from their Ctrl-based shortcuts. Only for platforms that draw their
 * own menu: the macOS native menu bar already handles its key equivalents.
 */
fun Modifier.commandShortcuts(menus: List<AppMenu>): Modifier {
    val bindings = menus.shortcutBindings().mapKeys { (shortcut, _) -> shortcut to shortcut.composeKey() }
    return onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown || !event.isCtrlPressed) return@onPreviewKeyEvent false
        val action = bindings.entries.firstOrNull { (id, _) ->
            id.second == event.key && id.first.shift == event.isShiftPressed
        }?.value
        action?.invoke()
        action != null
    }
}
