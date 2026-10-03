package dev.cuervolu.loredesigner.workspace.di

import co.touchlab.kermit.Logger
import dev.cuervolu.loredesigner.workspace.CreateWorkspace
import dev.cuervolu.loredesigner.workspace.OpenWorkspace
import dev.cuervolu.loredesigner.workspace.UpdateProjectConfig
import dev.cuervolu.loredesigner.workspace.WorkspaceOpener
import org.koin.core.module.Module
import org.koin.dsl.module

/** Workspace operations; the stores and registry they use are bound by the platform module. */
fun workspaceModule(): Module = module {
    factory { CreateWorkspace(get(), get()) }
    factory { OpenWorkspace(get()) }
    factory { UpdateProjectConfig(get()) }
    factory { WorkspaceOpener(get(), get(), get<Logger>().withTag("Workspaces")) }
}
