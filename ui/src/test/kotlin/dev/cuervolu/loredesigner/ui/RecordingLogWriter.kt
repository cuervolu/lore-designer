package dev.cuervolu.loredesigner.ui

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.mutableLoggerConfigInit
import java.util.concurrent.CopyOnWriteArrayList

/** Captures log entries so tests can assert on severity, tag, message and attached throwable. */
internal class RecordingLogWriter : LogWriter() {
    data class Entry(val severity: Severity, val tag: String, val message: String, val throwable: Throwable?)

    val entries: MutableList<Entry> = CopyOnWriteArrayList()

    fun at(severity: Severity): List<Entry> = entries.filter { it.severity == severity }

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        entries += Entry(severity, tag, message, throwable)
    }

    fun logger(tag: String = "Test", minSeverity: Severity = Severity.Verbose): Logger =
        Logger(mutableLoggerConfigInit(this, minSeverity = minSeverity), tag)
}
