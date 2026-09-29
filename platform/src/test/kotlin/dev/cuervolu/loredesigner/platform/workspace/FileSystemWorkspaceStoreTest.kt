package dev.cuervolu.loredesigner.platform.workspace

import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.OpenWorkspace
import dev.cuervolu.loredesigner.workspace.Workspace
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import dev.cuervolu.loredesigner.workspace.WorkspaceIdGenerator
import dev.cuervolu.loredesigner.workspace.WorkspaceResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FileSystemWorkspaceStoreTest {
    private val id = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb380")

    @Test
    fun `creates the expected structure and can reopen the same workspace`() = runTestWithTempDirectory { parent ->
        val location = parent.resolve("My World")
        val store = FileSystemWorkspaceStore()

        val created = CreateWorkspace(store, WorkspaceIdGenerator { id })(location, "My World", ProjectColor.VIOLET)
        val opened = OpenWorkspace(store)(location)

        val createdWorkspace = assertIs<WorkspaceResult.Success<Workspace>>(created).value
        val openedWorkspace = assertIs<WorkspaceResult.Success<Workspace>>(opened).value
        assertTrue(location.resolve("project.lore").exists())
        assertTrue(location.resolve(".lore").isDirectory())
        val typesFile = location.resolve(".lore/types.json")
        assertTrue(typesFile.exists())
        val typesJson = Json.parseToJsonElement(typesFile.readText()).jsonObject
        assertEquals(1, typesJson.getValue("version").jsonPrimitive.content.toInt())
        assertTrue(typesJson.getValue("types").jsonArray.isEmpty())
        assertEquals(createdWorkspace.config, openedWorkspace.config)
        assertEquals(id, openedWorkspace.config.id)
        assertEquals("My World", openedWorkspace.config.name)
        assertEquals(ProjectColor.VIOLET, openedWorkspace.config.color)
    }

    @Test
    fun `creates a workspace in an existing empty directory`() = runTestWithTempDirectory { parent ->
        val location = Files.createDirectory(parent.resolve("existing"))

        val result = CreateWorkspace(FileSystemWorkspaceStore(), WorkspaceIdGenerator { id })(location, "Existing")

        assertIs<WorkspaceResult.Success<Workspace>>(result)
    }

    @Test
    fun `rejects a non empty destination without changing it`() = runTestWithTempDirectory { parent ->
        val location = Files.createDirectory(parent.resolve("occupied"))
        val existingFile = Files.writeString(location.resolve("notes.txt"), "keep me")

        val result = CreateWorkspace(FileSystemWorkspaceStore(), WorkspaceIdGenerator { id })(location, "Occupied")

        assertIs<WorkspaceError.DestinationNotEmpty>(assertIs<WorkspaceResult.Failure>(result).error)
        assertEquals("keep me", existingFile.readText())
        assertEquals(listOf(existingFile), location.listDirectoryEntries())
    }

    @Test
    fun `opening a directory without project file reports not a workspace`() = runTestWithTempDirectory { parent ->
        val location = Files.createDirectory(parent.resolve("empty"))

        val result = OpenWorkspace(FileSystemWorkspaceStore())(location)

        assertIs<WorkspaceError.NotAWorkspace>(assertIs<WorkspaceResult.Failure>(result).error)
    }

    @Test
    fun `filesystem failures are returned with context`() = runTestWithTempDirectory { parent ->
        val regularFile = Files.writeString(parent.resolve("regular-file"), "content")
        val location = regularFile.resolve("workspace")

        val result = CreateWorkspace(FileSystemWorkspaceStore(), WorkspaceIdGenerator { id })(location, "World")

        val error = assertIs<WorkspaceError.FileSystemFailure>(assertIs<WorkspaceResult.Failure>(result).error)
        assertEquals(location.toAbsolutePath().normalize(), error.path)
    }

    @Test
    fun `failed creation removes only resources created in this execution`() = runTestWithTempDirectory { parent ->
        val existingLocation = Files.createDirectory(parent.resolve("existing"))
        val failingWriter = FailOnSecondWrite()
        val store = FileSystemWorkspaceStore.withFileWriter(failingWriter)
        val config = ProjectConfig(1, id, "World", null)

        val existingResult = store.create(existingLocation, config)
        val newLocation = parent.resolve("new")
        val newResult = FileSystemWorkspaceStore.withFileWriter(FailOnSecondWrite()).create(newLocation, config)

        assertIs<WorkspaceError.FileSystemFailure>(assertIs<WorkspaceResult.Failure>(existingResult).error)
        assertTrue(existingLocation.isDirectory())
        assertTrue(existingLocation.listDirectoryEntries().isEmpty())
        assertIs<WorkspaceError.FileSystemFailure>(assertIs<WorkspaceResult.Failure>(newResult).error)
        assertFalse(newLocation.exists())
    }

    @Test
    fun `file appearing during creation is preserved and reported as an io failure`() =
        runTestWithTempDirectory { parent ->
            val location = parent.resolve("racing")
            val writer = FileAppearingBeforeProjectCreation()
            val store = FileSystemWorkspaceStore.withFileWriter(writer)
            val config = ProjectConfig(1, id, "World", null)

            val result = store.create(location, config)

            assertIs<WorkspaceError.FileSystemFailure>(assertIs<WorkspaceResult.Failure>(result).error)
            assertEquals("external project", location.resolve("project.lore").readText())
            assertFalse(location.resolve(".lore/types.json").exists())
            assertFalse(location.resolve(".lore").exists())
        }

    @Test
    fun `unexpected writer failures are rolled back and rethrown`() = runTestWithTempDirectory { parent ->
        val location = parent.resolve("bug")
        val store =
            FileSystemWorkspaceStore.withFileWriter(
                object : WorkspaceFileWriter {
                    override fun create(target: Path, content: String) {
                        error("programming bug")
                    }

                    override fun replace(target: Path, content: String) = error("not used")
                },
            )
        val config = ProjectConfig(1, id, "World", null)

        assertFailsWith<IllegalStateException> { store.create(location, config) }

        assertFalse(location.exists())
    }

    private class FailOnSecondWrite : WorkspaceFileWriter {
        private val delegate = AtomicFileWriter()
        private var calls = 0

        override fun create(target: Path, content: String) {
            calls++
            if (calls == 2) throw IOException("simulated write failure")
            delegate.create(target, content)
        }

        override fun replace(target: Path, content: String) = delegate.replace(target, content)
    }

    private class FileAppearingBeforeProjectCreation : WorkspaceFileWriter {
        private val delegate = AtomicFileWriter()

        override fun create(target: Path, content: String) {
            if (target.fileName.toString() == "project.lore") {
                Files.writeString(target, "external project")
            }
            delegate.create(target, content)
        }

        override fun replace(target: Path, content: String) = delegate.replace(target, content)
    }

    private fun runTestWithTempDirectory(block: suspend (Path) -> Unit) = runTest {
        val directory = createTempDirectory("workspace-store-test")
        try {
            block(directory)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}
