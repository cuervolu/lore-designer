package dev.cuervolu.loredesigner.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composeunstyled.SwitchThumb
import com.composeunstyled.UnstyledSwitch
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.theme.DURATION_FAST_MILLIS
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.StandardEasing
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.shapes

private const val DISABLED_ALPHA = 0.5f

@Composable
fun LoreSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val track by animateColorAsState(
        targetValue = if (checked) Theme[colors][LoreColors.accent] else Theme[colors][LoreColors.border],
        animationSpec = tween(DURATION_FAST_MILLIS, easing = StandardEasing),
    )
    UnstyledSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        accessibilityLabel = accessibilityLabel,
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .size(width = 32.dp, height = 18.dp)
            .background(track, Theme[shapes][LoreShapes.pill])
            .padding(2.dp),
    ) {
        SwitchThumb(
            modifier = Modifier.size(14.dp).background(Color.White, CircleShape),
            animationSpec = tween(DURATION_FAST_MILLIS, easing = StandardEasing),
        )
    }
}
