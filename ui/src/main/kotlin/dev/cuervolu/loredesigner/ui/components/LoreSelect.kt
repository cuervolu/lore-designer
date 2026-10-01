package dev.cuervolu.loredesigner.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Lucide
import com.composeunstyled.DropdownMenuPanel
import com.composeunstyled.Text
import com.composeunstyled.UnstyledButton
import com.composeunstyled.UnstyledDropdownMenu
import com.composeunstyled.UnstyledDropdownMenuItem
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.theme.DURATION_FAST_MILLIS
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShadows
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreSizes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.StandardEasing
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.shadows
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.sizes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography

/** One choice of a [LoreSelect]; [textStyle] lets an option preview itself (e.g. a font). */
data class LoreSelectOption<T>(val value: T, val label: String, val textStyle: TextStyle? = null)

@Composable
fun <T> LoreSelect(
    value: T,
    options: List<LoreSelectOption<T>>,
    onValueChange: (T) -> Unit,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
    width: Dp = 180.dp,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = options.firstOrNull { it.value == value }
    val controlShape = Theme[shapes][LoreShapes.control]
    val menuShape = Theme[shapes][LoreShapes.menu]
    val menuShadows = Theme[shadows][LoreShadows.menu]
    val motion = tween<Float>(DURATION_FAST_MILLIS, easing = StandardEasing)

    UnstyledDropdownMenu(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.width(width),
        sideOffset = 4.dp,
        panel = {
            DropdownMenuPanel(
                enter = fadeIn(motion),
                exit = fadeOut(motion),
                modifier = Modifier
                    .widthIn(min = width, max = width * 2)
                    .width(IntrinsicSize.Max)
                    .let { base -> menuShadows.fold(base) { acc, shadow -> acc.dropShadow(menuShape, shadow) } }
                    .background(Theme[colors][LoreColors.surfaceEditor], menuShape)
                    .border(1.dp, Theme[colors][LoreColors.border], menuShape)
                    .padding(4.dp),
            ) {
                Column {
                    options.forEach { option ->
                        SelectOptionRow(
                            option = option,
                            selected = option.value == value,
                            onClick = {
                                onValueChange(option.value)
                                expanded = false
                            },
                        )
                    }
                }
            }
        },
    ) {
        UnstyledButton(
            onClick = { expanded = !expanded },
            enabled = enabled,
            contentPadding = PaddingValues(horizontal = Theme[spacing][LoreSpacing.space4]),
            modifier = Modifier
                .fillMaxWidth()
                .height(Theme[sizes][LoreSizes.controlHeightSmall])
                .background(Theme[colors][LoreColors.surfaceEditor], controlShape)
                .border(1.dp, Theme[colors][LoreColors.border], controlShape)
                .semantics {
                    contentDescription = accessibilityLabel
                    role = Role.DropdownList
                    selected?.let { stateDescription = it.label }
                },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space4]),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selected?.label.orEmpty(),
                    style = selected?.textStyle ?: Theme[typography][LoreTypography.body],
                    color = Theme[colors][LoreColors.textPrimary],
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Image(
                    imageVector = Lucide.ChevronDown,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    colorFilter = ColorFilter.tint(Theme[colors][LoreColors.textMuted]),
                )
            }
        }
    }
}

@Composable
private fun <T> SelectOptionRow(option: LoreSelectOption<T>, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val background = when {
        selected -> Theme[colors][LoreColors.accentSubtle]
        hovered -> Theme[colors][LoreColors.surface]
        else -> Color.Transparent
    }
    UnstyledDropdownMenuItem(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .background(background, Theme[shapes][LoreShapes.control]),
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
                text = option.label,
                style = option.textStyle ?: Theme[typography][LoreTypography.body],
                color = Theme[colors][LoreColors.textPrimary],
                singleLine = true,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
