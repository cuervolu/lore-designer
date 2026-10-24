package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import dev.cuervolu.loredesigner.editor.dock.DockLayout
import dev.cuervolu.loredesigner.editor.dock.DockOperation
import dev.cuervolu.loredesigner.editor.dock.DockResult
import dev.cuervolu.loredesigner.editor.dock.DockTarget
import dev.cuervolu.loredesigner.editor.dock.PanelId
import dev.cuervolu.loredesigner.editor.dock.PanelKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DockDropTest {
    private val a = PanelId("a")
    private val b = PanelId("b")
    private val c = PanelId("c")

    @Test
    fun `the drag threshold is exceeded only beyond five pixels`() {
        val origin = Offset(10f, 10f)

        assertFalse(exceedsDragThreshold(origin, origin))
        assertFalse(exceedsDragThreshold(origin, Offset(14.9f, 10f)))
        assertFalse(exceedsDragThreshold(origin, Offset(13f, 14f)))
        assertTrue(exceedsDragThreshold(origin, Offset(15.1f, 10f)))
        assertTrue(exceedsDragThreshold(origin, Offset(14f, 14f)))
    }

    @Test
    fun `hidden panels are skipped when mapping a visible slot to a layout index`() {
        val panels = listOf(a, b, c)

        assertEquals(1, layoutInsertIndex(panels, dragged = c, hidden = setOf(b), others = listOf(a), slot = 1))
        assertEquals(0, layoutInsertIndex(panels, dragged = c, hidden = setOf(b), others = listOf(a), slot = 0))
        assertEquals(1, layoutInsertIndex(panels, dragged = a, hidden = setOf(c), others = listOf(b), slot = 1))
        assertEquals(2, layoutInsertIndex(panels, dragged = a, hidden = emptySet(), others = listOf(b, c), slot = 2))
    }

    @Test
    fun `a group with nothing visible but the dragged panel receives it at the end`() {
        assertEquals(1, layoutInsertIndex(listOf(a, b), dragged = a, hidden = setOf(b), others = emptyList(), slot = 0))
    }

    private fun layout(): DockLayout {
        val base = WorkspaceDockState.defaultLayout()
        val explorer = checkNotNull(base.groupOf(ShellPanels.Explorer)).id
        return (base.apply(DockOperation.AddPanel(a, PanelKind.Tool, DockTarget.Tab(explorer))) as DockResult.Applied)
            .layout
    }

    private fun area(layout: DockLayout, group: PanelId, x: Float): GroupArea {
        val id = checkNotNull(layout.groupOf(group)).id
        val slots = checkNotNull(layout.findGroup(id)).panels.mapIndexed { i, panel ->
            TabSlot(panel, Rect(x + i * 100f, 0f, x + i * 100f + 100f, 30f))
        }
        return GroupArea(id, Rect(x, 0f, x + 400f, 300f), Rect(x, 0f, x + 400f, 30f), slots)
    }

    @Test
    fun `outside every group nothing resolves`() {
        val layout = layout()

        assertEquals(
            DropResolution.None,
            resolveDrop(Offset(900f, 900f), a, layout, emptySet(), listOf(area(layout, ShellPanels.Explorer, 0f))),
        )
    }

    @Test
    fun `a tool over a view group is invalid`() {
        val layout = layout()
        val view = checkNotNull(layout.groups.single { it.kind == PanelKind.View })
        val areas = listOf(GroupArea(view.id, Rect(0f, 0f, 400f, 300f), null, emptyList()))

        assertIs<DropResolution.Invalid>(resolveDrop(Offset(10f, 100f), a, layout, emptySet(), areas))
    }

    @Test
    fun `an area for a group that no longer exists is never a target`() {
        val layout = layout()
        val stale = area(layout, ShellPanels.Properties, 0f)
        val smaller = (layout.apply(DockOperation.Close(ShellPanels.Properties)) as DockResult.Applied).layout

        assertEquals(DropResolution.None, resolveDrop(Offset(10f, 10f), a, smaller, emptySet(), listOf(stale)))
    }

    @Test
    fun `over the tab bar the slot follows the pointer and ignores the dragged tab`() {
        val layout = layout()
        val areas = listOf(area(layout, ShellPanels.Explorer, 0f))

        val first = resolveDrop(Offset(10f, 10f), a, layout, emptySet(), areas)
        val last = resolveDrop(Offset(350f, 10f), ShellPanels.Explorer, layout, emptySet(), areas)

        assertEquals(0, assertIs<DropResolution.Valid>(first).target.index)
        assertEquals(1, assertIs<DropResolution.Valid>(last).target.index)
        assertEquals(0f, first.indicatorX)
    }

    @Test
    fun `over the body of its own group a panel stays where it is`() {
        val layout = layout()
        val areas = listOf(area(layout, ShellPanels.Explorer, 0f))

        val result =
            assertIs<DropResolution.Valid>(
                resolveDrop(Offset(10f, 200f), ShellPanels.Explorer, layout, emptySet(), areas),
            )

        assertEquals(0, result.target.index)
        assertEquals(null, result.indicatorX)
    }
}
