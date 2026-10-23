package dev.cuervolu.loredesigner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.composeunstyled.DialogHost
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.launcher.DirectoryPicker
import dev.cuervolu.loredesigner.ui.launcher.FileKitDirectoryPicker
import dev.cuervolu.loredesigner.ui.launcher.LauncherScreen
import dev.cuervolu.loredesigner.ui.launcher.LauncherViewModel
import dev.cuervolu.loredesigner.ui.launcher.LocalProjectSystemActions
import dev.cuervolu.loredesigner.ui.launcher.ProjectSystemActions
import dev.cuervolu.loredesigner.ui.navigation.AppRoute
import dev.cuervolu.loredesigner.ui.notifications.LoreNotificationHost
import dev.cuervolu.loredesigner.ui.session.AppSessionState
import dev.cuervolu.loredesigner.ui.session.rememberAppSessionState
import dev.cuervolu.loredesigner.ui.settings.SettingsModal
import dev.cuervolu.loredesigner.ui.settings.SettingsViewModelFactory
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.workspace.shell.WorkspaceShell
import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.WorkspaceOpener
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspacesRegistry
import org.koin.compose.koinInject

/**
 * Localized application content. Session state comes from [session], which callers create above
 * any localization boundary so that recreating this subtree loses nothing.
 */
@Composable
fun LoreDesignerApp(
    modifier: Modifier = Modifier,
    session: AppSessionState = rememberAppSessionState(),
    createWorkspace: CreateWorkspace = koinInject(),
    workspaceOpener: WorkspaceOpener = koinInject(),
    recentWorkspaces: RecentWorkspacesRegistry = koinInject(),
    settingsViewModelFactory: SettingsViewModelFactory = koinInject(),
    pickDirectory: DirectoryPicker = FileKitDirectoryPicker,
    projectSystemActions: ProjectSystemActions = LocalProjectSystemActions.current,
) {
    val backStack = session.backStack

    LoreNotificationHost(modifier = modifier) {
        DialogHost(modifier = Modifier.fillMaxSize().background(Theme[colors][LoreColors.background])) {
            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<AppRoute.Launcher> {
                        LauncherScreen(
                            viewModel = viewModel {
                                LauncherViewModel(
                                    createWorkspace,
                                    workspaceOpener,
                                    recentWorkspaces,
                                    projectSystemActions,
                                )
                            },
                            pickDirectory = pickDirectory,
                            onWorkspaceOpened = session::openWorkspace,
                        )
                    }
                    entry<AppRoute.Workspace> { route ->
                        WorkspaceShell(
                            workspace = session.activeWorkspace?.takeIf {
                                it.config.id.toString() == route.workspaceId
                            },
                            state = session.shell,
                            onClose = session::closeWorkspace,
                        )
                    }
                },
            )

            val workspace = session.activeWorkspace
            CompositionLocalProvider(LocalViewModelStoreOwner provides session.viewModelStoreOwner) {
                SettingsModal(
                    visible = session.settingsModal.isOpen,
                    viewModel = viewModel(key = "settings:${workspace?.config?.id ?: "launcher"}") {
                        settingsViewModelFactory.create(workspace)
                    },
                    onDismiss = session.settingsModal::dismiss,
                    onWorkspaceChange = { updated ->
                        if (session.activeWorkspace?.config?.id == updated.config.id) session.activeWorkspace = updated
                    },
                )
            }
        }
    }
}
