package dev.cuervolu.loredesigner.window

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cuervolu.loredesigner.ui.chrome.LauncherTitleBar
import dev.nucleusframework.core.runtime.Platform
import dev.nucleusframework.window.DecoratedWindowScope
import dev.nucleusframework.window.LocalWindowChromeInsets
import dev.nucleusframework.window.WindowControls
import dev.nucleusframework.window.windowDragArea

/**
 * Mounts the visual [LauncherTitleBar] on the Nucleus window chrome.
 *
 * macOS draws native traffic lights, so the bar only pads around them. Elsewhere the controls are
 * composed by Nucleus, which keeps the platform's native button order. Nucleus requires using one or
 * the other: combining them would reserve the controls' space twice.
 */
@Composable
internal fun DecoratedWindowScope.LauncherWindowTitleBar() {
    val isMacOS = Platform.Current == Platform.MacOS
    LauncherTitleBar(
        onSettingsClick = null,
        modifier = Modifier.windowDragArea(),
        contentPadding = if (isMacOS) LocalWindowChromeInsets.current.controlsInsets else PaddingValues(0.dp),
        windowControls = {
            if (!isMacOS) WindowControls(Modifier.fillMaxHeight())
        },
    )
}
