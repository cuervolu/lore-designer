package dev.cuervolu.loredesigner.ui.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.cuervolu.loredesigner.ui.commands.AppMenu
import dev.cuervolu.loredesigner.ui.commands.AppMenuActions
import dev.cuervolu.loredesigner.ui.commands.AppMenuLabels
import dev.cuervolu.loredesigner.ui.commands.WorkspaceMenuState
import dev.cuervolu.loredesigner.ui.commands.appMenus
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.menu_close_project
import dev.cuervolu.loredesigner.ui.resources.menu_exit
import dev.cuervolu.loredesigner.ui.resources.menu_explorer
import dev.cuervolu.loredesigner.ui.resources.menu_file
import dev.cuervolu.loredesigner.ui.resources.menu_log
import dev.cuervolu.loredesigner.ui.resources.menu_properties
import dev.cuervolu.loredesigner.ui.resources.menu_quit
import dev.cuervolu.loredesigner.ui.resources.menu_settings
import dev.cuervolu.loredesigner.ui.resources.menu_view
import dev.cuervolu.loredesigner.ui.resources.menu_window
import org.jetbrains.compose.resources.stringResource

/**
 * Application menus for the current session. This is the only place that maps session state to the
 * session-agnostic menu model; both the native and the Compose menu bars render its result.
 *
 * Call it under the same localization boundary as the UI so labels follow the language setting.
 */
@Composable
fun rememberAppMenus(session: AppSessionState, onQuit: () -> Unit, macOS: Boolean): List<AppMenu> {
    val labels = AppMenuLabels(
        file = stringResource(Res.string.menu_file),
        closeProject = stringResource(Res.string.menu_close_project),
        settings = stringResource(Res.string.menu_settings),
        exit = stringResource(if (macOS) Res.string.menu_quit else Res.string.menu_exit),
        window = stringResource(Res.string.menu_window),
        view = stringResource(Res.string.menu_view),
        explorer = stringResource(Res.string.menu_explorer),
        properties = stringResource(Res.string.menu_properties),
        log = stringResource(Res.string.menu_log),
    )
    val shell = session.shell
    val actions = remember(session, shell, onQuit) {
        AppMenuActions(
            onCloseWorkspace = session::closeWorkspace,
            onOpenSettings = session.settingsModal::open,
            onToggleExplorer = shell::toggleExplorer,
            onToggleProperties = shell::toggleProperties,
            onToggleBottomPanel = shell::toggleBottomPanel,
            onQuit = onQuit,
        )
    }
    val workspace = if (session.activeWorkspace == null) {
        null
    } else {
        WorkspaceMenuState(shell.explorerVisible, shell.propertiesVisible, shell.bottomPanelVisible)
    }
    return appMenus(labels, workspace, actions, macOS)
}
