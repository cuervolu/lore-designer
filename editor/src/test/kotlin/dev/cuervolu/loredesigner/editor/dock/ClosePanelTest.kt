package dev.cuervolu.loredesigner.editor.dock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ClosePanelTest {
    private fun tabs(active: String) = layoutOf(
        split(
            10,
            DockOrientation.Horizontal,
            group(1, PanelKind.View, "a", "b", "c", active = active) to 0.5f,
            group(2, PanelKind.Tool, "tool") to 0.5f,
        ),
    )

    @Test
    fun `closing the active tab activates the next one`() {
        val result = tabs("b").applied(DockOperation.Close(panel("b")))

        assertEquals(listOf("a", "c").map(::panel), result.group(1).panels)
        assertEquals(panel("c"), result.group(1).activePanel)
    }

    @Test
    fun `closing the last active tab activates the previous one`() {
        assertEquals(panel("b"), tabs("c").applied(DockOperation.Close(panel("c"))).group(1).activePanel)
    }

    @Test
    fun `closing an inactive tab keeps the active one`() {
        assertEquals(panel("b"), tabs("b").applied(DockOperation.Close(panel("a"))).group(1).activePanel)
    }

    @Test
    fun `closing the only panel of a tool group removes the group and collapses the split`() {
        val result = tabs("a").applied(DockOperation.Close(panel("tool")))

        assertEquals(result.group(1), result.root)
    }

    @Test
    fun `closing a tool destroys it instead of keeping it hidden`() {
        val result = baseLayout().applied(DockOperation.Close(panel("explorer")))

        assertNull(result.panels[panel("explorer")])
        assertNull(result.groupOf(panel("explorer")))
        assertNull(result.findGroup(node(1)))
    }

    @Test
    fun `closing a tool group redistributes its weight proportionally`() {
        val result = baseLayout().applied(DockOperation.Close(panel("explorer")))

        val root = assertIs<SplitNode>(result.root)
        assertWeights(listOf(0.75f, 0.25f), root.weights)
    }

    @Test
    fun `closing the last view keeps an empty view group`() {
        val layout = layoutOf(group(1, PanelKind.View, "only"))

        val result = layout.applied(DockOperation.Close(panel("only")))

        assertEquals(PanelGroup(node(1), PanelKind.View, emptyList(), null), result.root)
        assertEquals(emptyMap(), result.panels)
    }

    @Test
    fun `an emptied view group is removed when another view group has content`() {
        val layout = layoutOf(
            split(
                10,
                DockOrientation.Horizontal,
                group(1, PanelKind.View, "a") to 0.5f,
                group(2, PanelKind.View, "b") to 0.5f,
            ),
        )

        val result = layout.applied(DockOperation.Close(panel("a")))

        assertEquals(result.group(2), result.root)
    }

    @Test
    fun `closing an unknown panel is rejected`() {
        assertEquals(
            DockRejection.UnknownPanel(panel("ghost")),
            baseLayout().rejected(DockOperation.Close(panel("ghost"))),
        )
    }
}
