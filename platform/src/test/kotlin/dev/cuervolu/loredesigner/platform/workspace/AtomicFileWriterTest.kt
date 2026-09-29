package dev.cuervolu.loredesigner.platform.workspace

import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createTempDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AtomicFileWriterTest {
    @Test
    fun `safely replaces an existing file without leaving its temporary file`() = withTempDirectory { directory ->
        val target = directory.resolve("project.lore")
        Files.writeString(target, "old")

        AtomicFileWriter().write(target, "new")

        assertEquals("new", target.readText())
        assertFalse(directory.listDirectoryEntries().any { it.fileName.toString().endsWith(".tmp") })
    }

    @Test
    fun `falls back to a non atomic replacement when atomic move is unsupported`() = withTempDirectory { directory ->
        val target = directory.resolve("project.lore")
        val mover = AtomicMoveRejectingMover()

        AtomicFileWriter(mover).write(target, "content")

        assertEquals("content", target.readText())
        assertEquals(2, mover.calls)
        assertTrue(mover.fallbackUsed)
    }

    @Test
    fun `removes the temporary file when replacement fails`() = withTempDirectory { directory ->
        val target = directory.resolve("project.lore")
        val writer =
            AtomicFileWriter(
                object : FileMover {
                    override fun move(source: Path, target: Path, vararg options: CopyOption): Unit =
                        throw IOException("test")
                },
            )

        assertFailsWith<IOException> { writer.write(target, "content") }

        assertTrue(directory.listDirectoryEntries().isEmpty())
    }

    private class AtomicMoveRejectingMover : FileMover {
        var calls = 0
        var fallbackUsed = false

        override fun move(source: Path, target: Path, vararg options: CopyOption) {
            calls++
            if (StandardCopyOption.ATOMIC_MOVE in options) {
                throw AtomicMoveNotSupportedException(source.toString(), target.toString(), "test")
            }
            fallbackUsed = true
            Files.move(source, target, *options)
        }
    }

    private fun withTempDirectory(block: (Path) -> Unit) {
        val directory = createTempDirectory("atomic-file-writer-test")
        try {
            block(directory)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}
