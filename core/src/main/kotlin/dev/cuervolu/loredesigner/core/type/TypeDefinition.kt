package dev.cuervolu.loredesigner.core.type

data class TypeDefinition(val id: TypeId, val key: String, val name: String, val properties: List<PropertyDefinition>)
