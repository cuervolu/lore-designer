package dev.cuervolu.loredesigner.platform.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.nio.file.Path
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Kermit [LogWriter] that appends to a single active file under [directory], rotating it by size
 * per [policy]. Retention enforces both a maximum archive count (fixed archive slots) and a
 * maximum total size across the active file and its archives.
 *
 * Each entry starts on its own line as `2026-10-03 14:05:09.123 INFO [Tag] message`, in [clock]'s
 * local time, so a line that begins with a digit always starts an entry. Further lines of a message
 * and stack traces are indented underneath it. The first entry of each run is preceded by a blank
 * line, so separate application runs stand apart in a shared file.
 *
 * Kermit calls writers on the logging thread, so writes are serialized here. A logging failure must
 * never surface in the code that was logging (often a catch block already handling a failure), so I/O
 * problems are reported once to [fallback] and logging resumes as soon as the file is writable again.
 */
class RotatingFileLogWriter(
    directory: Path,
    private val policy: LogRotationPolicy = LogRotationPolicy.Default,
    private val fallback: PrintStream = System.err,
    private val clock: Clock = Clock.systemDefaultZone(),
) : LogWriter() {
    private val directory: File = directory.toFile()
    private val activeFile: File = File(this.directory, ACTIVE_FILE_NAME)
    private val lock = Any()
    private var failing = false
    private var startedRun = false

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val entry = format(severity, message, tag, throwable)
        synchronized(lock) {
            try {
                directory.mkdirs()
                rotateIfNeeded()
                val separator = if (!startedRun && activeFile.length() > 0) System.lineSeparator() else ""
                activeFile.appendText(separator + entry)
                startedRun = true
                enforceTotalSize()
                failing = false
            } catch (exception: IOException) {
                reportFailure(exception)
            } catch (exception: SecurityException) {
                reportFailure(exception)
            }
        }
    }

    private fun format(severity: Severity, message: String, tag: String, throwable: Throwable?): String {
        val lines = message.lines() + throwable?.stackTraceToString()?.trimEnd()?.lines().orEmpty()
        return buildString {
            append(TIMESTAMP.format(LocalDateTime.now(clock)))
            append(' ')
            append(severity.name.uppercase().padEnd(SEVERITY_WIDTH))
            append(" [")
            append(tag)
            append("] ")
            append(lines.first())
            append(System.lineSeparator())
            for (line in lines.drop(1)) {
                append(CONTINUATION_INDENT)
                append(line.replace("\t", CONTINUATION_INDENT))
                append(System.lineSeparator())
            }
        }
    }

    private fun reportFailure(exception: Exception) {
        if (failing) return
        failing = true
        fallback.println("Lore Designer could not write to log file $activeFile: $exception")
    }

    private fun rotateIfNeeded() {
        if (!activeFile.exists() || activeFile.length() < policy.maxFileSizeBytes) return

        for (index in policy.maxArchivedFiles downTo 1) {
            val source = archiveFile(index)
            if (!source.exists()) continue
            if (index == policy.maxArchivedFiles) {
                source.delete()
            } else {
                source.renameTo(archiveFile(index + 1))
            }
        }
        activeFile.renameTo(archiveFile(1))
    }

    private fun enforceTotalSize() {
        // Archive index 1 is the most recently rotated file; higher indices are older. Zero-padded
        // names sort naturally regardless of locale, so the descending-name order is oldest-first.
        val oldestFirst = existingArchives().sortedByDescending { it.name }
        var totalBytes = activeFile.length() + oldestFirst.sumOf(File::length)
        for (archive in oldestFirst) {
            if (totalBytes <= policy.maxTotalSizeBytes) break
            totalBytes -= archive.length()
            archive.delete()
        }
    }

    private fun existingArchives(): List<File> = (1..policy.maxArchivedFiles).map(::archiveFile).filter(File::exists)

    private fun archiveFile(index: Int): File = File(directory, "$ACTIVE_FILE_NAME.${"%02d".format(index)}")

    private companion object {
        const val ACTIVE_FILE_NAME = "loredesigner.log"
        const val CONTINUATION_INDENT = "    "

        // Wide enough for VERBOSE, so messages line up across severities.
        val SEVERITY_WIDTH = Severity.entries.maxOf { it.name.length }
        val TIMESTAMP: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    }
}
