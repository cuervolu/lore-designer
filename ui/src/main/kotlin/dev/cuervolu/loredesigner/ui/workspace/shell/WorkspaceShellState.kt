package dev.cuervolu.loredesigner.ui.workspace.shell

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** The user's preference for which shell regions are shown; the layout may still hide one when space is short. */
@Stable
class WorkspaceShellState {
    var explorerVisible: Boolean by mutableStateOf(true)
        private set
    var propertiesVisible: Boolean by mutableStateOf(true)
        private set

    /** Off by default: the log is opened on demand from the View menu. */
    var bottomPanelVisible: Boolean by mutableStateOf(false)
        private set

    fun toggleExplorer() {
        explorerVisible = !explorerVisible
    }

    fun toggleProperties() {
        propertiesVisible = !propertiesVisible
    }

    fun toggleBottomPanel() {
        bottomPanelVisible = !bottomPanelVisible
    }
}
