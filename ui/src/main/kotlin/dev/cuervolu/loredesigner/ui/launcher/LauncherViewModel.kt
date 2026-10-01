package dev.cuervolu.loredesigner.ui.launcher

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.OpenWorkspace
import dev.cuervolu.loredesigner.workspace.Workspace
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import dev.cuervolu.loredesigner.workspace.WorkspaceResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val openWorkspace: OpenWorkspace,
    private val homeDirectory: () -> String = { System.getProperty("user.home") },
) : ViewModel() {
    private val _state = MutableStateFlow(LauncherUiState())
    val state: StateFlow<LauncherUiState> = _state.asStateFlow()

    fun showNewProject() = showDialog(LauncherDialog.NewProject())

    fun showOpenProject() = showDialog(LauncherDialog.OpenProject())

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

        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            val result = when (dialog) {
                is LauncherDialog.NewProject -> createWorkspace(location, dialog.name.text.toString(), dialog.color)
                is LauncherDialog.OpenProject -> openWorkspace(location)
            }
            _state.update { state ->
                when (result) {
                    is WorkspaceResult.Success ->
                        state.copy(busy = false, dialog = null, error = null, openedWorkspace = result.value)

                    is WorkspaceResult.Failure ->
                        state.copy(busy = false, error = LauncherError.Workspace(result.error))
                }
            }
        }
    }

    fun onWorkspaceHandled() {
        _state.update { it.copy(openedWorkspace = null) }
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

    private sealed interface ParsedLocation {
        data class Valid(val path: Path) : ParsedLocation

        data class Invalid(val error: LauncherError) : ParsedLocation
    }
}
