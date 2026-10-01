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
 * the default for that field instead of failing the whole read.
 */
class MultiplatformApplicationSettingsStore(private val settings: Settings) : ApplicationSettingsStore {
    private val defaults = ApplicationSettings()

    override fun read(): ApplicationSettings = ApplicationSettings(
        theme = enumValue(Keys.THEME, defaults.theme),
        density = enumValue(Keys.DENSITY, defaults.density),
        language = enumValue(Keys.LANGUAGE, defaults.language),
        autosave = boolean(Keys.AUTOSAVE, defaults.autosave),
        spellcheck = boolean(Keys.SPELLCHECK, defaults.spellcheck),
        documentFont = settings.getStringOrNull(Keys.DOCUMENT_FONT)
            ?.let(::DocumentFont)
            ?.takeIf { it in DocumentFont.BuiltIn }
            ?: defaults.documentFont,
        autosaveInterval = enumValue(Keys.AUTOSAVE_INTERVAL, defaults.autosaveInterval),
        keepRecoveryCopies = boolean(Keys.KEEP_RECOVERY, defaults.keepRecoveryCopies),
        recoveryRetention = enumValue(Keys.RECOVERY_RETENTION, defaults.recoveryRetention),
        diagnosticLogging = boolean(Keys.DIAGNOSTIC_LOGGING, defaults.diagnosticLogging),
        showHiddenFilesIn = settings.getStringOrNull(Keys.SHOW_HIDDEN_FILES_IN)
            ?.split(LIST_SEPARATOR)
            ?.mapNotNull { runCatching { WorkspaceId.parse(it.trim()) }.getOrNull() }
            ?.toSet()
            ?: defaults.showHiddenFilesIn,
    )

    override fun write(settings: ApplicationSettings) {
        val current = read()
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
    }

    private fun <T> putIfChanged(key: String, current: T, next: T, encode: (T) -> String) {
        if (current != next) settings.putString(key, encode(next))
    }

    private fun boolean(key: String, default: Boolean): Boolean =
        settings.getStringOrNull(key)?.toBooleanStrictOrNull() ?: default

    private inline fun <reified E : Enum<E>> enumValue(key: String, default: E): E {
        val stored = settings.getStringOrNull(key) ?: return default
        return enumValues<E>().firstOrNull { it.storedName == stored } ?: default
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
fun fileBackedApplicationSettingsStore(
    file: Path,
    logger: Logger = Logger.withTag("Settings"),
): ApplicationSettingsStore =
    MultiplatformApplicationSettingsStore(PropertiesFileSettings.create(file, logger = logger))
