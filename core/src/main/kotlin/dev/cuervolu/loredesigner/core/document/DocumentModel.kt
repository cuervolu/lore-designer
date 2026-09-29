package dev.cuervolu.loredesigner.core.document

import dev.cuervolu.loredesigner.core.type.TypeId

data class DocumentModel(
    val id: DocumentId,
    val title: String,
    val typeId: TypeId?,
    val icon: String?,
    val properties: List<DocumentProperty>,
    val body: String,
)
