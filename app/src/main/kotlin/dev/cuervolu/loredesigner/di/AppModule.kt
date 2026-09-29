package dev.cuervolu.loredesigner.di

import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.platform.logging.createLoreDesignerLogger
import org.koin.core.module.Module
import org.koin.dsl.module
import java.nio.file.Path

fun loreDesignerModules(logsDirectory: Path): List<Module> = listOf(loggingModule(logsDirectory))

private fun loggingModule(logsDirectory: Path): Module = module {
    single<Logger> { createLoreDesignerLogger(logsDirectory) }
}
