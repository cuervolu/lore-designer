package dev.cuervolu.loredesigner.di

import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.platform.logging.createLoreDesignerLogger
import dev.cuervolu.loredesigner.platform.workspace.FileSystemWorkspaceStore
import dev.cuervolu.loredesigner.platform.workspace.UuidV7WorkspaceIdGenerator
import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.OpenWorkspace
import dev.cuervolu.loredesigner.workspace.WorkspaceIdGenerator
import dev.cuervolu.loredesigner.workspace.WorkspaceStore
import okio.FileSystem
import org.koin.core.module.Module
import org.koin.dsl.module
import java.nio.file.Path

fun loreDesignerModules(logsDirectory: Path): List<Module> = listOf(
    loggingModule(logsDirectory),
    workspaceModule(),
)

private fun loggingModule(logsDirectory: Path): Module = module {
    single<Logger> { createLoreDesignerLogger(logsDirectory) }
}

private fun workspaceModule(): Module = module {
    single<WorkspaceStore> { FileSystemWorkspaceStore(FileSystem.SYSTEM) }
    single<WorkspaceIdGenerator> { UuidV7WorkspaceIdGenerator() }
    factory { CreateWorkspace(get(), get()) }
    factory { OpenWorkspace(get()) }
}
