package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.cuervolu.loredesigner.editor.dock.DockOrientation

/** Size of a panel along one axis. A null [preferred] means the panel fills whatever the others leave. */
data class AxisSizing(val preferred: Dp?, val min: Dp)

data class PanelSizing(val width: AxisSizing, val height: AxisSizing, val shrinkOrder: Int = 0) {
    fun along(orientation: DockOrientation): AxisSizing =
        if (orientation == DockOrientation.Horizontal) width else height
}

/** The center must stay usable, so it is the last thing to give way. */
val ViewSizing = PanelSizing(
    width = AxisSizing(preferred = null, min = 360.dp),
    height = AxisSizing(preferred = null, min = 160.dp),
)

data class MeasureChild(val sizing: AxisSizing, val weight: Float, val shrinkOrder: Int)

val DockSeparator = 1.dp

/**
 * Splits [available] among [children]. Children with a preferred size keep it, whatever the window size,
 * and the fill children absorb every change. When the fill children's minimum no longer fits, fixed children
 * shrink toward their own minimum and then collapse (size zero), lowest [MeasureChild.shrinkOrder] first.
 * Nothing here changes the layout or the user's visibility choices, so collapsed children return with room.
 */
fun measureSplit(children: List<MeasureChild>, available: Dp, separator: Dp = DockSeparator): List<Dp> {
    val alive = BooleanArray(children.size) { true }
    val sizes = children.map { it.sizing.preferred ?: 0.dp }.toMutableList()
    val fixed = children.indices.filter { children[it].sizing.preferred != null }.sortedBy { children[it].shrinkOrder }
    val fill = children.indices.filter { children[it].sizing.preferred == null }

    fun room() = available - separator * (alive.count { it } - 1).coerceAtLeast(0)
    fun excess() = fixed.filter { alive[it] }.fold(0.dp) { sum, i -> sum + sizes[i] } +
        fill.fold(0.dp) { sum, i -> sum + children[i].sizing.min } - room()

    for (i in fixed) {
        val over = excess()
        if (over > 0.dp) sizes[i] = maxOf(children[i].sizing.min, sizes[i] - over)
    }
    for (i in fixed) {
        if (excess() > 0.dp) {
            alive[i] = false
            sizes[i] = 0.dp
        }
    }

    val remaining = (room() - fixed.filter { alive[it] }.fold(0.dp) { sum, i -> sum + sizes[i] }).coerceAtLeast(0.dp)
    if (fill.isNotEmpty()) {
        val totalWeight = fill.sumOf { children[it].weight.toDouble() }.toFloat()
        fill.forEach { sizes[it] = remaining * (children[it].weight / totalWeight) }
    } else {
        val last = alive.indexOfLast { it }
        if (last >= 0) sizes[last] = sizes[last] + remaining
    }
    return sizes
}
