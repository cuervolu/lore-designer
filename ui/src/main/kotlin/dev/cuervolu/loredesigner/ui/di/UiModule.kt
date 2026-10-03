package dev.cuervolu.loredesigner.ui.di

import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.ui.settings.SettingsPageRegistry
import dev.cuervolu.loredesigner.ui.settings.SettingsViewModel
import dev.cuervolu.loredesigner.ui.settings.SettingsViewModelFactory
import dev.cuervolu.loredesigner.ui.settings.data.ApplicationSettingsRepository
import dev.cuervolu.loredesigner.ui.settings.pages.builtInSettingsPages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

/** UI state holders and settings pages. [dev.cuervolu.loredesigner.ui.settings.SettingsEnvironment] is bound by the app. */
fun uiModule(): Module = module {
    single {
        ApplicationSettingsRepository(
            store = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            logger = get<Logger>().withTag("Settings"),
        )
    }
    single { SettingsPageRegistry(builtInSettingsPages()) }
    single<SettingsViewModelFactory> {
        SettingsViewModelFactory { workspace -> SettingsViewModel(get(), get(), workspace, get(), get()) }
    }
}
