package dev.cuervolu.loredesigner.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dev.cuervolu.loredesigner.ui.chrome.windowTitle
import dev.cuervolu.loredesigner.ui.launcher.FakeProjectSystemActions
import dev.cuervolu.loredesigner.ui.launcher.FakeRecentWorkspacesRegistry
import dev.cuervolu.loredesigner.ui.launcher.LocalProjectSystemActions
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.launcher_action_new
import dev.cuervolu.loredesigner.ui.resources.launcher_action_open
import dev.cuervolu.loredesigner.ui.resources.launcher_new_location_label
import dev.cuervolu.loredesigner.ui.resources.launcher_new_name_label
import dev.cuervolu.loredesigner.ui.resources.launcher_new_submit
import dev.cuervolu.loredesigner.ui.resources.launcher_open_path_label
import dev.cuervolu.loredesigner.ui.resources.launcher_open_submit
import dev.cuervolu.loredesigner.ui.resources.menu_close_project
import dev.cuervolu.loredesigner.ui.resources.menu_file
import dev.cuervolu.loredesigner.ui.resources.menu_settings
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_status_no_document
import dev.cuervolu.loredesigner.ui.session.rememberAppSessionState
import dev.cuervolu.loredesigner.ui.settings.SettingsTestHarness
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspacesRegistry
import okio.Path
import org.jetbrains.compose.resources.getString
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/**
 * Runs the production window content ([LoreDesignerWindowContent]) against fakes supplied through
 * Koin, the way the desktop app supplies the real implementations.
 */
@OptIn(ExperimentalTestApi::class)
internal class AppTestHarness {
    val settings = SettingsTestHarness()
    val store = settings.workspaceStore
    val registry = FakeRecentWorkspacesRegistry()
    val systemActions = FakeProjectSystemActions()

    private val koin = koinApplication {
        allowOverride(false)
        modules(
            module {
                single { store.createWorkspace() }
                single { store.opener(registry) }
                single<RecentWorkspacesRegistry> { registry }
                single { settings.factory }
            },
        )
    }

    fun ComposeUiTest.launch(pickedDirectory: Path? = null) {
        setContent {
            val appSettings by settings.repository.settings.collectAsState()
            val session = rememberAppSessionState()
            KoinIsolatedContext(koin) {
                CompositionLocalProvider(LocalProjectSystemActions provides systemActions) {
                    LoreDesignerWindowContent(
                        session = session,
                        title = windowTitle(
                            document = null,
                            project = session.activeWorkspace?.config?.name,
                            appName = APP_NAME,
                        ),
                        darkTheme = false,
                        language = appSettings.language,
                        macOS = false,
                        onQuit = {},
                        pickDirectory = { pickedDirectory },
                    )
                }
            }
        }
    }

    fun close() {
        koin.close()
    }

    private companion object {
        const val APP_NAME = "Lore Designer"
    }
}

/** The launcher is showing its actions and the workspace shell is not. */
@OptIn(ExperimentalTestApi::class)
internal suspend fun ComposeUiTest.assertOnLauncher() {
    onAllNodesWithText(getString(Res.string.launcher_action_open)).onFirst().assertExists()
    onAllNodesWithText(getString(Res.string.workspace_shell_status_no_document)).assertCountEquals(0)
}

/** The workspace shell is showing, titled [projectName], and the launcher is gone. */
@OptIn(ExperimentalTestApi::class)
internal suspend fun ComposeUiTest.assertOnWorkspace(projectName: String) {
    onNodeWithText(getString(Res.string.workspace_shell_status_no_document)).assertExists()
    // Shown by the title bar and by the shell itself.
    onAllNodesWithText(projectName).onFirst().assertExists()
    onAllNodesWithText(getString(Res.string.launcher_action_open)).assertCountEquals(0)
}

@OptIn(ExperimentalTestApi::class)
internal suspend fun ComposeUiTest.openProjectFromLauncher(path: String) {
    onAllNodesWithText(getString(Res.string.launcher_action_open)).onFirst().performClick()
    onNodeWithContentDescription(getString(Res.string.launcher_open_path_label)).performTextInput(path)
    onNodeWithText(getString(Res.string.launcher_open_submit)).performClick()
}

/** Opens the New project dialog and fills the name; [location] is typed only when given. */
@OptIn(ExperimentalTestApi::class)
internal suspend fun ComposeUiTest.fillNewProject(name: String, location: String? = null) {
    onAllNodesWithText(getString(Res.string.launcher_action_new)).onFirst().performClick()
    onNodeWithContentDescription(getString(Res.string.launcher_new_name_label)).performTextInput(name)
    if (location != null) {
        onNodeWithContentDescription(getString(Res.string.launcher_new_location_label)).performTextInput(location)
    }
}

@OptIn(ExperimentalTestApi::class)
internal suspend fun ComposeUiTest.submitNewProject() {
    onNodeWithText(getString(Res.string.launcher_new_submit)).performClick()
}

@OptIn(ExperimentalTestApi::class)
internal suspend fun ComposeUiTest.closeProjectFromMenu() {
    onNodeWithText(getString(Res.string.menu_file)).performClick()
    onNodeWithText(getString(Res.string.menu_close_project)).performClick()
}

@OptIn(ExperimentalTestApi::class)
internal suspend fun ComposeUiTest.openSettingsFromMenu() {
    onNodeWithText(getString(Res.string.menu_file)).performClick()
    onNodeWithText(getString(Res.string.menu_settings)).performClick()
}

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.waitForText(text: String) {
    waitUntil { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
}

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.waitUntilTextGone(text: String) {
    waitUntil { onAllNodesWithText(text).fetchSemanticsNodes().isEmpty() }
}
