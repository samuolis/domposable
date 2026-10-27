package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableLazyColumnProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.domposableAttributes
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Ul

@Composable
internal fun HtmlMeasuredWindow(
    props: DomposableLazyColumnProps,
    modifier: DomposableModifier,
) {
    val window = requireNotNull(props.window)
    val keys = remember(props.items) { props.items.map { it.key } }
    val state = rememberHtmlWindowState(keys)
    val known = remember { mutableMapOf<String, Double>() }
    DisposableEffect(keys) {
        known.keys.retainAll(keys.toSet())
        onDispose { }
    }
    val rows =
        remember(keys, window.rowHeight, props.gap) {
            MeasuredRows(keys, window.rowHeight.value.toDouble(), props.gap.value.toDouble(), known)
        }
    var revision by remember { mutableIntStateOf(0) }
    val extent = remember(rows, revision) { rows.extent }
    val range =
        remember(rows, revision, state.offset, window) {
            rows.range(state.offset, window.viewportHeight.value.toDouble(), window.overscan)
        }
    val indices = state.indices(range)
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            attr("data-domposable-window", "measured-column")
            style {
                property("height", "${window.viewportHeight.value}px")
                property("overflow-y", "auto")
                property("overflow-anchor", "none")
            }
            ref { element ->
                val detach = state.attach(element, false)
                onDispose { detach() }
            }
        }) {
            Ul(attrs = {
                classes("d-collection")
                attr("data-window-total", keys.size.toString())
                style {
                    property("position", "relative")
                    property("height", "${extent}px")
                }
            }) {
                indices.forEach { index ->
                    val item = props.items[index]
                    key(item.key) {
                        val top = remember(rows, revision, index) { rows.offset(index) }
                        val currentMeasure by rememberUpdatedState<(Double) -> Unit> { height ->
                            val viewport = state.viewport
                            val anchor = rows.indexAt(state.offset)
                            val before = rows.offset(anchor)
                            if (rows.update(
                                    index,
                                    height.coerceAtLeast(1.0),
                                )
                            ) {
                                // Keep the visible anchor's screen position when an earlier row resizes.
                                val adjustment = rows.offset(anchor) - before
                                if (adjustment != 0.0 && viewport != null) {
                                    viewport.scrollTop += adjustment
                                    state.offset = viewport.scrollTop
                                }
                                revision++
                            }
                        }
                        Li(attrs = {
                            attr("data-window-owner", state.id)
                            attr("data-window-key", item.key)
                            attr("aria-posinset", (index + 1).toString())
                            attr("aria-setsize", keys.size.toString())
                            style {
                                property("position", "absolute")
                                property("top", "${top}px")
                                property("width", "100%")
                                property("display", "flow-root")
                            }
                            ref { element ->
                                val observer =
                                    WindowResizeObserver { currentMeasure(element.getBoundingClientRect().height) }
                                observer.observe(element)
                                onDispose { observer.disconnect() }
                            }
                        }) { state.holder.SaveableStateProvider(item.key) { item.content() } }
                    }
                }
            }
        }
    }
}
