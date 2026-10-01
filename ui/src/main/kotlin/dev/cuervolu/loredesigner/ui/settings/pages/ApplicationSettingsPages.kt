package dev.cuervolu.loredesigner.ui.settings.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.core.settings.AppLanguage
import dev.cuervolu.loredesigner.core.settings.AutosaveInterval
import dev.cuervolu.loredesigner.core.settings.DocumentFont
import dev.cuervolu.loredesigner.core.settings.RecoveryRetention
import dev.cuervolu.loredesigner.core.settings.ThemePreference
import dev.cuervolu.loredesigner.core.settings.UiDensity
import dev.cuervolu.loredesigner.ui.components.LoreButton
import dev.cuervolu.loredesigner.ui.components.LoreButtonSize
import dev.cuervolu.loredesigner.ui.components.LoreButtonVariant
import dev.cuervolu.loredesigner.ui.components.LoreKeycap
import dev.cuervolu.loredesigner.ui.components.LoreReadOnlyField
import dev.cuervolu.loredesigner.ui.components.LoreSelect
import dev.cuervolu.loredesigner.ui.components.LoreSelectOption
import dev.cuervolu.loredesigner.ui.components.LoreSwitch
import dev.cuervolu.loredesigner.ui.launcher.LauncherSection
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.settings_app_data
import dev.cuervolu.loredesigner.ui.resources.settings_app_data_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_app_data_location
import dev.cuervolu.loredesigner.ui.resources.settings_app_data_location_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_app_logs
import dev.cuervolu.loredesigner.ui.resources.settings_app_logs_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_autosave
import dev.cuervolu.loredesigner.ui.resources.settings_autosave_interval
import dev.cuervolu.loredesigner.ui.resources.settings_autosave_interval_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_autosave_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_density
import dev.cuervolu.loredesigner.ui.resources.settings_density_comfortable
import dev.cuervolu.loredesigner.ui.resources.settings_density_compact
import dev.cuervolu.loredesigner.ui.resources.settings_density_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_diagnostic_logging
import dev.cuervolu.loredesigner.ui.resources.settings_diagnostic_logging_helper
import dev.cuervolu.loredesigner.ui.resources.settings_diagnostic_logging_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_document_font
import dev.cuervolu.loredesigner.ui.resources.settings_document_font_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_extensions_description
import dev.cuervolu.loredesigner.ui.resources.settings_extensions_empty
import dev.cuervolu.loredesigner.ui.resources.settings_extensions_installed
import dev.cuervolu.loredesigner.ui.resources.settings_extensions_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_font_inter
import dev.cuervolu.loredesigner.ui.resources.settings_font_jetbrains_mono
import dev.cuervolu.loredesigner.ui.resources.settings_font_source_serif_4
import dev.cuervolu.loredesigner.ui.resources.settings_interval_30_seconds
import dev.cuervolu.loredesigner.ui.resources.settings_interval_5_minutes
import dev.cuervolu.loredesigner.ui.resources.settings_interval_every_change
import dev.cuervolu.loredesigner.ui.resources.settings_keep_recovery
import dev.cuervolu.loredesigner.ui.resources.settings_keep_recovery_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_key_cmd
import dev.cuervolu.loredesigner.ui.resources.settings_key_ctrl
import dev.cuervolu.loredesigner.ui.resources.settings_language
import dev.cuervolu.loredesigner.ui.resources.settings_language_english
import dev.cuervolu.loredesigner.ui.resources.settings_language_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_language_spanish
import dev.cuervolu.loredesigner.ui.resources.settings_language_system
import dev.cuervolu.loredesigner.ui.resources.settings_open_data_folder
import dev.cuervolu.loredesigner.ui.resources.settings_open_folder_failed
import dev.cuervolu.loredesigner.ui.resources.settings_open_logs
import dev.cuervolu.loredesigner.ui.resources.settings_page_appearance
import dev.cuervolu.loredesigner.ui.resources.settings_page_diagnostics
import dev.cuervolu.loredesigner.ui.resources.settings_page_editor
import dev.cuervolu.loredesigner.ui.resources.settings_page_extensions
import dev.cuervolu.loredesigner.ui.resources.settings_page_files
import dev.cuervolu.loredesigner.ui.resources.settings_page_language
import dev.cuervolu.loredesigner.ui.resources.settings_page_shortcuts
import dev.cuervolu.loredesigner.ui.resources.settings_recovery
import dev.cuervolu.loredesigner.ui.resources.settings_recovery_helper
import dev.cuervolu.loredesigner.ui.resources.settings_recovery_retention
import dev.cuervolu.loredesigner.ui.resources.settings_recovery_retention_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_reset
import dev.cuervolu.loredesigner.ui.resources.settings_reset_caches
import dev.cuervolu.loredesigner.ui.resources.settings_reset_caches_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_reset_caches_unavailable
import dev.cuervolu.loredesigner.ui.resources.settings_retention_1_day
import dev.cuervolu.loredesigner.ui.resources.settings_retention_30_days
import dev.cuervolu.loredesigner.ui.resources.settings_retention_7_days
import dev.cuervolu.loredesigner.ui.resources.settings_shortcut_go_to
import dev.cuervolu.loredesigner.ui.resources.settings_shortcut_new_project
import dev.cuervolu.loredesigner.ui.resources.settings_shortcuts_description
import dev.cuervolu.loredesigner.ui.resources.settings_shortcuts_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_spellcheck
import dev.cuervolu.loredesigner.ui.resources.settings_spellcheck_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_theme
import dev.cuervolu.loredesigner.ui.resources.settings_theme_dark
import dev.cuervolu.loredesigner.ui.resources.settings_theme_keywords
import dev.cuervolu.loredesigner.ui.resources.settings_theme_light
import dev.cuervolu.loredesigner.ui.resources.settings_theme_system
import dev.cuervolu.loredesigner.ui.settings.SettingsDescription
import dev.cuervolu.loredesigner.ui.settings.SettingsEyebrow
import dev.cuervolu.loredesigner.ui.settings.SettingsHelper
import dev.cuervolu.loredesigner.ui.settings.SettingsLabel
import dev.cuervolu.loredesigner.ui.settings.SettingsPage
import dev.cuervolu.loredesigner.ui.settings.SettingsPageContext
import dev.cuervolu.loredesigner.ui.settings.SettingsRow
import dev.cuervolu.loredesigner.ui.settings.SettingsScope
import dev.cuervolu.loredesigner.ui.settings.SettingsSearchEntry
import dev.cuervolu.loredesigner.ui.settings.SettingsSectionHeader
import dev.cuervolu.loredesigner.ui.settings.SettingsStackedRow
import dev.cuervolu.loredesigner.ui.settings.settingsDivider
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.documentFontFamily
import dev.cuervolu.loredesigner.ui.theme.typography
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal object AppearancePage : SettingsPage {
    override val id = "app.appearance"
    override val scope = SettingsScope.Application
    override val order = 0
    override val title = text(Res.string.settings_page_appearance)
    override val searchEntries = listOf(
        entry(Res.string.settings_theme, Res.string.settings_theme_keywords),
        entry(Res.string.settings_density, Res.string.settings_density_keywords),
    )

    @Composable
    override fun Content(context: SettingsPageContext) {
        val settings = context.state.settings
        val themeLabel = stringResource(Res.string.settings_theme)
        SettingsRow(themeLabel) {
            LoreSelect(
                value = settings.theme,
                options = listOf(
                    option(ThemePreference.SYSTEM, Res.string.settings_theme_system),
                    option(ThemePreference.LIGHT, Res.string.settings_theme_light),
                    option(ThemePreference.DARK, Res.string.settings_theme_dark),
                ),
                onValueChange = { theme -> context.viewModel.updateSettings { it.copy(theme = theme) } },
                accessibilityLabel = themeLabel,
            )
        }
        val densityLabel = stringResource(Res.string.settings_density)
        SettingsRow(densityLabel, divider = false) {
            LoreSelect(
                value = settings.density,
                options = listOf(
                    option(UiDensity.COMPACT, Res.string.settings_density_compact),
                    option(UiDensity.COMFORTABLE, Res.string.settings_density_comfortable),
                ),
                onValueChange = { density -> context.viewModel.updateSettings { it.copy(density = density) } },
                accessibilityLabel = densityLabel,
            )
        }
    }
}

