package dev.cuervolu.loredesigner.platform.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.MutableLoggerConfig
import co.touchlab.kermit.Severity
import co.touchlab.kermit.mutableLoggerConfigInit
import co.touchlab.kermit.platformLogWriter
import java.nio.file.Path

/** The application-wide [Logger] together with the switch that controls how verbose it is. */
class LoreDesignerLogging internal constructor(val logger: Logger, val diagnostics: DiagnosticLogging)

/** Raises the logger to verbose output while the user has diagnostic logging turned on. */
class DiagnosticLogging internal constructor(private val config: MutableLoggerConfig) {
    val enabled: Boolean get() = config.minSeverity == Severity.Verbose

    fun setEnabled(enabled: Boolean) {
        config.minSeverity = if (enabled) Severity.Verbose else Severity.Info
    }
}

/** Builds the application logger, writing to the console and to a rotating file under [logsDirectory]. */
fun createLoreDesignerLogging(
    logsDirectory: Path,
    rotationPolicy: LogRotationPolicy = LogRotationPolicy.Default,
): LoreDesignerLogging {
    val config =
        mutableLoggerConfigInit(listOf(platformLogWriter(), RotatingFileLogWriter(logsDirectory, rotationPolicy)))
    val diagnostics = DiagnosticLogging(config).apply { setEnabled(false) }
    return LoreDesignerLogging(Logger(config = config, tag = "LoreDesigner"), diagnostics)
}

/** Records the one startup entry for [session]/[snapshot]. Content stays metadata-only. */
fun Logger.logSessionStart(session: LogSession, snapshot: DesktopStartupSnapshot) {
    i(tag = "App") {
        buildString {
            append("session=").append(session.id)
            append(" startedAt=").append(session.startedAt)
            append(" appVersion=").append(snapshot.appVersion)
            append(" runningOn=").append(snapshot.runningOn)
            append(" kernel=").append(snapshot.kernelVersion)
            append(" executableType=").append(snapshot.executableType)
            snapshot.desktopEnvironment?.let { append(" desktopEnvironment=").append(it) }
            snapshot.displayServer?.let { append(" displayServer=").append(it) }
        }
    }
}
