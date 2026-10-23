package dev.cuervolu.loredesigner.editor.dock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AddPanelTest {
    @Test
    fun `a view without target joins the first view group and becomes active`() {
        val result = baseLayout().applied(DockOperation.AddPanel(panel("doc3"), PanelKind.View))

        assertEquals(listOf(panel("doc1"), panel("doc2"), panel("doc3")), result.group(2).panels)
        assertEquals(panel("doc3"), result.group(2).activePanel)
        assertEquals(PanelKind.View, result.panels[panel("doc3")])
    }

    @Test
    fun `a tool without target joins the first tool group`() {
        val result = baseLayout().applied(DockOperation.AddPanel(panel("logs"), PanelKind.Tool))

        assertEquals(listOf(panel("explorer"), panel("logs")), result.group(1).panels)
        assertEquals(panel("logs"), result.group(1).activePanel)
    }

    @Test
    fun `a tool with no tool group gets a new group below the layout`() {
        val ids = SequentialIds()
        val initial = DockLayout.initial(ids)
        val result = initial.applied(DockOperation.AddPanel(panel("logs"), PanelKind.Tool), ids)

        val root = assertIs<SplitNode>(result.root)
        assertEquals(DockOrientation.Vertical, root.orientation)
        assertEquals(initial.root, root.children[0])
        val tools = assertIs<PanelGroup>(root.children[1])
        assertEquals(PanelKind.Tool, tools.kind)
        assertEquals(listOf(panel("logs")), tools.panels)
        assertWeights(listOf(0.5f, 0.5f), root.weights)
    }

    @Test
    fun `the first tool added next to an existing vertical split is flattened into it`() {
        val layout = layoutOf(
            split(
                10,
                DockOrientation.Vertical,
                group(1, PanelKind.View, "doc") to 0.5f,
                group(2, PanelKind.View, "other") to 0.5f,
            ),
        )

        val result = layout.applied(DockOperation.AddPanel(panel("logs"), PanelKind.Tool, share = 0.2f))

        val root = assertIs<SplitNode>(result.root)
        assertEquals(3, root.children.size)
        assertWeights(listOf(0.4f, 0.4f, 0.2f), root.weights)
    }

    @Test
    fun `a panel can be added to a specific tab index and the index is clamped`() {
        val first = baseLayout().applied(
            DockOperation.AddPanel(panel("doc3"), PanelKind.View, DockTarget.Tab(node(2), index = 0)),
        )
        assertEquals(listOf("doc3", "doc1", "doc2").map(::panel), first.group(2).panels)

        val clamped = baseLayout().applied(
            DockOperation.AddPanel(panel("doc3"), PanelKind.View, DockTarget.Tab(node(2), index = 99)),
        )
        assertEquals(listOf("doc1", "doc2", "doc3").map(::panel), clamped.group(2).panels)
    }

    @Test
    fun `a panel can be added beside a group`() {
        val result = baseLayout().applied(
            DockOperation.AddPanel(panel("doc3"), PanelKind.View, DockTarget.Edge(node(2), DockSide.Bottom)),
        )

        val nested = assertIs<SplitNode>(assertIs<SplitNode>(result.root).children[1])
        assertEquals(DockOrientation.Vertical, nested.orientation)
        assertEquals(result.group(2), nested.children[0])
        assertEquals(listOf(panel("doc3")), assertIs<PanelGroup>(nested.children[1]).panels)
    }

    @Test
    fun `a duplicate panel id is rejected`() {
        val reason = baseLayout().rejected(DockOperation.AddPanel(panel("doc1"), PanelKind.View))

        assertEquals(DockRejection.DuplicatePanel(panel("doc1")), reason)
    }

    @Test
    fun `a panel cannot be added to a group of another kind`() {
        val reason = baseLayout().rejected(
            DockOperation.AddPanel(panel("logs"), PanelKind.Tool, DockTarget.Tab(node(2))),
        )

        assertEquals(DockRejection.KindMismatch(panel("logs"), PanelKind.Tool, node(2)), reason)
    }

    @Test
    fun `an unknown target group is rejected`() {
        val reason = baseLayout().rejected(
            DockOperation.AddPanel(panel("doc3"), PanelKind.View, DockTarget.Tab(node(99))),
        )

        assertEquals(DockRejection.UnknownGroup(node(99)), reason)
    }

    @Test
    fun `shares outside the open unit interval are rejected`() {
        listOf(0f, 1f, -0.5f, 1.5f, Float.NaN).forEach { share ->
            val reason = baseLayout().rejected(
                DockOperation.AddPanel(panel("doc3"), PanelKind.View, DockTarget.Edge(node(2), DockSide.Right), share),
            )
            assertIs<DockRejection.InvalidShare>(reason)
        }
    }

    @Test
    fun `a rejected add leaves the layout untouched`() {
        val layout = baseLayout()
        layout.rejected(DockOperation.AddPanel(panel("doc1"), PanelKind.View))

        assertNull(layout.panels[panel("logs")])
        assertEquals(baseLayout(), layout)
    }
}
