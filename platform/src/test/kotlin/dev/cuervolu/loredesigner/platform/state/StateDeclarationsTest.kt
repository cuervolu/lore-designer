package dev.cuervolu.loredesigner.platform.state

import dev.cuervolu.loredesigner.platform.workspace.PersistentRecentWorkspacesRegistry
import kotlin.test.Test

/**
 * Validates every production [State] declaration together, so a malformed or conflicting declaration
 * fails here instead of the first time the component is loaded. Add new state components to the list.
 */
class StateDeclarationsTest {
    private val productionComponents: List<Class<*>> = listOf(
        PersistentRecentWorkspacesRegistry.PersistedRecentWorkspaces::class.java,
    )

    @Test
    fun `production state declarations are valid and consistent`() {
        productionComponents.fold(emptyList<ComponentSpec>()) { known, componentClass ->
            val spec = resolveStateMetadata(componentClass)
            checkStateDeclaration(spec, known)
            known + spec
        }
    }
}
