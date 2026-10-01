package dev.cuervolu.loredesigner.ui.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsSearchTest {
    private val appearance = FakePage(
        "app.appearance",
        SettingsScope.Application,
        title = "Appearance",
        entries = listOf("Theme" to "dark light system", "Density" to "compact comfortable"),
    )
    private val general = FakePage(
        "project.general",
        SettingsScope.Project,
        title = "General",
        entries = listOf("Project name" to "rename", "Project color" to "colour"),
    )
    private val maintenance = FakePage(
        "project.maintenance",
        SettingsScope.Project,
        title = "Maintenance",
        entries = listOf("Reset local project cache" to "cache clear"),
    )
    private val registry = SettingsPageRegistry(listOf(appearance, general, maintenance))
    private val resolve = { text: SettingsText -> (text as SettingsText.Plain).text }

    @Test
    fun `launcher context never returns project results`() {
        val groups = searchSettings(registry.available(hasWorkspace = false), "project", resolve)

        assertTrue(groups.isEmpty())
    }

    @Test
    fun `workspace context returns project results grouped after application ones`() {
        val groups = searchSettings(registry.available(hasWorkspace = true), "e", resolve)

        assertEquals(listOf(SettingsGroup.Application, SettingsGroup.Project), groups.map { it.group })
    }

    @Test
    fun `matches keywords and locates the query in the label`() {
        val byKeyword = searchSettings(
            registry.available(hasWorkspace = false),
            "DARK",
            resolve,
        ).single().results.single()
        val byLabel = searchSettings(
            registry.available(hasWorkspace = true),
            "color",
            resolve,
        ).single().results.single()

        assertEquals("Theme", byKeyword.label)
        assertEquals(-1, byKeyword.matchStart)
        assertEquals("Project color", byLabel.label)
        assertEquals(8, byLabel.matchStart)
        assertEquals(5, byLabel.matchLength)
        assertEquals("project.general", byLabel.pageId)
        assertEquals("General", byLabel.pageTitle)
    }

    @Test
    fun `blank queries return nothing`() {
        assertTrue(searchSettings(registry.available(hasWorkspace = true), "   ", resolve).isEmpty())
    }
}
