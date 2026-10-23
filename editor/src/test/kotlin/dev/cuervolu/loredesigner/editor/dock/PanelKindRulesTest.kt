package dev.cuervolu.loredesigner.editor.dock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PanelKindRulesTest {
    @Test
    fun `the initial layout is an empty view group`() {
        val layout = DockLayout.initial(SequentialIds())

        assertEquals(listOf(PanelKind.View), layout.groups.map { it.kind })
        assertEquals(emptyMap(), layout.panels)
    }

    @Test
    fun `groups never mix kinds after arbitrary moves`() {
        val ids = SequentialIds()
        var layout = baseLayout()
        listOf(
            DockOperation.Move(panel("doc1"), DockTarget.Edge(node(3), DockSide.Bottom)),
            DockOperation.Move(panel("props"), DockTarget.Tab(node(1))),
            DockOperation.AddPanel(panel("logs"), PanelKind.Tool),
            DockOperation.AddPanel(panel("doc3"), PanelKind.View),
        ).forEach { layout = layout.applied(it, ids) }

        layout.groups.forEach { g -> assertTrue(g.panels.all { layout.panels[it] == g.kind }) }
    }

    @Test
    fun `the view area survives closing every view`() {
        val result = baseLayout()
            .applied(DockOperation.Close(panel("doc1")))
            .applied(DockOperation.Close(panel("doc2")))

        assertEquals(listOf(PanelKind.View), result.groups.map { it.kind }.filter { it == PanelKind.View })
    }

    @Test
    fun `tools and views can be split against each other but stay in their own groups`() {
        val result = baseLayout().applied(DockOperation.Split(panel("props"), node(2), DockSide.Bottom))

        assertEquals(PanelKind.Tool, result.groupOf(panel("props"))?.kind)
        assertEquals(PanelKind.View, result.groupOf(panel("doc1"))?.kind)
    }
}
