package dev.cuervolu.loredesigner.ui.chrome

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WindowTitleTest {
    @Test
    fun `without a project the title is just the application`() {
        val title = windowTitle(document = null, project = null, appName = "Lore Designer")

        assertEquals("Lore Designer", title.plain)
        assertNull(title.context)
    }

    @Test
    fun `an open project without a document leads with the project`() {
        val title = windowTitle(document = null, project = "Embercourt", appName = "Lore Designer")

        assertEquals("Embercourt", title.emphasis)
        assertEquals("Lore Designer", title.context)
        assertEquals("Embercourt — Lore Designer", title.plain)
    }

    @Test
    fun `an active document leads, followed by project and application`() {
        val title = windowTitle(document = "Vessa Ardenhal", project = "Embercourt", appName = "Lore Designer")

        assertEquals("Vessa Ardenhal", title.emphasis)
        assertEquals("Embercourt — Lore Designer", title.context)
        assertEquals("Vessa Ardenhal — Embercourt — Lore Designer", title.plain)
    }

    @Test
    fun `a document without a project is ignored`() {
        val title = windowTitle(document = "Vessa Ardenhal", project = null, appName = "Lore Designer")

        assertEquals("Lore Designer", title.plain)
    }
}
