package dev.cuervolu.loredesigner.editor.dock

sealed interface DockViolation {
    data class DuplicateNodeId(val id: DockNodeId) : DockViolation

    data class DuplicatePanel(val panel: PanelId) : DockViolation

    data class PanelNotInCatalog(val panel: PanelId) : DockViolation

    data class PanelNotPlaced(val panel: PanelId) : DockViolation

    data class WrongPanelKind(val panel: PanelId, val group: DockNodeId) : DockViolation

    data class InvalidActivePanel(val group: DockNodeId) : DockViolation

    data class EmptyToolGroup(val group: DockNodeId) : DockViolation

    data class RedundantEmptyViewGroup(val group: DockNodeId) : DockViolation

    data object NoViewGroup : DockViolation

    data class TooFewChildren(val split: DockNodeId) : DockViolation

    data class WeightCountMismatch(val split: DockNodeId) : DockViolation

    data class NonPositiveWeight(val split: DockNodeId) : DockViolation

    data class WeightsNotNormalized(val split: DockNodeId) : DockViolation

    data class SameOrientationNesting(val split: DockNodeId) : DockViolation
}