internal object EditorPage : SettingsPage {
    override val id = "app.editor"
    override val scope = SettingsScope.Application
    override val order = 10
    override val title = text(Res.string.settings_page_editor)
    override val searchEntries = listOf(
        entry(Res.string.settings_autosave, Res.string.settings_autosave_keywords),
        entry(Res.string.settings_spellcheck, Res.string.settings_spellcheck_keywords),
        entry(Res.string.settings_document_font, Res.string.settings_document_font_keywords),
    )

    @Composable
    override fun Content(context: SettingsPageContext) {
        val settings = context.state.settings
        val autosave = stringResource(Res.string.settings_autosave)
        SettingsRow(autosave) {
            LoreSwitch(
                checked = settings.autosave,
                onCheckedChange = { value -> context.viewModel.updateSettings { it.copy(autosave = value) } },
                accessibilityLabel = autosave,
            )
        }
        val spellcheck = stringResource(Res.string.settings_spellcheck)
        SettingsRow(spellcheck) {
            LoreSwitch(
                checked = settings.spellcheck,
                onCheckedChange = { value -> context.viewModel.updateSettings { it.copy(spellcheck = value) } },
                accessibilityLabel = spellcheck,
            )
        }
        val fontLabel = stringResource(Res.string.settings_document_font)
        val body = Theme[typography][LoreTypography.body]
        SettingsRow(fontLabel, divider = false) {
            LoreSelect(
                value = settings.documentFont,
                options = DocumentFont.BuiltIn.map { font ->
                    LoreSelectOption(
                        font,
                        stringResource(font.label()),
                        body.copy(fontFamily = documentFontFamily(font)),
                    )
                },
                onValueChange = { font -> context.viewModel.updateSettings { it.copy(documentFont = font) } },
                accessibilityLabel = fontLabel,
            )
        }
    }

