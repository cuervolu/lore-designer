package dev.cuervolu.loredesigner.platform.state

import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.platform.workspace.AtomicFileWriter
import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.VersionFormatException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import okio.FileSystem
import okio.IOException
import okio.Path
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.time.Clock
import kotlin.time.toJavaInstant

/**
 * [StateStore] keeping each logical storage as one file in [directory].
 *
 * Saving re-reads the file and replaces only the saving component's entry, so components sharing a
 * storage (and entries written by other releases) survive each other's writes. Each entry carries its
 * own version. Files are replaced atomically. Before an entry that cannot be read is replaced (an
 * unparseable file, a malformed version or payload, an older major version nobody can migrate yet) the
 * whole file is copied to a uniquely named backup. An entry from a newer major version is never written.
 *
 * Logs name files and components but never include stored values. I/O failures carry their exception;
 * decoding failures report only the exception type, since their messages can quote the stored input.
 */
class FileStateStore(
    private val directory: Path,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val logger: Logger,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: Clock = Clock.System,
) : StateStore {
    private val fileWriter = AtomicFileWriter(fileSystem)
    private val jsonStorage = JsonStateStorage()
    private val propertiesStorage = PropertiesStateStorage()

    private val registrationLock = Any()
    private val componentsByClass = mutableMapOf<Class<*>, ComponentSpec>()
    private val storageLocks = mutableMapOf<String, Mutex>()

    override suspend fun <T : Any> load(component: PersistentStateComponent<T>): StateLoadResult {
        val spec = spec(component)
        return when (spec.storage.format) {
            StateFormat.JSON -> load(component, spec, jsonStorage)
            StateFormat.PROPERTIES -> load(component, spec, propertiesStorage)
        }
    }

    override suspend fun <T : Any> save(component: PersistentStateComponent<T>): StateSaveResult {
        val spec = spec(component)
        return when (spec.storage.format) {
            StateFormat.JSON -> save(component, spec, jsonStorage)
            StateFormat.PROPERTIES -> save(component, spec, propertiesStorage)
        }
    }

    private suspend fun <T : Any, P> load(
        component: PersistentStateComponent<T>,
        spec: ComponentSpec,
        backend: StateStorageBackend<P>,
    ): StateLoadResult = withContext(ioDispatcher) {
        val file = fileFor(spec.storage)
        lockFor(spec.storage).withLock {
            val content = try {
                if (!fileSystem.exists(file)) {
                    logger.d { "No state file $file yet; ${spec.name} starts from its defaults" }
                    return@withLock StateLoadResult.Missing
                }
                fileSystem.read(file) { readUtf8() }
            } catch (exception: IOException) {
                logger.w(exception) { "Could not read state file $file; ${spec.name} keeps its defaults" }
                return@withLock StateLoadResult.StorageUnreadable(exception.describe())
            }
            val document = try {
                backend.parse(content)
            } catch (exception: StateFormatException) {
                logger.w { "State file $file is unreadable (${exception.message}); ${spec.name} keeps its defaults" }
                return@withLock StateLoadResult.StorageUnreadable(exception.message ?: "Unreadable")
            }
            val stored = document.components[spec.name] ?: run {
                logger.d { "$file holds no state for ${spec.name}; it starts from its defaults" }
                return@withLock StateLoadResult.Missing
            }

            when (val entry = inspect(component, spec, backend, stored)) {
                is StoredEntry.Readable -> {
                    component.loadState(entry.value)
                    logger.d { "Loaded ${spec.name} version ${entry.persisted} from $file" }
                    StateLoadResult.Loaded
                }

                is StoredEntry.Invalid -> {
                    logger.w {
                        "Stored state of ${spec.name} in $file is invalid (${entry.reason}); it keeps its defaults"
                    }
                    StateLoadResult.ComponentInvalid(entry.reason)
                }

                is StoredEntry.NewerMajor -> {
                    logger.w {
                        "${spec.name} in $file was written by a newer release (${entry.persisted}, supported " +
                            "${spec.version}); it will not be loaded or overwritten"
                    }
                    StateLoadResult.NewerVersion(entry.persisted.toString(), spec.version.toString())
                }

                is StoredEntry.OlderMajor -> {
                    logger.w {
                        "${spec.name} in $file uses version ${entry.persisted}, which has no migration to " +
                            "${spec.version}; it keeps its defaults"
                    }
                    StateLoadResult.MigrationRequired(entry.persisted.toString(), spec.version.toString())
                }
            }
        }
    }

    // The component's state is captured under the storage lock so a save can never write a snapshot
    // older than one an overlapping save has already written.
    private suspend fun <T : Any, P> save(
        component: PersistentStateComponent<T>,
        spec: ComponentSpec,
        backend: StateStorageBackend<P>,
    ): StateSaveResult = withContext(ioDispatcher) {
        val file = fileFor(spec.storage)
        lockFor(spec.storage).withLock {
            try {
                val content = if (fileSystem.exists(file)) fileSystem.read(file) { readUtf8() } else null
                val document = content?.let { parseOrNull(backend, it, file) }
                var backupLabel = if (content != null && document == null) CORRUPT_LABEL else null
                var version = spec.version
                var newerState: P? = null

                document?.components?.get(spec.name)?.let { stored ->
                    when (val entry = inspect(component, spec, backend, stored)) {
                        is StoredEntry.Readable -> if (entry.persisted > spec.version) {
                            // A newer minor release wrote this; keep its version and the fields it added.
                            version = entry.persisted
                            newerState = entry.state
                        }

                        is StoredEntry.Invalid -> backupLabel = CORRUPT_LABEL

                        is StoredEntry.OlderMajor -> backupLabel = "v${entry.persisted.major}"

                        is StoredEntry.NewerMajor -> {
                            logger.w { "Not saving ${spec.name}: $file holds newer version ${entry.persisted}" }
                            return@withLock StateSaveResult.Blocked(entry.persisted.toString(), spec.version.toString())
                        }
                    }
                }

                val encoded = backend.encode(component.serializer, component.getState())
                val state = newerState?.let { existing ->
                    component.serializer.descriptor.fieldNames()
                        ?.let { backend.keepUnknownFields(existing, encoded, it) }
                        ?: run {
                            logger.w {
                                "Not saving ${spec.name}: its newer version $version in $file may hold fields " +
                                    "that cannot be preserved"
                            }
                            return@withLock StateSaveResult.Blocked(version.toString(), spec.version.toString())
                        }
                } ?: encoded

                val entry = backend.pack(VersionedState(version.toString(), state))
                val components = document?.components.orEmpty() + (spec.name to entry)
                val rendered = backend.render(StateDocument(components, document?.unrecognized))

                fileSystem.createDirectories(directory)
                backupLabel?.let { backUp(file, it) }
                fileWriter.replace(file, rendered)
                logger.v { "Saved ${spec.name} version $version to $file" }
                StateSaveResult.Saved
            } catch (exception: IOException) {
                logger.e(exception) { "Could not save ${spec.name} to $file; keeping it in memory" }
                StateSaveResult.Failed(exception)
            }
        }
    }

    private fun <P> parseOrNull(backend: StateStorageBackend<P>, content: String, file: Path): StateDocument<P>? = try {
        backend.parse(content)
    } catch (exception: StateFormatException) {
        logger.w { "Replacing unreadable state file $file (${exception.message})" }
        null
    }

    private fun <T : Any, P> inspect(
        component: PersistentStateComponent<T>,
        spec: ComponentSpec,
        backend: StateStorageBackend<P>,
        stored: P,
    ): StoredEntry<T, P> {
        val entry = backend.unpack(stored) ?: return StoredEntry.Invalid("Unrecognized entry")
        return when (val compatibility = stateCompatibility(spec.version, entry.version)) {
            is StateCompatibility.Malformed -> StoredEntry.Invalid("Malformed version")

            is StateCompatibility.NewerMajor -> StoredEntry.NewerMajor(compatibility.persisted)

            // An explicit migration from an older major would be applied here.
            is StateCompatibility.OlderMajor -> StoredEntry.OlderMajor(compatibility.persisted)

            is StateCompatibility.Compatible -> try {
                StoredEntry.Readable(
                    backend.decode(component.serializer, entry.state),
                    compatibility.persisted,
                    entry.state,
                )
            } catch (exception: SerializationException) {
                StoredEntry.Invalid(exception.describe())
            } catch (exception: IllegalArgumentException) {
                StoredEntry.Invalid(exception.describe())
            }
        }
    }

    private fun backUp(file: Path, label: String) {
        val backup = uniqueBackupFor(file, label)
        // Copied rather than moved so the original stays in place if writing its replacement fails.
        fileSystem.copy(file, backup)
        logger.w { "Kept a copy of $file as $backup before replacing it" }
    }

    private fun uniqueBackupFor(file: Path, label: String): Path {
        val timestamp = BACKUP_TIMESTAMP.format(clock.now().toJavaInstant())
        val base = "${file.name}.$label-$timestamp"
        return generateSequence(0) { it + 1 }
            .map { attempt -> directory / if (attempt == 0) base else "$base-$attempt" }
            .first { !fileSystem.exists(it) }
    }

    private fun fileFor(storage: StorageSpec): Path = directory / "${storage.name}.${storage.format.extension}"

    private fun lockFor(storage: StorageSpec): Mutex = synchronized(registrationLock) {
        storageLocks.getOrPut(storage.name) { Mutex() }
    }

    private fun spec(component: PersistentStateComponent<*>): ComponentSpec = synchronized(registrationLock) {
        componentsByClass.getOrPut(component.javaClass) {
            resolveStateMetadata(component.javaClass).also { checkStateDeclaration(it, componentsByClass.values) }
        }
    }

    private sealed interface StoredEntry<out T, out P> {
        data class Readable<T, P>(val value: T, val persisted: Version, val state: P) : StoredEntry<T, P>

        data class Invalid(val reason: String) : StoredEntry<Nothing, Nothing>

        data class NewerMajor(val persisted: Version) : StoredEntry<Nothing, Nothing>

        data class OlderMajor(val persisted: Version) : StoredEntry<Nothing, Nothing>
    }

    private companion object {
        const val CORRUPT_LABEL = "corrupt"

        val BACKUP_TIMESTAMP: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "yyyyMMdd-HHmmss",
        ).withZone(ZoneOffset.UTC)
    }
}

