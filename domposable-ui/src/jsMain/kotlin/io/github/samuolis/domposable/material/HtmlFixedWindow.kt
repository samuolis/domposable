package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableGridWindow
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableLazyGridProps
import io.github.samuolis.domposable.ui.DomposableLazyItem
import io.github.samuolis.domposable.ui.DomposableLazyRowProps
import io.github.samuolis.domposable.ui.DomposableListWindow
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRowWindow
import io.github.samuolis.domposable.ui.domposableAttributes
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Ul

@Composable
internal fun HtmlRowWindow(
    props: DomposableLazyRowProps,
    modifier: DomposableModifier,
) {
    val window = requireNotNull(props.window)
    HtmlFixedWindow(props.items, modifier, props.gap.value.toDouble(), row = window)
}

@Composable
internal fun HtmlGridWindow(
    props: DomposableLazyGridProps,
    modifier: DomposableModifier,
) {
    val window = requireNotNull(props.window)
    require(props.columns > 0)
    HtmlFixedWindow(props.items, modifier, props.gap.value.toDouble(), grid = window, columns = props.columns)
}

@Composable
private fun HtmlFixedWindow(
    items: List<DomposableLazyItem>,
    modifier: DomposableModifier,
    gap: Double,
    row: DomposableRowWindow? = null,
    grid: DomposableGridWindow? = null,
    columns: Int = 1,
) {
    require(gap.isFinite() && gap >= 0)
    val keys = remember(items) { items.map { it.key } }
    val state = rememberHtmlWindowState(keys)
    val horizontal = row != null
    var viewportWidth by remember { mutableDoubleStateOf(row?.viewportWidth?.value?.toDouble() ?: 0.0) }
    val unitHeight = row?.itemHeight?.value ?: requireNotNull(grid).rowHeight.value
    val unitSize = if (horizontal) row!!.itemWidth.value else unitHeight
    val viewportSize = if (horizontal) viewportWidth.coerceAtLeast(1.0) else grid!!.viewportHeight.value.toDouble()
    val overscan = row?.overscan ?: grid!!.overscanRows
    val rows = if (horizontal) items.size else ((items.size.toLong() + columns - 1) / columns).toInt()
    val model = DomposableListWindow(unitSize.dp, viewportSize.toFloat().dp, overscan)
    val visibleRows = listWindowRange(rows, model, gap.toFloat().dp, state.offset)
    val visibleItems =
        if (horizontal) {
            visibleRows
        } else {
            val first = (visibleRows.first.toLong() * columns).coerceAtMost(items.size.toLong()).toInt()
            val end = ((visibleRows.last.toLong() + 1) * columns).coerceAtMost(items.size.toLong()).toInt()
            first until end
        }
    val indices = state.indices(visibleItems)
    val extent = listExtent(rows, model, gap.toFloat().dp)
    val cellWidth =
        if (horizontal) {
            row!!.itemWidth.value.toDouble()
        } else {
            ((viewportWidth - (columns - 1) * gap) / columns).coerceAtLeast(0.0)
        }
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            attr("data-domposable-window", if (horizontal) "row" else "grid")
            attr("data-window-columns", columns.toString())
            style {
                property("height", "${row?.itemHeight?.value ?: grid!!.viewportHeight.value}px")
                if (horizontal) property("width", "${row!!.viewportWidth.value}px")
                property("max-width", "100%")
                property("overflow", if (horizontal) "auto hidden" else "hidden auto")
                property("overflow-anchor", "none")
            }
            ref { element ->
                val detach = state.attach(element, horizontal)
                val observer = WindowResizeObserver { viewportWidth = element.clientWidth.toDouble() }
                observer.observe(element)
                viewportWidth = element.clientWidth.toDouble()
                onDispose {
                    observer.disconnect()
                    detach()
                }
            }
        }) {
            Ul(attrs = {
                classes("d-collection")
                attr("data-window-total", items.size.toString())
                style {
                    property("position", "relative")
                    property("height", "${if (horizontal) unitHeight.toDouble() else extent}px")
                    if (horizontal) property("width", "${extent}px")
                }
            }) {
                indices.forEach { index ->
                    val item = items[index]
                    key(item.key) {
                        val inlineOffset = (if (horizontal) index else index % columns) * (cellWidth + gap)
                        Li(attrs = {
                            attr("data-window-owner", state.id)
                            attr("data-window-key", item.key)
                            attr("aria-posinset", (index + 1).toString())
                            attr("aria-setsize", items.size.toString())
                            style {
                                property("position", "absolute")
                                property(
                                    "inset-inline-start",
                                    "${inlineOffset}px",
                                )
                                property("top", "${if (horizontal) 0.0 else (index / columns) * (unitHeight + gap)}px")
                                property("width", "${cellWidth}px")
                                property("height", "${unitHeight}px")
                                property("overflow", "hidden")
                            }
                        }) { state.holder.SaveableStateProvider(item.key) { item.content() } }
                    }
                }
            }
        }
    }
}
