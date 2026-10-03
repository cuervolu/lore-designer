package dev.cuervolu.loredesigner.platform.settings

import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import dev.cuervolu.loredesigner.core.settings.ApplicationSettings
import dev.cuervolu.loredesigner.core.settings.ApplicationSettingsStore
import dev.cuervolu.loredesigner.core.settings.DocumentFont
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import okio.Path

/**
 * Maps [ApplicationSettings] onto a key-value [Settings] store.
 *
 * Values are stored as stable lowercase names rather than ordinals or Kotlin enum names, so reordering
 * or renaming constants never reinterprets an existing file. Unknown or damaged values fall back to
 * the default for that field instead of failing the whole read. Logs name keys, never their values.
 */
class MultiplatformApplicationSettingsStore(private val settings: Settings, private val logger: Logger) :
    ApplicationSettingsStore {
    private val defaults = ApplicationSettings()

    override fun read(): ApplicationSettings = ApplicationSettings(
        theme = enumValue(Keys.THEME, defaults.theme),
        density = enumValue(Keys.DENSITY, defaults.density),
        language = enumValue(Keys.LANGUAGE, defaults.language),
        autosave = boolean(Keys.AUTOSAVE, defaults.autosave),
        spellcheck = boolean(Keys.SPELLCHECK, defaults.spellcheck),
        documentFont = settings.getStringOrNull(Keys.DOCUMENT_FONT)
            ?.let(::DocumentFont)
            ?.let { font -> font.takeIf { it in DocumentFont.BuiltIn } ?: unrecognized(Keys.DOCUMENT_FONT) }
            ?: defaults.documentFont,
        autosaveInterval = enumValue(Keys.AUTOSAVE_INTERVAL, defaults.autosaveInterval),
        keepRecoveryCopies = boolean(Keys.KEEP_RECOVERY, defaults.keepRecoveryCopies),
        recoveryRetention = enumValue(Keys.RECOVERY_RETENTION, defaults.recoveryRetention),
        diagnosticLogging = boolean(Keys.DIAGNOSTIC_LOGGING, defaults.diagnosticLogging),
        showHiddenFilesIn = settings.getStringOrNull(Keys.SHOW_HIDDEN_FILES_IN)
            ?.split(LIST_SEPARATOR)
            ?.mapNotNull {
                runCatching { WorkspaceId.parse(it.trim()) }.getOrNull()
                    ?: unrecognized(Keys.SHOW_HIDDEN_FILES_IN)
            }
            ?.toSet()
            ?: defaults.showHiddenFilesIn,
    )

    override fun write(settings: ApplicationSettings) {
        val current = read()
        val changedKeys = mutableListOf<String>()

        fun <T> putIfChanged(key: String, stored: T, next: T, encode: (T) -> String) {
            if (stored == next) return
            changedKeys += key
            this.settings.putString(key, encode(next))
        }

        // The file-backed Settings rewrites the file on every put, so only changed keys are written.
        putIfChanged(Keys.THEME, current.theme, settings.theme) { it.storedName }
        putIfChanged(Keys.DENSITY, current.density, settings.density) { it.storedName }
        putIfChanged(Keys.LANGUAGE, current.language, settings.language) { it.storedName }
        putIfChanged(Keys.AUTOSAVE, current.autosave, settings.autosave) { it.toString() }
        putIfChanged(Keys.SPELLCHECK, current.spellcheck, settings.spellcheck) { it.toString() }
        putIfChanged(Keys.DOCUMENT_FONT, current.documentFont, settings.documentFont) { it.id }
        putIfChanged(Keys.AUTOSAVE_INTERVAL, current.autosaveInterval, settings.autosaveInterval) { it.storedName }
        putIfChanged(Keys.KEEP_RECOVERY, current.keepRecoveryCopies, settings.keepRecoveryCopies) { it.toString() }
        putIfChanged(Keys.RECOVERY_RETENTION, current.recoveryRetention, settings.recoveryRetention) { it.storedName }
        putIfChanged(Keys.DIAGNOSTIC_LOGGING, current.diagnosticLogging, settings.diagnosticLogging) { it.toString() }
        putIfChanged(Keys.SHOW_HIDDEN_FILES_IN, current.showHiddenFilesIn, settings.showHiddenFilesIn) { ids ->
            ids.map(WorkspaceId::toString).sorted().joinToString(LIST_SEPARATOR)
        }
        if (changedKeys.isNotEmpty()) logger.d { "Writing changed settings keys: ${changedKeys.joinToString()}" }
    }

    private fun <T> unrecognized(key: String): T? {
        logger.d { "Ignoring an unrecognized stored value for $key" }
        return null
    }

    private fun boolean(key: String, default: Boolean): Boolean {
        val stored = settings.getStringOrNull(key) ?: return default
        return stored.toBooleanStrictOrNull() ?: unrecognized(key) ?: default
    }

    private inline fun <reified E : Enum<E>> enumValue(key: String, default: E): E {
        val stored = settings.getStringOrNull(key) ?: return default
        return enumValues<E>().firstOrNull { it.storedName == stored } ?: unrecognized(key) ?: default
    }

    private object Keys {
        const val THEME = "appearance.theme"
        const val DENSITY = "appearance.density"
        const val LANGUAGE = "language.application"
        const val AUTOSAVE = "editor.autosave"
        const val SPELLCHECK = "editor.spellcheck"
        const val DOCUMENT_FONT = "editor.documentFont"
        const val AUTOSAVE_INTERVAL = "files.autosaveInterval"
        const val KEEP_RECOVERY = "files.keepRecoveryCopies"
        const val RECOVERY_RETENTION = "files.recoveryRetention"
        const val DIAGNOSTIC_LOGGING = "diagnostics.logging"
        const val SHOW_HIDDEN_FILES_IN = "projects.showHiddenFiles"
    }

    private companion object {
        const val LIST_SEPARATOR = ","
    }
}

private val Enum<*>.storedName: String get() = name.lowercase().replace('_', '-')

/** Application settings persisted to [file] as a properties file. */
fun fileBackedApplicationSettingsStore(file: Path, logger: Logger): ApplicationSettingsStore =
    MultiplatformApplicationSettingsStore(PropertiesFileSettings.create(file, logger = logger), logger)
