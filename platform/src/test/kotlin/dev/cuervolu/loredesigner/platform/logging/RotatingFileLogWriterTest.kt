package dev.cuervolu.loredesigner.platform.logging

import co.touchlab.kermit.Severity
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.listDirectoryEntries
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
}
