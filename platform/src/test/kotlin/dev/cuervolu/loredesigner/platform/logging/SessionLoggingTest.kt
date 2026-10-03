package dev.cuervolu.loredesigner.platform.logging

import co.touchlab.kermit.Severity
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionLoggingTest {
    private val session = LogSession("7f3c2a10-5d1e-4c2b-9a8f-0e1d2c3b4a59", Instant.parse("2026-10-03T17:05:09Z"))

    private val linuxSnapshot = DesktopStartupSnapshot(
        appVersion = "1.4.0",
        runningOn = "Arch Linux rolling",
        kernelVersion = "7.2.8-arch1-2",
        executableType = "DEV",
        desktopEnvironment = "KDE",
        displayServer = "wayland",
        timeZone = "America/Santiago",
    )

    @Test
    fun `session start is a header followed by an aligned metadata block`() {
        assertEquals(
            """
            ===== Session 7f3c2a10-5d1e-4c2b-9a8f-0e1d2c3b4a59 started =====
            version         1.4.0
            os              Arch Linux rolling
            kernel          7.2.8-arch1-2
            executable      DEV
            desktop         KDE
            display server  wayland
            time zone       America/Santiago
            """.trimIndent(),
            sessionStartMessage(session, linuxSnapshot),
        )
    }

    @Test
    fun `platform specific fields are left out when they do not apply`() {
        val windows = linuxSnapshot.copy(runningOn = "Windows 11", desktopEnvironment = null, displayServer = null)

        assertEquals(
            """
            ===== Session 7f3c2a10-5d1e-4c2b-9a8f-0e1d2c3b4a59 started =====
            version     1.4.0
            os          Windows 11
            kernel      7.2.8-arch1-2
            executable  DEV
            time zone   America/Santiago
            """.trimIndent(),
            sessionStartMessage(session, windows),
        )
    }

    @Test
    fun `session end names the session and how long it ran`() {
        val logs = RecordingLogWriter()

        logs.logger().logSessionEnd(session, endedAt = Instant.parse("2026-10-03T18:07:14Z"))

        val entry = logs.entries.single()
        assertEquals(Severity.Info, entry.severity)
        assertEquals(APP_TAG, entry.tag)
        assertEquals("===== Session ${session.id} ended after 1h 02m 05s =====", entry.message)
    }
}
