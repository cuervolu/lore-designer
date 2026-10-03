package dev.cuervolu.loredesigner.platform.state

import kotlinx.serialization.KSerializer

/**
 * A parsed storage file: one raw entry per component name, plus anything outside the component
 * namespace, which is written back untouched.
 */
internal data class StateDocument<P>(val components: Map<String, P>, val unrecognized: P?)

/** A component entry split into the version it was written with and its serialized state. */
internal data class VersionedState<P>(val version: String, val state: P)

internal class StateFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Converts between a storage file's text and per-component payloads of type [P]. */
internal interface StateStorageBackend<P> {
    /** @throws StateFormatException when [content] is not a readable storage file. */
    fun parse(content: String): StateDocument<P>

    fun render(document: StateDocument<P>): String

    /** Splits a raw component entry; `null` when it does not have the version/state shape. */
    fun unpack(entry: P): VersionedState<P>?

    fun pack(state: VersionedState<P>): P

    /** @throws kotlinx.serialization.SerializationException or [IllegalArgumentException] when [payload] does not fit. */
    fun <T> decode(serializer: KSerializer<T>, payload: P): T

    fun <T> encode(serializer: KSerializer<T>, value: T): P

    /**
     * Returns [encoded] plus the top-level fields of [existing] whose names are not in [knownFields], so
     * fields written by a newer minor version survive a save. `null` when [existing] has no fields to keep
     * in this format.
     */
    fun keepUnknownFields(existing: P, encoded: P, knownFields: Set<String>): P?
}
