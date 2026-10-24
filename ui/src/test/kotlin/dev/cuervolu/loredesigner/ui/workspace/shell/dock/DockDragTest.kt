package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.moveTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import dev.cuervolu.loredesigner.editor.dock.DockLayout
import dev.cuervolu.loredesigner.editor.dock.DockOperation
import dev.cuervolu.loredesigner.editor.dock.DockResult
import dev.cuervolu.loredesigner.editor.dock.DockTarget
import dev.cuervolu.loredesigner.editor.dock.PanelId
import dev.cuervolu.loredesigner.editor.dock.PanelKind
import dev.cuervolu.loredesigner.editor.dock.SplitNode
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DockDragTest {
    private val extra = PanelId("extra")
    private val doc = PanelId("doc")

    private val registry: PanelRegistry = (
        ShellPanels.Sizing.mapValues { (id, sizing) ->
            DockPanelSpec(
                label = { id.value },
                content = { Box(Modifier.fillMaxSize().testTag("content-${id.value}")) { Text("body ${id.value}") } },
                sizing = sizing,
            )
        }
        ) + mapOf(
        extra to DockPanelSpec(
            label = { "extra" },
            content = { Box(Modifier.fillMaxSize().testTag("content-extra")) },
            sizing = ShellPanels.Sizing.getValue(ShellPanels.Explorer),
        ),
        doc to DockPanelSpec(
            label = { "doc" },
            content = { Box(Modifier.fillMaxSize().testTag("content-doc")) },
            sizing = ViewSizing,
            editorSurface = true,
        ),
    )

    private fun DockLayout.with(panel: PanelId, kind: PanelKind, target: DockTarget): DockLayout =
        (apply(DockOperation.AddPanel(panel, kind, target)) as DockResult.Applied).layout

    private fun DockLayout.groupId(panel: PanelId) = checkNotNull(groupOf(panel)).id

    /** Explorer and a second tool share a group, so it shows a real tab strip; the editor area holds one document. */
    private fun layout(): DockLayout {
        val base = WorkspaceDockState.defaultLayout()
        val withExtra = base.with(extra, PanelKind.Tool, DockTarget.Tab(base.groupId(ShellPanels.Explorer)))
        val editor = withExtra.groups.single { it.kind == PanelKind.View }.id
        return withExtra.with(doc, PanelKind.View, DockTarget.Tab(editor))
    }

    private fun ComposeUiTest.show(state: WorkspaceDockState) = setContent {
        LoreDesignerTheme(darkTheme = false) {
            Box(Modifier.size(1000.dp, 800.dp)) {
                DockHost(state, registry, viewEmpty = { Box(Modifier.fillMaxSize()) })
            }
        }
    }

    private fun ComposeUiTest.bounds(tag: String) = onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot

    /** Presses [source] and moves past the drag threshold, leaving the button down. */
    private fun ComposeUiTest.pickUp(source: String) {
        onNodeWithTag(source).performMouseInput {
            moveTo(center)
            press()
            moveBy(Offset(8f, 0f))
        }
        waitForIdle()
    }

    private fun ComposeUiTest.moveOver(source: String, target: Offset) {
        val origin = bounds(source).topLeft
        onNodeWithTag(source).performMouseInput { moveTo(target - origin) }
        waitForIdle()
    }

    private fun ComposeUiTest.release(source: String) {
        onNodeWithTag(source).performMouseInput { release() }
        waitForIdle()
    }

    private fun ComposeUiTest.dragAndDrop(source: String, target: Offset) {
        pickUp(source)
        moveOver(source, target)
        release(source)
    }

    private fun ComposeUiTest.rightEdgeOf(tag: String): Offset = bounds(tag).let { Offset(it.right - 4f, it.center.y) }

    private fun ComposeUiTest.assertDragVisuals(visible: Boolean) {
        listOf("dock-drag-ghost").forEach {
            if (visible) onNodeWithTag(it).assertExists() else onNodeWithTag(it).assertDoesNotExist()
        }
    }

    @Test
    fun `a press that stays under the threshold is a click that activates the tab`() = runComposeUiTest {
        val state = WorkspaceDockState(layout())
        show(state)
        assertEquals(extra, state.layout.groupOf(extra)?.activePanel)
        val before = state.layout

        onNodeWithTag("dock-tab-explorer").performMouseInput {
            moveTo(center)
            press()
            moveBy(Offset(3f, 0f))
        }
        waitForIdle()
        assertDragVisuals(false)
        release("dock-tab-explorer")

        assertEquals(ShellPanels.Explorer, state.layout.groupOf(ShellPanels.Explorer)?.activePanel)
        assertEquals(before.groupId(extra), state.layout.groupId(extra))
        assertEquals(before.groups.size, state.layout.groups.size)
    }

    @Test
    fun `a panel alone in its group can be dragged into another group`() = runComposeUiTest {
        val state = WorkspaceDockState(layout())
        show(state)
        val catalog = state.layout.panels
        val sourceGroup = state.layout.groupId(ShellPanels.Properties)
        val targetBar = "dock-tabbar-${state.layout.groupId(ShellPanels.Explorer).value}"

        dragAndDrop("dock-tab-properties", rightEdgeOf(targetBar))

        val group = checkNotNull(state.layout.groupOf(ShellPanels.Properties))
        assertEquals(listOf(ShellPanels.Explorer, extra, ShellPanels.Properties), group.panels)
        assertEquals(ShellPanels.Properties, group.activePanel)
        assertEquals(null, state.layout.findGroup(sourceGroup))
        assertEquals(catalog, state.layout.panels)
        assertEquals(emptyList(), state.layout.validate())
    }

    @Test
    fun `dragging to a tab bar of another single tab group inserts at the pointer`() = runComposeUiTest {
        val state = WorkspaceDockState(layout())
        show(state)
        val propertiesBar = "dock-tabbar-${state.layout.groupId(ShellPanels.Properties).value}"

        dragAndDrop("dock-tab-extra", rightEdgeOf(propertiesBar) - Offset(EDGE_ZONE * 2, 0f))

        val group = checkNotNull(state.layout.groupOf(extra))
        assertEquals(listOf(ShellPanels.Properties, extra), group.panels)
        assertEquals(extra, group.activePanel)
        assertEquals(listOf(ShellPanels.Explorer), state.layout.groupOf(ShellPanels.Explorer)?.panels)
        assertEquals(emptyList(), state.layout.validate())
    }

    @Test
    fun `dropping on the body of a compatible group appends the panel`() = runComposeUiTest {
        val state = WorkspaceDockState(layout())
        show(state)

        dragAndDrop("dock-tab-extra", bounds("content-properties").center)

        assertEquals(listOf(ShellPanels.Properties, extra), state.layout.groupOf(extra)?.panels)
        assertEquals(emptyList(), state.layout.validate())
    }

    @Test
    fun `reordering inside a group shows the insertion line and commits only on release`() = runComposeUiTest {
        val state = WorkspaceDockState(layout())
        show(state)
        val before = state.layout
        val bar = "dock-tabbar-${before.groupId(ShellPanels.Explorer).value}"

        pickUp("dock-tab-explorer")
        moveOver("dock-tab-explorer", rightEdgeOf(bar))

        assertDragVisuals(true)
        onNodeWithTag("dock-drop-indicator").assertExists()
        assertSame(before, state.layout)

        release("dock-tab-explorer")

        assertEquals(listOf(extra, ShellPanels.Explorer), state.layout.groupOf(extra)?.panels)
        assertEquals(ShellPanels.Explorer, state.layout.groupOf(extra)?.activePanel)
        assertDragVisuals(false)
    }

    @Test
    fun `a tool cannot be dropped on a view group and the layout is left alone`() = runComposeUiTest {
        val state = WorkspaceDockState(layout())
        show(state)
        val before = state.layout

        pickUp("dock-tab-properties")
        moveOver("dock-tab-properties", bounds("content-doc").center)
        onNodeWithTag("dock-drop-invalid").assertExists()
        onNodeWithTag("dock-drop-valid").assertDoesNotExist()
        release("dock-tab-properties")

        assertSame(before, state.layout)
        assertDragVisuals(false)
    }

    @Test
    fun `a view cannot be dropped on a tool group`() = runComposeUiTest {
        val state = WorkspaceDockState(layout())
        show(state)
        val before = state.layout

        pickUp("dock-tab-doc")
        moveOver("dock-tab-doc", bounds("content-properties").center)
        onNodeWithTag("dock-drop-invalid").assertExists()
        release("dock-tab-doc")

        assertSame(before, state.layout)
    }

    @Test
    fun `escape cancels a drag in progress and the release afterwards changes nothing`() = runComposeUiTest {
        val state = WorkspaceDockState(layout())
        show(state)
        val before = state.layout

        pickUp("dock-tab-extra")
        moveOver("dock-tab-extra", bounds("content-properties").center)
        onNodeWithTag("dock-drop-valid").assertExists()

        onRoot().performKeyInput { pressKey(Key.Escape) }
        waitForIdle()
        assertDragVisuals(false)
        onNodeWithTag("dock-drop-valid").assertDoesNotExist()

        moveOver("dock-tab-extra", bounds("content-properties").center - Offset(5f, 0f))
        release("dock-tab-extra")
        assertSame(before, state.layout)

        dragAndDrop("dock-tab-extra", bounds("content-properties").center)
        assertEquals(listOf(ShellPanels.Properties, extra), state.layout.groupOf(extra)?.panels)
    }

    @Test
    fun `releasing outside any group cancels without touching the layout`() = runComposeUiTest {
        val state = WorkspaceDockState(layout())
        show(state)
        val before = state.layout

        pickUp("dock-tab-extra")
        moveOver("dock-tab-extra", Offset(1010f, 400f))
        onNodeWithTag("dock-drop-valid").assertDoesNotExist()
        onNodeWithTag("dock-drop-invalid").assertDoesNotExist()
        release("dock-tab-extra")

        assertSame(before, state.layout)
        assertDragVisuals(false)
    }

    @Test
    fun `a group that disappears during the drag cancels it instead of leaving a stale target`() = runComposeUiTest {
        val state = WorkspaceDockState(layout())
        show(state)
        val before = state.layout

        pickUp("dock-tab-extra")
        moveOver("dock-tab-extra", bounds("content-properties").center)
        state.hide(setOf(ShellPanels.Properties))
        waitForIdle()
        assertDragVisuals(false)

        release("dock-tab-extra")

        assertSame(before, state.layout)
    }

    @Test
    fun `properties can leave the right edge for explorer and be docked back to the right edge`() = runComposeUiTest {
        val state = WorkspaceDockState()
        show(state)
        val catalog = state.layout.panels
        val editorRight = bounds("content-properties").left

        dragAndDrop("dock-tab-properties", bounds("content-explorer").center)
        assertEquals(
            listOf(ShellPanels.Explorer, ShellPanels.Properties),
            state.layout.groupOf(ShellPanels.Explorer)?.panels,
        )
        assertEquals(emptyList(), state.layout.validate())

        pickUp("dock-tab-properties")
        moveOver("dock-tab-properties", Offset(990f, 300f))
        onNodeWithTag("dock-drop-edge").assertExists()
        assertEquals(
            listOf(ShellPanels.Explorer, ShellPanels.Properties),
            state.layout.groupOf(ShellPanels.Explorer)?.panels,
        )
        release("dock-tab-properties")

        val restored = checkNotNull(state.layout.groupOf(ShellPanels.Properties))
        assertEquals(listOf(ShellPanels.Properties), restored.panels)
        assertEquals(PanelKind.Tool, restored.kind)
        assertEquals(listOf(ShellPanels.Explorer), state.layout.groupOf(ShellPanels.Explorer)?.panels)
        assertEquals(editorRight, bounds("content-properties").left)
        assertEquals(catalog, state.layout.panels)
        assertEquals(emptyList(), state.layout.validate())
    }

    @Test
    fun `a view is never docked to a window edge`() = runComposeUiTest {
        val state = WorkspaceDockState(layout())
        show(state)
        val before = state.layout

        pickUp("dock-tab-doc")
        moveOver("dock-tab-doc", Offset(990f, 300f))
        onNodeWithTag("dock-drop-edge").assertDoesNotExist()
        release("dock-tab-doc")

        assertSame(before, state.layout)
    }

    @Test
    fun `log can leave the bottom for the explorer group and return without deforming the layout`() = runComposeUiTest {
        val state = WorkspaceDockState(initiallyHidden = emptySet())
        show(state)
        val editorWidth = bounds(
            "dock-group-${state.layout.groups.single {
                it.kind == PanelKind.View
            }.id.value}",
        ).width
        val logHeight = bounds("content-log").height
        val catalog = state.layout.panels

        dragAndDrop("dock-tab-log", bounds("content-explorer").center)
        assertEquals(listOf(ShellPanels.Explorer, ShellPanels.Log), state.layout.groupOf(ShellPanels.Log)?.panels)
        assertEquals(emptyList(), state.layout.validate())

        pickUp("dock-tab-log")
        moveOver("dock-tab-log", Offset(500f, bounds("content-properties").bottom - 8f))
        onNodeWithTag("dock-drop-edge").assertExists()
        release("dock-tab-log")

        assertEquals(emptyList(), state.layout.validate())
        assertEquals(catalog, state.layout.panels)
        val view = state.layout.groups.single { it.kind == PanelKind.View }
        val log = checkNotNull(state.layout.groupOf(ShellPanels.Log))
        assertEquals(listOf(ShellPanels.Log), log.panels)
        val column = state.layout.root.let { root ->
            (root as SplitNode).children.filterIsInstance<SplitNode>().single()
        }
        assertEquals(listOf(view.id, log.id), column.children.map { it.id })

        assertEquals(264f, bounds("content-explorer").width)
        assertEquals(300f, bounds("content-properties").width)
        assertEquals(editorWidth, bounds("dock-group-${view.id.value}").width)
        assertEquals(logHeight, bounds("content-log").height)
        assertEquals(bounds("dock-group-${view.id.value}").width, bounds("content-log").width)
    }
}
