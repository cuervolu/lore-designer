package dev.cuervolu.loredesigner.ui.launcher

import dev.cuervolu.loredesigner.workspace.FileSystemOperation
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import java.io.IOException
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class LauncherErrorMessagesTest {
    private val folder = Path.of("/worlds/Embercourt")

    private val allErrors: List<LauncherError> =
        listOf(
            WorkspaceError.NotAWorkspace(folder),
            WorkspaceError.InvalidWorkspaceLocation(folder),
            WorkspaceError.DestinationNotEmpty(folder),
            WorkspaceError.InvalidWorkspaceName("a/b"),
            WorkspaceError.InvalidProjectFile(folder, "Unexpected JSON token at offset 3"),
            WorkspaceError.UnsupportedProjectVersion(folder, 9),
            WorkspaceError.InvalidWorkspaceId(folder, "nope"),
            WorkspaceError.UnknownProjectColor(folder, "MAGENTA"),
        ).map(LauncherError::Workspace) +
            FileSystemOperation.entries.map {
                LauncherError.Workspace(WorkspaceError.FileSystemFailure(folder, it, IOException("EACCES: denied")))
            } +
            listOf(LauncherError.MissingLocation, LauncherError.InvalidLocation("\u0000"))

    @Test
    fun `technical details never reach message arguments`() {
        for (error in allErrors) {
            val args = error.toMessage().args.joinToString()
            assertFalse("Unexpected JSON" in args, "Parser detail leaked for $error")
            assertFalse("EACCES" in args, "Exception message leaked for $error")
            assertFalse("MAGENTA" in args || "nope" in args, "File content leaked for $error")
        }
    }

    @Test
    fun `folder-level errors name the folder the user chose`() {
        assertEquals(
            listOf("Embercourt"),
            LauncherError.Workspace(WorkspaceError.NotAWorkspace(folder)).toMessage().args,
        )
        assertEquals(
            listOf("Embercourt"),
            LauncherError.Workspace(WorkspaceError.DestinationNotEmpty(folder)).toMessage().args,
        )
    }

    @Test
    fun `read and write failures get different messages`() {
        fun messageFor(operation: FileSystemOperation) = LauncherError.Workspace(
            WorkspaceError.FileSystemFailure(folder, operation, IOException()),
        ).toMessage().resource

        assertEquals(messageFor(FileSystemOperation.READ), messageFor(FileSystemOperation.INSPECT))
        assertEquals(messageFor(FileSystemOperation.WRITE), messageFor(FileSystemOperation.CREATE_DIRECTORY))
        assertNotEquals(messageFor(FileSystemOperation.READ), messageFor(FileSystemOperation.WRITE))
    }
}
