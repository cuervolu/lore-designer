package dev.cuervolu.loredesigner

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import dev.cuervolu.loredesigner.ui.LoreDesignerApp
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import dev.cuervolu.loredesigner.window.LauncherWindowTitleBar
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.NucleusApplicationScope
import dev.nucleusframework.darkmodedetector.isSystemInDarkMode
import dev.nucleusframework.window.NucleusDecoratedWindowTheme
import dev.nucleusframework.window.WindowScaffold

@Composable
fun NucleusApplicationScope.LoreDesignerDesktopApp() {
    val darkTheme = isSystemInDarkMode()

    NucleusDecoratedWindowTheme(isDark = darkTheme) {
        DecoratedWindow(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(size = DpSize(1100.dp, 720.dp)),
            title = "Lore Designer",
            minimumSize = DpSize(800.dp, 520.dp),
        ) {
            LoreDesignerTheme(darkTheme = darkTheme) {
                WindowScaffold(titleBar = { LauncherWindowTitleBar() }) { contentPadding ->
                    LoreDesignerApp(Modifier.padding(contentPadding))
                }
            }
        }
    }
}
