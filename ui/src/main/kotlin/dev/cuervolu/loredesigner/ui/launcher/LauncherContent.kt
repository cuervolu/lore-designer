package dev.cuervolu.loredesigner.ui.launcher

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Folder
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.TriangleAlert
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.components.LoreButton
import dev.cuervolu.loredesigner.ui.components.LoreButtonVariant
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.launcher_action_new
import dev.cuervolu.loredesigner.ui.resources.launcher_action_open
import dev.cuervolu.loredesigner.ui.resources.launcher_empty_missing_body
import dev.cuervolu.loredesigner.ui.resources.launcher_empty_missing_title
import dev.cuervolu.loredesigner.ui.resources.launcher_empty_pinned_body
import dev.cuervolu.loredesigner.ui.resources.launcher_empty_pinned_title
import dev.cuervolu.loredesigner.ui.resources.launcher_empty_recent_body
import dev.cuervolu.loredesigner.ui.resources.launcher_empty_recent_title
import dev.cuervolu.loredesigner.ui.resources.launcher_empty_welcome_body
import dev.cuervolu.loredesigner.ui.resources.launcher_empty_welcome_title
import dev.cuervolu.loredesigner.ui.resources.launcher_project_count
import dev.cuervolu.loredesigner.ui.resources.launcher_section_missing
import dev.cuervolu.loredesigner.ui.resources.launcher_section_pinned
import dev.cuervolu.loredesigner.ui.resources.launcher_section_projects
import dev.cuervolu.loredesigner.ui.resources.launcher_section_recent
import dev.cuervolu.loredesigner.ui.resources.launcher_title_missing
import dev.cuervolu.loredesigner.ui.resources.launcher_title_pinned
import dev.cuervolu.loredesigner.ui.resources.launcher_title_projects
import dev.cuervolu.loredesigner.ui.resources.launcher_title_recent
import dev.cuervolu.loredesigner.ui.theme.DURATION_FAST_MILLIS
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreSizes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.StandardEasing
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.sizes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val SidebarWidth = 216.dp
private val HeaderHeight = 52.dp
private val EmptyStateIconBox = 44.dp
private val EmptyStateIconShape = RoundedCornerShape(10.dp)
private val EmptyStateMaxWidth = 380.dp

enum class LauncherSection(
    internal val label: StringResource,
    internal val title: StringResource,
    internal val icon: ImageVector,
) {
    Projects(Res.string.launcher_section_projects, Res.string.launcher_title_projects, Lucide.Folder),
    Pinned(Res.string.launcher_section_pinned, Res.string.launcher_title_pinned, Lucide.Star),
    Recent(Res.string.launcher_section_recent, Res.string.launcher_title_recent, Lucide.Calendar),
    Missing(Res.string.launcher_section_missing, Res.string.launcher_title_missing, Lucide.TriangleAlert),
}

/**
 * Launcher body below the title bar. Dialogs are rendered by the caller
 */
@Composable
fun LauncherContent(
    section: LauncherSection,
    onSectionChange: (LauncherSection) -> Unit,
    onNewProject: () -> Unit,
    onOpenProject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Theme[colors][LoreColors.background])
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown || !(event.isCtrlPressed || event.isMetaPressed)) {
                    return@onPreviewKeyEvent false
                }
                val target = when (event.key) {
                    Key.N -> {
                        onNewProject()
                        return@onPreviewKeyEvent true
                    }

                    Key.One -> LauncherSection.Projects

                    Key.Two -> LauncherSection.Pinned

                    Key.Three -> LauncherSection.Recent

                    Key.Four -> LauncherSection.Missing

                    else -> return@onPreviewKeyEvent false
                }
                onSectionChange(target)
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
    ) {
        LauncherSidebar(section = section, onSectionChange = onSectionChange)
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            LauncherHeader(onNewProject = onNewProject, onOpenProject = onOpenProject)
            SectionTitle(section)
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                SectionEmptyState(section = section, onNewProject = onNewProject, onOpenProject = onOpenProject)
            }
        }
    }
}

