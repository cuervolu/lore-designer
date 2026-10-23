package dev.cuervolu.loredesigner.ui.chrome

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.composeunstyled.Text
import com.composeunstyled.UnstyledButton
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.components.LoreIconButton
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.titlebar_search
import dev.cuervolu.loredesigner.ui.resources.titlebar_settings
import dev.cuervolu.loredesigner.ui.theme.DURATION_FAST_MILLIS
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.StandardEasing
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography
import org.jetbrains.compose.resources.stringResource

@Composable
fun TitleBarSettingsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    LoreIconButton(
        icon = Lucide.Settings,
        contentDescription = stringResource(Res.string.titlebar_settings),
        onClick = onClick,
        modifier = modifier.padding(end = Theme[spacing][LoreSpacing.space3]),
    )
}

/** Compact search entry of the workspace title bar; stays inert while [onClick] is null. */
@Composable
fun TitleBarSearchButton(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = Theme[shapes][LoreShapes.control]
    val muted = Theme[colors][LoreColors.textMuted]
    val background by animateColorAsState(
        targetValue = if (hovered && onClick != null) Theme[colors][LoreColors.surfaceRaised] else Color.Transparent,
        animationSpec = tween(DURATION_FAST_MILLIS, easing = StandardEasing),
    )

    UnstyledButton(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        interactionSource = interactionSource,
        modifier = modifier
            .padding(end = Theme[spacing][LoreSpacing.space5])
            .height(24.dp)
            .background(background, shape)
            .border(1.dp, Theme[colors][LoreColors.border], shape)
            .padding(start = 7.dp, end = 9.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space3]),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                imageVector = Lucide.Search,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                colorFilter = ColorFilter.tint(muted),
            )
            Text(
                text = stringResource(Res.string.titlebar_search),
                style = Theme[typography][LoreTypography.caption],
                color = muted,
                singleLine = true,
            )
        }
    }
}
