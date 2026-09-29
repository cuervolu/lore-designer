package dev.cuervolu.loredesigner.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import com.composeunstyled.UnstyledButton
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.theme.DURATION_FAST_MILLIS
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreSizes
import dev.cuervolu.loredesigner.ui.theme.StandardEasing
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.sizes

@Composable
fun LoreIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = Theme[shapes][LoreShapes.control]
    val background by animateColorAsState(
        targetValue = if (hovered) Theme[colors][LoreColors.surface] else Color.Transparent,
        animationSpec = tween(DURATION_FAST_MILLIS, easing = StandardEasing),
    )

    UnstyledButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .size(Theme[sizes][LoreSizes.iconButton])
            .background(background, shape),
    ) {
        Image(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(Theme[sizes][LoreSizes.icon]),
            colorFilter = ColorFilter.tint(Theme[colors][LoreColors.textSecondary]),
        )
    }
}
