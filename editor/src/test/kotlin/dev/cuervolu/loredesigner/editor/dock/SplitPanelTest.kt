package dev.cuervolu.loredesigner.editor.dock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SplitPanelTest {
    private fun root(layout: DockLayout) = assertIs<SplitNode>(layout.root)

    @Test
    fun `splitting along the parent orientation inserts a sibling that takes half of the anchor`() {
        val result = baseLayout().applied(DockOperation.Split(panel("doc2"), node(2), DockSide.Right))

        val root = root(result)
        assertEquals(DockOrientation.Horizontal, root.orientation)
        assertEquals(listOf(node(1), node(2)), root.children.take(2).map { it.id })
        assertEquals(listOf(panel("doc2")), assertIs<PanelGroup>(root.children[2]).panels)
        assertWeights(listOf(0.2f, 0.3f, 0.3f, 0.2f), root.weights)
    }

    @Test
    fun `a custom share gives the new group that fraction of the anchor`() {
        val result = baseLayout().applied(DockOperation.Split(panel("doc1"), node(3), DockSide.Right, share = 0.25f))

        assertWeights(listOf(0.2f, 0.6f, 0.15f, 0.05f), root(result).weights)
    }

    @Test
    fun `splitting on a leading side puts the new group first`() {
        val result = baseLayout().applied(DockOperation.Split(panel("doc2"), node(2), DockSide.Left))

        val root = root(result)
        assertEquals(listOf(panel("doc2")), assertIs<PanelGroup>(root.children[1]).panels)
        assertEquals(node(2), root.children[2].id)
        assertWeights(listOf(0.2f, 0.3f, 0.3f, 0.2f), root.weights)
    }

    @Test
    fun `splitting across the parent orientation nests a new split and keeps the parent weights`() {
        val result = baseLayout().applied(DockOperation.Split(panel("doc2"), node(2), DockSide.Bottom))

        val root = root(result)
        assertWeights(listOf(0.2f, 0.6f, 0.2f), root.weights)
        val nested = assertIs<SplitNode>(root.children[1])
        assertEquals(DockOrientation.Vertical, nested.orientation)
        assertEquals(node(2), nested.children[0].id)
        assertWeights(listOf(0.5f, 0.5f), nested.weights)
    }

    @Test
    fun `an uneven share is applied to the nested split with the new group last on trailing sides`() {
        val result = baseLayout().applied(DockOperation.Split(panel("doc2"), node(2), DockSide.Bottom, share = 0.3f))

        assertWeights(listOf(0.7f, 0.3f), assertIs<SplitNode>(root(result).children[1]).weights)
    }

    @Test
    fun `an uneven share on a leading side puts the small weight first`() {
        val result = baseLayout().applied(DockOperation.Split(panel("doc2"), node(2), DockSide.Top, share = 0.3f))

        val nested = assertIs<SplitNode>(root(result).children[1])
        assertWeights(listOf(0.3f, 0.7f), nested.weights)
        assertEquals(listOf(panel("doc2")), assertIs<PanelGroup>(nested.children[0]).panels)
    }

    @Test
    fun `splitting a lone root group wraps it`() {
        val layout = layoutOf(group(1, PanelKind.View, "doc1", "doc2"))

        val result = layout.applied(DockOperation.Split(panel("doc2"), node(1), DockSide.Left, share = 0.3f))

        val root = root(result)
        assertEquals(DockOrientation.Horizontal, root.orientation)
        assertEquals(listOf(panel("doc2")), assertIs<PanelGroup>(root.children[0]).panels)
        assertEquals(node(1), root.children[1].id)
        assertWeights(listOf(0.3f, 0.7f), root.weights)
    }

    @Test
    fun `the new group takes the kind of the moved panel`() {
        val result = baseLayout().applied(DockOperation.Split(panel("explorer"), node(2), DockSide.Top))
        val created = result.groupOf(panel("explorer"))
        assertEquals(PanelKind.Tool, created?.kind)
    }

    @Test
    fun `splitting the only panel of a group against itself is rejected`() {
        assertEquals(
            DockRejection.SelfSplit(panel("props"), node(3)),
            baseLayout().rejected(DockOperation.Split(panel("props"), node(3), DockSide.Left)),
        )
    }

    @Test
    fun `repeated splits of the same orientation stay flat`() {
        val ids = SequentialIds()
        val result = layoutOf(group(1, PanelKind.View, "a", "b", "c"))
            .applied(DockOperation.Split(panel("b"), node(1), DockSide.Right), ids)
            .applied(DockOperation.Split(panel("c"), node(1), DockSide.Right), ids)

        val root = root(result)
        assertEquals(3, root.children.size)
        assertWeights(listOf(0.25f, 0.25f, 0.5f), root.weights)
    }
}
