package dev.cuervolu.loredesigner.workspace

import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import okio.Path

interface WorkspaceStore {
    suspend fun create(location: Path, config: ProjectConfig): WorkspaceResult<Workspace>

    suspend fun open(location: Path): WorkspaceResult<Workspace>
}

fun interface WorkspaceIdGenerator {
    fun generate(): WorkspaceId
}
