package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import dev.cuervolu.loredesigner.editor.dock.DockOperation
import dev.cuervolu.loredesigner.editor.dock.DockOrientation
import dev.cuervolu.loredesigner.editor.dock.DockResult
import dev.cuervolu.loredesigner.editor.dock.DockTarget
import dev.cuervolu.loredesigner.editor.dock.PanelId
import dev.cuervolu.loredesigner.editor.dock.PanelKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class DockProjectionTest {
    private val layout = WorkspaceDockState.defaultLayout()

    private fun RenderNode.groups(): List<RenderGroup> = when (this) {
        is RenderGroup -> listOf(this)
        is RenderSplit -> children.flatMap { it.groups() }
    }

    @Test
    fun `the default layout puts explorer and properties around the editor with the log below it`() {
        val root = assertIs<RenderSplit>(layout.project(emptySet()))

        assertEquals(DockOrientation.Horizontal, root.orientation)
        assertEquals(3, root.children.size)
        assertEquals(listOf(ShellPanels.Explorer), assertIs<RenderGroup>(root.children[0]).tabs)
        assertEquals(listOf(ShellPanels.Properties), assertIs<RenderGroup>(root.children[2]).tabs)
        val center = assertIs<RenderSplit>(root.children[1])
        assertEquals(DockOrientation.Vertical, center.orientation)
        assertEquals(listOf(PanelKind.View, PanelKind.Tool), center.children.map { assertIs<RenderGroup>(it).kind })
        assertEquals(listOf(ShellPanels.Log), assertIs<RenderGroup>(center.children[1]).tabs)
    }

    @Test
    fun `a hidden panel disappears from the projection but not from the tree`() {
        val projected = layout.project(setOf(ShellPanels.Properties))

        assertEquals(setOf(ShellPanels.Explorer, ShellPanels.Log), projected.groups().flatMap { it.tabs }.toSet())
        assertEquals(ShellPanels.Properties, layout.groupOf(ShellPanels.Properties)?.panels?.single())
    }

    @Test
    fun `hiding a side panel collapses its group and renormalizes the weights`() {
        val root = assertIs<RenderSplit>(layout.project(setOf(ShellPanels.Properties)))

        assertEquals(2, root.children.size)
        assertEquals(1f, root.weights.sum(), 1e-4f)
    }

    @Test
    fun `hiding everything leaves only the empty editor area`() {
        val root =
            assertIs<RenderGroup>(layout.project(setOf(ShellPanels.Explorer, ShellPanels.Properties, ShellPanels.Log)))

        assertEquals(PanelKind.View, root.kind)
        assertNull(root.displayed)
    }

    @Test
    fun `a hidden active tab falls back to the first visible one without touching the model`() {
        val first = PanelId("first")
        val second = PanelId("second")
        val explorerGroup = checkNotNull(layout.groupOf(ShellPanels.Explorer)).id
        var withTabs = layout
        listOf(first, second).forEach {
            withTabs =
                (
                    withTabs.apply(
                        DockOperation.AddPanel(it, PanelKind.Tool, DockTarget.Tab(explorerGroup)),
                    ) as DockResult.Applied
                    ).layout
        }
        assertEquals(second, withTabs.groupOf(second)?.activePanel)

        val group = withTabs.project(setOf(second)).groups().single { first in it.tabs }

        assertEquals(listOf(ShellPanels.Explorer, first), group.tabs)
        assertEquals(ShellPanels.Explorer, group.displayed)
        assertEquals(second, withTabs.groupOf(second)?.activePanel)
    }
}
