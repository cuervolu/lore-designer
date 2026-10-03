package dev.cuervolu.loredesigner.platform.di

import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.core.settings.ApplicationSettingsStore
import dev.cuervolu.loredesigner.platform.logging.LoreDesignerLogging
import dev.cuervolu.loredesigner.platform.logging.createLoreDesignerLogging
import dev.cuervolu.loredesigner.platform.settings.fileBackedApplicationSettingsStore
import dev.cuervolu.loredesigner.platform.state.FileStateStore
import dev.cuervolu.loredesigner.platform.state.StateStore
import dev.cuervolu.loredesigner.platform.system.DesktopFolderOpener
import dev.cuervolu.loredesigner.platform.workspace.FileSystemWorkspaceStore
import dev.cuervolu.loredesigner.platform.workspace.PersistentRecentWorkspacesRegistry
import dev.cuervolu.loredesigner.platform.workspace.UuidV7WorkspaceIdGenerator
import dev.cuervolu.loredesigner.workspace.WorkspaceIdGenerator
import dev.cuervolu.loredesigner.workspace.WorkspaceStore
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspacesRegistry
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.koin.core.module.Module
import org.koin.dsl.module
import java.nio.file.Path

private const val SETTINGS_FILE_NAME = "settings.properties"
private const val LOGS_DIRECTORY_NAME = "logs"

/** Where logs are written inside the per-user application data folder. */
fun logsDirectoryIn(appDataDirectory: Path): Path = appDataDirectory.resolve(LOGS_DIRECTORY_NAME)

/** Desktop infrastructure: logging, filesystem-backed stores and system integration under [appDataDirectory]. */
fun platformModules(appDataDirectory: Path): List<Module> = listOf(
    loggingModule(logsDirectoryIn(appDataDirectory)),
    storageModule(appDataDirectory),
    systemModule(),
)

private fun loggingModule(logsDirectory: Path): Module = module {
    single { createLoreDesignerLogging(logsDirectory) }
    single<Logger> { get<LoreDesignerLogging>().logger }
    single { get<LoreDesignerLogging>().diagnostics }
}

private fun storageModule(appDataDirectory: Path): Module = module {
    single<WorkspaceStore> { FileSystemWorkspaceStore(FileSystem.SYSTEM, get<Logger>().withTag("Workspaces")) }
    single<WorkspaceIdGenerator> { UuidV7WorkspaceIdGenerator() }
    single<StateStore> {
        FileStateStore(appDataDirectory.toOkioPath(), FileSystem.SYSTEM, get<Logger>().withTag("State"))
    }
    single<RecentWorkspacesRegistry> {
        PersistentRecentWorkspacesRegistry(get(), get<Logger>().withTag("RecentWorkspaces"))
    }
    single<ApplicationSettingsStore> {
        fileBackedApplicationSettingsStore(
            file = appDataDirectory.resolve(SETTINGS_FILE_NAME).toOkioPath(),
            logger = get<Logger>().withTag("Settings"),
        )
    }
}

private fun systemModule(): Module = module {
    single { DesktopFolderOpener(get<Logger>().withTag("System")) }
}
