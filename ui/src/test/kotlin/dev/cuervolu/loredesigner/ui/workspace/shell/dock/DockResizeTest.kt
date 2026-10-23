package dev.cuervolu.loredesigner.ui.workspace.shell.dock

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DockResizeTest {
    private val explorer = AxisSizing(264.dp, 180.dp)
    private val center = AxisSizing(null, 360.dp)
    private val properties = AxisSizing(300.dp, 180.dp)
    private val sizings = listOf(explorer, center, properties)

    @Test
    fun `a separator moves the fixed neighbour and leaves the flexible one to absorb`() {
        assertEquals(SeparatorPlan.Fixed(0, growsWithDelta = true), planSeparator(sizings, 0, 1))
        assertEquals(SeparatorPlan.Fixed(2, growsWithDelta = false), planSeparator(sizings, 1, 2))
    }

    @Test
    fun `a separator between two flexible neighbours trades space between them`() {
        assertEquals(SeparatorPlan.Fill(0, 1), planSeparator(listOf(center, center), 0, 1))
    }

    @Test
    fun `two fixed neighbours move the leading one`() {
        val plan = planSeparator(listOf(explorer, properties), 0, 1)

        assertIs<SeparatorPlan.Fixed>(plan)
        assertEquals(0, plan.index)
    }

    @Test
    fun `growth is limited by the room left after every other minimum`() {
        assertEquals(1000.dp - 2.dp - 360.dp - 180.dp, maxFixedSize(sizings, 0, 1000.dp))
    }

    @Test
    fun `the limit never drops below the panel's own minimum`() {
        assertEquals(180.dp, maxFixedSize(sizings, 0, 400.dp))
    }

    @Test
    fun `dragging keeps the size within minimum and maximum`() {
        assertEquals(180.dp, dragFixed(264.dp, (-100).dp, 180.dp, 458.dp).size)
        assertEquals(458.dp, dragFixed(264.dp, 900.dp, 180.dp, 458.dp).size)
        assertEquals(300.dp, dragFixed(264.dp, 36.dp, 180.dp, 458.dp).size)
    }

    @Test
    fun `collapse starts below seventy percent of the minimum`() {
        assertFalse(dragFixed(264.dp, (-264 + 126).dp, 180.dp, 458.dp).collapsing)
        assertTrue(dragFixed(264.dp, (-264 + 125).dp, 180.dp, 458.dp).collapsing)
    }

    @Test
    fun `collapse uses the requested size even though the applied size stops at the minimum`() {
        val result = dragFixed(264.dp, (-400).dp, 180.dp, 458.dp)

        assertEquals(180.dp, result.size)
        assertEquals((-136).dp, result.requested)
        assertTrue(result.collapsing)
    }

    @Test
    fun `a panel without a minimum cannot collapse`() {
        assertFalse(dragFixed(100.dp, (-500).dp, 0.dp, 400.dp).collapsing)
    }

    @Test
    fun `flexible neighbours share their combined size proportionally`() {
        assertEquals(0.525f, dragFillShare(400.dp, 400.dp, 20.dp, 360.dp, 360.dp), 1e-4f)
        assertEquals(0.475f, dragFillShare(400.dp, 400.dp, (-20).dp, 360.dp, 360.dp), 1e-4f)
    }

    @Test
    fun `flexible neighbours keep their minimums`() {
        assertEquals(360f / 800f, dragFillShare(400.dp, 400.dp, (-300).dp, 360.dp, 360.dp), 1e-4f)
        assertEquals(440f / 800f, dragFillShare(400.dp, 400.dp, 300.dp, 360.dp, 360.dp), 1e-4f)
    }
}
