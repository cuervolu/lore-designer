package dev.cuervolu.loredesigner.ui.settings

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Where a page applies, application pages are always available, project pages need an open workspace. */
enum class SettingsScope { Application, Project }

/** Sidebar group a page is listed under. */
enum class SettingsGroup { Application, Project, Extensions }

/**
 * User-visible text of a page. Built-in pages use bundled resources; [Plain] exists for pages
 * contributed at runtime (e.g. by extensions) that ship their own already-localized text.
 */
sealed interface SettingsText {
    data class Resource(val resource: StringResource) : SettingsText

    data class Plain(val text: String) : SettingsText
}

@Composable
fun SettingsText.resolve(): String = when (this) {
    is SettingsText.Resource -> stringResource(resource)
    is SettingsText.Plain -> text
}

/** A searchable setting on a page. [subsection] is shown in the result path, e.g. "Files › Recovery". */
data class SettingsSearchEntry(
    val label: SettingsText,
    val keywords: SettingsText? = null,
    val subsection: SettingsText? = null,
)

/**
 * A page of the Settings modal. Built-in pages and future contributed pages register the same way,
 * through [SettingsPageRegistry]; the shell renders whatever is registered and available.
 */
interface SettingsPage {
    /** Stable identifier, unique across all registered pages. */
    val id: String
    val scope: SettingsScope

    /** Position within its sidebar group; lower comes first. */
    val order: Int
    val title: SettingsText
    val searchEntries: List<SettingsSearchEntry>

    /** Name of whatever contributed the page; non-null pages are listed under "Extensions". */
    val contributor: SettingsText? get() = null

    @Composable
    fun Content(context: SettingsPageContext)
}

val SettingsPage.group: SettingsGroup
    get() = when {
        contributor != null -> SettingsGroup.Extensions
        scope == SettingsScope.Project -> SettingsGroup.Project
        else -> SettingsGroup.Application
    }

class SettingsPageRegistry(pages: List<SettingsPage>) {
    init {
        val duplicates = pages.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(duplicates.isEmpty()) { "Duplicate settings page ids: $duplicates" }
    }

    private val pages: List<SettingsPage> = pages.sortedWith(compareBy({ it.group }, { it.order }))

    /** Pages for the current context, in sidebar order. Project pages need an open workspace. */
    fun available(hasWorkspace: Boolean): List<SettingsPage> =
        pages.filter { it.scope == SettingsScope.Application || hasWorkspace }
}
