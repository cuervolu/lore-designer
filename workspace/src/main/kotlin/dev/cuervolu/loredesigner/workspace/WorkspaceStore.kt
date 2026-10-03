package dev.cuervolu.loredesigner.workspace

import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import okio.Path

/**
 * Filesystem access to workspaces. Implementations resolve and normalize every location they are given,
 * and the [Workspace.location]s they return are absolute and normalized.
 */
interface WorkspaceStore {
    suspend fun create(location: Path, config: ProjectConfig): WorkspaceResult<Workspace>

    suspend fun open(location: Path): WorkspaceResult<Workspace>

    /** Rewrites the project file of the workspace at [location]; the folder itself is never moved or renamed. */
    suspend fun updateConfig(location: Path, config: ProjectConfig): WorkspaceResult<Workspace>

    /**
     * Whether [location] is a folder holding a `project.lore` file. The file
     * is not read, so a `true` answer does not mean [open] will succeed.
     */
    suspend fun hasProjectFile(location: Path): Boolean
}

fun interface WorkspaceIdGenerator {
    fun generate(): WorkspaceId
}
