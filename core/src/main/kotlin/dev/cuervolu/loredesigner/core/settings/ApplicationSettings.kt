package dev.cuervolu.loredesigner.core.settings

import dev.cuervolu.loredesigner.core.workspace.WorkspaceId

/** Personal, machine-local preferences. They never live inside a workspace. */
data class ApplicationSettings(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val density: UiDensity = UiDensity.COMPACT,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val autosave: Boolean = true,
    val spellcheck: Boolean = true,
    val documentFont: DocumentFont = DocumentFont.Default,
    val autosaveInterval: AutosaveInterval = AutosaveInterval.EVERY_CHANGE,
    val keepRecoveryCopies: Boolean = true,
    val recoveryRetention: RecoveryRetention = RecoveryRetention.DAYS_7,
    val diagnosticLogging: Boolean = false,
    /** Projects for which the user chose to reveal hidden files; keyed by id so it follows a moved folder. */
    val showHiddenFilesIn: Set<WorkspaceId> = emptySet(),
)

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemDark
        LIGHT -> false
        DARK -> true
    }
}

enum class UiDensity {
    COMPACT,
    COMFORTABLE,
}

/** [tag] is a BCP-47 language tag; `null` follows the operating system. */
enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    ENGLISH("en"),
    SPANISH("es"),
}

enum class AutosaveInterval {
    EVERY_CHANGE,
    EVERY_30_SECONDS,
    EVERY_5_MINUTES,
}

enum class RecoveryRetention {
    ONE_DAY,
    DAYS_7,
    DAYS_30,
}

/**
 * Typeface used for document text. Modeled as an open id rather than an enum so fonts contributed
 * later (bundled or by extensions) do not require a migration of persisted values.
 */
@JvmInline
value class DocumentFont(val id: String) {
    companion object {
        val SourceSerif4 = DocumentFont("source-serif-4")
        val Inter = DocumentFont("inter")
        val JetBrainsMono = DocumentFont("jetbrains-mono")

        val Default = SourceSerif4
        val BuiltIn: List<DocumentFont> = listOf(SourceSerif4, Inter, JetBrainsMono)
    }
}

/** Synchronous persistence boundary for [ApplicationSettings]; implementations decide the backing medium. */
interface ApplicationSettingsStore {
    fun read(): ApplicationSettings

    fun write(settings: ApplicationSettings)
}
