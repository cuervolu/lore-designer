package dev.cuervolu.loredesigner.workspace

import dev.cuervolu.loredesigner.core.workspace.CURRENT_PROJECT_FORMAT_VERSION
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import okio.Path

/**
 * Creates a new workspace in a folder named after the project inside [parentDirectory].
 *
 * The name doubles as the folder name, so it must be a single path segment that is valid on every
 * supported platform: projects are expected to move between machines.
 */
class CreateWorkspace(
    private val workspaceStore: WorkspaceStore,
    private val workspaceIdGenerator: WorkspaceIdGenerator,
) {
    suspend operator fun invoke(
        parentDirectory: Path,
        name: String,
        color: ProjectColor? = null,
    ): WorkspaceResult<Workspace> {
        val folderName = name.trim()
        if (!isPortableFolderName(folderName)) {
            return WorkspaceResult.Failure(WorkspaceError.InvalidWorkspaceName(name))
        }
        val location = parentDirectory.normalized() / folderName

        val config =
            ProjectConfig(
                version = CURRENT_PROJECT_FORMAT_VERSION,
                id = workspaceIdGenerator.generate(),
                name = folderName,
                color = color,
            )
        return workspaceStore.create(location, config)
    }
}

// Windows is the most restrictive of the supported platforms, so its rules define portability.
private const val FORBIDDEN_CHARACTERS = "/\\:*?\"<>|"

private val RESERVED_DEVICE_NAMES: Set<String> =
    setOf("CON", "PRN", "AUX", "NUL") +
        (1..9).map { "COM$it" } +
        (1..9).map { "LPT$it" }

internal fun isPortableFolderName(name: String): Boolean {
    if (name.isEmpty() || name == "." || name == "..") return false
    if (name.any { it in FORBIDDEN_CHARACTERS || it.isISOControl() }) return false
    if (name.endsWith('.') || name.endsWith(' ')) return false
    // Windows reserves device names even when followed by an extension ("nul.txt").
    val stem = name.substringBefore('.').trimEnd()
    return stem.uppercase() !in RESERVED_DEVICE_NAMES
}
