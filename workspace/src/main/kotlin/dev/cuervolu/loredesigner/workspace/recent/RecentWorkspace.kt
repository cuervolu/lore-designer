package dev.cuervolu.loredesigner.workspace.recent

import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import okio.Path
import kotlin.time.Instant

/**
 * A workspace this installation remembers having opened at [location].
 *
 * Only [id] identifies the workspace. The name and color are cached from the last successful open so a
 * missing workspace can still be shown; whenever `project.lore` is readable its values win.
 */
data class RecentWorkspace(
    val id: WorkspaceId,
    val location: Path,
    val lastKnownName: String,
    val lastKnownColor: ProjectColor?,
    val lastOpenedAt: Instant,
    val pinned: Boolean,
)
