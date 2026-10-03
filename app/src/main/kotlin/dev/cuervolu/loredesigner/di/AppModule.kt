package dev.cuervolu.loredesigner.di

import dev.cuervolu.loredesigner.platform.di.logsDirectoryIn
import dev.cuervolu.loredesigner.platform.di.platformModules
import dev.cuervolu.loredesigner.settings.DesktopSettingsEnvironment
import dev.cuervolu.loredesigner.ui.di.uiModule
import dev.cuervolu.loredesigner.ui.settings.SettingsEnvironment
import dev.cuervolu.loredesigner.workspace.di.workspaceModule
import org.koin.core.module.Module
import org.koin.dsl.module
import java.nio.file.Path

/** [appDataDirectory] is the per-user application data folder; logs and settings live inside it. */
fun loreDesignerModules(appDataDirectory: Path): List<Module> =
    platformModules(appDataDirectory) + workspaceModule() + uiModule() + desktopModule(appDataDirectory)

private fun desktopModule(appDataDirectory: Path): Module = module {
    single<SettingsEnvironment> {
        DesktopSettingsEnvironment(appDataDirectory, logsDirectoryIn(appDataDirectory), get(), get())
    }
}
