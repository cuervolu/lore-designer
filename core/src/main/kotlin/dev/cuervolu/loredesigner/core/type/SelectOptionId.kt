package dev.cuervolu.loredesigner.core.type

import kotlin.uuid.Uuid

@JvmInline
value class SelectOptionId(val value: Uuid) {
    override fun toString(): String = value.toString()

    companion object {
        fun parse(value: String): SelectOptionId = SelectOptionId(Uuid.parse(value))
    }
}
