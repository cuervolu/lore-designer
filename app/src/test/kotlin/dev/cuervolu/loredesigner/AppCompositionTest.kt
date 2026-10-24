package dev.cuervolu.loredesigner

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.core.settings.AppLanguage
import dev.cuervolu.loredesigner.platform.system.DesktopFolderOpener
import dev.cuervolu.loredesigner.system.DesktopProjectSystemActions
import dev.cuervolu.loredesigner.ui.LoreDesignerWindowContent
import dev.cuervolu.loredesigner.ui.chrome.windowTitle
import dev.cuervolu.loredesigner.ui.launcher.LocalProjectSystemActions
import dev.cuervolu.loredesigner.ui.session.rememberAppSessionState
import dev.cuervolu.loredesigner.ui.settings.data.ApplicationSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.util.Locale
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

// The resources of `ui` are not visible from this module, so the English UI strings are repeated here.
private const val NEW_PROJECT = "New project"
private const val PROJECT_NAME_LABEL = "Project name"
private const val LOCATION_LABEL = "Location"
private const val CREATE_PROJECT = "Create project"
private const val NO_DOCUMENT = "No document selected"
private const val FILE_MENU = "File"
private const val CLOSE_PROJECT = "Close project"
private const val SETTINGS_MENU_ITEM = "Settings…"
private const val GENERAL_PAGE = "General"
private const val WAIT_MILLIS = 10_000L

/**
 * Composes the production window content ([LoreDesignerWindowContent], what `LoreDesignerDesktopApp`
 * renders inside its native window) on the real Koin graph and real storage. Only the native window
 * is missing: it cannot exist without a display.
 */
@OptIn(ExperimentalTestApi::class)
class AppCompositionTest {
    private val originalLocale = Locale.getDefault()

    // The app depends on coroutines-swing, which makes Main the AWT event queue; the test body does not run there.
    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun useTestMainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun restore() {
        Dispatchers.resetMain()
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `creating a project through the assembled app writes it to disk and lists it after closing`() =
        runComposeUiTest {
            ScratchDirectory().use { scratch ->
                val projects = scratch.root.resolve("projects").createDirectories()
                AssembledApp.start(scratch.root.resolve("appdata")).use { app ->
                    val settings = app.koin.get<ApplicationSettingsRepository>()
                    settings.update { it.copy(language = AppLanguage.ENGLISH) }
                    val folderOpener = app.koin.get<DesktopFolderOpener>()
                    val logger = app.koin.get<Logger>()

                    setContent {
                        val appSettings by settings.settings.collectAsState()
                        val session = rememberAppSessionState()
                        val clipboard = LocalClipboard.current
                        val systemActions = remember(clipboard) {
                            DesktopProjectSystemActions(folderOpener, clipboard, logger.withTag("System"))
                        }
                        CompositionLocalProvider(LocalProjectSystemActions provides systemActions) {
                            LoreDesignerWindowContent(
                                session = session,
                                title = windowTitle(null, session.activeWorkspace?.config?.name, "Lore Designer"),
                                darkTheme = false,
                                language = appSettings.language,
                                macOS = false,
                                onQuit = {},
                            )
                        }
                    }

                    onAllNodesWithText(NEW_PROJECT).onFirst().performClick()
                    onNodeWithContentDescription(PROJECT_NAME_LABEL).performTextInput("Embercourt")
                    onNodeWithContentDescription(LOCATION_LABEL).performTextInput(projects.toString())
                    onNodeWithText(CREATE_PROJECT).performClick()

                    waitUntil(timeoutMillis = WAIT_MILLIS) {
                        onAllNodesWithText(NO_DOCUMENT).fetchSemanticsNodes().isNotEmpty()
                    }
                    val projectFile = projects.resolve("Embercourt/project.lore")
                    assertTrue(projectFile.exists(), "Creating a project must write project.lore")
                    assertTrue("Embercourt" in projectFile.readText(), "project.lore must hold the project name")

                    onNodeWithText(FILE_MENU).performClick()
                    onNodeWithText(SETTINGS_MENU_ITEM).performClick()
                    waitUntil(timeoutMillis = WAIT_MILLIS) {
                        onAllNodesWithText(GENERAL_PAGE).fetchSemanticsNodes().isNotEmpty()
                    }
                    onNodeWithContentDescription("Close settings").performClick()
                    waitUntil(timeoutMillis = WAIT_MILLIS) {
                        onAllNodesWithText(GENERAL_PAGE).fetchSemanticsNodes().isEmpty()
                    }

                    onNodeWithText(FILE_MENU).performClick()
                    onNodeWithText(CLOSE_PROJECT).performClick()
                    waitUntil(timeoutMillis = WAIT_MILLIS) {
                        onAllNodesWithText(projects.resolve("Embercourt").toString()).fetchSemanticsNodes().isNotEmpty()
                    }
                    onAllNodesWithText(NO_DOCUMENT).assertCountEquals(0)
                }
            }
        }
}
