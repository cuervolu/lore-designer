package dev.cuervolu.loredesigner.core.workspace

import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectConfigTest {

    @Test
    fun `stores project configuration`() {
        val id = WorkspaceId.parse(
            "01995f7e-1d74-7c83-a8a9-4fd2ed9cb380"
        )

        val config = ProjectConfig(
            version = 1,
            id = id,
            name = "The Drowned Court",
            color = ProjectColor.VIOLET,
        )

        assertEquals(1, config.version)
        assertEquals(id, config.id)
        assertEquals("The Drowned Court", config.name)
        assertEquals(ProjectColor.VIOLET, config.color)
    }
}