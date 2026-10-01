package dev.cuervolu.loredesigner.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.key
import androidx.compose.runtime.staticCompositionLocalOf
import dev.cuervolu.loredesigner.core.settings.AppLanguage
import java.util.Locale

/**
 * Application locale override for Compose Resources.
 *
 * Compose Resources resolves strings from the platform locale and offers no public way to change it
 * at runtime, so this follows the JetBrains-recommended workaround: set the JVM default locale while
 * composing and recreate the localized subtree with `key` (see [ProvideAppLocale]).
 *
 * This is the only place that touches `Locale.getDefault()`. If the UI moves to Kotlin Multiplatform,
 * this object becomes an `expect` declaration with per-platform `actual`s; callers stay unchanged.
 */
object LocalAppLocale {
    private var systemLocale: Locale? = null
    private val LocalLocaleTag = staticCompositionLocalOf { Locale.getDefault().toLanguageTag() }

    /** BCP-47 tag of the locale in effect for the current composition. */
    val current: String
        @Composable get() = LocalLocaleTag.current

    /** Applies [tag] (or the operating system locale when `null`) and provides it to the composition. */
    @Composable
    infix fun provides(tag: String?): ProvidedValue<*> {
        val system = systemLocale ?: Locale.getDefault().also { systemLocale = it }
        val locale = if (tag == null) system else Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        return LocalLocaleTag provides locale.toLanguageTag()
    }
}

/**
 * Localization boundary. Everything inside [content] is recreated when [language] changes, so it
 * must not own session state: hoist back stacks, open workspaces and dialogs above this call.
 */
@Composable
fun ProvideAppLocale(language: AppLanguage, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppLocale provides language.tag) {
        key(language.tag) { content() }
    }
}
