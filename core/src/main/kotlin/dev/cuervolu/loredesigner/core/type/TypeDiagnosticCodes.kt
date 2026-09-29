package dev.cuervolu.loredesigner.core.type

import dev.cuervolu.loredesigner.core.diagnostic.DiagnosticCode

object TypeDiagnosticCodes {
    val InvalidKey = DiagnosticCode("types.invalid-key")
    val BlankName = DiagnosticCode("types.blank-name")
    val InvalidPropertyKey = DiagnosticCode("types.invalid-property-key")
    val BlankPropertyName = DiagnosticCode("types.blank-property-name")
    val DuplicatePropertyKey = DiagnosticCode("types.duplicate-property-key")
    val DuplicatePropertyId = DiagnosticCode("types.duplicate-property-id")
    val InvalidNumberRange = DiagnosticCode("types.invalid-number-range")
    val DuplicateSelectOptionKey = DiagnosticCode("types.duplicate-select-option-key")
    val DuplicateSelectOptionId = DiagnosticCode("types.duplicate-select-option-id")

    // Lets the UI verify every code has a translation.
    val all: List<DiagnosticCode> = listOf(
        InvalidKey,
        BlankName,
        InvalidPropertyKey,
        BlankPropertyName,
        DuplicatePropertyKey,
        DuplicatePropertyId,
        InvalidNumberRange,
        DuplicateSelectOptionKey,
        DuplicateSelectOptionId,
    )
}
