package dev.cuervolu.loredesigner.workspace

import dev.cuervolu.loredesigner.core.workspace.ProjectColor

/**
 * Changes the display name and color stored in `project.lore`.
 *
 * Unlike [CreateWorkspace], the name is not a folder name here: renaming a project never renames its
 * directory, so only blank names and control characters are rejected.
 */
class UpdateProjectConfig(private val workspaceStore: WorkspaceStore) {
    suspend operator fun invoke(workspace: Workspace, name: String, color: ProjectColor?): WorkspaceResult<Workspace> {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.any { it.isISOControl() }) {
            return WorkspaceResult.Failure(WorkspaceError.InvalidWorkspaceName(name))
        }
        val updated = workspace.config.copy(name = trimmed, color = color)
        if (updated == workspace.config) return WorkspaceResult.Success(workspace)
        return workspaceStore.updateConfig(workspace.location, updated)
    }
}
