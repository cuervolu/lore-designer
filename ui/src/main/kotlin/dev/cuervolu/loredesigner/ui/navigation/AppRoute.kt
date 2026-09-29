package dev.cuervolu.loredesigner.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

sealed interface AppRoute : NavKey {
    @Serializable
    data object Launcher : AppRoute

    @Serializable
    data class Workspace(val workspaceId: String) : AppRoute
}

// Outside Android the back stack cannot discover NavKey subtypes reflectively; they must be registered.
internal val AppRouteSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AppRoute.Launcher::class, AppRoute.Launcher.serializer())
            subclass(AppRoute.Workspace::class, AppRoute.Workspace.serializer())
        }
    }
}
