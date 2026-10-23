package dev.cuervolu.loredesigner

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.rememberWindowState
import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.platform.logging.APP_TAG
import dev.cuervolu.loredesigner.platform.system.DesktopFolderOpener
import dev.cuervolu.loredesigner.resources.Res
import dev.cuervolu.loredesigner.resources.app_icon
import dev.cuervolu.loredesigner.system.DesktopProjectSystemActions
import dev.cuervolu.loredesigner.ui.LoreDesignerApp
import dev.cuervolu.loredesigner.ui.chrome.windowTitle
import dev.cuervolu.loredesigner.ui.commands.commandShortcuts
import dev.cuervolu.loredesigner.ui.i18n.ProvideAppLocale
import dev.cuervolu.loredesigner.ui.launcher.LocalProjectSystemActions
import dev.cuervolu.loredesigner.ui.session.rememberAppMenus
import dev.cuervolu.loredesigner.ui.session.rememberAppSessionState
import dev.cuervolu.loredesigner.ui.settings.data.ApplicationSettingsRepository
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import dev.cuervolu.loredesigner.window.AppWindowTitleBar
import dev.cuervolu.loredesigner.window.NativeAppMenuBar
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.NucleusApplicationScope
import dev.nucleusframework.core.runtime.Platform
import dev.nucleusframework.darkmodedetector.isSystemInDarkMode
import dev.nucleusframework.window.NucleusDecoratedWindowTheme
import dev.nucleusframework.window.WindowScaffold
import dev.nucleusframework.window.tao.LocalWindowExceptionHandlerFactory
import dev.nucleusframework.window.tao.WindowExceptionHandlerFactory
import org.jetbrains.compose.resources.painterResource

private const val APP_NAME = "Lore Designer"

/**
 * Logs failures raised inside a window, then rethrows them so Nucleus still takes its fatal path
 * (native error dialog and exit). Nucleus reports those only through java.util.logging, which does
 * not reach the application log file.
 */
@OptIn(ExperimentalComposeUiApi::class)
private fun loggingWindowExceptionHandlerFactory(logger: Logger) = WindowExceptionHandlerFactory {
    WindowExceptionHandler { throwable ->
        logger.e(throwable) { "Unhandled exception in window" }
        throw throwable
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NucleusApplicationScope.LoreDesignerDesktopApp(
    settingsRepository: ApplicationSettingsRepository,
    folderOpener: DesktopFolderOpener,
    logger: Logger,
) {
    val settings by settingsRepository.settings.collectAsState()
    val darkTheme = settings.theme.isDark(systemDark = isSystemInDarkMode())

    val windowExceptionHandlerFactory = remember(logger) {
        loggingWindowExceptionHandlerFactory(logger.withTag(APP_TAG))
    }

    // Hoisted above the window so the native menu bar, which lives at application scope, shares it.
    val session = rememberAppSessionState()
    val isMacOS = Platform.Current == Platform.MacOS
    val title = windowTitle(document = null, project = session.activeWorkspace?.config?.name, appName = APP_NAME)

    if (isMacOS) {
        // Its own locale boundary: wrapping the window instead would recreate the window on a language change.
        ProvideAppLocale(settings.language) {
            NativeAppMenuBar(
                menus = rememberAppMenus(session, onQuit = ::exitApplication, macOS = true),
                appName = APP_NAME,
            )
        }
    }

    NucleusDecoratedWindowTheme(isDark = darkTheme) {
        CompositionLocalProvider(LocalWindowExceptionHandlerFactory provides windowExceptionHandlerFactory) {
            DecoratedWindow(
                onCloseRequest = ::exitApplication,
                state = rememberWindowState(size = DpSize(1100.dp, 720.dp)),
                title = title.plain,
                icon = painterResource(Res.drawable.app_icon),
                minimumSize = DpSize(800.dp, 520.dp),
                nativeContextMenu = true,
            ) {
                val clipboard = LocalClipboard.current
                val systemActions = remember(clipboard) {
                    DesktopProjectSystemActions(folderOpener, clipboard, logger.withTag("System"))
                }
                CompositionLocalProvider(LocalProjectSystemActions provides systemActions) {
                    LoreDesignerTheme(darkTheme = darkTheme) {
                        ProvideAppLocale(settings.language) {
                            val menus = rememberAppMenus(session, onQuit = ::exitApplication, macOS = isMacOS)
                            WindowScaffold(
                                titleBar = {
                                    AppWindowTitleBar(
                                        onSettingsClick = session.settingsModal::open,
                                        menus = menus,
                                        workspace = session.activeWorkspace,
                                        title = title,
                                    )
                                },
                            ) { contentPadding ->
                                LoreDesignerApp(
                                    modifier = Modifier
                                        .padding(contentPadding)
                                        .let { if (isMacOS) it else it.commandShortcuts(menus) },
                                    session = session,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
