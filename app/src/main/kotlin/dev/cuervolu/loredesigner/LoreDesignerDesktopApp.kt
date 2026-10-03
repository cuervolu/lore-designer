package dev.cuervolu.loredesigner

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.platform.system.DesktopFolderOpener
import dev.cuervolu.loredesigner.resources.Res
import dev.cuervolu.loredesigner.resources.app_icon
import dev.cuervolu.loredesigner.system.DesktopProjectSystemActions
import dev.cuervolu.loredesigner.ui.LoreDesignerApp
import dev.cuervolu.loredesigner.ui.i18n.ProvideAppLocale
import dev.cuervolu.loredesigner.ui.launcher.LocalProjectSystemActions
import dev.cuervolu.loredesigner.ui.session.rememberAppSessionState
import dev.cuervolu.loredesigner.ui.settings.data.ApplicationSettingsRepository
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import dev.cuervolu.loredesigner.window.LauncherWindowTitleBar
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.NucleusApplicationScope
import dev.nucleusframework.darkmodedetector.isSystemInDarkMode
import dev.nucleusframework.window.NucleusDecoratedWindowTheme
import dev.nucleusframework.window.WindowScaffold
import org.jetbrains.compose.resources.painterResource

@Composable
fun NucleusApplicationScope.LoreDesignerDesktopApp(
    settingsRepository: ApplicationSettingsRepository,
    folderOpener: DesktopFolderOpener,
    logger: Logger,
) {
    val settings by settingsRepository.settings.collectAsState()
    val darkTheme = settings.theme.isDark(systemDark = isSystemInDarkMode())

    NucleusDecoratedWindowTheme(isDark = darkTheme) {
        DecoratedWindow(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(size = DpSize(1100.dp, 720.dp)),
            title = "Lore Designer",
            icon = painterResource(Res.drawable.app_icon),
            minimumSize = DpSize(800.dp, 520.dp),
            nativeContextMenu = true,
        ) {
            // Created above the locale boundary so a language change keeps navigation and dialogs.
            val session = rememberAppSessionState()
            val clipboard = LocalClipboard.current
            val systemActions = remember(clipboard) { DesktopProjectSystemActions(folderOpener, clipboard, logger) }
            CompositionLocalProvider(LocalProjectSystemActions provides systemActions) {
                LoreDesignerTheme(darkTheme = darkTheme) {
                    ProvideAppLocale(settings.language) {
                        WindowScaffold(
                            titleBar = { LauncherWindowTitleBar(onSettingsClick = session.settingsModal::open) },
                        ) { contentPadding ->
                            LoreDesignerApp(Modifier.padding(contentPadding), session = session)
                        }
                    }
                }
            }
        }
    }
}
