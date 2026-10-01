package dev.cuervolu.loredesigner.platform.workspace

import okio.FileSystem
import okio.IOException
import okio.Path
import kotlin.random.Random

internal class AtomicFileWriter(private val fileSystem: FileSystem) {
    /**
     * Creates [target] with [content], failing if it already exists.
     *
     * Okio has no exclusive atomic publish, so the target is first reserved with an exclusive empty
     * create and then atomically replaced by a fully written temporary file. The reservation only
     * closes the initial creation race; it is not a lock. A crash after reserving can leave an empty
     * target behind, which reopens as an invalid project file.
     */
    fun create(target: Path, content: String) {
        fileSystem.write(target, mustCreate = true) {}
        val temporaryFile = temporaryFileFor(target)
        try {
            fileSystem.write(temporaryFile, mustCreate = true) { writeUtf8(content) }
            fileSystem.atomicMove(temporaryFile, target)
        } catch (failure: Exception) {
            deleteAfterFailure(temporaryFile, failure)
            deleteAfterFailure(target, failure)
            throw failure
        }
    }

    /** Replaces (or creates) [target] with [content] so readers only ever see the old or the new file. */
    fun replace(target: Path, content: String) {
        val temporaryFile = temporaryFileFor(target)
        try {
            fileSystem.write(temporaryFile, mustCreate = true) { writeUtf8(content) }
            fileSystem.atomicMove(temporaryFile, target)
        } catch (failure: Exception) {
            deleteAfterFailure(temporaryFile, failure)
            throw failure
        }
    }

    private fun temporaryFileFor(target: Path): Path =
        requireNotNull(target.parent) / ".${target.name}.${Random.nextLong().toULong().toString(16)}.tmp"

    private fun deleteAfterFailure(path: Path, originalFailure: Exception) {
        try {
            fileSystem.delete(path, mustExist = false)
        } catch (cleanupFailure: IOException) {
            originalFailure.addSuppressed(cleanupFailure)
        }
    }
}
