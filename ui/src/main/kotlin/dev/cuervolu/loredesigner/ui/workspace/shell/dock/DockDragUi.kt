package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.editor.dock.DockNodeId
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
import kotlin.math.roundToInt

internal const val DRAG_SOURCE_ALPHA = 0.35f
private val InsertionWidth = 3.dp
private val GhostOffset = 12.dp
private const val HIGHLIGHT_ALPHA = 0.12f

@Composable
internal fun Modifier.trackGroup(drag: DockDragState, group: DockNodeId): Modifier {
    DisposableEffect(drag, group) { onDispose { drag.setGroup(group, null) } }
    return onGloballyPositioned { drag.setGroup(group, it) }
}

@Composable
internal fun Modifier.trackTabBar(drag: DockDragState, group: DockNodeId): Modifier {
    DisposableEffect(drag, group) { onDispose { drag.setTabBar(group, null) } }
    return onGloballyPositioned { drag.setTabBar(group, it) }
}

/**
 * Makes the element a drag handle for [panel]. A press only turns into a drag once the pointer has moved past
 * [DRAG_THRESHOLD]; until then the events are left alone so a plain click still reaches the tab. Place it after
 * the click handler so consuming the moves cancels that click.
 */
@Composable
internal fun Modifier.dockDragSource(group: DockNodeId, panel: PanelId, dragState: DockDragState): Modifier {
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    DisposableEffect(dragState, group, panel) { onDispose { dragState.setTab(group, panel, null) } }
    return onGloballyPositioned {
        coordinates = it
        dragState.setTab(group, panel, it)
    }.pointerInput(dragState, panel) {
        fun root(position: Offset): Offset = coordinates?.localToRoot(position) ?: position
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var started = false
            var last = root(down.position)
            val released = drag(down.id) { change ->
                if (!started) {
                    if (!exceedsDragThreshold(down.position, change.position)) return@drag
                    started = dragState.start(panel, root(change.position))
                }
                if (started) {
                    change.consume()
                    last = root(change.position)
                    // Escape ends the drag while the button is still down; the rest of the gesture is then ignored.
                    if (dragState.dragged == panel) dragState.update(last)
                }
            }
            if (started && dragState.dragged == panel) {
                if (released) dragState.drop(last) else dragState.cancel()
            }
        }
    }
}

/** Drop feedback and the ghost that follows the cursor. It draws only and never takes pointer input. */
@Composable
internal fun DockDragOverlay(drag: DockDragState, registry: PanelRegistry, origin: Offset) {
    val panel = drag.dragged ?: return
    when (val resolution = drag.resolution) {
        DropResolution.None -> Unit

        is DropResolution.Invalid -> DropHighlight(
            resolution.area.bounds,
            origin,
            Theme[colors][LoreColors.danger],
            "dock-drop-invalid",
        )

        is DropResolution.ValidEdge ->
            DropHighlight(resolution.zone, origin, Theme[colors][LoreColors.accent], "dock-drop-edge")

        is DropResolution.Valid -> {
            val indicatorX = resolution.indicatorX
            val bar = resolution.area.tabBar
            if (indicatorX != null && bar != null) {
                DropHighlight(bar, origin, Theme[colors][LoreColors.accent], "dock-drop-valid")
                InsertionLine(bar, indicatorX, origin)
            } else {
                DropHighlight(resolution.area.bounds, origin, Theme[colors][LoreColors.accent], "dock-drop-valid")
            }
        }
    }
    registry[panel]?.let { DragGhost(it, drag.pointer - origin) }
}

@Composable
private fun DropHighlight(bounds: Rect, origin: Offset, color: Color, tag: String) {
    val density = LocalDensity.current
    Box(
        Modifier
            .offset { IntOffset((bounds.left - origin.x).roundToInt(), (bounds.top - origin.y).roundToInt()) }
            .size(with(density) { bounds.width.toDp() }, with(density) { bounds.height.toDp() })
            .background(color.copy(alpha = HIGHLIGHT_ALPHA))
            .border(1.dp, color)
            .testTag(tag),
    )
}

@Composable
private fun InsertionLine(bar: Rect, x: Float, origin: Offset) {
    val density = LocalDensity.current
    val height = with(density) { bar.height.toDp() }
    Box(
        Modifier
            .offset {
                IntOffset(
                    (x - origin.x - InsertionWidth.toPx() / 2).roundToInt(),
                    (bar.top - origin.y).roundToInt(),
                )
            }
            .size(InsertionWidth, height)
            .background(Theme[colors][LoreColors.accent])
            .testTag("dock-drop-indicator"),
    )
}

@Composable
private fun DragGhost(spec: DockPanelSpec, at: Offset) {
    val shape = Theme[shapes][LoreShapes.control]
    Row(
        modifier = Modifier
            .offset {
                IntOffset(at.x.roundToInt() + GhostOffset.roundToPx(), at.y.roundToInt() + GhostOffset.roundToPx())
            }
            .background(Theme[colors][LoreColors.surfaceRaised], shape)
            .border(1.dp, Theme[colors][LoreColors.border], shape)
            .padding(horizontal = Theme[spacing][LoreSpacing.space4], vertical = Theme[spacing][LoreSpacing.space2])
            .testTag("dock-drag-ghost"),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space3]),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        spec.icon?.let {
            Image(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(Theme[sizes][LoreSizes.icon]),
                colorFilter = ColorFilter.tint(Theme[colors][LoreColors.textSecondary]),
            )
        }
        Text(
            text = spec.label(),
            style = Theme[typography][LoreTypography.label],
            color = Theme[colors][LoreColors.textPrimary],
            singleLine = true,
        )
    }
}
