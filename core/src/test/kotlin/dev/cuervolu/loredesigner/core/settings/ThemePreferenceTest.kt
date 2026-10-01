package dev.cuervolu.loredesigner.core.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemePreferenceTest {
    @Test
    fun `system follows the operating system`() {
        assertEquals(true, ThemePreference.SYSTEM.isDark(systemDark = true))
        assertEquals(false, ThemePreference.SYSTEM.isDark(systemDark = false))
    }

    @Test
    fun `explicit preferences ignore the operating system`() {
        for (systemDark in listOf(true, false)) {
            assertEquals(false, ThemePreference.LIGHT.isDark(systemDark))
            assertEquals(true, ThemePreference.DARK.isDark(systemDark))
        }
    }
}
