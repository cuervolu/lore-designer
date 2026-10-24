package dev.cuervolu.loredesigner.workspace

import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspacesRegistry
import kotlinx.coroutines.CancellationException

/**
 * Changes the display name and color stored in `project.lore`.
 *
 * Unlike [CreateWorkspace], the name is not a folder name here: renaming a project never renames its
 * directory, so only blank names and control characters are rejected.
 *
 * The launcher lists a project from the name and color cached in [recentWorkspaces], so a successful change
 * refreshes that cache. It is only a cache: failing to refresh it is logged and never fails the change.
 */
class UpdateProjectConfig(
    private val workspaceStore: WorkspaceStore,
    private val recentWorkspaces: RecentWorkspacesRegistry,
    private val logger: Logger,
) {
    suspend operator fun invoke(workspace: Workspace, name: String, color: ProjectColor?): WorkspaceResult<Workspace> {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.any { it.isISOControl() }) {
            return WorkspaceResult.Failure(WorkspaceError.InvalidWorkspaceName(name))
        }
        val updated = workspace.config.copy(name = trimmed, color = color)
        if (updated == workspace.config) return WorkspaceResult.Success(workspace)
        val result = workspaceStore.updateConfig(workspace.location, updated)
        if (result is WorkspaceResult.Success) refreshRecent(result.value)
        return result
    }

    private suspend fun refreshRecent(workspace: Workspace) {
        try {
            recentWorkspaces.updateLocation(workspace)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            logger.w(exception) {
                "Could not refresh recent workspace ${workspace.config.id}; the launcher may show its old name"
            }
        }
    }
}
