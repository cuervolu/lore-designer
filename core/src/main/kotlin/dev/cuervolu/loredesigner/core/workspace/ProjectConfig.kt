package dev.cuervolu.loredesigner.core.workspace

const val CURRENT_PROJECT_FORMAT_VERSION: Int = 1

data class ProjectConfig(val version: Int, val id: WorkspaceId, val name: String, val color: ProjectColor? = null)
