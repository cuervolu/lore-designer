package dev.cuervolu.loredesigner.platform.logging

import co.touchlab.kermit.Logger

/**
 * Writes failures nobody caught to the application log before handing them to [delegate].
 *
 * Coroutines without a `CoroutineExceptionHandler` also end up here: kotlinx.coroutines passes them
 * to the failing thread's uncaught exception handler.
 */
class LoggingUncaughtExceptionHandler(
    private val logger: Logger,
    private val delegate: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        logger.e(throwable) { "Uncaught exception on thread '${thread.name}'" }
        delegate?.uncaughtException(thread, throwable)
    }
}

/** Installs [LoggingUncaughtExceptionHandler] as the JVM-wide default, keeping any previous default. */
fun installUncaughtExceptionLogging(logger: Logger) {
    val previous = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler(LoggingUncaughtExceptionHandler(logger, previous))
}
