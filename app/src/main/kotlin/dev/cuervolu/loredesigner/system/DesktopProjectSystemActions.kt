package dev.cuervolu.loredesigner.system

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.platform.system.DesktopFolderOpener
import dev.cuervolu.loredesigner.ui.launcher.ProjectSystemActions
import java.awt.datatransfer.StringSelection
import java.nio.file.Path

/**
 * [clipboard] must be the window's Compose clipboard: on Linux, Nucleus backs it with GTK so copies
 * reach Wayland sessions, which the AWT system clipboard does not.
 */
internal class DesktopProjectSystemActions(
    private val folderOpener: DesktopFolderOpener,
    private val clipboard: Clipboard,
    private val logger: Logger,
) : ProjectSystemActions {
    override suspend fun showInFolder(projectLocation: String): Boolean {
        val project = Path.of(projectLocation)
        return folderOpener.open(project.parent ?: project)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    override suspend fun copyText(text: String): Boolean = try {
        clipboard.setClipEntry(ClipEntry(StringSelection(text)))
        true
    } catch (exception: IllegalStateException) {
        // AWT reports a clipboard held by another application this way.
        logger.w(exception) { "Clipboard is unavailable" }
        false
    } catch (exception: UnsupportedOperationException) {
        // Includes HeadlessException.
        logger.w(exception) { "Clipboard is not supported" }
        false
    }
}
