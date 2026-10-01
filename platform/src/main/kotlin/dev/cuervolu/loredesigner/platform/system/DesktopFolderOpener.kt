package dev.cuervolu.loredesigner.platform.system

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/** Starts an external program and returns as soon as it is running; it never waits for the program to exit. */
fun interface ProcessLauncher {
    @Throws(IOException::class)
    fun start(command: List<String>)
}

/** Launches without a shell and detaches the child's streams so nothing is retained once it has started. */
object SystemProcessLauncher : ProcessLauncher {
    override fun start(command: List<String>) {
        // The handle is dropped on purpose: the JDK reaps the child when it exits.
        ProcessBuilder(command)
            .redirectInput(ProcessBuilder.Redirect.from(File(if (isWindows()) "NUL" else "/dev/null")))
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
    }

    private fun isWindows() = System.getProperty("os.name").orEmpty().startsWith("Windows", ignoreCase = true)
}

/**
 * Reveals a folder in the platform file manager.
 *
 * On Linux this deliberately avoids `java.awt.Desktop.open()`. AWT drives GTK from its own thread
 * while the Nucleus (Tao) window runs its own GTK main loop, and the second call deadlocks the
 * whole application. FileKit's `openFileWithDefaultApplication()` is not an alternative on this
 * target because its JVM implementation delegates to the same `Desktop.open()`; its `xdg-open`
 * implementation only exists for the Linux Native target. So Linux launches `xdg-open` directly,
 * without a shell and with the path as its own argument. macOS and Windows keep `Desktop.open()`.
 *
 * Work always runs on [ioDispatcher], never on the UI thread.
 */
class DesktopFolderOpener(
    private val logger: Logger,
    private val osName: String = System.getProperty("os.name").orEmpty(),
    private val processLauncher: ProcessLauncher = SystemProcessLauncher,
    private val desktopOpen: (File) -> Boolean = ::openWithAwtDesktop,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    /** Returns `false` when the folder could not be created or no file manager could be launched. */
    suspend fun open(directory: Path): Boolean = withContext(ioDispatcher) {
        try {
            Files.createDirectories(directory)
            if (osName.startsWith("Linux", ignoreCase = true)) {
                processLauncher.start(listOf(XDG_OPEN, directory.toString()))
                true
            } else {
                desktopOpen(directory.toFile()).also { opened ->
                    if (!opened) logger.w { "Opening folders is not supported on $osName" }
                }
            }
        } catch (exception: IOException) {
            logger.w(exception) { "Could not open folder $directory" }
            false
        } catch (exception: SecurityException) {
            logger.w(exception) { "Not allowed to open folder $directory" }
            false
        }
    }

    private companion object {
        const val XDG_OPEN = "xdg-open"
    }
}

private fun openWithAwtDesktop(directory: File): Boolean {
    // getDesktop() throws on headless or unsupported platforms, so it is only called after the check.
    if (!Desktop.isDesktopSupported()) return false
    val desktop = Desktop.getDesktop().takeIf { it.isSupported(Desktop.Action.OPEN) } ?: return false
    desktop.open(directory)
    return true
}
