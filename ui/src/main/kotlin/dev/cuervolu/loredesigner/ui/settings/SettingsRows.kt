package dev.cuervolu.loredesigner.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.typography

private const val DIMMED_ALPHA = 0.5f

/** Draws the design's 1dp subtle divider along the bottom edge. */
@Composable
internal fun Modifier.settingsDivider(show: Boolean = true): Modifier {
    if (!show) return this
    val color = Theme[colors][LoreColors.borderSubtle]
    return drawBehind {
        val y = size.height - 0.5.dp.toPx()
        drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
    }
}

/** Label (with optional helper) on the left, control on the right. */
@Composable
internal fun SettingsRow(
    label: String,
    modifier: Modifier = Modifier,
    helper: String? = null,
    divider: Boolean = true,
    dimmed: Boolean = false,
    control: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .settingsDivider(divider)
            .padding(vertical = 10.dp)
            .alpha(if (dimmed) DIMMED_ALPHA else 1f),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsLabel(label, helper, Modifier.weight(1f))
        control()
    }
}

/** Label above full-width content, e.g. a read-only path. */
@Composable
internal fun SettingsStackedRow(
    label: String,
    modifier: Modifier = Modifier,
    helper: String? = null,
    divider: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().settingsDivider(divider).padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SettingsLabel(label, helper)
        content()
    }
}

@Composable
internal fun SettingsLabel(label: String, helper: String? = null, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = label,
            style = Theme[typography][LoreTypography.body],
            color = Theme[colors][LoreColors.textPrimary],
        )
        if (helper != null) SettingsHelper(helper, Modifier.padding(top = 2.dp))
    }
}

@Composable
internal fun SettingsHelper(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = Theme[typography][LoreTypography.caption],
        color = Theme[colors][LoreColors.textMuted],
        modifier = modifier,
    )
}

/** Uppercase subsection heading with an optional muted description beneath it. */
@Composable
internal fun SettingsSectionHeader(title: String, modifier: Modifier = Modifier, helper: String? = null) {
    Column(modifier.fillMaxWidth().settingsDivider().padding(bottom = 4.dp)) {
        SettingsEyebrow(title)
        if (helper != null) SettingsHelper(helper, Modifier.padding(top = 4.dp))
    }
}

@Composable
internal fun SettingsEyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = Theme[typography][LoreTypography.eyebrow],
        color = Theme[colors][LoreColors.textMuted],
        singleLine = true,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/** Introductory paragraph shown under a page title. */
@Composable
internal fun SettingsDescription(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = Theme[typography][LoreTypography.body],
        color = Theme[colors][LoreColors.textSecondary],
        modifier = modifier.padding(bottom = 12.dp),
    )
}
