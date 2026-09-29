package dev.cuervolu.loredesigner.core.document

import dev.cuervolu.loredesigner.core.type.SelectOptionId
import kotlinx.datetime.LocalDate

sealed interface PropertyValue {

    data class Text(val value: String) : PropertyValue

    data class Number(val value: Double) : PropertyValue

    data class Boolean(val value: kotlin.Boolean) : PropertyValue

    data class Select(val optionIds: List<SelectOptionId>) : PropertyValue

    data class Date(val value: LocalDate) : PropertyValue

    data class Relation(val documentIds: List<DocumentId>) : PropertyValue
}
