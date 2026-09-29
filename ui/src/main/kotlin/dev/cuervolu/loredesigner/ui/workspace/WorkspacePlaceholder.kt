package dev.cuervolu.loredesigner.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.components.LoreButton
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.workspace_placeholder_close
import dev.cuervolu.loredesigner.ui.resources.workspace_placeholder_id
import dev.cuervolu.loredesigner.ui.resources.workspace_placeholder_not_open_body
import dev.cuervolu.loredesigner.ui.resources.workspace_placeholder_not_open_title
import dev.cuervolu.loredesigner.ui.resources.workspace_placeholder_note
import dev.cuervolu.loredesigner.ui.resources.workspace_placeholder_path
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography
import dev.cuervolu.loredesigner.workspace.Workspace
import org.jetbrains.compose.resources.stringResource

/**
 * Stand-in for the workspace screen: proves navigation works and identifies the opened project.
 * [workspace] is null when the route no longer matches the active session (e.g. a restored back stack).
 */
@Composable
fun WorkspacePlaceholder(workspace: Workspace?, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Theme[colors][LoreColors.background]),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 520.dp).padding(Theme[spacing][LoreSpacing.space8]),
            verticalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space5]),
        ) {
            if (workspace == null) {
                Text(
                    text = stringResource(Res.string.workspace_placeholder_not_open_title),
                    style = Theme[typography][LoreTypography.title],
                    color = Theme[colors][LoreColors.textPrimary],
                )
                Text(
                    text = stringResource(Res.string.workspace_placeholder_not_open_body),
                    style = Theme[typography][LoreTypography.body],
                    color = Theme[colors][LoreColors.textSecondary],
                )
            } else {
                Text(
                    text = workspace.config.name,
                    style = Theme[typography][LoreTypography.title],
                    color = Theme[colors][LoreColors.textPrimary],
                )
                Text(
                    text = stringResource(Res.string.workspace_placeholder_note),
                    style = Theme[typography][LoreTypography.body],
                    color = Theme[colors][LoreColors.textSecondary],
                )
                Detail(stringResource(Res.string.workspace_placeholder_path), workspace.location.toString())
                Detail(stringResource(Res.string.workspace_placeholder_id), workspace.config.id.toString())
            }
            LoreButton(text = stringResource(Res.string.workspace_placeholder_close), onClick = onClose)
        }
    }
}

@Composable
private fun Detail(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space1])) {
        Text(
            text = label.uppercase(),
            style = Theme[typography][LoreTypography.eyebrow],
            color = Theme[colors][LoreColors.textMuted],
        )
        Text(
            text = value,
            style = Theme[typography][LoreTypography.mono],
            color = Theme[colors][LoreColors.textPrimary],
        )
    }
}
