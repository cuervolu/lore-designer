package dev.cuervolu.loredesigner.di

import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.core.settings.ApplicationSettingsStore
import dev.cuervolu.loredesigner.platform.logging.LoreDesignerLogging
import dev.cuervolu.loredesigner.platform.logging.createLoreDesignerLogging
import dev.cuervolu.loredesigner.platform.settings.fileBackedApplicationSettingsStore
import dev.cuervolu.loredesigner.platform.system.DesktopFolderOpener
import dev.cuervolu.loredesigner.platform.workspace.FileSystemWorkspaceStore
import dev.cuervolu.loredesigner.platform.workspace.UuidV7WorkspaceIdGenerator
import dev.cuervolu.loredesigner.settings.DesktopSettingsEnvironment
import dev.cuervolu.loredesigner.ui.settings.SettingsEnvironment
import dev.cuervolu.loredesigner.ui.settings.SettingsPageRegistry
import dev.cuervolu.loredesigner.ui.settings.SettingsViewModel
import dev.cuervolu.loredesigner.ui.settings.SettingsViewModelFactory
import dev.cuervolu.loredesigner.ui.settings.data.ApplicationSettingsRepository
import dev.cuervolu.loredesigner.ui.settings.pages.builtInSettingsPages
import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.OpenWorkspace
import dev.cuervolu.loredesigner.workspace.UpdateProjectConfig
import dev.cuervolu.loredesigner.workspace.WorkspaceIdGenerator
import dev.cuervolu.loredesigner.workspace.WorkspaceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.koin.core.module.Module
import org.koin.dsl.module
import java.nio.file.Path

private const val SETTINGS_FILE_NAME = "settings.properties"
private const val LOGS_DIRECTORY_NAME = "logs"

/** [appDataDirectory] is the per-user application data folder; logs and settings live inside it. */
fun loreDesignerModules(appDataDirectory: Path): List<Module> = listOf(
    loggingModule(appDataDirectory.resolve(LOGS_DIRECTORY_NAME)),
    workspaceModule(),
    settingsModule(appDataDirectory),
)

private fun loggingModule(logsDirectory: Path): Module = module {
    single { createLoreDesignerLogging(logsDirectory) }
    single<Logger> { get<LoreDesignerLogging>().logger }
    single { get<LoreDesignerLogging>().diagnostics }
}

private fun workspaceModule(): Module = module {
    single<WorkspaceStore> { FileSystemWorkspaceStore(FileSystem.SYSTEM) }
    single<WorkspaceIdGenerator> { UuidV7WorkspaceIdGenerator() }
    factory { CreateWorkspace(get(), get()) }
    factory { OpenWorkspace(get()) }
    factory { UpdateProjectConfig(get()) }
}

private fun settingsModule(appDataDirectory: Path): Module = module {
    single<ApplicationSettingsStore> {
        fileBackedApplicationSettingsStore(
            file = appDataDirectory.resolve(SETTINGS_FILE_NAME).toOkioPath(),
            logger = get<Logger>().withTag("Settings"),
        )
    }
    single { ApplicationSettingsRepository(get(), CoroutineScope(SupervisorJob() + Dispatchers.Default)) }
    single { SettingsPageRegistry(builtInSettingsPages()) }
    single { DesktopFolderOpener(get<Logger>().withTag("System")) }
    single<SettingsEnvironment> {
        DesktopSettingsEnvironment(appDataDirectory, appDataDirectory.resolve(LOGS_DIRECTORY_NAME), get(), get())
    }
    single<SettingsViewModelFactory> {
        SettingsViewModelFactory { workspace -> SettingsViewModel(get(), get(), workspace, get(), get()) }
    }
}
