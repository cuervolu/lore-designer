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
import kotlinx.serialization.json.Json
import okio.FileMetadata
import okio.FileNotFoundException
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

class FileSystemWorkspaceStore(private val fileSystem: FileSystem) : WorkspaceStore {
    private val fileWriter = AtomicFileWriter(fileSystem)
    private val projectFileCodec = ProjectFileCodec()

    override suspend fun create(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> =
        withContext(Dispatchers.IO) {
            val absoluteLocation =
                try {
                    absolute(location)
                } catch (exception: IOException) {
                    return@withContext WorkspaceResult.Failure(
                        WorkspaceError.FileSystemFailure(location, FileSystemOperation.INSPECT, exception),
                    )
                }
            createWorkspace(absoluteLocation, config)
        }

    override suspend fun open(location: Path): WorkspaceResult<Workspace> = withContext(Dispatchers.IO) {
        val absoluteLocation =
            try {
                absolute(location)
            } catch (exception: IOException) {
                return@withContext WorkspaceResult.Failure(
                    WorkspaceError.FileSystemFailure(location, FileSystemOperation.READ, exception),
                )
            }
        openWorkspace(absoluteLocation)
    }

    override suspend fun updateConfig(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> =
        withContext(Dispatchers.IO) {
            val projectFile = location.resolve(PROJECT_FILE_NAME)
            try {
                if (fileSystem.metadataFollowingLinksOrNull(projectFile)?.isRegularFile != true) {
                    return@withContext WorkspaceResult.Failure(WorkspaceError.NotAWorkspace(location))
                }
                fileWriter.replace(projectFile, projectFileCodec.encode(config))
                WorkspaceResult.Success(Workspace(location, config))
            } catch (exception: IOException) {
                WorkspaceResult.Failure(
                    WorkspaceError.FileSystemFailure(projectFile, FileSystemOperation.WRITE, exception),
                )
            }
        }

    private fun absolute(location: Path): Path = if (location.isAbsolute) {
        location.normalized()
    } else {
        fileSystem.canonicalize(
            ".".toPath(),
        ).resolve(location, normalize = true)
    }

    private fun createWorkspace(location: Path, config: ProjectConfig): WorkspaceResult<Workspace> {
        val typesContent = initialTypesFile()
        val projectContent = projectFileCodec.encode(config)
        val createdResources = mutableListOf<Path>()
        val existedBefore: Boolean
        try {
            existedBefore = fileSystem.metadataOrNull(location) != null
            if (existedBefore) {
                if (fileSystem.metadataFollowingLinksOrNull(location)?.isDirectory != true) {
                    return WorkspaceResult.Failure(WorkspaceError.InvalidWorkspaceLocation(location))
                }
                if (fileSystem.list(location).isNotEmpty()) {
                    return WorkspaceResult.Failure(WorkspaceError.DestinationNotEmpty(location))
                }
            }
        } catch (exception: IOException) {
            return WorkspaceResult.Failure(
                WorkspaceError.FileSystemFailure(location, FileSystemOperation.INSPECT, exception),
            )
        }
        if (!existedBefore) {
            try {
                fileSystem.createDirectories(location)
                createdResources.add(location)
            } catch (exception: IOException) {
                rollbackCreation(createdResources, exception)
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
            fileSystem.createDirectory(metadataDirectory, mustCreate = true)
            createdResources.add(metadataDirectory)
            activePath = typesFile
            operation = FileSystemOperation.WRITE
            fileWriter.create(typesFile, typesContent)
            createdResources.add(typesFile)
            activePath = projectFile
            fileWriter.create(projectFile, projectContent)
            createdResources.add(projectFile)
        } catch (exception: IOException) {
            rollbackCreation(createdResources, exception)
            return WorkspaceResult.Failure(
                WorkspaceError.FileSystemFailure(activePath, operation, exception),
            )
        } catch (exception: RuntimeException) {
            rollbackCreation(createdResources, exception)
            throw exception
        }

        return WorkspaceResult.Success(Workspace(location, config))
    }

    private fun openWorkspace(location: Path): WorkspaceResult<Workspace> {
        val projectFile = location.resolve(PROJECT_FILE_NAME)
        try {
            if (fileSystem.metadataFollowingLinksOrNull(location)?.isDirectory != true ||
                fileSystem.metadataFollowingLinksOrNull(projectFile)?.isRegularFile != true
            ) {
                return WorkspaceResult.Failure(WorkspaceError.NotAWorkspace(location))
            }
            val content = fileSystem.read(projectFile) { readUtf8() }
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

    private fun rollbackCreation(createdResources: List<Path>, originalFailure: Exception) {
        createdResources
            .asReversed()
            .forEach { path ->
                try {
                    fileSystem.delete(path, mustExist = false)
                } catch (cleanupFailure: IOException) {
                    originalFailure.addSuppressed(cleanupFailure)
                }
            }
    }

    // Okio reports symlinks without following them; the workspace checks must see their targets.
    private fun FileSystem.metadataFollowingLinksOrNull(path: Path): FileMetadata? {
        val metadata = metadataOrNull(path) ?: return null
        if (metadata.symlinkTarget == null) return metadata
        return try {
            metadataOrNull(canonicalize(path))
        } catch (_: FileNotFoundException) {
            null
        }
    }

    private fun initialTypesFile(): String = TYPES_JSON.encodeToString(InitialTypesFile()) + System.lineSeparator()

    @Serializable
    private data class InitialTypesFile(val version: Int = 1, val types: List<String> = emptyList())

    private companion object {
        private const val PROJECT_FILE_NAME = "project.lore"
        private const val METADATA_DIRECTORY_NAME = ".lore"
        private const val TYPES_FILE_NAME = "types.json"

        private val TYPES_JSON =
            Json {
                prettyPrint = true
                encodeDefaults = true
            }
    }
}
