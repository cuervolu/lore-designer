package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import dev.cuervolu.loredesigner.editor.dock.DockLayout
import dev.cuervolu.loredesigner.editor.dock.DockNodeId
import dev.cuervolu.loredesigner.editor.dock.DockSide
import dev.cuervolu.loredesigner.editor.dock.DockTarget
import dev.cuervolu.loredesigner.editor.dock.PanelId
import dev.cuervolu.loredesigner.editor.dock.PanelKind

/** Distance in pixels the pointer must travel before a press on a tab becomes a drag instead of a click. */
internal const val DRAG_THRESHOLD = 5f

/** Width in pixels of the strips along the window's left, right and bottom edges that dock a tool outside every group. */
internal const val EDGE_ZONE = 24f

internal fun exceedsDragThreshold(origin: Offset, current: Offset): Boolean =
    (current - origin).getDistance() > DRAG_THRESHOLD

internal class TabSlot(val panel: PanelId, val bounds: Rect)

/** Where a group is on screen right now, in root coordinates. [tabs] only holds the visible panels, in order. */
internal class GroupArea(val group: DockNodeId, val bounds: Rect, val tabBar: Rect?, val tabs: List<TabSlot>)

internal sealed interface DropResolution {
    data object None : DropResolution

    /** [indicatorX] is where the insertion line goes when dropping on the tab bar, null when dropping on the body. */
    data class Valid(val area: GroupArea, val target: DockTarget.Tab, val indicatorX: Float?) : DropResolution

    /** The pointer is in an outer [zone] of the window; the panel gets a new group at the window edge, beside the outermost group on that side. */
    data class ValidEdge(val zone: Rect, val target: DockTarget.Edge) : DropResolution

    data class Invalid(val area: GroupArea) : DropResolution
}

/**
 * Decides what dropping [dragged] at [pointer] would do. Only groups that still exist in [layout] are
 * candidates, so a registration left over from a group that has since gone can never become a target.
 */
internal fun resolveDrop(
    pointer: Offset,
    dragged: PanelId,
    layout: DockLayout,
    hidden: Set<PanelId>,
    areas: List<GroupArea>,
    window: Rect? = null,
): DropResolution {
    val kind = layout.panels[dragged] ?: return DropResolution.None
    // The outer strips come first so they win over whichever interior group happens to lie under them.
    if (kind == PanelKind.Tool && window != null) {
        edgeResolution(pointer, dragged, layout, areas, window)?.let { return it }
    }
    val area = areas.lastOrNull { it.bounds.contains(pointer) && layout.findGroup(it.group) != null }
        ?: return DropResolution.None
    val group = checkNotNull(layout.findGroup(area.group))
    if (group.kind != kind) return DropResolution.Invalid(area)

    val tabBar = area.tabBar
    if (tabBar == null || !tabBar.contains(pointer)) {
        val own = group.panels.indexOf(dragged)
        // Dropping a panel on its own body leaves it where it is rather than sending it to the end.
        return DropResolution.Valid(area, DockTarget.Tab(group.id, own.takeIf { it >= 0 }), indicatorX = null)
    }

    val others = area.tabs.filter { it.panel != dragged }
    val slot = others.count { it.bounds.center.x < pointer.x }
    val index = layoutInsertIndex(group.panels, dragged, hidden, others.map { it.panel }, slot)
    return DropResolution.Valid(area, DockTarget.Tab(group.id, index), indicatorX(tabBar, area.tabs, others, slot))
}

private fun edgeResolution(
    pointer: Offset,
    dragged: PanelId,
    layout: DockLayout,
    areas: List<GroupArea>,
    window: Rect,
): DropResolution? {
    val live = areas.filter { layout.findGroup(it.group) != null }
    val candidates = listOf(
        Triple(DockSide.Left, Rect(window.left, window.top, window.left + EDGE_ZONE, window.bottom)) { a: GroupArea ->
            (a.bounds.left <= window.left + EDGE_TOLERANCE) to a.bounds.height
        },
        Triple(
            DockSide.Right,
            Rect(window.right - EDGE_ZONE, window.top, window.right, window.bottom),
        ) { a: GroupArea ->
            (a.bounds.right >= window.right - EDGE_TOLERANCE) to a.bounds.height
        },
        Triple(
            DockSide.Bottom,
            Rect(window.left, window.bottom - EDGE_ZONE, window.right, window.bottom),
        ) { a: GroupArea ->
            (a.bounds.bottom >= window.bottom - EDGE_TOLERANCE) to a.bounds.width
        },
    )
    val (side, zone, touching) = candidates.firstOrNull { it.second.contains(pointer) } ?: return null
    // The editor area owns the window edge when it touches it, so tools dock around it rather than under a side panel.
    val onEdge = live.filter { touching(it).first }
    val anchor = onEdge.filter { layout.findGroup(it.group)?.kind == PanelKind.View }.ifEmpty { onEdge }
        .maxByOrNull { touching(it).second } ?: return null
    val group = checkNotNull(layout.findGroup(anchor.group))
    // A panel that is alone in the group already on this edge has nowhere further to go.
    if (group.panels == listOf(dragged)) return DropResolution.Invalid(anchor)
    return DropResolution.ValidEdge(zone, DockTarget.Edge(group.id, side))
}

private const val EDGE_TOLERANCE = 2f

/**
 * Maps the [slot] among the visible [others] to the final index in [groupPanels], which also holds hidden
 * panels that the user cannot see between tabs. [DockTarget.Tab.index] is the position once [dragged] is removed.
 */
internal fun layoutInsertIndex(
    groupPanels: List<PanelId>,
    dragged: PanelId,
    hidden: Set<PanelId>,
    others: List<PanelId>,
    slot: Int,
): Int {
    val remaining = groupPanels - dragged
    val visible = others.filter { it in remaining && it !in hidden }
    return when {
        visible.isEmpty() -> remaining.size
        slot >= visible.size -> remaining.indexOf(visible.last()) + 1
        else -> remaining.indexOf(visible[slot])
    }
}

private fun indicatorX(tabBar: Rect, all: List<TabSlot>, others: List<TabSlot>, slot: Int): Float = when {
    others.isEmpty() -> all.firstOrNull()?.bounds?.left ?: tabBar.left
    slot == 0 -> others.first().bounds.left
    slot >= others.size -> others.last().bounds.right
    else -> (others[slot - 1].bounds.right + others[slot].bounds.left) / 2
}
