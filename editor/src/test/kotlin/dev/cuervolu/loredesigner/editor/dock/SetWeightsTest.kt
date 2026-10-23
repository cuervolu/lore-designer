package dev.cuervolu.loredesigner.editor.dock

import kotlin.test.Test
import kotlin.test.assertEquals

class SetWeightsTest {
    @Test
    fun `weights are replaced and the tree is otherwise untouched`() {
        val result = baseLayout().applied(DockOperation.SetWeights(node(10), listOf(0.3f, 0.5f, 0.2f)))

        assertWeights(listOf(0.3f, 0.5f, 0.2f), (result.root as SplitNode).weights)
        assertEquals(baseLayout().groups, result.groups)
    }

    @Test
    fun `weights between two view groups can be redistributed`() {
        val layout = layoutOf(
            split(
                10,
                DockOrientation.Horizontal,
                group(1, PanelKind.View, "a") to 0.5f,
                group(2, PanelKind.View, "b") to 0.5f,
            ),
        )

        val result = layout.applied(DockOperation.SetWeights(node(10), listOf(0.25f, 0.75f)))

        assertWeights(listOf(0.25f, 0.75f), result.findSplit(node(10))!!.weights)
    }

    @Test
    fun `weights that do not sum to one are rejected`() {
        val weights = listOf(0.3f, 0.3f, 0.3f)

        assertEquals(
            DockRejection.InvalidWeights(node(10), weights),
            baseLayout().rejected(DockOperation.SetWeights(node(10), weights)),
        )
    }

    @Test
    fun `non positive weights are rejected`() {
        val weights = listOf(0f, 0.8f, 0.2f)

        assertEquals(
            DockRejection.InvalidWeights(node(10), weights),
            baseLayout().rejected(DockOperation.SetWeights(node(10), weights)),
        )
    }

    @Test
    fun `a wrong number of weights is rejected`() {
        val weights = listOf(0.5f, 0.5f)

        assertEquals(
            DockRejection.InvalidWeights(node(10), weights),
            baseLayout().rejected(DockOperation.SetWeights(node(10), weights)),
        )
    }

    @Test
    fun `an unknown split is rejected`() {
        assertEquals(
            DockRejection.UnknownSplit(node(99)),
            baseLayout().rejected(DockOperation.SetWeights(node(99), listOf(1f))),
        )
    }
}
