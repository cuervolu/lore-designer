package dev.cuervolu.loredesigner.ui.settings

import dev.cuervolu.loredesigner.core.settings.ApplicationSettings
import dev.cuervolu.loredesigner.core.settings.ThemePreference
import dev.cuervolu.loredesigner.ui.settings.data.ApplicationSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationSettingsRepositoryTest {
    @Test
    fun `initial value is read synchronously from the store`() {
        val store = FakeApplicationSettingsStore(ApplicationSettings(theme = ThemePreference.DARK))
        val repository = ApplicationSettingsRepository(store, TestScope())

        assertEquals(ThemePreference.DARK, repository.settings.value.theme)
    }

    @Test
    fun `updates apply immediately and rapid changes collapse into the latest write`() = runTest {
        val store = FakeApplicationSettingsStore()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val writer = CoroutineScope(dispatcher)
        val repository = ApplicationSettingsRepository(store, writer, dispatcher)

        repository.update { it.copy(theme = ThemePreference.LIGHT) }
        repository.update { it.copy(theme = ThemePreference.DARK) }
        assertEquals(ThemePreference.DARK, repository.settings.value.theme)
        advanceUntilIdle()

        assertEquals(listOf(ApplicationSettings(theme = ThemePreference.DARK)), store.writes)
        writer.cancel()
    }

    @Test
    fun `updates that change nothing are not written`() = runTest {
        val store = FakeApplicationSettingsStore()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val writer = CoroutineScope(dispatcher)
        val repository = ApplicationSettingsRepository(store, writer, dispatcher)

        repository.update { it }
        advanceUntilIdle()

        assertEquals(emptyList(), store.writes)
        writer.cancel()
    }
}
