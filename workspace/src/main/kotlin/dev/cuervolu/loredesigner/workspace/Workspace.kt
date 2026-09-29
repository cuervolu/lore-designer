package dev.cuervolu.loredesigner.workspace

import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import java.nio.file.Path

data class Workspace(val location: Path, val config: ProjectConfig)
