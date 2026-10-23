package dev.cuervolu.loredesigner.editor.dock

enum class PanelKind {
    /** Auxiliary panels (explorer, properties, logs). Never share a group with views. */
    Tool,

    /** Content panels (documents). Live in groups that make up the editor area. */
    View,
}

enum class DockOrientation {
    /** Children are laid out side by side. */
    Horizontal,

    /** Children are stacked top to bottom. */
    Vertical,
}

enum class DockSide(val orientation: DockOrientation, val leading: Boolean) {
    Left(DockOrientation.Horizontal, leading = true),
    Right(DockOrientation.Horizontal, leading = false),
    Top(DockOrientation.Vertical, leading = true),
    Bottom(DockOrientation.Vertical, leading = false),
}

sealed interface DockNode {
    val id: DockNodeId
}

data class SplitNode(
    override val id: DockNodeId,
    val orientation: DockOrientation,
    val children: List<DockNode>,
    val weights: List<Float>,
) : DockNode

data class PanelGroup(
    override val id: DockNodeId,
    val kind: PanelKind,
    val panels: List<PanelId>,
    val activePanel: PanelId?,
) : DockNode
