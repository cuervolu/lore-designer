package dev.cuervolu.loredesigner

import dev.cuervolu.loredesigner.core.settings.AppLanguage
import dev.cuervolu.loredesigner.core.settings.ApplicationSettingsStore
import dev.cuervolu.loredesigner.ui.settings.data.ApplicationSettingsRepository
import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.Workspace
import dev.cuervolu.loredesigner.workspace.WorkspaceOpener
import dev.cuervolu.loredesigner.workspace.WorkspaceResult
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspacesRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okio.Path.Companion.toOkioPath
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

/** Runs the real storage stack, stopping and restarting it as a user quitting and relaunching would. */
class AppAssemblyTest {
    @Test
    fun `a project and the settings survive a restart`() = runBlocking {
        ScratchDirectory().use { scratch ->
            val appData = scratch.root.resolve("appdata").createDirectories()
            val projects = scratch.root.resolve("projects").createDirectories()

            val created = AssembledApp.start(appData).use { app ->
                val workspace = assertIs<WorkspaceResult.Success<Workspace>>(
                    app.koin.get<CreateWorkspace>()(projects.toOkioPath(), "Embercourt"),
                ).value
                app.koin.get<WorkspaceOpener>().recordOpened(workspace)
                app.koin.get<ApplicationSettingsRepository>().update { it.copy(language = AppLanguage.SPANISH) }
                awaitSettingsOnDisk(app.koin.get(), AppLanguage.SPANISH)
                workspace
            }
            assertTrue(projects.resolve("Embercourt/project.lore").exists(), "The project file must be on disk")

            AssembledApp.start(appData).use { app ->
                val registry = app.koin.get<RecentWorkspacesRegistry>()
                registry.initialize()
                val remembered = registry.workspaces.value.single()
                assertEquals(created.config.id, remembered.id)
                assertEquals("Embercourt", remembered.lastKnownName)

                val reopened = assertIs<WorkspaceResult.Success<Workspace>>(
                    app.koin.get<WorkspaceOpener>().open(created.location, expectedId = created.config.id),
                ).value
                assertEquals(created, reopened)
                assertEquals(AppLanguage.SPANISH, app.koin.get<ApplicationSettingsRepository>().settings.value.language)
            }
        }
    }

    // Settings are written asynchronously, so the restart must wait for the file rather than the call.
    private suspend fun awaitSettingsOnDisk(store: ApplicationSettingsStore, language: AppLanguage) {
        withTimeout(10_000.milliseconds) {
            while (store.read().language != language) delay(20.milliseconds)
        }
    }
}
