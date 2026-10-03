package dev.cuervolu.loredesigner.platform.logging

import co.touchlab.kermit.Severity
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.io.path.createFile
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteExisting
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readLines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RotatingFileLogWriterTest {
    private fun newWriter(policy: LogRotationPolicy): Pair<RotatingFileLogWriter, Path> {
        val directory = createTempDirectory("logging-test")
        return RotatingFileLogWriter(directory, policy) to directory
    }

    private fun RotatingFileLogWriter.logLine(message: String) =
        log(Severity.Info, message, tag = "Test", throwable = null)

    private fun Path.archives() = listDirectoryEntries().filter {
        it.fileName.toString().startsWith("loredesigner.log.")
    }

    @Test
    fun `rotates the active file once it exceeds the size limit`() {
        val policy = LogRotationPolicy(maxFileSizeBytes = 200, maxArchivedFiles = 5, maxTotalSizeBytes = Long.MAX_VALUE)
        val (writer, directory) = newWriter(policy)

        repeat(20) { writer.logLine("x".repeat(20)) }

        assertTrue(
            directory.archives().isNotEmpty(),
            "expected at least one archived file after exceeding the size limit",
        )
    }

    @Test
    fun `retains at most the configured number of archived files`() {
        val policy = LogRotationPolicy(maxFileSizeBytes = 50, maxArchivedFiles = 2, maxTotalSizeBytes = Long.MAX_VALUE)
        val (writer, directory) = newWriter(policy)

        repeat(100) { writer.logLine("x".repeat(20)) }

        assertEquals(2, directory.archives().size)
    }

    @Test
    fun `enforces the total retained size across active file and archives`() {
        val policy = LogRotationPolicy(maxFileSizeBytes = 50, maxArchivedFiles = 5, maxTotalSizeBytes = 120)
        val (writer, directory) = newWriter(policy)

        repeat(100) { writer.logLine("x".repeat(20)) }

        val totalBytes = directory.listDirectoryEntries().sumOf { it.toFile().length() }
        assertTrue(
            totalBytes <= policy.maxTotalSizeBytes,
            "total retained size $totalBytes exceeded ${policy.maxTotalSizeBytes}",
        )
    }

    @Test
    fun `concurrent writers never lose or interleave lines`() {
        val policy =
            LogRotationPolicy(
                maxFileSizeBytes = Long.MAX_VALUE,
                maxArchivedFiles = 1,
                maxTotalSizeBytes = Long.MAX_VALUE,
            )
        val (writer, directory) = newWriter(policy)
        val start = CountDownLatch(1)

        val threads = (0 until 8).map { worker ->
            thread {
                start.await()
                repeat(250) { line -> writer.logLine("worker-$worker line-$line ${"x".repeat(200)}") }
            }
        }
        start.countDown()
        threads.forEach(Thread::join)

        val lines = directory.resolve("loredesigner.log").readLines()
        assertEquals(8 * 250, lines.size)
        assertTrue(lines.all { Regex("""^[\d-]+ [\d:.]+ INFO    \[Test] worker-\d line-\d+ x{200}$""").matches(it) })
    }

    @Test
    fun `an unwritable log location is reported once and never throws`() {
        val parent = createTempDirectory("logging-test")
        val blocked = parent.resolve("logs").createFile()
        val fallback = ByteArrayOutputStream()
        val writer = RotatingFileLogWriter(blocked, LogRotationPolicy.Default, PrintStream(fallback, true))

        repeat(3) { writer.logLine("lost") }

        assertEquals(1, fallback.toString().lines().count { it.contains("could not write to log file") })

        blocked.deleteExisting()
        writer.logLine("recovered")
        assertEquals(
            listOf("recovered"),
            blocked.resolve("loredesigner.log").readLines().map {
                it.substringAfter("] ")
            },
        )
    }

    private val fixedClock = Clock.fixed(Instant.parse("2026-10-03T17:05:09.123Z"), ZoneOffset.ofHours(-3))

    private fun fixedWriter(directory: Path) =
        RotatingFileLogWriter(directory, LogRotationPolicy.Default, clock = fixedClock)

    @Test
    fun `entries are single lines with local time, padded severity and tag`() {
        val directory = createTempDirectory("logging-test")
        val writer = fixedWriter(directory)

        writer.log(Severity.Info, "Workspace opened", tag = "Workspaces", throwable = null)
        writer.log(Severity.Verbose, "Saved state", tag = "State", throwable = null)

        assertEquals(
            listOf(
                "2026-10-03 14:05:09.123 INFO    [Workspaces] Workspace opened",
                "2026-10-03 14:05:09.123 VERBOSE [State] Saved state",
            ),
            directory.resolve("loredesigner.log").readLines(),
        )
    }

    @Test
    fun `extra message lines and stack traces are indented under their entry`() {
        val directory = createTempDirectory("logging-test")
        val writer = fixedWriter(directory)

        writer.log(Severity.Info, "Header\nkey  value", tag = "App", throwable = null)
        writer.log(Severity.Error, "Save failed", tag = "State", throwable = IllegalStateException("disk full"))

        val lines = directory.resolve("loredesigner.log").readLines()
        assertEquals("2026-10-03 14:05:09.123 INFO    [App] Header", lines[0])
        assertEquals("    key  value", lines[1])
        assertEquals("2026-10-03 14:05:09.123 ERROR   [State] Save failed", lines[2])
        assertEquals("    java.lang.IllegalStateException: disk full", lines[3])
        assertTrue(lines.drop(4).all { it.startsWith("        at ") }, "${lines.drop(4)}")
        assertEquals(2, lines.count { it.first().isDigit() }, "only entry lines start with a timestamp")
    }

    @Test
    fun `each run is separated from the previous one by a blank line`() {
        val directory = createTempDirectory("logging-test")

        fixedWriter(directory).apply {
            logLine("first run, entry 1")
            logLine("first run, entry 2")
        }
        fixedWriter(directory).logLine("second run")

        val lines = directory.resolve("loredesigner.log").readLines().map { it.substringAfter("] ") }
        assertEquals(listOf("first run, entry 1", "first run, entry 2", "", "second run"), lines)
    }
}
