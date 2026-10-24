@file:OptIn(ExperimentalTestApi::class)

package dev.cuervolu.loredesigner.ui.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.cuervolu.loredesigner.core.settings.AppLanguage
import dev.cuervolu.loredesigner.core.settings.ThemePreference
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.ui.AppTestHarness
import dev.cuervolu.loredesigner.ui.closeProjectFromMenu
import dev.cuervolu.loredesigner.ui.openProjectFromLauncher
import dev.cuervolu.loredesigner.ui.openSettingsFromMenu
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.launcher_color_green
import dev.cuervolu.loredesigner.ui.resources.menu_file
import dev.cuervolu.loredesigner.ui.resources.settings_close
import dev.cuervolu.loredesigner.ui.resources.settings_diagnostic_logging
import dev.cuervolu.loredesigner.ui.resources.settings_language
import dev.cuervolu.loredesigner.ui.resources.settings_language_spanish
import dev.cuervolu.loredesigner.ui.resources.settings_open_folder_failed
import dev.cuervolu.loredesigner.ui.resources.settings_open_logs
import dev.cuervolu.loredesigner.ui.resources.settings_page_appearance
import dev.cuervolu.loredesigner.ui.resources.settings_page_diagnostics
import dev.cuervolu.loredesigner.ui.resources.settings_page_general
import dev.cuervolu.loredesigner.ui.resources.settings_page_language
import dev.cuervolu.loredesigner.ui.resources.settings_project_name
import dev.cuervolu.loredesigner.ui.resources.settings_search_placeholder
import dev.cuervolu.loredesigner.ui.resources.settings_theme
import dev.cuervolu.loredesigner.ui.resources.settings_theme_dark
import dev.cuervolu.loredesigner.ui.resources.titlebar_settings
import dev.cuervolu.loredesigner.ui.waitForText
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Settings used the way a user does, checking what the rest of the application then shows or stores. */
class SettingsFlowTest {
    private val app = AppTestHarness()
    private val settings = app.settings
    private val originalLocale: Locale = Locale.getDefault()

    @AfterTest
    fun tearDown() {
        app.close()
        Locale.setDefault(originalLocale)
    }

    private fun ComposeUiTest.openLauncherSettings(page: StringResourceHolder) {
        launchInEnglish()
        runBlocking {
            onNodeWithContentDescription(getString(Res.string.titlebar_settings)).performClick()
            onAllNodesWithText(getString(page.resource)).onFirst().performClick()
        }
    }

    // Strings come from resources, so pin the language instead of depending on the machine's.
    private fun ComposeUiTest.launchInEnglish() {
        settings.repository.update { it.copy(language = AppLanguage.ENGLISH) }
        with(app) { launch() }
    }

    private fun ComposeUiTest.openProject() {
        launchInEnglish()
        runBlocking { openProjectFromLauncher("/worlds/Embercourt") }
        waitForText("Embercourt")
    }

    private fun ComposeUiTest.windowBackground(): Color {
        val image = onRoot().captureToImage().toPixelMap()
        return image[image.width / 2, image.height - 2]
    }

    private fun Color.luminance() = 0.299f * red + 0.587f * green + 0.114f * blue

    @Test
    fun `choosing the dark theme repaints the window and is stored`() = runComposeUiTest {
        openLauncherSettings(StringResourceHolder(Res.string.settings_page_appearance))
        val before = windowBackground().luminance()

        runBlocking {
            onNodeWithContentDescription(getString(Res.string.settings_theme)).performClick()
            onNodeWithText(getString(Res.string.settings_theme_dark)).performClick()
        }
        waitForIdle()

        assertEquals(ThemePreference.DARK, settings.store.stored.theme)
        assertTrue(windowBackground().luminance() < before, "the window must get darker")
    }

