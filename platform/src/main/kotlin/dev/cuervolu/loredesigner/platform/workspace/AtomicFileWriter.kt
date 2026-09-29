package dev.cuervolu.loredesigner.platform.workspace

import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

internal interface WorkspaceFileWriter {
    fun create(target: Path, content: String)

    fun replace(target: Path, content: String)
}

internal interface FileOperations {
    fun move(source: Path, target: Path, vararg options: CopyOption)

    fun createLink(link: Path, existing: Path)

    fun copy(source: Path, target: Path)
}

private object NioFileOperations : FileOperations {
    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        Files.move(source, target, *options)
    }

    override fun createLink(link: Path, existing: Path) {
        Files.createLink(link, existing)
    }

    override fun copy(source: Path, target: Path) {
        Files.copy(source, target)
    }
}

internal class AtomicFileWriter(private val fileOperations: FileOperations = NioFileOperations) : WorkspaceFileWriter {
    override fun create(target: Path, content: String) {
        withTemporaryFile(target, content) { temporaryFile ->
            try {
                fileOperations.createLink(target, temporaryFile)
            } catch (_: UnsupportedOperationException) {
                fileOperations.copy(temporaryFile, target)
            }
        }
    }

    override fun replace(target: Path, content: String) {
        withTemporaryFile(target, content) { temporaryFile ->
            try {
                fileOperations.move(
                    temporaryFile,
                    target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: AtomicMoveNotSupportedException) {
                fileOperations.move(temporaryFile, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    private fun withTemporaryFile(target: Path, content: String, publish: (Path) -> Unit) {
        val temporaryFile = Files.createTempFile(target.parent, ".${target.fileName}.", ".tmp")
        try {
            Files.writeString(temporaryFile, content, StandardCharsets.UTF_8)
            publish(temporaryFile)
        } finally {
            Files.deleteIfExists(temporaryFile)
        }
    }
}
