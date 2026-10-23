package dev.cuervolu.loredesigner.ui.commands

/** Labels already resolved for the active locale; native menus cannot resolve resources themselves. */
data class AppMenuLabels(
    val file: String,
    val closeProject: String,
    val settings: String,
    val exit: String,
    val window: String,
    val view: String,
    val explorer: String,
    val properties: String,
    val log: String,
)

/** Which shell regions are visible; null at [appMenus] means no workspace is open. */
data class WorkspaceMenuState(
    val explorerVisible: Boolean,
    val propertiesVisible: Boolean,
    val bottomPanelVisible: Boolean,
)

data class AppMenuActions(
    val onCloseWorkspace: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onToggleExplorer: () -> Unit,
    val onToggleProperties: () -> Unit,
    val onToggleBottomPanel: () -> Unit,
    val onQuit: () -> Unit,
)

/** Ids the native macOS menu relocates into the application menu. */
object AppCommandIds {
    const val SETTINGS = "file.settings"
    const val EXIT = "file.exit"
    const val WINDOW_MENU = "window"
}

object AppShortcuts {
    val Settings = CommandShortcut(',')
    val ToggleExplorer = CommandShortcut('b')
    val ToggleProperties = CommandShortcut('p', shift = true)
    val ToggleLog = CommandShortcut('j')
    val Exit = CommandShortcut('q')
    val CloseProject = CommandShortcut('w', shift = true)
}

/**
 * Builds the application menus. On [macOS] the Window menu is added (the system fills it in) and the
 * Settings and Exit commands stay here for the native adapter to move into the application menu.
 */
fun appMenus(
    labels: AppMenuLabels,
    workspace: WorkspaceMenuState?,
    actions: AppMenuActions,
    macOS: Boolean,
): List<AppMenu> {
    val file = buildList {
        add(
            MenuEntry.Item(
                id = "file.close-project",
                title = labels.closeProject,
                shortcut = AppShortcuts.CloseProject,
                enabled = workspace != null,
                onClick = actions.onCloseWorkspace,
            ),
        )
        add(MenuEntry.Separator)
        add(
            MenuEntry.Item(
                id = AppCommandIds.SETTINGS,
                title = labels.settings,
                shortcut = AppShortcuts.Settings,
                onClick = actions.onOpenSettings,
            ),
        )
        add(MenuEntry.Separator)
        add(
            MenuEntry.Item(
                id = AppCommandIds.EXIT,
                title = labels.exit,
                shortcut = AppShortcuts.Exit,
                onClick = actions.onQuit,
            ),
        )
    }
    val view = listOf(
        MenuEntry.Item(
            id = "view.explorer",
            title = labels.explorer,
            shortcut = AppShortcuts.ToggleExplorer,
            enabled = workspace != null,
            checked = workspace?.explorerVisible ?: false,
            onClick = actions.onToggleExplorer,
        ),
        MenuEntry.Item(
            id = "view.properties",
            title = labels.properties,
            shortcut = AppShortcuts.ToggleProperties,
            enabled = workspace != null,
            checked = workspace?.propertiesVisible ?: false,
            onClick = actions.onToggleProperties,
        ),
        MenuEntry.Item(
            id = "view.log",
            title = labels.log,
            shortcut = AppShortcuts.ToggleLog,
            enabled = workspace != null,
            checked = workspace?.bottomPanelVisible ?: false,
            onClick = actions.onToggleBottomPanel,
        ),
    )
    val menus = listOf(AppMenu("file", labels.file, file), AppMenu("view", labels.view, view))
    return if (macOS) menus + AppMenu(AppCommandIds.WINDOW_MENU, labels.window, emptyList()) else menus
}

/** Every enabled item bound to a shortcut, for hosts that must dispatch key presses themselves. */
fun List<AppMenu>.shortcutBindings(): Map<CommandShortcut, () -> Unit> = buildMap {
    for (menu in this@shortcutBindings) {
        for (entry in menu.entries) {
            if (entry is MenuEntry.Item && entry.enabled && entry.shortcut != null) put(entry.shortcut, entry.onClick)
        }
    }
}
