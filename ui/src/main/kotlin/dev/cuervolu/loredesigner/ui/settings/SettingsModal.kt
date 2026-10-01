package dev.cuervolu.loredesigner.ui.settings

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.X
import com.composeunstyled.DialogPanel
import com.composeunstyled.Scrim
import com.composeunstyled.Text
import com.composeunstyled.UnstyledDialog
import com.composeunstyled.theme.Theme
import dev.cuervolu.loredesigner.ui.components.LoreIconButton
import dev.cuervolu.loredesigner.ui.components.LoreNavItem
import dev.cuervolu.loredesigner.ui.components.LoreTextField
import dev.cuervolu.loredesigner.ui.resources.Res
import dev.cuervolu.loredesigner.ui.resources.settings_close
import dev.cuervolu.loredesigner.ui.resources.settings_group_application
import dev.cuervolu.loredesigner.ui.resources.settings_group_extensions
import dev.cuervolu.loredesigner.ui.resources.settings_group_project
import dev.cuervolu.loredesigner.ui.resources.settings_path_separator
import dev.cuervolu.loredesigner.ui.resources.settings_search_no_results
import dev.cuervolu.loredesigner.ui.resources.settings_search_placeholder
import dev.cuervolu.loredesigner.ui.resources.settings_search_results
import dev.cuervolu.loredesigner.ui.resources.settings_title
import dev.cuervolu.loredesigner.ui.theme.DURATION_BASE_MILLIS
import dev.cuervolu.loredesigner.ui.theme.LoreColors
import dev.cuervolu.loredesigner.ui.theme.LoreShadows
import dev.cuervolu.loredesigner.ui.theme.LoreShapes
import dev.cuervolu.loredesigner.ui.theme.LoreTypography
import dev.cuervolu.loredesigner.ui.theme.StandardEasing
import dev.cuervolu.loredesigner.ui.theme.colors
import dev.cuervolu.loredesigner.ui.theme.dotToken
import dev.cuervolu.loredesigner.ui.theme.shadows
import dev.cuervolu.loredesigner.ui.theme.shapes
import dev.cuervolu.loredesigner.ui.theme.typography
import dev.cuervolu.loredesigner.workspace.Workspace
import org.jetbrains.compose.resources.stringResource

private val ModalWidth = 880.dp
private val ModalHeight = 600.dp
private val SidebarWidth = 216.dp
private val HeaderHeight = 44.dp
private val ContentMaxWidth = 580.dp
private const val ENTER_SCALE = 0.98f

/** Creates the state holder for one Settings context; the workspace is `null` in the launcher. */
fun interface SettingsViewModelFactory {
    fun create(workspace: Workspace?): SettingsViewModel
}

/**
 * The single Settings modal. Pages come from the registry behind [viewModel]: application pages are
 * always listed, project pages only when a workspace is open. Must be composed under a `DialogHost`.
 */