    private fun DocumentFont.label(): StringResource = when (this) {
        DocumentFont.Inter -> Res.string.settings_font_inter
        DocumentFont.JetBrainsMono -> Res.string.settings_font_jetbrains_mono
        else -> Res.string.settings_font_source_serif_4
    }
}

internal object LanguagePage : SettingsPage {
    override val id = "app.language"
    override val scope = SettingsScope.Application
    override val order = 20
    override val title = text(Res.string.settings_page_language)
    override val searchEntries = listOf(entry(Res.string.settings_language, Res.string.settings_language_keywords))

    @Composable
    override fun Content(context: SettingsPageContext) {
        val label = stringResource(Res.string.settings_language)
        SettingsRow(label, divider = false) {
            LoreSelect(
                value = context.state.settings.language,
                options = listOf(
                    option(AppLanguage.SYSTEM, Res.string.settings_language_system),
                    option(AppLanguage.ENGLISH, Res.string.settings_language_english),
                    option(AppLanguage.SPANISH, Res.string.settings_language_spanish),
                ),
                onValueChange = { language -> context.viewModel.updateSettings { it.copy(language = language) } },
                accessibilityLabel = label,
            )
        }
    }
}

/** Lists only shortcuts that are actually bound today (see `LauncherContent`). */
internal object ShortcutsPage : SettingsPage {
    private val sections = listOf(
        LauncherSection.Projects to "1",
        LauncherSection.Pinned to "2",
        LauncherSection.Recent to "3",
        LauncherSection.Missing to "4",
    )

