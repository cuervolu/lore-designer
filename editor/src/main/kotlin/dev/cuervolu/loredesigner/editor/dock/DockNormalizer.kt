package dev.cuervolu.loredesigner.editor.dock

/**
 * Restores the structural invariants after an operation: empty tool groups and splits disappear, redundant empty
 * view groups are dropped (one always survives so the editor area never vanishes), single-child splits collapse
 * and same-orientation nesting is flattened with weights kept proportional.
 */
internal fun normalize(root: DockNode): DockNode {
    val viewGroups = root.groups().filter { it.kind == PanelKind.View }
    val keptEmptyView = if (viewGroups.any { it.panels.isNotEmpty() }) null else viewGroups.first().id

    fun visit(node: DockNode): DockNode? = when (node) {
        is PanelGroup -> when {
            node.panels.isNotEmpty() -> node
            node.kind == PanelKind.View && node.id == keptEmptyView -> node
            else -> null
        }

        is SplitNode -> {
            val kept = node.children.indices.mapNotNull { i -> visit(node.children[i])?.let { it to node.weights[i] } }
            when (kept.size) {
                0 -> null
                1 -> kept.single().first
                else -> flatten(node, kept)
            }
        }
    }
    return checkNotNull(visit(root)) { "Normalization removed the whole tree" }
}

private fun flatten(split: SplitNode, kept: List<Pair<DockNode, Float>>): SplitNode {
    val children = mutableListOf<DockNode>()
    val weights = mutableListOf<Float>()
    val total = kept.sumOf { it.second.toDouble() }.toFloat()
    for ((child, weight) in kept) {
        val share = weight / total
        if (child is SplitNode && child.orientation == split.orientation) {
            children += child.children
            weights += child.weights.map { it * share }
        } else {
            children += child
            weights += share
        }
    }
    return split.copy(children = children, weights = weights)
}
