package dev.cuervolu.loredesigner.ui.launcher

class FakeProjectSystemActions : ProjectSystemActions {
    var showInFolderResult = true
    var copyResult = true
    val shownLocations = mutableListOf<String>()
    val copiedTexts = mutableListOf<String>()

    override suspend fun showInFolder(projectLocation: String): Boolean {
        shownLocations += projectLocation
        return showInFolderResult
    }

    override suspend fun copyText(text: String): Boolean {
        copiedTexts += text
        return copyResult
    }
}
