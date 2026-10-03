package dev.cuervolu.loredesigner.platform.workspace

import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.platform.state.PersistentStateComponent
import dev.cuervolu.loredesigner.platform.state.State
import dev.cuervolu.loredesigner.platform.state.StateStore
import dev.cuervolu.loredesigner.workspace.Workspace
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspace
import dev.cuervolu.loredesigner.workspace.recent.RecentWorkspacesRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okio.Path.Companion.toPath
import kotlin.time.Clock
import kotlin.time.Instant

/** [RecentWorkspacesRegistry] persisted in `workspaces.json` in the application data folder. */
class PersistentRecentWorkspacesRegistry(
    private val stateStore: StateStore,
    private val logger: Logger,
    private val clock: Clock = Clock.System,
) : RecentWorkspacesRegistry {
    private val entries = MutableStateFlow<List<RecentWorkspace>>(emptyList())
    override val workspaces: StateFlow<List<RecentWorkspace>> = entries.asStateFlow()

    private val loadedState = MutableStateFlow(false)
    override val loaded: StateFlow<Boolean> = loadedState.asStateFlow()

    private val persisted = PersistedRecentWorkspaces()

    // Serializes load and every read-modify-save so a mutation can never persist a list that was
    // built before the stored one was loaded.
    private val mutex = Mutex()

    override suspend fun initialize() = mutex.withLock { ensureLoaded() }

    override suspend fun recordOpened(workspace: Workspace) =
        mutate("record opening ${workspace.config.id}") { current ->
            val id = workspace.config.id
            val opened = RecentWorkspace(
                id = id,
                location = workspace.location,
                lastKnownName = workspace.config.name,
                lastKnownColor = workspace.config.color,
                lastOpenedAt = clock.now(),
                pinned = current.firstOrNull { it.id == id }?.pinned ?: false,
            )
            current.filterNot { it.id == id } + opened
        }

    override suspend fun updateLocation(workspace: Workspace) = mutate("relocate ${workspace.config.id}") { current ->
        current.map { recent ->
            if (recent.id != workspace.config.id) return@map recent
            recent.copy(
                location = workspace.location,
                lastKnownName = workspace.config.name,
                lastKnownColor = workspace.config.color,
            )
        }
    }

    override suspend fun setPinned(id: WorkspaceId, pinned: Boolean) =
        mutate("${if (pinned) "pin" else "unpin"} $id") { current ->
            current.map { if (it.id == id) it.copy(pinned = pinned) else it }
        }

    override suspend fun forget(id: WorkspaceId) = mutate("forget $id") { current -> current.filterNot { it.id == id } }

    private suspend fun mutate(operation: String, transform: (List<RecentWorkspace>) -> List<RecentWorkspace>) {
        mutex.withLock {
            ensureLoaded()
            val current = entries.value
            val next = transform(current).sortedForDisplay()
            if (next == current) {
                logger.v { "Recent workspaces: $operation changed nothing" }
                return
            }
            logger.d { "Recent workspaces: $operation (${next.size} remembered)" }
            entries.value = next
            // Failures are logged by the store; the in-memory list stays authoritative for this session.
            stateStore.save(persisted)
        }
    }

    private suspend fun ensureLoaded() {
        if (loadedState.value) return
        stateStore.load(persisted)
        loadedState.value = true
    }

    private fun List<RecentWorkspace>.sortedForDisplay(): List<RecentWorkspace> =
        sortedWith(compareByDescending<RecentWorkspace> { it.lastOpenedAt }.thenBy { it.lastKnownName })

    private fun RecentWorkspace.toEntry() = StoredEntry(
        id = id.toString(),
        path = location.toString(),
        lastKnownName = lastKnownName,
        lastKnownColor = lastKnownColor?.serializedName,
        lastOpenedAt = lastOpenedAt,
        pinned = pinned,
    )

    // Decoding messages can quote the stored entry, so only the position and exception type are logged.
    private fun JsonElement.toRecentWorkspace(index: Int): RecentWorkspace? {
        val entry = try {
            entryJson.decodeFromJsonElement(StoredEntry.serializer(), this)
        } catch (exception: SerializationException) {
            logger.w {
                "Ignoring remembered workspace entry #$index that cannot be read (${exception::class.simpleName})"
            }
            return null
        } catch (exception: IllegalArgumentException) {
            logger.w {
                "Ignoring remembered workspace entry #$index that cannot be read (${exception::class.simpleName})"
            }
            return null
        }
        val workspaceId = try {
            WorkspaceId.parse(entry.id)
        } catch (_: IllegalArgumentException) {
            logger.w { "Ignoring remembered workspace entry #$index with an invalid id" }
            return null
        }
        return RecentWorkspace(
            id = workspaceId,
            location = entry.path.toPath(),
            lastKnownName = entry.lastKnownName,
            // A color from a newer release is only a cached hint; dropping it is harmless.
            lastKnownColor = entry.lastKnownColor?.let(::projectColorOrNull),
            lastOpenedAt = entry.lastOpenedAt,
            pinned = entry.pinned,
        )
    }

    /** The persisted form of the registry; internal only so the declaration can be validated in tests. */
    @State(storage = "workspaces", name = "RecentWorkspaces", version = "1.0.0")
    internal inner class PersistedRecentWorkspaces : PersistentStateComponent<RecentWorkspacesState> {
        override val serializer: KSerializer<RecentWorkspacesState> = RecentWorkspacesState.serializer()

        override fun getState(): RecentWorkspacesState = RecentWorkspacesState(
            entries.value.map { entryJson.encodeToJsonElement(StoredEntry.serializer(), it.toEntry()) },
        )

        override fun loadState(state: RecentWorkspacesState) {
            val readable = state.entries.mapIndexedNotNull { index, entry -> entry.toRecentWorkspace(index) }
            entries.value = readable
                .groupBy { it.id }
                .map { (_, duplicates) -> duplicates.maxBy { it.lastOpenedAt } }
                .sortedForDisplay()
            logger.d {
                "Loaded ${entries.value.size} recent workspaces (${state.entries.size - readable.size} unreadable, " +
                    "${readable.size - entries.value.size} duplicates dropped)"
            }
        }
    }

    private companion object {
        val entryJson = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}

// Entries are kept as raw JSON so one unreadable entry is dropped on its own instead of invalidating
// the whole list.
@Serializable
internal data class RecentWorkspacesState(val entries: List<JsonElement> = emptyList())

@Serializable
private data class StoredEntry(
    val id: String,
    val path: String,
    val lastKnownName: String,
    val lastKnownColor: String? = null,
    val lastOpenedAt: Instant,
    val pinned: Boolean = false,
)
