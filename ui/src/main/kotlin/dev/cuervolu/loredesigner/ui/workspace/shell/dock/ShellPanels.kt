package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.cuervolu.loredesigner.editor.dock.DockOrientation
import dev.cuervolu.loredesigner.editor.dock.PanelId

object ShellPanels {
    val Explorer = PanelId("explorer")
    val Properties = PanelId("properties")
    val Log = PanelId("log")

    val Sizing: Map<PanelId, PanelSizing> = mapOf(
        Explorer to PanelSizing(
            width = AxisSizing(preferred = 264.dp, min = 180.dp),
            height = AxisSizing(preferred = null, min = 0.dp),
            shrinkOrder = 1,
        ),
        Properties to PanelSizing(
            width = AxisSizing(preferred = 300.dp, min = 180.dp),
            height = AxisSizing(preferred = null, min = 0.dp),
            shrinkOrder = 0,
        ),
        Log to PanelSizing(
            width = AxisSizing(preferred = null, min = 0.dp),
            height = AxisSizing(preferred = 200.dp, min = 96.dp),
            shrinkOrder = 0,
        ),
    )
}

/** The visual side of a panel. Where it sits in the tree is decided by the layout, not by this. */
class DockPanelSpec(
    val label: @Composable () -> String,
    val content: @Composable () -> Unit,
    val sizing: PanelSizing,
    val header: (@Composable () -> Unit)? = null,
    val editorSurface: Boolean = false,
    val icon: ImageVector? = null,
)

typealias PanelRegistry = Map<PanelId, DockPanelSpec>

internal fun RenderNode.sizingAlong(orientation: DockOrientation, registry: PanelRegistry): AxisSizing = when (this) {
    is RenderGroup -> panelSizing(registry).along(orientation)

    is RenderSplit -> {
        val parts = children.map { it.sizingAlong(orientation, registry) }
        val preferred = parts.map { it.preferred }
        if (this.orientation == orientation) {
            val gaps = DockSeparator * (parts.size - 1)
            AxisSizing(
                preferred = if (preferred.all {
                        it != null
                    }
                ) {
                    preferred.filterNotNull().fold(gaps) { a, b -> a + b }
                } else {
                    null
                },
                min = parts.fold(gaps) { sum, part -> sum + part.min },
            )
        } else {
            AxisSizing(
                preferred = if (preferred.all { it != null }) preferred.filterNotNull().max() else null,
                min = parts.maxOf { it.min },
            )
        }
    }
}

internal fun RenderNode.shrinkOrder(registry: PanelRegistry): Int = when (this) {
    is RenderGroup -> panelSizing(registry).shrinkOrder
    is RenderSplit -> children.minOf { it.shrinkOrder(registry) }
}

private fun RenderGroup.panelSizing(registry: PanelRegistry): PanelSizing =
    displayed?.let { registry[it]?.sizing } ?: ViewSizing

internal fun RenderNode.panels(): Set<PanelId> = when (this) {
    is RenderGroup -> tabs.toSet()
    is RenderSplit -> children.flatMapTo(mutableSetOf()) { it.panels() }
}
