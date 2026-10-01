package dev.cuervolu.loredesigner.workspace

import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import okio.Path

data class Workspace(val location: Path, val config: ProjectConfig)
