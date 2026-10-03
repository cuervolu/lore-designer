package dev.cuervolu.loredesigner.ui.launcher

import androidx.compose.runtime.staticCompositionLocalOf

/** Desktop integrations the launcher offers for a project; implemented by the application. */
interface ProjectSystemActions {
    /**
     * Reveals the project folder at [projectLocation] by opening the folder that contains it.
     * Returns `false` when no file manager could be opened.
     */
    suspend fun showInFolder(projectLocation: String): Boolean

    /** Returns `false` when the system clipboard refused the text. */
    suspend fun copyText(text: String): Boolean
}

val LocalProjectSystemActions = staticCompositionLocalOf<ProjectSystemActions> {
    error("No ProjectSystemActions provided")
}
