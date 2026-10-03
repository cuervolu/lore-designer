package dev.cuervolu.loredesigner.platform.workspace

import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.platform.logging.RecordingLogWriter
import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.OpenWorkspace
import dev.cuervolu.loredesigner.workspace.Workspace
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import dev.cuervolu.loredesigner.workspace.WorkspaceIdGenerator
import dev.cuervolu.loredesigner.workspace.WorkspaceResult
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Covers behavior that depends on the real OS filesystem: atomic moves and native path semantics. */
class FileSystemWorkspaceStoreSystemTest {
    private val id = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb380")
    private val fileSystem = FileSystem.SYSTEM
    private val store = FileSystemWorkspaceStore(fileSystem, RecordingLogWriter().logger())

    @Test
    fun `workspace created on disk reopens with its configuration and no temporary files`() =
        runTestWithTempDirectory { parent ->
            val location = parent / "My World"

            val created = CreateWorkspace(store, WorkspaceIdGenerator { id })(parent, "My World", ProjectColor.BLUE)
            val opened = OpenWorkspace(store)(location)

            assertEquals(
                assertIs<WorkspaceResult.Success<Workspace>>(created).value,
                assertIs<WorkspaceResult.Success<Workspace>>(opened).value,
            )
            assertEquals(ProjectConfig(1, id, "My World", ProjectColor.BLUE), opened.value.config)
            assertEquals(listOf(location / ".lore", location / "project.lore"), fileSystem.list(location).sorted())
            assertEquals(listOf(location / ".lore" / "types.json"), fileSystem.list(location / ".lore"))
        }

    @Test
    fun `creating below a regular file is a filesystem failure with context`() = runTestWithTempDirectory { parent ->
        val regularFile = parent / "regular-file"
        fileSystem.write(regularFile) { writeUtf8("content") }

        val result = CreateWorkspace(store, WorkspaceIdGenerator { id })(regularFile, "World")

        val error = assertIs<WorkspaceError.FileSystemFailure>(assertIs<WorkspaceResult.Failure>(result).error)
        assertEquals(regularFile / "World", error.path)
    }

    private val WorkspaceResult<Workspace>.value: Workspace
        get() = assertIs<WorkspaceResult.Success<Workspace>>(this).value

    private fun runTestWithTempDirectory(block: suspend (Path) -> Unit) = runTest {
        val directory =
            FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "workspace-store-test-${Random.nextLong().toULong().toString(16)}"
        fileSystem.createDirectory(directory, mustCreate = true)
        try {
            block(fileSystem.canonicalize(directory))
        } finally {
            fileSystem.deleteRecursively(directory)
        }
    }
}
