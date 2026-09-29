package dev.cuervolu.loredesigner.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.composeunstyled.theme.ThemeProperty
import com.composeunstyled.theme.ThemeToken
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.inter_bold
import dev.cuervolu.loredesigner.ui.resources.inter_medium
import dev.cuervolu.loredesigner.ui.resources.inter_regular
import dev.cuervolu.loredesigner.ui.resources.inter_semibold
import dev.cuervolu.loredesigner.ui.resources.jetbrains_mono_medium
import dev.cuervolu.loredesigner.ui.resources.jetbrains_mono_regular
import org.jetbrains.compose.resources.Font

val typography = ThemeProperty<TextStyle>("typography")

object LoreTypography {
    val title = ThemeToken<TextStyle>("title")
    val headingLarge = ThemeToken<TextStyle>("heading_large")
    val heading = ThemeToken<TextStyle>("heading")
    val body = ThemeToken<TextStyle>("body")
    val bodyStrong = ThemeToken<TextStyle>("body_strong")
    val label = ThemeToken<TextStyle>("label")
    val caption = ThemeToken<TextStyle>("caption")

    /** Small structural labels; callers uppercase the text themselves. */
    val eyebrow = ThemeToken<TextStyle>("eyebrow")
    val mono = ThemeToken<TextStyle>("mono")
}

@Composable
internal fun interFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
    Font(Res.font.inter_semibold, FontWeight.SemiBold),
    Font(Res.font.inter_bold, FontWeight.Bold),
)

@Composable
internal fun jetBrainsMonoFontFamily(): FontFamily = FontFamily(
    Font(Res.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(Res.font.jetbrains_mono_medium, FontWeight.Medium),
)

@Composable
internal fun loreTypography(): Map<ThemeToken<TextStyle>, TextStyle> {
    val ui = TextStyle(fontFamily = interFontFamily(), lineHeight = 1.45.em)
    val mono = jetBrainsMonoFontFamily()

    return mapOf(
        LoreTypography.title to ui.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 1.25.em),
        LoreTypography.headingLarge to ui.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
        LoreTypography.heading to ui.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
        LoreTypography.body to ui.copy(fontSize = 13.sp, fontWeight = FontWeight.Normal),
        LoreTypography.bodyStrong to ui.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
        LoreTypography.label to ui.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
        LoreTypography.caption to ui.copy(fontSize = 12.sp, fontWeight = FontWeight.Normal),
        LoreTypography.eyebrow to ui.copy(fontSize = 12.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.04.em),
        LoreTypography.mono to ui.copy(fontFamily = mono, fontSize = 12.sp, fontWeight = FontWeight.Normal),
    )
}
