package dev.cuervolu.loredesigner.core.document

import dev.cuervolu.loredesigner.core.type.PropertyId

data class DocumentProperty(
    val definitionId: PropertyId,
    val value: PropertyValue,
)
