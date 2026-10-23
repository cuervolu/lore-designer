package dev.cuervolu.loredesigner.editor.dock

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DockInvariantsTest {
    private val view = group(1, PanelKind.View, "doc")

    private fun violations(layout: DockLayout) = layout.validate()

    @Test
    fun `fixtures and the initial layout are valid`() {
        assertEquals(emptyList(), baseLayout().validate())
        assertEquals(emptyList(), DockLayout.initial().validate())
    }

    @Test
    fun `detects a duplicated panel`() {
        val layout = layoutOf(
            split(10, DockOrientation.Horizontal, view to 0.5f, group(2, PanelKind.View, "doc") to 0.5f),
        )

        assertContains(violations(layout), DockViolation.DuplicatePanel(panel("doc")))
    }

    @Test
    fun `detects a duplicated node id`() {
        val layout = layoutOf(
            split(10, DockOrientation.Horizontal, view to 0.5f, group(1, PanelKind.View, "other") to 0.5f),
        )

        assertContains(violations(layout), DockViolation.DuplicateNodeId(node(1)))
    }

    @Test
    fun `detects panels missing from the catalog or from the tree`() {
        val layout = DockLayout(view, mapOf(panel("ghost") to PanelKind.View))

        assertContains(violations(layout), DockViolation.PanelNotInCatalog(panel("doc")))
        assertContains(violations(layout), DockViolation.PanelNotPlaced(panel("ghost")))
    }

    @Test
    fun `detects a panel in a group of another kind`() {
        val layout = DockLayout(view, mapOf(panel("doc") to PanelKind.Tool))

        assertContains(violations(layout), DockViolation.WrongPanelKind(panel("doc"), node(1)))
    }

    @Test
    fun `detects an invalid active panel`() {
        val outside = layoutOf(group(1, PanelKind.View, "doc", active = "other"))
        val missing = layoutOf(group(1, PanelKind.View, "doc", active = null))
        val onEmpty = DockLayout(PanelGroup(node(1), PanelKind.View, emptyList(), panel("doc")), emptyMap())

        listOf(outside, missing, onEmpty).forEach {
            assertContains(violations(it), DockViolation.InvalidActivePanel(node(1)))
        }
    }

    @Test
    fun `detects an empty tool group and a layout without a view group`() {
        val layout = DockLayout(PanelGroup(node(1), PanelKind.Tool, emptyList(), null), emptyMap())

        assertContains(violations(layout), DockViolation.EmptyToolGroup(node(1)))
        assertContains(violations(layout), DockViolation.NoViewGroup)
    }

    @Test
    fun `detects redundant empty view groups`() {
        val withContent = layoutOf(
            split(10, DockOrientation.Horizontal, view to 0.5f, group(2, PanelKind.View) to 0.5f),
        )
        val allEmpty = layoutOf(
            split(10, DockOrientation.Horizontal, group(1, PanelKind.View) to 0.5f, group(2, PanelKind.View) to 0.5f),
        )

        assertContains(violations(withContent), DockViolation.RedundantEmptyViewGroup(node(2)))
        assertEquals(listOf(DockViolation.RedundantEmptyViewGroup(node(2))), violations(allEmpty))
    }

    @Test
    fun `detects malformed splits`() {
        val single = layoutOf(split(10, DockOrientation.Horizontal, view to 1f))
        val mismatch = layoutOf(
            SplitNode(node(10), DockOrientation.Horizontal, listOf(view, group(2, PanelKind.View, "b")), listOf(1f)),
        )
        val nonPositive = layoutOf(
            split(10, DockOrientation.Horizontal, view to 0f, group(2, PanelKind.View, "b") to 1f),
        )
        val unnormalized = layoutOf(
            split(10, DockOrientation.Horizontal, view to 0.5f, group(2, PanelKind.View, "b") to 0.6f),
        )
        val nested = layoutOf(
            split(
                10,
                DockOrientation.Horizontal,
                view to 0.5f,
                split(
                    11,
                    DockOrientation.Horizontal,
                    group(2, PanelKind.View, "b") to 0.5f,
                    group(3, PanelKind.View, "c") to 0.5f,
                ) to 0.5f,
            ),
        )

        assertContains(violations(single), DockViolation.TooFewChildren(node(10)))
        assertContains(violations(mismatch), DockViolation.WeightCountMismatch(node(10)))
        assertContains(violations(nonPositive), DockViolation.NonPositiveWeight(node(10)))
        assertContains(violations(unnormalized), DockViolation.WeightsNotNormalized(node(10)))
        assertContains(violations(nested), DockViolation.SameOrientationNesting(node(11)))
    }

    @Test
    fun `operations reject a corrupt layout instead of repairing it`() {
        val corrupt = layoutOf(split(10, DockOrientation.Horizontal, view to 1f))

        val reason = corrupt.rejected(DockOperation.Activate(panel("doc")))

        val invalid = assertIs<DockRejection.InvalidLayout>(reason)
        assertContains(invalid.violations, DockViolation.TooFewChildren(node(10)))
    }

    @Test
    fun `every operation is rejected on a corrupt layout`() {
        val corrupt = layoutOf(split(10, DockOrientation.Horizontal, view to 1f))

        listOf(
            DockOperation.AddPanel(panel("new"), PanelKind.View),
            DockOperation.Close(panel("doc")),
            DockOperation.Move(panel("doc"), DockTarget.Tab(node(1))),
            DockOperation.Split(panel("doc"), node(1), DockSide.Left),
        ).forEach { assertIs<DockRejection.InvalidLayout>(corrupt.rejected(it)) }
    }

    @Test
    fun `every operation on every panel and target keeps the layout valid`() {
        val layout = baseLayout()
        val targets = layout.groups.flatMap { g ->
            listOf<DockTarget>(DockTarget.Tab(g.id), DockTarget.Tab(g.id, 0)) +
                DockSide.entries.map { DockTarget.Edge(g.id, it) }
        }
        var applied = 0
        for (p in layout.panels.keys) {
            for (target in targets) {
                val result = layout.apply(DockOperation.Move(p, target), SequentialIds())
                if (result is DockResult.Applied) {
                    applied++
                    assertEquals(emptyList(), result.layout.validate())
                    assertEquals(layout.panels, result.layout.panels)
                    assertEquals(
                        layout.groups.flatMap { it.panels }.toSet(),
                        result.layout.groups.flatMap { it.panels }.toSet(),
                    )
                }
            }
            assertEquals(emptyList(), layout.applied(DockOperation.Close(p)).validate())
            assertEquals(emptyList(), layout.applied(DockOperation.Activate(p)).validate())
        }
        assertTrue(applied > 0)
    }
}
