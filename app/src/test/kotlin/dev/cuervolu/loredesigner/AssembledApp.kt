package dev.cuervolu.loredesigner

import dev.cuervolu.loredesigner.di.loreDesignerModules
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

/**
 * The production Koin graph, started the way `main` starts it, with all storage redirected to a
 * temporary directory so nothing touches the user's real data.
 */
internal class AssembledApp private constructor(val appDataDirectory: Path, val koin: Koin) : AutoCloseable {
    override fun close() {
        stopKoin()
    }

    companion object {
        fun start(appDataDirectory: Path): AssembledApp =
            AssembledApp(appDataDirectory, startKoin { modules(loreDesignerModules(appDataDirectory)) }.koin)
    }
}

/** A scratch directory that is deleted with [close]; holds both app data and project folders. */
internal class ScratchDirectory : AutoCloseable {
    val root: Path = createTempDirectory("lore-designer-app-test")

    override fun close() {
        root.toFile().deleteRecursively()
    }
}
