package dev.cuervolu.loredesigner

import androidx.compose.runtime.Composable
import dev.cuervolu.loredesigner.ui.LoreDesignerApp
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.NucleusApplicationScope

@Composable
public fun NucleusApplicationScope.LoreDesignerDesktopApp() {
    DecoratedWindow(
        onCloseRequest = ::exitApplication,
        title = "Lore Designer",
    ) {
        LoreDesignerApp()
    }
}
