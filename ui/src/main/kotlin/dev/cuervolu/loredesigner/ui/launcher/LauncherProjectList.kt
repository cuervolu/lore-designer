package dev.cuervolu.loredesigner.ui.launcher

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Folder
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.X
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.ui.components.LoreButton
import dev.cuervolu.loredesigner.ui.components.LoreIconButton
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.launcher_project_forget
import dev.cuervolu.loredesigner.ui.resources.launcher_project_locate
import dev.cuervolu.loredesigner.ui.resources.launcher_project_missing
import dev.cuervolu.loredesigner.ui.resources.launcher_project_pin
import dev.cuervolu.loredesigner.ui.resources.launcher_project_unpin
import dev.cuervolu.loredesigner.ui.theme.DURATION_FAST_MILLIS
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.StandardEasing
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.dotToken
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.tintToken
import dev.cuervolu.loredesigner.ui.theme.typography
import org.jetbrains.compose.resources.stringResource

private val ProjectIconBox = 32.dp
private val ProjectIconShape = RoundedCornerShape(8.dp)
private val ProjectRowMinHeight = 52.dp

/** Callbacks for the actions a project row offers. */
class LauncherProjectActions(
    val onOpen: (WorkspaceId) -> Unit,
    val onPinnedChange: (WorkspaceId, Boolean) -> Unit,
    val onForget: (WorkspaceId) -> Unit,
    val onLocate: (WorkspaceId) -> Unit,
)

@Composable
internal fun LauncherProjectList(
    projects: List<LauncherProject>,
    actions: LauncherProjectActions,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = Theme[spacing][LoreSpacing.space4],
            end = Theme[spacing][LoreSpacing.space4],
            bottom = Theme[spacing][LoreSpacing.space6],
        ),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(projects, key = { it.id.toString() }) { project ->
            ProjectRow(project = project, actions = actions)
        }
    }
}

@Composable
private fun ProjectRow(project: LauncherProject, actions: LauncherProjectActions) {
    val missing = project.availability == ProjectAvailability.Missing
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = Theme[shapes][LoreShapes.control]
    val background by animateColorAsState(
        targetValue = if (hovered || focused) Theme[colors][LoreColors.surfaceEditor] else Color.Transparent,
        animationSpec = tween(DURATION_FAST_MILLIS, easing = StandardEasing),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ProjectRowMinHeight)
            .background(background, shape)
            .border(1.dp, if (focused) Theme[colors][LoreColors.focusRing] else Color.Transparent, shape)
            // Missing rows stay openable: opening re-checks the folder, which may be back.
            .clickable(
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null,
                onClick = { actions.onOpen(project.id) },
            )
            .padding(horizontal = Theme[spacing][LoreSpacing.space4], vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space4]),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProjectIcon(project)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space3]),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = project.name,
                    style = Theme[typography][LoreTypography.bodyStrong],
                    color = Theme[colors][if (missing) LoreColors.textSecondary else LoreColors.textPrimary],
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (missing) MissingBadge()
            }
            Text(
                text = project.path,
                style = Theme[typography][LoreTypography.mono],
                color = Theme[colors][LoreColors.textMuted],
                singleLine = true,
                overflow = TextOverflow.MiddleEllipsis,
            )
        }
        if (missing) {
            LoreButton(
                text = stringResource(Res.string.launcher_project_locate),
                onClick = { actions.onLocate(project.id) },
            )
        }
        LoreIconButton(
            icon = Lucide.Star,
            contentDescription = stringResource(
                if (project.pinned) Res.string.launcher_project_unpin else Res.string.launcher_project_pin,
            ),
            onClick = { actions.onPinnedChange(project.id, !project.pinned) },
            tint = if (project.pinned) Theme[colors][LoreColors.accent] else Color.Unspecified,
        )
        LoreIconButton(
            icon = Lucide.X,
            contentDescription = stringResource(Res.string.launcher_project_forget),
            onClick = { actions.onForget(project.id) },
        )
    }
}

@Composable
private fun ProjectIcon(project: LauncherProject) {
    val missing = project.availability == ProjectAvailability.Missing
    val color = project.color
    val (background, glyph) = when {
        missing -> Theme[colors][LoreColors.surfaceEditor] to Theme[colors][LoreColors.warning]
        color != null -> Theme[colors][color.tintToken()] to Theme[colors][color.dotToken()]
        else -> Theme[colors][LoreColors.accentSubtle] to Theme[colors][LoreColors.accent]
    }
    Box(
        modifier = Modifier.size(ProjectIconBox).background(background, ProjectIconShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            imageVector = if (missing) Lucide.TriangleAlert else Lucide.Folder,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            colorFilter = ColorFilter.tint(glyph),
        )
    }
}

@Composable
private fun MissingBadge() {
    Text(
        text = stringResource(Res.string.launcher_project_missing),
        style = Theme[typography][LoreTypography.caption],
        color = Theme[colors][LoreColors.warning],
        modifier = Modifier
            .background(Theme[colors][LoreColors.surfaceEditor], Theme[shapes][LoreShapes.control])
            .padding(horizontal = 6.dp, vertical = 1.dp),
        singleLine = true,
    )
}
