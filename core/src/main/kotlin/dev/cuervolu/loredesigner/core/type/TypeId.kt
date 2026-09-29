package dev.cuervolu.loredesigner.core.type

import kotlin.uuid.Uuid

@JvmInline
value class TypeId(val value: Uuid) {

    override fun toString(): String = value.toString()

    companion object {
        fun parse(value: String): TypeId = TypeId(Uuid.parse(value))
    }
}
