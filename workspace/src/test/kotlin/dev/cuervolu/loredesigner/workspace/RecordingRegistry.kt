package dev.cuervolu.loredesigner.workspace

import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspace
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspacesRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class RecordingRegistry : RecentWorkspacesRegistry {
    val recorded = mutableListOf<Workspace>()
    val relocated = mutableListOf<Workspace>()
    var failure: Exception? = null
    override val workspaces: StateFlow<List<RecentWorkspace>> = MutableStateFlow(emptyList())
    override val loaded: StateFlow<Boolean> = MutableStateFlow(true)

    override suspend fun initialize() = Unit

    override suspend fun recordOpened(workspace: Workspace) {
        failure?.let { throw it }
        recorded += workspace
    }

    override suspend fun updateLocation(workspace: Workspace) {
        failure?.let { throw it }
        relocated += workspace
    }

    override suspend fun setPinned(id: WorkspaceId, pinned: Boolean) = Unit

    override suspend fun forget(id: WorkspaceId) = Unit
}
