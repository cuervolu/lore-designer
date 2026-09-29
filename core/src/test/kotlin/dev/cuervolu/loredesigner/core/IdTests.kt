package dev.cuervolu.loredesigner.core

import dev.cuervolu.loredesigner.core.document.DocumentId
import dev.cuervolu.loredesigner.core.type.PropertyId
import dev.cuervolu.loredesigner.core.type.SelectOptionId
import dev.cuervolu.loredesigner.core.type.TypeId
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import kotlin.test.Test
import kotlin.test.assertEquals

class IdTests {

    private val raw = "01995f81-765c-74d9-a36e-18d3f434ef22"

    private fun assertRoundTrip(parse: (String) -> Any) {
        assertEquals(raw, parse(raw).toString())
    }

    @Test
    fun `all ids round trip`() {
        assertRoundTrip(WorkspaceId::parse)
        assertRoundTrip(DocumentId::parse)
        assertRoundTrip(TypeId::parse)
        assertRoundTrip(PropertyId::parse)
        assertRoundTrip(SelectOptionId::parse)
    }
}