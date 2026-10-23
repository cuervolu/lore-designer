package dev.cuervolu.loredesigner.editor.dock

import kotlin.test.Test
import kotlin.test.assertEquals

class ActivatePanelTest {
    @Test
    fun `activating switches only the owning group`() {
        val result = baseLayout().applied(DockOperation.Activate(panel("doc2")))

        assertEquals(panel("doc2"), result.group(2).activePanel)
        assertEquals(panel("explorer"), result.group(1).activePanel)
    }

    @Test
    fun `activating the active panel changes nothing`() {
        val layout = baseLayout()

        assertEquals(layout, layout.applied(DockOperation.Activate(panel("doc1"))))
    }

    @Test
    fun `activating an unknown panel is rejected`() {
        assertEquals(
            DockRejection.UnknownPanel(panel("ghost")),
            baseLayout().rejected(DockOperation.Activate(panel("ghost"))),
        )
    }
}
