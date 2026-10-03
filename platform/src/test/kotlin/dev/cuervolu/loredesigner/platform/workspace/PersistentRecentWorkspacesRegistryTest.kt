package dev.cuervolu.loredesigner.platform.workspace

import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.platform.state.FileStateStore
import dev.cuervolu.loredesigner.workspace.Workspace
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.Path
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class PersistentRecentWorkspacesRegistryTest {
    private val fileSystem = FakeFileSystem()
    private val appData = "/app-data".toPath()
    private val dispatcher = StandardTestDispatcher()
    private var now = Instant.parse("2026-10-01T12:00:00Z")
    private val clock = object : Clock {
        override fun now(): Instant = now
    }

    private val idA = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb380")
    private val idB = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb381")

    /** A fresh store and registry, as after an application restart. */
    private fun newRegistry() = PersistentRecentWorkspacesRegistry(
        FileStateStore(appData, fileSystem, ioDispatcher = dispatcher, clock = clock),
        clock,
    )

    private fun test(body: suspend TestScope.() -> Unit) = runTest(dispatcher) { body() }

    private fun workspace(id: WorkspaceId, path: String, name: String = "Embercourt", color: ProjectColor? = null) =
        Workspace(path.toPath(), ProjectConfig(1, id, name, color))

    private fun advance() {
        now += 5.minutes
    }

    @AfterTest
    fun tearDown() {
        fileSystem.checkNoOpenFiles()
    }

    @Test
    fun `first registration is persisted as a versioned component of workspaces json`() = test {
        val registry = newRegistry()
        registry.recordOpened(workspace(idA, "/worlds/Embercourt", color = ProjectColor.TEAL))

        val entry = registry.workspaces.value.single()
        assertEquals(idA, entry.id)
        assertEquals("/worlds/Embercourt".toPath(), entry.location)
        assertEquals(ProjectColor.TEAL, entry.lastKnownColor)
        assertEquals(now, entry.lastOpenedAt)
        assertFalse(entry.pinned)
        val root = Json.parseToJsonElement(fileSystem.read(appData / "workspaces.json") { readUtf8() }).jsonObject
        val component = root.getValue("components").jsonObject.getValue("RecentWorkspaces").jsonObject
        assertEquals("1.0.0", component.getValue("version").jsonPrimitive.content)
        val stored = component.getValue("state").jsonObject.getValue("entries").jsonArray.single().jsonObject
        assertEquals("teal", stored.getValue("lastKnownColor").jsonPrimitive.content)
    }

    @Test
    fun `updating the location keeps recency and the pin`() = test {
        val registry = newRegistry()
        registry.recordOpened(workspace(idA, "/worlds/Embercourt"))
        registry.setPinned(idA, true)
        val openedAt = now
        advance()

        registry.updateLocation(workspace(idA, "/archive/Ember", name = "Ember"))
        registry.updateLocation(workspace(idB, "/worlds/Unknown"))

        val entry = newRegistry().apply { initialize() }.workspaces.value.single()
        assertEquals("/archive/Ember".toPath(), entry.location)
        assertEquals("Ember", entry.lastKnownName)
        assertEquals(openedAt, entry.lastOpenedAt, "relocating is not opening")
        assertTrue(entry.pinned)
    }

    @Test
    fun `reopening updates the timestamp instead of duplicating`() = test {
        val registry = newRegistry()
        registry.recordOpened(workspace(idA, "/worlds/Embercourt"))
        registry.recordOpened(workspace(idB, "/worlds/Other", name = "Other"))
        advance()
        registry.recordOpened(workspace(idA, "/worlds/Embercourt"))

        assertEquals(listOf(idA, idB), registry.workspaces.value.map { it.id })
        assertEquals(now, registry.workspaces.value.first().lastOpenedAt)
    }

    @Test
    fun `identity is the workspace id, not the path`() = test {
        val registry = newRegistry()
        registry.recordOpened(workspace(idA, "/worlds/Embercourt"))
        advance()
        registry.recordOpened(workspace(idB, "/worlds/Embercourt", name = "Replacement"))

        assertEquals(2, registry.workspaces.value.size)
    }

    @Test
    fun `a moved workspace updates its existing entry and cached metadata`() = test {
        val registry = newRegistry()
        registry.recordOpened(workspace(idA, "/worlds/Embercourt", color = ProjectColor.RED))
        registry.setPinned(idA, true)
        advance()
        registry.recordOpened(workspace(idA, "/archive/Ember", name = "Ember", color = ProjectColor.BLUE))

        val entry = registry.workspaces.value.single()
        assertEquals("/archive/Ember".toPath(), entry.location)
        assertEquals("Ember", entry.lastKnownName)
        assertEquals(ProjectColor.BLUE, entry.lastKnownColor)
        assertTrue(entry.pinned, "reopening keeps the pin")
    }

    @Test
    fun `pin and unpin persist across a restart`() = test {
        newRegistry().apply {
            recordOpened(workspace(idA, "/worlds/Embercourt"))
            recordOpened(workspace(idB, "/worlds/Other", name = "Other"))
            setPinned(idA, true)
        }

        val restarted = newRegistry().apply { initialize() }
        assertEquals(setOf(idA), restarted.workspaces.value.filter { it.pinned }.map { it.id }.toSet())

        restarted.setPinned(idA, false)
        assertTrue(newRegistry().apply { initialize() }.workspaces.value.none { it.pinned })
    }

    @Test
    fun `forgetting removes only the entry and never touches the workspace folder`() = test {
        val folder = "/worlds/Embercourt".toPath()
        fileSystem.createDirectories(folder)
        fileSystem.write(folder / "project.lore") { writeUtf8("kept") }
        val registry = newRegistry()
        registry.recordOpened(workspace(idA, folder.toString()))

        registry.forget(idA)

        assertTrue(registry.workspaces.value.isEmpty())
        assertTrue(newRegistry().apply { initialize() }.workspaces.value.isEmpty())
        assertEquals("kept", fileSystem.read(folder / "project.lore") { readUtf8() })
    }

    @Test
    fun `entries survive a restart even when their folder is gone`() = test {
        newRegistry().recordOpened(workspace(idA, "/worlds/Embercourt", color = ProjectColor.AMBER))

        val restarted = newRegistry()
        restarted.initialize()

        val entry = restarted.workspaces.value.single()
        assertEquals("Embercourt", entry.lastKnownName)
        assertEquals(ProjectColor.AMBER, entry.lastKnownColor)
        assertFalse(fileSystem.exists("/worlds/Embercourt".toPath()))
        assertTrue(restarted.loaded.value)
    }

    @Test
    fun `corrupted registry file does not prevent startup`() = test {
        write(appData / "workspaces.json", "{ this is not json")

        val registry = newRegistry()
        registry.initialize()

        assertTrue(registry.loaded.value)
        assertTrue(registry.workspaces.value.isEmpty())
    }

    @Test
    fun `invalid and duplicate stored entries are cleaned up while loading`() = test {
        write(
            appData / "workspaces.json",
            """
            {"components": {"RecentWorkspaces": {"version": "1.0.0", "state": {"entries": [
              {"id": "nope", "path": "/x", "lastKnownName": "Broken", "lastOpenedAt": "2026-01-01T00:00:00Z"},
              {"id": "$idA", "path": "/old", "lastKnownName": "Old", "lastOpenedAt": "2026-01-01T00:00:00Z"},
              {"id": "$idA", "path": "/new", "lastKnownName": "New", "lastKnownColor": "ultraviolet",
               "lastOpenedAt": "2026-02-01T00:00:00Z"}
            ]}}}}
            """.trimIndent(),
        )

        val registry = newRegistry().apply { initialize() }

        val entry = registry.workspaces.value.single()
        assertEquals("/new".toPath(), entry.location)
        assertEquals(null, entry.lastKnownColor)
    }

    @Test
    fun `one unreadable entry is dropped without losing the others`() = test {
        write(
            appData / "workspaces.json",
            """
            {"components": {"RecentWorkspaces": {"version": "1.0.0", "state": {"entries": [
              {"id": "$idA", "path": "/a", "lastKnownName": "A", "lastOpenedAt": "not a date"},
              {"id": "$idB", "path": "/b", "lastOpenedAt": "2026-02-01T00:00:00Z"},
              {"id": "$idB", "path": "/b", "lastKnownName": "B", "lastOpenedAt": "2026-02-01T00:00:00Z"}
            ]}}}}
            """.trimIndent(),
        )

        val registry = newRegistry().apply { initialize() }

        assertEquals(listOf(idB), registry.workspaces.value.map { it.id })
    }

    @Test
    fun `a mutation before initialize loads first instead of clobbering the stored list`() = test {
        newRegistry().recordOpened(workspace(idA, "/worlds/Embercourt"))

        val restarted = newRegistry()
        restarted.recordOpened(workspace(idB, "/worlds/Other", name = "Other"))

        assertEquals(setOf(idA, idB), newRegistry().apply { initialize() }.workspaces.value.map { it.id }.toSet())
    }

    private fun write(path: Path, content: String) {
        fileSystem.createDirectories(path.parent!!)
        fileSystem.write(path) { writeUtf8(content) }
    }
}
