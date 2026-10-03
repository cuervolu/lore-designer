package dev.cuervolu.loredesigner.system

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.asAwtTransferable
import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.platform.system.DesktopFolderOpener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.awt.datatransfer.DataFlavor
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalComposeUiApi::class)
class DesktopProjectSystemActionsTest {
    private val root: Path = createTempDirectory("project-actions-test")
    private val launched = mutableListOf<List<String>>()
    private val clipboard = RecordingClipboard()

    private val actions = DesktopProjectSystemActions(
        folderOpener = DesktopFolderOpener(
            logger = Logger.withTag("test"),
            osName = "Linux",
            processLauncher = { command -> launched += command },
            ioDispatcher = Dispatchers.Unconfined,
        ),
        clipboard = clipboard,
        logger = Logger.withTag("test"),
    )

    @AfterTest
    fun tearDown() {
        root.toFile().deleteRecursively()
    }

    @Test
    fun `show in folder opens the folder that contains the project`() = runBlocking {
        val project = root.resolve("worlds/Embercourt").createDirectories()

        assertTrue(actions.showInFolder(project.toString()))

        assertEquals(listOf(listOf("xdg-open", root.resolve("worlds").toString())), launched)
    }

    @Test
    fun `copied text reaches the clipboard as plain text`() = runBlocking {
        assertTrue(actions.copyText("/worlds/Embercourt"))

        val transferable = clipboard.entry?.asAwtTransferable
        assertEquals("/worlds/Embercourt", transferable?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `an unavailable clipboard is reported instead of thrown`() = runBlocking {
        clipboard.failure = IllegalStateException("cannot open system clipboard")

        assertFalse(actions.copyText("/worlds/Embercourt"))
    }

    private class RecordingClipboard : Clipboard {
        var entry: ClipEntry? = null
        var failure: RuntimeException? = null

        override suspend fun getClipEntry(): ClipEntry? = entry

        override suspend fun setClipEntry(clipEntry: ClipEntry?) {
            failure?.let { throw it }
            entry = clipEntry
        }
    }
}
