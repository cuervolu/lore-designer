package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import dev.cuervolu.loredesigner.editor.dock.DockNodeId
import dev.cuervolu.loredesigner.editor.dock.PanelId

/**
 * An in-flight tab drag. The dock tree is only read while dragging and is changed by [WorkspaceDockState.move]
 * when the drag is committed, so cancelling never has anything to undo.
 *
 * Group geometry is kept as live [LayoutCoordinates] and turned into rectangles whenever it is needed,
 * so it follows resizes and re-layouts instead of going stale.
 */
@Stable
internal class DockDragState(private val dock: WorkspaceDockState) {
    var dragged: PanelId? by mutableStateOf(null)
        private set

    var pointer: Offset by mutableStateOf(Offset.Zero)
        private set

    var resolution: DropResolution by mutableStateOf(DropResolution.None)
        private set

    val active: Boolean get() = dragged != null

    private class Registration {
        var group: LayoutCoordinates? = null
        var tabBar: LayoutCoordinates? = null
        val tabs = mutableMapOf<PanelId, LayoutCoordinates>()
    }

    var host: LayoutCoordinates? = null

    /** Pixels per dp of the window, which turns the dp-based distances of the drag into pixels. */
    var density: Float = 1f

    private val registrations = mutableMapOf<DockNodeId, Registration>()

    fun setGroup(group: DockNodeId, coordinates: LayoutCoordinates?) {
        registration(group).group = coordinates
        prune(group)
    }

    fun setTabBar(group: DockNodeId, coordinates: LayoutCoordinates?) {
        registration(group).tabBar = coordinates
        prune(group)
    }

    fun setTab(group: DockNodeId, panel: PanelId, coordinates: LayoutCoordinates?) {
        val registration = registration(group)
        if (coordinates == null) registration.tabs.remove(panel) else registration.tabs[panel] = coordinates
        prune(group)
    }

    fun start(panel: PanelId, at: Offset): Boolean {
        if (panel !in dock.layout.panels) return false
        dragged = panel
        update(at)
        return true
    }

    fun update(at: Offset) {
        val panel = dragged ?: return
        pointer = at
        resolution = resolve(panel, at)
    }

    /** Ends the drag and returns what it resolved to at [at], measured against the geometry as it is now. */
    fun finish(at: Offset): DropResolution {
        val panel = dragged ?: return DropResolution.None
        val result = resolve(panel, at)
        cancel()
        return result
    }

    /** Commits the drag at [at]. Anything but a valid destination cancels it. Returns whether the layout changed. */
    fun drop(at: Offset): Boolean {
        val panel = dragged ?: return false
        val result = finish(at)
        return when (result) {
            is DropResolution.Valid -> dock.move(panel, result.target)
            is DropResolution.ValidEdge -> dock.move(panel, result.target)
            else -> false
        }
    }

    fun cancel() {
        dragged = null
        resolution = DropResolution.None
    }

    private fun resolve(panel: PanelId, at: Offset): DropResolution = resolveDrop(
        at,
        panel,
        dock.layout,
        dock.hidden,
        areas(),
        host?.takeIf { it.isAttached }?.boundsInRoot(),
        edgeZone = EDGE_ZONE * density,
    )

    private fun areas(): List<GroupArea> = registrations.mapNotNull { (id, registration) ->
        val group = registration.group?.takeIf { it.isAttached } ?: return@mapNotNull null
        val order = dock.layout.findGroup(id)?.panels ?: return@mapNotNull null
        GroupArea(
            group = id,
            bounds = group.boundsInRoot(),
            tabBar = registration.tabBar?.takeIf { it.isAttached }?.boundsInRoot(),
            tabs = order.mapNotNull { panel ->
                registration.tabs[panel]?.takeIf { it.isAttached }?.let { TabSlot(panel, it.boundsInRoot()) }
            },
        )
    }

    private fun registration(group: DockNodeId) = registrations.getOrPut(group) { Registration() }

    private fun prune(group: DockNodeId) {
        val registration = registrations[group] ?: return
        if (registration.group == null && registration.tabBar == null && registration.tabs.isEmpty()) {
            registrations.remove(group)
        }
    }
}
