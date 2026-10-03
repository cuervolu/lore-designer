package dev.cuervolu.loredesigner.ui.launcher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.launcher_empty_missing_title
import dev.cuervolu.loredesigner.ui.resources.launcher_empty_welcome_title
import dev.cuervolu.loredesigner.ui.resources.launcher_project_count
import dev.cuervolu.loredesigner.ui.resources.launcher_project_forget
import dev.cuervolu.loredesigner.ui.resources.launcher_project_locate
import dev.cuervolu.loredesigner.ui.resources.launcher_project_pin
import dev.cuervolu.loredesigner.ui.resources.launcher_project_unpin
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LauncherContentTest {
    private val available = LauncherProject(
        id = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-000000000001"),
        name = "Embercourt",
        path = "/worlds/Embercourt",
        color = ProjectColor.TEAL,
        pinned = true,
        availability = ProjectAvailability.Available,
    )
    private val missing = LauncherProject(
        id = WorkspaceId.parse("01995f7e-1d74-7c83-a8a9-000000000002"),
        name = "Thistlewood",
        path = "/media/usb/Thistlewood",
        color = null,
        pinned = false,
        availability = ProjectAvailability.Missing,
    )
    private val projects = LauncherProjects(
        all = listOf(available),
        pinned = listOf(available),
        recent = listOf(available),
        missing = listOf(missing),
    )

    private val events = mutableListOf<String>()
    private val actions = LauncherProjectActions(
        onOpen = { events += "open $it" },
        onPinnedChange = { id, pinned -> events += "pin $id $pinned" },
        onForget = { events += "forget $it" },
        onLocate = { events += "locate $it" },
    )

    @Test
    fun `sections show their real counts and hide empty states when they have projects`() = runComposeUiTest {
        val oneProject = getPluralString(Res.plurals.launcher_project_count, 1, 1)
        val noProjects = getPluralString(Res.plurals.launcher_project_count, 0, 0)
        val welcome = getString(Res.string.launcher_empty_welcome_title)
        var current by mutableStateOf(projects)
        setContent {
            LoreDesignerTheme(darkTheme = false) {
                LauncherContent(
                    section = LauncherSection.Projects,
                    onSectionChange = {},
                    onNewProject = {},
                    onOpenProject = {},
                    projects = current,
                    projectsLoaded = true,
                    projectActions = actions,
                )
            }
        }

        onNodeWithText(oneProject).assertExists()
        onNodeWithText("Embercourt").assertExists()
        onNodeWithText(welcome).assertDoesNotExist()

        current = LauncherProjects()

        onNodeWithText(noProjects).assertExists()
        onNodeWithText(welcome).assertExists()
    }

    @Test
    fun `available rows open and toggle their pin`() = runComposeUiTest {
        val unpin = getString(Res.string.launcher_project_unpin)
        setContent {
            LoreDesignerTheme(darkTheme = false) {
                LauncherContent(
                    section = LauncherSection.Pinned,
                    onSectionChange = {},
                    onNewProject = {},
                    onOpenProject = {},
                    projects = projects,
                    projectsLoaded = true,
                    projectActions = actions,
                )
            }
        }

        onNodeWithText("Embercourt").performClick()
        onNodeWithContentDescription(unpin).performClick()

        assertEquals(listOf("open ${available.id}", "pin ${available.id} false"), events)
    }

    @Test
    fun `missing rows can be retried, located, pinned or forgotten`() = runComposeUiTest {
        val locate = getString(Res.string.launcher_project_locate)
        val forget = getString(Res.string.launcher_project_forget)
        val pin = getString(Res.string.launcher_project_pin)
        val nothingMissing = getString(Res.string.launcher_empty_missing_title)
        setContent {
            LoreDesignerTheme(darkTheme = false) {
                LauncherContent(
                    section = LauncherSection.Missing,
                    onSectionChange = {},
                    onNewProject = {},
                    onOpenProject = {},
                    projects = projects,
                    projectsLoaded = true,
                    projectActions = actions,
                )
            }
        }

        onNodeWithText(nothingMissing).assertDoesNotExist()
        onNodeWithText("/media/usb/Thistlewood").assertExists()
        onNodeWithText("Thistlewood").performClick()
        onNodeWithText(locate).performClick()
        onNodeWithContentDescription(pin).performClick()
        onNodeWithContentDescription(forget).performClick()

        assertEquals(
            listOf("open ${missing.id}", "locate ${missing.id}", "pin ${missing.id} true", "forget ${missing.id}"),
            events,
        )
    }

    @Test
    fun `project rows take keyboard focus and open with enter`() = runComposeUiTest {
        setContent {
            LoreDesignerTheme(darkTheme = false) {
                LauncherContent(
                    section = LauncherSection.Projects,
                    onSectionChange = {},
                    onNewProject = {},
                    onOpenProject = {},
                    projects = projects,
                    projectsLoaded = true,
                    projectActions = actions,
                )
            }
        }

        val row = onNodeWithText("Embercourt")
        row.requestFocus()
        row.assertIsFocused()
        row.performKeyInput { pressKey(Key.Enter) }

        assertEquals(listOf("open ${available.id}"), events)
    }

    @Test
    fun `nothing is shown until remembered projects are loaded`() = runComposeUiTest {
        val welcome = getString(Res.string.launcher_empty_welcome_title)
        setContent {
            LoreDesignerTheme(darkTheme = false) {
                LauncherContent(
                    section = LauncherSection.Projects,
                    onSectionChange = {},
                    onNewProject = {},
                    onOpenProject = {},
                    projects = LauncherProjects(),
                    projectsLoaded = false,
                    projectActions = actions,
                )
            }
        }

        onNodeWithText(welcome).assertDoesNotExist()
    }
}
