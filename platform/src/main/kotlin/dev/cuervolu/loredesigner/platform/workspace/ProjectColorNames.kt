package dev.cuervolu.loredesigner.platform.workspace

import dev.cuervolu.loredesigner.core.workspace.ProjectColor

// Persisted in project.lore and in local state; these names must never change once released.
internal val ProjectColor.serializedName: String
    get() =
        when (this) {
            ProjectColor.VIOLET -> "violet"
            ProjectColor.BLUE -> "blue"
            ProjectColor.CYAN -> "cyan"
            ProjectColor.TEAL -> "teal"
            ProjectColor.GREEN -> "green"
            ProjectColor.AMBER -> "amber"
            ProjectColor.ORANGE -> "orange"
            ProjectColor.ROSE -> "rose"
            ProjectColor.RED -> "red"
            ProjectColor.SLATE -> "slate"
        }

internal fun projectColorOrNull(serializedName: String): ProjectColor? =
    ProjectColor.entries.firstOrNull { it.serializedName == serializedName }
