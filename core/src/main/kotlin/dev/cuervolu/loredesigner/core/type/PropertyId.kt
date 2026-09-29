package dev.cuervolu.loredesigner.core.type

import kotlin.uuid.Uuid

@JvmInline
value class PropertyId(val value: Uuid) {
    override fun toString(): String = value.toString()

    companion object {
        fun parse(value: String): PropertyId = PropertyId(Uuid.parse(value))
    }
}
