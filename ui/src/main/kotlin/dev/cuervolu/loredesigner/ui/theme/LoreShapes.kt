package dev.cuervolu.loredesigner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.ThemeProperty
import com.composeunstyled.theme.ThemeToken

val shapes = ThemeProperty<Shape>("shapes")

// Panels are intentionally flat (0dp) so chrome sits flush against its neighbours; they get no token.
object LoreShapes {
    val control = ThemeToken<Shape>("control")
    val menu = ThemeToken<Shape>("menu")
    val popover = ThemeToken<Shape>("popover")
    val modal = ThemeToken<Shape>("modal")
    val thumbnail = ThemeToken<Shape>("thumbnail")
    val pill = ThemeToken<Shape>("pill")
}

internal val ShapeValues: Map<ThemeToken<Shape>, Shape> = mapOf(
    LoreShapes.control to RoundedCornerShape(6.dp),
    LoreShapes.menu to RoundedCornerShape(8.dp),
    LoreShapes.popover to RoundedCornerShape(8.dp),
    LoreShapes.modal to RoundedCornerShape(10.dp),
    LoreShapes.thumbnail to RoundedCornerShape(4.dp),
    LoreShapes.pill to RoundedCornerShape(percent = 50),
)
