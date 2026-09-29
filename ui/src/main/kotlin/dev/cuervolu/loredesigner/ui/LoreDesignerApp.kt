package dev.cuervolu.loredesigner.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
public fun LoreDesignerApp() {
    Box(modifier = Modifier.fillMaxSize()) {
        BasicText(text = "Lore Designer")
    }
}
