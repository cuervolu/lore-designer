package dev.cuervolu.loredesigner.core.types

import dev.cuervolu.loredesigner.core.diagnostic.Diagnostic
import dev.cuervolu.loredesigner.core.diagnostic.DiagnosticArgument
import dev.cuervolu.loredesigner.core.diagnostic.DiagnosticSeverity
import dev.cuervolu.loredesigner.core.type.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypeDefinitionValidatorTest {

    private val validator = TypeDefinitionValidator()

    @Test
    fun `reports invalid type key with the key as argument`() {
        val diagnostics = validator.validate(type(key = "Character Name"))

        assertEquals(
            listOf(
                Diagnostic(
                    severity = DiagnosticSeverity.ERROR,
                    code = TypeDiagnosticCodes.InvalidKey,
                    arguments = listOf(DiagnosticArgument.Text("Character Name")),
                ),
            ),
            diagnostics,
        )
    }

    @Test
    fun `accepts valid type key`() {
        val diagnostics = validator.validate(type(key = "main_character"))

        assertTrue(diagnostics.isEmpty())
    }

    @Test
    fun `reports blank type name without arguments`() {
        val diagnostics = validator.validate(type(name = "  "))

        assertEquals(TypeDiagnosticCodes.BlankName, diagnostics.single().code)
        assertTrue(diagnostics.single().arguments.isEmpty())
    }

    @Test
    fun `uses property specific codes for invalid property key and blank property name`() {
        val diagnostics = validator.validate(
            type(properties = listOf(textProperty(PROPERTY_ID_1, key = "Bad Key", name = ""))),
        )

        assertEquals(
            listOf(
                TypeDiagnosticCodes.InvalidPropertyKey to listOf(DiagnosticArgument.Text("Bad Key")),
                TypeDiagnosticCodes.BlankPropertyName to listOf(DiagnosticArgument.Text("Bad Key")),
            ),
            diagnostics.map { it.code to it.arguments },
        )
    }

    @Test
    fun `reports duplicated property keys and ids`() {
        val diagnostics = validator.validate(
            type(
                properties = listOf(
                    textProperty(PROPERTY_ID_1, key = "age"),
                    textProperty(PROPERTY_ID_1, key = "age"),
                ),
            ),
        )

        assertEquals(
            listOf(
                TypeDiagnosticCodes.DuplicatePropertyKey to listOf(DiagnosticArgument.Text("age")),
                TypeDiagnosticCodes.DuplicatePropertyId to listOf(DiagnosticArgument.Text(PROPERTY_ID_1)),
            ),
            diagnostics.map { it.code to it.arguments },
        )
    }

    @Test
    fun `reports number range with bounds as decimal arguments`() {
        val property = PropertyDefinition.Number(
            id = PropertyId.parse(PROPERTY_ID_1),
            key = "age",
            name = "Age",
            required = false,
            min = 10.0,
            max = 2.5,
        )

        val diagnostics = validator.validate(type(properties = listOf(property)))

        assertEquals(TypeDiagnosticCodes.InvalidNumberRange, diagnostics.single().code)
        assertEquals(
            listOf(
                DiagnosticArgument.Text("age"),
                DiagnosticArgument.Decimal(10.0),
                DiagnosticArgument.Decimal(2.5),
            ),
            diagnostics.single().arguments,
        )
    }

    @Test
    fun `accepts number range with equal bounds`() {
        val property = PropertyDefinition.Number(
            id = PropertyId.parse(PROPERTY_ID_1),
            key = "age",
            name = "Age",
            required = false,
            min = 3.0,
            max = 3.0,
        )

        assertTrue(validator.validate(type(properties = listOf(property))).isEmpty())
    }

    @Test
    fun `reports duplicated select option keys and ids with the owning property`() {
        val option = SelectOption(SelectOptionId.parse(OPTION_ID), key = "red", name = "Red")
        val property = PropertyDefinition.Select(
            id = PropertyId.parse(PROPERTY_ID_1),
            key = "color",
            name = "Color",
            required = false,
            multiple = false,
            options = listOf(option, option),
        )

        val diagnostics = validator.validate(type(properties = listOf(property)))

        assertEquals(
            listOf(
                TypeDiagnosticCodes.DuplicateSelectOptionKey to
                    listOf(DiagnosticArgument.Text("color"), DiagnosticArgument.Text("red")),
                TypeDiagnosticCodes.DuplicateSelectOptionId to
                    listOf(DiagnosticArgument.Text("color"), DiagnosticArgument.Text(OPTION_ID)),
            ),
            diagnostics.map { it.code to it.arguments },
        )
    }

    private fun type(
        key: String = "character",
        name: String = "Character",
        properties: List<PropertyDefinition> = emptyList(),
    ) = TypeDefinition(
        id = TypeId.parse("01995f81-765c-74d9-a36e-18d3f434ef22"),
        key = key,
        name = name,
        properties = properties,
    )

    private fun textProperty(id: String, key: String, name: String = "Name") = PropertyDefinition.Text(
        id = PropertyId.parse(id),
        key = key,
        name = name,
        required = false,
    )

    private companion object {
        const val PROPERTY_ID_1 = "01995f81-765c-74d9-a36e-18d3f434ef23"
        const val OPTION_ID = "01995f81-765c-74d9-a36e-18d3f434ef24"
    }
}
