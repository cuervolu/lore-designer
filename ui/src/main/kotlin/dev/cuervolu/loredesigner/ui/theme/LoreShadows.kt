package dev.cuervolu.loredesigner.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.ThemeProperty
import com.composeunstyled.theme.ThemeToken

/** Layered shadows, drawn in order; only surfaces that genuinely float above the layout use them. */
val shadows = ThemeProperty<List<Shadow>>("shadows")

object LoreShadows {
    val modal = ThemeToken<List<Shadow>>("modal")
}

internal val LightShadows: Map<ThemeToken<List<Shadow>>, List<Shadow>> = mapOf(
    LoreShadows.modal to listOf(
        Shadow(radius = 40.dp, color = Color(0x2E14141A), offset = DpOffset(0.dp, 16.dp)),
        Shadow(radius = 6.dp, color = Color(0x1414141A), offset = DpOffset(0.dp, 2.dp)),
    ),
)

internal val DarkShadows: Map<ThemeToken<List<Shadow>>, List<Shadow>> = mapOf(
    LoreShadows.modal to listOf(
        Shadow(radius = 48.dp, color = Color(0x99000000), offset = DpOffset(0.dp, 20.dp)),
        Shadow(radius = 6.dp, color = Color(0x59000000), offset = DpOffset(0.dp, 2.dp)),
    ),
)
