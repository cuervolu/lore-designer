package dev.cuervolu.loredesigner.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.UnstyledButton
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.theme.DURATION_FAST_MILLIS
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreSizes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.StandardEasing
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.sizes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography

enum class LoreButtonVariant { Primary, Secondary, Ghost, Danger }

enum class LoreButtonSize { Medium, Small }

private const val DISABLED_ALPHA = 0.5f

@Composable
fun LoreButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: LoreButtonVariant = LoreButtonVariant.Secondary,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    size: LoreButtonSize = LoreButtonSize.Medium,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val palette = variant.palette()
    val shape = Theme[shapes][LoreShapes.control]
    val background by animateColorAsState(
        targetValue = if (hovered && enabled) palette.hoverBackground else palette.background,
        animationSpec = tween(DURATION_FAST_MILLIS, easing = StandardEasing),
    )

    UnstyledButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        contentPadding = PaddingValues(
            horizontal = if (size == LoreButtonSize.Small) 10.dp else Theme[spacing][LoreSpacing.space5],
        ),
        modifier = modifier
            .height(
                Theme[sizes][
                    if (size ==
                        LoreButtonSize.Small
                    ) {
                        LoreSizes.controlHeightSmall
                    } else {
                        LoreSizes.controlHeight
                    },
                ],
            )
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .background(background, shape)
            .border(1.dp, palette.border, shape),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space3]),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Image(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    colorFilter = ColorFilter.tint(palette.content),
                )
            }
            Text(
                text = text,
                style = if (size == LoreButtonSize.Small) {
                    Theme[typography][LoreTypography.caption].copy(fontWeight = FontWeight.Medium)
                } else {
                    Theme[typography][LoreTypography.label]
                },
                color = palette.content,
                singleLine = true,
            )
        }
    }
}

private class ButtonPalette(val background: Color, val hoverBackground: Color, val content: Color, val border: Color)

@Composable
private fun LoreButtonVariant.palette(): ButtonPalette = when (this) {
    LoreButtonVariant.Primary -> ButtonPalette(
        background = Theme[colors][LoreColors.accent],
        hoverBackground = Theme[colors][LoreColors.accentHover],
        content = Theme[colors][LoreColors.textOnAccent],
        border = Color.Transparent,
    )

    LoreButtonVariant.Secondary -> ButtonPalette(
        background = Theme[colors][LoreColors.surfaceEditor],
        hoverBackground = Theme[colors][LoreColors.surface],
        content = Theme[colors][LoreColors.textPrimary],
        border = Theme[colors][LoreColors.border],
    )

    LoreButtonVariant.Ghost -> ButtonPalette(
        background = Color.Transparent,
        hoverBackground = Theme[colors][LoreColors.surface],
        content = Theme[colors][LoreColors.textSecondary],
        border = Color.Transparent,
    )

    LoreButtonVariant.Danger -> ButtonPalette(
        background = Theme[colors][LoreColors.danger],
        hoverBackground = Theme[colors][LoreColors.danger],
        content = Color.White,
        border = Color.Transparent,
    )
}
