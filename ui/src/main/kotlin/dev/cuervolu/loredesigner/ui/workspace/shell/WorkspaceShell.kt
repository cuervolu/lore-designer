package dev.cuervolu.loredesigner.ui.workspace.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.PanelBottom
import com.composables.icons.lucide.PanelLeft
import com.composables.icons.lucide.PanelRight
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.components.LoreButton
import dev.cuervolu.loredesigner.ui.components.LoreButtonVariant
import dev.cuervolu.loredesigner.ui.components.LoreIconButton
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_back_to_launcher
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_center_empty_body
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_center_empty_title
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_explorer_empty_body
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_explorer_empty_title
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_log_empty
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_log_title
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_not_open_body
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_not_open_title
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_properties_empty_body
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_properties_empty_title
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_properties_title
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_status_no_document
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_toggle_explorer
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_toggle_log
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_toggle_properties
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreSizes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.dotToken
import dev.cuervolu.loredesigner.ui.theme.sizes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography
import dev.cuervolu.loredesigner.workspace.Workspace
import org.jetbrains.compose.resources.stringResource

private val SeparatorWidth = 1.dp

/**
 * Structure of the workspace screen: Explorer, center, Properties, bottom panel and status bar.
 *
 * Each region takes its content as a slot with an empty state as default, so later views can fill
 * a region without changing how the shell is composed. [workspace] is null when the route no longer
 * matches the active session (for example a restored back stack).
 */
@Composable
fun WorkspaceShell(
    workspace: Workspace?,
    state: WorkspaceShellState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    explorer: @Composable () -> Unit = { ExplorerEmptyState() },
    center: @Composable () -> Unit = { CenterEmptyState() },
    properties: @Composable () -> Unit = { PropertiesEmptyState() },
    bottomPanel: @Composable () -> Unit = { LogEmptyState() },
) {
    if (workspace == null) {
        NotOpen(onClose = onClose, modifier = modifier)
        return
    }
    Column(modifier = modifier.fillMaxSize().background(Theme[colors][LoreColors.border])) {
        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val layout = shellLayout(
                width = maxWidth,
                height = maxHeight,
                explorerVisible = state.explorerVisible,
                propertiesVisible = state.propertiesVisible,
                bottomVisible = state.bottomPanelVisible,
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(SeparatorWidth),
            ) {
                if (layout.explorerWidth > 0.dp) {
                    Region(
                        title = { ProjectTitle(workspace) },
                        modifier = Modifier.width(layout.explorerWidth).fillMaxHeight(),
                        content = explorer,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(SeparatorWidth),
                ) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth()
                            .background(Theme[colors][LoreColors.surfaceEditor]),
                    ) { center() }
                    if (layout.bottomHeight > 0.dp) {
                        Region(
                            title = { RegionTitle(stringResource(Res.string.workspace_shell_log_title)) },
                            background = Theme[colors][LoreColors.surfaceEditor],
                            modifier = Modifier.height(layout.bottomHeight).fillMaxWidth(),
                            content = bottomPanel,
                        )
                    }
                }
                if (layout.propertiesWidth > 0.dp) {
                    Region(
                        title = { RegionTitle(stringResource(Res.string.workspace_shell_properties_title)) },
                        modifier = Modifier.width(layout.propertiesWidth).fillMaxHeight(),
                        content = properties,
                    )
                }
            }
        }
        StatusBar(state)
    }
}

@Composable
private fun Region(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = Theme[colors][LoreColors.surface],
    content: @Composable () -> Unit,
) {
    val borderColor = Theme[colors][LoreColors.borderSubtle]
    Column(modifier = modifier.background(background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Theme[sizes][LoreSizes.controlHeight])
                .drawBehind {
                    val y = size.height - 0.5.dp.toPx()
                    drawLine(borderColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }
                .padding(horizontal = Theme[spacing][LoreSpacing.space5]),
            verticalAlignment = Alignment.CenterVertically,
        ) { title() }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) { content() }
    }
}

