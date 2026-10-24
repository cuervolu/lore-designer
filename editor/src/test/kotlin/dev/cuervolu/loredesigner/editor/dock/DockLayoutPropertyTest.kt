package dev.cuervolu.loredesigner.editor.dock

import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Applies random operation sequences and checks the model after every step.
 *
 * Kotest picks the sequences from a fixed seed, so a run is repeatable, and shrinks a failing sequence to
 * the shortest one that still fails. Set `DOCK_PROPERTY_SEED` to replay the seed a failure reports.
 */
class DockLayoutPropertyTest {
    private val seed = System.getenv("DOCK_PROPERTY_SEED")?.toLongOrNull() ?: DEFAULT_SEED

    @Test
    fun `random operations from an empty layout keep the model consistent`() =
        checkSequences(DockLayout.initial { node(INITIAL_GROUP) })

    @Test
    fun `random operations from a populated layout keep the model consistent`() = checkSequences(baseLayout())

    private fun checkSequences(start: DockLayout) {
        runBlocking {
            checkAll(PropTestConfig(seed = seed, iterations = ITERATIONS), Arb.list(stepArb, 1..MAX_STEPS)) { steps ->
                start.run(steps)
            }
        }
    }

    /** Resolves [steps] against the evolving layout and verifies each result; failures carry the trace. */
    private fun DockLayout.run(steps: List<Step>): DockLayout {
        val ids = SequentialIds()
        var layout = this
        val trace = mutableListOf<String>()
        steps.forEachIndexed { index, step ->
            val operation = step.resolve(layout)
            trace += "#$index $operation"
            try {
                layout = verifyStep(layout, operation, ids)
            } catch (failure: Throwable) {
                fail("seed=$seed, ${failure.message}\nfrom $this\nsequence:\n${trace.joinToString("\n")}", failure)
            }
        }
        return layout
    }

    private fun verifyStep(before: DockLayout, operation: DockOperation, ids: DockIdGenerator): DockLayout =
        when (val result = before.apply(operation, ids)) {
            is DockResult.Applied -> {
                assertEquals(emptyList(), result.layout.validate(), "layout broke its invariants")
                assertTrue(
                    sameShape(result.layout.root, normalize(result.layout.root)),
                    "result is not normalized: normalizing it again changes ${result.layout.root}",
                )
                assertTrue(
                    illegalReasons(before, operation).isEmpty(),
                    "applied an operation that should be rejected: ${illegalReasons(before, operation)}",
                )
                verifyConservation(before, result.layout, operation)
                result.layout
            }

            is DockResult.Rejected -> {
                val expected = illegalReasons(before, operation)
                assertTrue(
                    expected.any { it.isInstance(result.reason) },
                    "rejected with ${result.reason}, expected one of $expected",
                )
                // A rejection must be free of side effects: the same layout still behaves the same.
                assertEquals(result, before.apply(operation, SequentialIds()))
                assertEquals(emptyList(), before.validate())
                before
            }
        }

    // Normalizing divides weights again, which moves them by float rounding; the structure must not change.
    private fun sameShape(a: DockNode, b: DockNode): Boolean = when {
        a is PanelGroup && b is PanelGroup -> a == b

        a is SplitNode && b is SplitNode ->
            a.id == b.id && a.orientation == b.orientation && a.children.size == b.children.size &&
                a.weights.zip(b.weights).all { (x, y) -> kotlin.math.abs(x - y) <= WEIGHT_TOLERANCE } &&
                a.children.zip(b.children).all { (x, y) -> sameShape(x, y) }

        else -> false
    }

