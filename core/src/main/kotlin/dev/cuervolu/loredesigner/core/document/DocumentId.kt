package dev.cuervolu.loredesigner.core.document

import kotlin.uuid.Uuid

@JvmInline
value class DocumentId(val value: Uuid) {

    override fun toString(): String = value.toString()

    companion object {
        fun parse(value: String): DocumentId = DocumentId(Uuid.parse(value))
    }
}
