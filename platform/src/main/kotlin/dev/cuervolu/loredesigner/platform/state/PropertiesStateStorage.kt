package dev.cuervolu.loredesigner.platform.state

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.StringReader
import java.util.Properties
import kotlinx.serialization.properties.Properties as SerializationProperties

/**
 * Flat `key=value` storage mirroring the JSON layout: `<name>.version=1.0.0` and `<name>.state.<key>=...`.
 * Keys outside any component namespace are kept as they are.
 */
@OptIn(ExperimentalSerializationApi::class)
internal class PropertiesStateStorage : StateStorageBackend<Map<String, String>> {
    override fun parse(content: String): StateDocument<Map<String, String>> {
        val properties = Properties()
        try {
            properties.load(StringReader(content))
        } catch (exception: IllegalArgumentException) {
            // Properties.load throws this for malformed unicode escapes.
            throw StateFormatException("Malformed properties", exception)
        } catch (exception: IOException) {
            throw StateFormatException("Malformed properties", exception)
        }

        val components = mutableMapOf<String, MutableMap<String, String>>()
        val unrecognized = mutableMapOf<String, String>()
        properties.stringPropertyNames().forEach { key ->
            val value = properties.getProperty(key)
            val separator = key.indexOf('.')
            if (separator <= 0) {
                unrecognized[key] = value
            } else {
                components.getOrPut(key.substring(0, separator)) { mutableMapOf() }[key.substring(separator + 1)] =
                    value
            }
        }
        return StateDocument(components, unrecognized.takeIf { it.isNotEmpty() })
    }

    override fun render(document: StateDocument<Map<String, String>>): String {
        val properties = Properties()
        document.unrecognized?.forEach { (key, value) -> properties.setProperty(key, value) }
        document.components.forEach { (name, values) ->
            values.forEach { (key, value) -> properties.setProperty("$name.$key", value) }
        }
        // Since JDK 18 `store` sorts keys, but it always writes a timestamp comment; dropping it keeps
        // unchanged state byte-identical. The ISO-8859-1 stream form escapes everything else as \uXXXX.
        val bytes = ByteArrayOutputStream().also { properties.store(it, null) }
        return bytes.toString(Charsets.ISO_8859_1)
            .lineSequence()
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .joinToString(separator = "\n", postfix = "\n")
    }

    override fun unpack(entry: Map<String, String>): VersionedState<Map<String, String>>? {
        val version = entry[VERSION_KEY] ?: return null
        val state = entry.filterKeys { it.startsWith(STATE_PREFIX) }.mapKeys { it.key.removePrefix(STATE_PREFIX) }
        return VersionedState(version, state)
    }

    override fun pack(state: VersionedState<Map<String, String>>): Map<String, String> =
        mapOf(VERSION_KEY to state.version) + state.state.mapKeys { STATE_PREFIX + it.key }

    override fun <T> decode(serializer: KSerializer<T>, payload: Map<String, String>): T =
        SerializationProperties.decodeFromStringMap(serializer, payload)

    override fun <T> encode(serializer: KSerializer<T>, value: T): Map<String, String> =
        SerializationProperties.encodeToStringMap(serializer, value)

    // Nested values are flattened as `field.child` or `field.0`, so a key belongs to the field named by
    // its first segment.
    override fun keepUnknownFields(
        existing: Map<String, String>,
        encoded: Map<String, String>,
        knownFields: Set<String>,
    ): Map<String, String> = existing.filterKeys { it.substringBefore('.') !in knownFields } + encoded

    private companion object {
        const val VERSION_KEY = "version"
        const val STATE_PREFIX = "state."
    }
}
