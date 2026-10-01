package dev.cuervolu.loredesigner.ui.settings

/** A search hit; [matchStart]/[matchLength] locate the query inside [label] (-1 when only keywords matched). */
data class SettingsSearchResult(
    val pageId: String,
    val label: String,
    val pageTitle: String,
    val subsection: String?,
    val matchStart: Int,
    val matchLength: Int,
)

data class SettingsSearchGroup(val group: SettingsGroup, val results: List<SettingsSearchResult>)

/**
 * Case-insensitive substring search over [pages], which must already be limited to the pages
 * available in the current context. Text is resolved through [resolve] so this stays free of Compose.
 */
fun searchSettings(
    pages: List<SettingsPage>,
    query: String,
    resolve: (SettingsText) -> String,
): List<SettingsSearchGroup> {
    val needle = query.trim().lowercase()
    if (needle.isEmpty()) return emptyList()

    val results = pages.flatMap { page ->
        val pageTitle = resolve(page.title)
        val contributor = page.contributor?.let(resolve).orEmpty()
        page.searchEntries.mapNotNull { entry ->
            val label = resolve(entry.label)
            val subsection = entry.subsection?.let(resolve)
            val haystack = listOf(
                label,
                entry.keywords?.let(resolve).orEmpty(),
                subsection.orEmpty(),
                pageTitle,
                contributor,
            )
                .joinToString(" ")
                .lowercase()
            if (needle !in haystack) return@mapNotNull null
            val start = label.lowercase().indexOf(needle)
            page.group to SettingsSearchResult(
                pageId = page.id,
                label = label,
                pageTitle = pageTitle,
                subsection = subsection,
                matchStart = start,
                matchLength = if (start >= 0) needle.length else 0,
            )
        }
    }
    return results
        .groupBy({ it.first }, { it.second })
        .toSortedMap()
        .map { (group, hits) -> SettingsSearchGroup(group, hits) }
}
