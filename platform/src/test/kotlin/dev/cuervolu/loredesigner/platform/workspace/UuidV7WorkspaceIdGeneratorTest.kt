package dev.cuervolu.loredesigner.platform.workspace

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

class UuidV7WorkspaceIdGeneratorTest {
    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `generates RFC version seven identifiers`() {
        val uuid = UuidV7WorkspaceIdGenerator().generate().value.toJavaUuid()

        assertEquals(7, uuid.version())
        assertEquals(2, uuid.variant())
    }
}
