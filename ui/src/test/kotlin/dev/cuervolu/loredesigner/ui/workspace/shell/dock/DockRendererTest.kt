package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import com.composeunstyled.Text
import dev.cuervolu.loredesigner.editor.dock.DockOperation
import dev.cuervolu.loredesigner.editor.dock.DockResult
import dev.cuervolu.loredesigner.editor.dock.DockSide
import dev.cuervolu.loredesigner.editor.dock.DockTarget
import dev.cuervolu.loredesigner.editor.dock.PanelId
import dev.cuervolu.loredesigner.editor.dock.PanelKind
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        width: Int = 1000,
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

    private fun androidx.compose.ui.test.ComposeUiTest.drag(tag: String, dx: Float, dy: Float = 0f) {
        onNodeWithTag(tag).performMouseInput {
            moveTo(center)
            press()
            moveBy(Offset(dx / 2, dy / 2))
            moveBy(Offset(dx / 2, dy / 2))
            release()
        }
        waitForIdle()
    }

    private fun WorkspaceDockState.separator(panel: PanelId) =
        "dock-separator-${checkNotNull(layout.groupOf(panel)).id.value}"

    private fun androidx.compose.ui.test.ComposeUiTest.width(tag: String) =
        onNodeWithTag(tag).getUnclippedBoundsInRoot().width

    @Test
    fun `dragging the explorer separator resizes it and the editor absorbs the change`() = runComposeUiTest {
        val state = WorkspaceDockState()
        show(state)
        val properties = width("content-properties")

        drag(state.separator(ShellPanels.Explorer), dx = 50f)

        assertEquals(314.dp, width("content-explorer"))
        assertEquals(properties, width("content-properties"))
        assertEquals(1000.dp - 314.dp - 300.dp - 2.dp, width("editor-area"))
    }

    @Test
    fun `the separator hit zone is nine pixels wide and the layout is unchanged by it`() = runComposeUiTest {
        val state = WorkspaceDockState()
        show(state)

        assertEquals(9.dp, width(state.separator(ShellPanels.Explorer)))
        assertEquals(264.dp, width("content-explorer"))
    }

    @Test
    fun `dragging never leaves the minimum size`() = runComposeUiTest {
        val state = WorkspaceDockState()
        show(state)

        drag(state.separator(ShellPanels.Explorer), dx = -90f)

        assertEquals(180.dp, width("content-explorer"))
        assertEquals(true, state.explorerVisible)
    }

    @Test
    fun `dragging never starves the editor below its minimum`() = runComposeUiTest {
        val state = WorkspaceDockState()
        show(state, width = 1000)

        drag(state.separator(ShellPanels.Explorer), dx = 900f)

        assertEquals(1000.dp - 360.dp - 180.dp - 2.dp, width("content-explorer"))
        assertEquals(360.dp, width("editor-area"))
    }

    @Test
    fun `double clicking a separator restores the default size`() = runComposeUiTest {
        val state = WorkspaceDockState()
        show(state)
        drag(state.separator(ShellPanels.Explorer), dx = 90f)

        onNodeWithTag(state.separator(ShellPanels.Explorer)).performMouseInput { doubleClick() }
        waitForIdle()

        assertEquals(264.dp, width("content-explorer"))
    }

    @Test
    fun `dragging below seventy percent of the minimum hides the panel on release and keeps its place`() =
        runComposeUiTest {
            val state = WorkspaceDockState()
            val layout = state.layout
            show(state)
            val separator = state.separator(ShellPanels.Explorer)

            onNodeWithTag(separator).performMouseInput {
                moveTo(center)
                press()
                moveBy(Offset(-200f, 0f))
            }
            waitForIdle()
            assertEquals(true, state.explorerVisible)
            assertEquals(180.dp, width("content-explorer"))

            onNodeWithTag(separator).performMouseInput { release() }
            waitForIdle()

            assertEquals(false, state.explorerVisible)
            assertEquals(layout, state.layout)
            onNodeWithTag("content-explorer").assertDoesNotExist()
        }

    @Test
    fun `returning above the threshold before releasing cancels the collapse`() = runComposeUiTest {
        val state = WorkspaceDockState()
        show(state)

        onNodeWithTag(state.separator(ShellPanels.Explorer)).performMouseInput {
            moveTo(center)
            press()
            moveBy(Offset(-200f, 0f))
            moveBy(Offset(150f, 0f))
            release()
        }
        waitForIdle()

        assertEquals(true, state.explorerVisible)
        assertEquals(214.dp, width("content-explorer"))
    }

    @Test
    fun `a panel hidden by dragging returns with its last valid size, not the minimum`() = runComposeUiTest {
        val state = WorkspaceDockState()
        show(state)
        drag(state.separator(ShellPanels.Explorer), dx = 40f)

        drag(state.separator(ShellPanels.Explorer), dx = -400f)
        assertEquals(false, state.explorerVisible)
        state.toggleExplorer()
        waitForIdle()

        assertEquals(304.dp, width("content-explorer"))
    }

    @Test
    fun `adjusted sizes survive resizing the window`() = runComposeUiTest {
        val state = WorkspaceDockState()
        var windowWidth by mutableStateOf(1000)
        setContent {
            LoreDesignerTheme(darkTheme = false) {
                Box(Modifier.size(windowWidth.dp, 800.dp)) {
                    DockHost(state, registry, viewEmpty = { Box(Modifier.fillMaxSize().testTag("editor-area")) })
                }
            }
        }
        drag(state.separator(ShellPanels.Explorer), dx = 60f)

        windowWidth = 900
        waitForIdle()
        assertEquals(324.dp, width("content-explorer"))
        windowWidth = 1400
        waitForIdle()
        assertEquals(324.dp, width("content-explorer"))
    }

    @Test
    fun `a separator between two view groups redistributes them proportionally`() = runComposeUiTest {
        var layout = WorkspaceDockState.defaultLayout()
        val editorArea = checkNotNull(layout.groups.single { it.kind == PanelKind.View }).id
        listOf(
            PanelId("left-doc") to DockTarget.Tab(editorArea),
            PanelId("right-doc") to DockTarget.Edge(editorArea, DockSide.Right),
        ).forEach { (panel, target) ->
            layout = (layout.apply(DockOperation.AddPanel(panel, PanelKind.View, target)) as DockResult.Applied).layout
        }
        val docs = listOf("left-doc", "right-doc").associate { name ->
            PanelId(name) to DockPanelSpec(
                label = { name },
                content = { Box(Modifier.fillMaxSize().testTag("content-$name")) },
                sizing = ViewSizing,
                editorSurface = true,
            )
        }
        val state = WorkspaceDockState(
            layout,
            initiallyHidden = setOf(ShellPanels.Explorer, ShellPanels.Properties, ShellPanels.Log),
        )
        show(state, width = 1000, registry = registry + docs)
        val left = width("content-left-doc")
        val right = width("content-right-doc")

        drag(state.separator(PanelId("left-doc")), dx = 100f)

        assertEquals(left + 100.dp, width("content-left-doc"))
        assertEquals(right - 100.dp, width("content-right-doc"))

        onNodeWithTag(state.separator(PanelId("left-doc"))).performMouseInput { doubleClick() }
        waitForIdle()
        assertTrue(abs(width("content-left-doc").value - width("content-right-doc").value) <= 1f)
    }
}
