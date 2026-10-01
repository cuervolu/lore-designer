package dev.cuervolu.loredesigner.ui.settings

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import dev.cuervolu.loredesigner.core.settings.ThemePreference
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.ui.launcher.TestWorkspaceId
import dev.cuervolu.loredesigner.workspace.Workspace
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val harness = SettingsTestHarness()
    private val workspace =
        Workspace("/worlds/Embercourt".toPath(), ProjectConfig(1, TestWorkspaceId, "Embercourt", ProjectColor.VIOLET))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setting changes are persisted through the store`() {
        val viewModel = harness.factory.create(null)

        viewModel.updateSettings { it.copy(theme = ThemePreference.DARK) }

        assertEquals(ThemePreference.DARK, viewModel.state.value.settings.theme)
        assertEquals(ThemePreference.DARK, harness.store.stored.theme)
    }

    @Test
    fun `diagnostic logging changes reach the logger`() {
        val viewModel = harness.factory.create(null)

        viewModel.updateSettings { it.copy(diagnosticLogging = true) }
        viewModel.updateSettings { it.copy(theme = ThemePreference.LIGHT) }

        assertEquals(listOf(true), harness.environment.diagnosticLoggingChanges)
    }

    @Test
    fun `project pages cannot be selected without a workspace`() {
        val viewModel = harness.factory.create(null)

        viewModel.selectPage("project.general")

        assertEquals("app.appearance", viewModel.state.value.selectedPageId)
    }

    @Test
    fun `selecting a page clears the search`() {
        val viewModel = harness.factory.create(workspace)
        viewModel.query.setTextAndPlaceCursorAtEnd("theme")

        viewModel.selectPage("project.maintenance")

        assertEquals("project.maintenance", viewModel.state.value.selectedPageId)
        assertEquals("", viewModel.query.text.toString())
    }

    @Test
    fun `renaming the project saves through the use case and keeps the location`() {
        val viewModel = harness.factory.create(workspace)

        viewModel.projectName.setTextAndPlaceCursorAtEnd("  Ember Court ")
        viewModel.commitProjectName()

        val saved = harness.workspaceStore.updatedConfigs.single()
        assertEquals("Ember Court", saved.name)
        assertEquals(ProjectColor.VIOLET, saved.color)
        assertEquals("Ember Court", viewModel.state.value.workspace?.config?.name)
        assertEquals(workspace.location, viewModel.state.value.workspace?.location)
        assertEquals("Ember Court", viewModel.projectName.text.toString())
    }

    @Test
    fun `unchanged name does not write the project file`() {
        val viewModel = harness.factory.create(workspace)

        viewModel.commitProjectName()

        assertTrue(harness.workspaceStore.updatedConfigs.isEmpty())
    }

    @Test
    fun `invalid names and failed saves are reported`() {
        val viewModel = harness.factory.create(workspace)
        viewModel.projectName.setTextAndPlaceCursorAtEnd("   ")
        viewModel.commitProjectName()
        assertIs<WorkspaceError.InvalidWorkspaceName>(viewModel.state.value.projectError)

        harness.workspaceStore.updateError = WorkspaceError.NotAWorkspace(workspace.location)
        viewModel.setProjectColor(ProjectColor.GREEN)

        assertIs<WorkspaceError.NotAWorkspace>(viewModel.state.value.projectError)
        assertEquals(ProjectColor.VIOLET, viewModel.state.value.workspace?.config?.color)
    }

    @Test
    fun `show hidden files is stored per project`() {
        val viewModel = harness.factory.create(workspace)

        viewModel.setShowHiddenFiles(true)
        assertEquals(setOf(TestWorkspaceId), harness.store.stored.showHiddenFilesIn)

        viewModel.setShowHiddenFiles(false)
        assertTrue(harness.store.stored.showHiddenFilesIn.isEmpty())
    }

    @Test
    fun `failing to open a folder exposes its path`() {
        val viewModel = harness.factory.create(null)
        harness.environment.openSucceeds = false

        viewModel.openLogs()
        assertEquals("/data/lore/logs", viewModel.state.value.unopenedFolder)

        harness.environment.openSucceeds = true
        viewModel.openApplicationData()
        assertNull(viewModel.state.value.unopenedFolder)
        assertEquals(listOf("/data/lore/logs", "/data/lore"), harness.environment.openedFolders)
    }
}
