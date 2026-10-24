package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.moveTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performMouseInput
import dev.cuervolu.loredesigner.editor.dock.PanelId
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The dock is specified in dp, so every gesture and measurement here is written in dp and run at 1x and 2x.
 * A constant that is really a pixel count passes at 1x and breaks at 2x, which is the regression to catch.
 */
@OptIn(ExperimentalTestApi::class)
class DockDensityTest {
    private fun assertNear(expected: Float, actual: Float, density: Float, message: String) = assertTrue(
        abs(expected - actual) <= TOLERANCE_DP,
        "$message at ${density}x: expected $expected dp but was $actual dp",
    )

    private fun ComposeUiTest.separatorTag(state: WorkspaceDockState, panel: PanelId) =
        "dock-separator-${checkNotNull(state.layout.groupOf(panel)).id.value}"

    /** Root-pixel position of [dp] along the window's x axis. */
    private fun ComposeUiTest.atX(dp: Float, density: Float, y: Float = 400f) = Offset(dp * density, y * density)

    @Test
    fun `the harness applies the density it is given`() = forEachDensity { density ->
        showDock(WorkspaceDockState())

        assertEquals(WINDOW_WIDTH_DP * density, windowPx().width, "window width in px at ${density}x")
        assertEquals(264f, boundsDp("content-explorer", density).width, "explorer width in dp at ${density}x")
        assertEquals(264f * density, boundsPx("content-explorer").width, "explorer width in px at ${density}x")
    }

    @Test
    fun `a movement under the drag threshold is a click at every density`() = forEachDensity { density ->
        val state = WorkspaceDockState(layoutWithExplorerTabs())
        showDock(state)
        val before = state.layout

        pressAndMove("dock-tab-explorer", dp = UNDER_THRESHOLD_DP, density)
        onNodeWithTag("dock-drag-ghost").assertDoesNotExist()
        release("dock-tab-explorer")

        assertEquals(before.groups.map { it.panels }, state.layout.groups.map { it.panels }, "layout at ${density}x")
        assertEquals(
            ShellPanels.Explorer,
            state.layout.groupOf(ShellPanels.Explorer)?.activePanel,
            "click at ${density}x",
        )
    }

    @Test
    fun `a movement past the drag threshold starts a drag at every density`() = forEachDensity { density ->
        val state = WorkspaceDockState(layoutWithExplorerTabs())
        showDock(state)

        pressAndMove("dock-tab-explorer", dp = PAST_THRESHOLD_DP, density)

        onNodeWithTag("dock-drag-ghost").assertExists()
        release("dock-tab-explorer")
        onNodeWithTag("dock-drag-ghost").assertDoesNotExist()
    }

    @Test
    fun `the window-edge strip is the same width in dp at every density`() = forEachDensity { density ->
        val state = WorkspaceDockState()
        showDock(state)
        val window = windowPx()

        pressAndMove("dock-tab-explorer", dp = PAST_THRESHOLD_DP, density)
        moveOver("dock-tab-explorer", Offset(window.right - 10f * density, window.center.y))

        val zone = boundsDp("dock-drop-edge", density)
        assertNear(EDGE_ZONE_DP, zone.width, density, "edge strip width")
        assertNear(WINDOW_WIDTH_DP.toFloat(), zone.right, density, "edge strip right edge")
        release("dock-tab-explorer")
    }

    @Test
    fun `a pointer just inside the edge strip docks at the window edge and just outside it does not`() =
        forEachDensity { density ->
            val state = WorkspaceDockState()
            showDock(state)
            val window = windowPx()

            pressAndMove("dock-tab-explorer", dp = PAST_THRESHOLD_DP, density)
            moveOver("dock-tab-explorer", Offset(window.right - (EDGE_ZONE_DP - 4f) * density, window.center.y))
            onNodeWithTag("dock-drop-edge").assertExists()

            moveOver("dock-tab-explorer", Offset(window.right - (EDGE_ZONE_DP + 4f) * density, window.center.y))
            onNodeWithTag("dock-drop-edge").assertDoesNotExist()
            onNodeWithTag("dock-drop-valid").assertExists()
            release("dock-tab-explorer")
        }

    @Test
    fun `dropping in the edge strip creates the same layout at every density`() {
        val results = mutableListOf<List<List<PanelId>>>()
        forEachDensity { density ->
            val state = WorkspaceDockState()
            showDock(state)
            val window = windowPx()

            pressAndMove("dock-tab-explorer", dp = PAST_THRESHOLD_DP, density)
            moveOver("dock-tab-explorer", Offset(window.right - 8f * density, window.center.y))
            release("dock-tab-explorer")

            results += state.layout.groups.map { it.panels }
        }
        assertEquals(1, results.distinct().size, "layouts differ between densities: $results")
        assertTrue(results.first().any { it == listOf(ShellPanels.Explorer) })
    }

    @Test
    fun `a separator is nine dp wide and centred on its one dp line`() = forEachDensity { density ->
        val state = WorkspaceDockState()
        showDock(state)

        val grab = boundsDp(separatorTag(state, ShellPanels.Explorer), density)
        val explorer = boundsDp("content-explorer", density)

        assertNear(9f, grab.width, density, "separator width")
        assertNear(explorer.right + 0.5f, (grab.left + grab.right) / 2, density, "separator centre")
        assertNear(WINDOW_HEIGHT_DP.toFloat(), grab.height, density, "separator height")
    }

