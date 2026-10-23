package dev.cuervolu.loredesigner.editor.dock

import kotlin.math.abs

private fun rejected(reason: DockRejection) = DockResult.Rejected(reason)

internal fun DockLayout.applyOperation(operation: DockOperation, ids: DockIdGenerator): DockResult = when (operation) {
    is DockOperation.AddPanel -> addPanel(operation, ids)

    is DockOperation.Activate -> activate(operation.panel)

    is DockOperation.Close -> close(operation.panel)

    is DockOperation.Move -> move(operation.panel, operation.target, operation.share, ids)

    is DockOperation.Split ->
        move(operation.panel, DockTarget.Edge(operation.group, operation.side), operation.share, ids)

    is DockOperation.SetWeights -> setWeights(operation.split, operation.weights)
}

private fun DockLayout.setWeights(split: DockNodeId, weights: List<Float>): DockResult {
    val node = findSplit(split) ?: return rejected(DockRejection.UnknownSplit(split))
    val valid = weights.size == node.children.size &&
        weights.all { it > 0f } &&
        abs(weights.sum() - 1f) <= WEIGHT_TOLERANCE
    if (!valid) return rejected(DockRejection.InvalidWeights(split, weights))
    return DockResult.Applied(copy(root = root.mapSplit(split) { it.copy(weights = weights) }))
}

private fun DockNode.mapSplit(id: DockNodeId, transform: (SplitNode) -> SplitNode): DockNode = when (this) {
    is PanelGroup -> this
    is SplitNode -> if (this.id == id) transform(this) else copy(children = children.map { it.mapSplit(id, transform) })
}

private fun DockLayout.addPanel(op: DockOperation.AddPanel, ids: DockIdGenerator): DockResult {
    if (op.panel in panels) return rejected(DockRejection.DuplicatePanel(op.panel))
    if (!DockSplitPolicy.isValidShare(op.share)) return rejected(DockRejection.InvalidShare(op.share))
    val withPanel = copy(panels = panels + (op.panel to op.kind))
    val target = op.target
    if (target != null) {
        return withPanel.place(op.panel, op.kind, target, op.share, ids)
    }
    val sameKind = groups.firstOrNull { it.kind == op.kind }
    if (sameKind != null) {
        return withPanel.place(op.panel, op.kind, DockTarget.Tab(sameKind.id), op.share, ids)
    }
    // Only tools can lack a group: the view group is an invariant.
    val group = PanelGroup(ids.next(), op.kind, listOf(op.panel), op.panel)
    val wrapped = wrapBeside(root, group, DockSide.Bottom, op.share, ids)
    return DockResult.Applied(withPanel.copy(root = normalize(wrapped)))
}

private fun DockLayout.activate(panel: PanelId): DockResult {
    val group = groupOf(panel) ?: return rejected(DockRejection.UnknownPanel(panel))
    if (group.activePanel == panel) return DockResult.Applied(this)
    return DockResult.Applied(copy(root = root.mapGroup(group.id) { it.copy(activePanel = panel) }))
}

private fun DockLayout.close(panel: PanelId): DockResult {
    if (groupOf(panel) == null) return rejected(DockRejection.UnknownPanel(panel))
    return DockResult.Applied(DockLayout(normalize(root.withoutPanel(panel)), panels - panel))
}

private fun DockLayout.move(panel: PanelId, target: DockTarget, share: Float, ids: DockIdGenerator): DockResult {
    val kind = panels[panel] ?: return rejected(DockRejection.UnknownPanel(panel))
    if (!DockSplitPolicy.isValidShare(share)) return rejected(DockRejection.InvalidShare(share))
    val source = checkNotNull(groupOf(panel)) { "Valid layout has every catalogued panel placed" }
    if (target is DockTarget.Edge && target.group == source.id && source.panels.size == 1) {
        return rejected(DockRejection.SelfSplit(panel, source.id))
    }
    return copy(root = root.withoutPanel(panel)).place(panel, kind, target, share, ids)
}

/** Inserts an already-detached [panel] at [target] and normalizes. */
private fun DockLayout.place(
    panel: PanelId,
    kind: PanelKind,
    target: DockTarget,
    share: Float,
    ids: DockIdGenerator,
): DockResult {
    val anchor = findGroup(target.group) ?: return rejected(DockRejection.UnknownGroup(target.group))
    val newRoot = when (target) {
        is DockTarget.Tab -> {
            if (anchor.kind != kind) return rejected(DockRejection.KindMismatch(panel, kind, anchor.id))
            root.mapGroup(anchor.id) { it.withPanelAt(panel, target.index) }
        }

        is DockTarget.Edge -> {
            val group = PanelGroup(ids.next(), kind, listOf(panel), panel)
            root.insertBeside(anchor.id, group, target.side, share, ids)
        }
    }
    return DockResult.Applied(copy(root = normalize(newRoot)))
}

private val DockTarget.group: DockNodeId
    get() = when (this) {
        is DockTarget.Tab -> group
        is DockTarget.Edge -> group
    }

private fun PanelGroup.withPanelAt(panel: PanelId, index: Int?): PanelGroup {
    val at = (index ?: panels.size).coerceIn(0, panels.size)
    return copy(panels = panels.toMutableList().apply { add(at, panel) }, activePanel = panel)
}

private fun DockNode.mapGroup(id: DockNodeId, transform: (PanelGroup) -> PanelGroup): DockNode = when (this) {
    is PanelGroup -> if (this.id == id) transform(this) else this
    is SplitNode -> copy(children = children.map { it.mapGroup(id, transform) })
}

/** Leaves the (possibly empty) group in place. [normalize] decides whether it survives. */
private fun DockNode.withoutPanel(panel: PanelId): DockNode = when (this) {
    is PanelGroup -> if (panel !in panels) this else removeFromGroup(panel)
    is SplitNode -> copy(children = children.map { it.withoutPanel(panel) })
}

private fun PanelGroup.removeFromGroup(panel: PanelId): PanelGroup {
    val index = panels.indexOf(panel)
    val remaining = panels - panel
    val active = if (activePanel == panel) remaining.getOrNull(index) ?: remaining.getOrNull(index - 1) else activePanel
    return copy(panels = remaining, activePanel = active)
}

private fun DockNode.insertBeside(
    anchor: DockNodeId,
    group: PanelGroup,
    side: DockSide,
    share: Float,
    ids: DockIdGenerator,
): DockNode = when (this) {
    is PanelGroup -> if (id == anchor) wrapBeside(this, group, side, share, ids) else this

    is SplitNode -> {
        val index = children.indexOfFirst { it.id == anchor }
        if (index >= 0 && orientation == side.orientation) {
            val anchorWeight = weights[index]
            val newWeight = anchorWeight * share
            val keptWeight = anchorWeight - newWeight
            val at = if (side.leading) index else index + 1
            val newChildren = children.toMutableList().apply { add(at, group) }
            val newWeights = weights.toMutableList().apply {
                set(index, keptWeight)
                add(at, newWeight)
            }
            copy(children = newChildren, weights = newWeights)
        } else {
            copy(children = children.map { it.insertBeside(anchor, group, side, share, ids) })
        }
    }
}

private fun wrapBeside(
    node: DockNode,
    group: PanelGroup,
    side: DockSide,
    share: Float,
    ids: DockIdGenerator,
): SplitNode = if (side.leading) {
    SplitNode(ids.next(), side.orientation, listOf(group, node), listOf(share, 1f - share))
} else {
    SplitNode(ids.next(), side.orientation, listOf(node, group), listOf(1f - share, share))
}
