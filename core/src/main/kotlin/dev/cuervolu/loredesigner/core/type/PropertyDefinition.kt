package dev.cuervolu.loredesigner.core.type

sealed interface PropertyDefinition {
    val id: PropertyId
    val key: String
    val name: String
    val required: kotlin.Boolean

    data class Text(
        override val id: PropertyId,
        override val key: String,
        override val name: String,
        override val required: kotlin.Boolean,
    ) : PropertyDefinition

    data class Number(
        override val id: PropertyId,
        override val key: String,
        override val name: String,
        override val required: kotlin.Boolean,
        val min: Double? = null,
        val max: Double? = null,
    ) : PropertyDefinition

    data class Boolean(
        override val id: PropertyId,
        override val key: String,
        override val name: String,
        override val required: kotlin.Boolean,
    ) : PropertyDefinition

    data class Date(
        override val id: PropertyId,
        override val key: String,
        override val name: String,
        override val required: kotlin.Boolean,
    ) : PropertyDefinition

    data class Select(
        override val id: PropertyId,
        override val key: String,
        override val name: String,
        override val required: kotlin.Boolean,
        val multiple: kotlin.Boolean,
        val options: List<SelectOption>,
    ) : PropertyDefinition

    data class Relation(
        override val id: PropertyId,
        override val key: String,
        override val name: String,
        override val required: kotlin.Boolean,
        val multiple: kotlin.Boolean,
        val targetTypes: List<TypeId>,
    ) : PropertyDefinition
}
