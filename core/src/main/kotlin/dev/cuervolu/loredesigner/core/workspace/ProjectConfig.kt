package dev.cuervolu.loredesigner.core.workspace

data class ProjectConfig(
    val version: Int,
    val id: WorkspaceId,
    val name: String,
    val color: ProjectColor? = null,
)
