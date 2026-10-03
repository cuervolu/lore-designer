package dev.cuervolu.loredesigner.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.CircleX
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.X
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToastWidthPolicy
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterDefaults
import com.dokar.sonner.ToasterState
import com.dokar.sonner.rememberToasterState
import dev.cuervolu.loredesigner.ui.components.LoreIconButton
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.notification_dismiss
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreSpacing
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.spacing
import dev.cuervolu.loredesigner.ui.theme.typography
import org.jetbrains.compose.resources.stringResource

enum class LoreNotificationType {
    Info,
    Success,
    Warning,
    Error,
}

/** Shows transient notifications in the nearest [LoreNotificationHost]. */
@Stable
class LoreNotifier internal constructor(private val toaster: ToasterState) {
    fun show(message: String, type: LoreNotificationType = LoreNotificationType.Info) {
        val duration = when (type) {
            // Problems stay up longer: they usually ask the reader to do something.
            LoreNotificationType.Warning, LoreNotificationType.Error -> ToasterDefaults.DurationLong

            LoreNotificationType.Info, LoreNotificationType.Success -> ToasterDefaults.DurationDefault
        }
        toaster.show(message = message, type = type.toToastType(), icon = type, duration = duration)
    }

    fun dismissAll() = toaster.dismissAll()

    internal fun dismiss(id: Any) = toaster.dismiss(id)
}

val LocalLoreNotifier =
    staticCompositionLocalOf<LoreNotifier> { error("No LoreNotificationHost above this composable") }

/** Provides [LocalLoreNotifier] to [content] and draws its notifications over it. */
@Composable
fun LoreNotificationHost(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val toaster = rememberToasterState()
    val notifier = remember(toaster) { LoreNotifier(toaster) }
    CompositionLocalProvider(LocalLoreNotifier provides notifier) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
            LoreToaster(toaster = toaster, notifier = notifier)
        }
    }
}

@Composable
private fun LoreToaster(toaster: ToasterState, notifier: LoreNotifier) {
    val surface = Theme[colors][LoreColors.surfaceRaised]
    val border = BorderStroke(1.dp, Theme[colors][LoreColors.borderSubtle])
    val textColor = Theme[colors][LoreColors.textPrimary]
    val shape = Theme[shapes][LoreShapes.popover]
    val dismissLabel = stringResource(Res.string.notification_dismiss)

    Toaster(
        state = toaster,
        darkTheme = Theme[colors][LoreColors.background].luminance() < 0.5f,
        swipeable = false,
        contentColor = { textColor },
        border = { border },
        background = { SolidColor(surface) },
        shape = { shape },
        containerPadding = PaddingValues(Theme[spacing][LoreSpacing.space6]),
        contentPadding = { PaddingValues(start = 12.dp, top = 8.dp, end = 6.dp, bottom = 8.dp) },
        widthPolicy = { ToastWidthPolicy(max = 380.dp) },
        alignment = Alignment.BottomEnd,
        iconSlot = { toast -> NotificationIcon(toast.notificationType()) },
        messageSlot = { toast ->
            Text(
                text = toast.message.toString(),
                style = Theme[typography][LoreTypography.body],
                color = textColor,
                modifier = Modifier
                    .padding(start = Theme[spacing][LoreSpacing.space4], end = Theme[spacing][LoreSpacing.space3])
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        },
        actionSlot = { toast ->
            LoreIconButton(
                icon = Lucide.X,
                contentDescription = dismissLabel,
                onClick = { notifier.dismiss(toast.id) },
            )
        },
    )
}

@Composable
private fun NotificationIcon(type: LoreNotificationType) {
    val (icon, tint) = when (type) {
        LoreNotificationType.Info -> Lucide.Info to LoreColors.accent
        LoreNotificationType.Success -> Lucide.CircleCheck to LoreColors.success
        LoreNotificationType.Warning -> Lucide.TriangleAlert to LoreColors.warning
        LoreNotificationType.Error -> Lucide.CircleX to LoreColors.danger
    }
    Image(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(16.dp).testTag(notificationIconTag(type)),
        colorFilter = ColorFilter.tint(Theme[colors][tint]),
    )
}

internal fun notificationIconTag(type: LoreNotificationType) = "notification-icon-${type.name.lowercase()}"

// The type travels as the toast's icon so slots read Lore's type rather than Sonner's.
private fun Toast.notificationType(): LoreNotificationType = icon as? LoreNotificationType ?: LoreNotificationType.Info

private fun LoreNotificationType.toToastType() = when (this) {
    LoreNotificationType.Info -> ToastType.Info
    LoreNotificationType.Success -> ToastType.Success
    LoreNotificationType.Warning -> ToastType.Warning
    LoreNotificationType.Error -> ToastType.Error
}
