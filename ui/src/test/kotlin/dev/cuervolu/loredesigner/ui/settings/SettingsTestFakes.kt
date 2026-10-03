package dev.cuervolu.loredesigner.ui.settings

import dev.cuervolu.loredesigner.core.settings.ApplicationSettings
import dev.cuervolu.loredesigner.core.settings.ApplicationSettingsStore
import dev.cuervolu.loredesigner.ui.RecordingLogWriter
import dev.cuervolu.loredesigner.ui.launcher.FakeWorkspaceStore
import dev.cuervolu.loredesigner.ui.settings.data.ApplicationSettingsRepository
import dev.cuervolu.loredesigner.ui.settings.pages.builtInSettingsPages
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class FakeApplicationSettingsStore(var stored: ApplicationSettings = ApplicationSettings()) :
    ApplicationSettingsStore {
    val writes = mutableListOf<ApplicationSettings>()

    /** Thrown by the next write instead of storing it. */
    var nextWriteFailure: Exception? = null

    override fun read(): ApplicationSettings = stored

    override fun write(settings: ApplicationSettings) {
        nextWriteFailure?.let { failure ->
            nextWriteFailure = null
            throw failure
        }
        writes += settings
        stored = settings
    }
}

internal class FakeSettingsEnvironment : SettingsEnvironment {
    override val applicationDataDirectory = "/data/lore"
    override val logsDirectory = "/data/lore/logs"
    var openSucceeds = true
    val openedFolders = mutableListOf<String>()
    val diagnosticLoggingChanges = mutableListOf<Boolean>()

    override suspend fun openFolder(directory: String): Boolean {
        openedFolders += directory
        return openSucceeds
    }

    override fun setDiagnosticLogging(enabled: Boolean) {
        diagnosticLoggingChanges += enabled
    }
}

/** Everything a Settings modal needs, wired with fakes; writes run on [ioDispatcher]. */
internal class SettingsTestHarness(
    initial: ApplicationSettings = ApplicationSettings(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
    ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
) {
    val store = FakeApplicationSettingsStore(initial)
    val repository = ApplicationSettingsRepository(store, scope, RecordingLogWriter().logger(), ioDispatcher)
    val registry = SettingsPageRegistry(builtInSettingsPages())
    val environment = FakeSettingsEnvironment()
    val workspaceStore = FakeWorkspaceStore()

    val factory = SettingsViewModelFactory { workspace ->
        SettingsViewModel(repository, registry, workspace, workspaceStore.updateProjectConfig(), environment)
    }
}
