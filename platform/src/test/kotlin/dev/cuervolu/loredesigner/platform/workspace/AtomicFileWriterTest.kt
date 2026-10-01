package dev.cuervolu.loredesigner.platform.workspace

import dev.cuervolu.loredesigner.platform.workspace.FaultyFileSystem.Operation
import okio.IOException
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AtomicFileWriterTest {
    private val fakeFileSystem = FakeFileSystem()
    private val fileSystem = FaultyFileSystem(fakeFileSystem)
    private val directory = "/world".toPath().also { fakeFileSystem.createDirectories(it) }
    private val target = directory / "project.lore"

    @AfterTest
    fun noOpenFiles() {
        fakeFileSystem.checkNoOpenFiles()
    }

    @Test
    fun `creates the file with its content without leaving a temporary file`() {
        AtomicFileWriter(fileSystem).create(target, "content")

        assertEquals("content", fakeFileSystem.read(target) { readUtf8() })
        assertEquals(listOf(target), fakeFileSystem.list(directory))
    }

    @Test
    fun `fails on an existing target without changing it`() {
        fakeFileSystem.write(target) { writeUtf8("external content") }

        assertFailsWith<IOException> { AtomicFileWriter(fileSystem).create(target, "workspace content") }

        assertEquals("external content", fakeFileSystem.read(target) { readUtf8() })
        assertEquals(listOf(target), fakeFileSystem.list(directory))
    }

    @Test
    fun `removes the temporary file and the reservation when writing fails`() {
        fileSystem.beforeOperation = { operation, path ->
            if (operation == Operation.SINK && path.name.endsWith(".tmp")) throw IOException("disk full")
        }

        assertFailsWith<IOException> { AtomicFileWriter(fileSystem).create(target, "content") }

        assertTrue(fakeFileSystem.list(directory).isEmpty())
    }

    @Test
    fun `removes the temporary file and the reservation when publishing fails`() {
        fileSystem.beforeOperation = { operation, _ ->
            if (operation == Operation.ATOMIC_MOVE) throw IOException("move failed")
        }

        assertFailsWith<IOException> { AtomicFileWriter(fileSystem).create(target, "content") }

        assertTrue(fakeFileSystem.list(directory).isEmpty())
    }

    @Test
    fun `cleanup failures are attached to the original failure`() {
        fileSystem.beforeOperation = { operation, _ ->
            when (operation) {
                Operation.ATOMIC_MOVE -> throw IOException("move failed")
                Operation.DELETE -> throw IOException("delete failed")
                else -> Unit
            }
        }

        val failure = assertFailsWith<IOException> { AtomicFileWriter(fileSystem).create(target, "content") }

        assertEquals("move failed", failure.message)
        assertEquals(listOf("delete failed", "delete failed"), failure.suppressed.map { it.message })
    }
}
