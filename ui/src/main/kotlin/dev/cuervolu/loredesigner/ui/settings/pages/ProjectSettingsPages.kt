package dev.cuervolu.loredesigner.ui.settings.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.components.LoreButton
import dev.cuervolu.loredesigner.ui.components.LoreButtonSize
import dev.cuervolu.loredesigner.ui.components.LoreButtonVariant
import dev.cuervolu.loredesigner.ui.components.LoreReadOnlyField
import dev.cuervolu.loredesigner.ui.components.LoreSwitch
import dev.cuervolu.loredesigner.ui.components.LoreTextField
import dev.cuervolu.loredesigner.ui.launcher.ColorSwatches
import dev.cuervolu.loredesigner.ui.launcher.LauncherError
import dev.cuervolu.loredesigner.ui.launcher.localizedMessage
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.settings_index
import dev.cuervolu.loredesigner.ui.resources.settings_index_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_index_unavailable
import dev.cuervolu.loredesigner.ui.resources.settings_page_general
import dev.cuervolu.loredesigner.ui.resources.settings_page_indexing
import dev.cuervolu.loredesigner.ui.resources.settings_page_maintenance
import dev.cuervolu.loredesigner.ui.resources.settings_page_types
import dev.cuervolu.loredesigner.ui.resources.settings_page_workspace
import dev.cuervolu.loredesigner.ui.resources.settings_project_color
import dev.cuervolu.loredesigner.ui.resources.settings_project_color_helper
import dev.cuervolu.loredesigner.ui.resources.settings_project_color_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_project_file
import dev.cuervolu.loredesigner.ui.resources.settings_project_file_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_project_name
import dev.cuervolu.loredesigner.ui.resources.settings_project_name_helper
import dev.cuervolu.loredesigner.ui.resources.settings_project_name_invalid
import dev.cuervolu.loredesigner.ui.resources.settings_project_name_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_reindex
import dev.cuervolu.loredesigner.ui.resources.settings_reset
import dev.cuervolu.loredesigner.ui.resources.settings_reset_project_cache
import dev.cuervolu.loredesigner.ui.resources.settings_reset_project_cache_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_reset_project_cache_unavailable
import dev.cuervolu.loredesigner.ui.resources.settings_show_hidden
import dev.cuervolu.loredesigner.ui.resources.settings_show_hidden_helper
import dev.cuervolu.loredesigner.ui.resources.settings_show_hidden_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_types_description
import dev.cuervolu.loredesigner.ui.resources.settings_types_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_types_unavailable
import dev.cuervolu.loredesigner.ui.resources.settings_workspace_location
import dev.cuervolu.loredesigner.ui.resources.settings_workspace_location_keywords
import dev.cuervolu.loredesigner.ui.settings.SettingsDescription
import dev.cuervolu.loredesigner.ui.settings.SettingsHelper
import dev.cuervolu.loredesigner.ui.settings.SettingsPage
import dev.cuervolu.loredesigner.ui.settings.SettingsPageContext
import dev.cuervolu.loredesigner.ui.settings.SettingsRow
import dev.cuervolu.loredesigner.ui.settings.SettingsScope
import dev.cuervolu.loredesigner.ui.settings.SettingsStackedRow
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.typography
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import org.jetbrains.compose.resources.stringResource

private const val PROJECT_FILE_NAME = "project.lore"

internal object ProjectGeneralPage : SettingsPage {
    override val id = "project.general"
    override val scope = SettingsScope.Project
    override val order = 0
    override val title = text(Res.string.settings_page_general)
    override val searchEntries = listOf(
        entry(Res.string.settings_project_name, Res.string.settings_project_name_keywords),
        entry(Res.string.settings_project_color, Res.string.settings_project_color_keywords),
    )

