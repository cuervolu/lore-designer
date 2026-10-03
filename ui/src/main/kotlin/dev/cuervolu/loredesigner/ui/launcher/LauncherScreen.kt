package dev.cuervolu.loredesigner.ui.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.launcher_browse_title_locate
import dev.cuervolu.loredesigner.ui.resources.launcher_browse_title_new
import dev.cuervolu.loredesigner.ui.resources.launcher_browse_title_open
import dev.cuervolu.loredesigner.workspace.Workspace
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import kotlinx.coroutines.launch
import okio.Path
import okio.Path.Companion.toOkioPath
import org.jetbrains.compose.resources.stringResource

/** Picks a directory with the platform's native dialog; `null` when the user cancels. */
typealias DirectoryPicker = suspend (title: String) -> Path?

internal val FileKitDirectoryPicker: DirectoryPicker = { title ->
    FileKit.openDirectoryPicker(dialogSettings = FileKitDialogSettings(title = title))?.file?.toOkioPath()
}

@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel,
    onWorkspaceOpened: (Workspace) -> Unit,
    modifier: Modifier = Modifier,
    pickDirectory: DirectoryPicker = FileKitDirectoryPicker,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var section by rememberSaveable { mutableStateOf(LauncherSection.Projects) }
    val scope = rememberCoroutineScope()
    val currentOnWorkspaceOpened by rememberUpdatedState(onWorkspaceOpened)

    LaunchedEffect(state.openedWorkspace) {
        val workspace = state.openedWorkspace ?: return@LaunchedEffect
        currentOnWorkspaceOpened(workspace)
        viewModel.onWorkspaceHandled()
    }
    // The view model outlives the screen while a workspace is open, so folders are re-checked each time
    // the launcher is shown again.
    LaunchedEffect(viewModel) { viewModel.refreshAvailability() }

    val newProjectBrowseTitle = stringResource(Res.string.launcher_browse_title_new)
    val openProjectBrowseTitle = stringResource(Res.string.launcher_browse_title_open)
    val locateBrowseTitle = stringResource(Res.string.launcher_browse_title_locate)
    fun browse(title: String) {
        scope.launch { pickDirectory(title)?.let(viewModel::onDirectoryChosen) }
    }
    val currentLocateTitle by rememberUpdatedState(locateBrowseTitle)
    val projectActions = remember(viewModel, scope, pickDirectory) {
        LauncherProjectActions(
            onOpen = viewModel::openProject,
            onPinnedChange = viewModel::setPinned,
            onForget = viewModel::forgetProject,
            onLocate = { id ->
                scope.launch { pickDirectory(currentLocateTitle)?.let { viewModel.relocateProject(id, it) } }
            },
        )
    }

    LauncherContent(
        section = section,
        onSectionChange = { section = it },
        onNewProject = viewModel::showNewProject,
        onOpenProject = viewModel::showOpenProject,
        projects = state.projects,
        projectsLoaded = state.projectsLoaded,
        projectActions = projectActions,
        error = state.error.takeIf { state.dialog == null },
        onDismissError = viewModel::dismissError,
        modifier = modifier,
    )
    NewProjectDialog(
        dialog = state.dialog as? LauncherDialog.NewProject,
        busy = state.busy,
        error = state.error,
        onColorChange = viewModel::onColorChange,
        onBrowse = { browse(newProjectBrowseTitle) },
        onSubmit = viewModel::submit,
        onDismiss = viewModel::dismissDialog,
    )
    OpenProjectDialog(
        dialog = state.dialog as? LauncherDialog.OpenProject,
        busy = state.busy,
        error = state.error,
        onBrowse = { browse(openProjectBrowseTitle) },
        onSubmit = viewModel::submit,
        onDismiss = viewModel::dismissDialog,
    )
}
