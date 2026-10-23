package dev.cuervolu.loredesigner.ui.chrome

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.app_name
import dev.cuervolu.loredesigner.ui.resources.titlebar_search
import dev.cuervolu.loredesigner.ui.resources.titlebar_settings
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AppTitleBarTest {
    private val barWidth = 1000.dp

    private fun androidx.compose.ui.test.ComposeUiTest.show(content: @androidx.compose.runtime.Composable () -> Unit) =
        setContent {
            LoreDesignerTheme(darkTheme = false) {
                Box(Modifier.width(barWidth)) { content() }
            }
        }

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.centerX(): Dp =
        getUnclippedBoundsInRoot().let { (it.left + it.right) / 2 }

    @Test
    fun `settings button invokes callback`() = runComposeUiTest {
        // Resolved from resources so the test follows whatever locale the machine runs in.
        val settings = getString(Res.string.titlebar_settings)
        var clicks = 0
        show { AppTitleBar(actions = { TitleBarSettingsButton(onClick = { clicks++ }) }) }

        onNodeWithContentDescription(settings).performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `brand is only shown when provided`() = runComposeUiTest {
        val appName = getString(Res.string.app_name)
        val withBrand = androidx.compose.runtime.mutableStateOf(false)
        show { AppTitleBar(brand = if (withBrand.value) ({ TitleBarBrand() }) else null) }

        onNodeWithText(appName).assertDoesNotExist()
        withBrand.value = true
        waitForIdle()
        onNodeWithText(appName).assertExists()
    }

    @Test
    fun `search button stays inert without a handler`() = runComposeUiTest {
        show { AppTitleBar(actions = { TitleBarSearchButton() }) }

        onNodeWithText(getString(Res.string.titlebar_search)).assertExists()
    }

    @Test
    fun `window controls slot is rendered`() = runComposeUiTest {
        show { AppTitleBar(windowControls = { Box(Modifier.testTag("controls")) }) }

        onNodeWithTag("controls").assertExists()
    }

    @Test
    fun `title sits on the bar center whatever the platform inset`() = runComposeUiTest {
        show {
            AppTitleBar(
                contentPadding = PaddingValues(start = 80.dp),
                title = { Box(Modifier.size(100.dp, 20.dp).testTag("title")) },
                actions = { Box(Modifier.size(40.dp, 20.dp)) },
            )
        }

        assertEquals(barWidth / 2, onNodeWithTag("title").centerX())
    }

    @Test
    fun `actions are pinned to the end of the bar`() = runComposeUiTest {
        show {
            AppTitleBar(
                title = { Box(Modifier.size(100.dp, 20.dp)) },
                actions = { Box(Modifier.size(40.dp, 20.dp).testTag("actions")) },
            )
        }

        assertEquals(barWidth, onNodeWithTag("actions").getUnclippedBoundsInRoot().right)
    }

    @Test
    fun `title gives way to a wide start slot instead of overlapping it`() = runComposeUiTest {
        show {
            AppTitleBar(
                menuBar = { Box(Modifier.size(700.dp, 20.dp)) },
                title = { Box(Modifier.size(100.dp, 20.dp).testTag("title")) },
                actions = { Box(Modifier.size(40.dp, 20.dp)) },
            )
        }

        val title = onNodeWithTag("title").getUnclippedBoundsInRoot()
        assertTrue(title.left >= 700.dp, "title starts at ${title.left}, inside the start slot")
        assertTrue(title.right <= barWidth - 40.dp, "title ends at ${title.right}, inside the end slot")
    }
}
