package dev.cuervolu.loredesigner

import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.di.loreDesignerModules
import dev.cuervolu.loredesigner.platform.logging.APP_TAG
import dev.cuervolu.loredesigner.platform.logging.DesktopStartupSnapshot
import dev.cuervolu.loredesigner.platform.logging.DiagnosticLogging
import dev.cuervolu.loredesigner.platform.logging.LogSession
import dev.cuervolu.loredesigner.platform.logging.installUncaughtExceptionLogging
import dev.cuervolu.loredesigner.platform.logging.logSessionEnd
import dev.cuervolu.loredesigner.platform.logging.logSessionStart
import dev.cuervolu.loredesigner.ui.settings.data.ApplicationSettingsRepository
import dev.nucleusframework.application.NucleusBackend
import dev.nucleusframework.application.aotTraining
import dev.nucleusframework.application.nucleusApplication
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import org.koin.core.context.startKoin

private const val APP_ID = "dev.cuervolu.loredesigner"

fun main() {
    FileKit.init(appId = APP_ID)
    val appDataDirectory = FileKit.filesDir.file.toPath()

    val koin = startKoin {
        modules(loreDesignerModules(appDataDirectory))
    }.koin

    // Resolved before anything else so later startup failures already reach the log file.
    val logger = koin.get<Logger>()
    installUncaughtExceptionLogging(logger.withTag(APP_TAG))
    val session = LogSession.start()
    logger.logSessionStart(session, DesktopStartupSnapshot.capture())
    // Nucleus ends the process with exitProcess, so code after nucleusApplication never runs.
    Runtime.getRuntime().addShutdownHook(Thread({ logger.logSessionEnd(session) }, "log-session-end"))

    val settingsRepository = koin.get<ApplicationSettingsRepository>()
    koin.get<DiagnosticLogging>().setEnabled(settingsRepository.settings.value.diagnosticLogging)

    nucleusApplication(backend = NucleusBackend.Tao) {
        aotTraining()
        LoreDesignerDesktopApp(settingsRepository, koin.get(), logger)
    }
}
