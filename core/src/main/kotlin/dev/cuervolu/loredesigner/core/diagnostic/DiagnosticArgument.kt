package dev.cuervolu.loredesigner.core.diagnostic

sealed interface DiagnosticArgument {
    data class Text(val value: String) : DiagnosticArgument

    data class Decimal(val value: Double) : DiagnosticArgument
}
