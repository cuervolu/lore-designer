package dev.cuervolu.loredesigner.workspace

import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspacesRegistry
import kotlinx.coroutines.CancellationException
import okio.Path

/**
 * Opens workspaces and keeps [recentWorkspaces] up to date.
 *
 * `project.lore` stays authoritative: the registry only remembers where a workspace was last seen, so
 * opening a remembered workspace still validates the folder and refuses one holding a different id.
 * Recent state is a local cache, so failing to record it is logged and never fails an open.
 */
class WorkspaceOpener(
    private val workspaceStore: WorkspaceStore,
    private val recentWorkspaces: RecentWorkspacesRegistry,
    private val logger: Logger = Logger.withTag("Workspaces"),
) {
    /**
     * Opens the workspace at [location]. With [expectedId], a folder holding any other workspace fails with
     * [WorkspaceError.DifferentWorkspace]. Does not record the open; call [recordOpened] once it is shown.
     */
    suspend fun open(location: Path, expectedId: WorkspaceId? = null): WorkspaceResult<Workspace> {
        val result = workspaceStore.open(location)
        if (expectedId == null || result !is WorkspaceResult.Success) return result
        val found = result.value
        return if (found.config.id == expectedId) {
            result
        } else {
            WorkspaceResult.Failure(WorkspaceError.DifferentWorkspace(found.location, expectedId, found.config))
        }
    }

    /** Points the remembered workspace [id] at [location], provided the folder holds that same workspace. */
    suspend fun relocate(id: WorkspaceId, location: Path): WorkspaceResult<Workspace> {
        val result = open(location, expectedId = id)
        if (result is WorkspaceResult.Success) {
            bestEffort("update the location of") { recentWorkspaces.updateLocation(result.value) }
        }
        return result
    }

    suspend fun recordOpened(workspace: Workspace) {
        bestEffort("remember") { recentWorkspaces.recordOpened(workspace) }
    }

    /** Cheap availability probe; see [WorkspaceStore.hasProjectFile]. */
    suspend fun hasProjectFile(location: Path): Boolean = workspaceStore.hasProjectFile(location)

    private suspend fun bestEffort(action: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            logger.w(exception) { "Could not $action a recent workspace; the launcher list may be out of date" }
        }
    }
}
