package dev.cuervolu.loredesigner.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.composeunstyled.DialogPanel
import com.composeunstyled.Scrim
import com.composeunstyled.Text
import com.composeunstyled.UnstyledDialog
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.theme.DURATION_BASE_MILLIS
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShadows
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.StandardEasing
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.shadows
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography

private val DialogWidth = 520.dp
private val DialogPaddingHorizontal = 28.dp
private const val ENTER_SCALE = 0.98f

/** Modal dialog; must be composed under a `DialogHost`. `Esc` and clicking the scrim dismiss it. */
@Composable
fun LoreDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val motion = tween<Float>(DURATION_BASE_MILLIS, easing = StandardEasing)
    val shape = Theme[shapes][LoreShapes.modal]
    val divider = Theme[colors][LoreColors.borderSubtle]
    val modalShadows = Theme[shadows][LoreShadows.modal]

    UnstyledDialog(
        visible = visible,
        onDismissRequest = onDismissRequest,
        overlay = {
            Scrim(
                scrimColor = Theme[colors][LoreColors.overlayScrim],
                enter = fadeIn(motion),
                exit = fadeOut(motion),
            )
        },
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            DialogPanel(
                paneTitle = title,
                enter = fadeIn(motion) + scaleIn(motion, initialScale = ENTER_SCALE),
                exit = fadeOut(motion) + scaleOut(motion, targetScale = ENTER_SCALE),
                modifier = modifier
                    .width(DialogWidth)
                    .let { base -> modalShadows.fold(base) { acc, shadow -> acc.dropShadow(shape, shadow) } }
                    .clip(shape)
                    .background(Theme[colors][LoreColors.surfaceEditor])
                    .border(1.dp, Theme[colors][LoreColors.border], shape),
            ) {
                Column {
                    Column(
                        modifier = Modifier.padding(
                            start = DialogPaddingHorizontal,
                            end = DialogPaddingHorizontal,
                            top = DialogPaddingHorizontal,
                            bottom = Theme[spacing][LoreSpacing.space7],
                        ),
                    ) {
                        Text(
                            text = title,
                            style = Theme[typography][LoreTypography.headingLarge],
                            color = Theme[colors][LoreColors.textPrimary],
                        )
                        if (description != null) {
                            Text(
                                text = description,
                                modifier = Modifier.padding(top = Theme[spacing][LoreSpacing.space4]),
                                style = Theme[typography][LoreTypography.body],
                                color = Theme[colors][LoreColors.textSecondary],
                            )
                        }
                        Column(
                            modifier = Modifier.padding(top = Theme[spacing][LoreSpacing.space6]),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            content = content,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Theme[colors][LoreColors.surface])
                            .drawBehind {
                                drawLine(divider, Offset.Zero, Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                            }
                            .padding(
                                horizontal = DialogPaddingHorizontal,
                                vertical = Theme[spacing][LoreSpacing.space6],
                            ),
                        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space4], Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions,
                    )
                }
            }
        }
    }
}
