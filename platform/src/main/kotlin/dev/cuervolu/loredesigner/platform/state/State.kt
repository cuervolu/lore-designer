package dev.cuervolu.loredesigner.platform.state

enum class StateFormat(internal val extension: String) {
    JSON("json"),
    PROPERTIES("properties"),
}

/**
 * Declares where a [PersistentStateComponent] keeps its state.
 *
 * This is for local, non-authoritative application state only; workspace files such as `project.lore`
 * keep their own codecs and validation.
 *
 * @property storage logical storage name, not a file name. For example `"ui"` becomes `ui.json` or
 *   `ui.properties` depending on [format]. Several components may share one storage, in which case
 *   they must declare the same [format].
 * @property name identifies the component inside its storage; blank uses the class's simple name.
 * @property version strict SemVer of this component's state schema. Each component in a shared
 *   storage is versioned on its own. A new major version means older releases must not read the state.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class State(
    val storage: String,
    val name: String = "",
    val version: String = "1.0.0",
    val format: StateFormat = StateFormat.JSON,
)
