package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.ui.unit.dp
import dev.cuervolu.loredesigner.editor.dock.DockLayout
import dev.cuervolu.loredesigner.editor.dock.DockOperation
import dev.cuervolu.loredesigner.editor.dock.DockResult
import dev.cuervolu.loredesigner.editor.dock.DockSide
import dev.cuervolu.loredesigner.editor.dock.DockTarget
import dev.cuervolu.loredesigner.editor.dock.PanelId
import dev.cuervolu.loredesigner.editor.dock.PanelKind
import dev.cuervolu.loredesigner.editor.dock.SplitNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class WorkspaceDockStateTest {
    @Test
    fun `the default layout is valid and starts with the log hidden`() {
        val state = WorkspaceDockState()

        assertEquals(emptyList(), state.layout.validate())
        assertTrue(state.explorerVisible)
        assertTrue(state.propertiesVisible)
        assertFalse(state.bottomPanelVisible)
    }

    @Test
    fun `hiding and showing never changes the layout`() {
        val state = WorkspaceDockState()
        val before = state.layout

        state.toggleExplorer()
        state.toggleProperties()
        state.toggleBottomPanel()
        state.toggleBottomPanel()
        state.toggleExplorer()
        state.toggleProperties()

        assertEquals(before, state.layout)
        assertEquals(setOf(ShellPanels.Log), state.hidden)
    }

    @Test
    fun `a panel returns to its own group and side after being hidden`() {
        val state = WorkspaceDockState()
        val group = checkNotNull(state.layout.groupOf(ShellPanels.Properties))

        state.toggleProperties()
        state.toggleProperties()

        assertEquals(group, state.layout.groupOf(ShellPanels.Properties))
        assertEquals(
            state.layout.groups.indexOf(group),
            state.layout.groups.indexOfFirst {
                ShellPanels.Properties in
                    it.panels
            },
        )
    }

    @Test
    fun `activating a hidden panel reveals it and makes it the active tab`() {
        val extra = PanelId("extra")
        val base = WorkspaceDockState.defaultLayout()
        val group = checkNotNull(base.groupOf(ShellPanels.Explorer)).id
        val layout = (
            base.apply(
                DockOperation.AddPanel(extra, PanelKind.Tool, DockTarget.Tab(group)),
            ) as DockResult.Applied
            ).layout
        val state = WorkspaceDockState(layout, initiallyHidden = setOf(ShellPanels.Explorer))

        state.activate(ShellPanels.Explorer)

        assertTrue(state.explorerVisible)
        assertEquals(ShellPanels.Explorer, state.layout.groupOf(extra)?.activePanel)
    }

    @Test
    fun `activating an unknown panel keeps the layout and visibility`() {
        val state = WorkspaceDockState()
        val before = state.layout

        state.activate(PanelId("ghost"))

        assertEquals(before, state.layout)
        assertEquals(setOf(ShellPanels.Log), state.hidden)
    }

    @Test
    fun `every toggle sequence leaves a valid layout`() {
        val state = WorkspaceDockState(DockLayout.initial())
        listOf(ShellPanels.Explorer, ShellPanels.Log, ShellPanels.Explorer).forEach(state::toggle)

        assertEquals(emptyList(), state.layout.validate())
    }

    @Test
    fun `dragged sizes are kept while the panel is hidden and shown again`() {
        val state = WorkspaceDockState()
        val explorer = state.layout.groupOf(ShellPanels.Explorer)!!.id

        state.resizeFixed(explorer, 320.dp)
        state.toggleExplorer()
        state.toggleExplorer()

        assertEquals(320.dp, state.sizeOverride(explorer))
        state.resetFixed(explorer)
        assertEquals(null, state.sizeOverride(explorer))
    }

    @Test
    fun `hiding by gesture keeps the panel in its place`() {
        val state = WorkspaceDockState()
        val before = state.layout

        state.hide(setOf(ShellPanels.Explorer))

        assertFalse(state.explorerVisible)
        assertEquals(before, state.layout)
    }

    @Test
    fun `resizing flexible neighbours only moves weight between them`() {
        val state = twoViewGroups()
        val split = state.layout.root as SplitNode
        val (first, second) = split.children.map { it.id }

        state.resizeFill(split.id, first, second, beforeShare = 0.75f)

        val resized = state.layout.findSplit(split.id)!!
        assertEquals(0.75f, resized.weights[0], 1e-4f)
        assertEquals(0.25f, resized.weights[1], 1e-4f)
        assertEquals(emptyList(), state.layout.validate())
    }

    private fun twoViewGroups(): WorkspaceDockState {
        var layout = DockLayout.initial()
        val editorArea = layout.groups.single().id
        listOf("a", "b").forEachIndexed { index, name ->
            val target = if (index == 0) {
                DockTarget.Tab(editorArea)
            } else {
                DockTarget.Edge(editorArea, DockSide.Right)
            }
            layout = (layout.apply(DockOperation.AddPanel(PanelId(name), PanelKind.View, target)) as DockResult.Applied)
                .layout
        }
        return WorkspaceDockState(layout, initiallyHidden = emptySet())
    }

    private fun withExtraTool(): Triple<WorkspaceDockState, PanelId, DockLayout> {
        val extra = PanelId("extra")
        val base = WorkspaceDockState.defaultLayout()
        val explorer = checkNotNull(base.groupOf(ShellPanels.Explorer)).id
        val layout = (
            base.apply(
                DockOperation.AddPanel(extra, PanelKind.Tool, DockTarget.Tab(explorer)),
            ) as DockResult.Applied
            )
            .layout
        return Triple(WorkspaceDockState(layout), extra, layout)
    }

    @Test
    fun `moving a panel to another group keeps the catalog, activates it and keeps the tree valid`() {
        val (state, extra, before) = withExtraTool()
        val target = checkNotNull(before.groupOf(ShellPanels.Properties)).id

        assertTrue(state.move(extra, DockTarget.Tab(target, 0)))

        assertEquals(before.panels, state.layout.panels)
        assertEquals(listOf(extra, ShellPanels.Properties), state.layout.findGroup(target)?.panels)
        assertEquals(extra, state.layout.findGroup(target)?.activePanel)
        assertEquals(listOf(ShellPanels.Explorer), state.layout.groupOf(ShellPanels.Explorer)?.panels)
        assertEquals(emptyList(), state.layout.validate())
    }

    @Test
    fun `a rejected move leaves the very same layout in place`() {
        val (state, extra, before) = withExtraTool()
        val view = checkNotNull(before.groups.single { it.kind == PanelKind.View }).id

        assertFalse(state.move(extra, DockTarget.Tab(view)))

        assertSame(before, state.layout)
    }

    @Test
    fun `moving a hidden panel reveals it`() {
        val (state, extra, before) = withExtraTool()
        state.hide(setOf(extra))

        state.move(extra, DockTarget.Tab(checkNotNull(before.groupOf(ShellPanels.Properties)).id))

        assertTrue(state.isVisible(extra))
    }

    @Test
    fun `moving the last panel out of a group drops the size override of the group that disappeared`() {
        val (state, _, before) = withExtraTool()
        val properties = checkNotNull(before.groupOf(ShellPanels.Properties)).id
        val explorer = checkNotNull(before.groupOf(ShellPanels.Explorer)).id
        state.resizeFixed(properties, 320.dp)
        state.resizeFixed(explorer, 280.dp)

        state.move(ShellPanels.Properties, DockTarget.Tab(explorer))

        assertEquals(mapOf(explorer to 280.dp), state.sizeOverrides)
    }

    @Test
    fun `rebalancing shares only the weight held by the flexible children`() {
        val state = WorkspaceDockState()
        val root = state.layout.root as SplitNode
        val (explorer, editor, properties) = root.children.map { it.id }
        val before = root.weights

        state.rebalanceFill(root.id, setOf(explorer, editor))

        val after = (state.layout.root as SplitNode).weights
        assertEquals((before[0] + before[1]) / 2, after[0])
        assertEquals(after[0], after[1])
        assertEquals(before[2], after[2])
        assertEquals(emptyList(), state.layout.validate())
        assertEquals(properties, state.layout.root.let { (it as SplitNode).children[2].id })
    }
}
