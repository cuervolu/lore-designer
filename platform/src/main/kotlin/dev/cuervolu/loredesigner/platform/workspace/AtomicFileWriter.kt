package dev.cuervolu.loredesigner.platform.workspace

import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

internal fun interface WorkspaceFileWriter {
    fun write(target: Path, content: String)
}

internal interface FileMover {
    fun move(source: Path, target: Path, vararg options: CopyOption)
}

private object NioFileMover : FileMover {
    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        Files.move(source, target, *options)
    }
}

internal class AtomicFileWriter(private val fileMover: FileMover = NioFileMover) : WorkspaceFileWriter {
    override fun write(target: Path, content: String) {
        val temporaryFile = Files.createTempFile(target.parent, ".${target.fileName}.", ".tmp")
        try {
            Files.writeString(temporaryFile, content, StandardCharsets.UTF_8)
            try {
                fileMover.move(
                    temporaryFile,
                    target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: AtomicMoveNotSupportedException) {
                fileMover.move(temporaryFile, target, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporaryFile)
        }
    }
}
