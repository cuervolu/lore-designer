package dev.cuervolu.loredesigner.ui.launcher

import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.ui.RecordingLogWriter
import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.UpdateProjectConfig
import dev.cuervolu.loredesigner.workspace.Workspace
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import dev.cuervolu.loredesigner.workspace.WorkspaceIdGenerator
import dev.cuervolu.loredesigner.workspace.WorkspaceOpener
import dev.cuervolu.loredesigner.workspace.WorkspaceResult
import dev.cuervolu.loredesigner.workspace.WorkspaceStore
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspacesRegistry
import kotlinx.coroutines.CompletableDeferred
import okio.Path

internal val TestWorkspaceId: WorkspaceId = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb380")

/** Succeeds by default; set [createError]/[openError]/[updateError] to fail, or [gate] to hold operations open. */
internal class FakeWorkspaceStore : WorkspaceStore {
    var createError: WorkspaceError? = null
    var openError: WorkspaceError? = null
    var openedName: String = "Embercourt"
    var openedId: WorkspaceId = TestWorkspaceId

    /** Locations [hasProjectFile] reports as gone; every other location looks like a workspace. */
    val missingLocations = mutableSetOf<Path>()
    var gate: CompletableDeferred<Unit>? = null

    val createdLocations = mutableListOf<Path>()
    val openedLocations = mutableListOf<Path>()
    val createdConfigs = mutableListOf<ProjectConfig>()
    val updatedConfigs = mutableListOf<ProjectConfig>()
    var updateError: WorkspaceError? = null

    override suspend fun create(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> {
        createdLocations.add(location)
        createdConfigs.add(config)
        gate?.await()
        return createError?.let { WorkspaceResult.Failure(it) } ?: WorkspaceResult.Success(Workspace(location, config))
    }

    override suspend fun open(location: Path): WorkspaceResult<Workspace> {
        openedLocations.add(location)
        gate?.await()
        return openError?.let { WorkspaceResult.Failure(it) }
            ?: WorkspaceResult.Success(Workspace(location, ProjectConfig(1, openedId, openedName)))
    }

    override suspend fun updateConfig(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> {
        updatedConfigs.add(config)
        gate?.await()
        return updateError?.let { WorkspaceResult.Failure(it) } ?: WorkspaceResult.Success(Workspace(location, config))
    }

    override suspend fun hasProjectFile(location: Path): Boolean = location !in missingLocations

    fun createWorkspace() = CreateWorkspace(this, WorkspaceIdGenerator { TestWorkspaceId })

    fun updateProjectConfig() = UpdateProjectConfig(this)

    fun opener(registry: RecentWorkspacesRegistry) = WorkspaceOpener(this, registry, RecordingLogWriter().logger())
}
