package dev.cuervolu.loredesigner.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.cuervolu.loredesigner.core.settings.AppLanguage
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.launcher_browse
import dev.cuervolu.loredesigner.ui.resources.launcher_color_green
import dev.cuervolu.loredesigner.ui.resources.launcher_empty_welcome_title
import dev.cuervolu.loredesigner.ui.resources.launcher_error_destination_not_empty
import dev.cuervolu.loredesigner.ui.resources.launcher_error_invalid_name
import dev.cuervolu.loredesigner.ui.resources.launcher_error_missing_location
import dev.cuervolu.loredesigner.ui.resources.launcher_error_not_a_workspace
import dev.cuervolu.loredesigner.ui.resources.launcher_new_name_label
import dev.cuervolu.loredesigner.ui.resources.settings_close
import dev.cuervolu.loredesigner.ui.resources.settings_page_appearance
import dev.cuervolu.loredesigner.ui.resources.settings_page_general
import dev.cuervolu.loredesigner.ui.resources.settings_page_language
import dev.cuervolu.loredesigner.ui.resources.titlebar_settings
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_status_no_document
import dev.cuervolu.loredesigner.workspace.WorkspaceError
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
    private val app = AppTestHarness()
    private val settings = app.settings
    private val store = app.store

    @AfterTest
    fun tearDown() {
        app.close()
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `settings button on the launcher opens the modal without project settings`() = runComposeUiTest {
        with(app) { launch() }

        val appearance = getString(Res.string.settings_page_appearance)
        onNodeWithContentDescription(getString(Res.string.titlebar_settings)).performClick()

        waitForText(appearance)
        onAllNodesWithText(getString(Res.string.settings_page_general)).assertCountEquals(0)

        onNodeWithContentDescription(getString(Res.string.settings_close)).performClick()
        waitUntilTextGone(appearance)
    }

    @Test
    fun `settings opened from a workspace include project settings`() = runComposeUiTest {
        with(app) { launch() }
        openProjectFromLauncher("/worlds/Embercourt")
        waitForText("Embercourt")

        onAllNodesWithContentDescription(getString(Res.string.titlebar_settings)).assertCountEquals(0)
        openSettingsFromMenu()

        waitForText(getString(Res.string.settings_page_general))
        onNodeWithText("EMBERCOURT").assertExists()
    }

    @Test
    fun `changing the language relocalizes live and keeps the session`() = runComposeUiTest {
        settings.repository.update { it.copy(language = AppLanguage.ENGLISH) }
        with(app) { launch() }
        openProjectFromLauncher("/worlds/Embercourt")
        waitForText("Embercourt")
        openSettingsFromMenu()
        onNodeWithText(getString(Res.string.settings_page_language)).performClick()
        val englishStatus = getString(Res.string.workspace_shell_status_no_document)

        settings.repository.update { it.copy(language = AppLanguage.SPANISH) }
        waitForIdle()

        val spanishStatus = getString(Res.string.workspace_shell_status_no_document)
        assertEquals("es", Locale.getDefault().language)
        assertNotEquals(englishStatus, spanishStatus)
        // The workspace is still open, the modal still shows the Language page, now in Spanish.
        assertOnWorkspace("Embercourt")
        onAllNodesWithText(getString(Res.string.settings_page_language)).assertCountEquals(2)
        assertEquals(AppLanguage.SPANISH, settings.store.stored.language)

        settings.repository.update { it.copy(language = AppLanguage.ENGLISH) }
        waitForIdle()

        onNodeWithText(englishStatus).assertExists()
        onAllNodesWithText(spanishStatus).assertCountEquals(0)
    }

    @Test
    fun `launcher is the initial destination`() = runComposeUiTest {
        with(app) { launch() }

        onNodeWithText(getString(Res.string.launcher_empty_welcome_title)).assertExists()
        assertOnLauncher()
    }

    @Test
    fun `opening a project navigates to the workspace and closing returns`() = runComposeUiTest {
        with(app) { launch() }

        openProjectFromLauncher("/worlds/Embercourt")

        waitForText("Embercourt")
        assertOnWorkspace("Embercourt")
        assertEquals(listOf("/worlds/Embercourt".toPath()), store.openedLocations)

        closeProjectFromMenu()

        waitForText("/worlds/Embercourt")
        assertOnLauncher()
        onNodeWithText(getString(Res.string.launcher_empty_welcome_title)).assertDoesNotExist()
    }

    @Test
    fun `a closed project stays listed and reopens from the launcher`() = runComposeUiTest {
        with(app) { launch() }
        openProjectFromLauncher("/worlds/Embercourt")
        waitForText("Embercourt")
        closeProjectFromMenu()
        waitForText("/worlds/Embercourt")

        onNodeWithText("/worlds/Embercourt").performMouseInput { doubleClick() }

        waitForText(getString(Res.string.workspace_shell_status_no_document))
        assertOnWorkspace("Embercourt")
        assertEquals(List(2) { "/worlds/Embercourt".toPath() }, store.openedLocations)
    }

    @Test
    fun `failed open stays on the launcher with a message`() = runComposeUiTest {
        store.openError = WorkspaceError.NotAWorkspace("/tmp/notes".toPath())
        with(app) { launch() }

        openProjectFromLauncher("/tmp/notes")

        waitForText(getString(Res.string.launcher_error_not_a_workspace, "notes"))
        assertOnLauncher()
        assertEquals(listOf("/tmp/notes".toPath()), store.openedLocations)
        assertTrue(app.registry.workspaces.value.isEmpty(), "A failed open must not be remembered")
    }

    @Test
    fun `creating a project with a browsed location navigates to the workspace`() = runComposeUiTest {
        with(app) { launch(pickedDirectory = "/worlds".toPath()) }

        fillNewProject("Thistlewood")
        onAllNodesWithText(getString(Res.string.launcher_browse)).onFirst().performClick()
        waitForIdle()
        submitNewProject()

        waitForText(getString(Res.string.workspace_shell_status_no_document))
        assertOnWorkspace("Thistlewood")
        assertEquals(listOf("/worlds/Thistlewood".toPath()), store.createdLocations)
    }

    @Test
    fun `failed create shows a message and does not navigate`() = runComposeUiTest {
        store.createError = WorkspaceError.DestinationNotEmpty("/worlds/Thistlewood".toPath())
        with(app) { launch() }

        fillNewProject("Thistlewood", location = "/worlds")
        submitNewProject()

        waitForText(getString(Res.string.launcher_error_destination_not_empty, "Thistlewood"))
        assertOnLauncher()
        assertEquals(1, store.createdLocations.size)
    }

    @Test
    fun `creating without a location shows a message and does not touch storage`() = runComposeUiTest {
        with(app) { launch() }

        fillNewProject("Thistlewood")
        submitNewProject()

        waitForText(getString(Res.string.launcher_error_missing_location))
        assertOnLauncher()
        assertTrue(store.createdLocations.isEmpty())
    }

    @Test
    fun `creating with an invalid name shows a message and stays on the launcher`() = runComposeUiTest {
        with(app) { launch() }

        fillNewProject("a/b", location = "/worlds")
        submitNewProject()

        waitForText(getString(Res.string.launcher_error_invalid_name))
        assertOnLauncher()
        assertTrue(store.createdLocations.isEmpty())
    }

    @Test
    fun `the selected color is saved with the new project`() = runComposeUiTest {
        with(app) { launch() }

        fillNewProject("Thistlewood", location = "/worlds")
        onNodeWithContentDescription(getString(Res.string.launcher_color_green)).performClick()
        submitNewProject()

        waitUntil { store.createdConfigs.isNotEmpty() }
        assertEquals(ProjectColor.GREEN, store.createdConfigs.single().color)
    }

    @Test
    fun `cancelling the folder picker keeps the typed location`() = runComposeUiTest {
        with(app) { launch(pickedDirectory = null) }

        fillNewProject("Thistlewood", location = "/worlds")
        onAllNodesWithText(getString(Res.string.launcher_browse)).onFirst().performClick()
        waitForIdle()
        submitNewProject()

        waitUntil { store.createdLocations.isNotEmpty() }
        assertEquals(listOf("/worlds/Thistlewood".toPath()), store.createdLocations)
    }

    @Test
    fun `ctrl+n opens the new project dialog`() = runComposeUiTest {
        val nameLabel = getString(Res.string.launcher_new_name_label)
        with(app) { launch() }
        waitForIdle()

        onRoot().performKeyInput {
            keyDown(Key.CtrlLeft)
            keyDown(Key.N)
            keyUp(Key.N)
            keyUp(Key.CtrlLeft)
        }

        waitUntil { onAllNodes(hasContentDescription(nameLabel)).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun `ctrl+comma opens settings through the window shortcuts`() = runComposeUiTest {
        with(app) { launch() }
        val appearance = getString(Res.string.settings_page_appearance)

        onRoot().performKeyInput {
            keyDown(Key.CtrlLeft)
            keyDown(Key.Comma)
            keyUp(Key.Comma)
            keyUp(Key.CtrlLeft)
        }
        waitForText(appearance)
        onNodeWithContentDescription(getString(Res.string.settings_close)).performClick()
        waitUntilTextGone(appearance)
    }
}
