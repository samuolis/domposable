package io.github.samuolis.domposable.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavEntry
import io.github.samuolis.domposable.screens.DomposableEntryScope
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableModifierOperation
import io.github.samuolis.domposable.ui.domposableAttributes
import io.github.samuolis.domposable.ui.weight
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Section

@Composable
actual fun <T : Any> DomposableNavDisplay(
    entries: List<NavEntry<T>>,
    modifier: DomposableModifier,
    visibleEntries: Int,
    onBack: () -> Unit,
) {
    require(entries.isNotEmpty()) { "Navigation requires at least one entry" }
    require(visibleEntries > 0)
    require(entries.map { it.contentKey }.distinct().size == entries.size) { "Navigation content keys must be unique" }
    val hasExplicitHeight =
        modifier.elements.any {
            it is DomposableModifierOperation.Height ||
                it is DomposableModifierOperation.ViewportHeight ||
                it is DomposableModifierOperation.MaxHeight
        }
    val hasWeight = modifier.elements.any { it is DomposableModifierOperation.Weight }
    val displayModifier =
        if (hasExplicitHeight || hasWeight) modifier else DomposableModifier.weight(1f).then(modifier)
    DomposableHtmlModifiers(displayModifier) {
        Div(attrs = {
            style {
                property("display", "flex")
                property("height", "100%")
                property("min-height", "0")
                property("min-width", "0")
            }
            domposableAttributes(modifier)
        }) {
            entries.takeLast(visibleEntries).forEach { entry ->
                key(entry.contentKey) {
                    Section(attrs = {
                        attr("data-entry-id", entry.contentKey.toString())
                        style {
                            property("display", "flex")
                            property("flex-direction", "column")
                            property("flex", "1")
                            property("min-width", "0")
                            property("min-height", "0")
                            property("overflow", "visible")
                        }
                    }) { entry.Content() }
                }
            }
        }
    }
}

@Composable
actual fun <R : Any> DomposableBrowserHistory(
    backStack: SnapshotStateList<DomposableEntryScope<R>>,
    codec: DomposableRouteCodec<R>,
    urls: DomposableBrowserUrls,
) = BrowserHistoryBinding(backStack, codec, urls)
