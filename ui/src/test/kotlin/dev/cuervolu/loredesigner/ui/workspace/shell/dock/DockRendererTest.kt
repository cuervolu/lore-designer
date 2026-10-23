package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import com.composeunstyled.Text
import dev.cuervolu.loredesigner.editor.dock.DockOperation
import dev.cuervolu.loredesigner.editor.dock.DockResult
import dev.cuervolu.loredesigner.editor.dock.DockTarget
import dev.cuervolu.loredesigner.editor.dock.PanelId
import dev.cuervolu.loredesigner.editor.dock.PanelKind
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DockRendererTest {
    private val registry: PanelRegistry = ShellPanels.Sizing.mapValues { (id, sizing) ->
        DockPanelSpec(
            label = { id.value },
            content = { Box(Modifier.fillMaxSize().testTag("content-${id.value}")) { Text("body ${id.value}") } },
            sizing = sizing,
        )
    }

    private fun androidx.compose.ui.test.ComposeUiTest.show(
        state: WorkspaceDockState,
        width: Int = 1280,
        height: Int = 800,
        registry: PanelRegistry = this@DockRendererTest.registry,
    ) = setContent {
        LoreDesignerTheme(darkTheme = false) {
            Box(Modifier.size(width.dp, height.dp)) {
                DockHost(state, registry, viewEmpty = {
                    Box(Modifier.fillMaxSize().testTag("editor-area")) { Text("empty editor") }
                })
            }
        }
    }

    @Test
    fun `every visible panel and the empty editor are rendered`() = runComposeUiTest {
        show(WorkspaceDockState(initiallyHidden = emptySet()))

        listOf("explorer", "properties", "log").forEach { onNodeWithTag("content-$it").assertExists() }
        onNodeWithText("empty editor").assertExists()
    }

    @Test
    fun `hidden panels are not composed and reappear when shown`() = runComposeUiTest {
        val state = WorkspaceDockState()
        show(state)

        onNodeWithTag("content-log").assertDoesNotExist()
        state.toggleBottomPanel()
        waitForIdle()
        onNodeWithTag("content-log").assertExists()
    }

    @Test
    fun `side panels keep their preferred width while the editor absorbs the window size`() {
        listOf(960, 1000).forEach { width ->
            runComposeUiTest {
                show(WorkspaceDockState(), width = width)

                assertEquals(264.dp, onNodeWithTag("content-explorer").getUnclippedBoundsInRoot().width)
                assertEquals(300.dp, onNodeWithTag("content-properties").getUnclippedBoundsInRoot().width)
                assertEquals(
                    width.dp - 264.dp - 300.dp - 2.dp,
                    onNodeWithTag("editor-area").getUnclippedBoundsInRoot().width,
                )
            }
        }
    }

    @Test
    fun `a narrow window collapses properties before explorer`() = runComposeUiTest {
        show(WorkspaceDockState(), width = 700)

        onNodeWithTag("content-properties").assertDoesNotExist()
        onNodeWithTag("content-explorer").assertExists()
        onNodeWithText("empty editor").assertExists()
    }

    @Test
    fun `clicking a tab switches the displayed panel of its group only`() = runComposeUiTest {
        val extra = PanelId("extra")
        val base = WorkspaceDockState.defaultLayout()
        val group = checkNotNull(base.groupOf(ShellPanels.Explorer)).id
        val layout = (
            base.apply(
                DockOperation.AddPanel(extra, PanelKind.Tool, DockTarget.Tab(group)),
            ) as DockResult.Applied
            ).layout
        val state = WorkspaceDockState(layout)
        val withExtra = registry + (
            extra to DockPanelSpec(
                label = { "extra" },
                content = { Box(Modifier.testTag("content-extra")) },
                sizing = ShellPanels.Sizing.getValue(ShellPanels.Explorer),
            )
            )
        show(state, registry = withExtra)

        onNodeWithTag("content-extra").assertExists()
        onNodeWithTag("content-explorer").assertDoesNotExist()
        onNodeWithTag("content-properties").assertExists()

        onNodeWithText("explorer").performClick()
        waitForIdle()

        onNodeWithTag("content-explorer").assertExists()
        onNodeWithTag("content-extra").assertDoesNotExist()
        assertEquals(ShellPanels.Explorer, state.layout.groupOf(ShellPanels.Explorer)?.activePanel)
    }
}
