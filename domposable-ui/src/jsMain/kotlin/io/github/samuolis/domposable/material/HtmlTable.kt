package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableTableAppearance
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTableProps
import io.github.samuolis.domposable.ui.DomposableTableRow
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableAttributes
import org.jetbrains.compose.web.dom.Caption
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Table
import org.jetbrains.compose.web.dom.Tbody
import org.jetbrains.compose.web.dom.Td
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Th
import org.jetbrains.compose.web.dom.Thead
import org.jetbrains.compose.web.dom.Tr

@Composable
internal fun HtmlTable(
    props: DomposableTableProps,
    modifier: DomposableModifier,
) {
    val window = props.window
    val keys = remember(props.rows) { props.rows.map { it.key } }
    val state = rememberHtmlWindowState(keys)
    val known = remember { mutableMapOf<String, Double>() }
    DisposableEffect(keys) {
        known.keys.retainAll(keys.toSet())
        onDispose { }
    }
    val geometry =
        remember(keys, window) {
            MeasuredRows(
                keys,
                window?.rowHeight?.value?.toDouble() ?: 48.0,
                0.0,
                if (window?.measureRows == true) known else mutableMapOf(),
            )
        }
    var revision by remember { mutableIntStateOf(0) }
    var bodyStart by remember { mutableDoubleStateOf(0.0) }
    val extent = remember(geometry, revision) { geometry.extent }
    val range =
        remember(geometry, revision, state.offset, bodyStart, window) {
            if (window == null) {
                keys.indices
            } else {
                geometry.range(
                    (state.offset - bodyStart).coerceAtLeast(0.0),
                    window.viewportHeight.value.toDouble(),
                    window.overscan,
                )
            }
        }
    val indices = state.indices(range)
    val surface =
        LocalDomposableTokens.current.colors.surface
            .css()
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.table
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            if (window != null) {
                attr("data-domposable-window", "table")
                attr("role", "region")
                attr("aria-label", props.caption)
                attr("tabindex", "0")
            }
            style {
                property("max-width", "100%")
                property("overflow", "auto")
                property("overflow-anchor", "none")
                window?.let { property("height", "${it.viewportHeight.value}px") }
            }
            ref { element ->
                val detach = state.attach(element, false)
                onDispose { detach() }
            }
        }) {
            Table(attrs = {
                attr("aria-rowcount", (props.rows.size + 1).toString())
                attr("aria-colcount", props.columns.size.toString())
                style {
                    property("border-collapse", "collapse")
                    property("table-layout", "fixed")
                    property("width", "100%")
                    appearance.textAppearance?.let { applyCatalogTextAppearance(it) }
                }
            }) {
                Caption { Text(props.caption) }
                Thead(attrs = {
                    style {
                        property("position", "sticky")
                        property("top", "0")
                        property("z-index", "1")
                        property("background", appearance.headerContainer?.css() ?: surface)
                        appearance.headerContent?.let { property("color", it.css()) }
                    }
                }) {
                    Tr(attrs = { attr("aria-rowindex", "1") }) {
                        props.columns.forEach { column ->
                            key(column.key) {
                                Th(attrs = {
                                    attr("scope", "col")
                                    style {
                                        property("padding", "8px")
                                        property("text-align", "start")
                                        appearance.headerContainer?.let { property("background-color", it.css()) }
                                        appearance.headerContent?.let { property("color", it.css()) }
                                        appearance.border?.let { property("border-bottom", "1px solid ${it.css()}") }
                                        appearance.headerTextAppearance?.let { applyCatalogTextAppearance(it) }
                                    }
                                }) {
                                    Text(column.label)
                                }
                            }
                        }
                    }
                }
                Tbody(attrs = {
                    ref { element ->
                        val observer =
                            WindowResizeObserver {
                                state.viewport?.let { viewport ->
                                    bodyStart =
                                        element.getBoundingClientRect().top - viewport.getBoundingClientRect().top +
                                        viewport.scrollTop
                                }
                            }
                        observer.observe(element)
                        onDispose { observer.disconnect() }
                    }
                }) {
                    var cursor = 0
                    indices.forEach { index ->
                        if (window != null && index > cursor) {
                            TableSpacer(props.columns.size, geometry.offset(index) - geometry.offset(cursor))
                        }
                        val row = props.rows[index]
                        key(row.key) {
                            HtmlTableRow(props, appearance, row, index, state) { height ->
                                val anchor = geometry.indexAt((state.offset - bodyStart).coerceAtLeast(0.0))
                                val before = geometry.offset(anchor)
                                if (geometry.update(index, height.coerceAtLeast(1.0))) {
                                    val adjustment = geometry.offset(anchor) - before
                                    state.viewport?.let { viewport ->
                                        if (adjustment != 0.0) {
                                            viewport.scrollTop += adjustment
                                            state.offset = viewport.scrollTop
                                        }
                                    }
                                    revision++
                                }
                            }
                        }
                        cursor = index + 1
                    }
                    if (window != null &&
                        cursor < props.rows.size
                    ) {
                        TableSpacer(props.columns.size, extent - geometry.offset(cursor))
                    }
                }
            }
        }
    }
}

@Composable
private fun TableSpacer(
    columns: Int,
    height: Double,
) {
    if (height <= 0.0) return
    Tr(attrs = {
        attr("aria-hidden", "true")
        attr("data-window-spacer", "true")
    }) {
        Td(attrs = {
            attr("colspan", columns.toString())
            style {
                property("height", "${height}px")
                property("padding", "0")
                property("border", "none")
            }
        }) { }
    }
}

@Composable
private fun HtmlTableRow(
    props: DomposableTableProps,
    appearance: DomposableTableAppearance,
    row: DomposableTableRow,
    index: Int,
    state: HtmlWindowState,
    onMeasure: (Double) -> Unit,
) {
    val fixed = props.window?.takeUnless { it.measureRows }
    val currentMeasure by rememberUpdatedState(onMeasure)
    Tr(attrs = {
        attr("data-window-owner", state.id)
        attr("data-window-key", row.key)
        attr("aria-rowindex", (index + 2).toString())
        style {
            appearance.rowContainer?.let { property("background-color", it.css()) }
            appearance.rowContent?.let { property("color", it.css()) }
            appearance.border?.let { property("border-bottom", "1px solid ${it.css()}") }
        }
        fixed?.let { style { property("height", "${it.rowHeight.value}px") } }
        if (props.window?.measureRows == true) {
            ref { element ->
                val observer = WindowResizeObserver { currentMeasure(element.getBoundingClientRect().height) }
                observer.observe(element)
                onDispose { observer.disconnect() }
            }
        }
    }) {
        state.holder.SaveableStateProvider(row.key) {
            row.cells.forEachIndexed { column, cell ->
                key(props.columns[column].key) {
                    Td(attrs = {
                        style {
                            property("padding", appearance.cellPadding?.cssPadding() ?: "8px")
                            property("vertical-align", "top")
                            appearance.rowContainer?.let { property("background-color", it.css()) }
                            appearance.rowContent?.let { property("color", it.css()) }
                            appearance.border?.let { property("border-bottom", "1px solid ${it.css()}") }
                        }
                    }) {
                        Div(attrs = {
                            fixed?.let {
                                style {
                                    property("height", "${(it.rowHeight.value - 16).coerceAtLeast(0f)}px")
                                    property("overflow", "hidden")
                                }
                            }
                        }) { cell() }
                    }
                }
            }
        }
    }
}
