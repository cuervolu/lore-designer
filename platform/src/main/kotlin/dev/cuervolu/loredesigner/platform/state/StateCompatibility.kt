package dev.cuervolu.loredesigner.platform.state

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.toVersionOrNull

internal sealed interface StateCompatibility {
    /** Same major version; [persisted] may be older or newer within it. */
    data class Compatible(val persisted: Version) : StateCompatibility

    data class NewerMajor(val persisted: Version) : StateCompatibility

    /** Explicit migrations would hook in here; until one exists the old payload is never decoded. */
    data class OlderMajor(val persisted: Version) : StateCompatibility

    data class Malformed(val persisted: String) : StateCompatibility
}

/** Decides whether component state written with [persisted] may be read by a release declaring [declared]. */
internal fun stateCompatibility(declared: Version, persisted: String): StateCompatibility {
    val version = persisted.toVersionOrNull(strict = true) ?: return StateCompatibility.Malformed(persisted)
    return when {
        version.major > declared.major -> StateCompatibility.NewerMajor(version)
        version.major < declared.major -> StateCompatibility.OlderMajor(version)
        else -> StateCompatibility.Compatible(version)
    }
}
