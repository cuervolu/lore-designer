package dev.cuervolu.loredesigner.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.theme.DURATION_FAST_MILLIS
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreSizes
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.StandardEasing
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.sizes
import dev.cuervolu.loredesigner.ui.theme.typography

/** Compact selectable row used in navigation trees (the design system's `TreeItem`). */
@Composable
fun LoreNavItem(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val background by animateColorAsState(
        targetValue = when {
            selected -> Theme[colors][LoreColors.accentSubtle]
            hovered -> Theme[colors][LoreColors.surfaceEditor]
            else -> Color.Transparent
        },
        animationSpec = tween(DURATION_FAST_MILLIS, easing = StandardEasing),
    )
    Text(
        text = label,
        style = Theme[typography][LoreTypography.body],
        color = if (selected) Theme[colors][LoreColors.accent] else Theme[colors][LoreColors.textPrimary],
        singleLine = true,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .fillMaxWidth()
            .height(Theme[sizes][LoreSizes.controlHeightSmall])
            .background(background, Theme[shapes][LoreShapes.control])
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
            )
            .padding(start = 19.dp, end = 8.dp)
            .wrapContentHeight(),
    )
}
