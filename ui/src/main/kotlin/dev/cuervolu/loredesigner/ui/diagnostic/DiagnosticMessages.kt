package dev.cuervolu.loredesigner.ui.diagnostic

import androidx.compose.runtime.Composable
import dev.cuervolu.loredesigner.core.diagnostic.Diagnostic
import dev.cuervolu.loredesigner.core.diagnostic.DiagnosticArgument
import dev.cuervolu.loredesigner.core.diagnostic.DiagnosticCode
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.allStringResources
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import java.text.NumberFormat
import java.util.*

/**
 * Resolves a diagnostic into a message for the current locale.
 *
 * Falls back to the raw code when no translation exists, so a missing string never breaks a screen.
 */
@Composable
fun Diagnostic.localizedMessage(): String {
    val resource = code.stringResource() ?: return code.value
    val args = arguments.map { it.localizedValue() }.toTypedArray()
    return stringResource(resource, *args)
}

// Resources are looked up by naming convention (`types.invalid-key` -> `diagnostic_types_invalid_key`)
// so new codes only need entries in strings.xml. Keep all uses of the generated map in this file.
internal fun DiagnosticCode.resourceKey(): String = "diagnostic_" + value.replace('.', '_').replace('-', '_')

internal fun DiagnosticCode.stringResource(): StringResource? = Res.allStringResources[resourceKey()]

private fun DiagnosticArgument.localizedValue(): String = when (this) {
    is DiagnosticArgument.Text -> value
    is DiagnosticArgument.Decimal -> NumberFormat.getNumberInstance(Locale.getDefault()).format(value)
}
