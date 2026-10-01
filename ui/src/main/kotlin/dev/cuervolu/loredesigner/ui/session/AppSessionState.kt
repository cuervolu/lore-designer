package dev.cuervolu.loredesigner.ui.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import dev.cuervolu.loredesigner.ui.navigation.AppRoute
import dev.cuervolu.loredesigner.ui.navigation.AppRouteSavedStateConfiguration
import dev.cuervolu.loredesigner.workspace.Workspace

/** Whether the shared Settings modal is showing; any chrome (launcher, workspace) opens the same one. */
@Stable
class SettingsModalState {
    var isOpen: Boolean by mutableStateOf(false)
        private set

    fun open() {
        isOpen = true
    }

    fun dismiss() {
        isOpen = false
    }
}

/**
 * State that belongs to the running session rather than to how it is presented.
 *
 * It is created above the localization boundary so that recreating the localized UI (for example
 * after a language change) keeps navigation, the open workspace, the Settings modal and the
 * session-scoped ViewModels intact.
 *
 * `rememberSaveable` state of navigation entries is deliberately not hoisted: Compose disposes the
 * previous NavDisplay's entries only after the recreated one has registered them, so a shared
 * `SaveableStateHolder` would see every entry key twice. Such state is presentation-only (scroll
 * offsets, selected tabs) and resets when the language changes.
 */
@Stable
class AppSessionState internal constructor(val backStack: NavBackStack<NavKey>) {
    var activeWorkspace: Workspace? by mutableStateOf(null)

    val settingsModal = SettingsModalState()

    /** Owns ViewModels that live outside navigation entries, such as the Settings modal's. */
    val viewModelStoreOwner: ViewModelStoreOwner = object : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }
}

@Composable
fun rememberAppSessionState(): AppSessionState {
    val backStack = rememberNavBackStack(AppRouteSavedStateConfiguration, AppRoute.Launcher)
    val session = remember(backStack) { AppSessionState(backStack) }
    DisposableEffect(session) {
        onDispose { session.viewModelStoreOwner.viewModelStore.clear() }
    }
    return session
}
