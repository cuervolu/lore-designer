package dev.cuervolu.loredesigner.ui.settings.pages

import dev.cuervolu.loredesigner.ui.settings.SettingsPage
import dev.cuervolu.loredesigner.ui.settings.SettingsSearchEntry
import dev.cuervolu.loredesigner.ui.settings.SettingsText
import org.jetbrains.compose.resources.StringResource

/** Pages shipped with the application, registered through the same registry as contributed pages. */
fun builtInSettingsPages(): List<SettingsPage> = listOf(
    AppearancePage,
    EditorPage,
    LanguagePage,
    ShortcutsPage,
    FilesPage,
    ExtensionsPage,
    DiagnosticsPage,
    ProjectGeneralPage,
    ProjectTypesPage,
    ProjectWorkspacePage,
    ProjectIndexingPage,
    ProjectMaintenancePage,
)

internal fun text(resource: StringResource) = SettingsText.Resource(resource)

internal fun entry(label: StringResource, keywords: StringResource? = null, subsection: StringResource? = null) =
    SettingsSearchEntry(text(label), keywords?.let(::text), subsection?.let(::text))
