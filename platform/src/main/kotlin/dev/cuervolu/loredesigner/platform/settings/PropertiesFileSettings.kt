package dev.cuervolu.loredesigner.platform.settings

import co.touchlab.kermit.Logger
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.buffer
import java.util.Properties
import kotlin.random.Random

/**
 * File-backed [Settings] built on Multiplatform Settings' [PropertiesSettings].
 *
 * `PropertiesSettings` only mutates an in-memory [Properties]; every change is written back here,
 * through a temporary file and an atomic move so a crash never leaves a truncated settings file.
 * The file is not created until the first write.
 */
object PropertiesFileSettings {
    fun create(
        file: Path,
        fileSystem: FileSystem = FileSystem.SYSTEM,
        logger: Logger = Logger.withTag("Settings"),
    ): Settings {
        val properties = load(file, fileSystem, logger)
        return PropertiesSettings(properties) { persist(file, fileSystem, it, logger) }
    }

    private fun load(file: Path, fileSystem: FileSystem, logger: Logger): Properties {
        val properties = Properties()
        try {
            if (fileSystem.exists(file)) {
                fileSystem.source(file).buffer().use { properties.load(it.inputStream()) }
            }
        } catch (exception: IOException) {
            logger.w(exception) { "Could not read settings file; using defaults" }
            properties.clear()
        } catch (exception: IllegalArgumentException) {
            // Properties.load throws this for malformed unicode escapes.
            logger.w(exception) { "Settings file is malformed; using defaults" }
            properties.clear()
        }
        return properties
    }

    private fun persist(file: Path, fileSystem: FileSystem, properties: Properties, logger: Logger) {
        val directory = requireNotNull(file.parent) { "Settings file must have a parent directory" }
        val temporaryFile = directory / ".${file.name}.${Random.nextLong().toULong().toString(16)}.tmp"
        try {
            fileSystem.createDirectories(directory)
            fileSystem.write(temporaryFile, mustCreate = true) { properties.store(outputStream(), null) }
            fileSystem.atomicMove(temporaryFile, file)
        } catch (exception: IOException) {
            logger.e(exception) { "Could not write settings file" }
            try {
                fileSystem.delete(temporaryFile, mustExist = false)
            } catch (cleanupFailure: IOException) {
                exception.addSuppressed(cleanupFailure)
            }
        }
    }
}
