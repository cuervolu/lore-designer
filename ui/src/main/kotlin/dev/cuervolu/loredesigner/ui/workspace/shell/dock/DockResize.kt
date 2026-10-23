package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Dragging a panel below this fraction of its minimum size announces, and on release performs, its collapse. */
internal const val COLLAPSE_THRESHOLD = 0.7f

/** What a separator moves, as indices into the children currently shown in its split. */
internal sealed interface SeparatorPlan {
    /** Resizes [index]; the flexible area absorbs the change. [growsWithDelta] is false when it sits after the separator. */
    data class Fixed(val index: Int, val growsWithDelta: Boolean) : SeparatorPlan

    /** Two flexible neighbours trade space, so their sum stays the same. */
    data class Fill(val before: Int, val after: Int) : SeparatorPlan
}

internal fun planSeparator(sizings: List<AxisSizing>, before: Int, after: Int): SeparatorPlan {
    val beforeFixed = sizings[before].preferred != null
    val afterFixed = sizings[after].preferred != null
    return when {
        beforeFixed -> SeparatorPlan.Fixed(before, growsWithDelta = true)
        afterFixed -> SeparatorPlan.Fixed(after, growsWithDelta = false)
        else -> SeparatorPlan.Fill(before, after)
    }
}

/**
 * The most [index] can take: everything the split offers once every other child keeps at least its minimum.
 * Other fixed children still give way through [measureSplit], in shrink order, when the flexible area runs out.
 */
internal fun maxFixedSize(sizings: List<AxisSizing>, index: Int, available: Dp, separator: Dp = DockSeparator): Dp {
    val room = available - separator * (sizings.size - 1)
    val others = sizings.indices.filter { it != index }.fold(0.dp) { sum, i -> sum + sizings[i].min }
    return maxOf(sizings[index].min, room - others)
}

/**
 * [requested] is where the pointer asks the panel to be, before any limit. [size] is what is actually applied, and
 * [collapsing] is decided from the request, so a drag far past the minimum still counts although [size] stops at it.
 */
internal data class FixedDrag(val requested: Dp, val size: Dp, val collapsing: Boolean)

internal fun dragFixed(start: Dp, delta: Dp, min: Dp, max: Dp): FixedDrag {
    val requested = start + delta
    return FixedDrag(
        requested = requested,
        size = requested.coerceIn(min, max),
        collapsing = min > 0.dp && requested < min * COLLAPSE_THRESHOLD,
    )
}

/** Share of the pair's combined size that goes to the leading neighbour, within both minimums. */
internal fun dragFillShare(startBefore: Dp, startAfter: Dp, delta: Dp, minBefore: Dp, minAfter: Dp): Float {
    val total = startBefore + startAfter
    if (total <= 0.dp) return 0.5f
    val before = (startBefore + delta).coerceIn(minOf(minBefore, total), maxOf(minBefore, total - minAfter))
    return (before / total).coerceIn(0f, 1f)
}
