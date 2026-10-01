package dev.cuervolu.loredesigner.platform.workspace

import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.workspace.WorkspaceError
import dev.cuervolu.loredesigner.workspace.WorkspaceResult
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class ProjectFileCodecTest {
    private val codec = ProjectFileCodec()
    private val path = "project.lore".toPath()
    private val id = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-4fd2ed9cb380")

    @Test
    fun `project configuration round trips`() {
        val original = ProjectConfig(1, id, "My World", ProjectColor.VIOLET)

        val result = codec.decode(path, codec.encode(original))

        assertEquals(original, assertIs<WorkspaceResult.Success<ProjectConfig>>(result).value)
    }

    @Test
    fun `null color is omitted and round trips`() {
        val original = ProjectConfig(1, id, "Colorless", null)

        val encoded = codec.encode(original)
        val result = codec.decode(path, encoded)

        assertFalse(encoded.contains("color"))
        assertEquals(original, assertIs<WorkspaceResult.Success<ProjectConfig>>(result).value)
    }

    @Test
    fun `missing required fields are invalid project files`() {
        val validFields =
            linkedMapOf(
                "version" to "1",
                "id" to "\"$id\"",
                "name" to "\"My World\"",
            )

        validFields.keys.forEach { missingField ->
            val content =
                validFields
                    .filterKeys { it != missingField }
                    .entries
                    .joinToString("\n") { (key, value) -> "$key = $value" }

            val result = codec.decode(path, content)

            assertIs<WorkspaceError.InvalidProjectFile>(
                assertIs<WorkspaceResult.Failure>(result, "missing $missingField must fail").error,
            )
        }
    }

    @Test
    fun `malformed toml is an invalid project file`() {
        val result = codec.decode(path, "version = [")

        assertIs<WorkspaceError.InvalidProjectFile>(assertIs<WorkspaceResult.Failure>(result).error)
    }

    @Test
    fun `invalid uuid has a specific error`() {
        val result = codec.decode(path, validProject(id = "not-a-uuid"))

        assertIs<WorkspaceError.InvalidWorkspaceId>(assertIs<WorkspaceResult.Failure>(result).error)
    }

    @Test
    fun `uuid from another version is rejected`() {
        val result = codec.decode(path, validProject(id = "550e8400-e29b-41d4-a716-446655440000"))

        assertIs<WorkspaceError.InvalidWorkspaceId>(assertIs<WorkspaceResult.Failure>(result).error)
    }

    @Test
    fun `unknown color has a specific error`() {
        val result = codec.decode(path, validProject(color = "orange"))

        assertIs<WorkspaceError.UnknownProjectColor>(assertIs<WorkspaceResult.Failure>(result).error)
    }

    @Test
    fun `unsupported version has a specific error`() {
        val result = codec.decode(path, validProject(version = 2))

        val error = assertIs<WorkspaceError.UnsupportedProjectVersion>(
            assertIs<WorkspaceResult.Failure>(result).error,
        )
        assertEquals(2, error.version)
    }

    @Test
    fun `unknown keys are rejected for version one`() {
        val result = codec.decode(path, validProject() + "\nunexpected = true")

        assertIs<WorkspaceError.InvalidProjectFile>(assertIs<WorkspaceResult.Failure>(result).error)
    }

    @Test
    fun `blank name is an invalid project file`() {
        val result = codec.decode(path, validProject(name = "   "))

        assertIs<WorkspaceError.InvalidProjectFile>(assertIs<WorkspaceResult.Failure>(result).error)
    }

    private fun validProject(
        version: Int = 1,
        id: String = this.id.toString(),
        name: String = "My World",
        color: String? = "violet",
    ): String = buildString {
        appendLine("version = $version")
        appendLine("id = \"$id\"")
        appendLine("name = \"$name\"")
        if (color != null) append("color = \"$color\"")
    }
}
