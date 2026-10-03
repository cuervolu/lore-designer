package dev.cuervolu.loredesigner.ui.launcher

import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.workspace.Workspace
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspace
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspacesRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** In-memory registry with the real ordering and identity rules; each recorded open is one minute later. */
internal class FakeRecentWorkspacesRegistry(initial: List<RecentWorkspace> = emptyList()) : RecentWorkspacesRegistry {
    private var clock = Instant.parse("2026-10-01T12:00:00Z")
    private val entries = MutableStateFlow(initial.sortedByDescending { it.lastOpenedAt })
    override val workspaces: StateFlow<List<RecentWorkspace>> = entries
    override val loaded = MutableStateFlow(false)

    override suspend fun initialize() {
        loaded.value = true
    }

    /** Thrown by [recordOpened] when set, standing in for a persistence failure. */
    var recordFailure: Exception? = null

    override suspend fun recordOpened(workspace: Workspace) {
        recordFailure?.let { throw it }
        clock += 1.minutes
        entries.update { current ->
            val pinned = current.firstOrNull { it.id == workspace.config.id }?.pinned ?: false
            val opened = RecentWorkspace(
                workspace.config.id,
                workspace.location,
                workspace.config.name,
                workspace.config.color,
                clock,
                pinned,
            )
            (current.filterNot { it.id == opened.id } + opened).sortedByDescending { it.lastOpenedAt }
        }
    }

    override suspend fun updateLocation(workspace: Workspace) {
        entries.update { current ->
            current.map {
                if (it.id != workspace.config.id) {
                    it
                } else {
                    it.copy(
                        location = workspace.location,
                        lastKnownName = workspace.config.name,
                        lastKnownColor = workspace.config.color,
                    )
                }
            }
        }
    }

    override suspend fun setPinned(id: WorkspaceId, pinned: Boolean) {
        entries.update { current -> current.map { if (it.id == id) it.copy(pinned = pinned) else it } }
    }

    override suspend fun forget(id: WorkspaceId) {
        entries.update { current -> current.filterNot { it.id == id } }
    }
}
