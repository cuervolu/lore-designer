package dev.cuervolu.loredesigner.ui.launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TriangleAlert
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.ui.components.LoreButton
import dev.cuervolu.loredesigner.ui.components.LoreButtonVariant
import dev.cuervolu.loredesigner.ui.components.LoreDialog
import dev.cuervolu.loredesigner.ui.components.LoreTextField
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.common_cancel
import dev.cuervolu.loredesigner.ui.resources.launcher_action_new
import dev.cuervolu.loredesigner.ui.resources.launcher_action_open
import dev.cuervolu.loredesigner.ui.resources.launcher_browse
import dev.cuervolu.loredesigner.ui.resources.launcher_color_amber
import dev.cuervolu.loredesigner.ui.resources.launcher_color_blue
import dev.cuervolu.loredesigner.ui.resources.launcher_color_cyan
import dev.cuervolu.loredesigner.ui.resources.launcher_color_green
import dev.cuervolu.loredesigner.ui.resources.launcher_color_orange
import dev.cuervolu.loredesigner.ui.resources.launcher_color_red
import dev.cuervolu.loredesigner.ui.resources.launcher_color_rose
import dev.cuervolu.loredesigner.ui.resources.launcher_color_slate
import dev.cuervolu.loredesigner.ui.resources.launcher_color_teal
import dev.cuervolu.loredesigner.ui.resources.launcher_color_violet
import dev.cuervolu.loredesigner.ui.resources.launcher_new_color_label
import dev.cuervolu.loredesigner.ui.resources.launcher_new_description
import dev.cuervolu.loredesigner.ui.resources.launcher_new_location_label
import dev.cuervolu.loredesigner.ui.resources.launcher_new_location_placeholder
import dev.cuervolu.loredesigner.ui.resources.launcher_new_name_label
import dev.cuervolu.loredesigner.ui.resources.launcher_new_submit
import dev.cuervolu.loredesigner.ui.resources.launcher_new_submitting
import dev.cuervolu.loredesigner.ui.resources.launcher_open_description
import dev.cuervolu.loredesigner.ui.resources.launcher_open_path_label
import dev.cuervolu.loredesigner.ui.resources.launcher_open_path_placeholder
import dev.cuervolu.loredesigner.ui.resources.launcher_open_submit
import dev.cuervolu.loredesigner.ui.resources.launcher_open_submitting
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.dotToken
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography
import org.jetbrains.compose.resources.stringResource

private val SwatchSize = 22.dp

// Keeps the last dialog content on screen while the exit animation plays after it is dismissed.
@Composable
private fun <T : Any> rememberLastNonNull(value: T?): T? {
    val last = remember { mutableStateOf(value) }
    if (value != null) last.value = value
    return last.value
}

@Composable
internal fun NewProjectDialog(
    dialog: LauncherDialog.NewProject?,
    busy: Boolean,
    error: LauncherError?,
    onColorChange: (ProjectColor) -> Unit,
    onBrowse: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    LoreDialog(
        visible = dialog != null,
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.launcher_action_new),
        description = stringResource(Res.string.launcher_new_description),
        actions = {
            DialogActions(
                submitLabel = stringResource(
                    if (busy) Res.string.launcher_new_submitting else Res.string.launcher_new_submit,
                ),
                busy = busy,
                onSubmit = onSubmit,
                onDismiss = onDismiss,
            )
        },
    ) {
        val shown = rememberLastNonNull(dialog)
        if (shown != null) {
            NewProjectFields(shown, busy, error, onColorChange, onBrowse, onSubmit)
        }
    }
}

@Composable
private fun NewProjectFields(
    dialog: LauncherDialog.NewProject,
    busy: Boolean,
    error: LauncherError?,
    onColorChange: (ProjectColor) -> Unit,
    onBrowse: () -> Unit,
    onSubmit: () -> Unit,
) {
    val nameLabel = stringResource(Res.string.launcher_new_name_label)
    LoreTextField(
        state = dialog.name,
        accessibilityLabel = nameLabel,
        placeholder = nameLabel,
        enabled = !busy,
        onSubmit = onSubmit,
        modifier = Modifier.fillMaxWidth(),
    )
    Field(label = stringResource(Res.string.launcher_new_location_label)) { label ->
        PathRow(
            label = label,
            state = dialog.location,
            placeholder = stringResource(Res.string.launcher_new_location_placeholder),
            busy = busy,
            onBrowse = onBrowse,
            onSubmit = onSubmit,
        )
    }
    Field(label = stringResource(Res.string.launcher_new_color_label)) {
        ColorSwatches(selected = dialog.color, enabled = !busy, onSelect = onColorChange)
    }
    if (error != null) ErrorBanner(error)
}

