package dev.cuervolu.loredesigner.platform.settings

import dev.cuervolu.loredesigner.core.settings.AppLanguage
import dev.cuervolu.loredesigner.core.settings.ApplicationSettings
import dev.cuervolu.loredesigner.core.settings.ThemePreference
import dev.cuervolu.loredesigner.platform.logging.RecordingLogWriter
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PropertiesFileSettingsTest {
    private val logs = RecordingLogWriter()
    private val root: Path = createTempDirectory("settings-test").toOkioPath()
    private val file = root / "app-data" / "settings.properties"

    @AfterTest
    fun cleanUp() {
        FileSystem.SYSTEM.deleteRecursively(root)
    }

    private fun newStore() = MultiplatformApplicationSettingsStore(
        PropertiesFileSettings.create(file, logger = logs.logger()),
        logs.logger(),
    )

    @Test
    fun `missing file yields defaults and is not created by reading`() {
        assertEquals(ApplicationSettings(), newStore().read())

        assertFalse(FileSystem.SYSTEM.exists(file))
    }

    @Test
    fun `writing creates the file and its parent directory`() {
        newStore().write(ApplicationSettings(theme = ThemePreference.DARK))

        assertTrue(FileSystem.SYSTEM.exists(file))
        assertTrue(FileSystem.SYSTEM.read(file) { readUtf8() }.contains("appearance.theme=dark"))
        assertEquals(listOf(file), FileSystem.SYSTEM.list(file.parent!!), "Temporary files were left behind")
    }

    @Test
    fun `values survive recreating the store from the same file`() {
        val saved = ApplicationSettings(theme = ThemePreference.LIGHT, language = AppLanguage.SPANISH, autosave = false)
        newStore().write(saved)

        assertEquals(saved, newStore().read())
    }

    @Test
    fun `a malformed file is read as defaults and replaced on the next write`() {
        FileSystem.SYSTEM.createDirectories(file.parent!!)
        FileSystem.SYSTEM.write(file) { writeUtf8("appearance.theme=\\uZZZZ\n") }

        val store = newStore()
        assertEquals(ApplicationSettings(), store.read())
        store.write(ApplicationSettings(theme = ThemePreference.DARK))

        assertEquals(ThemePreference.DARK, newStore().read().theme)
    }
}