    override val id = "app.shortcuts"
    override val scope = SettingsScope.Application
    override val order = 30
    override val title = text(Res.string.settings_page_shortcuts)
    override val searchEntries: List<SettingsSearchEntry> =
        listOf(entry(Res.string.settings_shortcut_new_project, Res.string.settings_shortcuts_keywords))

    @Composable
    override fun Content(context: SettingsPageContext) {
        val modifier = stringResource(if (isMacOS) Res.string.settings_key_cmd else Res.string.settings_key_ctrl)
        SettingsDescription(stringResource(Res.string.settings_shortcuts_description))
        Column {
            ShortcutRow(stringResource(Res.string.settings_shortcut_new_project), listOf(modifier, "N"))
            sections.forEachIndexed { index, (section, key) ->
                ShortcutRow(
                    label = stringResource(Res.string.settings_shortcut_go_to, stringResource(section.label)),
                    keys = listOf(modifier, key),
                    divider = index < sections.lastIndex,
                )
            }
        }
    }

    @Composable
    private fun ShortcutRow(label: String, keys: List<String>, divider: Boolean = true) {
        Row(
            modifier = Modifier.settingsDivider(divider).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SettingsLabel(label, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) { keys.forEach { LoreKeycap(it) } }
        }
    }

    private val isMacOS = System.getProperty("os.name").orEmpty().startsWith("Mac", ignoreCase = true)
}

internal object FilesPage : SettingsPage {
    override val id = "app.files"
    override val scope = SettingsScope.Application
    override val order = 40
    override val title = text(Res.string.settings_page_files)
    override val searchEntries = listOf(
        entry(Res.string.settings_autosave_interval, Res.string.settings_autosave_interval_keywords),
        entry(Res.string.settings_app_data_location, Res.string.settings_app_data_location_keywords),
        entry(
            Res.string.settings_keep_recovery,
            Res.string.settings_keep_recovery_keywords,
            Res.string.settings_recovery,
        ),
        entry(
            Res.string.settings_recovery_retention,
            Res.string.settings_recovery_retention_keywords,
            Res.string.settings_recovery,
        ),
    )

    @Composable
    override fun Content(context: SettingsPageContext) {
        val settings = context.state.settings
        val intervalLabel = stringResource(Res.string.settings_autosave_interval)
        SettingsRow(intervalLabel) {
            LoreSelect(
                value = settings.autosaveInterval,
                options = listOf(
                    option(AutosaveInterval.EVERY_CHANGE, Res.string.settings_interval_every_change),
                    option(AutosaveInterval.EVERY_30_SECONDS, Res.string.settings_interval_30_seconds),
                    option(AutosaveInterval.EVERY_5_MINUTES, Res.string.settings_interval_5_minutes),
                ),
                onValueChange = { interval ->
                    context.viewModel.updateSettings { it.copy(autosaveInterval = interval) }
                },
                accessibilityLabel = intervalLabel,
            )
        }
        val dataLabel = stringResource(Res.string.settings_app_data_location)
        SettingsStackedRow(dataLabel, divider = false) {
            LoreReadOnlyField(context.viewModel.applicationDataDirectory, accessibilityLabel = dataLabel)
        }
        SettingsSectionHeader(
            title = stringResource(Res.string.settings_recovery),
            helper = stringResource(Res.string.settings_recovery_helper),
            modifier = Modifier.padding(top = 20.dp),
        )
        val keepLabel = stringResource(Res.string.settings_keep_recovery)
        SettingsRow(keepLabel) {
            LoreSwitch(
                checked = settings.keepRecoveryCopies,
                onCheckedChange = { keep -> context.viewModel.updateSettings { it.copy(keepRecoveryCopies = keep) } },
                accessibilityLabel = keepLabel,
            )
        }
        val retentionLabel = stringResource(Res.string.settings_recovery_retention)
        SettingsRow(retentionLabel, divider = false, dimmed = !settings.keepRecoveryCopies) {
            LoreSelect(
                value = settings.recoveryRetention,
                options = listOf(
                    option(RecoveryRetention.ONE_DAY, Res.string.settings_retention_1_day),
                    option(RecoveryRetention.DAYS_7, Res.string.settings_retention_7_days),
                    option(RecoveryRetention.DAYS_30, Res.string.settings_retention_30_days),
                ),
                onValueChange = { retention ->
                    context.viewModel.updateSettings { it.copy(recoveryRetention = retention) }
                },
                accessibilityLabel = retentionLabel,
                enabled = settings.keepRecoveryCopies,
            )
        }
    }
}

