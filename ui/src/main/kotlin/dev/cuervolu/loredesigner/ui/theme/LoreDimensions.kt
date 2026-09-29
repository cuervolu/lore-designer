package dev.cuervolu.loredesigner.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.ThemeProperty
import com.composeunstyled.theme.ThemeToken

val spacing = ThemeProperty<Dp>("spacing")
val sizes = ThemeProperty<Dp>("sizes")

/** Numbered to match the design system's `--space-N` scale one to one. */
object LoreSpacing {
    val space1 = ThemeToken<Dp>("space_1")
    val space2 = ThemeToken<Dp>("space_2")
    val space3 = ThemeToken<Dp>("space_3")
    val space4 = ThemeToken<Dp>("space_4")
    val space5 = ThemeToken<Dp>("space_5")
    val space6 = ThemeToken<Dp>("space_6")
    val space7 = ThemeToken<Dp>("space_7")
    val space8 = ThemeToken<Dp>("space_8")
    val space9 = ThemeToken<Dp>("space_9")
    val space10 = ThemeToken<Dp>("space_10")
    val space11 = ThemeToken<Dp>("space_11")
    val space12 = ThemeToken<Dp>("space_12")
}

object LoreSizes {
    val titleBarHeight = ThemeToken<Dp>("title_bar_height")
    val controlHeight = ThemeToken<Dp>("control_height")
    val controlHeightSmall = ThemeToken<Dp>("control_height_small")
    val iconButton = ThemeToken<Dp>("icon_button")
    val icon = ThemeToken<Dp>("icon")
}

internal val SpacingValues: Map<ThemeToken<Dp>, Dp> = mapOf(
    LoreSpacing.space1 to 2.dp,
    LoreSpacing.space2 to 4.dp,
    LoreSpacing.space3 to 6.dp,
    LoreSpacing.space4 to 8.dp,
    LoreSpacing.space5 to 12.dp,
    LoreSpacing.space6 to 16.dp,
    LoreSpacing.space7 to 20.dp,
    LoreSpacing.space8 to 24.dp,
    LoreSpacing.space9 to 32.dp,
    LoreSpacing.space10 to 40.dp,
    LoreSpacing.space11 to 48.dp,
    LoreSpacing.space12 to 64.dp,
)

internal val SizeValues: Map<ThemeToken<Dp>, Dp> = mapOf(
    LoreSizes.titleBarHeight to 40.dp,
    LoreSizes.controlHeight to 30.dp,
    LoreSizes.controlHeightSmall to 26.dp,
    LoreSizes.iconButton to 26.dp,
    LoreSizes.icon to 14.dp,
)
