package dev.cuervolu.loredesigner.workspace

import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import okio.Path

sealed interface WorkspaceResult<out T> {
    data class Success<T>(val value: T) : WorkspaceResult<T>

    data class Failure(val error: WorkspaceError) : WorkspaceResult<Nothing>
}

sealed interface WorkspaceError {
    data class NotAWorkspace(val location: Path) : WorkspaceError

    data class InvalidWorkspaceLocation(val location: Path) : WorkspaceError

    data class DestinationNotEmpty(val location: Path) : WorkspaceError

    data class InvalidWorkspaceName(val name: String) : WorkspaceError

    data class InvalidProjectFile(val path: Path, val reason: String) : WorkspaceError

    data class UnsupportedProjectVersion(val path: Path, val version: Int) : WorkspaceError

    data class InvalidWorkspaceId(val path: Path, val value: String) : WorkspaceError

    data class UnknownProjectColor(val path: Path, val value: String) : WorkspaceError

    /** [location] holds a valid workspace, but not the [expected] one. */
    data class DifferentWorkspace(val location: Path, val expected: WorkspaceId, val found: ProjectConfig) :
        WorkspaceError

    data class FileSystemFailure(val path: Path, val operation: FileSystemOperation, val cause: Exception) :
        WorkspaceError
}

enum class FileSystemOperation {
    INSPECT,
    CREATE_DIRECTORY,
    READ,
    WRITE,
}