    @Composable
    override fun Content(context: SettingsPageContext) {
        val state = context.state
        val workspace = state.workspace ?: return
        val nameLabel = stringResource(Res.string.settings_project_name)
        val hadFocus = remember { mutableStateOf(false) }
        SettingsRow(nameLabel, helper = stringResource(Res.string.settings_project_name_helper)) {
            LoreTextField(
                state = context.viewModel.projectName,
                accessibilityLabel = nameLabel,
                enabled = !state.projectSaving,
                onSubmit = context.viewModel::commitProjectName,
                modifier = Modifier
                    .width(220.dp)
                    .onFocusChanged { focus ->
                        if (hadFocus.value && !focus.hasFocus) context.viewModel.commitProjectName()
                        hadFocus.value = focus.hasFocus
                    },
            )
        }
        SettingsStackedRow(
            label = stringResource(Res.string.settings_project_color),
            helper = stringResource(Res.string.settings_project_color_helper),
            divider = false,
        ) {
            ColorSwatches(
                selected = workspace.config.color,
                enabled = !state.projectSaving,
                onSelect = context.viewModel::setProjectColor,
            )
        }
        state.projectError?.let { error ->
            Text(
                text = if (error is WorkspaceError.InvalidWorkspaceName) {
                    stringResource(Res.string.settings_project_name_invalid)
                } else {
                    LauncherError.Workspace(error).localizedMessage()
                },
                style = Theme[typography][LoreTypography.caption],
                color = Theme[colors][LoreColors.danger],
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
    }
}

/** The project's type definitions cannot be read or edited yet, so the page only explains what types are. */
internal object ProjectTypesPage : SettingsPage {
    override val id = "project.types"
    override val scope = SettingsScope.Project
    override val order = 10
    override val title = text(Res.string.settings_page_types)
    override val searchEntries = listOf(entry(Res.string.settings_page_types, Res.string.settings_types_keywords))

    @Composable
    override fun Content(context: SettingsPageContext) {
        SettingsDescription(stringResource(Res.string.settings_types_description))
        SettingsHelper(stringResource(Res.string.settings_types_unavailable))
    }
}

internal object ProjectWorkspacePage : SettingsPage {
    override val id = "project.workspace"
    override val scope = SettingsScope.Project
    override val order = 20
    override val title = text(Res.string.settings_page_workspace)
    override val searchEntries = listOf(
        entry(Res.string.settings_show_hidden, Res.string.settings_show_hidden_keywords),
        entry(Res.string.settings_workspace_location, Res.string.settings_workspace_location_keywords),
    )

    @Composable
    override fun Content(context: SettingsPageContext) {
        val workspace = context.state.workspace ?: return
        val showHidden = stringResource(Res.string.settings_show_hidden)
        SettingsRow(showHidden, helper = stringResource(Res.string.settings_show_hidden_helper)) {
            LoreSwitch(
                checked = workspace.config.id in context.state.settings.showHiddenFilesIn,
                onCheckedChange = context.viewModel::setShowHiddenFiles,
                accessibilityLabel = showHidden,
            )
        }
        val location = stringResource(Res.string.settings_workspace_location)
        SettingsStackedRow(location, divider = false) {
            LoreReadOnlyField(workspace.location.toString(), accessibilityLabel = location)
        }
    }
}

/** There is no search index yet; re-indexing stays disabled until the indexer exists. */
internal object ProjectIndexingPage : SettingsPage {
    override val id = "project.indexing"
    override val scope = SettingsScope.Project
    override val order = 30
    override val title = text(Res.string.settings_page_indexing)
    override val searchEntries = listOf(entry(Res.string.settings_index, Res.string.settings_index_keywords))

    @Composable
    override fun Content(context: SettingsPageContext) {
        SettingsRow(
            label = stringResource(Res.string.settings_index),
            helper = stringResource(Res.string.settings_index_unavailable),
            divider = false,
        ) {
            LoreButton(
                text = stringResource(Res.string.settings_reindex),
                onClick = {},
                size = LoreButtonSize.Small,
                enabled = false,
            )
        }
    }
}

internal object ProjectMaintenancePage : SettingsPage {
    override val id = "project.maintenance"
    override val scope = SettingsScope.Project
    override val order = 40
    override val title = text(Res.string.settings_page_maintenance)
    override val searchEntries = listOf(
        entry(Res.string.settings_project_file, Res.string.settings_project_file_keywords),
        entry(Res.string.settings_reset_project_cache, Res.string.settings_reset_project_cache_keywords),
    )

    @Composable
    override fun Content(context: SettingsPageContext) {
        val workspace = context.state.workspace ?: return
        val projectFile = stringResource(Res.string.settings_project_file)
        SettingsStackedRow(projectFile) {
            LoreReadOnlyField((workspace.location / PROJECT_FILE_NAME).toString(), accessibilityLabel = projectFile)
        }
        // Projects have no local cache yet; the action stays disabled until they do.
        SettingsRow(
            label = stringResource(Res.string.settings_reset_project_cache),
            helper = stringResource(Res.string.settings_reset_project_cache_unavailable),
            divider = false,
        ) {
            LoreButton(
                text = stringResource(Res.string.settings_reset),
                onClick = {},
                variant = LoreButtonVariant.Danger,
                size = LoreButtonSize.Small,
                enabled = false,
            )
        }
    }
}
