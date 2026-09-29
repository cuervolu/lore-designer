package dev.cuervolu.loredesigner.core.diagnostic

@JvmInline
value class DiagnosticCode(val value: String) {
    override fun toString(): String = value
}
