package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.editor.dock.DockLayout
import dev.cuervolu.loredesigner.editor.dock.DockNode
import dev.cuervolu.loredesigner.editor.dock.DockNodeId
import dev.cuervolu.loredesigner.editor.dock.DockOperation
import dev.cuervolu.loredesigner.editor.dock.DockResult
import dev.cuervolu.loredesigner.editor.dock.DockSide
import dev.cuervolu.loredesigner.editor.dock.DockTarget
import dev.cuervolu.loredesigner.editor.dock.PanelId
import dev.cuervolu.loredesigner.editor.dock.PanelKind
import dev.cuervolu.loredesigner.editor.dock.SplitNode

/**
 * The workspace's dock layout plus which panels the user has hidden.
 *
 * A hidden panel keeps its place in [layout] (group, tab, side), so
 * showing it again restores it exactly where it was. The window can still collapse a visible panel when
 * space is short, but that is decided while measuring and never recorded here.
 */
@Stable
class WorkspaceDockState(
    initialLayout: DockLayout = defaultLayout(),
    initiallyHidden: Set<PanelId> = setOf(ShellPanels.Log),
    private val logger: Logger = Logger.withTag("Dock"),
) {
    var layout: DockLayout by mutableStateOf(initialLayout)
        private set

    var hidden: Set<PanelId> by mutableStateOf(initiallyHidden)
        private set

    /** Sizes the user dragged fixed-size nodes to, along their split's axis, keyed by node. Defaults apply when absent. */
    var sizeOverrides: Map<DockNodeId, Dp> by mutableStateOf(emptyMap())
        private set

    val explorerVisible: Boolean get() = isVisible(ShellPanels.Explorer)
    val propertiesVisible: Boolean get() = isVisible(ShellPanels.Properties)
    val bottomPanelVisible: Boolean get() = isVisible(ShellPanels.Log)

    fun isVisible(panel: PanelId): Boolean = panel !in hidden

    fun setVisible(panel: PanelId, visible: Boolean) {
        hidden = if (visible) hidden - panel else hidden + panel
    }

    fun toggle(panel: PanelId) = setVisible(panel, !isVisible(panel))

    fun toggleExplorer() = toggle(ShellPanels.Explorer)

    fun toggleProperties() = toggle(ShellPanels.Properties)

    fun toggleBottomPanel() = toggle(ShellPanels.Log)

    /** Hides [panels] without touching [layout], so each one returns to its place when shown again. */
    fun hide(panels: Set<PanelId>) {
        hidden = hidden + panels
    }

    fun sizeOverride(node: DockNodeId): Dp? = sizeOverrides[node]

    fun resizeFixed(node: DockNodeId, size: Dp) {
        sizeOverrides = sizeOverrides + (node to size)
    }

    fun resetFixed(node: DockNodeId) {
        sizeOverrides = sizeOverrides - node
    }

    /**
     * Moves the boundary between the flexible nodes [before] and [after] of [split] so that [before] gets
     * [beforeShare] of their combined weight. Weights of every other child are left alone.
     */
    fun resizeFill(split: DockNodeId, before: DockNodeId, after: DockNodeId, beforeShare: Float) {
        val node = layout.findSplit(split) ?: return logger.w { "Cannot resize unknown split $split" }
        val beforeIndex = node.children.indexOfFirst { it.contains(before) }
        val afterIndex = node.children.indexOfFirst { it.contains(after) }
        if (beforeIndex < 0 || afterIndex < 0) return logger.w { "Nodes $before/$after are not in split $split" }
        val pair = node.weights[beforeIndex] + node.weights[afterIndex]
        val weights = node.weights.toMutableList().apply {
            this[beforeIndex] = pair * beforeShare
            this[afterIndex] = pair - this[beforeIndex]
        }
        when (val result = layout.apply(DockOperation.SetWeights(split, weights))) {
            is DockResult.Applied -> layout = result.layout
            is DockResult.Rejected -> logger.w { "Cannot resize split $split: ${result.reason}" }
        }
    }

    /** Brings [panel] to the front of its group, revealing it first if the user had hidden it. */
    fun activate(panel: PanelId) {
        when (val result = layout.apply(DockOperation.Activate(panel))) {
            is DockResult.Applied -> {
                layout = result.layout
                setVisible(panel, true)
            }

            is DockResult.Rejected -> logger.w { "Cannot activate $panel: ${result.reason}" }
        }
    }

    private fun DockNode.contains(id: DockNodeId): Boolean = this.id == id ||
        (this is SplitNode && children.any { it.contains(id) })

    companion object {
        /** Explorer left, Properties right and Log under the editor area, built only with core operations. */
        fun defaultLayout(): DockLayout {
            var layout = DockLayout.initial()
            val editorArea = layout.groups.single().id
            fun add(panel: PanelId, side: DockSide) {
                val result = layout.apply(
                    DockOperation.AddPanel(panel, PanelKind.Tool, DockTarget.Edge(editorArea, side)),
                )
                layout = (result as DockResult.Applied).layout
            }
            add(ShellPanels.Explorer, DockSide.Left)
            add(ShellPanels.Properties, DockSide.Right)
            add(ShellPanels.Log, DockSide.Bottom)
            return layout
        }
    }
}
