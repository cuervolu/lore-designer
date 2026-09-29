package dev.cuervolu.loredesigner.platform.logging

import dev.nucleusframework.core.runtime.ExecutableRuntime
import dev.nucleusframework.core.runtime.LinuxDesktopEnvironment
import dev.nucleusframework.core.runtime.NucleusApp
import dev.nucleusframework.core.runtime.Platform
import dev.nucleusframework.systeminfo.SystemInfo
import java.time.Instant

/** Desktop/runtime metadata recorded once per launch alongside a [LogSession]. */
data class DesktopStartupSnapshot(
    val appVersion: String,
    val startedAt: String,
    val runningOn: String,
    val kernelVersion: String,
    val executableType: String,
    val desktopEnvironment: String? = null,
    val displayServer: String? = null,
) {
    companion object {
        // SystemInfo.osInfo() can fail depending on platform/permissions; fall back rather than
        // let a startup-metadata failure take down the application.
        fun capture(startedAt: Instant): DesktopStartupSnapshot {
            val osInfo = runCatching { SystemInfo.osInfo() }.getOrNull()
            val platform = Platform.Current

            return DesktopStartupSnapshot(
                appVersion = NucleusApp.version ?: ExecutableRuntime.markerVersion() ?: "unknown",
                startedAt = startedAt.toString(),
                runningOn = osInfo?.name?.let { name ->
                    osInfo.osVersion?.let { version -> "$name $version" } ?: name
                } ?: platform.name,
                kernelVersion = osInfo?.kernelVersion ?: "unknown",
                executableType = ExecutableRuntime.type().name,
                desktopEnvironment = if (platform == Platform.Linux) {
                    LinuxDesktopEnvironment.Current.name
                } else {
                    null
                },
                displayServer = if (platform == Platform.Linux) {
                    if (Platform.isWayland) "wayland" else "x11"
                } else {
                    null
                },
            )
        }
    }
}
