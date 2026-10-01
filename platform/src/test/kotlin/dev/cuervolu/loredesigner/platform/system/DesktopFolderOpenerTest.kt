package dev.cuervolu.loredesigner.platform.system

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Collections
import kotlin.io.path.createTempDirectory
import kotlin.io.path.isDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopFolderOpenerTest {
    private val root: Path = createTempDirectory("folder-opener-test")
    private val launched: MutableList<List<String>> = Collections.synchronizedList(mutableListOf())
    private val desktopOpened = mutableListOf<File>()

    @AfterTest
    fun cleanUp() {
        root.toFile().deleteRecursively()
    }

    private fun opener(osName: String, launcher: ProcessLauncher = ProcessLauncher { launched += it }) =
        DesktopFolderOpener(
            logger = Logger.withTag("test"),
            osName = osName,
            processLauncher = launcher,
            desktopOpen = {
                desktopOpened += it
                true
            },
        )

    @Test
    fun `linux launches xdg-open directly with the path as a single argument`() = runTest {
        val folder = root.resolve("app data").resolve("logs")

        assertTrue(opener("Linux").open(folder))

        assertEquals(listOf(listOf("xdg-open", folder.toString())), launched)
        assertTrue(folder.isDirectory(), "Missing folders are created before opening")
        assertTrue(desktopOpened.isEmpty(), "Linux must not go through java.awt.Desktop")
    }

    @Test
    fun `repeated opens each launch independently and never block`() = runTest {
        val opener = opener("Linux")
        val logs = root.resolve("logs")

        // Real time: opening runs on Dispatchers.IO, which the test scheduler's virtual clock does not track.
        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(5_000) {
                repeat(3) { assertTrue(opener.open(logs)) }
                assertTrue(opener.open(root))
                (1..5).map { async { opener.open(logs) } }.awaitAll().forEach { assertTrue(it) }
            }
        }

        assertEquals(9, launched.size)
        assertEquals(listOf("xdg-open", root.toString()), launched[3])
    }

    @Test
    fun `macOS and Windows keep the desktop integration`() = runTest {
        for (os in listOf("Mac OS X", "Windows 11")) {
            assertTrue(opener(os).open(root))
        }

        assertEquals(listOf(root.toFile(), root.toFile()), desktopOpened)
        assertTrue(launched.isEmpty())
    }

    @Test
    fun `launch failures are reported and later attempts still run`() = runTest {
        var failNext = true
        val opener = opener("Linux") { command ->
            if (failNext) {
                failNext = false
                throw IOException("xdg-open not found")
            }
            launched += command
        }

        assertFalse(opener.open(root))
        assertTrue(opener.open(root))
        assertEquals(1, launched.size)
    }

    @Test
    fun `work runs on the injected dispatcher, not the caller`() = runTest {
        val opener = DesktopFolderOpener(
            logger = Logger.withTag("test"),
            osName = "Linux",
            processLauncher = { launched += it },
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        assertTrue(opener.open(root))
        assertEquals(1, launched.size)
    }

    @Test
    fun `system launcher returns while the child is still running`() {
        val sleep = listOf("/bin/sleep", "/usr/bin/sleep").firstOrNull { Files.isExecutable(Path.of(it)) } ?: return

        val started = System.nanoTime()
        repeat(3) { SystemProcessLauncher.start(listOf(sleep, "3")) }
        val elapsedMillis = (System.nanoTime() - started) / 1_000_000

        assertTrue(elapsedMillis < 2_000, "Launching waited for the child to exit ($elapsedMillis ms)")
    }
}