/** No extension system exists yet, so the page only explains extensions and shows that none are installed. */
internal object ExtensionsPage : SettingsPage {
    override val id = "app.extensions"
    override val scope = SettingsScope.Application
    override val order = 50
    override val title = text(Res.string.settings_page_extensions)
    override val searchEntries =
        listOf(entry(Res.string.settings_extensions_installed, Res.string.settings_extensions_keywords))

    @Composable
    override fun Content(context: SettingsPageContext) {
        SettingsDescription(stringResource(Res.string.settings_extensions_description))
        Column(Modifier.settingsDivider().padding(bottom = 6.dp)) {
            SettingsEyebrow(stringResource(Res.string.settings_extensions_installed))
        }
        SettingsHelper(stringResource(Res.string.settings_extensions_empty), Modifier.padding(vertical = 10.dp))
    }
}

internal object DiagnosticsPage : SettingsPage {
    override val id = "app.diagnostics"
    override val scope = SettingsScope.Application
    override val order = 60
    override val title = text(Res.string.settings_page_diagnostics)
    override val searchEntries = listOf(
        entry(Res.string.settings_diagnostic_logging, Res.string.settings_diagnostic_logging_keywords),
        entry(Res.string.settings_app_logs, Res.string.settings_app_logs_keywords),
        entry(Res.string.settings_app_data, Res.string.settings_app_data_keywords),
        entry(Res.string.settings_reset_caches, Res.string.settings_reset_caches_keywords),
    )

    @Composable
    override fun Content(context: SettingsPageContext) {
        val logging = stringResource(Res.string.settings_diagnostic_logging)
        SettingsRow(logging, helper = stringResource(Res.string.settings_diagnostic_logging_helper)) {
            LoreSwitch(
                checked = context.state.settings.diagnosticLogging,
                onCheckedChange = { enabled ->
                    context.viewModel.updateSettings { it.copy(diagnosticLogging = enabled) }
                },
                accessibilityLabel = logging,
            )
        }
        SettingsRow(stringResource(Res.string.settings_app_logs)) {
            LoreButton(
                text = stringResource(Res.string.settings_open_logs),
                onClick = context.viewModel::openLogs,
                size = LoreButtonSize.Small,
            )
        }
        SettingsRow(stringResource(Res.string.settings_app_data)) {
            LoreButton(
                text = stringResource(Res.string.settings_open_data_folder),
                onClick = context.viewModel::openApplicationData,
                size = LoreButtonSize.Small,
            )
        }
        context.state.unopenedFolder?.let { folder ->
            SettingsHelper(stringResource(Res.string.settings_open_folder_failed, folder), Modifier.padding(top = 6.dp))
        }
        // Lore Designer has no local caches yet; the action stays disabled until it does.
        SettingsRow(
            label = stringResource(Res.string.settings_reset_caches),
            helper = stringResource(Res.string.settings_reset_caches_unavailable),
            divider = false,
        ) {
            LoreButton(
                text = stringResource(Res.string.settings_reset),
                onClick = {},
                variant = LoreButtonVariant.Danger,
                size = LoreButtonSize.Small,
                enabled = false,
            )
        }
    }
}

@Composable
private fun <T> option(value: T, label: StringResource) = LoreSelectOption(value, stringResource(label))
