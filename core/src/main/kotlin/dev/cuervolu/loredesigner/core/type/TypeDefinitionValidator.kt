package dev.cuervolu.loredesigner.core.type

import dev.cuervolu.loredesigner.core.diagnostic.Diagnostic
import dev.cuervolu.loredesigner.core.diagnostic.DiagnosticArgument
import dev.cuervolu.loredesigner.core.diagnostic.DiagnosticSeverity

class TypeDefinitionValidator {
    private val keyPattern = Regex("[a-z][a-z0-9_]*")

    fun validate(type: TypeDefinition): List<Diagnostic> {
        val diagnostics = mutableListOf<Diagnostic>()

        validateTypeKey(type, diagnostics)
        validateTypeName(type, diagnostics)
        validateProperties(type, diagnostics)

        return diagnostics
    }

    private fun validateTypeKey(
        type: TypeDefinition,
        diagnostics: MutableList<Diagnostic>,
    ) {
        if (!keyPattern.matches(type.key)) {
            diagnostics += Diagnostic(
                severity = DiagnosticSeverity.ERROR,
                code = TypeDiagnosticCodes.InvalidKey,
                arguments = listOf(DiagnosticArgument.Text(type.key)),
            )
        }
    }

    private fun validateTypeName(
        type: TypeDefinition,
        diagnostics: MutableList<Diagnostic>,
    ) {
        if (type.name.isBlank()) {
            diagnostics += Diagnostic(
                severity = DiagnosticSeverity.ERROR,
                code = TypeDiagnosticCodes.BlankName,
            )
        }
    }

    private fun validateProperties(
        type: TypeDefinition,
        diagnostics: MutableList<Diagnostic>,
    ) {
        validateDuplicatePropertyKeys(type, diagnostics)
        validateDuplicatePropertyIds(type, diagnostics)

        for (property in type.properties) {
            validatePropertyKey(property, diagnostics)
            validatePropertyName(property, diagnostics)

            when (property) {
                is PropertyDefinition.Number ->
                    validateNumberProperty(property, diagnostics)

                is PropertyDefinition.Select ->
                    validateSelectProperty(property, diagnostics)

                else -> Unit
            }
        }
    }

    private fun validateDuplicatePropertyKeys(
        type: TypeDefinition,
        diagnostics: MutableList<Diagnostic>,
    ) {
        val duplicateKeys = type.properties
            .groupBy { it.key }
            .filterValues { it.size > 1 }
            .keys

        for (key in duplicateKeys) {
            diagnostics += Diagnostic(
                severity = DiagnosticSeverity.ERROR,
                code = TypeDiagnosticCodes.DuplicatePropertyKey,
                arguments = listOf(DiagnosticArgument.Text(key)),
            )
        }
    }

    private fun validateDuplicatePropertyIds(
        type: TypeDefinition,
        diagnostics: MutableList<Diagnostic>,
    ) {
        val duplicateIds = type.properties
            .groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys

        for (id in duplicateIds) {
            diagnostics += Diagnostic(
                severity = DiagnosticSeverity.ERROR,
                code = TypeDiagnosticCodes.DuplicatePropertyId,
                arguments = listOf(DiagnosticArgument.Text(id.toString())),
            )
        }
    }

    private fun validatePropertyKey(
        property: PropertyDefinition,
        diagnostics: MutableList<Diagnostic>,
    ) {
        if (!keyPattern.matches(property.key)) {
            diagnostics += Diagnostic(
                severity = DiagnosticSeverity.ERROR,
                code = TypeDiagnosticCodes.InvalidPropertyKey,
                arguments = listOf(DiagnosticArgument.Text(property.key)),
            )
        }
    }

    private fun validatePropertyName(
        property: PropertyDefinition,
        diagnostics: MutableList<Diagnostic>,
    ) {
        if (property.name.isBlank()) {
            diagnostics += Diagnostic(
                severity = DiagnosticSeverity.ERROR,
                code = TypeDiagnosticCodes.BlankPropertyName,
                arguments = listOf(DiagnosticArgument.Text(property.key)),
            )
        }
    }

    private fun validateNumberProperty(
        property: PropertyDefinition.Number,
        diagnostics: MutableList<Diagnostic>,
    ) {
        val min = property.min
        val max = property.max

        if (min != null && max != null && min > max) {
            diagnostics += Diagnostic(
                severity = DiagnosticSeverity.ERROR,
                code = TypeDiagnosticCodes.InvalidNumberRange,
                arguments = listOf(
                    DiagnosticArgument.Text(property.key),
                    DiagnosticArgument.Decimal(min),
                    DiagnosticArgument.Decimal(max),
                ),
            )
        }
    }

    private fun validateSelectProperty(
        property: PropertyDefinition.Select,
        diagnostics: MutableList<Diagnostic>,
    ) {
        val duplicateKeys = property.options
            .groupBy { it.key }
            .filterValues { it.size > 1 }
            .keys

        for (key in duplicateKeys) {
            diagnostics += Diagnostic(
                severity = DiagnosticSeverity.ERROR,
                code = TypeDiagnosticCodes.DuplicateSelectOptionKey,
                arguments = listOf(
                    DiagnosticArgument.Text(property.key),
                    DiagnosticArgument.Text(key),
                ),
            )
        }

        val duplicateIds = property.options
            .groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys

        for (id in duplicateIds) {
            diagnostics += Diagnostic(
                severity = DiagnosticSeverity.ERROR,
                code = TypeDiagnosticCodes.DuplicateSelectOptionId,
                arguments = listOf(
                    DiagnosticArgument.Text(property.key),
                    DiagnosticArgument.Text(id.toString()),
                ),
            )
        }
    }
}
