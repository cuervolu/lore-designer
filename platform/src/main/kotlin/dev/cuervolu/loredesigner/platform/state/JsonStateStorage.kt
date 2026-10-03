package dev.cuervolu.loredesigner.platform.state

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * `{"components": {"<name>": {"version": "1.0.0", "state": {...}}}}`. Other top-level keys are kept as
 * they are.
 */
internal class JsonStateStorage : StateStorageBackend<JsonElement> {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun parse(content: String): StateDocument<JsonElement> {
        val root = try {
            json.parseToJsonElement(content)
        } catch (exception: SerializationException) {
            throw StateFormatException("Invalid JSON", exception)
        }
        if (root !is JsonObject) throw StateFormatException("Root must be a JSON object")

        val components = when (val element = root[COMPONENTS_KEY]) {
            null, JsonNull -> emptyMap()
            is JsonObject -> element.toMap()
            else -> throw StateFormatException("\"$COMPONENTS_KEY\" must be an object")
        }
        val unrecognized = JsonObject(root - COMPONENTS_KEY).takeIf { it.isNotEmpty() }
        return StateDocument(components, unrecognized)
    }

    override fun render(document: StateDocument<JsonElement>): String {
        val root = buildJsonObject {
            put(COMPONENTS_KEY, JsonObject(document.components.toSortedMap()))
            (document.unrecognized as? JsonObject)?.toSortedMap()?.forEach { (key, value) -> put(key, value) }
        }
        return json.encodeToString(JsonObject.serializer(), root) + "\n"
    }

    override fun unpack(entry: JsonElement): VersionedState<JsonElement>? {
        if (entry !is JsonObject) return null
        val version = (entry[VERSION_KEY] as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return null
        val state = entry[STATE_KEY] ?: return null
        return VersionedState(version, state)
    }

    override fun pack(state: VersionedState<JsonElement>): JsonElement = buildJsonObject {
        put(VERSION_KEY, JsonPrimitive(state.version))
        put(STATE_KEY, state.state)
    }

    override fun <T> decode(serializer: KSerializer<T>, payload: JsonElement): T =
        json.decodeFromJsonElement(serializer, payload)

    override fun <T> encode(serializer: KSerializer<T>, value: T): JsonElement =
        json.encodeToJsonElement(serializer, value)

    override fun keepUnknownFields(
        existing: JsonElement,
        encoded: JsonElement,
        knownFields: Set<String>,
    ): JsonElement? {
        if (existing !is JsonObject || encoded !is JsonObject) return null
        return JsonObject(existing.filterKeys { it !in knownFields } + encoded)
    }

    private companion object {
        const val COMPONENTS_KEY = "components"
        const val VERSION_KEY = "version"
        const val STATE_KEY = "state"
    }
}
