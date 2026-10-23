package dev.cuervolu.loredesigner.editor.dock

import kotlin.math.abs

internal const val WEIGHT_TOLERANCE = 1e-4f

internal fun validateLayout(layout: DockLayout): List<DockViolation> {
    val violations = mutableListOf<DockViolation>()
    val seenNodes = mutableSetOf<DockNodeId>()
    val placed = mutableSetOf<PanelId>()
    val groups = layout.groups

    fun visit(node: DockNode, parentOrientation: DockOrientation?) {
        if (!seenNodes.add(node.id)) violations += DockViolation.DuplicateNodeId(node.id)
        when (node) {
            is PanelGroup -> {
                node.panels.forEach { panel ->
                    if (!placed.add(panel)) violations += DockViolation.DuplicatePanel(panel)
                    val kind = layout.panels[panel]
                    if (kind == null) {
                        violations += DockViolation.PanelNotInCatalog(panel)
                    } else if (kind != node.kind) {
                        violations += DockViolation.WrongPanelKind(panel, node.id)
                    }
                }
                val activeOk = if (node.panels.isEmpty()) node.activePanel == null else node.activePanel in node.panels
                if (!activeOk) violations += DockViolation.InvalidActivePanel(node.id)
                if (node.panels.isEmpty() && node.kind == PanelKind.Tool) {
                    violations += DockViolation.EmptyToolGroup(node.id)
                }
            }

            is SplitNode -> {
                if (node.children.size < 2) violations += DockViolation.TooFewChildren(node.id)
                if (node.weights.size != node.children.size) {
                    violations += DockViolation.WeightCountMismatch(node.id)
                } else {
                    if (node.weights.any { it <= 0f }) violations += DockViolation.NonPositiveWeight(node.id)
                    if (abs(node.weights.sum() - 1f) > WEIGHT_TOLERANCE) {
                        violations += DockViolation.WeightsNotNormalized(node.id)
                    }
                }
                if (node.orientation == parentOrientation) violations += DockViolation.SameOrientationNesting(node.id)
                node.children.forEach { visit(it, node.orientation) }
            }
        }
    }
    visit(layout.root, null)

    (layout.panels.keys - placed).forEach { violations += DockViolation.PanelNotPlaced(it) }

    val viewGroups = groups.filter { it.kind == PanelKind.View }
    if (viewGroups.isEmpty()) violations += DockViolation.NoViewGroup
    val emptyViewGroups = viewGroups.filter { it.panels.isEmpty() }
    val redundant = if (emptyViewGroups.size < viewGroups.size) emptyViewGroups else emptyViewGroups.drop(1)
    redundant.forEach { violations += DockViolation.RedundantEmptyViewGroup(it.id) }
    return violations
}