    private fun verifyConservation(before: DockLayout, after: DockLayout, operation: DockOperation) {
        val expectedCatalog = when (operation) {
            is DockOperation.AddPanel -> before.panels + (operation.panel to operation.kind)
            is DockOperation.Close -> before.panels - operation.panel
            else -> before.panels
        }
        assertEquals(expectedCatalog, after.panels, "catalog drifted")
        assertEquals(
            after.groups.flatMap { it.panels }.toSet(),
            after.panels.keys,
            "placed panels differ from the catalog",
        )

        val moved = when (operation) {
            is DockOperation.AddPanel -> operation.panel
            is DockOperation.Move -> operation.panel
            is DockOperation.Split -> operation.panel
            is DockOperation.Close -> operation.panel
            is DockOperation.Activate, is DockOperation.SetWeights -> null
        }
        val untouched = before.panels.keys - setOfNotNull(moved)
        untouched.forEach { panel ->
            assertEquals(before.groupOf(panel)?.id, after.groupOf(panel)?.id, "$panel changed group")
        }
        before.groups.forEach { old ->
            val remaining = old.panels - setOfNotNull(moved)
            val now = after.findGroup(old.id)
            if (now != null) {
                assertEquals(remaining, now.panels - setOfNotNull(moved), "tabs of ${old.id} were reordered")
            } else {
                assertTrue(remaining.isEmpty(), "group ${old.id} vanished while still holding $remaining")
            }
        }

        when (operation) {
            is DockOperation.Activate -> {
                assertEquals(operation.panel, after.groupOf(operation.panel)?.activePanel)
                assertEquals(before.groups.map { it.id }, after.groups.map { it.id })
                assertEquals(
                    before.root.splits().map { it.id to it.weights },
                    after.root.splits().map {
                        it.id to
                            it.weights
                    },
                )
            }

            is DockOperation.SetWeights -> {
                assertEquals(operation.weights, after.findSplit(operation.split)?.weights)
                assertEquals(before.groups, after.groups)
            }

            is DockOperation.Move -> verifyPlacement(after, operation.panel, operation.target)

            is DockOperation.Split ->
                verifyPlacement(after, operation.panel, DockTarget.Edge(operation.group, operation.side))

            is DockOperation.AddPanel -> operation.target?.let { verifyPlacement(after, operation.panel, it) }

            is DockOperation.Close -> assertTrue(after.groups.none { operation.panel in it.panels })
        }
    }

    private fun verifyPlacement(after: DockLayout, panel: PanelId, target: DockTarget) {
        val group = checkNotNull(after.groupOf(panel)) { "$panel was not placed" }
        assertEquals(panel, group.activePanel, "a placed panel becomes active")
        when (target) {
            is DockTarget.Tab -> {
                assertEquals(target.group, group.id)
                val others = group.panels.size - 1
                assertEquals((target.index ?: others).coerceIn(0, others), group.panels.indexOf(panel))
            }

            is DockTarget.Edge -> assertEquals(listOf(panel), group.panels, "an edge drop creates a group of its own")
        }
    }

    /**
     * Every rejection that applies to [operation] on [layout]. The implementation checks them in some order;
     * which one it reports is not part of the contract, but at least one must, and none means it must apply.
     */
    private fun illegalReasons(layout: DockLayout, operation: DockOperation): List<KClass<*>> {
        val reasons = mutableListOf<KClass<*>>()
        fun add(reason: KClass<*>) {
            reasons += reason
        }

        fun checkTarget(panel: PanelId, kind: PanelKind, target: DockTarget?) {
            val group = target?.let { t ->
                layout.findGroup(
                    when (t) {
                        is DockTarget.Tab -> t.group
                        is DockTarget.Edge -> t.group
                    },
                )
            }
            if (target != null && group == null) add(DockRejection.UnknownGroup::class)
            if (target is DockTarget.Tab && group != null && group.kind != kind) add(DockRejection.KindMismatch::class)
            val source = layout.groupOf(panel)
            if (target is DockTarget.Edge && source != null && target.group == source.id && source.panels.size == 1) {
                add(DockRejection.SelfSplit::class)
            }
        }

        when (operation) {
            is DockOperation.AddPanel -> {
                if (operation.panel in layout.panels) add(DockRejection.DuplicatePanel::class)
                if (!DockSplitPolicy.isValidShare(operation.share)) add(DockRejection.InvalidShare::class)
                if (operation.target != null) {
                    // The panel is not placed yet, so only the target itself can be wrong.
                    checkTarget(operation.panel, operation.kind, operation.target)
                }
            }

            is DockOperation.Activate -> if (layout.groupOf(operation.panel) == null) {
                add(DockRejection.UnknownPanel::class)
            }

            is DockOperation.Close -> if (layout.groupOf(operation.panel) == null) {
                add(DockRejection.UnknownPanel::class)
            }

            is DockOperation.Move -> moveReasons(
                layout,
                operation.panel,
                operation.target,
                operation.share,
                ::add,
                ::checkTarget,
            )

            is DockOperation.Split -> moveReasons(
                layout,
                operation.panel,
                DockTarget.Edge(operation.group, operation.side),
                operation.share,
                ::add,
                ::checkTarget,
            )

            is DockOperation.SetWeights -> {
                val split = layout.findSplit(operation.split)
                when {
                    split == null -> add(DockRejection.UnknownSplit::class)

                    operation.weights.size != split.children.size ||
                        operation.weights.any { it <= 0f } ||
                        kotlin.math.abs(operation.weights.sum() - 1f) > WEIGHT_TOLERANCE ->
                        add(DockRejection.InvalidWeights::class)
                }
            }
        }
        return reasons
    }

