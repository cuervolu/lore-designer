package dev.cuervolu.loredesigner.ui.workspace.shell

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShellLayoutTest {
    private fun layout(width: Int, height: Int, e: Boolean = true, p: Boolean = true, b: Boolean = true) =
        shellLayout(width.dp, height.dp, e, p, b)

    @Test
    fun `a roomy window uses the preferred sizes`() {
        assertEquals(ShellLayout(264.dp, 300.dp, 200.dp), layout(1440, 900))
    }

    @Test
    fun `hidden regions take no space`() {
        assertEquals(ShellLayout(0.dp, 0.dp, 0.dp), layout(1440, 900, e = false, p = false, b = false))
    }

    @Test
    fun `properties shrinks before explorer`() {
        val result = layout(width = 900, height = 600)

        assertEquals(264.dp, result.explorerWidth)
        assertEquals(276.dp, result.propertiesWidth)
    }

    @Test
    fun `explorer shrinks once properties reaches its floor`() {
        val result = layout(width = 800, height = 600)

        assertEquals(260.dp, result.explorerWidth)
        assertEquals(180.dp, result.propertiesWidth)
    }

    @Test
    fun `the center keeps its minimum width at the smallest window`() {
        listOf(800, 900, 1000, 1100).forEach { width ->
            val result = layout(width, 600)
            val center = width.dp - result.explorerWidth - result.propertiesWidth
            assertTrue(center >= 360.dp, "center is $center at width $width")
        }
    }

    @Test
    fun `side panels disappear when even their floors do not fit`() {
        val result = layout(width = 500, height = 600)

        assertEquals(0.dp, result.propertiesWidth)
        assertEquals(0.dp, result.explorerWidth)
    }

    @Test
    fun `the bottom panel yields height to the center`() {
        assertEquals(200.dp, layout(1200, 520).bottomHeight)
        assertEquals(120.dp, layout(1200, 280).bottomHeight)
        assertEquals(0.dp, layout(1200, 220).bottomHeight)
    }
}
