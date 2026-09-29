package dev.cuervolu.loredesigner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.composeunstyled.DialogHost
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.launcher.DirectoryPicker
import dev.cuervolu.loredesigner.ui.launcher.FileKitDirectoryPicker
import dev.cuervolu.loredesigner.ui.launcher.LauncherScreen
import dev.cuervolu.loredesigner.ui.launcher.LauncherViewModel
import dev.cuervolu.loredesigner.ui.navigation.AppRoute
import dev.cuervolu.loredesigner.ui.navigation.AppRouteSavedStateConfiguration
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.workspace.WorkspacePlaceholder
import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.OpenWorkspace
import dev.cuervolu.loredesigner.workspace.Workspace
import org.koin.compose.koinInject

@Composable
fun LoreDesignerApp(
    modifier: Modifier = Modifier,
    createWorkspace: CreateWorkspace = koinInject(),
    openWorkspace: OpenWorkspace = koinInject(),
    pickDirectory: DirectoryPicker = FileKitDirectoryPicker,
) {
    val backStack = rememberNavBackStack(AppRouteSavedStateConfiguration, AppRoute.Launcher)
    var activeWorkspace by remember { mutableStateOf<Workspace?>(null) }

    DialogHost(modifier = modifier.fillMaxSize().background(Theme[colors][LoreColors.background])) {
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
                        viewModel = viewModel { LauncherViewModel(createWorkspace, openWorkspace) },
                        pickDirectory = pickDirectory,
                        onWorkspaceOpened = { workspace ->
                            activeWorkspace = workspace
                            backStack.add(AppRoute.Workspace(workspace.config.id.toString()))
                        },
                    )
                }
                entry<AppRoute.Workspace> { route ->
                    WorkspacePlaceholder(
                        workspace = activeWorkspace?.takeIf { it.config.id.toString() == route.workspaceId },
                        onClose = {
                            activeWorkspace = null
                            backStack.remove(route)
                        },
                    )
                }
            },
        )
    }
}
