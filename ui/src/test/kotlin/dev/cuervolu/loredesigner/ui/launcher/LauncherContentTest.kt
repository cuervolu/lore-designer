package dev.cuervolu.loredesigner.ui.launcher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.cuervolu.loredesigner.core.workspace.ProjectColor
import dev.cuervolu.loredesigner.core.workspace.WorkspaceId
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.launcher_empty_welcome_title
import dev.cuervolu.loredesigner.ui.resources.launcher_menu_copy_path
import dev.cuervolu.loredesigner.ui.resources.launcher_menu_copy_previous_path
import dev.cuervolu.loredesigner.ui.resources.launcher_menu_locate
import dev.cuervolu.loredesigner.ui.resources.launcher_menu_open
import dev.cuervolu.loredesigner.ui.resources.launcher_menu_pin
import dev.cuervolu.loredesigner.ui.resources.launcher_menu_remove
import dev.cuervolu.loredesigner.ui.resources.launcher_menu_show_in_folder
import dev.cuervolu.loredesigner.ui.resources.launcher_menu_unpin
import dev.cuervolu.loredesigner.ui.resources.launcher_project_count
import dev.cuervolu.loredesigner.ui.resources.launcher_project_locate
import dev.cuervolu.loredesigner.ui.resources.launcher_project_unpin
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import org.jetbrains.compose.resources.StringResource
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
        onShowInFolder = { events += "show $it" },
        onCopyPath = { events += "copy $it" },
    )

    private fun ComposeUiTest.showSection(section: LauncherSection) {
        setContent {
            LoreDesignerTheme(darkTheme = false) {
                LauncherContent(
                    section = section,
                    onSectionChange = {},
                    onNewProject = {},
                    onOpenProject = {},
                    projects = projects,
                    projectsLoaded = true,
                    projectActions = actions,
                )
            }
        }
    }

    private suspend fun menuLabels(vararg resources: StringResource) = resources.map { getString(it) }

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
    fun `a single click selects a row without opening it and a double click opens it`() = runComposeUiTest {
        showSection(LauncherSection.Projects)

        val row = onNodeWithText("Embercourt")
        row.assertIsNotSelected()
        row.performMouseInput { click() }
        mainClock.advanceTimeBy(1_000)
        row.assertIsSelected()
        assertEquals(emptyList(), events)

        row.performMouseInput { doubleClick() }
        mainClock.advanceTimeBy(1_000)
        assertEquals(listOf("open ${available.id}"), events)
    }

    @Test
    fun `the pin star still toggles and rows have no remove button`() = runComposeUiTest {
        val unpin = getString(Res.string.launcher_project_unpin)
        val remove = getString(Res.string.launcher_menu_remove)
        showSection(LauncherSection.Pinned)

        onNodeWithContentDescription(unpin).performClick()

        assertEquals(listOf("pin ${available.id} false"), events)
        onNodeWithContentDescription(remove).assertDoesNotExist()
        onNodeWithText(remove).assertDoesNotExist()
    }

    @Test
    fun `available rows offer open, folder, copy, pin and remove in their context menu`() = runComposeUiTest {
        val expected = menuLabels(
            Res.string.launcher_menu_open,
            Res.string.launcher_menu_show_in_folder,
            Res.string.launcher_menu_copy_path,
            Res.string.launcher_menu_unpin,
            Res.string.launcher_menu_remove,
        )
        val missingOnly = menuLabels(Res.string.launcher_menu_locate, Res.string.launcher_menu_copy_previous_path)
        showSection(LauncherSection.Projects)

        onNodeWithText("Embercourt").performMouseInput { rightClick() }

        expected.forEach { onNodeWithText(it).assertExists() }
        missingOnly.forEach { onNodeWithText(it).assertDoesNotExist() }
    }

    @Test
    fun `missing rows offer locate, copy previous path and remove in their context menu`() = runComposeUiTest {
        val expected = menuLabels(
            Res.string.launcher_menu_locate,
            Res.string.launcher_menu_copy_previous_path,
            Res.string.launcher_menu_remove,
        )
        val availableOnly = menuLabels(
            Res.string.launcher_menu_open,
            Res.string.launcher_menu_show_in_folder,
            Res.string.launcher_menu_copy_path,
            Res.string.launcher_menu_pin,
        )
        showSection(LauncherSection.Missing)

        onNodeWithText("/media/usb/Thistlewood").assertExists()
        onNodeWithText("Thistlewood").performMouseInput { rightClick() }

        expected.forEach { onNodeWithText(it).assertExists() }
        availableOnly.forEach { onNodeWithText(it).assertDoesNotExist() }
    }

    @Test
    fun `missing rows keep the inline locate button`() = runComposeUiTest {
        val locate = getString(Res.string.launcher_project_locate)
        showSection(LauncherSection.Missing)

        onNodeWithText(locate).performClick()

        assertEquals(listOf("locate ${missing.id}"), events)
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
