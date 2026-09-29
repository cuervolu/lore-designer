package dev.cuervolu.loredesigner.platform.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import java.io.File
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.createDirectories

/**
 * Kermit [LogWriter] that appends to a single active file under [directory], rotating it by size
 * per [policy]. Retention enforces both a maximum archive count (fixed archive slots) and a
 * maximum total size across the active file and its archives.
 */
class RotatingFileLogWriter(directory: Path, private val policy: LogRotationPolicy = LogRotationPolicy.Default) :
    LogWriter() {
    private val directory: File = directory.createDirectories().toFile()
    private val activeFile: File = File(this.directory, ACTIVE_FILE_NAME)

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        rotateIfNeeded()
        val line = buildString {
            append(Instant.now())
            append(' ')
            append(severity.name)
            append(" [")
            append(tag)
            append("] ")
            append(message)
            if (throwable != null) {
                append(" - ")
                append(throwable.stackTraceToString())
            }
        }
        activeFile.appendText(line + System.lineSeparator())
        enforceTotalSize()
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
    }
}
