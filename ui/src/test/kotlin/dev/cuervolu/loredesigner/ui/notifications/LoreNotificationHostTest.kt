package dev.cuervolu.loredesigner.ui.notifications

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.notification_dismiss
import dev.cuervolu.loredesigner.ui.theme.LoreDesignerTheme
import org.jetbrains.compose.resources.getString
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LoreNotificationHostTest {
    private lateinit var notifier: LoreNotifier

    private fun ComposeUiTest.showHost() {
        setContent {
            LoreDesignerTheme(darkTheme = false) {
                LoreNotificationHost {
                    notifier = LocalLoreNotifier.current
                }
            }
        }
    }

    @Test
    fun `each type shows its message with its own icon`() = runComposeUiTest {
        showHost()

        LoreNotificationType.entries.forEach { type ->
            runOnIdle { notifier.show("Message ${type.name}", type) }
            onNodeWithText("Message ${type.name}").assertExists()
            onNodeWithTag(notificationIconTag(type), useUnmergedTree = true).assertExists()
        }
    }

    @Test
    fun `the close button dismisses the notification`() = runComposeUiTest {
        val dismiss = getString(Res.string.notification_dismiss)
        showHost()

        runOnIdle { notifier.show("Path copied", LoreNotificationType.Success) }
        onNodeWithContentDescription(dismiss).performClick()
        mainClock.advanceTimeBy(2_000)

        onNodeWithText("Path copied").assertDoesNotExist()
    }

    @Test
    fun `notifications dismiss themselves after their duration`() = runComposeUiTest {
        showHost()

        runOnIdle { notifier.show("Saved", LoreNotificationType.Info) }
        onNodeWithText("Saved").assertExists()
        mainClock.advanceTimeBy(10_000)

        onNodeWithText("Saved").assertDoesNotExist()
    }

    @Test
    fun `dismissing all clears every notification`() = runComposeUiTest {
        showHost()

        runOnIdle {
            notifier.show("First", LoreNotificationType.Error)
            notifier.show("Second", LoreNotificationType.Warning)
        }
        onNodeWithText("Second").assertExists()
        runOnIdle { notifier.dismissAll() }
        mainClock.advanceTimeBy(2_000)

        onNodeWithText("First").assertDoesNotExist()
        onNodeWithText("Second").assertDoesNotExist()
    }
}
