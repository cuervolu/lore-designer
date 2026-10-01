package dev.cuervolu.loredesigner.platform.workspace

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import dev.cuervolu.loredesigner.core.workspace.CURRENT_PROJECT_FORMAT_VERSION
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import dev.cuervolu.loredesigner.workspace.WorkspaceResult
import kotlinx.serialization.Serializable
import okio.Path
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

internal class ProjectFileCodec {
    private val toml =
        Toml(
            inputConfig = TomlInputConfig.compliant(ignoreUnknownNames = false),
            outputConfig = TomlOutputConfig.compliant(),
        )

    fun encode(config: ProjectConfig): String = toml.encodeToString(
        ProjectFile.serializer(),
        ProjectFile(
            version = config.version,
            id = config.id.toString(),
            name = config.name,
            color = config.color?.serializedName,
        ),
    )

    fun decode(path: Path, content: String): WorkspaceResult<ProjectConfig> {
        val projectFile =
            try {
                toml.decodeFromString(ProjectFile.serializer(), content)
            } catch (exception: Exception) {
                return WorkspaceResult.Failure(
                    WorkspaceError.InvalidProjectFile(path, exception.message ?: "Invalid TOML"),
                )
            }

        if (projectFile.version != CURRENT_PROJECT_FORMAT_VERSION) {
            return WorkspaceResult.Failure(
                WorkspaceError.UnsupportedProjectVersion(path, projectFile.version),
            )
        }
        if (projectFile.name.isBlank()) {
            return WorkspaceResult.Failure(
                WorkspaceError.InvalidProjectFile(path, "Project name must not be blank"),
            )
        }

        val workspaceId =
            try {
                WorkspaceId.parse(projectFile.id)
            } catch (_: IllegalArgumentException) {
                return WorkspaceResult.Failure(
                    WorkspaceError.InvalidWorkspaceId(path, projectFile.id),
                )
            }
        if (!workspaceId.isVersion7()) {
            return WorkspaceResult.Failure(
                WorkspaceError.InvalidWorkspaceId(path, projectFile.id),
            )
        }

        val color =
            when (val serialized = projectFile.color) {
                null -> null

                else ->
                    ProjectColor.entries.firstOrNull { it.serializedName == serialized }
                        ?: return WorkspaceResult.Failure(WorkspaceError.UnknownProjectColor(path, serialized))
            }

        return WorkspaceResult.Success(
            ProjectConfig(
                version = projectFile.version,
                id = workspaceId,
                name = projectFile.name,
                color = color,
            ),
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun WorkspaceId.isVersion7(): Boolean =
        value.toJavaUuid().let { uuid -> uuid.version() == 7 && uuid.variant() == 2 }

    // Persisted in project.lore; these names must never change once released.
    private val ProjectColor.serializedName: String
        get() =
            when (this) {
                ProjectColor.VIOLET -> "violet"
                ProjectColor.BLUE -> "blue"
                ProjectColor.CYAN -> "cyan"
                ProjectColor.TEAL -> "teal"
                ProjectColor.GREEN -> "green"
                ProjectColor.AMBER -> "amber"
                ProjectColor.ORANGE -> "orange"
                ProjectColor.ROSE -> "rose"
                ProjectColor.RED -> "red"
                ProjectColor.SLATE -> "slate"
            }

    @Serializable
    private data class ProjectFile(val version: Int, val id: String, val name: String, val color: String? = null)
}
