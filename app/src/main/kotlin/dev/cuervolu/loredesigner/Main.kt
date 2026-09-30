package dev.cuervolu.loredesigner

import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.di.loreDesignerModules
import dev.cuervolu.loredesigner.platform.logging.DesktopStartupSnapshot
import dev.cuervolu.loredesigner.platform.logging.LogSession
import dev.cuervolu.loredesigner.platform.logging.logSessionStart
import dev.nucleusframework.application.NucleusBackend
import dev.nucleusframework.application.aotTraining
import dev.nucleusframework.application.nucleusApplication
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import org.koin.core.context.startKoin

private const val APP_ID = "dev.cuervolu.loredesigner"

fun main() {
    FileKit.init(appId = APP_ID)
    val logsDirectory = FileKit.filesDir.file.toPath().resolve("logs")

    val koinApplication = startKoin {
        modules(loreDesignerModules(logsDirectory))
    }

    val logger = koinApplication.koin.get<Logger>()
    val session = LogSession.start()
    logger.logSessionStart(session, DesktopStartupSnapshot.capture(session.startedAt))

    nucleusApplication(backend = NucleusBackend.Tao) {
        aotTraining()
        LoreDesignerDesktopApp()
    }
}
