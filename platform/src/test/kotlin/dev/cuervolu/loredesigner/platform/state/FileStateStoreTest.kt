package dev.cuervolu.loredesigner.platform.state

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.mutableLoggerConfigInit
import dev.cuervolu.loredesigner.platform.logging.RecordingLogWriter
import dev.cuervolu.loredesigner.platform.workspace.FaultyFileSystem
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.IOException
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class FileStateStoreTest {
    private val fileSystem = FakeFileSystem()
    private val directory = "/app-data".toPath()
    private val dispatcher = StandardTestDispatcher()
    private var now = Instant.parse("2026-10-01T12:00:00Z")
    private val logged = mutableListOf<String>()
    private val errors = mutableListOf<RecordingLogWriter.Entry>()
    private val logger = Logger(
        mutableLoggerConfigInit(
            listOf(
                object : LogWriter() {
                    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
                        logged += message
                        throwable?.let { logged += it.stackTraceToString() }
                        if (severity ==
                            Severity.Error
                        ) {
                            errors += RecordingLogWriter.Entry(severity, tag, message, throwable)
                        }
                    }
                },
            ),
        ),
    )

    private fun store(fs: okio.FileSystem = fileSystem, io: kotlinx.coroutines.CoroutineDispatcher = dispatcher) =
        FileStateStore(
            directory = directory,
            fileSystem = fs,
            logger = logger,
            ioDispatcher = io,
            clock = object : Clock {
                override fun now(): Instant = now
            },
        )

    private fun test(body: suspend TestScope.() -> Unit) = runTest(dispatcher) { body() }

    private fun read(name: String) = fileSystem.read(directory / name) { readUtf8() }

    private fun write(name: String, content: String) {
        fileSystem.createDirectories(directory)
        fileSystem.write(directory / name) { writeUtf8(content) }
    }

    private fun stateFiles() = fileSystem.listOrNull(directory).orEmpty().map { it.name }.sorted()

    private fun jsonEntry(file: String, component: String): JsonObject =
        Json.parseToJsonElement(read(file)).jsonObject.getValue("components").jsonObject.getValue(component).jsonObject

    private fun JsonObject.version() = getValue("version").jsonPrimitive.content

    private fun JsonObject.state() = getValue("state").jsonObject

    @AfterTest
    fun tearDown() {
        fileSystem.checkNoOpenFiles()
    }

    @Test
    fun `metadata uses the simple class name, json and version 1_0_0 by default`() {
        val spec = resolveStateMetadata(DefaultNamedComponent::class.java)

        assertEquals("DefaultNamedComponent", spec.name)
        assertEquals("ui", spec.storage.name)
        assertEquals(StateFormat.JSON, spec.storage.format)
        assertEquals("1.0.0", spec.version.toString())
    }

    @Test
    fun `storage names resolve to json and properties files in the data directory`() = test {
        store().save(DefaultNamedComponent().apply { loadState(TestState("hello")) })
        store().save(ApplicationComponent().apply { loadState(TestState("hello")) })

        assertEquals(listOf("application.properties", "ui.json"), stateFiles())
    }

    @Test
    fun `json state round trips with a version per component`() = test {
        val saved = TestState("Embercourt", 3, listOf("a", "b"))
        store().save(LauncherComponent().apply { loadState(saved) })

        val reloaded = LauncherComponent()
        assertEquals(StateLoadResult.Loaded, store().load(reloaded))
        assertEquals(saved, reloaded.getState())
        val root = Json.parseToJsonElement(read("ui.json")).jsonObject
        assertEquals(setOf("components"), root.keys, "there is no file-wide version")
        val entry = jsonEntry("ui.json", "Launcher")
        assertEquals("1.0.0", entry.version())
        assertEquals("Embercourt", entry.state().getValue("label").jsonPrimitive.content)
    }

    @Test
    fun `properties state round trips including non ascii text`() = test {
        val saved = TestState("Ñandú — 世界", 7, listOf("x"))
        store().save(ApplicationComponent().apply { loadState(saved) })

        val reloaded = ApplicationComponent()
        assertEquals(StateLoadResult.Loaded, store().load(reloaded))
        assertEquals(saved, reloaded.getState())
        val lines = read("application.properties").lines()
        assertTrue("App.version=1.0.0" in lines, lines.toString())
        assertTrue(lines.any { it.startsWith("App.state.label=") }, lines.toString())
        assertTrue(lines.all { line -> line.all { it.code < 128 } }, "properties are written as escaped ASCII")
        assertTrue(lines.none { it.startsWith("#") }, "no timestamp comment")
    }

    @Test
    fun `components sharing a json storage keep their own versions and state`() = test {
        val store = store()
        store.save(LauncherComponent().apply { loadState(TestState("launcher")) })
        store.save(WindowV2Component().apply { loadState(TestState("window")) })
        store.save(LauncherComponent().apply { loadState(TestState("launcher, again")) })

        val launcher = LauncherComponent()
        val window = WindowV2Component()
        assertEquals(StateLoadResult.Loaded, store().load(launcher))
        assertEquals(StateLoadResult.Loaded, store().load(window))
        assertEquals("launcher, again", launcher.getState().label)
        assertEquals("window", window.getState().label)
        assertEquals("1.0.0", jsonEntry("ui.json", "Launcher").version())
        assertEquals("2.1.0", jsonEntry("ui.json", "Window").version())
        assertEquals(listOf("ui.json"), stateFiles())
    }

    @Test
    fun `components sharing a properties storage keep their own versions and state`() = test {
        store().save(ApplicationComponent().apply { loadState(TestState("app")) })
        store().save(OtherApplicationV3Component().apply { loadState(TestState("other")) })
        store().save(ApplicationComponent().apply { loadState(TestState("app 2")) })

        val app = ApplicationComponent()
        val other = OtherApplicationV3Component()
        assertEquals(StateLoadResult.Loaded, store().load(app))
        assertEquals(StateLoadResult.Loaded, store().load(other))
        assertEquals("app 2", app.getState().label)
        assertEquals("other", other.getState().label)
        val lines = read("application.properties").lines()
        assertTrue("App.version=1.0.0" in lines && "Other.version=3.0.0" in lines, lines.toString())
    }

    @Test
    fun `saving keeps json components and top level keys this release does not know`() = test {
        write(
            "ui.json",
            """{"components": {"FromTheFuture": {"anything": [1, 2]}}, "metadata": {"by": "later release"}}""",
        )

        store().save(LauncherComponent().apply { loadState(TestState("launcher")) })

        val root = Json.parseToJsonElement(read("ui.json")).jsonObject
        assertEquals(setOf("FromTheFuture", "Launcher"), root.getValue("components").jsonObject.keys)
        assertEquals("later release", root.getValue("metadata").jsonObject.getValue("by").jsonPrimitive.content)
    }

    @Test
    fun `saving keeps properties of unknown components and keys outside any component`() = test {
        write("application.properties", "Future.version=1.0.0\nFuture.state.x=1\nloose=kept\n")

        store().save(ApplicationComponent().apply { loadState(TestState("app")) })

        val lines = read("application.properties").lines()
        assertTrue(
            listOf("Future.version=1.0.0", "Future.state.x=1", "loose=kept").all {
                it in lines
            },
            lines.toString(),
        )
    }

    @Test
    fun `missing file leaves defaults and creates nothing`() = test {
        val component = LauncherComponent()

        assertEquals(StateLoadResult.Missing, store().load(component))
        assertEquals(TestState(), component.getState())
        assertEquals(emptyList(), stateFiles())
    }

    @Test
    fun `missing component entry in an existing storage leaves defaults`() = test {
        store().save(WindowV2Component().apply { loadState(TestState("window")) })
        val launcher = LauncherComponent()

        assertEquals(StateLoadResult.Missing, store().load(launcher))
        assertEquals(TestState(), launcher.getState())
    }

    @Test
    fun `malformed or oddly shaped json is unreadable and the component keeps defaults`() = test {
        write("ui.json", "{ not json")
        val component = LauncherComponent()
        assertIs<StateLoadResult.StorageUnreadable>(store().load(component))
        assertEquals(TestState(), component.getState())

        write("ui.json", """["a", "b"]""")
        assertIs<StateLoadResult.StorageUnreadable>(store().load(component))
    }

    @Test
    fun `malformed properties escape is reported and the component keeps defaults`() = test {
        write("application.properties", "App.version=1.0.0\nApp.state.label=\\uZZZZ\n")
        val component = ApplicationComponent()

        assertIs<StateLoadResult.StorageUnreadable>(store().load(component))
        assertEquals(TestState(), component.getState())
    }

    @Test
    fun `an invalid json payload is backed up before it is replaced and its sibling survives`() = test {
        val original =
            """{"components": {"Launcher": {"version": "1.0.0", "state": {"count": "secret-payload"}}, """ +
                """"Window": {"version": "2.1.0", "state": {"label": "kept"}}}}"""
        write("ui.json", original)
        val launcher = LauncherComponent()
        val window = WindowV2Component()

        assertIs<StateLoadResult.ComponentInvalid>(store().load(launcher))
        assertEquals(StateLoadResult.Loaded, store().load(window))
        assertEquals(TestState(), launcher.getState())

        assertEquals(StateSaveResult.Saved, store().save(launcher.apply { loadState(TestState("repaired")) }))

        assertEquals(original, read("ui.json.corrupt-20261001-120000"))
        val reloadedWindow = WindowV2Component().also { store().load(it) }
        val reloadedLauncher = LauncherComponent().also { store().load(it) }
        assertEquals("kept", reloadedWindow.getState().label)
        assertEquals("repaired", reloadedLauncher.getState().label)
        assertTrue(logged.none { "secret-payload" in it }, "stored values must never be logged")
    }

    @Test
    fun `an invalid properties payload is backed up before it is replaced and its sibling survives`() = test {
        val original = "App.version=1.0.0\nApp.state.count=many\nOther.version=3.0.0\nOther.state.label=fine\n"
        write("application.properties", original)
        val app = ApplicationComponent()
        val other = OtherApplicationV3Component()

        assertIs<StateLoadResult.ComponentInvalid>(store().load(app))
        assertEquals(StateLoadResult.Loaded, store().load(other))
        assertEquals("fine", other.getState().label)

        store().save(app.apply { loadState(TestState("repaired")) })

        assertEquals(original, read("application.properties.corrupt-20261001-120000"))
        val reloaded = OtherApplicationV3Component().also { store().load(it) }
        assertEquals("fine", reloaded.getState().label)
    }

    @Test
    fun `an older version within the same major is loaded and saved with the current version`() = test {
        write("versioned.json", """{"components": {"Data": {"version": "1.0.4", "state": {"label": "older"}}}}""")
        val component = VersionedComponent()

        assertEquals(StateLoadResult.Loaded, store().load(component))
        assertEquals("older", component.getState().label)
        store().save(component)

        assertEquals("1.2.0", jsonEntry("versioned.json", "Data").version())
        assertEquals(listOf("versioned.json"), stateFiles())
    }

    @Test
    fun `a newer json version within the same major keeps its version and unknown fields`() = test {
        write(
            "versioned.json",
            """{"components": {"Data": {"version": "1.9.3", "state": {"label": "minor", "added": {"x": true}}}}}""",
        )
        val component = VersionedComponent()

        assertEquals(StateLoadResult.Loaded, store().load(component))
        assertEquals("minor", component.getState().label)
        assertEquals(StateSaveResult.Saved, store().save(component.apply { loadState(TestState("edited")) }))

        val entry = jsonEntry("versioned.json", "Data")
        assertEquals("1.9.3", entry.version(), "the version is never lowered")
        assertEquals("edited", entry.state().getValue("label").jsonPrimitive.content)
        assertEquals("true", entry.state().getValue("added").jsonObject.getValue("x").jsonPrimitive.content)
    }

    @Test
    fun `a newer properties version within the same major keeps its version and unknown keys`() = test {
        write(
            "application.properties",
            "App.version=1.4.0\nApp.state.label=minor\nApp.state.added.0=x\nApp.state.tags.0=old\n",
        )
        val component = ApplicationComponent()

        assertEquals(StateLoadResult.Loaded, store().load(component))
        store().save(component.apply { loadState(TestState("edited")) })

        val lines = read("application.properties").lines()
        assertTrue("App.version=1.4.0" in lines, lines.toString())
        assertTrue("App.state.added.0=x" in lines, lines.toString())
        assertTrue("App.state.label=edited" in lines, lines.toString())
        assertTrue(lines.none { it.startsWith("App.state.tags.") }, "known fields are replaced, not merged")
    }

    @Test
    fun `a newer minor version whose state is not an object is not overwritten`() = test {
        val stored = """{"components": {"Names": {"version": "1.5.0", "state": ["a", "b"]}}}"""
        write("lists.json", stored)
        val component = NamesComponent()

        assertEquals(StateLoadResult.Loaded, store().load(component))
        assertIs<StateSaveResult.Blocked>(store().save(component.apply { loadState(listOf("c")) }))
        assertEquals(stored, read("lists.json"))
    }

    @Test
    fun `newer major version is neither loaded nor overwritten`() = test {
        val future =
            """{"components": {"Data": {"version": "2.0.0", "state": {"label": "future"}}, """ +
                """"Other": {"version": "1.0.0", "state": {}}}}"""
        write("versioned.json", future)
        val component = VersionedComponent()

        assertEquals(StateLoadResult.NewerVersion("2.0.0", "1.2.0"), store().load(component))
        assertEquals(TestState(), component.getState())

        val result = store().save(component.apply { loadState(TestState("mine")) })

        assertEquals(StateSaveResult.Blocked("2.0.0", "1.2.0"), result)
        assertEquals(future, read("versioned.json"))
        assertEquals(listOf("versioned.json"), stateFiles())
    }

    @Test
    fun `older major version is never decoded and is backed up before being replaced`() = test {
        val old = """{"components": {"Data": {"version": "0.4.0", "state": {"label": "old"}}}}"""
        write("versioned.json", old)
        val component = VersionedComponent()

        assertEquals(StateLoadResult.MigrationRequired("0.4.0", "1.2.0"), store().load(component))
        assertEquals(TestState(), component.getState(), "an older major is never decoded implicitly")

        assertEquals(StateSaveResult.Saved, store().save(component.apply { loadState(TestState("new")) }))
        assertEquals(old, read("versioned.json.v0-20261001-120000"))
        assertEquals("1.2.0", jsonEntry("versioned.json", "Data").version())
    }

    @Test
    fun `malformed or missing persisted versions are invalid and preserved before replacement`() = test {
        val malformed = """{"components": {"Data": {"version": "one", "state": {"label": "x"}}}}"""
        write("versioned.json", malformed)
        assertIs<StateLoadResult.ComponentInvalid>(store().load(VersionedComponent()))
        store().save(VersionedComponent())
        assertEquals(malformed, read("versioned.json.corrupt-20261001-120000"))

        val unversioned = """{"components": {"Data": {"label": "no version"}}}"""
        write("versioned.json", unversioned)
        assertIs<StateLoadResult.ComponentInvalid>(store().load(VersionedComponent()))
        store().save(VersionedComponent())
        assertEquals(unversioned, read("versioned.json.corrupt-20261001-120000-1"))
    }

    @Test
    fun `repeated corruption never overwrites an earlier backup`() = test {
        write("ui.json", "first broken")
        store().save(LauncherComponent())
        write("ui.json", "second broken")
        store().save(LauncherComponent())

        assertEquals("first broken", read("ui.json.corrupt-20261001-120000"))
        assertEquals("second broken", read("ui.json.corrupt-20261001-120000-1"))
    }

    @Test
    fun `overlapping saves of one component never write a stale snapshot`() = test {
        val io = StandardTestDispatcher(testScheduler)
        val store = store(io = io)
        val component = LauncherComponent().apply { loadState(TestState("older")) }

        // The first save waits to be dispatched to the I/O dispatcher...
        val first = launch(start = CoroutineStart.UNDISPATCHED) { store.save(component) }
        component.loadState(TestState("newer"))
        // ...while a second one, already on that dispatcher, runs to completion before it.
        launch(io, start = CoroutineStart.UNDISPATCHED) { store.save(component) }
        first.join()

        val reloaded = LauncherComponent().also { store().load(it) }
        assertEquals("newer", reloaded.getState().label)
    }

    @Test
    fun `declaration errors fail loudly`() = test {
        assertTrue(
            "not-semver" in assertFailsWith<IllegalStateException> { store().load(MalformedVersionComponent()) }
                .message.orEmpty(),
        )
        assertFailsWith<IllegalStateException> { store().load(PathLikeStorageComponent()) }
        assertFailsWith<IllegalStateException> { store().load(UnannotatedComponent()) }

        val store = store()
        store.load(LauncherComponent())
        assertTrue(
            "PROPERTIES" in assertFailsWith<IllegalStateException> { store.load(ConflictingFormatComponent()) }
                .message.orEmpty(),
        )
        assertFailsWith<IllegalStateException> { store.load(DuplicateLauncherComponent()) }
    }

    @Test
    fun `failed atomic replace keeps the previous file and leaves no temporary files`() = test {
        val faulty = FaultyFileSystem(fileSystem)
        val store = store(faulty)
        store.save(LauncherComponent().apply { loadState(TestState("before")) })
        val before = read("ui.json")
        faulty.beforeOperation = { operation, _ ->
            if (operation == FaultyFileSystem.Operation.ATOMIC_MOVE) throw IOException("disk full")
        }

        val component = LauncherComponent().apply { loadState(TestState("after")) }
        val result = store.save(component)

        assertIs<StateSaveResult.Failed>(result)
        assertEquals("disk full", errors.single().throwable?.message, "the I/O cause is logged with the failure")
        assertEquals("after", component.getState().label, "in-memory state is kept")
        assertEquals(before, read("ui.json"))
        assertEquals(listOf("ui.json"), stateFiles())
    }

    @Test
    fun `unreadable file during save is not overwritten`() = test {
        val faulty = FaultyFileSystem(fileSystem)
        val content = """{"components": {"Window": {"version": "2.1.0", "state": {"label": "keep"}}}}"""
        write("ui.json", content)
        faulty.beforeOperation = { operation, _ ->
            if (operation == FaultyFileSystem.Operation.SOURCE) throw IOException("unplugged")
        }

        assertIs<StateSaveResult.Failed>(store(faulty).save(LauncherComponent()))
        assertEquals(content, read("ui.json"))
    }
}

