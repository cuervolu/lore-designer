package dev.cuervolu.loredesigner.workspace.recent

import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.workspace.Workspace
import kotlinx.coroutines.flow.StateFlow

/**
 * Workspaces this installation has seen, keyed by [WorkspaceId].
 *
 * The registry is a local, non-authoritative cache. Entries are kept when their
 * folder disappears so moved or disconnected workspaces stay known.
 */
interface RecentWorkspacesRegistry {
    /** Most recently opened first. Empty until [loaded] becomes `true`. */
    val workspaces: StateFlow<List<RecentWorkspace>>

    val loaded: StateFlow<Boolean>

    /** Loads the remembered workspaces; calling it again does nothing. Mutations load first on their own. */
    suspend fun initialize()

    /** Remembers [workspace] as just opened, replacing the known path and cached metadata but keeping its pin. */
    suspend fun recordOpened(workspace: Workspace)

    /**
     * Stores [workspace]'s current location and cached metadata for an entry that is already remembered,
     * without counting as an open. Unknown ids are ignored.
     */
    suspend fun updateLocation(workspace: Workspace)

    suspend fun setPinned(id: WorkspaceId, pinned: Boolean)

    /** Forgets the entry. The workspace folder itself is never touched. */
    suspend fun forget(id: WorkspaceId)
}
