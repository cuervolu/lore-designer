package dev.cuervolu.loredesigner.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.TextInput
import com.composeunstyled.UnstyledTextField
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreSizes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.sizes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography

// Matches the design's 6dp control radius; the halo sits just outside the 1dp border.
private val FocusHaloWidth = 2.dp
private val ControlCornerRadius = 6.dp

@Composable
fun LoreTextField(
    state: TextFieldState,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    onSubmit: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = Theme[shapes][LoreShapes.control]
    val halo = Theme[colors][LoreColors.accentSubtle]
    val textColor = Theme[colors][LoreColors.textPrimary]

    UnstyledTextField(
        state = state,
        enabled = enabled,
        accessibilityLabel = accessibilityLabel,
        interactionSource = interactionSource,
        textStyle = Theme[typography][LoreTypography.body],
        textColor = textColor,
        cursorBrush = SolidColor(textColor),
        selectionColors = LocalTextSelectionColors.current,
        lineLimits = TextFieldLineLimits.SingleLine,
        onKeyboardAction = onSubmit?.let { submit -> KeyboardActionHandler { submit() } },
        modifier = modifier
            .height(Theme[sizes][LoreSizes.controlHeight])
            .drawBehind {
                if (focused) {
                    val inset = FocusHaloWidth.toPx() / 2
                    val radius = (ControlCornerRadius + FocusHaloWidth / 2).toPx()
                    drawRoundRect(
                        color = halo,
                        topLeft = Offset(-inset, -inset),
                        size = Size(size.width + inset * 2, size.height + inset * 2),
                        cornerRadius = CornerRadius(radius),
                        style = Stroke(FocusHaloWidth.toPx()),
                    )
                }
            }
            .background(Theme[colors][LoreColors.surfaceEditor], shape)
            .border(
                width = 1.dp,
                color = if (focused) Theme[colors][LoreColors.focusRing] else Theme[colors][LoreColors.border],
                shape = shape,
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Theme[spacing][LoreSpacing.space4]),
            horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space3]),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Image(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    colorFilter = ColorFilter.tint(Theme[colors][LoreColors.textMuted]),
                )
            }
            TextInput(
                modifier = Modifier.fillMaxWidth(),
                placeholder = placeholder?.let { text ->
                    {
                        Text(
                            text = text,
                            style = Theme[typography][LoreTypography.body],
                            color = Theme[colors][LoreColors.textMuted],
                            singleLine = true,
                        )
                    }
                },
            )
        }
    }
}
