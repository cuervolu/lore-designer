package dev.cuervolu.loredesigner.platform.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter
import java.nio.file.Path

/** Builds the application-wide [Logger], writing to the console and to a rotating file under [logsDirectory]. */
fun createLoreDesignerLogger(
    logsDirectory: Path,
    rotationPolicy: LogRotationPolicy = LogRotationPolicy.Default,
): Logger = Logger(
    config = loggerConfigInit(platformLogWriter(), RotatingFileLogWriter(logsDirectory, rotationPolicy)),
    tag = "LoreDesigner",
)

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