@Composable
private fun RegionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = Theme[typography][LoreTypography.eyebrow],
        color = Theme[colors][LoreColors.textMuted],
        singleLine = true,
    )
}

@Composable
private fun ProjectTitle(workspace: Workspace) {
    val dot = workspace.config.color?.dotToken() ?: LoreColors.textMuted
    Row(
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space4]),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(Theme[colors][dot], CircleShape))
        Text(
            text = workspace.config.name,
            style = Theme[typography][LoreTypography.bodyStrong],
            color = Theme[colors][LoreColors.textPrimary],
            singleLine = true,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EmptyState(title: String, body: String?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(Theme[spacing][LoreSpacing.space6]),
        verticalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space3], Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = Theme[typography][LoreTypography.label],
            color = Theme[colors][LoreColors.textPrimary],
            textAlign = TextAlign.Center,
        )
        if (body != null) {
            Text(
                text = body,
                style = Theme[typography][LoreTypography.body],
                color = Theme[colors][LoreColors.textSecondary],
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ExplorerEmptyState() = EmptyState(
    title = stringResource(Res.string.workspace_shell_explorer_empty_title),
    body = stringResource(Res.string.workspace_shell_explorer_empty_body),
)

@Composable
private fun CenterEmptyState() = EmptyState(
    title = stringResource(Res.string.workspace_shell_center_empty_title),
    body = stringResource(Res.string.workspace_shell_center_empty_body),
)

@Composable
private fun PropertiesEmptyState() = EmptyState(
    title = stringResource(Res.string.workspace_shell_properties_empty_title),
    body = stringResource(Res.string.workspace_shell_properties_empty_body),
)

@Composable
private fun LogEmptyState() = Text(
    text = stringResource(Res.string.workspace_shell_log_empty),
    style = Theme[typography][LoreTypography.body],
    color = Theme[colors][LoreColors.textMuted],
    modifier = Modifier.padding(Theme[spacing][LoreSpacing.space5]),
)

@Composable
private fun StatusBar(state: WorkspaceShellState) {
    val muted = Theme[colors][LoreColors.textMuted]
    val active = Theme[colors][LoreColors.accent]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = SeparatorWidth)
            .background(Theme[colors][LoreColors.surface])
            .height(Theme[sizes][LoreSizes.controlHeightSmall])
            .padding(horizontal = Theme[spacing][LoreSpacing.space3]),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space1]),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LoreIconButton(
            icon = Lucide.PanelLeft,
            contentDescription = stringResource(Res.string.workspace_shell_toggle_explorer),
            onClick = state::toggleExplorer,
            tint = if (state.explorerVisible) active else muted,
        )
        LoreIconButton(
            icon = Lucide.PanelRight,
            contentDescription = stringResource(Res.string.workspace_shell_toggle_properties),
            onClick = state::toggleProperties,
            tint = if (state.propertiesVisible) active else muted,
        )
        LoreIconButton(
            icon = Lucide.PanelBottom,
            contentDescription = stringResource(Res.string.workspace_shell_toggle_log),
            onClick = state::toggleBottomPanel,
            tint = if (state.bottomPanelVisible) active else muted,
        )
        Box(Modifier.weight(1f))
        Text(
            text = stringResource(Res.string.workspace_shell_status_no_document),
            style = Theme[typography][LoreTypography.caption],
            color = Theme[colors][LoreColors.textSecondary],
            singleLine = true,
        )
    }
}

@Composable
private fun NotOpen(onClose: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().background(Theme[colors][LoreColors.background]),
        verticalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space5], Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.workspace_shell_not_open_title),
            style = Theme[typography][LoreTypography.title],
            color = Theme[colors][LoreColors.textPrimary],
        )
        Text(
            text = stringResource(Res.string.workspace_shell_not_open_body),
            style = Theme[typography][LoreTypography.body],
            color = Theme[colors][LoreColors.textSecondary],
        )
        LoreButton(
            text = stringResource(Res.string.workspace_shell_back_to_launcher),
            onClick = onClose,
            variant = LoreButtonVariant.Secondary,
        )
    }
}
