package dev.cuervolu.loredesigner.ui.chrome

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.app_name
import dev.cuervolu.loredesigner.ui.resources.titlebar_settings
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LauncherTitleBarTest {

    @Test
    fun `settings button invokes callback`() = runComposeUiTest {
        // Resolved from resources so the test follows whatever locale the machine runs in.
        val settings = getString(Res.string.titlebar_settings)
        var clicks = 0
        setContent {
            LoreDesignerTheme(darkTheme = false) {
                LauncherTitleBar(onSettingsClick = { clicks++ })
            }
        }

        onNodeWithContentDescription(settings).performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `settings button is hidden without a callback`() = runComposeUiTest {
        val settings = getString(Res.string.titlebar_settings)
        val appName = getString(Res.string.app_name)
        setContent {
            LoreDesignerTheme(darkTheme = false) {
                LauncherTitleBar(onSettingsClick = null)
            }
        }

        onAllNodesWithContentDescription(settings).assertCountEquals(0)
        onNodeWithText(appName).assertExists()
    }

    @Test
    fun `window controls slot is rendered`() = runComposeUiTest {
        setContent {
            LoreDesignerTheme(darkTheme = true) {
                LauncherTitleBar(
                    onSettingsClick = null,
                    windowControls = { Box(Modifier.testTag("controls")) },
                )
            }
        }

        onNodeWithTag("controls").assertExists()
    }
}
