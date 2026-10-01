package dev.cuervolu.loredesigner.settings

import dev.cuervolu.loredesigner.platform.logging.DiagnosticLogging
import dev.cuervolu.loredesigner.platform.system.DesktopFolderOpener
import dev.cuervolu.loredesigner.ui.settings.SettingsEnvironment
import java.nio.file.Path

internal class DesktopSettingsEnvironment(
    appDataDirectory: Path,
    logsDirectory: Path,
    private val folderOpener: DesktopFolderOpener,
    private val diagnosticLogging: DiagnosticLogging,
) : SettingsEnvironment {
    override val applicationDataDirectory: String = appDataDirectory.toString()
    override val logsDirectory: String = logsDirectory.toString()

    override suspend fun openFolder(directory: String): Boolean = folderOpener.open(Path.of(directory))

    override fun setDiagnosticLogging(enabled: Boolean) = diagnosticLogging.setEnabled(enabled)
}
