package dev.cuervolu.loredesigner.ui.settings

import androidx.compose.runtime.Composable
import dev.cuervolu.loredesigner.ui.settings.pages.builtInSettingsPages
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SettingsPageRegistryTest {
    private val registry = SettingsPageRegistry(builtInSettingsPages())

    @Test
    fun `launcher context lists only application pages`() {
        val pages = registry.available(hasWorkspace = false)

        assertTrue(pages.isNotEmpty())
        assertTrue(pages.all { it.scope == SettingsScope.Application })
        assertEquals(
            listOf(
                "app.appearance",
                "app.editor",
                "app.language",
                "app.shortcuts",
                "app.files",
                "app.extensions",
                "app.diagnostics",
            ),
            pages.map { it.id },
        )
    }

    @Test
    fun `workspace context adds project pages after application pages`() {
        val pages = registry.available(hasWorkspace = true)

        assertEquals(
            registry.available(hasWorkspace = false),
            pages.takeWhile {
                it.scope == SettingsScope.Application
            },
        )
        assertEquals(
            listOf("project.general", "project.types", "project.workspace", "project.indexing", "project.maintenance"),
            pages.filter { it.scope == SettingsScope.Project }.map { it.id },
        )
    }

    @Test
    fun `contributed pages register like built-in ones and are grouped under extensions`() {
        val contributed = FakePage("ext.timeline", SettingsScope.Application, order = 0, contributor = "Timeline view")
        val projectContributed = FakePage("ext.atlas", SettingsScope.Project, order = 0, contributor = "World atlas")
        val pages = SettingsPageRegistry(builtInSettingsPages() + contributed + projectContributed)

        assertEquals(contributed, pages.available(hasWorkspace = false).last())
        assertEquals(
            listOf(contributed, projectContributed),
            pages.available(hasWorkspace = true).filter {
                it.group ==
                    SettingsGroup.Extensions
            },
        )
    }

    @Test
    fun `duplicate page ids are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            SettingsPageRegistry(
                listOf(FakePage("same", SettingsScope.Application), FakePage("same", SettingsScope.Project)),
            )
        }
    }
}

internal class FakePage(
    override val id: String,
    override val scope: SettingsScope,
    override val order: Int = 0,
    title: String = id,
    entries: List<Pair<String, String?>> = emptyList(),
    contributor: String? = null,
) : SettingsPage {
    override val title = SettingsText.Plain(title)
    override val searchEntries = entries.map { (label, keywords) ->
        SettingsSearchEntry(SettingsText.Plain(label), keywords?.let(SettingsText::Plain))
    }
    override val contributor: SettingsText? = contributor?.let(SettingsText::Plain)

    @Composable
    override fun Content(context: SettingsPageContext) = Unit
}
