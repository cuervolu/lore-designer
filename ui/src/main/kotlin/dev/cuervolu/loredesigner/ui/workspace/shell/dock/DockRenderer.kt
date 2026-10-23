package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.hoverable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
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
import java.awt.Cursor

private val SeparatorGrab = 9.dp
private const val COLLAPSING_ALPHA = 0.45f

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
        DockNodeView(root, registry, viewEmpty, state)
    }
}

@Composable
private fun DockNodeView(
    node: RenderNode,
    registry: PanelRegistry,
    viewEmpty: @Composable () -> Unit,
    state: WorkspaceDockState,
) {
    when (node) {
        is RenderGroup -> DockGroupView(node, registry, viewEmpty, state::activate)
        is RenderSplit -> DockSplitView(node, registry, viewEmpty, state)
    }
}

@Composable
private fun DockSplitView(
    split: RenderSplit,
    registry: PanelRegistry,
    viewEmpty: @Composable () -> Unit,
    state: WorkspaceDockState,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontal = split.orientation == DockOrientation.Horizontal
        val available = if (horizontal) maxWidth else maxHeight
        var collapsing by remember { mutableStateOf<Int?>(null) }
        val sizings = split.children.mapIndexed { index, child ->
            val base = child.sizingAlong(split.orientation, registry)
            val preferred = base.preferred
            when {
                preferred == null -> base
                index == collapsing -> base.copy(preferred = base.min)
                else -> base.copy(preferred = state.sizeOverride(child.id) ?: preferred)
            }
        }
        val sizes = remember(split, sizings, available) {
            measureSplit(
                children = split.children.mapIndexed { index, child ->
                    MeasureChild(sizings[index], split.weights[index], child.shrinkOrder(registry))
                },
                available = available,
            )
        }
        val shown = split.children.indices.filter { sizes[it] > 0.dp }
        val content: @Composable (Int) -> Unit = { index ->
            val size: Dp = sizes[index]
            val sizeModifier = if (horizontal) {
                Modifier.width(size).fillMaxHeight()
            } else {
                Modifier.height(size).fillMaxWidth()
            }
            val dim = if (index == collapsing) Modifier.alpha(COLLAPSING_ALPHA) else Modifier
            Box(sizeModifier.then(dim)) { DockNodeView(split.children[index], registry, viewEmpty, state) }
        }
        if (horizontal) {
            Row(Modifier.fillMaxSize(), Arrangement.spacedBy(DockSeparator)) { shown.forEach { content(it) } }
        } else {
            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(DockSeparator)) { shown.forEach { content(it) } }
        }

        val session = remember { DragSession() }
        val shownSizings = shown.map { sizings[it] }
        val shownSizes = shown.map { sizes[it] }
        var gapStart = 0.dp
        for (k in 0 until shown.lastIndex) {
            gapStart += shownSizes[k]
            val before = split.children[shown[k]]
            val after = split.children[shown[k + 1]]
            val plan = planSeparator(shownSizings, k, k + 1)
            val resetAction = {
                when (plan) {
                    is SeparatorPlan.Fixed -> state.resetFixed(split.children[shown[plan.index]].id)
                    is SeparatorPlan.Fill -> state.resizeFill(split.id, before.id, after.id, 0.5f)
                }
            }
            DockSeparatorHandle(
                horizontal = horizontal,
                position = gapStart - SeparatorGrab / 2 + DockSeparator / 2,
                tag = "dock-separator-${before.id.value}",
                highlighted = collapsing != null && plan is SeparatorPlan.Fixed &&
                    shown[plan.index] == collapsing,
                onDragStart = {
                    session.sizes = shownSizes
                    session.sizings = shownSizings
                },
                onDrag = { delta ->
                    when (plan) {
                        is SeparatorPlan.Fixed -> {
                            val target = split.children[shown[plan.index]]
                            val result = dragFixed(
                                start = session.sizes[plan.index],
                                delta = if (plan.growsWithDelta) delta else -delta,
                                min = session.sizings[plan.index].min,
                                max = maxFixedSize(session.sizings, plan.index, available),
                            )
                            collapsing = if (result.collapsing) shown[plan.index] else null
                            if (!result.collapsing) state.resizeFixed(target.id, result.size)
                        }

                        is SeparatorPlan.Fill -> state.resizeFill(
                            split.id,
                            before.id,
                            after.id,
                            dragFillShare(
                                startBefore = session.sizes[plan.before],
                                startAfter = session.sizes[plan.after],
                                delta = delta,
                                minBefore = session.sizings[plan.before].min,
                                minAfter = session.sizings[plan.after].min,
                            ),
                        )
                    }
                },
                onDragEnd = {
                    collapsing?.let { state.hide(split.children[it].panels()) }
                    collapsing = null
                },
                onReset = resetAction,
            )
            gapStart += DockSeparator
        }
    }
}

/** Sizes captured when a drag starts, so the whole gesture is measured against one baseline. */
private class DragSession {
    var sizes: List<Dp> = emptyList()
    var sizings: List<AxisSizing> = emptyList()
}

/**
 * A [SeparatorGrab]-wide pointer target centred on the 1 dp line. It is overlaid on the panes rather than laid
 * out between them, so it never takes room from them.
 */
@Composable
private fun DockSeparatorHandle(
    horizontal: Boolean,
    position: Dp,
    tag: String,
    highlighted: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Dp) -> Unit,
    onDragEnd: () -> Unit,
    onReset: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    var dragging by remember { mutableStateOf(false) }
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val density = LocalDensity.current
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnReset by rememberUpdatedState(onReset)
    val lineColor = Theme[colors][LoreColors.accent]
    val cursor = remember(horizontal) {
        PointerIcon(Cursor(if (horizontal) Cursor.E_RESIZE_CURSOR else Cursor.S_RESIZE_CURSOR))
    }
    val placement = if (horizontal) {
        Modifier.offset(x = position).width(SeparatorGrab).fillMaxHeight()
    } else {
        Modifier.offset(y = position).height(SeparatorGrab).fillMaxWidth()
    }
    Box(
        placement
            .testTag(tag)
            .onGloballyPositioned { coordinates = it }
            .hoverable(interactionSource)
            .pointerHoverIcon(cursor)
            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { currentOnReset() }) }
            .pointerInput(horizontal) {
                // The handle moves while it is dragged, so deltas are taken in root space, not handle space.
                fun rootAxis(position: Offset): Float {
                    val root = coordinates?.localToRoot(position) ?: position
                    return if (horizontal) root.x else root.y
                }
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val origin = rootAxis(down.position)
                    var started = false
                    drag(down.id) { change ->
                        if (!started) {
                            started = true
                            dragging = true
                            currentOnDragStart()
                        }
                        change.consume()
                        with(density) { currentOnDrag((rootAxis(change.position) - origin).toDp()) }
                    }
                    if (started) {
                        dragging = false
                        currentOnDragEnd()
                    }
                }
            }
            .drawBehind {
                if (hovered || dragging || highlighted) {
                    val thickness = DockSeparator.toPx()
                    if (horizontal) {
                        drawRect(lineColor, Offset((size.width - thickness) / 2, 0f), Size(thickness, size.height))
                    } else {
                        drawRect(lineColor, Offset(0f, (size.height - thickness) / 2), Size(size.width, thickness))
                    }
                }
            },
    )
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
