package dev.cuervolu.loredesigner.platform.workspace

import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.workspace.FileSystemOperation
import dev.cuervolu.loredesigner.workspace.Workspace
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import dev.cuervolu.loredesigner.workspace.WorkspaceResult
import dev.cuervolu.loredesigner.workspace.WorkspaceStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.io.path.isDirectory

class FileSystemWorkspaceStore private constructor(
    private val fileWriter: WorkspaceFileWriter,
    private val projectFileCodec: ProjectFileCodec,
) : WorkspaceStore {
    constructor() : this(AtomicFileWriter(), ProjectFileCodec())

    override suspend fun create(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> =
        withContext(Dispatchers.IO) {
            createWorkspace(location.toAbsolutePath().normalize(), config)
        }

    override suspend fun open(location: Path): WorkspaceResult<Workspace> = withContext(Dispatchers.IO) {
        openWorkspace(location.toAbsolutePath().normalize())
    }

    private fun createWorkspace(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> {
        val existedBefore = Files.exists(location, LinkOption.NOFOLLOW_LINKS)
        try {
            if (existedBefore) {
                if (!location.isDirectory()) {
                    return WorkspaceResult.Failure(WorkspaceError.InvalidWorkspaceLocation(location))
                }
                Files.list(location).use { entries ->
                    if (entries.findAny().isPresent) {
                        return WorkspaceResult.Failure(WorkspaceError.DestinationNotEmpty(location))
                    }
                }
            }
        } catch (exception: IOException) {
            return WorkspaceResult.Failure(
                WorkspaceError.FileSystemFailure(location, FileSystemOperation.INSPECT, exception),
            )
        }
        if (!existedBefore) {
            try {
                Files.createDirectories(location)
            } catch (exception: IOException) {
                try {
                    Files.deleteIfExists(location)
                } catch (cleanupFailure: IOException) {
                    exception.addSuppressed(cleanupFailure)
                }
                return WorkspaceResult.Failure(
                    WorkspaceError.FileSystemFailure(location, FileSystemOperation.CREATE_DIRECTORY, exception),
                )
            }
        }

        val metadataDirectory = location.resolve(METADATA_DIRECTORY_NAME)
        val typesFile = metadataDirectory.resolve(TYPES_FILE_NAME)
        val projectFile = location.resolve(PROJECT_FILE_NAME)
        var activePath = metadataDirectory
        var operation = FileSystemOperation.CREATE_DIRECTORY
        try {
            Files.createDirectory(metadataDirectory)
            activePath = typesFile
            operation = FileSystemOperation.WRITE
            fileWriter.write(typesFile, initialTypesFile())
            activePath = projectFile
            fileWriter.write(projectFile, projectFileCodec.encode(config))
        } catch (exception: Exception) {
            rollbackCreation(
                location = location,
                removeLocation = !existedBefore,
                projectFile = projectFile,
                typesFile = typesFile,
                metadataDirectory = metadataDirectory,
                originalFailure = exception,
            )
            return WorkspaceResult.Failure(
                WorkspaceError.FileSystemFailure(activePath, operation, exception),
            )
        }

        return WorkspaceResult.Success(Workspace(location, config))
    }

    private fun openWorkspace(location: Path): WorkspaceResult<Workspace> {
        val projectFile = location.resolve(PROJECT_FILE_NAME)
        try {
            if (!location.isDirectory() || !Files.isRegularFile(projectFile)) {
                return WorkspaceResult.Failure(WorkspaceError.NotAWorkspace(location))
            }
            val content = Files.readString(projectFile)
            return when (val decoded = projectFileCodec.decode(projectFile, content)) {
                is WorkspaceResult.Failure -> decoded
                is WorkspaceResult.Success -> WorkspaceResult.Success(Workspace(location, decoded.value))
            }
        } catch (exception: IOException) {
            return WorkspaceResult.Failure(
                WorkspaceError.FileSystemFailure(projectFile, FileSystemOperation.READ, exception),
            )
        }
    }

    private fun rollbackCreation(
        location: Path,
        removeLocation: Boolean,
        projectFile: Path,
        typesFile: Path,
        metadataDirectory: Path,
        originalFailure: Exception,
    ) {
        listOf(projectFile, typesFile, metadataDirectory)
            .plus(if (removeLocation) listOf(location) else emptyList())
            .forEach { path ->
                try {
                    Files.deleteIfExists(path)
                } catch (cleanupFailure: IOException) {
                    originalFailure.addSuppressed(cleanupFailure)
                }
            }
    }

    private fun initialTypesFile(): String = TYPES_JSON.encodeToString(InitialTypesFile()) + System.lineSeparator()

    @Serializable
    private data class InitialTypesFile(val version: Int = 1, val types: List<String> = emptyList())

    internal companion object {
        private const val PROJECT_FILE_NAME = "project.lore"
        private const val METADATA_DIRECTORY_NAME = ".lore"
        private const val TYPES_FILE_NAME = "types.json"

        private val TYPES_JSON =
            Json {
                prettyPrint = true
                encodeDefaults = true
            }

        fun withFileWriter(fileWriter: WorkspaceFileWriter): FileSystemWorkspaceStore =
            FileSystemWorkspaceStore(fileWriter, ProjectFileCodec())
    }
}
