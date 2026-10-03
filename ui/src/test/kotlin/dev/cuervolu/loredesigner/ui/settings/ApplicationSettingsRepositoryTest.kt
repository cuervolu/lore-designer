package dev.cuervolu.loredesigner.ui.settings

import co.touchlab.kermit.Severity
import dev.cuervolu.loredesigner.core.settings.ApplicationSettings
import dev.cuervolu.loredesigner.core.settings.ThemePreference
import dev.cuervolu.loredesigner.ui.RecordingLogWriter
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
import kotlin.test.assertSame

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationSettingsRepositoryTest {
    private val logs = RecordingLogWriter()

    @Test
    fun `initial value is read synchronously from the store`() {
        val store = FakeApplicationSettingsStore(ApplicationSettings(theme = ThemePreference.DARK))
        val repository = ApplicationSettingsRepository(store, TestScope(), logs.logger())

        assertEquals(ThemePreference.DARK, repository.settings.value.theme)
    }

    @Test
    fun `updates apply immediately and rapid changes collapse into the latest write`() = runTest {
        val store = FakeApplicationSettingsStore()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val writer = CoroutineScope(dispatcher)
        val repository = ApplicationSettingsRepository(store, writer, logs.logger(), dispatcher)

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
        val repository = ApplicationSettingsRepository(store, writer, logs.logger(), dispatcher)

        repository.update { it }
        advanceUntilIdle()

        assertEquals(emptyList(), store.writes)
        writer.cancel()
    }

    @Test
    fun `a failed write is logged and later changes are still written`() = runTest {
        val store = FakeApplicationSettingsStore()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val writer = CoroutineScope(dispatcher)
        val repository = ApplicationSettingsRepository(store, writer, logs.logger(), dispatcher)
        val failure = IllegalStateException("store rejected the write")
        store.nextWriteFailure = failure

        repository.update { it.copy(theme = ThemePreference.LIGHT) }
        advanceUntilIdle()
        repository.update { it.copy(theme = ThemePreference.DARK) }
        advanceUntilIdle()

        assertEquals(listOf(ApplicationSettings(theme = ThemePreference.DARK)), store.writes)
        assertSame(failure, logs.at(Severity.Error).single().throwable)
        writer.cancel()
    }
}