@Composable
internal fun OpenProjectDialog(
    dialog: LauncherDialog.OpenProject?,
    busy: Boolean,
    error: LauncherError?,
    onBrowse: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    LoreDialog(
        visible = dialog != null,
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.launcher_action_open),
        description = stringResource(Res.string.launcher_open_description),
        actions = {
            DialogActions(
                submitLabel = stringResource(
                    if (busy) Res.string.launcher_open_submitting else Res.string.launcher_open_submit,
                ),
                busy = busy,
                onSubmit = onSubmit,
                onDismiss = onDismiss,
            )
        },
    ) {
        val shown = rememberLastNonNull(dialog)
        if (shown != null) {
            PathRow(
                label = stringResource(Res.string.launcher_open_path_label),
                state = shown.path,
                placeholder = stringResource(Res.string.launcher_open_path_placeholder),
                busy = busy,
                onBrowse = onBrowse,
                onSubmit = onSubmit,
            )
            if (error != null) ErrorBanner(error)
        }
    }
}

@Composable
private fun DialogActions(submitLabel: String, busy: Boolean, onSubmit: () -> Unit, onDismiss: () -> Unit) {
    LoreButton(
        text = stringResource(Res.string.common_cancel),
        onClick = onDismiss,
        variant = LoreButtonVariant.Ghost,
        enabled = !busy,
    )
    LoreButton(
        text = submitLabel,
        onClick = onSubmit,
        variant = LoreButtonVariant.Primary,
        enabled = !busy,
    )
}

@Composable
private fun Field(label: String, content: @Composable (label: String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space4])) {
        Text(
            text = label,
            style = Theme[typography][LoreTypography.caption],
            color = Theme[colors][LoreColors.textMuted],
        )
        content(label)
    }
}

@Composable
private fun PathRow(
    label: String,
    state: TextFieldState,
    placeholder: String,
    busy: Boolean,
    onBrowse: () -> Unit,
    onSubmit: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space4]),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LoreTextField(
            state = state,
            accessibilityLabel = label,
            placeholder = placeholder,
            enabled = !busy,
            onSubmit = onSubmit,
            modifier = Modifier.weight(1f),
        )
        LoreButton(text = stringResource(Res.string.launcher_browse), onClick = onBrowse, enabled = !busy)
    }
}

@Composable
internal fun ColorSwatches(selected: ProjectColor?, enabled: Boolean, onSelect: (ProjectColor) -> Unit) {
    val outline = Theme[colors][LoreColors.textPrimary]
    Row(
        modifier = Modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space4]),
    ) {
        ProjectColor.entries.forEach { color ->
            val isSelected = color == selected
            val name = stringResource(color.label())
            // The outline sits 2dp outside the swatch, like the design's `outline-offset`.
            Box(
                modifier = Modifier
                    .size(SwatchSize + 8.dp)
                    .border(2.dp, if (isSelected) outline else Color.Transparent, CircleShape)
                    .selectable(
                        selected = isSelected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        interactionSource = null,
                        indication = null,
                        onClick = { onSelect(color) },
                    )
                    .semantics { contentDescription = name },
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(SwatchSize).background(Theme[colors][color.dotToken()], CircleShape))
            }
        }
    }
}

internal fun ProjectColor.label() = when (this) {
    ProjectColor.VIOLET -> Res.string.launcher_color_violet
    ProjectColor.BLUE -> Res.string.launcher_color_blue
    ProjectColor.CYAN -> Res.string.launcher_color_cyan
    ProjectColor.TEAL -> Res.string.launcher_color_teal
    ProjectColor.GREEN -> Res.string.launcher_color_green
    ProjectColor.AMBER -> Res.string.launcher_color_amber
    ProjectColor.ORANGE -> Res.string.launcher_color_orange
    ProjectColor.ROSE -> Res.string.launcher_color_rose
    ProjectColor.RED -> Res.string.launcher_color_red
    ProjectColor.SLATE -> Res.string.launcher_color_slate
}

@Composable
internal fun ErrorBanner(error: LauncherError, modifier: Modifier = Modifier) {
    val danger = Theme[colors][LoreColors.danger]
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Theme[colors][LoreColors.dangerSubtle], Theme[shapes][LoreShapes.control])
            .padding(10.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space4]),
    ) {
        Image(
            imageVector = Lucide.TriangleAlert,
            contentDescription = null,
            modifier = Modifier.padding(top = 1.dp).size(15.dp),
            colorFilter = ColorFilter.tint(danger),
        )
        Text(
            text = error.localizedMessage(),
            style = Theme[typography][LoreTypography.caption],
            color = danger,
            modifier = Modifier.weight(1f),
        )
    }
}
