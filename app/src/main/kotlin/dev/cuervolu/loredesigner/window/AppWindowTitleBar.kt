package dev.cuervolu.loredesigner.window

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cuervolu.loredesigner.ui.chrome.AppMenuBar
import dev.cuervolu.loredesigner.ui.chrome.AppTitleBar
import dev.cuervolu.loredesigner.ui.chrome.TitleBarBrand
import dev.cuervolu.loredesigner.ui.chrome.TitleBarSearchButton
import dev.cuervolu.loredesigner.ui.chrome.TitleBarSettingsButton
import dev.cuervolu.loredesigner.ui.chrome.WindowTitle
import dev.cuervolu.loredesigner.ui.chrome.WindowTitleText
import dev.cuervolu.loredesigner.ui.commands.AppMenu
import dev.cuervolu.loredesigner.workspace.Workspace
import dev.nucleusframework.core.runtime.Platform
import dev.nucleusframework.window.DecoratedWindowScope
import dev.nucleusframework.window.LocalWindowChromeInsets
import dev.nucleusframework.window.WindowControls
import dev.nucleusframework.window.windowDragArea

/**
 * Mounts the visual [AppTitleBar] on the Nucleus window chrome.
 *
 * The launcher shows the brand and the Settings button; an open [workspace] shows its [title] and a
 * search entry instead, leaving Settings to the menu bar.
 *
 * macOS draws native traffic lights and the native menu bar, so the bar only pads around the
 * lights. Elsewhere the controls are composed by Nucleus, which keeps the platform's native button
 * order, and the menu bar is drawn inside the title bar. Nucleus requires using one or the other for
 * the controls: combining them would reserve their space twice.
 */
@Composable
internal fun DecoratedWindowScope.AppWindowTitleBar(
    onSettingsClick: () -> Unit,
    menus: List<AppMenu>,
    workspace: Workspace?,
    title: WindowTitle,
) {
    val isMacOS = Platform.Current == Platform.MacOS
    AppTitleBar(
        modifier = Modifier.windowDragArea(),
        contentPadding = if (isMacOS) LocalWindowChromeInsets.current.controlsInsets else PaddingValues(0.dp),
        projectColor = workspace?.config?.color,
        brand = if (workspace == null) ({ TitleBarBrand() }) else null,
        menuBar = if (isMacOS) null else ({ AppMenuBar(menus) }),
        title = if (workspace != null) ({ WindowTitleText(title) }) else null,
        actions = {
            if (workspace == null) TitleBarSettingsButton(onSettingsClick) else TitleBarSearchButton()
        },
        windowControls = {
            if (!isMacOS) WindowControls(Modifier.fillMaxHeight())
        },
    )
}