    private fun moveReasons(
        layout: DockLayout,
        panel: PanelId,
        target: DockTarget,
        share: Float,
        add: (KClass<*>) -> Unit,
        checkTarget: (PanelId, PanelKind, DockTarget?) -> Unit,
    ) {
        val kind = layout.panels[panel]
        if (kind == null) {
            add(DockRejection.UnknownPanel::class)
            return
        }
        if (!DockSplitPolicy.isValidShare(share)) add(DockRejection.InvalidShare::class)
        checkTarget(panel, kind, target)
    }

    /**
     * A choice of operation made without looking at a layout, so the generator can shrink it. [resolve] turns
     * it into a concrete operation against the layout it will run on.
     */
    private data class Step(val op: Int, val a: Int, val b: Int, val c: Int, val d: Int) {
        fun resolve(layout: DockLayout): DockOperation {
            val panels = layout.panels.keys.sortedBy { it.value }
            fun existingPanel() = panels.getOrNull(a % (panels.size + 1)) ?: GHOST
            val share = SHARES[d % SHARES.size]
            return when (op) {
                in 0..2 -> DockOperation.AddPanel(
                    panel = PanelId("p${a % NAME_POOL}"),
                    kind = if (b % 3 == 0) PanelKind.View else PanelKind.Tool,
                    target = if (c % 4 == 0) null else target(layout, c / 4, d / SHARES.size),
                    share = share,
                )

                3 -> DockOperation.Activate(existingPanel())

                4 -> DockOperation.Close(existingPanel())

                in 5..8 -> DockOperation.Move(existingPanel(), target(layout, c, b), share)

                9, 10 -> DockOperation.Split(existingPanel(), groupId(layout, c), DockSide.entries[b % 4], share)

                else -> DockOperation.SetWeights(splitId(layout, a), weights(layout, a, b, c))
            }
        }

        private fun target(layout: DockLayout, selector: Int, variant: Int): DockTarget {
            val group = groupId(layout, selector)
            return when {
                variant % 3 == 0 -> DockTarget.Tab(group)
                variant % 3 == 1 -> DockTarget.Tab(group, index = variant / 3 % 5 - 1)
                else -> DockTarget.Edge(group, DockSide.entries[variant / 3 % 4])
            }
        }

        private fun groupId(layout: DockLayout, selector: Int): DockNodeId {
            val groups = layout.groups
            return groups.getOrNull(selector % (groups.size + 1))?.id ?: node(UNKNOWN_NODE)
        }

        private fun splitId(layout: DockLayout, selector: Int): DockNodeId {
            val splits = layout.root.splits()
            return splits.getOrNull(selector % (splits.size + 1))?.id ?: node(UNKNOWN_NODE)
        }

        private fun weights(layout: DockLayout, selector: Int, variant: Int, raw: Int): List<Float> {
            val count = layout.root.splits().getOrNull(selector % (layout.root.splits().size + 1))?.children?.size ?: 2
            return when (variant % 4) {
                0 -> List(count) { 1f / count }
                1 -> List(count) { index -> (index + 1 + raw % 3).toFloat() }.let { w -> w.map { it / w.sum() } }
                2 -> List(count + 1) { 1f / (count + 1) }
                else -> List(count) { if (it == 0) 0f else 1f / (count - 1).coerceAtLeast(1) }
            }
        }
    }

    private companion object {
        const val DEFAULT_SEED = 20261023L
        const val ITERATIONS = 400
        const val MAX_STEPS = 30
        const val NAME_POOL = 6
        const val INITIAL_GROUP = 100L
        const val UNKNOWN_NODE = 9_999L
        val GHOST = PanelId("ghost")
        val SHARES = listOf(0.5f, 0.25f, 0.75f, 0.1f, 0f, 1f, -0.5f, 1.5f, Float.NaN)

        val stepArb: Arb<Step> = Arb.bind(
            Arb.int(0..11),
            Arb.int(0..1000),
            Arb.int(0..1000),
            Arb.int(0..1000),
            Arb.int(0..1000),
            ::Step,
        )
    }
}
