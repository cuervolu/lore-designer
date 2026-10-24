@file:OptIn(ExperimentalTestApi::class)

package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.moveTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import dev.cuervolu.loredesigner.editor.dock.DockLayout
import dev.cuervolu.loredesigner.editor.dock.DockOperation
import dev.cuervolu.loredesigner.editor.dock.DockResult
import dev.cuervolu.loredesigner.editor.dock.DockTarget
import dev.cuervolu.loredesigner.editor.dock.PanelId
import dev.cuervolu.loredesigner.editor.dock.PanelKind
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme

internal const val WINDOW_WIDTH_DP = 1000
internal const val WINDOW_HEIGHT_DP = 800

/** The densities the dock is checked at: a standard display and a 2x (Retina, HiDPI) one. */
internal val Densities = listOf(1f, 2f)

/**
 * Runs [block] once per density with a scene whose size in dp is the same at each, so gestures written in dp
 * must produce the same result everywhere. The scene density is real: pixel input and bounds scale with it.
 */
internal fun forEachDensity(block: SkikoComposeUiTest.(density: Float) -> Unit) {
    Densities.forEach { density ->
        runSkikoComposeUiTest(
            size = Size(WINDOW_WIDTH_DP * density, WINDOW_HEIGHT_DP * density),
            density = Density(density),
        ) { block(density) }
    }
}

internal val extraPanel = PanelId("extra")

private val registry: PanelRegistry = ShellPanels.Sizing.mapValues { (id, sizing) -> spec(id.value, sizing) } +
    (extraPanel to spec("extra", ShellPanels.Sizing.getValue(ShellPanels.Explorer)))

private fun spec(name: String, sizing: PanelSizing) = DockPanelSpec(
    label = { name },
    content = { Box(Modifier.fillMaxSize().testTag("content-$name")) { Text("body $name") } },
    sizing = sizing,
)

/** The default shell layout with [extraPanel] sharing the explorer's group, so that group shows a tab strip. */
internal fun layoutWithExplorerTabs(): DockLayout {
    val base = WorkspaceDockState.defaultLayout()
    val explorerGroup = checkNotNull(base.groupOf(ShellPanels.Explorer)).id
    val result = base.apply(DockOperation.AddPanel(extraPanel, PanelKind.Tool, DockTarget.Tab(explorerGroup)))
    return (result as DockResult.Applied).layout
}

internal fun ComposeUiTest.showDock(state: WorkspaceDockState) = setContent {
    LoreDesignerTheme(darkTheme = false) {
        Box(Modifier.size(WINDOW_WIDTH_DP.dp, WINDOW_HEIGHT_DP.dp)) {
            DockHost(state, registry, viewEmpty = { Box(Modifier.fillMaxSize().testTag("editor-area")) })
        }
    }
}

internal fun ComposeUiTest.boundsPx(tag: String): Rect = onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot

/** Bounds in dp, derived from the pixel bounds so a density that is not applied shows up as a wrong size. */
internal fun ComposeUiTest.boundsDp(tag: String, density: Float): Rect = boundsPx(tag).let {
    Rect(it.left / density, it.top / density, it.right / density, it.bottom / density)
}

internal fun ComposeUiTest.windowPx(): Rect = onRoot().fetchSemanticsNode().boundsInRoot

/** Presses [source] and moves [dp] along x (in dp), leaving the button down. */
internal fun ComposeUiTest.pressAndMove(source: String, dp: Float, density: Float) {
    onNodeWithTag(source).performMouseInput {
        moveTo(center)
        press()
        moveBy(Offset(dp * density, 0f))
    }
    waitForIdle()
}

/** Moves the pressed pointer to [target], given in root pixels, while [source] has the gesture. */
internal fun ComposeUiTest.moveOver(source: String, target: Offset) {
    val origin = boundsPx(source).topLeft
    onNodeWithTag(source).performMouseInput { moveTo(target - origin) }
    waitForIdle()
}

internal fun ComposeUiTest.release(source: String) {
    onNodeWithTag(source).performMouseInput { release() }
    waitForIdle()
}
