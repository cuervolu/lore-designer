package dev.cuervolu.loredesigner.editor.dock

/**
 * The tree only references panels by [PanelId]. What a panel is lives in [panels]. The constructor does not
 * normalize or validate, so a layout from an untrusted source can be inspected with [validate] and is rejected
 * by [apply] rather than silently repaired.
 */
data class DockLayout(val root: DockNode, val panels: Map<PanelId, PanelKind>) {
    val groups: List<PanelGroup> get() = root.groups()

    fun findGroup(id: DockNodeId): PanelGroup? = groups.firstOrNull { it.id == id }

    fun groupOf(panel: PanelId): PanelGroup? = groups.firstOrNull { panel in it.panels }

    fun validate(): List<DockViolation> = validateLayout(this)

    fun apply(operation: DockOperation, ids: DockIdGenerator = DockIdGenerator.Random): DockResult {
        val violations = validate()
        if (violations.isNotEmpty()) return DockResult.Rejected(DockRejection.InvalidLayout(violations))
        val result = applyOperation(operation, ids)
        if (result is DockResult.Applied) {
            val broken = result.layout.validate()
            check(broken.isEmpty()) { "$operation produced an invalid layout: $broken" }
        }
        return result
    }

    companion object {
        fun initial(ids: DockIdGenerator = DockIdGenerator.Random): DockLayout =
            DockLayout(PanelGroup(ids.next(), PanelKind.View, emptyList(), activePanel = null), emptyMap())
    }
}

internal fun DockNode.groups(): List<PanelGroup> = when (this) {
    is PanelGroup -> listOf(this)
    is SplitNode -> children.flatMap { it.groups() }
}
