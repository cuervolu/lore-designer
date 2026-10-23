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
import dev.cuervolu.loredesigner.ui.resources.workspace_shell_explorer_title
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
import dev.cuervolu.loredesigner.ui.workspace.shell.dock.DockHost
import dev.cuervolu.loredesigner.ui.workspace.shell.dock.DockPanelSpec
import dev.cuervolu.loredesigner.ui.workspace.shell.dock.PanelRegistry
import dev.cuervolu.loredesigner.ui.workspace.shell.dock.ShellPanels
import dev.cuervolu.loredesigner.ui.workspace.shell.dock.WorkspaceDockState
import dev.cuervolu.loredesigner.workspace.Workspace
import org.jetbrains.compose.resources.stringResource

private val SeparatorWidth = 1.dp

/**
 * Structure of the workspace screen.
 *
 * Each region takes its content as a slot with an empty state as default, so later views can fill
 * a region without changing how the shell is composed. [workspace] is null when the route no longer
 * matches the active session (for example a restored back stack).
 */
@Composable
fun WorkspaceShell(
    workspace: Workspace?,
    state: WorkspaceDockState,
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
    val registry = shellPanelRegistry(
        explorerTitle = { ProjectTitle(workspace) },
        explorer = explorer,
        properties = properties,
        bottomPanel = bottomPanel,
    )
    Column(modifier = modifier.fillMaxSize().background(Theme[colors][LoreColors.border])) {
        DockHost(
            state = state,
            registry = registry,
            viewEmpty = center,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
        StatusBar(state)
    }
}

@Composable
private fun shellPanelRegistry(
    explorerTitle: @Composable () -> Unit,
    explorer: @Composable () -> Unit,
    properties: @Composable () -> Unit,
    bottomPanel: @Composable () -> Unit,
): PanelRegistry = mapOf(
    ShellPanels.Explorer to DockPanelSpec(
        label = { stringResource(Res.string.workspace_shell_explorer_title) },
        content = explorer,
        sizing = ShellPanels.Sizing.getValue(ShellPanels.Explorer),
        header = explorerTitle,
    ),
    ShellPanels.Properties to DockPanelSpec(
        label = { stringResource(Res.string.workspace_shell_properties_title) },
        content = properties,
        sizing = ShellPanels.Sizing.getValue(ShellPanels.Properties),
    ),
    ShellPanels.Log to DockPanelSpec(
        label = { stringResource(Res.string.workspace_shell_log_title) },
        content = bottomPanel,
        sizing = ShellPanels.Sizing.getValue(ShellPanels.Log),
        editorSurface = true,
    ),
)

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
private fun StatusBar(state: WorkspaceDockState) {
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
