package dev.cuervolu.loredesigner.di

import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.OpenWorkspace
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertNotNull

class AppModuleTest {
    @Test
    fun `workspace use cases are available from dependency injection`() {
        val logsDirectory = createTempDirectory("app-module-test")
        try {
            val koin = startKoin { modules(loreDesignerModules(logsDirectory)) }.koin

            assertNotNull(koin.get<CreateWorkspace>())
            assertNotNull(koin.get<OpenWorkspace>())
        } finally {
            stopKoin()
            logsDirectory.toFile().deleteRecursively()
        }
    }
}
