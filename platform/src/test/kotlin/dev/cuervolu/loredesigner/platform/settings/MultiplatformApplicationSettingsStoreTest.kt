package dev.cuervolu.loredesigner.platform.settings

import com.russhwolf.settings.MapSettings
import dev.cuervolu.loredesigner.core.settings.AppLanguage
import dev.cuervolu.loredesigner.core.settings.ApplicationSettings
import dev.cuervolu.loredesigner.core.settings.AutosaveInterval
import dev.cuervolu.loredesigner.core.settings.DocumentFont
import dev.cuervolu.loredesigner.core.settings.RecoveryRetention
import dev.cuervolu.loredesigner.core.settings.ThemePreference
import dev.cuervolu.loredesigner.core.settings.UiDensity
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MultiplatformApplicationSettingsStoreTest {
    private val backing = MapSettings()
    private val store = MultiplatformApplicationSettingsStore(backing)

    private val customized = ApplicationSettings(
        theme = ThemePreference.DARK,
        density = UiDensity.COMFORTABLE,
        language = AppLanguage.SPANISH,
        autosave = false,
        spellcheck = false,
        documentFont = DocumentFont.JetBrainsMono,
        autosaveInterval = AutosaveInterval.EVERY_5_MINUTES,
        keepRecoveryCopies = false,
        recoveryRetention = RecoveryRetention.DAYS_30,
        diagnosticLogging = true,
        showHiddenFilesIn = setOf(
            WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb380"),
            WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb381"),
        ),
    )

    @Test
    fun `an empty store reads the defaults`() {
        assertEquals(ApplicationSettings(), store.read())
    }

    @Test
    fun `every field round-trips`() {
        store.write(customized)

        assertEquals(customized, MultiplatformApplicationSettingsStore(backing).read())
    }

    @Test
    fun `writing defaults over an empty store writes nothing`() {
        store.write(ApplicationSettings())

        assertTrue(backing.keys.isEmpty())
    }

    @Test
    fun `values are stored under stable names`() {
        store.write(
            ApplicationSettings(theme = ThemePreference.LIGHT, autosaveInterval = AutosaveInterval.EVERY_30_SECONDS),
        )

        assertEquals("light", backing.getStringOrNull("appearance.theme"))
        assertEquals("every-30-seconds", backing.getStringOrNull("files.autosaveInterval"))
    }

    @Test
    fun `unknown or damaged values fall back to defaults per field`() {
        backing.putString("appearance.theme", "sepia")
        backing.putString("editor.autosave", "maybe")
        backing.putString("editor.documentFont", "comic-sans")
        backing.putString("projects.showHiddenFiles", "not-a-uuid,01995f7e-1d74-7c83-a8a9-4fd2ed9cb380")
        backing.putString("language.application", "spanish")

        val read = store.read()

        assertEquals(ThemePreference.SYSTEM, read.theme)
        assertEquals(true, read.autosave)
        assertEquals(DocumentFont.Default, read.documentFont)
        assertEquals(setOf(WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb380")), read.showHiddenFilesIn)
        assertEquals(AppLanguage.SPANISH, read.language)
    }
}
