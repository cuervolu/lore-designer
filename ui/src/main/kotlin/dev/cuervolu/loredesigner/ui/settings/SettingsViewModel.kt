package dev.cuervolu.loredesigner.ui.settings

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cuervolu.loredesigner.core.settings.ApplicationSettings
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.ui.settings.data.ApplicationSettingsRepository
import dev.cuervolu.loredesigner.workspace.UpdateProjectConfig
import dev.cuervolu.loredesigner.workspace.Workspace
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import dev.cuervolu.loredesigner.workspace.WorkspaceResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: ApplicationSettings,
    val selectedPageId: String,
    /** The open workspace as last saved; `null` in the launcher. */
    val workspace: Workspace?,
    val projectSaving: Boolean = false,
    val projectError: WorkspaceError? = null,
    /** Folder that could not be revealed, so the page can show its path instead. */
    val unopenedFolder: String? = null,
)

/**
 * State holder for one Settings modal context. A workspace-scoped instance is created per open
 * workspace, so [availablePages] never changes during its lifetime.
 */
class SettingsViewModel(
    private val repository: ApplicationSettingsRepository,
    registry: SettingsPageRegistry,
    workspace: Workspace?,
    private val updateProjectConfig: UpdateProjectConfig,
    private val environment: SettingsEnvironment,
) : ViewModel() {
    val availablePages: List<SettingsPage> = registry.available(hasWorkspace = workspace != null)

    // Text fields hold TextFieldState so typing never round-trips through the StateFlow.
    val query = TextFieldState()
    val projectName = TextFieldState(workspace?.config?.name.orEmpty())

    private val local = MutableStateFlow(
        SettingsUiState(
            settings = repository.settings.value,
            selectedPageId = availablePages.first().id,
            workspace = workspace,
        ),
    )

    val state: StateFlow<SettingsUiState> = combine(local, repository.settings) { local, settings ->
        local.copy(settings = settings)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, local.value)

    val applicationDataDirectory: String get() = environment.applicationDataDirectory

    fun selectPage(id: String) {
        if (availablePages.none { it.id == id }) return
        query.clearText()
        local.update { it.copy(selectedPageId = id, unopenedFolder = null) }
    }

    fun updateSettings(transform: (ApplicationSettings) -> ApplicationSettings) {
        val before = repository.settings.value
        repository.update(transform)
        val after = repository.settings.value
        if (after.diagnosticLogging != before.diagnosticLogging) {
            environment.setDiagnosticLogging(after.diagnosticLogging)
        }
    }

    fun setShowHiddenFiles(show: Boolean) {
        val id = local.value.workspace?.config?.id ?: return
        updateSettings {
            it.copy(showHiddenFilesIn = if (show) it.showHiddenFilesIn + id else it.showHiddenFilesIn - id)
        }
    }

    fun openLogs() = openFolder(environment.logsDirectory)

    fun openApplicationData() = openFolder(environment.applicationDataDirectory)

    /** Saves the edited name; called when the field is submitted or loses focus. */
    fun commitProjectName() {
        val workspace = local.value.workspace ?: return
        saveProject(projectName.text.toString(), workspace.config.color)
    }

    fun setProjectColor(color: ProjectColor) {
        val workspace = local.value.workspace ?: return
        saveProject(workspace.config.name, color)
    }

    private fun saveProject(name: String, color: ProjectColor?) {
        val workspace = local.value.workspace ?: return
        if (local.value.projectSaving) return
        local.update { it.copy(projectSaving = true, projectError = null) }
        viewModelScope.launch {
            val result = updateProjectConfig(workspace, name, color)
            local.update { state ->
                when (result) {
                    is WorkspaceResult.Success -> state.copy(projectSaving = false, workspace = result.value)
                    is WorkspaceResult.Failure -> state.copy(projectSaving = false, projectError = result.error)
                }
            }
            if (result is WorkspaceResult.Success) {
                // Reflect the trimmed name the store accepted.
                projectName.setTextAndPlaceCursorAtEnd(result.value.config.name)
            }
        }
    }

    private fun openFolder(directory: String) {
        viewModelScope.launch {
            val opened = environment.openFolder(directory)
            local.update { it.copy(unopenedFolder = if (opened) null else directory) }
        }
    }
}
