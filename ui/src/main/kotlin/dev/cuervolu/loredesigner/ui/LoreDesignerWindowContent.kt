package dev.cuervolu.loredesigner.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import dev.cuervolu.loredesigner.core.settings.AppLanguage
import dev.cuervolu.loredesigner.ui.chrome.AppMenuBar
import dev.cuervolu.loredesigner.ui.chrome.AppTitleBar
import dev.cuervolu.loredesigner.ui.chrome.TitleBarBrand
import dev.cuervolu.loredesigner.ui.chrome.TitleBarSearchButton
import dev.cuervolu.loredesigner.ui.chrome.TitleBarSettingsButton
import dev.cuervolu.loredesigner.ui.chrome.WindowTitle
import dev.cuervolu.loredesigner.ui.chrome.WindowTitleText
import dev.cuervolu.loredesigner.ui.commands.commandShortcuts
import dev.cuervolu.loredesigner.ui.i18n.ProvideAppLocale
import dev.cuervolu.loredesigner.ui.launcher.DirectoryPicker
import dev.cuervolu.loredesigner.ui.launcher.FileKitDirectoryPicker
import dev.cuervolu.loredesigner.ui.session.AppSessionState
import dev.cuervolu.loredesigner.ui.session.rememberAppMenus
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme

/**
 * The parts of the window frame that only the host window system can provide. The defaults are a
 * plain frame, enough to run the application content without a native window.
 *
 * [frame] lays out the title bar above the content and hands the content its insets. The title bar is
 * composed inside [frame], so [titleBarContentPadding] may read locals that the frame provides.
 */
@Stable
class WindowChrome(
    val titleBarModifier: Modifier = Modifier,
    val titleBarContentPadding: @Composable () -> PaddingValues = { PaddingValues(0.dp) },
    val windowControls: @Composable () -> Unit = {},
    val frame: @Composable (titleBar: @Composable () -> Unit, content: @Composable (PaddingValues) -> Unit) -> Unit =
        { titleBar, content ->
            Column {
                titleBar()
                Box(Modifier.fillMaxHeight()) { content(PaddingValues(0.dp)) }
            }
        },
)

/**
 * Everything inside the application window. The desktop host and the tests both enter here,
 * so they cannot drift apart.
 *
 * [session] is created by the caller above this composable because the macOS native menu bar,
 * which lives at application scope, shares it. [LoreDesignerApp]'s remaining dependencies come
 * from Koin.
 */
@Composable
fun LoreDesignerWindowContent(
    session: AppSessionState,
    title: WindowTitle,
    darkTheme: Boolean,
    language: AppLanguage,
    macOS: Boolean,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier,
    chrome: WindowChrome = WindowChrome(),
    pickDirectory: DirectoryPicker = FileKitDirectoryPicker,
) {
    LoreDesignerTheme(darkTheme = darkTheme) {
        ProvideAppLocale(language) {
            val menus = rememberAppMenus(session, onQuit = onQuit, macOS = macOS)
            val workspace = session.activeWorkspace
            val shortcutFocus = remember { FocusRequester() }
            // Key events only reach the shortcuts while something under them has focus. Opening a project destroys
            // the launcher's focused row and nothing in the shell takes focus, so the shortcuts would go dead.
            // macOS handles its shortcuts in the native menu and does not attach the requester.
            LaunchedEffect(workspace?.config?.id) {
                if (workspace != null && !macOS) shortcutFocus.requestFocus()
            }
            chrome.frame(
                {
                    AppTitleBar(
                        modifier = chrome.titleBarModifier,
                        contentPadding = if (macOS) chrome.titleBarContentPadding() else PaddingValues(0.dp),
                        projectColor = workspace?.config?.color,
                        brand = if (workspace == null) ({ TitleBarBrand() }) else null,
                        menuBar = if (macOS) null else ({ AppMenuBar(menus) }),
                        title = if (workspace != null) ({ WindowTitleText(title) }) else null,
                        actions = {
                            if (workspace == null) {
                                TitleBarSettingsButton(session.settingsModal::open)
                            } else {
                                TitleBarSearchButton()
                            }
                        },
                        windowControls = chrome.windowControls,
                    )
                },
                { contentPadding ->
                    LoreDesignerApp(
                        modifier = modifier
                            .padding(contentPadding)
                            .let {
                                if (macOS) it else it.commandShortcuts(menus).focusRequester(shortcutFocus).focusable()
                            },
                        session = session,
                        pickDirectory = pickDirectory,
                    )
                },
            )
        }
    }
}
