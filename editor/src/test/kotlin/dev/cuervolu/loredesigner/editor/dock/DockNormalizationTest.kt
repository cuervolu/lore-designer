package dev.cuervolu.loredesigner.editor.dock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DockNormalizationTest {
    @Test
    fun `a split left with one child collapses into that child`() {
        val layout = layoutOf(
            split(
                10,
                DockOrientation.Vertical,
                group(1, PanelKind.View, "doc") to 0.5f,
                split(
                    11,
                    DockOrientation.Horizontal,
                    group(2, PanelKind.Tool, "a") to 0.5f,
                    group(3, PanelKind.Tool, "b") to 0.5f,
                ) to 0.5f,
            ),
        )

        val result = layout.applied(DockOperation.Close(panel("a")))

        val root = assertIs<SplitNode>(result.root)
        assertEquals(listOf(node(1), node(3)), root.children.map { it.id })
        assertWeights(listOf(0.5f, 0.5f), root.weights)
    }

    @Test
    fun `collapsing cascades through nested splits`() {
        val layout = layoutOf(
            split(
                10,
                DockOrientation.Horizontal,
                group(1, PanelKind.View, "doc") to 0.5f,
                split(
                    11,
                    DockOrientation.Vertical,
                    split(
                        12,
                        DockOrientation.Horizontal,
                        group(2, PanelKind.Tool, "a") to 0.5f,
                        group(3, PanelKind.Tool, "b") to 0.5f,
                    ) to 0.5f,
                    group(4, PanelKind.Tool, "c") to 0.5f,
                ) to 0.5f,
            ),
        )

        val result = layout.applied(DockOperation.Close(panel("c")))

        val root = assertIs<SplitNode>(result.root)
        assertEquals(listOf(node(1), node(2), node(3)), root.children.map { it.id })
        assertWeights(listOf(0.5f, 0.25f, 0.25f), root.weights)
    }

    @Test
    fun `a child split with the parent orientation is flattened with proportional weights`() {
        val layout = layoutOf(
            split(
                10,
                DockOrientation.Horizontal,
                group(1, PanelKind.View, "doc") to 0.5f,
                split(
                    11,
                    DockOrientation.Vertical,
                    group(2, PanelKind.View, "x") to 0.5f,
                    split(
                        12,
                        DockOrientation.Horizontal,
                        group(3, PanelKind.Tool, "a") to 0.5f,
                        group(4, PanelKind.Tool, "b") to 0.5f,
                    ) to 0.5f,
                ) to 0.5f,
            ),
        )

        val result = layout.applied(DockOperation.Close(panel("x")))

        val root = assertIs<SplitNode>(result.root)
        assertEquals(node(10), root.id)
        assertEquals(listOf(node(1), node(3), node(4)), root.children.map { it.id })
        assertWeights(listOf(0.5f, 0.25f, 0.25f), root.weights)
    }

    @Test
    fun `an emptied tool group inside a split is dropped`() {
        val result = baseLayout().applied(DockOperation.Close(panel("props")))

        assertEquals(listOf(node(1), node(2)), assertIs<SplitNode>(result.root).children.map { it.id })
    }

    @Test
    fun `when every view group is empty exactly one survives`() {
        val layout = layoutOf(
            split(
                10,
                DockOrientation.Horizontal,
                group(1, PanelKind.View, "a") to 0.5f,
                group(2, PanelKind.View, "b") to 0.5f,
            ),
        )

        val result = layout
            .applied(DockOperation.Close(panel("a")))
            .applied(DockOperation.Close(panel("b")))

        assertEquals(listOf(node(2)), result.groups.map { it.id })
        assertEquals(emptyList(), result.groups.single().panels)
    }

    @Test
    fun `an empty view group is replaced when content is added elsewhere`() {
        val result = layoutOf(group(1, PanelKind.View))
            .applied(DockOperation.AddPanel(panel("doc"), PanelKind.View))

        assertEquals(listOf(panel("doc")), result.group(1).panels)
        assertEquals(panel("doc"), result.group(1).activePanel)
    }

    @Test
    fun `weights always sum to one after a sequence of operations`() {
        val ids = SequentialIds()
        var layout = baseLayout()
        listOf(
            DockOperation.Split(panel("doc2"), node(2), DockSide.Bottom, 0.3f),
            DockOperation.Split(panel("props"), node(2), DockSide.Right, 0.4f),
            DockOperation.Close(panel("explorer")),
            DockOperation.AddPanel(panel("logs"), PanelKind.Tool),
        ).forEach { layout = layout.applied(it, ids) }

        fun check(node: DockNode) {
            if (node is SplitNode) {
                assertEquals(1f, node.weights.sum(), 1e-4f)
                node.children.forEach(::check)
            }
        }
        check(layout.root)
    }
}
