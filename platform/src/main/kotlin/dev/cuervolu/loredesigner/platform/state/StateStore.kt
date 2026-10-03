package dev.cuervolu.loredesigner.platform.state

/**
 * Loads and saves [PersistentStateComponent]s according to their [State] declaration.
 *
 * Only programmer errors throw (missing or malformed [State], storages declared inconsistently).
 * Unreadable, incompatible or unwritable files are reported through the results and never stop the
 * application. The component simply keeps its in-memory state.
 */
interface StateStore {
    suspend fun <T : Any> load(component: PersistentStateComponent<T>): StateLoadResult

    suspend fun <T : Any> save(component: PersistentStateComponent<T>): StateSaveResult
}

sealed interface StateLoadResult {
    data object Loaded : StateLoadResult

    /** Neither the storage file nor this component's entry exists. */
    data object Missing : StateLoadResult

    /** The storage is readable but this component's entry, or its version, could not be decoded. */
    data class ComponentInvalid(val reason: String) : StateLoadResult

    /** The storage file cannot be read or parsed. */
    data class StorageUnreadable(val reason: String) : StateLoadResult

    /** Written by a newer, incompatible release. The entry is left untouched. */
    data class NewerVersion(val persisted: String, val supported: String) : StateLoadResult

    /** Written with an older major version and no migration exists for it yet. */
    data class MigrationRequired(val persisted: String, val supported: String) : StateLoadResult
}

sealed interface StateSaveResult {
    data object Saved : StateSaveResult

    /**
     * The stored entry was not overwritten: it belongs to a newer major version, or to a newer minor
     * version whose extra fields this format cannot keep.
     */
    data class Blocked(val persisted: String, val supported: String) : StateSaveResult

    data class Failed(val cause: Exception) : StateSaveResult
}
