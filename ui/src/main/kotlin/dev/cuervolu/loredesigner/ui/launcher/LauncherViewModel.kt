package dev.cuervolu.loredesigner.ui.launcher

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.Workspace
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import dev.cuervolu.loredesigner.workspace.WorkspaceOpener
import dev.cuervolu.loredesigner.workspace.WorkspaceResult
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspace
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspacesRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.Path
import okio.Path.Companion.toPath

data class LauncherUiState(
    val dialog: LauncherDialog? = null,
    val busy: Boolean = false,
    val error: LauncherError? = null,
    /** Set after a successful create/open; the screen navigates and then calls [LauncherViewModel.onWorkspaceHandled]. */
    val openedWorkspace: Workspace? = null,
    val projects: LauncherProjects = LauncherProjects(),
    /** `false` until the remembered projects are read, so empty states do not flash at startup. */
    val projectsLoaded: Boolean = false,
)

// Text fields hold TextFieldState so typing never round-trips through the StateFlow.
sealed interface LauncherDialog {
    class NewProject(
        val name: TextFieldState = TextFieldState(),
        val location: TextFieldState = TextFieldState(),
        val color: ProjectColor = ProjectColor.VIOLET,
    ) : LauncherDialog {
        fun copy(color: ProjectColor): NewProject = NewProject(name, location, color)
    }

    class OpenProject(val path: TextFieldState = TextFieldState()) : LauncherDialog
}

sealed interface LauncherError {
    data class Workspace(val error: WorkspaceError) : LauncherError

    data object MissingLocation : LauncherError

    data class InvalidLocation(val input: String) : LauncherError
}

