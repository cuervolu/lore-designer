package dev.cuervolu.loredesigner.platform.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.MutableLoggerConfig
import co.touchlab.kermit.Severity
import co.touchlab.kermit.mutableLoggerConfigInit
import co.touchlab.kermit.platformLogWriter
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

/** The application-wide [Logger] together with the switch that controls how verbose it is. */
class LoreDesignerLogging internal constructor(val logger: Logger, val diagnostics: DiagnosticLogging)

/**
 * Raises the logger to verbose output while the user has diagnostic logging turned on. Normal output
 * stops at [Severity.Info]; debug and verbose entries explain how the application reached a state.
 */
class DiagnosticLogging internal constructor(private val config: MutableLoggerConfig, private val logger: Logger) {
    val enabled: Boolean get() = config.minSeverity == Severity.Verbose

    fun setEnabled(enabled: Boolean) {
        if (enabled == this.enabled) return
        // Announced while the more verbose level is active, so the toggle is recorded in both directions.
        if (enabled) {
            config.minSeverity = Severity.Verbose
            logger.i { "Diagnostic logging enabled" }
        } else {
            logger.i { "Diagnostic logging disabled" }
            config.minSeverity = Severity.Info
        }
    }
}

/** Tag for application lifecycle entries: session start and end, uncaught failures, diagnostics toggle. */
const val APP_TAG = "App"

/** Builds the application logger, writing to the console and to a rotating file under [logsDirectory]. */
fun createLoreDesignerLogging(
    logsDirectory: Path,
    rotationPolicy: LogRotationPolicy = LogRotationPolicy.Default,
): LoreDesignerLogging {
    val config =
        mutableLoggerConfigInit(listOf(platformLogWriter(), RotatingFileLogWriter(logsDirectory, rotationPolicy)))
    config.minSeverity = Severity.Info
    val logger = Logger(config = config, tag = "LoreDesigner")
    return LoreDesignerLogging(logger, DiagnosticLogging(config, logger.withTag(APP_TAG)))
}

/**
 * Records the start of [session] as a header line followed by an aligned metadata block, so separate
 * runs are easy to spot and `grep "Session "` lists every start and end. Content stays metadata-only.
 */
fun Logger.logSessionStart(session: LogSession, snapshot: DesktopStartupSnapshot) {
    i(tag = APP_TAG) { sessionStartMessage(session, snapshot) }
}

/** Records the end of [session]; runs from a shutdown hook, so it also covers exits after a fatal error. */
fun Logger.logSessionEnd(session: LogSession, endedAt: Instant = Instant.now()) {
    i(tag = APP_TAG) {
        "$SESSION_RULE Session ${session.id} ended after ${uptime(session.startedAt, endedAt)} $SESSION_RULE"
    }
}

internal fun sessionStartMessage(session: LogSession, snapshot: DesktopStartupSnapshot): String {
    val fields = listOfNotNull(
        "version" to snapshot.appVersion,
        "os" to snapshot.runningOn,
        "kernel" to snapshot.kernelVersion,
        "executable" to snapshot.executableType,
        snapshot.desktopEnvironment?.let { "desktop" to it },
        snapshot.displayServer?.let { "display server" to it },
        "time zone" to snapshot.timeZone,
    )
    val keyWidth = fields.maxOf { (key, _) -> key.length }
    return buildString {
        append(SESSION_RULE).append(" Session ").append(session.id).append(" started ").append(SESSION_RULE)
        for ((key, value) in fields) {
            append('\n').append(key.padEnd(keyWidth)).append("  ").append(value)
        }
    }
}

private fun uptime(startedAt: Instant, endedAt: Instant): String {
    val duration = Duration.between(startedAt, endedAt).coerceAtLeast(Duration.ZERO)
    return "%dh %02dm %02ds".format(duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart())
}

private const val SESSION_RULE = "====="
