package dev.cuervolu.loredesigner.core.diagnostic

data class Diagnostic(
    val severity: DiagnosticSeverity,
    val code: DiagnosticCode,
    val message: String
    // TODO: Find a way to represent location information
    //    val location: DiagnosticLocation? = null,
)
