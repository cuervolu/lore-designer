package dev.cuervolu.loredesigner.platform.state

import kotlinx.serialization.KSerializer

/**
 * A component whose state a [StateStore] persists. The class must be annotated with [State].
 *
 * [serializer] is declared explicitly rather than discovered reflectively so the persisted shape is
 * always the one the component compiles against.
 */
interface PersistentStateComponent<T : Any> {
    val serializer: KSerializer<T>

    fun getState(): T

    /** Called with persisted state; not called when nothing usable was stored, so defaults stay in place. */
    fun loadState(state: T)
}
