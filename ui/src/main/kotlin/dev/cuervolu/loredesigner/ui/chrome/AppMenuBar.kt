package dev.cuervolu.loredesigner.ui.chrome

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composeunstyled.Text
import com.composeunstyled.UnstyledButton
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.commands.AppMenu
import dev.cuervolu.loredesigner.ui.commands.MenuEntry
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShadows
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.shadows
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography

private const val REOPEN_GUARD_MILLIS = 150L

/**
 * Menu bar drawn inside the title bar on Windows and Linux. As in IntelliJ IDEA, opening one menu
 * makes its neighbours switch on hover instead of on click. macOS uses the native menu bar instead.
 */
@Composable
fun AppMenuBar(menus: List<AppMenu>, macOSShortcuts: Boolean = false, modifier: Modifier = Modifier) {
    var openMenu: String? by remember { mutableStateOf(null) }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        menus.forEach { menu ->
            MenuTitle(
                menu = menu,
                macOSShortcuts = macOSShortcuts,
                expanded = openMenu == menu.id,
                anyOpen = openMenu != null,
                onOpenChange = { open ->
                    if (open) {
                        openMenu = menu.id
                    } else if (openMenu == menu.id) {
                        openMenu = null
                    }
                },
            )
        }
    }
}

@Composable
private fun MenuTitle(
    menu: AppMenu,
    macOSShortcuts: Boolean,
    expanded: Boolean,
    anyOpen: Boolean,
    onOpenChange: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    LaunchedEffect(hovered, anyOpen) {
        if (hovered && anyOpen) onOpenChange(true)
    }
    // The press that dismisses the popup arrives before this button's click; without this guard the
    // click would immediately reopen the menu it just closed.
    var dismissedAt by remember { mutableLongStateOf(0L) }

    Box {
        UnstyledButton(
            onClick = {
                val justDismissed = System.currentTimeMillis() - dismissedAt < REOPEN_GUARD_MILLIS
                if (expanded || !justDismissed) onOpenChange(!expanded)
            },
            interactionSource = interactionSource,
            contentPadding = PaddingValues(horizontal = Theme[spacing][LoreSpacing.space4]),
            modifier = Modifier
                .height(26.dp)
                .background(
                    if (expanded || hovered) Theme[colors][LoreColors.surfaceRaised] else Color.Transparent,
                    Theme[shapes][LoreShapes.control],
                )
                .semantics { stateDescription = if (expanded) "expanded" else "collapsed" },
        ) {
            Text(
                text = menu.title,
                style = Theme[typography][LoreTypography.body],
                color = Theme[colors][LoreColors.textPrimary],
                singleLine = true,
            )
        }
        if (expanded) {
            // Not focusable: a modal layer would swallow the hover that switches between menus.
            val gap = with(LocalDensity.current) { 2.dp.roundToPx() }
            val position = remember(gap) { BelowAnchor(gap) }
            Popup(
                popupPositionProvider = position,
                onDismissRequest = {
                    dismissedAt = System.currentTimeMillis()
                    onOpenChange(false)
                },
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),
            ) {
                MenuPanel(menu, macOSShortcuts, onClose = { onOpenChange(false) })
            }
        }
    }
}

/** Places a popup directly under its anchor, left-aligned with it. */
private class BelowAnchor(private val gap: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ) = IntOffset(anchorBounds.left, anchorBounds.bottom + gap)
}

@Composable
private fun MenuPanel(menu: AppMenu, macOSShortcuts: Boolean, onClose: () -> Unit) {
    val menuShape = Theme[shapes][LoreShapes.menu]
    val menuShadows = Theme[shadows][LoreShadows.menu]
    Column(
        modifier = Modifier
            .widthIn(min = 200.dp)
            .width(IntrinsicSize.Max)
            .let { base -> menuShadows.fold(base) { acc, shadow -> acc.dropShadow(menuShape, shadow) } }
            .background(Theme[colors][LoreColors.surfaceEditor], menuShape)
            .border(1.dp, Theme[colors][LoreColors.border], menuShape)
            .padding(4.dp),
    ) {
        menu.entries.forEach { entry ->
            when (entry) {
                MenuEntry.Separator -> MenuSeparator()
                is MenuEntry.Item -> MenuItemRow(entry, macOSShortcuts, onClose)
            }
        }
    }
}

@Composable
private fun MenuItemRow(item: MenuEntry.Item, macOSShortcuts: Boolean, onClose: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val textColor = if (item.enabled) Theme[colors][LoreColors.textPrimary] else Theme[colors][LoreColors.textMuted]
    UnstyledButton(
        onClick = {
            onClose()
            item.onClick()
        },
        enabled = item.enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (hovered && item.enabled) Theme[colors][LoreColors.surface] else Color.Transparent,
                Theme[shapes][LoreShapes.control],
            )
            .semantics { if (item.checked != null) role = Role.Checkbox },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(Theme[spacing][LoreSpacing.space4]),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(14.dp)) {
                if (item.checked == true) {
                    Image(
                        imageVector = Lucide.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        colorFilter = ColorFilter.tint(Theme[colors][LoreColors.accent]),
                    )
                }
            }
            Text(
                text = item.title,
                style = Theme[typography][LoreTypography.body],
                color = textColor,
                singleLine = true,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            item.shortcut?.let {
                Text(
                    text = it.display(macOSShortcuts),
                    style = Theme[typography][LoreTypography.caption],
                    color = Theme[colors][LoreColors.textMuted],
                    singleLine = true,
                    modifier = Modifier.padding(start = Theme[spacing][LoreSpacing.space6]),
                )
            }
        }
    }
}

@Composable
private fun MenuSeparator() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(1.dp)
            .background(Theme[colors][LoreColors.borderSubtle]),
    )
}
