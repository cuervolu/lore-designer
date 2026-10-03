package dev.cuervolu.loredesigner.platform.workspace

import co.touchlab.kermit.Severity
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.platform.logging.RecordingLogWriter
import dev.cuervolu.loredesigner.platform.workspace.FaultyFileSystem.Operation
import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.FileSystemOperation
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
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class FileSystemWorkspaceStoreTest {
    private val logs = RecordingLogWriter()
    private val id = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb380")
    private val config = ProjectConfig(1, id, "World", null)
    private val fakeFileSystem = FakeFileSystem()
    private val fileSystem = FaultyFileSystem(fakeFileSystem)
    private val store = FileSystemWorkspaceStore(fileSystem, logs.logger())
    private val parent = "/worlds".toPath().also { fakeFileSystem.createDirectories(it) }

    private val createWorkspace = CreateWorkspace(store, WorkspaceIdGenerator { id })
    private val openWorkspace = OpenWorkspace(store)

    @AfterTest
    fun noOpenFiles() {
        fakeFileSystem.checkNoOpenFiles()
    }

    @Test
    fun `created workspaces reopen with the persisted configuration and expected files`() = runTest {
        for (color in ProjectColor.entries + null) {
            val name = "World ${color ?: "plain"}"
            val location = parent / name

            val created = createWorkspace(parent, name, color).successValue()
            val opened = openWorkspace(location).successValue()

            assertEquals(location, created.location)
            assertEquals(created, opened)
            assertEquals(ProjectConfig(1, id, name, color), opened.config)
            assertEquals(listOf(location / ".lore", location / "project.lore"), fakeFileSystem.list(location).sorted())
            assertEquals(listOf(location / ".lore" / "types.json"), fakeFileSystem.list(location / ".lore"))
            val projectFile = location / "project.lore"
            val persisted = ProjectFileCodec().decode(projectFile, fakeFileSystem.read(projectFile) { readUtf8() })
            assertEquals(opened.config, assertIs<WorkspaceResult.Success<ProjectConfig>>(persisted).value)
            val typesJson =
                Json.parseToJsonElement(fakeFileSystem.read(location / ".lore" / "types.json") { readUtf8() })
                    .jsonObject
            assertEquals(1, typesJson.getValue("version").jsonPrimitive.content.toInt())
            assertTrue(typesJson.getValue("types").jsonArray.isEmpty())
        }
    }

    @Test
    fun `relative locations resolve against the working directory`() = runTest {
        fakeFileSystem.workingDirectory = "/home/writer".toPath()
        fakeFileSystem.createDirectories("/home/writer/worlds".toPath())

        val created = createWorkspace("worlds".toPath(), "Ceili").successValue()
        val opened = openWorkspace("worlds/../worlds/Ceili".toPath()).successValue()

        assertEquals("/home/writer/worlds/Ceili".toPath(), created.location)
        assertEquals(created.location, opened.location)
    }

    @Test
    fun `creates a workspace in an existing empty directory`() = runTest {
        fakeFileSystem.createDirectory(parent / "Existing")

        createWorkspace(parent, "Existing").successValue()

        assertTrue(fakeFileSystem.exists(parent / "Existing" / "project.lore"))
    }

    @Test
    fun `rejects a non empty destination without changing it`() = runTest {
        val location = parent / "Occupied"
        fakeFileSystem.createDirectory(location)
        fakeFileSystem.write(location / "notes.txt") { writeUtf8("keep me") }

        val error = createWorkspace(parent, "Occupied").failureError()

        assertEquals(WorkspaceError.DestinationNotEmpty(location), error)
        assertEquals(listOf(location / "notes.txt"), fakeFileSystem.list(location))
        assertEquals("keep me", fakeFileSystem.read(location / "notes.txt") { readUtf8() })
    }

    @Test
    fun `rejects a location that is a regular file without changing it`() = runTest {
        val location = parent / "World"
        fakeFileSystem.write(location) { writeUtf8("not a folder") }

        val error = createWorkspace(parent, "World").failureError()

        assertEquals(WorkspaceError.InvalidWorkspaceLocation(location), error)
        assertEquals("not a folder", fakeFileSystem.read(location) { readUtf8() })
    }

    @Test
    fun `follows a symlink to an empty directory and rejects a dangling one`() = runTest {
        fakeFileSystem.allowSymlinks = true
        val target = "/elsewhere/Linked".toPath()
        fakeFileSystem.createDirectories(target)
        fakeFileSystem.createSymlink(parent / "Linked", target)
        fakeFileSystem.createSymlink(parent / "Dangling", "/missing".toPath())

        createWorkspace(parent, "Linked").successValue()
        val danglingError = createWorkspace(parent, "Dangling").failureError()

        assertTrue(fakeFileSystem.exists(target / "project.lore"))
        assertEquals(config.copy(name = "Linked"), openWorkspace(parent / "Linked").successValue().config)
        assertEquals(WorkspaceError.InvalidWorkspaceLocation(parent / "Dangling"), danglingError)
    }

    @Test
    fun `opening anything other than a workspace folder reports not a workspace`() = runTest {
        fakeFileSystem.createDirectory(parent / "empty")
        fakeFileSystem.createDirectories(parent / "odd" / "project.lore")
        fakeFileSystem.write(parent / "file") { writeUtf8("text") }

        for (location in listOf(parent / "missing", parent / "empty", parent / "odd", parent / "file")) {
            assertEquals(WorkspaceError.NotAWorkspace(location), openWorkspace(location).failureError())
        }
    }

    @Test
    fun `invalid project files are reported with the project file path`() = runTest {
        val validId = id.toString()
        val cases: List<Pair<String, KClass<out WorkspaceError>>> =
            listOf(
                "" to WorkspaceError.InvalidProjectFile::class,
                "version = = 1" to WorkspaceError.InvalidProjectFile::class,
                "version = 2\nid = \"$validId\"\nname = \"W\"" to WorkspaceError.UnsupportedProjectVersion::class,
                "version = 1\nid = \"8c4a2c1e-0f4b-4d6e-9a3f-2b1c0d9e8f7a\"\nname = \"W\"" to
                    WorkspaceError.InvalidWorkspaceId::class,
                "version = 1\nid = \"$validId\"\nname = \"W\"\ncolor = \"magenta\"" to
                    WorkspaceError.UnknownProjectColor::class,
            )
        val location = parent / "Damaged"
        val projectFile = location / "project.lore"
        fakeFileSystem.createDirectory(location)

        for ((content, expected) in cases) {
            fakeFileSystem.write(projectFile) { writeUtf8(content) }

            val error = openWorkspace(location).failureError()

            assertTrue(expected.isInstance(error), "Expected ${expected.simpleName} for <$content> but got $error")
            assertEquals(projectFile, error.path())
        }
    }

    @Test
    fun `read failures on open are returned with context`() = runTest {
        createWorkspace(parent, "World").successValue()
        fileSystem.beforeOperation = { operation, _ ->
            if (operation == Operation.SOURCE) throw IOException("unreadable")
        }

        val error = openWorkspace(parent / "World").failureError()

        val failure = assertIs<WorkspaceError.FileSystemFailure>(error)
        assertEquals(parent / "World" / "project.lore", failure.path)
        assertEquals(FileSystemOperation.READ, failure.operation)
    }

    @Test
    fun `failure to create the workspace folder is returned with context`() = runTest {
        failOn(Operation.CREATE_DIRECTORY, "World")

        val failure = assertIs<WorkspaceError.FileSystemFailure>(createWorkspace(parent, "World").failureError())

        assertEquals(parent / "World", failure.path)
        assertEquals(FileSystemOperation.CREATE_DIRECTORY, failure.operation)
        assertFalse(fakeFileSystem.exists(parent / "World"))
    }

    @Test
    fun `failure to create the metadata folder removes the new workspace folder`() = runTest {
        failOn(Operation.CREATE_DIRECTORY, ".lore")

        val failure = assertIs<WorkspaceError.FileSystemFailure>(createWorkspace(parent, "World").failureError())

        assertEquals(parent / "World" / ".lore", failure.path)
        assertEquals(FileSystemOperation.CREATE_DIRECTORY, failure.operation)
        assertFalse(fakeFileSystem.exists(parent / "World"))
    }

    @Test
    fun `failure to write the types file removes everything created`() = runTest {
        failOn(Operation.ATOMIC_MOVE, "types.json")

        val failure = assertIs<WorkspaceError.FileSystemFailure>(createWorkspace(parent, "World").failureError())

        assertEquals(parent / "World" / ".lore" / "types.json", failure.path)
        assertEquals(FileSystemOperation.WRITE, failure.operation)
        assertFalse(fakeFileSystem.exists(parent / "World"))
    }

    @Test
    fun `failed creation removes only resources created in this execution`() = runTest {
        val existing = parent / "existing"
        fakeFileSystem.createDirectory(existing)
        failOn(Operation.ATOMIC_MOVE, "project.lore")

        val existingFailure = assertIs<WorkspaceError.FileSystemFailure>(store.create(existing, config).failureError())
        val newFailure = assertIs<WorkspaceError.FileSystemFailure>(store.create(parent / "new", config).failureError())

        assertEquals(existing / "project.lore", existingFailure.path)
        assertEquals(FileSystemOperation.WRITE, existingFailure.operation)
        assertTrue(fakeFileSystem.metadata(existing).isDirectory)
        assertTrue(fakeFileSystem.list(existing).isEmpty())
        assertEquals(parent / "new" / "project.lore", newFailure.path)
        assertFalse(fakeFileSystem.exists(parent / "new"))
    }

    @Test
    fun `rollback cleanup failures are attached to the original failure`() = runTest {
        fileSystem.beforeOperation = { operation, path ->
            if (operation == Operation.ATOMIC_MOVE && path.name == "project.lore") throw IOException("write failed")
            if (operation == Operation.DELETE && path.name == ".lore") throw IOException("cleanup failed")
        }

        val failure = assertIs<WorkspaceError.FileSystemFailure>(createWorkspace(parent, "World").failureError())

        assertEquals("write failed", failure.cause.message)
        assertTrue(failure.cause.suppressed.any { it.message == "cleanup failed" })
    }

    @Test
    fun `project file appearing during creation is preserved and reported`() = runTest {
        val location = parent / "racing"
        fileSystem.beforeOperation = { operation, path ->
            if (operation == Operation.SINK && path.name == "project.lore" && !fakeFileSystem.exists(path)) {
                fakeFileSystem.write(path) { writeUtf8("external project") }
            }
        }

        val failure = assertIs<WorkspaceError.FileSystemFailure>(store.create(location, config).failureError())

        assertEquals(location / "project.lore", failure.path)
        assertEquals("external project", fakeFileSystem.read(location / "project.lore") { readUtf8() })
        assertEquals(listOf(location / "project.lore"), fakeFileSystem.list(location))
    }

    @Test
    fun `metadata folder appearing during creation is preserved and reported`() = runTest {
        val location = parent / "racing"
        val external = location / ".lore" / "external.txt"
        fileSystem.beforeOperation = { operation, path ->
            if (operation == Operation.CREATE_DIRECTORY && path.name == ".lore") {
                fakeFileSystem.createDirectory(path)
                fakeFileSystem.write(external) { writeUtf8("external") }
            }
        }

        val failure = assertIs<WorkspaceError.FileSystemFailure>(store.create(location, config).failureError())

        assertEquals(location / ".lore", failure.path)
        assertEquals(FileSystemOperation.CREATE_DIRECTORY, failure.operation)
        assertEquals("external", fakeFileSystem.read(external) { readUtf8() })
        assertFalse(fakeFileSystem.exists(location / "project.lore"))
    }

    @Test
    fun `unexpected failures are rolled back and rethrown`() = runTest {
        fileSystem.beforeOperation = { operation, path ->
            if (operation == Operation.SINK && path.name == "types.json") error("programming bug")
        }

        assertFailsWith<IllegalStateException> { store.create(parent / "bug", config) }

        assertFalse(fakeFileSystem.exists(parent / "bug"))
    }

    @Test
    fun `updating the configuration rewrites project file and keeps the folder`() = runTest {
        val created = createWorkspace(parent, "World", ProjectColor.VIOLET).successValue()
        val renamed = created.config.copy(name = "World: Reforged", color = ProjectColor.GREEN)

        val updated = store.updateConfig(created.location, renamed).successValue()
        val reopened = openWorkspace(created.location).successValue()

        assertEquals(created.location, updated.location)
        assertEquals(renamed, reopened.config)
        assertEquals(listOf(parent / "World"), fakeFileSystem.list(parent))
        assertEquals(
            listOf(created.location / ".lore", created.location / "project.lore"),
            fakeFileSystem.list(created.location).sorted(),
        )
    }

    @Test
    fun `updating a folder without a project file reports not a workspace`() = runTest {
        fakeFileSystem.createDirectory(parent / "empty")

        val error = store.updateConfig(parent / "empty", config).failureError()

        assertIs<WorkspaceError.NotAWorkspace>(error)
        assertTrue(fakeFileSystem.list(parent / "empty").isEmpty())
    }

    @Test
    fun `failed update keeps the previous project file`() = runTest {
        val created = createWorkspace(parent, "World").successValue()
        failOn(Operation.ATOMIC_MOVE, "project.lore")

        val error = store.updateConfig(created.location, created.config.copy(name = "Other")).failureError()

        val failure = assertIs<WorkspaceError.FileSystemFailure>(error)
        assertEquals(FileSystemOperation.WRITE, failure.operation)
        assertEquals("World", openWorkspace(created.location).successValue().config.name)
        assertEquals(
            listOf(created.location / ".lore", created.location / "project.lore"),
            fakeFileSystem.list(created.location).sorted(),
        )
    }

    @Test
    fun `project file probe checks presence without parsing`() = runTest {
        val created = createWorkspace(parent, "World").successValue()
        val plainFolder = (parent / "plain").also { fakeFileSystem.createDirectory(it) }
        val damaged = (parent / "damaged").also { fakeFileSystem.createDirectory(it) }
        fakeFileSystem.write(damaged / "project.lore") { writeUtf8("not toml at all [") }

        assertTrue(store.hasProjectFile(created.location))
        assertTrue(store.hasProjectFile(damaged), "the probe must not parse project.lore")
        assertFalse(store.hasProjectFile(plainFolder))
        assertFalse(store.hasProjectFile(parent / "gone"))
        assertFalse(store.hasProjectFile(created.location / "project.lore"), "a file is not a workspace folder")
    }

    @Test
    fun `filesystem failures are logged once as errors with the original exception`() = runTest {
        failOn(Operation.CREATE_DIRECTORY, ".lore")

        val failure = assertIs<WorkspaceError.FileSystemFailure>(createWorkspace(parent, "World").failureError())

        val error = logs.at(Severity.Error).single()
        assertSame(failure.cause, error.throwable)
        assertTrue(".lore" in error.message && "CREATE_DIRECTORY" in error.message, error.message)
        assertTrue(logs.at(Severity.Info).isEmpty(), "a failed creation is not reported as created")
    }

    @Test
    fun `rollback leftovers are named in a warning without repeating the stack trace`() = runTest {
        fileSystem.beforeOperation = { operation, path ->
            if (operation == Operation.ATOMIC_MOVE && path.name == "project.lore") throw IOException("write failed")
            if (operation == Operation.DELETE && path.name == ".lore") throw IOException("cleanup failed")
        }

        createWorkspace(parent, "World").failureError()

        // The workspace folder cannot be removed either, because .lore is still inside it.
        val warnings = logs.at(Severity.Warn)
        assertEquals(
            listOf(parent / "World" / ".lore", parent / "World").map { "Could not remove $it " },
            warnings.map { it.message.substringBefore("after") },
        )
        assertTrue(warnings.all { it.throwable == null })
        assertEquals(1, logs.at(Severity.Error).size)
    }

    @Test
    fun `choosing a folder that is not a workspace is only diagnostic`() = runTest {
        openWorkspace(parent / "missing").failureError()

        assertTrue(logs.entries.all { it.severity == Severity.Debug }, "${logs.entries}")
        assertTrue(logs.entries.single().message.contains("NotAWorkspace"))
    }

    @Test
    fun `invalid project files are warned about without quoting their content`() = runTest {
        val location = parent / "Damaged"
        fakeFileSystem.createDirectory(location)
        fakeFileSystem.write(location / "project.lore") { writeUtf8("secret-lore = = \"The king dies\"") }

        openWorkspace(location).failureError()

        val warning = logs.at(Severity.Warn).single()
        assertTrue((location / "project.lore").toString() in warning.message)
        assertTrue(logs.entries.none { "secret-lore" in it.message || "king" in it.message }, "${logs.entries}")
    }

    @Test
    fun `creating and updating a workspace are lifecycle events`() = runTest {
        val workspace = createWorkspace(parent, "World").successValue()
        store.updateConfig(workspace.location, workspace.config.copy(name = "Renamed")).successValue()

        val info = logs.at(Severity.Info).map { it.message }
        assertEquals(2, info.size, "$info")
        assertTrue(info.all { id.toString() in it })
    }

    @Test
    fun `probe failures are explained in diagnostic output`() = runTest {
        val created = createWorkspace(parent, "World").successValue()
        val failingProbe = FileSystemWorkspaceStore(
            object : okio.ForwardingFileSystem(fakeFileSystem) {
                override fun metadataOrNull(path: Path) = throw IOException("permission denied")
            },
            logs.logger(),
        )

        assertFalse(failingProbe.hasProjectFile(created.location))

        val debug = logs.at(Severity.Debug).last()
        assertEquals("permission denied", debug.throwable?.message)
    }

    private fun failOn(failingOperation: Operation, name: String) {
        fileSystem.beforeOperation = { operation, path ->
            if (operation == failingOperation && path.name == name) throw IOException("simulated $operation failure")
        }
    }

    private fun WorkspaceResult<Workspace>.successValue(): Workspace =
        assertIs<WorkspaceResult.Success<Workspace>>(this).value

    private fun WorkspaceResult<Workspace>.failureError(): WorkspaceError =
        assertIs<WorkspaceResult.Failure>(this).error

    private fun WorkspaceError.path(): Path? = when (this) {
        is WorkspaceError.InvalidProjectFile -> path
        is WorkspaceError.UnsupportedProjectVersion -> path
        is WorkspaceError.InvalidWorkspaceId -> path
        is WorkspaceError.UnknownProjectColor -> path
        else -> null
    }
}
