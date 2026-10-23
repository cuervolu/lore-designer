package dev.cuervolu.loredesigner.editor.dock

object DockSplitPolicy {
    /** Fraction of the anchor's space handed to a newly created group. */
    const val DEFAULT_SHARE: Float = 0.5f

    fun isValidShare(share: Float): Boolean = share > 0f && share < 1f
}

sealed interface DockTarget {
    /** Drop into the tab strip of [group]; [index] is the final position, clamped to the strip. Null appends. */
    data class Tab(val group: DockNodeId, val index: Int? = null) : DockTarget

    /** Create a new group on [side] of [group]. */
    data class Edge(val group: DockNodeId, val side: DockSide) : DockTarget
}

sealed interface DockOperation {
    /** Without [target], goes to the first group of its kind, creating a bottom tool group if none exists. */
    data class AddPanel(
        val panel: PanelId,
        val kind: PanelKind,
        val target: DockTarget? = null,
        val share: Float = DockSplitPolicy.DEFAULT_SHARE,
    ) : DockOperation

    data class Activate(val panel: PanelId) : DockOperation

    /** Destroys the panel instance. It is not a hide: nothing about the panel is retained. */
    data class Close(val panel: PanelId) : DockOperation

    data class Move(val panel: PanelId, val target: DockTarget, val share: Float = DockSplitPolicy.DEFAULT_SHARE) :
        DockOperation

    data class Split(
        val panel: PanelId,
        val group: DockNodeId,
        val side: DockSide,
        val share: Float = DockSplitPolicy.DEFAULT_SHARE,
    ) : DockOperation
}

sealed interface DockRejection {
    data class InvalidLayout(val violations: List<DockViolation>) : DockRejection

    data class UnknownPanel(val panel: PanelId) : DockRejection

    data class UnknownGroup(val group: DockNodeId) : DockRejection

    data class DuplicatePanel(val panel: PanelId) : DockRejection

    data class KindMismatch(val panel: PanelId, val panelKind: PanelKind, val group: DockNodeId) : DockRejection

    data class InvalidShare(val share: Float) : DockRejection

    /** Splitting a group against its own edge with its only panel would just recreate the same group. */
    data class SelfSplit(val panel: PanelId, val group: DockNodeId) : DockRejection
}

sealed interface DockResult {
    data class Applied(val layout: DockLayout) : DockResult

    data class Rejected(val reason: DockRejection) : DockResult
}