    @Test
    fun `the separator grab area reaches four dp either side of the line and no further`() = forEachDensity { density ->
        val state = WorkspaceDockState()
        showDock(state)
        val line = boundsDp("content-explorer", density).right

        // A press on the separator moves it; the same press just outside lands on panel content instead.
        onRoot().performMouseInput {
            moveTo(atX(line - 3f, density))
            press()
            moveBy(Offset(40f * density, 0f))
            release()
        }
        waitForIdle()
        assertNear(264f + 40f, boundsDp("content-explorer", density).width, density, "width after grabbing inside")

        val moved = boundsDp("content-explorer", density).right
        onRoot().performMouseInput {
            moveTo(atX(moved - 6f, density))
            press()
            moveBy(Offset(40f * density, 0f))
            release()
        }
        waitForIdle()
        assertNear(264f + 40f, boundsDp("content-explorer", density).width, density, "width after grabbing outside")
    }

    @Test
    fun `dragging a separator resizes by the same dp at every density`() = forEachDensity { density ->
        val state = WorkspaceDockState()
        showDock(state)

        onNodeWithTag(separatorTag(state, ShellPanels.Explorer)).performMouseInput {
            moveTo(center)
            press()
            moveBy(Offset(60f * density, 0f))
            release()
        }
        waitForIdle()

        assertNear(264f + 60f, boundsDp("content-explorer", density).width, density, "explorer width")
        assertNear(300f, boundsDp("content-properties", density).width, density, "properties width stays put")
    }

    @Test
    fun `a panel cannot be dragged below its minimum width and collapses only past seventy percent of it`() =
        forEachDensity { density ->
            val state = WorkspaceDockState()
            showDock(state)
            val tag = separatorTag(state, ShellPanels.Explorer)
            val min = 180f

            // Asked for 264 - 100 = 164 dp: under the minimum but over the collapse threshold, so it stops at 180.
            onNodeWithTag(tag).performMouseInput {
                moveTo(center)
                press()
                moveBy(Offset(-100f * density, 0f))
            }
            waitForIdle()
            assertNear(min, boundsDp("content-explorer", density).width, density, "width held at the minimum")
            onNodeWithTag(tag).performMouseInput { release() }
            waitForIdle()
            assertTrue(
                state.explorerVisible,
                "explorer must survive a drag that stays above the collapse threshold at ${density}x",
            )

            // Asked for 264 - 150 = 114 dp: under 0.7 * 180 = 126 dp, so releasing collapses it.
            onNodeWithTag(tag).performMouseInput {
                moveTo(center)
                press()
                moveBy(Offset(-150f * density, 0f))
                release()
            }
            waitForIdle()
            assertTrue(!state.explorerVisible, "explorer must collapse past the threshold at ${density}x")
        }

    @Test
    fun `the insertion line is three dp wide and sits where the dragged tab would land`() = forEachDensity { density ->
        val state = WorkspaceDockState(layoutWithExplorerTabs())
        showDock(state)
        val bar = boundsPx("dock-tabbar-${checkNotNull(state.layout.groupOf(ShellPanels.Explorer)).id.value}")

        pressAndMove("dock-tab-explorer", dp = PAST_THRESHOLD_DP, density)
        moveOver("dock-tab-explorer", Offset(bar.right - 4f * density, bar.center.y))

        val line = boundsDp("dock-drop-indicator", density)
        assertNear(3f, line.width, density, "insertion line width")
        assertNear(boundsDp("dock-tab-extra", density).right, (line.left + line.right) / 2, density, "insertion line x")
        release("dock-tab-explorer")
    }

    @Test
    fun `a pointer left of a tab's centre inserts before it at every density`() = forEachDensity { density ->
        assertEquals(listOf(ShellPanels.Explorer, extraPanel), dropOnExtraTab(offsetFromCentreDp = -2f, density))
    }

    @Test
    fun `a pointer right of a tab's centre inserts after it at every density`() = forEachDensity { density ->
        assertEquals(listOf(extraPanel, ShellPanels.Explorer), dropOnExtraTab(offsetFromCentreDp = 2f, density))
    }

    /** Drags the explorer tab onto the extra tab and returns the order of the group it ends up in. */
    private fun ComposeUiTest.dropOnExtraTab(offsetFromCentreDp: Float, density: Float): List<PanelId> {
        val state = WorkspaceDockState(layoutWithExplorerTabs())
        showDock(state)
        val extra = boundsPx("dock-tab-extra")
        pressAndMove("dock-tab-explorer", dp = PAST_THRESHOLD_DP, density)
        moveOver("dock-tab-explorer", Offset(extra.center.x + offsetFromCentreDp * density, extra.center.y))
        release("dock-tab-explorer")
        return checkNotNull(state.layout.groupOf(ShellPanels.Explorer)).panels
    }

    private companion object {
        const val TOLERANCE_DP = 0.5f
        const val UNDER_THRESHOLD_DP = 3f
        const val PAST_THRESHOLD_DP = 8f
        const val EDGE_ZONE_DP = 24f
    }
}
