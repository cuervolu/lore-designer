package dev.cuervolu.loredesigner.ui.theme

import androidx.compose.ui.graphics.Color
import com.composeunstyled.theme.ThemeProperty
import com.composeunstyled.theme.ThemeToken
import dev.cuervolu.loredesigner.core.workspace.ProjectColor

val colors = ThemeProperty<Color>("colors")

object LoreColors {
    val background = ThemeToken<Color>("background")
    val surface = ThemeToken<Color>("surface")
    val surfaceEditor = ThemeToken<Color>("surface_editor")
    val surfaceRaised = ThemeToken<Color>("surface_raised")

    val textPrimary = ThemeToken<Color>("text_primary")
    val textSecondary = ThemeToken<Color>("text_secondary")
    val textMuted = ThemeToken<Color>("text_muted")
    val textOnAccent = ThemeToken<Color>("text_on_accent")

    val border = ThemeToken<Color>("border")
    val borderSubtle = ThemeToken<Color>("border_subtle")

    val accent = ThemeToken<Color>("accent")
    val accentHover = ThemeToken<Color>("accent_hover")
    val accentSubtle = ThemeToken<Color>("accent_subtle")
    val focusRing = ThemeToken<Color>("focus_ring")

    val danger = ThemeToken<Color>("danger")
    val dangerSubtle = ThemeToken<Color>("danger_subtle")
    val success = ThemeToken<Color>("success")
    val warning = ThemeToken<Color>("warning")

    val overlayScrim = ThemeToken<Color>("overlay_scrim")

    val projectViolet = ThemeToken<Color>("project_violet")
    val projectVioletSubtle = ThemeToken<Color>("project_violet_subtle")
    val projectBlue = ThemeToken<Color>("project_blue")
    val projectBlueSubtle = ThemeToken<Color>("project_blue_subtle")
    val projectGreen = ThemeToken<Color>("project_green")
    val projectGreenSubtle = ThemeToken<Color>("project_green_subtle")
}

/** Color of the small identity dot and icon glyph for a project. */
fun ProjectColor.dotToken(): ThemeToken<Color> = when (this) {
    ProjectColor.VIOLET -> LoreColors.projectViolet
    ProjectColor.BLUE -> LoreColors.projectBlue
    ProjectColor.GREEN -> LoreColors.projectGreen
}

/** Subtle tint behind a project's icon. */
fun ProjectColor.tintToken(): ThemeToken<Color> = when (this) {
    ProjectColor.VIOLET -> LoreColors.projectVioletSubtle
    ProjectColor.BLUE -> LoreColors.projectBlueSubtle
    ProjectColor.GREEN -> LoreColors.projectGreenSubtle
}

internal val LightColors: Map<ThemeToken<Color>, Color> = mapOf(
    LoreColors.background to Color(0xFFF5F5F7),
    LoreColors.surface to Color(0xFFFAFAFB),
    LoreColors.surfaceEditor to Color(0xFFFFFFFF),
    LoreColors.surfaceRaised to Color(0xFFFFFFFF),
    LoreColors.textPrimary to Color(0xFF25252B),
    LoreColors.textSecondary to Color(0xFF696A73),
    LoreColors.textMuted to Color(0xFF9697A1),
    LoreColors.textOnAccent to Color(0xFFFFFFFF),
    LoreColors.border to Color(0xFFDFDFE4),
    LoreColors.borderSubtle to Color(0xFFEAEAED),
    LoreColors.accent to Color(0xFF7F73CC),
    LoreColors.accentHover to Color(0xFF6455C1),
    LoreColors.accentSubtle to Color(0xFFEEECFA),
    LoreColors.focusRing to Color(0xFF7F73CC),
    LoreColors.danger to Color(0xFFC15353),
    LoreColors.dangerSubtle to Color(0xFFFBEAEA),
    LoreColors.success to Color(0xFF489160),
    LoreColors.warning to Color(0xFFB68531),
    LoreColors.overlayScrim to Color(0x52141418),
    LoreColors.projectViolet to Color(0xFF7F73CC),
    LoreColors.projectVioletSubtle to Color(0xFFEEECFA),
    LoreColors.projectBlue to Color(0xFF5382E5),
    LoreColors.projectBlueSubtle to Color(0xFFE9EFFE),
    LoreColors.projectGreen to Color(0xFF489160),
    LoreColors.projectGreenSubtle to Color(0xFFE9F4EC),
)

internal val DarkColors: Map<ThemeToken<Color>, Color> = mapOf(
    LoreColors.background to Color(0xFF18181D),
    LoreColors.surface to Color(0xFF202027),
    LoreColors.surfaceEditor to Color(0xFF24242C),
    LoreColors.surfaceRaised to Color(0xFF292930),
    LoreColors.textPrimary to Color(0xFFE6E6EA),
    LoreColors.textSecondary to Color(0xFFAAAAB5),
    LoreColors.textMuted to Color(0xFF757682),
    LoreColors.textOnAccent to Color(0xFF18181D),
    LoreColors.border to Color(0xFF34343D),
    LoreColors.borderSubtle to Color(0xFF2B2B33),
    LoreColors.accent to Color(0xFF9B90DD),
    LoreColors.accentHover to Color(0xFFB7AFE7),
    LoreColors.accentSubtle to Color(0xFF302C49),
    LoreColors.focusRing to Color(0xFF9B90DD),
    LoreColors.danger to Color(0xFFDA7778),
    LoreColors.dangerSubtle to Color(0xFF3A2529),
    LoreColors.success to Color(0xFF74B788),
    LoreColors.warning to Color(0xFFCEA350),
    LoreColors.overlayScrim to Color(0x80000000),
    LoreColors.projectViolet to Color(0xFF9B90DD),
    LoreColors.projectVioletSubtle to Color(0xFF302C49),
    LoreColors.projectBlue to Color(0xFF7CA2EF),
    LoreColors.projectBlueSubtle to Color(0xFF252D42),
    LoreColors.projectGreen to Color(0xFF74B788),
    LoreColors.projectGreenSubtle to Color(0xFF243A2C),
)
