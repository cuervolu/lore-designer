package dev.cuervolu.loredesigner.editor.dock

import kotlin.uuid.Uuid

@JvmInline
value class PanelId(val value: String) {
    init {
        require(value.isNotBlank()) { "PanelId must not be blank" }
    }

    override fun toString(): String = value
}

@JvmInline
value class DockNodeId(val value: Uuid) {

    override fun toString(): String = value.toString()

    companion object {
        fun parse(value: String): DockNodeId = DockNodeId(Uuid.parse(value))
    }
}

fun interface DockIdGenerator {
    fun next(): DockNodeId

    companion object {
        val Random: DockIdGenerator = DockIdGenerator { DockNodeId(Uuid.random()) }
    }
}
