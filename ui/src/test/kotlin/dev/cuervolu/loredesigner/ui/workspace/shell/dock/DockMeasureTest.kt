package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.cuervolu.loredesigner.editor.dock.DockNodeId
import dev.cuervolu.loredesigner.editor.dock.DockOrientation
import dev.cuervolu.loredesigner.editor.dock.PanelKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockMeasureTest {
    private val explorer = MeasureChild(AxisSizing(264.dp, 180.dp), weight = 0.2f, shrinkOrder = 1)
    private val center = MeasureChild(AxisSizing(null, 360.dp), weight = 0.6f, shrinkOrder = 0)
    private val properties = MeasureChild(AxisSizing(300.dp, 180.dp), weight = 0.2f, shrinkOrder = 0)
    private val horizontal = listOf(explorer, center, properties)

    private fun measure(width: Int, children: List<MeasureChild> = horizontal): List<Dp> =
        measureSplit(children, width.dp)

    @Test
    fun `fixed panels keep their preferred size while the center absorbs growth`() {
        listOf(1200, 1440, 2000).forEach { width ->
            val sizes = measure(width)

            assertEquals(264.dp, sizes[0])
            assertEquals(300.dp, sizes[2])
            assertEquals(width.dp - 264.dp - 300.dp - 2.dp, sizes[1])
        }
    }

    @Test
    fun `properties shrinks before explorer`() {
        val sizes = measure(900)

        assertEquals(264.dp, sizes[0])
        assertEquals(274.dp, sizes[2])
        assertEquals(360.dp, sizes[1])
    }

    @Test
    fun `explorer shrinks once properties reaches its floor`() {
        val sizes = measure(800)

        assertEquals(180.dp, sizes[2])
        assertEquals(258.dp, sizes[0])
        assertEquals(360.dp, sizes[1])
    }

    @Test
    fun `side panels collapse in shrink order when their floors do not fit`() {
        val oneSideGone = measure(width = 700)
        assertEquals(0.dp, oneSideGone[2])
        assertEquals(180.dp, oneSideGone[0])

        val bothGone = measure(width = 300)
        assertEquals(0.dp, bothGone[0])
        assertEquals(0.dp, bothGone[2])
        assertEquals(300.dp, bothGone[1])
    }

    @Test
    fun `no size is ever negative`() {
        listOf(0, 50, 200, 400, 700, 1000).forEach { width ->
            measure(width).forEach { assertTrue(it >= 0.dp, "negative size at width $width") }
        }
    }

    @Test
    fun `the bottom panel keeps its height and yields before the center`() {
        val log = MeasureChild(AxisSizing(200.dp, 96.dp), weight = 0.3f, shrinkOrder = 0)
        val editor = MeasureChild(AxisSizing(null, 160.dp), weight = 0.7f, shrinkOrder = 0)
        val column = listOf(editor, log)

        assertEquals(200.dp, measureSplit(column, 800.dp)[1])
        assertEquals(200.dp, measureSplit(column, 520.dp)[1])
        assertEquals(119.dp, measureSplit(column, 280.dp)[1])
        assertEquals(0.dp, measureSplit(column, 220.dp)[1])
    }

    @Test
    fun `fill siblings share the remaining space by weight`() {
        val a = MeasureChild(AxisSizing(null, 100.dp), weight = 0.25f, shrinkOrder = 0)
        val b = MeasureChild(AxisSizing(null, 100.dp), weight = 0.75f, shrinkOrder = 0)

        val sizes = measureSplit(listOf(a, b), 801.dp)

        assertEquals(200.dp, sizes[0])
        assertEquals(600.dp, sizes[1])
    }

    @Test
    fun `without a fill child the last panel takes the leftover space`() {
        val sizes = measureSplit(listOf(explorer, properties), 1000.dp)

        assertEquals(264.dp, sizes[0])
        assertEquals(735.dp, sizes[1])
    }

    @Test
    fun `a dragged size replaces the preferred one and gives way when the window shrinks`() {
        val wider = explorer.copy(sizing = AxisSizing(400.dp, 180.dp))
        val children = listOf(wider, center, properties)

        assertEquals(400.dp, measure(1400, children)[0])
        assertEquals(358.dp, measure(900, children)[0])
        assertEquals(400.dp, measure(1400, children)[0])
    }

    @Test
    fun `a column mixing a fixed width panel with a fill panel stays fixed width`() {
        val registry = ShellPanels.Sizing.mapValues { (id, sizing) ->
            DockPanelSpec(label = { id.value }, content = {}, sizing = sizing)
        }
        val column = RenderSplit(
            id = DockNodeId(kotlin.uuid.Uuid.random()),
            orientation = DockOrientation.Vertical,
            children = listOf(
                RenderGroup(
                    DockNodeId(kotlin.uuid.Uuid.random()),
                    PanelKind.Tool,
                    listOf(ShellPanels.Properties),
                    ShellPanels.Properties,
                ),
                RenderGroup(
                    DockNodeId(kotlin.uuid.Uuid.random()),
                    PanelKind.Tool,
                    listOf(ShellPanels.Log),
                    ShellPanels.Log,
                ),
            ),
            weights = listOf(0.5f, 0.5f),
        )

        assertEquals(300.dp, column.sizingAlong(DockOrientation.Horizontal, registry).preferred)
        assertEquals(null, column.sizingAlong(DockOrientation.Vertical, registry).preferred)
    }
}
