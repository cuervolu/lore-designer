package dev.cuervolu.loredesigner.di

import dev.cuervolu.loredesigner.core.settings.ThemePreference
import dev.cuervolu.loredesigner.ui.settings.SettingsEnvironment
import dev.cuervolu.loredesigner.ui.settings.SettingsPageRegistry
import dev.cuervolu.loredesigner.ui.settings.SettingsViewModelFactory
import dev.cuervolu.loredesigner.ui.settings.data.ApplicationSettingsRepository
import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.OpenWorkspace
import dev.cuervolu.loredesigner.workspace.UpdateProjectConfig
import dev.cuervolu.loredesigner.workspace.WorkspaceOpener
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspacesRegistry
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class AppModuleTest {
    @Test
    fun `workspace and settings dependencies resolve from dependency injection`() {
        val appDataDirectory = createTempDirectory("app-module-test")
        try {
            val koin = startKoin { modules(loreDesignerModules(appDataDirectory)) }.koin

            assertNotNull(koin.get<CreateWorkspace>())
            assertNotNull(koin.get<OpenWorkspace>())
            assertNotNull(koin.get<UpdateProjectConfig>())
            assertNotNull(koin.get<SettingsPageRegistry>())
            assertNotNull(koin.get<SettingsViewModelFactory>())
            assertEquals(appDataDirectory.toString(), koin.get<SettingsEnvironment>().applicationDataDirectory)
            assertEquals(ThemePreference.SYSTEM, koin.get<ApplicationSettingsRepository>().settings.value.theme)
            assertFalse(appDataDirectory.resolve("settings.properties").exists(), "Reading must not create the file")
            assertNotNull(koin.get<WorkspaceOpener>())
            assertNotNull(koin.get<RecentWorkspacesRegistry>())
            assertFalse(appDataDirectory.resolve("workspaces.json").exists(), "Startup must not create the registry")
        } finally {
            stopKoin()
            appDataDirectory.toFile().deleteRecursively()
        }
    }
}
