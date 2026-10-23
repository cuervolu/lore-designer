package dev.cuervolu.loredesigner.ui.chrome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.app_name
import dev.cuervolu.loredesigner.ui.resources.logo
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreSizes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.dotToken
import dev.cuervolu.loredesigner.ui.theme.sizes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private enum class TitleBarSlot { Start, Title, End }

private val TitleMargin = 10.dp
private val MinTitleWidth = 240.dp

/**
 * Title bar of the application window. It has an optional [brand]
 * and [menuBar] at the start, an optional [title] on the bar's center, then [actions] and the
 * [windowControls] at the end.
 *
 * The host passes its drag behaviour through [modifier], reserves platform zones (e.g. macOS traffic
 * lights) with [contentPadding] and supplies native [windowControls]. [projectColor] tints the bar's
 * leading edge, without one it uses the accent.
 */
@Composable
fun AppTitleBar(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    projectColor: ProjectColor? = null,
    brand: (@Composable () -> Unit)? = null,
    menuBar: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    windowControls: @Composable () -> Unit = {},
) {
    val surface = Theme[colors][LoreColors.surface]
    val tint = Theme[colors][projectColor?.dotToken() ?: LoreColors.accent]
    val borderColor = Theme[colors][LoreColors.borderSubtle]
    val background = remember(surface, tint) {
        Brush.horizontalGradient(
            0f to tint.copy(alpha = 0.04f).compositeOver(surface),
            0.4f to surface,
        )
    }

    TitleBarLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(Theme[sizes][LoreSizes.titleBarHeight])
            .background(background)
            .drawBehind {
                val y = size.height - 0.5.dp.toPx()
                drawLine(borderColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            },
        start = {
            // Only the start slot is inset: the title is centered on the whole bar, so it must not
            // depend on how much room the platform reserves for its window controls.
            Row(modifier = Modifier.padding(contentPadding), verticalAlignment = Alignment.CenterVertically) {
                brand?.invoke()
                menuBar?.invoke()
            }
        },
        title = { title?.invoke() },
        end = {
            Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                actions()
                windowControls()
            }
        },
    )
}

/**
 * Places [title] on the bar's horizontal center. It only moves off center, and then narrows, when the
 * start or end content leaves no room to keep it there.
 */
@Composable
private fun TitleBarLayout(
    modifier: Modifier,
    start: @Composable () -> Unit,
    title: @Composable () -> Unit,
    end: @Composable () -> Unit,
) {
    Layout(
        content = {
            Box(Modifier.layoutId(TitleBarSlot.Start), contentAlignment = Alignment.CenterStart) { start() }
            Box(Modifier.layoutId(TitleBarSlot.Title), contentAlignment = Alignment.Center) { title() }
            Box(Modifier.layoutId(TitleBarSlot.End), contentAlignment = Alignment.CenterEnd) { end() }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val slotConstraints = Constraints(maxWidth = width, maxHeight = height)
        fun slot(which: TitleBarSlot) = measurables.first { it.layoutId == which }

        val startPlaceable = slot(TitleBarSlot.Start).measure(slotConstraints)
        val endPlaceable = slot(TitleBarSlot.End).measure(slotConstraints)

        val margin = TitleMargin.roundToPx()
        val leftEdge = startPlaceable.width + margin
        val rightEdge = width - endPlaceable.width - margin
        val available = (rightEdge - leftEdge).coerceAtLeast(0)
        val center = width / 2
        val symmetric = 2 * minOf(center - leftEdge, rightEdge - center)
        val titleWidth = minOf(available, maxOf(MinTitleWidth.roundToPx(), symmetric))
        val titleLeft = (center - titleWidth / 2).coerceIn(leftEdge, maxOf(leftEdge, rightEdge - titleWidth))
        val titlePlaceable = slot(TitleBarSlot.Title)
            .measure(Constraints(maxWidth = titleWidth, maxHeight = height))

        layout(width, height) {
            startPlaceable.placeRelative(0, (height - startPlaceable.height) / 2)
            endPlaceable.placeRelative(width - endPlaceable.width, (height - endPlaceable.height) / 2)
            titlePlaceable.placeRelative(
                titleLeft + (titleWidth - titlePlaceable.width) / 2,
                (height - titlePlaceable.height) / 2,
            )
        }
    }
}

/** Mark and name shown at the start of the launcher's title bar; the workspace identifies itself by its title instead. */
@Composable
fun TitleBarBrand(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space4]),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(Res.string.app_name),
            style = Theme[typography][LoreTypography.bodyStrong],
            color = Theme[colors][LoreColors.textPrimary],
            singleLine = true,
        )
    }
}