    @Test
    fun `choosing a language relocalizes the menus and is stored`() = runComposeUiTest {
        openProject()
        runBlocking { openSettingsFromMenu() }
        val englishFile = runBlocking { getString(Res.string.menu_file) }
        runBlocking {
            onNodeWithText(getString(Res.string.settings_page_language)).performClick()
            onNodeWithContentDescription(getString(Res.string.settings_language)).performClick()
            onNodeWithText(getString(Res.string.settings_language_spanish)).performClick()
        }
        waitForIdle()

        assertEquals(AppLanguage.SPANISH, settings.store.stored.language)
        val spanishFile = runBlocking { getString(Res.string.menu_file) }
        assertTrue(englishFile != spanishFile)
        onNodeWithText(spanishFile).assertExists()
    }

    @Test
    fun `renaming the project from settings updates the window and the launcher entry`() = runComposeUiTest {
        openProject()
        runBlocking { openSettingsFromMenu() }
        runBlocking { onAllNodesWithText(getString(Res.string.settings_page_general)).onFirst().performClick() }

        val name = runBlocking { getString(Res.string.settings_project_name) }
        onNodeWithContentDescription(name).performClick()
        onNodeWithContentDescription(name).performTextReplacement("Embercourt Reborn")
        // Leaving the field commits the edit, as when a user moves on to the search box.
        runBlocking { onNodeWithContentDescription(getString(Res.string.settings_search_placeholder)).performClick() }

        waitUntil { settings.workspaceStore.updatedConfigs.isNotEmpty() }
        assertEquals("Embercourt Reborn", settings.workspaceStore.updatedConfigs.single().name)
        runBlocking { onNodeWithContentDescription(getString(Res.string.settings_close)).performClick() }
        waitForText("Embercourt Reborn")

        runBlocking { closeProjectFromMenu() }
        waitForText("/worlds/Embercourt")
        onNodeWithText("Embercourt Reborn").assertExists()
    }

    @Test
    fun `recolouring the project from settings is saved and shown in the launcher`() = runComposeUiTest {
        openProject()
        runBlocking { openSettingsFromMenu() }
        runBlocking { onAllNodesWithText(getString(Res.string.settings_page_general)).onFirst().performClick() }

        runBlocking { onNodeWithContentDescription(getString(Res.string.launcher_color_green)).performClick() }

        waitUntil { settings.workspaceStore.updatedConfigs.isNotEmpty() }
        assertEquals(ProjectColor.GREEN, settings.workspaceStore.updatedConfigs.single().color)
        runBlocking { onNodeWithContentDescription(getString(Res.string.settings_close)).performClick() }
        runBlocking { closeProjectFromMenu() }
        waitForText("/worlds/Embercourt")
        assertEquals(ProjectColor.GREEN, app.registry.workspaces.value.single().lastKnownColor)
    }

    @Test
    fun `turning diagnostic logging on reaches the logging environment`() = runComposeUiTest {
        openLauncherSettings(StringResourceHolder(Res.string.settings_page_diagnostics))

        runBlocking { onNodeWithContentDescription(getString(Res.string.settings_diagnostic_logging)).performClick() }
        waitForIdle()

        assertEquals(true, settings.store.stored.diagnosticLogging)
        assertEquals(listOf(true), settings.environment.diagnosticLoggingChanges)
    }

    @Test
    fun `open logs asks the environment for the logs folder and reports a failure`() = runComposeUiTest {
        settings.environment.openSucceeds = false
        openLauncherSettings(StringResourceHolder(Res.string.settings_page_diagnostics))

        runBlocking { onNodeWithText(getString(Res.string.settings_open_logs)).performClick() }
        waitForIdle()

        assertEquals(listOf(settings.environment.logsDirectory), settings.environment.openedFolders)
        runBlocking {
            onNodeWithText(getString(Res.string.settings_open_folder_failed, settings.environment.logsDirectory))
                .assertExists()
        }
    }
}

/** Keeps the page choice readable at the call site. */
private class StringResourceHolder(val resource: StringResource)