@Composable
fun SettingsModal(
    visible: Boolean,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit,
    onWorkspaceChange: (Workspace) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnWorkspaceChange by rememberUpdatedState(onWorkspaceChange)
    LaunchedEffect(state.workspace) { state.workspace?.let { currentOnWorkspaceChange(it) } }

    val motion = tween<Float>(DURATION_BASE_MILLIS, easing = StandardEasing)
    val shape = Theme[shapes][LoreShapes.modal]
    val modalShadows = Theme[shadows][LoreShadows.modal]
    val title = stringResource(Res.string.settings_title)

    UnstyledDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        overlay = {
            Scrim(scrimColor = Theme[colors][LoreColors.overlayScrim], enter = fadeIn(motion), exit = fadeOut(motion))
        },
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            DialogPanel(
                paneTitle = title,
                enter = fadeIn(motion) + scaleIn(motion, initialScale = ENTER_SCALE),
                exit = fadeOut(motion) + scaleOut(motion, targetScale = ENTER_SCALE),
                modifier = Modifier
                    .widthIn(max = ModalWidth)
                    .fillMaxWidth()
                    .heightIn(max = ModalHeight)
                    .fillMaxHeight()
                    .let { base -> modalShadows.fold(base) { acc, shadow -> acc.dropShadow(shape, shadow) } }
                    .clip(shape)
                    .background(Theme[colors][LoreColors.surfaceEditor])
                    .border(1.dp, Theme[colors][LoreColors.border], shape),
            ) {
                Column(Modifier.fillMaxSize()) {
                    SettingsHeader(title = title, onClose = onDismiss)
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        SettingsBody(state, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(title: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderHeight)
            .background(Theme[colors][LoreColors.surface])
            .bottomBorder(Theme[colors][LoreColors.border])
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = Theme[typography][LoreTypography.bodyStrong].copy(fontSize = 14.sp),
            color = Theme[colors][LoreColors.textPrimary],
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        LoreIconButton(
            icon = Lucide.X,
            contentDescription = stringResource(Res.string.settings_close),
            onClick = onClose,
        )
    }
}

@Composable
private fun RowScope.SettingsBody(state: SettingsUiState, viewModel: SettingsViewModel) {
    val pages = viewModel.availablePages
    val resolve = rememberTextResolver(pages)
    val query = viewModel.query.text.toString()
    val searching = query.isNotBlank()
    val results = remember(pages, query, resolve) { searchSettings(pages, query, resolve) }
    val selected = pages.firstOrNull { it.id == state.selectedPageId } ?: pages.first()

    SettingsSidebar(
        pages = pages,
        selectedId = if (searching) null else selected.id,
        workspace = state.workspace,
        resolve = resolve,
        viewModel = viewModel,
    )
    Box(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
        Column(
            Modifier.widthIn(max = ContentMaxWidth).padding(start = 28.dp, end = 28.dp, top = 28.dp, bottom = 32.dp),
        ) {
            Text(
                text = if (searching) {
                    stringResource(
                        Res.string.settings_search_results,
                        query.trim(),
                    )
                } else {
                    resolve(selected.title)
                },
                style = Theme[typography][LoreTypography.headingLarge],
                color = Theme[colors][LoreColors.textPrimary],
                modifier = Modifier.padding(bottom = 10.dp).semantics { heading() },
            )
            if (searching) {
                SearchResults(results, state.workspace, onOpen = viewModel::selectPage)
            } else {
                selected.Content(SettingsPageContext(state, viewModel))
            }
        }
    }
}

@Composable
private fun SettingsSidebar(
    pages: List<SettingsPage>,
    selectedId: String?,
    workspace: Workspace?,
    resolve: (SettingsText) -> String,
    viewModel: SettingsViewModel,
) {
    val searchLabel = stringResource(Res.string.settings_search_placeholder)
    Column(
        modifier = Modifier
            .width(SidebarWidth)
            .fillMaxHeight()
            .background(Theme[colors][LoreColors.surface])
            .endBorder(Theme[colors][LoreColors.border]),
    ) {
        LoreTextField(
            state = viewModel.query,
            accessibilityLabel = searchLabel,
            placeholder = searchLabel,
            leadingIcon = Lucide.Search,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 6.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 8.dp, end = 8.dp, bottom = 12.dp),
        ) {
            pages.groupBy { it.group }.forEach { (group, groupPages) ->
                when (group) {
                    SettingsGroup.Application -> SidebarGroupLabel(
                        stringResource(Res.string.settings_group_application),
                    )

                    SettingsGroup.Project -> ProjectGroupLabel(workspace)

                    SettingsGroup.Extensions -> SidebarGroupLabel(
                        stringResource(Res.string.settings_group_extensions),
                        separated = true,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    groupPages.forEach { page ->
                        LoreNavItem(
                            label = resolve(page.title),
                            selected = page.id == selectedId,
                            onClick = { viewModel.selectPage(page.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarGroupLabel(text: String, separated: Boolean = false) {
    Box(
        Modifier
            .fillMaxWidth()
            .then(
                if (separated) {
                    Modifier.padding(
                        top = 10.dp,
                    ).topBorder(Theme[colors][LoreColors.borderSubtle])
                } else {
                    Modifier
                },
            )
            .padding(start = 8.dp, end = 8.dp, top = if (separated) 10.dp else 8.dp, bottom = 4.dp),
    ) {
        SettingsEyebrow(text)
    }
}

@Composable
private fun ProjectGroupLabel(workspace: Workspace?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .topBorder(Theme[colors][LoreColors.borderSubtle])
            .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        SettingsEyebrow(stringResource(Res.string.settings_group_project))
        if (workspace != null) ProjectIdentity(workspace)
    }
}

@Composable
private fun ProjectIdentity(workspace: Workspace) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(7.dp)
                .background(Theme[colors][workspace.config.color?.dotToken() ?: LoreColors.textMuted], CircleShape),
        )
        Text(
            text = workspace.config.name.uppercase(),
            style = Theme[typography][LoreTypography.eyebrow].copy(fontWeight = FontWeight.SemiBold),
            color = Theme[colors][LoreColors.textSecondary],
            singleLine = true,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SearchResults(groups: List<SettingsSearchGroup>, workspace: Workspace?, onOpen: (String) -> Unit) {
    if (groups.isEmpty()) {
        Text(
            text = stringResource(Res.string.settings_search_no_results),
            style = Theme[typography][LoreTypography.body],
            color = Theme[colors][LoreColors.textMuted],
            modifier = Modifier.padding(vertical = 10.dp),
        )
        return
    }
    groups.forEachIndexed { index, group ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (index == 0) 4.dp else 20.dp)
                .settingsDivider()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsEyebrow(
                stringResource(
                    when (group.group) {
                        SettingsGroup.Application -> Res.string.settings_group_application
                        SettingsGroup.Project -> Res.string.settings_group_project
                        SettingsGroup.Extensions -> Res.string.settings_group_extensions
                    },
                ),
            )
            if (group.group == SettingsGroup.Project && workspace != null) ProjectIdentity(workspace)
        }
        Column(Modifier.padding(top = 4.dp)) {
            group.results.forEach { result -> SearchResultRow(result, onClick = { onOpen(result.pageId) }) }
        }
    }
}

@Composable
private fun SearchResultRow(result: SettingsSearchResult, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val highlight = Theme[colors][LoreColors.accentSubtle]
    val label = remember(result, highlight) { result.highlightedLabel(highlight) }
    val path = if (result.subsection != null) {
        stringResource(Res.string.settings_path_separator, result.pageTitle, result.subsection)
    } else {
        result.pageTitle
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (hovered) Theme[colors][LoreColors.surface] else Color.Transparent,
                Theme[shapes][LoreShapes.control],
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = Theme[typography][LoreTypography.body],
                color = Theme[colors][LoreColors.textPrimary],
            )
            Text(
                text = path,
                style = Theme[typography][LoreTypography.caption],
                color = Theme[colors][LoreColors.textMuted],
                modifier = Modifier.padding(top = 1.dp),
            )
        }
        Image(
            imageVector = Lucide.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            colorFilter = ColorFilter.tint(Theme[colors][LoreColors.textMuted]),
        )
    }
}

private fun SettingsSearchResult.highlightedLabel(highlight: Color): AnnotatedString = buildAnnotatedString {
    if (matchStart < 0) {
        append(label)
        return@buildAnnotatedString
    }
    append(label.substring(0, matchStart))
    withStyle(SpanStyle(background = highlight)) { append(label.substring(matchStart, matchStart + matchLength)) }
    append(label.substring(matchStart + matchLength))
}

/** Resolves every text the pages expose so search can run outside composition. */
@Composable
private fun rememberTextResolver(pages: List<SettingsPage>): (SettingsText) -> String {
    val texts = remember(pages) {
        pages.flatMap { page ->
            listOfNotNull(page.title, page.contributor) +
                page.searchEntries.flatMap { listOfNotNull(it.label, it.keywords, it.subsection) }
        }.distinct()
    }
    val resolved = texts.associateWith { it.resolve() }
    return remember(resolved) { { text -> resolved[text].orEmpty() } }
}

private fun Modifier.bottomBorder(color: Color): Modifier = drawBehind {
    val y = size.height - 0.5.dp.toPx()
    drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
}

private fun Modifier.topBorder(color: Color): Modifier = drawBehind {
    drawLine(color, Offset(0f, 0.5.dp.toPx()), Offset(size.width, 0.5.dp.toPx()), strokeWidth = 1.dp.toPx())
}

private fun Modifier.endBorder(color: Color): Modifier = drawBehind {
    val x = size.width - 0.5.dp.toPx()
    drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
}
