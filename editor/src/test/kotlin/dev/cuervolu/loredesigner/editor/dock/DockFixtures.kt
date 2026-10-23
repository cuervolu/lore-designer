package dev.cuervolu.loredesigner.editor.dock

import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

/** Generated ids live in the high-word-0 range; [node] ids in the high-word-1 range, so they never collide. */
class SequentialIds : DockIdGenerator {
    private var counter = 0L

    override fun next(): DockNodeId = DockNodeId(Uuid.fromLongs(0, ++counter))
}

fun node(n: Long) = DockNodeId(Uuid.fromLongs(1, n))

fun panel(name: String) = PanelId(name)

fun group(n: Long, kind: PanelKind, vararg panels: String, active: String? = panels.firstOrNull()) =
    PanelGroup(node(n), kind, panels.map(::PanelId), active?.let(::PanelId))

fun split(n: Long, orientation: DockOrientation, vararg children: Pair<DockNode, Float>) =
    SplitNode(node(n), orientation, children.map { it.first }, children.map { it.second })

fun layoutOf(root: DockNode): DockLayout =
    DockLayout(root, root.groups().flatMap { g -> g.panels.map { it to g.kind } }.toMap())

/** explorer(0.2) | doc1, doc2 (0.6) | props(0.2), as groups 1, 2, 3 under split 10. */
fun baseLayout() = layoutOf(
    split(
        10,
        DockOrientation.Horizontal,
        group(1, PanelKind.Tool, "explorer") to 0.2f,
        group(2, PanelKind.View, "doc1", "doc2") to 0.6f,
        group(3, PanelKind.Tool, "props") to 0.2f,
    ),
)

fun DockLayout.applied(operation: DockOperation, ids: DockIdGenerator = SequentialIds()): DockLayout {
    val result = apply(operation, ids)
    assertIs<DockResult.Applied>(result, "expected $operation to apply but got $result")
    assertEquals(emptyList(), result.layout.validate())
    return result.layout
}

fun DockLayout.rejected(operation: DockOperation, ids: DockIdGenerator = SequentialIds()): DockRejection {
    val result = apply(operation, ids)
    assertIs<DockResult.Rejected>(result, "expected $operation to be rejected but got $result")
    return result.reason
}

fun assertWeights(expected: List<Float>, actual: List<Float>) {
    assertEquals(expected.size, actual.size, "weights $actual")
    expected.zip(actual).forEach { (e, a) ->
        assertTrue(kotlin.math.abs(e - a) < 1e-4f, "expected $expected but was $actual")
    }
}

fun DockLayout.group(id: Long): PanelGroup = checkNotNull(findGroup(node(id))) { "no group $id in $root" }
