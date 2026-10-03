package dev.cuervolu.loredesigner.ui.launcher

import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspace

/** How many available projects the Recent section shows. */
internal const val RECENT_PROJECT_LIMIT = 10

enum class ProjectAvailability {
    /** Not checked yet; shown as openable so the launcher can render from the cached list immediately. */
    Unknown,
    Available,
    Missing,
}

data class LauncherProject(
    val id: WorkspaceId,
    val name: String,
    val path: String,
    val color: ProjectColor?,
    val pinned: Boolean,
    val availability: ProjectAvailability,
)

/** Remembered projects split into the launcher sections, each most recently opened first. */
data class LauncherProjects(
    val all: List<LauncherProject> = emptyList(),
    val pinned: List<LauncherProject> = emptyList(),
    val recent: List<LauncherProject> = emptyList(),
    val missing: List<LauncherProject> = emptyList(),
) {
    fun forSection(section: LauncherSection): List<LauncherProject> = when (section) {
        LauncherSection.Projects -> all
        LauncherSection.Pinned -> pinned
        LauncherSection.Recent -> recent
        LauncherSection.Missing -> missing
    }
}

/** [recents] must already be ordered most recently opened first. */
internal fun launcherProjects(
    recents: List<RecentWorkspace>,
    availabilityOf: (RecentWorkspace) -> ProjectAvailability,
): LauncherProjects {
    val projects = recents.map { recent ->
        LauncherProject(
            id = recent.id,
            name = recent.lastKnownName,
            path = recent.location.toString(),
            color = recent.lastKnownColor,
            pinned = recent.pinned,
            availability = availabilityOf(recent),
        )
    }
    val (missing, present) = projects.partition { it.availability == ProjectAvailability.Missing }
    return LauncherProjects(
        all = present,
        // A pinned project stays pinned while its folder is unavailable, shown with its missing state.
        pinned = projects.filter { it.pinned },
        recent = present.take(RECENT_PROJECT_LIMIT),
        missing = missing,
    )
}
