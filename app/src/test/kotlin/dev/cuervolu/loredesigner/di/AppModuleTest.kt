package dev.cuervolu.loredesigner.di

import dev.cuervolu.loredesigner.ScratchDirectory
import dev.cuervolu.loredesigner.core.settings.ThemePreference
import dev.cuervolu.loredesigner.ui.settings.SettingsEnvironment
import dev.cuervolu.loredesigner.ui.settings.data.ApplicationSettingsRepository
import org.koin.dsl.koinApplication
import org.koin.test.check.checkModules
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AppModuleTest {
    @Test
    fun `every definition in the production graph can be built`() {
        ScratchDirectory().use { scratch ->
            koinApplication { modules(loreDesignerModules(scratch.root)) }.checkModules()
        }
    }

    @Test
    fun `startup reads storage without creating files`() {
        ScratchDirectory().use { scratch ->
            val koin = koinApplication { modules(loreDesignerModules(scratch.root)) }.koin

            assertEquals(scratch.root.toString(), koin.get<SettingsEnvironment>().applicationDataDirectory)
            assertEquals(ThemePreference.SYSTEM, koin.get<ApplicationSettingsRepository>().settings.value.theme)
            assertFalse(scratch.root.resolve("settings.properties").exists(), "Reading must not create the file")
            assertFalse(scratch.root.resolve("workspaces.json").exists(), "Startup must not create the registry")
            koin.close()
        }
    }

    @Test
    fun `logs are written inside the application data directory`() {
        ScratchDirectory().use { scratch ->
            val appData = scratch.root.resolve("data").createDirectories()
            val koin = koinApplication { modules(loreDesignerModules(appData)) }.koin

            assertEquals(appData.resolve("logs").toString(), koin.get<SettingsEnvironment>().logsDirectory)
            koin.close()
        }
    }
}
