package dev.cuervolu.loredesigner.workspace

import dev.cuervolu.loredesigner.core.workspace.CURRENT_PROJECT_FORMAT_VERSION
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import kotlinx.coroutines.test.runTest
import okio.Path
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class WorkspaceUseCasesTest {
    private val workspaceId = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb380")

    @Test
    fun `create builds current project configuration with generated id`() = runTest {
        val store = RecordingWorkspaceStore()
        val createWorkspace = CreateWorkspace(store, WorkspaceIdGenerator { workspaceId })
        val parent = "worlds".toPath()

        val result = createWorkspace(parent, "My World", ProjectColor.VIOLET)

        val workspace = assertIs<WorkspaceResult.Success<Workspace>>(result).value
        assertEquals("worlds/My World".toPath(), workspace.location)
        assertEquals(
            ProjectConfig(
                version = CURRENT_PROJECT_FORMAT_VERSION,
                id = workspaceId,
                name = "My World",
                color = ProjectColor.VIOLET,
            ),
            workspace.config,
        )
    }

    @Test
    fun `create trims the name used for the folder and the project`() = runTest {
        val createWorkspace = CreateWorkspace(RecordingWorkspaceStore(), WorkspaceIdGenerator { workspaceId })

        val result = createWorkspace("folder/../worlds".toPath(), "  Embercourt  ")

        val workspace = assertIs<WorkspaceResult.Success<Workspace>>(result).value
        assertEquals("worlds/Embercourt".toPath(), workspace.location)
        assertEquals("Embercourt", workspace.config.name)
    }

    @Test
    fun `create rejects names that are not portable folder names without accessing storage`() = runTest {
        val invalidNames =
            listOf(
                "a/b",
                "a\\b",
                "..",
                ".",
                "a:b",
                "what?",
                "trailing.",
                "CON",
                "nul.txt",
                "Com1",
                "lpt9.md",
                "tab\tname",
            )
        val createWorkspace = CreateWorkspace(FailingWorkspaceStore(), WorkspaceIdGenerator { workspaceId })

        for (name in invalidNames) {
            val result = createWorkspace("worlds".toPath(), name)

            val error = assertIs<WorkspaceError.InvalidWorkspaceName>(assertIs<WorkspaceResult.Failure>(result).error)
            assertEquals(name, error.name)
        }
    }

    @Test
    fun `create accepts names that only resemble reserved device names`() = runTest {
        val createWorkspace = CreateWorkspace(RecordingWorkspaceStore(), WorkspaceIdGenerator { workspaceId })

        for (name in listOf("Console", "COM10", "Aux Realm", "Nullspace")) {
            assertIs<WorkspaceResult.Success<Workspace>>(createWorkspace("worlds".toPath(), name))
        }
    }

    @Test
    fun `create rejects blank name before generating an id or accessing storage`() = runTest {
        var generated = false
        val createWorkspace =
            CreateWorkspace(
                FailingWorkspaceStore(),
                WorkspaceIdGenerator {
                    generated = true
                    workspaceId
                },
            )

        val result = createWorkspace("world".toPath(), "  ")

        assertIs<WorkspaceError.InvalidWorkspaceName>(assertIs<WorkspaceResult.Failure>(result).error)
        assertEquals(false, generated)
    }

    @Test
    fun `open normalizes the workspace location`() = runTest {
        val store = RecordingWorkspaceStore()

        OpenWorkspace(store)("folder/../world".toPath())

        assertEquals("world".toPath(), store.openedLocation)
    }

    @Test
    fun `update rewrites name and color without changing the location`() = runTest {
        val store = RecordingWorkspaceStore()
        val workspace =
            Workspace("worlds/Embercourt".toPath(), config(name = "Embercourt", color = ProjectColor.VIOLET))

        val result = UpdateProjectConfig(store)(workspace, "  Ember Court  ", ProjectColor.GREEN)

        val updated = assertIs<WorkspaceResult.Success<Workspace>>(result).value
        assertEquals("worlds/Embercourt".toPath(), updated.location)
        assertEquals(config(name = "Ember Court", color = ProjectColor.GREEN), updated.config)
        assertEquals(listOf("worlds/Embercourt".toPath() to updated.config), store.updates)
    }

    @Test
    fun `update accepts names that are not valid folder names`() = runTest {
        val workspace = Workspace("worlds/Embercourt".toPath(), config(name = "Embercourt"))

        val result = UpdateProjectConfig(RecordingWorkspaceStore())(workspace, "Ember: Court?", null)

        assertEquals("Ember: Court?", assertIs<WorkspaceResult.Success<Workspace>>(result).value.config.name)
    }

    @Test
    fun `update rejects blank names without accessing storage`() = runTest {
        val workspace = Workspace("worlds/Embercourt".toPath(), config(name = "Embercourt"))

        for (name in listOf("", "   ", "tab\tname")) {
            val result = UpdateProjectConfig(FailingWorkspaceStore())(workspace, name, null)

            assertIs<WorkspaceError.InvalidWorkspaceName>(assertIs<WorkspaceResult.Failure>(result).error)
        }
    }

    @Test
    fun `update without changes does not touch storage`() = runTest {
        val workspace = Workspace("worlds/Embercourt".toPath(), config(name = "Embercourt", color = ProjectColor.BLUE))

        val result = UpdateProjectConfig(FailingWorkspaceStore())(workspace, "Embercourt", ProjectColor.BLUE)

        assertEquals(workspace, assertIs<WorkspaceResult.Success<Workspace>>(result).value)
    }

    private fun config(name: String, color: ProjectColor? = null) =
        ProjectConfig(version = CURRENT_PROJECT_FORMAT_VERSION, id = workspaceId, name = name, color = color)

    private class RecordingWorkspaceStore : WorkspaceStore {
        var openedLocation: Path? = null
        val updates = mutableListOf<Pair<Path, ProjectConfig>>()

        override suspend fun updateConfig(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> {
            updates += location to config
            return WorkspaceResult.Success(Workspace(location, config))
        }

        override suspend fun create(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> =
            WorkspaceResult.Success(Workspace(location, config))

        override suspend fun open(location: Path): WorkspaceResult<Workspace> {
            openedLocation = location
            return WorkspaceResult.Failure(WorkspaceError.NotAWorkspace(location))
        }
    }

    private class FailingWorkspaceStore : WorkspaceStore {
        override suspend fun create(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> =
            error("storage must not be called")

        override suspend fun open(location: Path): WorkspaceResult<Workspace> = error("storage must not be called")

        override suspend fun updateConfig(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> =
            error("storage must not be called")
    }
}