class LauncherViewModel(
    private val createWorkspace: CreateWorkspace,
    private val workspaceOpener: WorkspaceOpener,
    private val recentWorkspaces: RecentWorkspacesRegistry,
    private val homeDirectory: () -> String = { System.getProperty("user.home") },
) : ViewModel() {
    private val _state = MutableStateFlow(LauncherUiState())
    val state: StateFlow<LauncherUiState> = _state.asStateFlow()

    // Latest known availability per remembered workspace; ignored once that workspace's location changes.
    private val availability = MutableStateFlow<Map<WorkspaceId, LocationCheck>>(emptyMap())

    // Locations already probed since the last refresh, so list updates only probe what is new.
    private val probed = mutableSetOf<Pair<WorkspaceId, Path>>()

    init {
        viewModelScope.launch { recentWorkspaces.initialize() }
        viewModelScope.launch {
            combine(recentWorkspaces.workspaces, recentWorkspaces.loaded, availability) { recents, loaded, checks ->
                val projects = launcherProjects(recents) { recent ->
                    checks[recent.id]?.takeIf { it.location == recent.location }?.availability
                        ?: ProjectAvailability.Unknown
                }
                projects to loaded
            }.collect { (projects, loaded) ->
                _state.update { it.copy(projects = projects, projectsLoaded = loaded) }
            }
        }
        // The list renders from the cache while locations are probed in the background.
        viewModelScope.launch { recentWorkspaces.workspaces.collect(::probe) }
    }

    fun showNewProject() = showDialog(LauncherDialog.NewProject())

    fun showOpenProject() = showDialog(LauncherDialog.OpenProject())

    /** Probes every remembered location again, for example when the launcher is shown again. */
    fun refreshAvailability() {
        probed.clear()
        probe(recentWorkspaces.workspaces.value)
    }

    /** Opens a remembered project through the normal validation of `project.lore`. */
    fun openProject(id: WorkspaceId) {
        val recent = recentWorkspaces.workspaces.value.firstOrNull { it.id == id } ?: return
        launchBusy {
            when (val result = workspaceOpener.open(recent.location, expectedId = id)) {
                is WorkspaceResult.Success -> onOpened(result.value)

                is WorkspaceResult.Failure -> {
                    fail(result.error)
                    when (result.error) {
                        is WorkspaceError.DifferentWorkspace -> markHeldByAnother(id, recent.location)

                        is WorkspaceError.NotAWorkspace, is WorkspaceError.FileSystemFailure ->
                            setAvailability(id, recent.location, ProjectAvailability.Missing)

                        else -> Unit
                    }
                }
            }
        }
    }

    /** Points a remembered project at [folder] without opening it; refused unless the folder holds that project. */
    fun relocateProject(id: WorkspaceId, folder: Path) {
        launchBusy {
            when (val result = workspaceOpener.relocate(id, folder)) {
                is WorkspaceResult.Success -> markFound(result.value)
                is WorkspaceResult.Failure -> fail(result.error)
            }
        }
    }

    fun setPinned(id: WorkspaceId, pinned: Boolean) {
        viewModelScope.launch { recentWorkspaces.setPinned(id, pinned) }
    }

    /** Removes the project from the launcher only; its folder is left untouched. */
    fun forgetProject(id: WorkspaceId) {
        viewModelScope.launch { recentWorkspaces.forget(id) }
    }

    fun dismissError() {
        if (_state.value.dialog == null) _state.update { it.copy(error = null) }
    }

    fun dismissDialog() {
        if (_state.value.busy) return
        _state.update { it.copy(dialog = null, error = null) }
    }

    fun onColorChange(color: ProjectColor) {
        _state.update { state ->
            val dialog = state.dialog as? LauncherDialog.NewProject ?: return@update state
            state.copy(dialog = dialog.copy(color = color))
        }
    }

    /** Fills the path field of the active dialog with a folder chosen in the native picker. */
    fun onDirectoryChosen(directory: Path) {
        when (val dialog = _state.value.dialog) {
            is LauncherDialog.NewProject -> dialog.location.setTextAndPlaceCursorAtEnd(directory.toString())
            is LauncherDialog.OpenProject -> dialog.path.setTextAndPlaceCursorAtEnd(directory.toString())
            null -> Unit
        }
    }

    fun submit() {
        val current = _state.value
        if (current.busy) return
        val dialog = current.dialog ?: return

        val locationText = when (dialog) {
            is LauncherDialog.NewProject -> dialog.location.text.toString()
            is LauncherDialog.OpenProject -> dialog.path.text.toString()
        }
        val location = when (val parsed = parseLocation(locationText)) {
            is ParsedLocation.Valid -> parsed.path

            is ParsedLocation.Invalid -> {
                _state.update { it.copy(error = parsed.error) }
                return
            }
        }

        launchBusy {
            val result = when (dialog) {
                is LauncherDialog.NewProject -> createWorkspace(location, dialog.name.text.toString(), dialog.color)
                is LauncherDialog.OpenProject -> workspaceOpener.open(location)
            }
            when (result) {
                is WorkspaceResult.Success -> onOpened(result.value)
                is WorkspaceResult.Failure -> fail(result.error)
            }
        }
    }

    fun onWorkspaceHandled() {
        _state.update { it.copy(openedWorkspace = null) }
    }

    private fun launchBusy(block: suspend () -> Unit) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                block()
            } finally {
                _state.update { it.copy(busy = false) }
            }
        }
    }

    // Recording happens after the navigation state is set: recent workspaces are only a local cache and
    // must never delay or fail opening a valid workspace.
    private suspend fun onOpened(workspace: Workspace) {
        markFound(workspace)
        _state.update { it.copy(busy = false, dialog = null, error = null, openedWorkspace = workspace) }
        workspaceOpener.recordOpened(workspace)
    }

    private fun fail(error: WorkspaceError) {
        _state.update { it.copy(busy = false, error = LauncherError.Workspace(error)) }
    }

    private fun probe(recents: List<RecentWorkspace>) {
        recents.filter { probed.add(it.id to it.location) }.forEach { recent ->
            viewModelScope.launch {
                val found = workspaceOpener.hasProjectFile(recent.location)
                availability.update { checks ->
                    val known = checks[recent.id]
                    // The probe cannot tell workspaces apart, so it never clears a confirmed mismatch.
                    if (found && known?.location == recent.location && known.heldByAnother) {
                        checks
                    } else {
                        val result = if (found) ProjectAvailability.Available else ProjectAvailability.Missing
                        checks + (recent.id to LocationCheck(recent.location, result))
                    }
                }
            }
        }
    }

    /** [workspace] is at its location, so any other remembered workspace last seen there is not. */
    private fun markFound(workspace: Workspace) {
        val displaced = recentWorkspaces.workspaces.value.filter {
            it.location == workspace.location && it.id != workspace.config.id
        }
        availability.update { checks ->
            checks + (workspace.config.id to LocationCheck(workspace.location, ProjectAvailability.Available)) +
                displaced.map { it.id to LocationCheck(it.location, ProjectAvailability.Missing, heldByAnother = true) }
        }
    }

    private fun markHeldByAnother(id: WorkspaceId, location: Path) {
        availability.update { it + (id to LocationCheck(location, ProjectAvailability.Missing, heldByAnother = true)) }
    }

    private fun setAvailability(id: WorkspaceId, location: Path, value: ProjectAvailability) {
        availability.update { it + (id to LocationCheck(location, value)) }
    }

    private fun showDialog(dialog: LauncherDialog) {
        if (_state.value.busy) return
        _state.update { it.copy(dialog = dialog, error = null) }
    }

    private fun parseLocation(input: String): ParsedLocation {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ParsedLocation.Invalid(LauncherError.MissingLocation)
        // Users type paths the way their shell shows them; Path does not expand "~" by itself.
        val expanded = when {
            trimmed == "~" -> homeDirectory()
            trimmed.startsWith("~/") || trimmed.startsWith("~\\") -> homeDirectory() + trimmed.substring(1)
            else -> trimmed
        }
        // Control characters are invalid on every supported platform; any other path the OS rejects
        // surfaces as a filesystem failure when the workspace is accessed.
        if (expanded.any { it.isISOControl() }) {
            return ParsedLocation.Invalid(LauncherError.InvalidLocation(trimmed))
        }
        return ParsedLocation.Valid(expanded.toPath())
    }

    private data class LocationCheck(
        val location: Path,
        val availability: ProjectAvailability,
        /** The folder holds a different workspace; only opening it again can show otherwise. */
        val heldByAnother: Boolean = false,
    )

    private sealed interface ParsedLocation {
        data class Valid(val path: Path) : ParsedLocation

        data class Invalid(val error: LauncherError) : ParsedLocation
    }
}