@Serializable
data class TestState(val label: String = "default", val count: Int = 0, val tags: List<String> = emptyList())

abstract class TestComponent : PersistentStateComponent<TestState> {
    private var state = TestState()
    override val serializer: KSerializer<TestState> = TestState.serializer()

    override fun getState(): TestState = state

    override fun loadState(state: TestState) {
        this.state = state
    }
}

@State(storage = "ui")
class DefaultNamedComponent : TestComponent()

@State(storage = "ui", name = "Launcher")
class LauncherComponent : TestComponent()

@State(storage = "ui", name = "Window", version = "2.1.0")
class WindowV2Component : TestComponent()

@State(storage = "ui", name = "Launcher")
class DuplicateLauncherComponent : TestComponent()

@State(storage = "ui", name = "Other", format = StateFormat.PROPERTIES)
class ConflictingFormatComponent : TestComponent()

@State(storage = "application", name = "App", format = StateFormat.PROPERTIES)
class ApplicationComponent : TestComponent()

@State(storage = "application", name = "Other", version = "3.0.0", format = StateFormat.PROPERTIES)
class OtherApplicationV3Component : TestComponent()

@State(storage = "versioned", name = "Data", version = "1.2.0")
class VersionedComponent : TestComponent()

@State(storage = "lists", name = "Names")
class NamesComponent : PersistentStateComponent<List<String>> {
    private var names = emptyList<String>()
    override val serializer: KSerializer<List<String>> = ListSerializer(String.serializer())

    override fun getState(): List<String> = names

    override fun loadState(state: List<String>) {
        names = state
    }
}

@State(storage = "broken", version = "not-semver")
class MalformedVersionComponent : TestComponent()

@State(storage = "../ui.json")
class PathLikeStorageComponent : TestComponent()

class UnannotatedComponent : TestComponent()
