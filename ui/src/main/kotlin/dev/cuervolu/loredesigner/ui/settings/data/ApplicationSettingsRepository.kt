package dev.cuervolu.loredesigner.ui.settings.data

import dev.cuervolu.loredesigner.core.settings.ApplicationSettings
import dev.cuervolu.loredesigner.core.settings.ApplicationSettingsStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

/**
 * Observable application settings for the whole process.
 *
 * The store is read synchronously on construction so the first frame already uses the saved theme
 * and language. Changes apply in memory immediately; disk writes run on [ioDispatcher], one at a
 * time, and rapid changes collapse into a single write of the latest value.
 */
class ApplicationSettingsRepository(
    private val store: ApplicationSettingsStore,
    scope: CoroutineScope,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val state = MutableStateFlow(store.read())
    val settings: StateFlow<ApplicationSettings> = state.asStateFlow()

    private val pendingWrites = Channel<ApplicationSettings>(Channel.CONFLATED)

    init {
        scope.launch(ioDispatcher) {
            for (settings in pendingWrites) store.write(settings)
        }
    }

    fun update(transform: (ApplicationSettings) -> ApplicationSettings) {
        val previous = state.value
        val next = state.updateAndGet(transform)
        if (next != previous) pendingWrites.trySend(next)
    }
}
