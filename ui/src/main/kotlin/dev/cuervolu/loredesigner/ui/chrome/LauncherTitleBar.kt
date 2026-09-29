package dev.cuervolu.loredesigner.ui.chrome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.components.LoreIconButton
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.app_name
import dev.cuervolu.loredesigner.ui.resources.logo
import dev.cuervolu.loredesigner.ui.resources.titlebar_settings
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreSizes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.sizes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Title bar for the launcher window: brand mark, one optional utility action and the window controls.
 *
 * The host passes its drag behaviour through [modifier],reserves platform zones
 * (e.g. macOS traffic lights) with [contentPadding] and supplies native
 * [windowControls].
 */
@Composable
fun LauncherTitleBar(
    onSettingsClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    windowControls: @Composable () -> Unit = {},
) {
    val surface = Theme[colors][LoreColors.surface]
    val accent = Theme[colors][LoreColors.accent]
    val borderColor = Theme[colors][LoreColors.borderSubtle]
    val background = remember(surface, accent) {
        Brush.horizontalGradient(
            0f to accent.copy(alpha = 0.04f).compositeOver(surface),
            0.4f to surface,
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Theme[sizes][LoreSizes.titleBarHeight])
            .background(background)
            .drawBehind {
                val y = size.height - 0.5.dp.toPx()
                drawLine(borderColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
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
        Spacer(Modifier.weight(1f))
        if (onSettingsClick != null) {
            LoreIconButton(
                icon = Lucide.Settings,
                contentDescription = stringResource(Res.string.titlebar_settings),
                onClick = onSettingsClick,
                modifier = Modifier.padding(end = Theme[spacing][LoreSpacing.space3]),
            )
        }
        Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            windowControls()
        }
    }
}
