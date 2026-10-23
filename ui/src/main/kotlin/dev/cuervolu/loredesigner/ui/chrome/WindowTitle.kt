package dev.cuervolu.loredesigner.ui.chrome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography

private const val SEPARATOR = "—"

/**
 * The window title as `Document — Project — Application`, dropping the leading parts that do not
 * apply. [emphasis] is what the user is working on and [context] is where, shown more quietly.
 */
data class WindowTitle(val emphasis: String, val context: String?) {
    /** Single-line form for places that cannot style it, such as the operating system's window title. */
    val plain: String get() = if (context == null) emphasis else "$emphasis $SEPARATOR $context"
}

fun windowTitle(document: String?, project: String?, appName: String): WindowTitle = when {
    project == null -> WindowTitle(appName, null)
    document == null -> WindowTitle(project, appName)
    else -> WindowTitle(document, "$project $SEPARATOR $appName")
}

@Composable
fun WindowTitleText(title: WindowTitle, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space3]),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.emphasis,
            style = Theme[typography][LoreTypography.bodyStrong],
            color = Theme[colors][LoreColors.textPrimary],
            singleLine = true,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (title.context != null) {
            Text(
                text = "$SEPARATOR ${title.context}",
                style = Theme[typography][LoreTypography.body],
                color = Theme[colors][LoreColors.textMuted],
                singleLine = true,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}
