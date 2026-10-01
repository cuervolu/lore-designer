package dev.cuervolu.loredesigner.ui.i18n

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import dev.cuervolu.loredesigner.core.settings.AppLanguage
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LocalAppLocaleTest {
    private val original: Locale = Locale.getDefault()

    @AfterTest
    fun restore() {
        Locale.setDefault(original)
    }

    @Test
    fun `explicit languages override the default and system restores it`() = runComposeUiTest {
        var language by mutableStateOf(AppLanguage.SPANISH)
        val seen = mutableListOf<String>()
        var compositions = 0

        setContent {
            ProvideAppLocale(language) {
                seen += LocalAppLocale.current
                compositions++
            }
        }
        waitForIdle()
        assertEquals("es", Locale.getDefault().language)
        assertEquals("es", seen.last())

        language = AppLanguage.ENGLISH
        waitForIdle()
        assertEquals("en", Locale.getDefault().language)
        assertEquals("en", seen.last())

        language = AppLanguage.SYSTEM
        waitForIdle()
        assertEquals(original, Locale.getDefault())
        assertEquals(3, compositions)
    }
}
