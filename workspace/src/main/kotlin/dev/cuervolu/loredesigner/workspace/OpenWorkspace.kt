package dev.cuervolu.loredesigner.workspace

import java.nio.file.Path

class OpenWorkspace(private val workspaceStore: WorkspaceStore) {
    suspend operator fun invoke(location: Path): WorkspaceResult<Workspace> =
        workspaceStore.open(location.toAbsolutePath().normalize())
}
