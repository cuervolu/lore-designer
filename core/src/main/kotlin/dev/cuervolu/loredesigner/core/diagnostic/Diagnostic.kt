package dev.cuervolu.loredesigner.core.diagnostic

/**
 * Describes what happened, not how it is shown: the UI resolves [code] and [arguments]
 * into a localized message.
 */
data class Diagnostic(
    val severity: DiagnosticSeverity,
    val code: DiagnosticCode,
    val arguments: List<DiagnosticArgument> = emptyList(),
    // TODO: Find a way to represent location information
    //    val location: DiagnosticLocation? = null,
)
