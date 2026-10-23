package dev.cuervolu.loredesigner.ui.session

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.cuervolu.loredesigner.core.workspace.ProjectConfig
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.ui.navigation.AppRoute
import dev.cuervolu.loredesigner.workspace.Workspace
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalTestApi::class)
class AppSessionStateTest {
    private fun workspace(name: String) = Workspace(
        location = "/worlds/$name".toPath(),
        config = ProjectConfig(version = 1, id = WorkspaceId(Uuid.random()), name = name, color = null),
    )

    private fun withSession(block: (AppSessionState) -> Unit) = runComposeUiTest {
        lateinit var session: AppSessionState
        setContent { session = rememberAppSessionState() }
        runOnIdle { block(session) }
    }

    @Test
    fun `opening a workspace navigates to it`() = withSession { session ->
        val workspace = workspace("Embercourt")

        session.openWorkspace(workspace)

        assertEquals(workspace, session.activeWorkspace)
        assertEquals(AppRoute.Workspace(workspace.config.id.toString()), session.backStack.last())
    }

    @Test
    fun `closing returns to the launcher and resets the layout`() = withSession { session ->
        session.openWorkspace(workspace("Embercourt"))
        val shell = session.shell
        shell.toggleExplorer()
        assertFalse(shell.explorerVisible)

        session.closeWorkspace()

        assertNull(session.activeWorkspace)
        assertEquals(listOf<Any>(AppRoute.Launcher), session.backStack.toList())
        assertNotSame(shell, session.shell)
        assertTrue(session.shell.explorerVisible)
    }

    @Test
    fun `each project starts with the default layout`() = withSession { session ->
        session.openWorkspace(workspace("Embercourt"))
        session.shell.toggleProperties()
        session.closeWorkspace()

        session.openWorkspace(workspace("Thistlewood"))

        assertTrue(session.shell.propertiesVisible)
    }
}
