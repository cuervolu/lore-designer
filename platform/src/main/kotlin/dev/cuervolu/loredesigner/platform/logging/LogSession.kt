package dev.cuervolu.loredesigner.platform.logging

import java.time.Instant
import java.util.UUID

/** One instance per application launch; multiple sessions may share the same active log file. */
data class LogSession(val id: String, val startedAt: Instant) {
    companion object {
        fun start(): LogSession = LogSession(id = UUID.randomUUID().toString(), startedAt = Instant.now())
    }
}
