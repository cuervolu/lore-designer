package dev.cuervolu.loredesigner.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import dev.cuervolu.loredesigner.core.settings.AppLanguage
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.ui.chrome.LauncherTitleBar
import dev.cuervolu.loredesigner.ui.i18n.ProvideAppLocale
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.launcher_action_new
import dev.cuervolu.loredesigner.ui.resources.launcher_action_open
import dev.cuervolu.loredesigner.ui.resources.launcher_browse
import dev.cuervolu.loredesigner.ui.resources.launcher_color_green
import dev.cuervolu.loredesigner.ui.resources.launcher_empty_welcome_title
import dev.cuervolu.loredesigner.ui.resources.launcher_error_invalid_name
import dev.cuervolu.loredesigner.ui.resources.launcher_error_missing_location
import dev.cuervolu.loredesigner.ui.resources.launcher_error_not_a_workspace
import dev.cuervolu.loredesigner.ui.resources.launcher_new_location_label
import dev.cuervolu.loredesigner.ui.resources.launcher_new_name_label
import dev.cuervolu.loredesigner.ui.resources.launcher_new_submit
import dev.cuervolu.loredesigner.ui.resources.launcher_open_path_label
import dev.cuervolu.loredesigner.ui.resources.launcher_open_submit
import dev.cuervolu.loredesigner.ui.resources.settings_close
import dev.cuervolu.loredesigner.ui.resources.settings_page_appearance
import dev.cuervolu.loredesigner.ui.resources.settings_page_general
import dev.cuervolu.loredesigner.ui.resources.settings_page_language
import dev.cuervolu.loredesigner.ui.resources.titlebar_settings
import dev.cuervolu.loredesigner.ui.resources.workspace_placeholder_close
import dev.cuervolu.loredesigner.ui.session.rememberAppSessionState
import dev.cuervolu.loredesigner.ui.settings.SettingsTestHarness
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.getString
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

// Strings are resolved from resources so the tests pass under any machine locale.
private val originalLocale: Locale = Locale.getDefault()

@OptIn(ExperimentalTestApi::class)
class LoreDesignerAppTest {
    private val settings = SettingsTestHarness()
    private val store = settings.workspaceStore

    @AfterTest
    fun restoreLocale() {
        Locale.setDefault(originalLocale)
    }

    /** Mirrors the desktop composition: session state above the locale boundary, localized UI below. */
    private fun ComposeUiTest.launchApp(pickedDirectory: Path? = null) {
        setContent {
            val appSettings by settings.repository.settings.collectAsState()
            val session = rememberAppSessionState()
            LoreDesignerTheme(darkTheme = false) {
                ProvideAppLocale(appSettings.language) {
                    Column {
                        LauncherTitleBar(onSettingsClick = session.settingsModal::open)
                        LoreDesignerApp(
                            session = session,
                            createWorkspace = store.createWorkspace(),
                            openWorkspace = store.openWorkspace(),
                            settingsViewModelFactory = settings.factory,
                            pickDirectory = { pickedDirectory },
                        )
                    }
                }
            }
        }
    }

