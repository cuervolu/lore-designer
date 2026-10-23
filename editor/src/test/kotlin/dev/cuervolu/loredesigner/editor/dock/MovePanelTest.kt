package dev.cuervolu.loredesigner.editor.dock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class MovePanelTest {
    @Test
    fun `moving within a group reorders its tabs and activates the panel`() {
        val result = baseLayout().applied(DockOperation.Move(panel("doc1"), DockTarget.Tab(node(2), index = 1)))

        assertEquals(listOf("doc2", "doc1").map(::panel), result.group(2).panels)
        assertEquals(panel("doc1"), result.group(2).activePanel)
    }

    @Test
    fun `a reorder index past the end is clamped`() {
        val result = baseLayout().applied(DockOperation.Move(panel("doc1"), DockTarget.Tab(node(2), index = 99)))

        assertEquals(listOf("doc2", "doc1").map(::panel), result.group(2).panels)
    }

    @Test
    fun `moving a panel to its own position keeps the order`() {
        val result = baseLayout().applied(DockOperation.Move(panel("doc1"), DockTarget.Tab(node(2), index = 0)))

        assertEquals(listOf("doc1", "doc2").map(::panel), result.group(2).panels)
    }

    @Test
    fun `moving the last panel out of a tool group removes the group and redistributes weights`() {
        val result = baseLayout().applied(DockOperation.Move(panel("explorer"), DockTarget.Tab(node(3))))

        assertEquals(listOf("props", "explorer").map(::panel), result.group(3).panels)
        assertNull(result.findGroup(node(1)))
        assertWeights(listOf(0.75f, 0.25f), assertIs<SplitNode>(result.root).weights)
    }

    @Test
    fun `moving a panel does not lose or duplicate panels`() {
        val result = baseLayout().applied(DockOperation.Move(panel("doc1"), DockTarget.Edge(node(3), DockSide.Top)))

        assertEquals(baseLayout().panels, result.panels)
        assertEquals(
            baseLayout().groups.flatMap { it.panels }.toSet(),
            result.groups.flatMap { it.panels }.toSet(),
        )
    }

    @Test
    fun `moving a tool into a view group is rejected`() {
        assertEquals(
            DockRejection.KindMismatch(panel("explorer"), PanelKind.Tool, node(2)),
            baseLayout().rejected(DockOperation.Move(panel("explorer"), DockTarget.Tab(node(2)))),
        )
    }

    @Test
    fun `moving a view into a tool group is rejected`() {
        assertEquals(
            DockRejection.KindMismatch(panel("doc1"), PanelKind.View, node(1)),
            baseLayout().rejected(DockOperation.Move(panel("doc1"), DockTarget.Tab(node(1)))),
        )
    }

    @Test
    fun `a view dropped on a tool group edge creates a view group beside it`() {
        val result = baseLayout().applied(
            DockOperation.Move(panel("doc1"), DockTarget.Edge(node(1), DockSide.Left)),
            SequentialIds(),
        )

        val root = assertIs<SplitNode>(result.root)
        val created = assertIs<PanelGroup>(root.children[0])
        assertEquals(PanelKind.View, created.kind)
        assertEquals(listOf(panel("doc1")), created.panels)
        assertEquals(node(1), root.children[1].id)
        assertWeights(listOf(0.1f, 0.1f, 0.6f, 0.2f), root.weights)
    }

    @Test
    fun `a sole panel cannot be split against its own group`() {
        assertEquals(
            DockRejection.SelfSplit(panel("explorer"), node(1)),
            baseLayout().rejected(DockOperation.Move(panel("explorer"), DockTarget.Edge(node(1), DockSide.Right))),
        )
    }

    @Test
    fun `a panel can be split out of a multi panel group against its own edge`() {
        val result = baseLayout().applied(DockOperation.Move(panel("doc2"), DockTarget.Edge(node(2), DockSide.Right)))

        assertEquals(listOf(panel("doc1")), result.group(2).panels)
        val root = assertIs<SplitNode>(result.root)
        assertEquals(listOf(panel("doc2")), assertIs<PanelGroup>(root.children[2]).panels)
        assertWeights(listOf(0.2f, 0.3f, 0.3f, 0.2f), root.weights)
    }

    @Test
    fun `moving an unknown panel is rejected`() {
        assertEquals(
            DockRejection.UnknownPanel(panel("ghost")),
            baseLayout().rejected(DockOperation.Move(panel("ghost"), DockTarget.Tab(node(2)))),
        )
    }

    @Test
    fun `moving to an unknown group is rejected without changing the layout`() {
        val layout = baseLayout()

        assertEquals(
            DockRejection.UnknownGroup(node(99)),
            layout.rejected(DockOperation.Move(panel("doc1"), DockTarget.Edge(node(99), DockSide.Left))),
        )
        assertEquals(baseLayout(), layout)
    }

    @Test
    fun `an invalid share is rejected`() {
        assertEquals(
            DockRejection.InvalidShare(1f),
            baseLayout().rejected(DockOperation.Move(panel("doc1"), DockTarget.Edge(node(3), DockSide.Left), 1f)),
        )
    }
}
