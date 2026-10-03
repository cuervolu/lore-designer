package dev.cuervolu.loredesigner.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class LoreDesignerThemeTest {

    @Test
    fun `light and dark palettes define the same tokens`() {
        assertEquals(LightColors.keys, DarkColors.keys)
    }

    @Test
    fun `every project color maps to tokens defined in both palettes`() {
        for (color in ProjectColor.entries) {
            for (token in listOf(color.dotToken(), color.tintToken())) {
                assert(token in LightColors) { "$token missing from light palette" }
                assert(token in DarkColors) { "$token missing from dark palette" }
            }
        }
    }

    @Test
    fun `project colors resolve to distinct dots`() {
        val dots = ProjectColor.entries.map { LightColors.getValue(it.dotToken()) }
        assertEquals(dots.size, dots.toSet().size)
    }

    @Test
    fun `theme resolves the palette of the requested scheme`() = runComposeUiTest {
        var darkTheme by mutableStateOf(false)
        var background = Color.Unspecified
        var accent = Color.Unspecified

        setContent {
            LoreDesignerTheme(darkTheme = darkTheme) {
                background = Theme[colors][LoreColors.background]
                accent = Theme[colors][LoreColors.accent]
            }
        }

        assertEquals(Color(0xFFF5F5F7), background)
        assertEquals(Color(0xFF7F73CC), accent)

        darkTheme = true
        // The scheme change animates; skip to its end state.
        mainClock.advanceTimeBy(1_000)
        waitForIdle()

        assertEquals(Color(0xFF18181D), background)
        assertEquals(Color(0xFF9B90DD), accent)
    }

    @Test
    fun `every color token is readable through the theme in both schemes`() = runComposeUiTest {
        var darkTheme by mutableStateOf(false)
        val resolved = mutableMapOf<Boolean, Map<String, Color>>()

        setContent {
            LoreDesignerTheme(darkTheme = darkTheme) {
                resolved[darkTheme] = LightColors.keys.associate { it.name to Theme[colors][it] }
            }
        }
        darkTheme = true
        mainClock.advanceTimeBy(1_000)
        waitForIdle()

        assertEquals(LightColors.size, resolved.getValue(false).size)
        assertEquals(DarkColors.size, resolved.getValue(true).size)
        assertNotEquals(resolved.getValue(false), resolved.getValue(true))
    }

    @Test
    fun `shared tokens resolve regardless of scheme`() = runComposeUiTest {
        var titleBarHeight = androidx.compose.ui.unit.Dp.Unspecified
        setContent {
            LoreDesignerTheme(darkTheme = true) {
                titleBarHeight = Theme[sizes][LoreSizes.titleBarHeight]
                Theme[typography][LoreTypography.body]
                Theme[shapes][LoreShapes.control]
                Theme[spacing][LoreSpacing.space4]
            }
        }
        assertEquals(40f, titleBarHeight.value)
    }

    @Test
    fun `clickable falls back to the theme indication without crashing`() = runComposeUiTest {
        var clicks = 0
        setContent {
            LoreDesignerTheme(darkTheme = false) {
                Text("Tap", Modifier.clickable { clicks++ })
            }
        }

        onNodeWithText("Tap").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `right-clicking selectable text opens the context menu without crashing`() = runComposeUiTest {
        setContent {
            LoreDesignerTheme(darkTheme = true) {
                SelectionContainer { Text("/home/writer/.local/share/lore") }
            }
        }

        onNodeWithText("/home/writer/.local/share/lore").performMouseInput { rightClick() }
        waitForIdle()

        onNodeWithText("/home/writer/.local/share/lore").assertExists()
    }
}
