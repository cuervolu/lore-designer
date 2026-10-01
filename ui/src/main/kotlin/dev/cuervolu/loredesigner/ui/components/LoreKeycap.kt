package dev.cuervolu.loredesigner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.typography

private val KeycapShape = RoundedCornerShape(4.dp)

/** A single key of a keyboard shortcut; the thicker bottom edge mimics a physical key. */
@Composable
fun LoreKeycap(key: String, modifier: Modifier = Modifier) {
    val border = Theme[colors][LoreColors.border]
    Text(
        text = key,
        style = Theme[typography][LoreTypography.caption].copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
        color = Theme[colors][LoreColors.textSecondary],
        textAlign = TextAlign.Center,
        singleLine = true,
        modifier = modifier
            .defaultMinSize(minWidth = 20.dp)
            .height(20.dp)
            .background(Theme[colors][LoreColors.surface], KeycapShape)
            .border(1.dp, border, KeycapShape)
            .drawBehind {
                val y = size.height - 1.dp.toPx()
                drawLine(border, Offset(2.dp.toPx(), y), Offset(size.width - 2.dp.toPx(), y), strokeWidth = 1.dp.toPx())
            }
            .padding(horizontal = 5.dp)
            .wrapContentSize(),
    )
}
