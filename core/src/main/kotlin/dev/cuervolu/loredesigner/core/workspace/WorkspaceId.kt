package dev.cuervolu.loredesigner.core.workspace

import kotlin.uuid.Uuid

@JvmInline
value class WorkspaceId(val value: Uuid) {

    override fun toString(): String = value.toString()

    companion object {
        fun parse(value: String): WorkspaceId = WorkspaceId(Uuid.parse(value))
    }
}