    private suspend fun ComposeUiTest.openProject() {
        onAllNodesWithText(getString(Res.string.launcher_action_open)).onFirst().performClick()
        onNodeWithContentDescription(
            getString(Res.string.launcher_open_path_label),
        ).performTextInput("/worlds/Embercourt")
        onNodeWithText(getString(Res.string.launcher_open_submit)).performClick()
        waitUntil { onAllNodesWithText("Embercourt").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun `settings button on the launcher opens the modal without project settings`() = runComposeUiTest {
        launchApp()

        val appearance = getString(Res.string.settings_page_appearance)
        onNodeWithContentDescription(getString(Res.string.titlebar_settings)).performClick()

        waitUntil { onAllNodesWithText(appearance).fetchSemanticsNodes().isNotEmpty() }
        onAllNodesWithText(getString(Res.string.settings_page_general)).assertCountEquals(0)

        onNodeWithContentDescription(getString(Res.string.settings_close)).performClick()
        waitUntil { onAllNodesWithText(appearance).fetchSemanticsNodes().isEmpty() }
    }

    @Test
    fun `settings opened from a workspace include project settings`() = runComposeUiTest {
        launchApp()
        openProject()

        val general = getString(Res.string.settings_page_general)
        onNodeWithContentDescription(getString(Res.string.titlebar_settings)).performClick()

        waitUntil { onAllNodesWithText(general).fetchSemanticsNodes().isNotEmpty() }
        onNodeWithText("EMBERCOURT").assertExists()
    }

    @Test
    fun `changing the language relocalizes live and keeps the session`() = runComposeUiTest {
        settings.repository.update { it.copy(language = AppLanguage.ENGLISH) }
        launchApp()
        openProject()
        onNodeWithContentDescription(getString(Res.string.titlebar_settings)).performClick()
        onNodeWithText(getString(Res.string.settings_page_language)).performClick()
        val englishClose = getString(Res.string.workspace_placeholder_close)

        settings.repository.update { it.copy(language = AppLanguage.SPANISH) }
        waitForIdle()

        val spanishLanguagePage = getString(Res.string.settings_page_language)
        assertEquals("es", Locale.getDefault().language)
        assertNotEquals(englishClose, getString(Res.string.workspace_placeholder_close))
        // The workspace is still open, the modal still shows the Language page, now in Spanish.
        onNodeWithText(getString(Res.string.workspace_placeholder_close)).assertExists()
        onNodeWithText("Embercourt").assertExists()
        onAllNodesWithText(spanishLanguagePage).assertCountEquals(2)
        assertEquals(AppLanguage.SPANISH, settings.store.stored.language)

        settings.repository.update { it.copy(language = AppLanguage.ENGLISH) }
        waitForIdle()

        onNodeWithText(englishClose).assertExists()
    }

    @Test
    fun `launcher is the initial destination`() = runComposeUiTest {
        launchApp()

        onNodeWithText(getString(Res.string.launcher_empty_welcome_title)).assertExists()
    }

    @Test
    fun `opening a project navigates to the workspace and closing returns`() = runComposeUiTest {
        launchApp()

        onAllNodesWithText(getString(Res.string.launcher_action_open)).onFirst().performClick()
        onNodeWithContentDescription(
            getString(Res.string.launcher_open_path_label),
        ).performTextInput("/worlds/Embercourt")
        onNodeWithText(getString(Res.string.launcher_open_submit)).performClick()

        waitUntil { onAllNodesWithText("Embercourt").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(listOf("/worlds/Embercourt".toPath()), store.openedLocations)

        val welcome = getString(Res.string.launcher_empty_welcome_title)
        onNodeWithText(getString(Res.string.workspace_placeholder_close)).performClick()
        waitUntil { onAllNodesWithText(welcome).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun `failed open stays on the launcher with a message`() = runComposeUiTest {
        store.openError = WorkspaceError.NotAWorkspace("/tmp/notes".toPath())
        launchApp()

        onAllNodesWithText(getString(Res.string.launcher_action_open)).onFirst().performClick()
        onNodeWithContentDescription(getString(Res.string.launcher_open_path_label)).performTextInput("/tmp/notes")
        onNodeWithText(getString(Res.string.launcher_open_submit)).performClick()

        val message = getString(Res.string.launcher_error_not_a_workspace, "notes")
        waitUntil { onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty() }
        onAllNodesWithText(getString(Res.string.workspace_placeholder_close)).fetchSemanticsNodes().let {
            assertTrue(it.isEmpty(), "Navigated despite the failure")
        }
    }

    @Test
    fun `creating a project with a browsed location navigates to the workspace`() = runComposeUiTest {
        launchApp(pickedDirectory = "/worlds".toPath())

        onAllNodesWithText(getString(Res.string.launcher_action_new)).onFirst().performClick()
        onNodeWithContentDescription(getString(Res.string.launcher_new_name_label)).performTextInput("Thistlewood")
        onNodeWithText(getString(Res.string.launcher_new_location_label)).assertExists()
        onAllNodesWithText(getString(Res.string.launcher_browse)).onFirst().performClick()
        waitForIdle()
        onNodeWithText(getString(Res.string.launcher_new_submit)).performClick()

        waitUntil { onAllNodes(hasText("Thistlewood")).fetchSemanticsNodes().isNotEmpty() }
        assertEquals(listOf("/worlds/Thistlewood".toPath()), store.createdLocations)
    }

    @Test
    fun `failed create does not navigate`() = runComposeUiTest {
        store.createError = WorkspaceError.DestinationNotEmpty("/worlds/Thistlewood".toPath())
        launchApp()

        onAllNodesWithText(getString(Res.string.launcher_action_new)).onFirst().performClick()
        onNodeWithContentDescription(getString(Res.string.launcher_new_name_label)).performTextInput("Thistlewood")
        onNodeWithContentDescription(getString(Res.string.launcher_new_location_label)).performTextInput("/worlds")
        onNodeWithText(getString(Res.string.launcher_new_submit)).performClick()
        waitForIdle()

        assertEquals(1, store.createdLocations.size)
        assertTrue(
            onAllNodesWithText(getString(Res.string.workspace_placeholder_close)).fetchSemanticsNodes().isEmpty(),
        )
    }

    @Test
    fun `creating without a location shows a message and does not touch storage`() = runComposeUiTest {
        launchApp()

        onAllNodesWithText(getString(Res.string.launcher_action_new)).onFirst().performClick()
        onNodeWithContentDescription(getString(Res.string.launcher_new_name_label)).performTextInput("Thistlewood")
        onNodeWithText(getString(Res.string.launcher_new_submit)).performClick()

        val message = getString(Res.string.launcher_error_missing_location)
        waitUntil { onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty() }
        assertTrue(store.createdLocations.isEmpty())
    }

    @Test
    fun `creating with an invalid name shows a message and stays on the launcher`() = runComposeUiTest {
        launchApp()

        onAllNodesWithText(getString(Res.string.launcher_action_new)).onFirst().performClick()
        onNodeWithContentDescription(getString(Res.string.launcher_new_name_label)).performTextInput("a/b")
        onNodeWithContentDescription(getString(Res.string.launcher_new_location_label)).performTextInput("/worlds")
        onNodeWithText(getString(Res.string.launcher_new_submit)).performClick()

        val message = getString(Res.string.launcher_error_invalid_name)
        waitUntil { onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty() }
        assertTrue(store.createdLocations.isEmpty())
        assertTrue(
            onAllNodesWithText(getString(Res.string.workspace_placeholder_close)).fetchSemanticsNodes().isEmpty(),
        )
    }

    @Test
    fun `the selected color is saved with the new project`() = runComposeUiTest {
        launchApp()

        onAllNodesWithText(getString(Res.string.launcher_action_new)).onFirst().performClick()
        onNodeWithContentDescription(getString(Res.string.launcher_new_name_label)).performTextInput("Thistlewood")
        onNodeWithContentDescription(getString(Res.string.launcher_new_location_label)).performTextInput("/worlds")
        onNodeWithContentDescription(getString(Res.string.launcher_color_green)).performClick()
        onNodeWithText(getString(Res.string.launcher_new_submit)).performClick()

        waitUntil { store.createdConfigs.isNotEmpty() }
        assertEquals(ProjectColor.GREEN, store.createdConfigs.single().color)
    }

    @Test
    fun `cancelling the folder picker keeps the typed location`() = runComposeUiTest {
        launchApp(pickedDirectory = null)

        onAllNodesWithText(getString(Res.string.launcher_action_new)).onFirst().performClick()
        onNodeWithContentDescription(getString(Res.string.launcher_new_name_label)).performTextInput("Thistlewood")
        onNodeWithContentDescription(getString(Res.string.launcher_new_location_label)).performTextInput("/worlds")
        onAllNodesWithText(getString(Res.string.launcher_browse)).onFirst().performClick()
        waitForIdle()
        onNodeWithText(getString(Res.string.launcher_new_submit)).performClick()

        waitUntil { store.createdLocations.isNotEmpty() }
        assertEquals(listOf("/worlds/Thistlewood".toPath()), store.createdLocations)
    }

    @Test
    fun `ctrl+n opens the new project dialog`() = runComposeUiTest {
        val nameLabel = getString(Res.string.launcher_new_name_label)
        launchApp()
        waitForIdle()

        onRoot().performKeyInput {
            keyDown(Key.CtrlLeft)
            keyDown(Key.N)
            keyUp(Key.N)
            keyUp(Key.CtrlLeft)
        }

        waitUntil {
            onAllNodes(hasContentDescription(nameLabel)).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
