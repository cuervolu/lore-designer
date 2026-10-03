package dev.cuervolu.loredesigner.platform.logging

import co.touchlab.kermit.Severity
import kotlin.io.path.createTempDirectory
import kotlin.io.path.readLines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class LoggingSetupTest {
    private val directory = createTempDirectory("logging-setup-test")
    private val logging = createLoreDesignerLogging(directory)

    private fun logLines() = directory.resolve("loredesigner.log").readLines()

    @Test
    fun `diagnostic logging adds debug and verbose output to the log file`() {
        val logger = logging.logger.withTag("Probe")

        logger.d { "hidden debug" }
        logger.v { "hidden verbose" }
        logging.diagnostics.setEnabled(true)
        logger.d { "visible debug" }
        logger.v { "visible verbose" }
        logging.diagnostics.setEnabled(false)
        logger.d { "hidden again" }
        logger.i { "always visible" }

        val messages = logLines().map { it.substringAfter("] ") }
        assertEquals(
            listOf(
                "Diagnostic logging enabled",
                "visible debug",
                "visible verbose",
                "Diagnostic logging disabled",
                "always visible",
            ),
            messages,
        )
    }

    @Test
    fun `applying an unchanged diagnostic setting is not logged`() {
        logging.diagnostics.setEnabled(false)
        logging.logger.i { "marker" }

        assertFalse(logging.diagnostics.enabled)
        assertEquals(1, logLines().size)
    }

    @Test
    fun `uncaught exceptions are logged and passed on to the previous handler`() {
        val logs = RecordingLogWriter()
        val delegated = mutableListOf<Throwable>()
        val handler = LoggingUncaughtExceptionHandler(logs.logger(APP_TAG)) { _, throwable -> delegated += throwable }
        val failure = IllegalStateException("boom")

        handler.uncaughtException(Thread("worker-7"), failure)

        val entry = logs.entries.single()
        assertEquals(Severity.Error, entry.severity)
        assertSame(failure, entry.throwable)
        assertTrue("worker-7" in entry.message)
        assertEquals(listOf<Throwable>(failure), delegated)
    }
}
