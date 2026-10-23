package dev.cuervolu.loredesigner.ui.workspace.shell

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Dp of each region after fitting; zero means the region is not shown. */
data class ShellLayout(val explorerWidth: Dp, val propertiesWidth: Dp, val bottomHeight: Dp)

private val PreferredExplorerWidth = 264.dp
private val PreferredPropertiesWidth = 300.dp
private val PreferredBottomHeight = 200.dp
private val MinSidePanelWidth = 180.dp
private val MinBottomHeight = 96.dp
private val MinCenterWidth = 360.dp
private val MinCenterHeight = 160.dp

/**
 * Fits the preferred panel sizes into the available space while keeping the center usable.
 *
 * Properties gives way first, then Explorer, shrinking down to a floor and then hiding altogether.
 * The bottom panel yields height before the center does. Visibility flags are the user's
 * preference and are never changed here.
 */
fun shellLayout(
    width: Dp,
    height: Dp,
    explorerVisible: Boolean,
    propertiesVisible: Boolean,
    bottomVisible: Boolean,
): ShellLayout {
    var explorer = if (explorerVisible) PreferredExplorerWidth else 0.dp
    var properties = if (propertiesVisible) PreferredPropertiesWidth else 0.dp

    fun excess() = explorer + properties + MinCenterWidth - width

    if (excess() > 0.dp && properties > 0.dp) properties = maxOf(MinSidePanelWidth, properties - excess())
    if (excess() > 0.dp && explorer > 0.dp) explorer = maxOf(MinSidePanelWidth, explorer - excess())
    if (excess() > 0.dp && properties > 0.dp) properties = 0.dp
    if (excess() > 0.dp && explorer > 0.dp) explorer = 0.dp

    val bottomRoom = height - MinCenterHeight
    val bottom = when {
        !bottomVisible -> 0.dp
        bottomRoom < MinBottomHeight -> 0.dp
        else -> minOf(PreferredBottomHeight, bottomRoom)
    }
    return ShellLayout(explorer, properties, bottom)
}
