package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.editor.dock.DockOrientation
import dev.cuervolu.loredesigner.editor.dock.PanelId
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreSizes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.sizes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography

/**
 * Renders the dock tree of [state]. [viewEmpty] fills a view group that has no visible panel, which is
 * the editor area before any document is open.
 */
@Composable
fun DockHost(
    state: WorkspaceDockState,
    registry: PanelRegistry,
    viewEmpty: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val root = remember(state.layout, state.hidden) { state.layout.project(state.hidden) }
    Box(modifier = modifier.background(Theme[colors][LoreColors.border])) {
        DockNodeView(root, registry, viewEmpty, onActivate = state::activate)
    }
}

@Composable
private fun DockNodeView(
    node: RenderNode,
    registry: PanelRegistry,
    viewEmpty: @Composable () -> Unit,
    onActivate: (PanelId) -> Unit,
) {
    when (node) {
        is RenderGroup -> DockGroupView(node, registry, viewEmpty, onActivate)
        is RenderSplit -> DockSplitView(node, registry, viewEmpty, onActivate)
    }
}

@Composable
private fun DockSplitView(
    split: RenderSplit,
    registry: PanelRegistry,
    viewEmpty: @Composable () -> Unit,
    onActivate: (PanelId) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontal = split.orientation == DockOrientation.Horizontal
        val available = if (horizontal) maxWidth else maxHeight
        val sizes = remember(split, registry, available) {
            measureSplit(
                children = split.children.mapIndexed { index, child ->
                    MeasureChild(
                        child.sizingAlong(split.orientation, registry),
                        split.weights[index],
                        child.shrinkOrder(registry),
                    )
                },
                available = available,
            )
        }
        val shown = split.children.indices.filter { sizes[it] > 0.dp }
        val content: @Composable (Int) -> Unit = { index ->
            val size: Dp = sizes[index]
            val sizeModifier = if (horizontal) {
                Modifier.width(
                    size,
                ).fillMaxHeight()
            } else {
                Modifier.height(size).fillMaxWidth()
            }
            Box(sizeModifier) { DockNodeView(split.children[index], registry, viewEmpty, onActivate) }
        }
        if (horizontal) {
            Row(Modifier.fillMaxSize(), Arrangement.spacedBy(DockSeparator)) { shown.forEach { content(it) } }
        } else {
            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(DockSeparator)) { shown.forEach { content(it) } }
        }
    }
}

@Composable
private fun DockGroupView(
    group: RenderGroup,
    registry: PanelRegistry,
    viewEmpty: @Composable () -> Unit,
    onActivate: (PanelId) -> Unit,
) {
    val displayed = group.displayed?.let { registry[it] }
    val editorSurface = displayed?.editorSurface ?: (displayed == null)
    val surface = if (editorSurface) LoreColors.surfaceEditor else LoreColors.surface
    if (displayed == null) {
        Box(Modifier.fillMaxSize().background(Theme[colors][surface])) { viewEmpty() }
        return
    }
    val borderColor = Theme[colors][LoreColors.borderSubtle]
    Column(Modifier.fillMaxSize().background(Theme[colors][surface])) {
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
        ) {
            if (group.tabs.size > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space1])) {
                    group.tabs.forEach { panel ->
                        val spec = registry[panel] ?: return@forEach
                        DockTab(spec.label(), selected = panel == group.displayed, onClick = { onActivate(panel) })
                    }
                }
            } else {
                displayed.header?.invoke() ?: PanelTitle(displayed.label())
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) { displayed.content() }
    }
}

@Composable
fun PanelTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = Theme[typography][LoreTypography.eyebrow],
        color = Theme[colors][LoreColors.textMuted],
        singleLine = true,
    )
}

@Composable
private fun DockTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val background = when {
        selected -> Theme[colors][LoreColors.accentSubtle]
        hovered -> Theme[colors][LoreColors.surfaceEditor]
        else -> Color.Transparent
    }
    Text(
        text = label,
        style = Theme[typography][LoreTypography.label],
        color = if (selected) Theme[colors][LoreColors.accent] else Theme[colors][LoreColors.textSecondary],
        singleLine = true,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .height(Theme[sizes][LoreSizes.controlHeightSmall])
            .background(background, Theme[shapes][LoreShapes.control])
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
            )
            .padding(horizontal = Theme[spacing][LoreSpacing.space4])
            .wrapContentHeight(),
    )
}
