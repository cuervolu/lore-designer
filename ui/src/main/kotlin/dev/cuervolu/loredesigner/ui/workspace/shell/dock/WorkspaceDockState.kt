package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.editor.dock.DockLayout
import dev.cuervolu.loredesigner.editor.dock.DockOperation
import dev.cuervolu.loredesigner.editor.dock.DockResult
import dev.cuervolu.loredesigner.editor.dock.DockSide
import dev.cuervolu.loredesigner.editor.dock.DockTarget
import dev.cuervolu.loredesigner.editor.dock.PanelId
import dev.cuervolu.loredesigner.editor.dock.PanelKind

/**
 * The workspace's dock layout plus which panels the user has hidden.
 *
 * A hidden panel keeps its place in [layout] (group, tab, side), so
 * showing it again restores it exactly where it was. The window can still collapse a visible panel when
 * space is short, but that is decided while measuring and never recorded here.
 */
@Stable
class WorkspaceDockState(
    initialLayout: DockLayout = defaultLayout(),
    initiallyHidden: Set<PanelId> = setOf(ShellPanels.Log),
    private val logger: Logger = Logger.withTag("Dock"),
) {
    var layout: DockLayout by mutableStateOf(initialLayout)
        private set

    var hidden: Set<PanelId> by mutableStateOf(initiallyHidden)
        private set

    val explorerVisible: Boolean get() = isVisible(ShellPanels.Explorer)
    val propertiesVisible: Boolean get() = isVisible(ShellPanels.Properties)
    val bottomPanelVisible: Boolean get() = isVisible(ShellPanels.Log)

    fun isVisible(panel: PanelId): Boolean = panel !in hidden

    fun setVisible(panel: PanelId, visible: Boolean) {
        hidden = if (visible) hidden - panel else hidden + panel
    }

    fun toggle(panel: PanelId) = setVisible(panel, !isVisible(panel))

    fun toggleExplorer() = toggle(ShellPanels.Explorer)

    fun toggleProperties() = toggle(ShellPanels.Properties)

    fun toggleBottomPanel() = toggle(ShellPanels.Log)

    /** Brings [panel] to the front of its group, revealing it first if the user had hidden it. */
    fun activate(panel: PanelId) {
        when (val result = layout.apply(DockOperation.Activate(panel))) {
            is DockResult.Applied -> {
                layout = result.layout
                setVisible(panel, true)
            }

            is DockResult.Rejected -> logger.w { "Cannot activate $panel: ${result.reason}" }
        }
    }

    companion object {
        /** Explorer left, Properties right and Log under the editor area, built only with core operations. */
        fun defaultLayout(): DockLayout {
            var layout = DockLayout.initial()
            val editorArea = layout.groups.single().id
            fun add(panel: PanelId, side: DockSide) {
                val result = layout.apply(
                    DockOperation.AddPanel(panel, PanelKind.Tool, DockTarget.Edge(editorArea, side)),
                )
                layout = (result as DockResult.Applied).layout
            }
            add(ShellPanels.Explorer, DockSide.Left)
            add(ShellPanels.Properties, DockSide.Right)
            add(ShellPanels.Log, DockSide.Bottom)
            return layout
        }
    }
}
