package dev.cuervolu.loredesigner.platform.workspace

import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.workspace.WorkspaceIdGenerator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UuidV7WorkspaceIdGenerator : WorkspaceIdGenerator {
    @OptIn(ExperimentalUuidApi::class)
    override fun generate(): WorkspaceId = WorkspaceId(Uuid.generateV7())
}
