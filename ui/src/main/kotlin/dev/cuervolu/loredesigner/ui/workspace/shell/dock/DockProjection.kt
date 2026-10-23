package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import dev.cuervolu.loredesigner.editor.dock.DockLayout
import dev.cuervolu.loredesigner.editor.dock.DockNode
import dev.cuervolu.loredesigner.editor.dock.DockNodeId
import dev.cuervolu.loredesigner.editor.dock.DockOrientation
import dev.cuervolu.loredesigner.editor.dock.PanelGroup
import dev.cuervolu.loredesigner.editor.dock.PanelId
import dev.cuervolu.loredesigner.editor.dock.PanelKind
import dev.cuervolu.loredesigner.editor.dock.SplitNode

sealed interface RenderNode {
    val id: DockNodeId
}

data class RenderSplit(
    override val id: DockNodeId,
    val orientation: DockOrientation,
    val children: List<RenderNode>,
    val weights: List<Float>,
) : RenderNode

/** [displayed] is null only for a view group without visible panels, which shows the empty editor area. */
data class RenderGroup(
    override val id: DockNodeId,
    val kind: PanelKind,
    val tabs: List<PanelId>,
    val displayed: PanelId?,
) : RenderNode

/**
 * Projects the layout for display. Groups left without visible panels disappear, except view groups, which
 * keep the editor area alive. A split left with one child collapses into it, and weights are renormalized.
 */
fun DockLayout.project(hidden: Set<PanelId>): RenderNode =
    checkNotNull(root.project(hidden)) { "A valid layout always has a view group" }

private fun DockNode.project(hidden: Set<PanelId>): RenderNode? = when (this) {
    is PanelGroup -> projectGroup(hidden)
    is SplitNode -> projectSplit(hidden)
}

private fun PanelGroup.projectGroup(hidden: Set<PanelId>): RenderNode? {
    val tabs = panels.filterNot { it in hidden }
    if (tabs.isEmpty() && kind == PanelKind.Tool) return null
    val displayed = activePanel?.takeIf { it in tabs } ?: tabs.firstOrNull()
    return RenderGroup(id, kind, tabs, displayed)
}

private fun SplitNode.projectSplit(hidden: Set<PanelId>): RenderNode? {
    val kept = children.mapIndexedNotNull { index, child -> child.project(hidden)?.let { it to weights[index] } }
    return when (kept.size) {
        0 -> null

        1 -> kept.single().first

        else -> {
            val total = kept.sumOf { it.second.toDouble() }.toFloat()
            RenderSplit(id, orientation, kept.map { it.first }, kept.map { it.second / total })
        }
    }
}