internal data class StorageSpec(val name: String, val format: StateFormat)

internal data class ComponentSpec(
    val storage: StorageSpec,
    val name: String,
    val version: Version,
    val declaredBy: Class<*>,
)

// Exception messages from decoding can quote the stored input, so only the type is reported.
private fun Exception.describe(): String = this::class.simpleName ?: "Exception"

/** Top-level field names of a class-like state, or `null` when the state is not an object. */
private fun SerialDescriptor.fieldNames(): Set<String>? =
    if (kind == StructureKind.CLASS || kind == StructureKind.OBJECT) {
        (0 until elementsCount).mapTo(mutableSetOf(), ::getElementName)
    } else {
        null
    }

private val STORAGE_NAME = Regex("[a-z0-9][a-z0-9_-]*")

/** Reads and validates [State] on [componentClass]; failures are programmer errors and throw. */
internal fun resolveStateMetadata(componentClass: Class<*>): ComponentSpec {
    val state = checkNotNull(componentClass.getAnnotation(State::class.java)) {
        "${componentClass.name} must be annotated with @State to be persisted"
    }
    check(STORAGE_NAME.matches(state.storage)) {
        "@State(storage = \"${state.storage}\") on ${componentClass.name} must be a logical name matching " +
            "${STORAGE_NAME.pattern}, without path separators or file extension"
    }
    val name = state.name.ifBlank { componentClass.simpleName }
    check(name.isNotBlank()) { "${componentClass.name} has no simple name; declare @State(name = ...)" }
    check('.' !in name && name.none { it.isWhitespace() }) {
        "@State name '$name' on ${componentClass.name} must not contain '.' or whitespace"
    }
    val version = try {
        Version.parse(state.version, strict = true)
    } catch (exception: VersionFormatException) {
        throw IllegalStateException(
            "@State(version = \"${state.version}\") on ${componentClass.name} is not a valid semantic version",
            exception,
        )
    }
    return ComponentSpec(StorageSpec(state.storage, state.format), name, version, componentClass)
}

/**
 * Checks that [spec] can live next to the [known] declarations: one format per storage and one class per
 * component name. Failures are programmer errors and throw.
 */
internal fun checkStateDeclaration(spec: ComponentSpec, known: Collection<ComponentSpec>) {
    val sameStorage = known.filter { it.storage.name == spec.storage.name }
    sameStorage.firstOrNull { it.storage.format != spec.storage.format }?.let { other ->
        error(
            "Storage '${spec.storage.name}' is declared as ${other.storage.format} by ${other.declaredBy.name} " +
                "but as ${spec.storage.format} by ${spec.declaredBy.name}",
        )
    }
    sameStorage.firstOrNull { it.name == spec.name && it.declaredBy != spec.declaredBy }?.let { other ->
        error(
            "Component '${spec.name}' in storage '${spec.storage.name}' is declared by both " +
                "${other.declaredBy.name} and ${spec.declaredBy.name}",
        )
    }
}
