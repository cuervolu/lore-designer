package dev.cuervolu.loredesigner.ui.diagnostic

import dev.cuervolu.loredesigner.core.diagnostic.DiagnosticCode
import dev.cuervolu.loredesigner.core.type.TypeDiagnosticCodes
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiagnosticMessagesTest {

    @Test
    fun `resource key follows naming convention`() {
        assertEquals(
            "diagnostic_types_duplicate_select_option_key",
            DiagnosticCode("types.duplicate-select-option-key").resourceKey(),
        )
    }

    @Test
    fun `every type diagnostic code has a string resource`() {
        for (code in TypeDiagnosticCodes.all) {
            assertNotNull(code.stringResource(), "Missing string resource for diagnostic code '$code'")
        }
    }

    @Test
    fun `unknown code has no string resource`() {
        assertNull(DiagnosticCode("types.does-not-exist").stringResource())
    }

    @Test
    fun `spanish strings define the same keys and placeholders as default strings`() {
        val default = readStrings("values")
        val spanish = readStrings("values-es")

        assertEquals(default.keys, spanish.keys)
        for ((key, text) in default) {
            assertEquals(placeholders(text), placeholders(spanish.getValue(key)), "Placeholder mismatch in '$key'")
        }
    }

    @Test
    fun `default strings are not empty`() {
        assertTrue(readStrings("values").isNotEmpty())
    }

    private fun readStrings(qualifier: String): Map<String, String> {
        val file = File("src/main/composeResources/$qualifier/strings.xml")
        val nodes = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(file)
            .getElementsByTagName("string")
        return (0 until nodes.length).associate { index ->
            val node = nodes.item(index)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
        }
    }

    private fun placeholders(text: String): Set<String> = Regex("""%\d+\$[sd]""").findAll(text).map { it.value }.toSet()
}
