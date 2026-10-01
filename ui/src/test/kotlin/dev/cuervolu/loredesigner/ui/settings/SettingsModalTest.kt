package dev.cuervolu.loredesigner.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.composeunstyled.DialogHost
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.ui.launcher.TestWorkspaceId
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.settings_group_project
import dev.cuervolu.loredesigner.ui.resources.settings_page_appearance
import dev.cuervolu.loredesigner.ui.resources.settings_page_diagnostics
import dev.cuervolu.loredesigner.ui.resources.settings_page_general
import dev.cuervolu.loredesigner.ui.resources.settings_page_maintenance
import dev.cuervolu.loredesigner.ui.resources.settings_page_types
import dev.cuervolu.loredesigner.ui.resources.settings_project_file
import dev.cuervolu.loredesigner.ui.resources.settings_search_no_results
import dev.cuervolu.loredesigner.ui.resources.settings_search_placeholder
import dev.cuervolu.loredesigner.ui.resources.settings_search_results
import dev.cuervolu.loredesigner.ui.resources.settings_theme
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import dev.cuervolu.loredesigner.workspace.Workspace
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.getString
import kotlin.test.Test

// Strings are resolved from resources so the tests pass under any machine locale.
@OptIn(ExperimentalTestApi::class)
class SettingsModalTest {
    private val harness = SettingsTestHarness()
    private val workspace =
        Workspace("/worlds/Embercourt".toPath(), ProjectConfig(1, TestWorkspaceId, "Embercourt", ProjectColor.BLUE))

    private fun androidx.compose.ui.test.ComposeUiTest.showModal(workspace: Workspace?) {
        val viewModel = harness.factory.create(workspace)
        setContent {
            LoreDesignerTheme(darkTheme = false) {
                DialogHost(Modifier.fillMaxSize()) {
                    SettingsModal(visible = true, viewModel = viewModel, onDismiss = {}, onWorkspaceChange = {})
                }
            }
        }
    }

    @Test
    fun `launcher context hides every project section`() = runComposeUiTest {
        showModal(workspace = null)

        onNodeWithText(getString(Res.string.settings_page_diagnostics)).assertExists()
        onAllNodesWithText(getString(Res.string.settings_group_project), ignoreCase = true).assertCountEquals(0)
        onAllNodesWithText(getString(Res.string.settings_page_general)).assertCountEquals(0)
        onAllNodesWithText(getString(Res.string.settings_page_maintenance)).assertCountEquals(0)
    }

    @Test
    fun `workspace context lists the project section with its pages`() = runComposeUiTest {
        showModal(workspace)

        onNodeWithText(getString(Res.string.settings_group_project).uppercase()).assertExists()
        onNodeWithText("EMBERCOURT").assertExists()
        onNodeWithText(getString(Res.string.settings_page_types)).assertExists()
        onNodeWithText(getString(Res.string.settings_page_maintenance)).performClick()

        onNodeWithContentDescription(getString(Res.string.settings_project_file)).assertExists()
        onNodeWithText("/worlds/Embercourt/project.lore").assertExists()
    }

    @Test
    fun `search shows matching settings and opens their page`() = runComposeUiTest {
        val theme = getString(Res.string.settings_theme)
        showModal(workspace = null)

        onNodeWithContentDescription(getString(Res.string.settings_search_placeholder)).performTextInput(theme)

        onNodeWithText(getString(Res.string.settings_search_results, theme)).assertExists()
        // The result row: label plus its "Appearance" breadcrumb; selecting it leaves search mode.
        onAllNodesWithText(getString(Res.string.settings_page_appearance)).assertCountEquals(2)

        onAllNodesWithText(theme).onLast().performClick()

        onAllNodesWithText(getString(Res.string.settings_search_results, theme)).assertCountEquals(0)
        onNodeWithContentDescription(theme).assertExists()
    }

    @Test
    fun `search in the launcher never offers project settings`() = runComposeUiTest {
        showModal(workspace = null)

        onNodeWithContentDescription(getString(Res.string.settings_search_placeholder))
            .performTextInput(getString(Res.string.settings_project_file))

        onNodeWithText(getString(Res.string.settings_search_no_results)).assertExists()
    }
}
