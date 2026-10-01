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
    val projectCyan = ThemeToken<Color>("project_cyan")
    val projectCyanSubtle = ThemeToken<Color>("project_cyan_subtle")
    val projectTeal = ThemeToken<Color>("project_teal")
    val projectTealSubtle = ThemeToken<Color>("project_teal_subtle")
    val projectGreen = ThemeToken<Color>("project_green")
    val projectGreenSubtle = ThemeToken<Color>("project_green_subtle")
    val projectAmber = ThemeToken<Color>("project_amber")
    val projectAmberSubtle = ThemeToken<Color>("project_amber_subtle")
    val projectOrange = ThemeToken<Color>("project_orange")
    val projectOrangeSubtle = ThemeToken<Color>("project_orange_subtle")
    val projectRose = ThemeToken<Color>("project_rose")
    val projectRoseSubtle = ThemeToken<Color>("project_rose_subtle")
    val projectRed = ThemeToken<Color>("project_red")
    val projectRedSubtle = ThemeToken<Color>("project_red_subtle")
    val projectSlate = ThemeToken<Color>("project_slate")
    val projectSlateSubtle = ThemeToken<Color>("project_slate_subtle")
}

/** Color of the small identity dot and icon glyph for a project. */
fun ProjectColor.dotToken(): ThemeToken<Color> = when (this) {
    ProjectColor.VIOLET -> LoreColors.projectViolet
    ProjectColor.BLUE -> LoreColors.projectBlue
    ProjectColor.CYAN -> LoreColors.projectCyan
    ProjectColor.TEAL -> LoreColors.projectTeal
    ProjectColor.GREEN -> LoreColors.projectGreen
    ProjectColor.AMBER -> LoreColors.projectAmber
    ProjectColor.ORANGE -> LoreColors.projectOrange
    ProjectColor.ROSE -> LoreColors.projectRose
    ProjectColor.RED -> LoreColors.projectRed
    ProjectColor.SLATE -> LoreColors.projectSlate
}

/** Subtle tint behind a project's icon. */
fun ProjectColor.tintToken(): ThemeToken<Color> = when (this) {
    ProjectColor.VIOLET -> LoreColors.projectVioletSubtle
    ProjectColor.BLUE -> LoreColors.projectBlueSubtle
    ProjectColor.CYAN -> LoreColors.projectCyanSubtle
    ProjectColor.TEAL -> LoreColors.projectTealSubtle
    ProjectColor.GREEN -> LoreColors.projectGreenSubtle
    ProjectColor.AMBER -> LoreColors.projectAmberSubtle
    ProjectColor.ORANGE -> LoreColors.projectOrangeSubtle
    ProjectColor.ROSE -> LoreColors.projectRoseSubtle
    ProjectColor.RED -> LoreColors.projectRedSubtle
    ProjectColor.SLATE -> LoreColors.projectSlateSubtle
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
    LoreColors.projectCyan to Color(0xFF2C99A0),
    LoreColors.projectCyanSubtle to Color(0xFFE5F4F5),
    LoreColors.projectTeal to Color(0xFF2C907A),
    LoreColors.projectTealSubtle to Color(0xFFE5F5F0),
    LoreColors.projectGreen to Color(0xFF489160),
    LoreColors.projectGreenSubtle to Color(0xFFE9F4EC),
    LoreColors.projectAmber to Color(0xFFB68531),
    LoreColors.projectAmberSubtle to Color(0xFFFBF1E1),
    LoreColors.projectOrange to Color(0xFFC67440),
    LoreColors.projectOrangeSubtle to Color(0xFFFBEEE5),
    LoreColors.projectRose to Color(0xFFC36484),
    LoreColors.projectRoseSubtle to Color(0xFFFAEBF0),
    LoreColors.projectRed to Color(0xFFC15353),
    LoreColors.projectRedSubtle to Color(0xFFFBEAEA),
    LoreColors.projectSlate to Color(0xFF65686E),
    LoreColors.projectSlateSubtle to Color(0xFFEBEBEC),
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
    LoreColors.projectCyan to Color(0xFF5DB8BE),
    LoreColors.projectCyanSubtle to Color(0xFF20343A),
    LoreColors.projectTeal to Color(0xFF59B29A),
    LoreColors.projectTealSubtle to Color(0xFF1F3833),
    LoreColors.projectGreen to Color(0xFF74B788),
    LoreColors.projectGreenSubtle to Color(0xFF243A2C),
    LoreColors.projectAmber to Color(0xFFCEA350),
    LoreColors.projectAmberSubtle to Color(0xFF3A3221),
    LoreColors.projectOrange to Color(0xFFDA9263),
    LoreColors.projectOrangeSubtle to Color(0xFF3A2C22),
    LoreColors.projectRose to Color(0xFFDD8FA7),
    LoreColors.projectRoseSubtle to Color(0xFF3A2530),
    LoreColors.projectRed to Color(0xFFDA7778),
    LoreColors.projectRedSubtle to Color(0xFF3A2529),
    LoreColors.projectSlate to Color(0xFF9B9DA5),
    LoreColors.projectSlateSubtle to Color(0xFF2C2D33),
)
