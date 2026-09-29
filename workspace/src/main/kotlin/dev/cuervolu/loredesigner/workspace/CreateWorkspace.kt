package dev.cuervolu.loredesigner.workspace

import dev.cuervolu.loredesigner.core.workspace.CURRENT_PROJECT_FORMAT_VERSION
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import java.nio.file.Path

class CreateWorkspace(
    private val workspaceStore: WorkspaceStore,
    private val workspaceIdGenerator: WorkspaceIdGenerator,
) {
    suspend operator fun invoke(
        location: Path,
        name: String,
        color: ProjectColor? = null,
    ): WorkspaceResult<Workspace> {
        val normalizedLocation = location.toAbsolutePath().normalize()
        if (name.isBlank()) {
            return WorkspaceResult.Failure(WorkspaceError.InvalidWorkspaceName(name))
        }

        val config =
            ProjectConfig(
                version = CURRENT_PROJECT_FORMAT_VERSION,
                id = workspaceIdGenerator.generate(),
                name = name,
                color = color,
            )
        return workspaceStore.create(normalizedLocation, config)
    }
}