@Composable
private fun LauncherSidebar(section: LauncherSection, onSectionChange: (LauncherSection) -> Unit) {
    val border = Theme[colors][LoreColors.borderSubtle]
    Column(
        modifier = Modifier
            .width(SidebarWidth)
            .fillMaxHeight()
            .background(Theme[colors][LoreColors.surface])
            .drawBehind {
                val x = size.width - 0.5.dp.toPx()
                drawLine(border, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
            }
            .padding(horizontal = Theme[spacing][LoreSpacing.space4], vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        LauncherSection.entries.forEach { entry ->
            SidebarItem(
                section = entry,
                selected = entry == section,
                onClick = { onSectionChange(entry) },
            )
        }
    }
}

@Composable
private fun SidebarItem(section: LauncherSection, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val background by animateColorAsState(
        targetValue = when {
            selected -> Theme[colors][LoreColors.accentSubtle]
            hovered -> Theme[colors][LoreColors.surfaceEditor]
            else -> Color.Transparent
        },
        animationSpec = tween(DURATION_FAST_MILLIS, easing = StandardEasing),
    )
    val textColor = if (selected) Theme[colors][LoreColors.accent] else Theme[colors][LoreColors.textPrimary]
    val iconColor = if (selected) Theme[colors][LoreColors.accent] else Theme[colors][LoreColors.textSecondary]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Theme[sizes][LoreSizes.controlHeight])
            .background(background, Theme[shapes][LoreShapes.control])
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
            )
            .padding(horizontal = Theme[spacing][LoreSpacing.space4]),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space4]),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            imageVector = section.icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            colorFilter = ColorFilter.tint(iconColor),
        )
        Text(
            text = stringResource(section.label),
            style = Theme[typography][LoreTypography.label],
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor,
            singleLine = true,
        )
    }
}

@Composable
private fun LauncherHeader(onNewProject: () -> Unit, onOpenProject: () -> Unit) {
    val border = Theme[colors][LoreColors.borderSubtle]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderHeight)
            .drawBehind {
                val y = size.height - 0.5.dp.toPx()
                drawLine(border, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            .padding(horizontal = Theme[spacing][LoreSpacing.space6]),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space4]),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.weight(1f))
        LoreButton(
            text = stringResource(Res.string.launcher_action_open),
            onClick = onOpenProject,
            leadingIcon = Lucide.Folder,
        )
        LoreButton(
            text = stringResource(Res.string.launcher_action_new),
            onClick = onNewProject,
            variant = LoreButtonVariant.Primary,
            leadingIcon = Lucide.Plus,
        )
    }
}

@Composable
private fun SectionTitle(section: LauncherSection) {
    Row(
        modifier = Modifier.padding(
            start = Theme[spacing][LoreSpacing.space6],
            end = Theme[spacing][LoreSpacing.space6],
            top = 14.dp,
            bottom = 10.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = stringResource(section.title),
            modifier = Modifier.alignByBaseline(),
            style = Theme[typography][LoreTypography.title],
            color = Theme[colors][LoreColors.textPrimary],
        )
        Text(
            text = pluralStringResource(Res.plurals.launcher_project_count, 0, 0),
            modifier = Modifier.alignByBaseline(),
            style = Theme[typography][LoreTypography.body],
            color = Theme[colors][LoreColors.textMuted],
        )
    }
}

@Composable
private fun SectionEmptyState(section: LauncherSection, onNewProject: () -> Unit, onOpenProject: () -> Unit) {
    val (icon, title, body) = when (section) {
        LauncherSection.Projects -> Triple(
            Lucide.Sparkles,
            Res.string.launcher_empty_welcome_title,
            Res.string.launcher_empty_welcome_body,
        )

        LauncherSection.Pinned -> Triple(
            Lucide.Star,
            Res.string.launcher_empty_pinned_title,
            Res.string.launcher_empty_pinned_body,
        )

        LauncherSection.Recent -> Triple(
            Lucide.Sparkles,
            Res.string.launcher_empty_recent_title,
            Res.string.launcher_empty_recent_body,
        )

        LauncherSection.Missing -> Triple(
            Lucide.TriangleAlert,
            Res.string.launcher_empty_missing_title,
            Res.string.launcher_empty_missing_body,
        )
    }
    val showActions = section == LauncherSection.Projects || section == LauncherSection.Recent

    Column(
        modifier = Modifier
            .widthIn(max = EmptyStateMaxWidth)
            .padding(horizontal = Theme[spacing][LoreSpacing.space8], vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(EmptyStateIconBox)
                .background(Theme[colors][LoreColors.accentSubtle], EmptyStateIconShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(Theme[colors][LoreColors.accent]),
            )
        }
        Text(
            text = stringResource(title),
            style = Theme[typography][LoreTypography.heading],
            color = Theme[colors][LoreColors.textPrimary],
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(body),
            style = Theme[typography][LoreTypography.body],
            color = Theme[colors][LoreColors.textSecondary],
            textAlign = TextAlign.Center,
        )
        if (showActions) {
            Row(
                modifier = Modifier.padding(top = Theme[spacing][LoreSpacing.space2]),
                horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space4]),
            ) {
                LoreButton(text = stringResource(Res.string.launcher_action_open), onClick = onOpenProject)
                LoreButton(
                    text = stringResource(Res.string.launcher_action_new),
                    onClick = onNewProject,
                    variant = LoreButtonVariant.Primary,
                )
            }
        }
    }
}
