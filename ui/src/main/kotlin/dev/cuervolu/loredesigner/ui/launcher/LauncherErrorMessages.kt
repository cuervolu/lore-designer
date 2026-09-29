package dev.cuervolu.loredesigner.ui.launcher

import androidx.compose.runtime.Composable
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.launcher_error_damaged_project
import dev.cuervolu.loredesigner.ui.resources.launcher_error_destination_not_empty
import dev.cuervolu.loredesigner.ui.resources.launcher_error_invalid_location
import dev.cuervolu.loredesigner.ui.resources.launcher_error_invalid_name
import dev.cuervolu.loredesigner.ui.resources.launcher_error_missing_location
import dev.cuervolu.loredesigner.ui.resources.launcher_error_not_a_folder
import dev.cuervolu.loredesigner.ui.resources.launcher_error_not_a_workspace
import dev.cuervolu.loredesigner.ui.resources.launcher_error_read_failed
import dev.cuervolu.loredesigner.ui.resources.launcher_error_unsupported_version
import dev.cuervolu.loredesigner.ui.resources.launcher_error_write_failed
import dev.cuervolu.loredesigner.workspace.FileSystemOperation
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import java.nio.file.Path

/** A launcher message ready to resolve for the current locale. */
internal data class LauncherMessage(val resource: StringResource, val args: List<Any> = emptyList())

internal fun LauncherError.toMessage(): LauncherMessage = when (this) {
    LauncherError.MissingLocation -> LauncherMessage(Res.string.launcher_error_missing_location)
    is LauncherError.InvalidLocation -> LauncherMessage(Res.string.launcher_error_invalid_location, listOf(input))
    is LauncherError.Workspace -> error.toMessage()
}

private fun WorkspaceError.toMessage(): LauncherMessage = when (this) {
    is WorkspaceError.NotAWorkspace ->
        LauncherMessage(Res.string.launcher_error_not_a_workspace, listOf(location.displayName()))

    is WorkspaceError.InvalidWorkspaceLocation ->
        LauncherMessage(Res.string.launcher_error_not_a_folder, listOf(location.toString()))

    is WorkspaceError.DestinationNotEmpty ->
        LauncherMessage(Res.string.launcher_error_destination_not_empty, listOf(location.displayName()))

    is WorkspaceError.InvalidWorkspaceName -> LauncherMessage(Res.string.launcher_error_invalid_name)

    is WorkspaceError.InvalidProjectFile,
    is WorkspaceError.InvalidWorkspaceId,
    is WorkspaceError.UnknownProjectColor,
    -> LauncherMessage(Res.string.launcher_error_damaged_project)

    is WorkspaceError.UnsupportedProjectVersion ->
        LauncherMessage(Res.string.launcher_error_unsupported_version, listOf(version))

    is WorkspaceError.FileSystemFailure -> when (operation) {
        FileSystemOperation.INSPECT,
        FileSystemOperation.READ,
        -> LauncherMessage(Res.string.launcher_error_read_failed, listOf(path.toString()))

        FileSystemOperation.CREATE_DIRECTORY,
        FileSystemOperation.WRITE,
        -> LauncherMessage(Res.string.launcher_error_write_failed, listOf(path.toString()))
    }
}

private fun Path.displayName(): String = fileName?.toString() ?: toString()

@Composable
internal fun LauncherError.localizedMessage(): String {
    val message = toMessage()
    return stringResource(message.resource, *message.args.toTypedArray())
}
