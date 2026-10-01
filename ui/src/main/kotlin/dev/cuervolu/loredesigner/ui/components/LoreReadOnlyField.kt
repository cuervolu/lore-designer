package dev.cuervolu.loredesigner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.typography

/** Selectable, non-editable monospace value such as a file path. */
@Composable
fun LoreReadOnlyField(value: String, accessibilityLabel: String, modifier: Modifier = Modifier) {
    val shape = Theme[shapes][LoreShapes.control]
    SelectionContainer(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(Theme[colors][LoreColors.surface], shape)
            .border(1.dp, Theme[colors][LoreColors.borderSubtle], shape)
            .padding(horizontal = 8.dp)
            .semantics { contentDescription = accessibilityLabel },
    ) {
        Text(
            text = value,
            style = Theme[typography][LoreTypography.mono],
            color = Theme[colors][LoreColors.textSecondary],
            singleLine = true,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.wrapContentHeight(),
        )
    }
}
