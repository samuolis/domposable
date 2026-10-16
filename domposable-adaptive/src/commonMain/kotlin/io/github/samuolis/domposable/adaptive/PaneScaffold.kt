package io.github.samuolis.domposable.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableVisibility
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.region
import io.github.samuolis.domposable.ui.weight

/**
 * Renders caller-owned entries in one or two panes, without owning navigation or ViewModels.
 * [retainedList] keeps a list composed when a compact detail temporarily hides it.
 * Pane modifiers configure brand spacing and separators; no HTML or native layout code is needed.
 * Native expanded panes fill their bounded parent height; HTML panes stretch to the taller pane's content.
 */
@Composable
@Suppress("LongParameterList")
fun <T : Any> DomposablePaneScaffold(
    layout: DomposablePaneLayout<T>,
    listLabel: String,
    detailLabel: String,
    modifier: DomposableModifier = DomposableModifier,
    listModifier: DomposableModifier = DomposableModifier,
    detailModifier: DomposableModifier = DomposableModifier,
    listFraction: Float = 0.33f,
    minListWidth: DomposableLength = 0.dp,
    maxListWidth: DomposableLength? = null,
    gap: DomposableLength = 0.dp,
    retainedList: T? = null,
    singlePaneRole: DomposablePaneRole = DomposablePaneRole.Detail,
    contentKey: (T) -> Any = { it },
    content: @Composable (T) -> Unit,
) {
    require(listLabel.isNotBlank() && detailLabel.isNotBlank()) { "Panes require accessible labels" }
    require(listFraction.isFinite() && listFraction > 0f && listFraction < 1f) {
        "The list fraction must leave room for both panes"
    }
    require(maxListWidth == null || minListWidth.value <= maxListWidth.value)
    val expanded = layout is DomposablePaneLayout.ListDetail
    val list =
        when (layout) {
            is DomposablePaneLayout.ListDetail -> {
                layout.list
            }

            is DomposablePaneLayout.Single -> {
                if (singlePaneRole ==
                    DomposablePaneRole.List
                ) {
                    layout.entry
                } else {
                    retainedList
                }
            }
        }
    val detail =
        when (layout) {
            is DomposablePaneLayout.ListDetail -> {
                layout.detail
            }

            is DomposablePaneLayout.Single -> {
                layout.entry.takeIf { singlePaneRole == DomposablePaneRole.Detail }
            }
        }
    DomposableRow(
        modifier = modifier.fillMaxWidth(),
        gap = if (expanded) gap else 0.dp,
        stretch = expanded,
    ) {
        list?.let { entry ->
            key(contentKey(entry)) {
                val width =
                    if (expanded) {
                        DomposableModifier.fillMaxWidth(listFraction, minListWidth, maxListWidth)
                    } else {
                        DomposableModifier.fillMaxWidth()
                    }
                DomposableVisibility(
                    visible = expanded || singlePaneRole == DomposablePaneRole.List,
                    modifier =
                        width
                            .then(if (expanded) DomposableModifier.fillMaxHeight() else DomposableModifier)
                            .region(listLabel)
                            .then(listModifier),
                ) {
                    CompositionLocalProvider(
                        LocalDomposablePaneRole provides DomposablePaneRole.List.takeIf { expanded },
                    ) {
                        content(entry)
                    }
                }
            }
        }
        detail?.let { entry ->
            key(contentKey(entry)) {
                val width = if (expanded) DomposableModifier.weight(1f) else DomposableModifier.fillMaxWidth()
                val height = if (expanded) DomposableModifier.fillMaxHeight() else DomposableModifier
                DomposableVisibility(
                    visible = true,
                    modifier = width.then(height).region(detailLabel).then(detailModifier),
                ) {
                    CompositionLocalProvider(
                        LocalDomposablePaneRole provides DomposablePaneRole.Detail.takeIf { expanded },
                    ) {
                        content(entry)
                    }
                }
            }
        }
    }
}
