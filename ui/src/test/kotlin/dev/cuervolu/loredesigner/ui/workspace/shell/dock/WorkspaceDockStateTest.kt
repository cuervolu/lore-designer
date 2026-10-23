package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import dev.cuervolu.loredesigner.editor.dock.DockLayout
import dev.cuervolu.loredesigner.editor.dock.DockOperation
import dev.cuervolu.loredesigner.editor.dock.DockResult
import dev.cuervolu.loredesigner.editor.dock.DockTarget
import dev.cuervolu.loredesigner.editor.dock.PanelId
import dev.cuervolu.loredesigner.editor.dock.PanelKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
}
