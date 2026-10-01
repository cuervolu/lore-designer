package dev.cuervolu.loredesigner.ui.settings

/** Host services the Settings modal needs; implemented by the application, faked in tests. */
interface SettingsEnvironment {
    val applicationDataDirectory: String
    val logsDirectory: String

    /** Reveals [directory] in the platform file manager without blocking the caller; `false` when that failed. */
    suspend fun openFolder(directory: String): Boolean

    fun setDiagnosticLogging(enabled: Boolean)
}
