package dev.cuervolu.loredesigner.workspace

import co.touchlab.kermit.Severity
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspace
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspacesRegistry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import okio.Path
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WorkspaceOpenerTest {
    private val remembered = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb380")
    private val stranger = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb381")
    private val logs = RecordingLogWriter()
    private val store = FolderWorkspaceStore()
    private val registry = RecordingRegistry()
    private val opener = WorkspaceOpener(store, registry, logs.logger())

    @Test
    fun `opening without an expected id accepts whatever workspace the folder holds`() = runTest {
        store.folders["/worlds/Embercourt".toPath()] = ProjectConfig(1, stranger, "Someone")

        val result = opener.open("/worlds/Embercourt".toPath())

        assertEquals(stranger, assertIs<WorkspaceResult.Success<Workspace>>(result).value.config.id)
        assertTrue(registry.recorded.isEmpty(), "opening alone does not record")
    }

    @Test
    fun `opening a remembered workspace refuses a folder holding another one`() = runTest {
        store.folders["/worlds/Embercourt".toPath()] = ProjectConfig(1, stranger, "Replacement")

        val result = opener.open("/worlds/Embercourt".toPath(), expectedId = remembered)

        val error = assertIs<WorkspaceError.DifferentWorkspace>(assertIs<WorkspaceResult.Failure>(result).error)
        assertEquals(remembered, error.expected)
        assertEquals("Replacement", error.found.name)
        assertEquals("/worlds/Embercourt".toPath(), error.location)
    }

    @Test
    fun `relocating to the same workspace updates its location without recording an open`() = runTest {
        store.folders["/moved/Embercourt".toPath()] = ProjectConfig(1, remembered, "Embercourt")

        val result = opener.relocate(remembered, "/moved/Embercourt".toPath())

        val moved = assertIs<WorkspaceResult.Success<Workspace>>(result).value
        assertEquals(listOf(moved), registry.relocated)
        assertTrue(registry.recorded.isEmpty())
    }

    @Test
    fun `relocating to another workspace or a plain folder leaves the registry alone`() = runTest {
        store.folders["/elsewhere".toPath()] = ProjectConfig(1, stranger, "Someone else")

        assertIs<WorkspaceError.DifferentWorkspace>(
            assertIs<WorkspaceResult.Failure>(opener.relocate(remembered, "/elsewhere".toPath())).error,
        )
        assertEquals(
            WorkspaceError.NotAWorkspace("/empty".toPath()),
            assertIs<WorkspaceResult.Failure>(opener.relocate(remembered, "/empty".toPath())).error,
        )
        assertTrue(registry.relocated.isEmpty())
    }

    @Test
    fun `registry failures are logged as warnings but cancellation is not swallowed`() = runTest {
        val workspace = Workspace("/worlds/Embercourt".toPath(), ProjectConfig(1, remembered, "Embercourt"))
        store.folders[workspace.location] = workspace.config

        val failure = IllegalStateException("disk full")
        registry.failure = failure
        opener.recordOpened(workspace)
        assertIs<WorkspaceResult.Success<Workspace>>(opener.relocate(remembered, workspace.location))

        val warnings = logs.at(Severity.Warn)
        assertEquals(2, warnings.size)
        assertTrue(warnings.all { it.throwable === failure && remembered.toString() in it.message })

        registry.failure = CancellationException("closing")
        assertFailsWith<CancellationException> { opener.recordOpened(workspace) }
    }

    @Test
    fun `opening and relocating are recorded as lifecycle events`() = runTest {
        val workspace = Workspace("/worlds/Embercourt".toPath(), ProjectConfig(1, remembered, "Embercourt"))
        store.folders[workspace.location] = workspace.config

        opener.recordOpened(workspace)
        opener.relocate(remembered, workspace.location)
        opener.open("/elsewhere".toPath(), expectedId = remembered)

        val info = logs.at(Severity.Info).map { it.message }
        assertEquals(2, info.size)
        assertTrue(info.all { remembered.toString() in it && "/worlds/Embercourt" in it })
    }

    @Test
    fun `availability only asks whether the project file exists`() = runTest {
        store.folders["/worlds/Embercourt".toPath()] = ProjectConfig(1, remembered, "Embercourt")

        assertTrue(opener.hasProjectFile("/worlds/Embercourt".toPath()))
        assertEquals(false, opener.hasProjectFile("/worlds/Gone".toPath()))
        assertTrue(store.opened.isEmpty())
    }

    private class FolderWorkspaceStore : WorkspaceStore {
        val folders = mutableMapOf<Path, ProjectConfig>()
        val opened = mutableListOf<Path>()

        override suspend fun create(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> =
            error("not used")

        override suspend fun open(location: Path): WorkspaceResult<Workspace> {
            opened += location
            val config = folders[location] ?: return WorkspaceResult.Failure(WorkspaceError.NotAWorkspace(location))
            return WorkspaceResult.Success(Workspace(location, config))
        }

        override suspend fun updateConfig(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> =
            error("not used")

        override suspend fun hasProjectFile(location: Path): Boolean = location in folders
    }

    private class RecordingRegistry : RecentWorkspacesRegistry {
        val recorded = mutableListOf<Workspace>()
        val relocated = mutableListOf<Workspace>()
        var failure: Exception? = null
        override val workspaces: StateFlow<List<RecentWorkspace>> = MutableStateFlow(emptyList())
        override val loaded: StateFlow<Boolean> = MutableStateFlow(true)

        override suspend fun initialize() = Unit

        override suspend fun recordOpened(workspace: Workspace) {
            failure?.let { throw it }
            recorded += workspace
        }

        override suspend fun updateLocation(workspace: Workspace) {
            failure?.let { throw it }
            relocated += workspace
        }

        override suspend fun setPinned(id: WorkspaceId, pinned: Boolean) = Unit

        override suspend fun forget(id: WorkspaceId) = Unit
    }
}
