package dev.cuervolu.loredesigner.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import com.composeunstyled.theme.ColorScheme
import com.composeunstyled.theme.ColorSchemedThemeBuilder
import com.composeunstyled.theme.ThemeToken
import com.composeunstyled.theme.buildThemeV2

internal val StandardEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
internal const val DURATION_FAST_MILLIS = 100
internal const val DURATION_BASE_MILLIS = 150

private val LoreTheme = buildThemeV2 {
    name = "LoreDesigner"

    val typographyValues = loreTypography()
    properties[typography] = typographyValues
    properties[spacing] = SpacingValues
    properties[sizes] = SizeValues
    properties[shapes] = ShapeValues
    defaultTextStyle = typographyValues.getValue(LoreTypography.body)
    colorSchemeTransitionSpec = tween(DURATION_BASE_MILLIS, easing = StandardEasing)

    colorScheme(ColorScheme.Light) { applyScheme(LightColors, LightShadows) }
    colorScheme(ColorScheme.Dark) { applyScheme(DarkColors, DarkShadows) }
}

private fun ColorSchemedThemeBuilder.applyScheme(
    values: Map<ThemeToken<Color>, Color>,
    shadowValues: Map<ThemeToken<List<Shadow>>, List<Shadow>>,
) {
    properties[colors] = values
    properties[shadows] = shadowValues
    defaultContentColor = values.getValue(LoreColors.textPrimary)
    val accent = values.getValue(LoreColors.accent)
    // The selection background is drawn over the text, so it needs alpha to keep glyphs legible;
    // the opaque accentSubtle token would hide them.
    defaultTextSelectionColors = TextSelectionColors(
        handleColor = accent,
        backgroundColor = accent.copy(alpha = 0.4f),
    )
}

@Composable
fun LoreDesignerTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    LoreTheme(if (darkTheme) ColorScheme.Dark else ColorScheme.Light, content)
}
