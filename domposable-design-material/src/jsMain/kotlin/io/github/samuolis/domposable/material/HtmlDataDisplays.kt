package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableChartAppearance
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableTextAlignment
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.ui.DomposableChartSeries
import io.github.samuolis.domposable.ui.DomposableChartSize
import io.github.samuolis.domposable.ui.DomposableDataTableColumn
import io.github.samuolis.domposable.ui.DomposableDataTableProps
import io.github.samuolis.domposable.ui.DomposableDataTableRow
import io.github.samuolis.domposable.ui.DomposableDataTableSize
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableLineChartProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableValueChartProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableAttributes
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Table
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Tbody
import org.jetbrains.compose.web.dom.Td
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Th
import org.jetbrains.compose.web.dom.Thead
import org.jetbrains.compose.web.dom.Tr
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.js.asDynamic
import kotlin.math.abs
import kotlin.math.roundToInt

private const val DATA_CHART_SVG_NAMESPACE = "http://www.w3.org/2000/svg"
private const val DATA_CHART_WIDTH = 640.0
private const val DATA_CHART_TOP = 10.0
private const val DATA_CHART_LEFT = 44.0
private const val DATA_CHART_RIGHT = 634.0
private const val DATA_CHART_BOTTOM_INSET = 20
private const val DATA_CHART_FONT_SIZE = 12
private const val DATA_CHART_GRID_OPACITY = 0.3
private const val DATA_CHART_SELECTION_OPACITY = 0.55
private const val DATA_CHART_GRID_STROKE_WIDTH = 1
private const val DATA_CHART_SELECTION_STROKE_WIDTH = 1.5
private const val DATA_CHART_SERIES_STROKE_WIDTH = 2.5
private const val DATA_CHART_SELECTED_POINT_RADIUS = 5
private const val DATA_CHART_POINT_RADIUS = 3
private const val DATA_CHART_GRID_LABEL_BASELINE_OFFSET = 4.0
private const val DATA_CHART_AXIS_LABEL_BOTTOM_OFFSET = 2.0
private const val DATA_CHART_AXIS_LABEL_LEFT_OFFSET = 6

@Composable
internal fun HtmlLineChart(
    props: DomposableLineChartProps,
    modifier: DomposableModifier,
) {
    val tokens = LocalDomposableTokens.current
    val appearance = props.appearance ?: tokens.components.lineChart
    val gap =
        if (props.sizePreset == DomposableChartSize.Thumbnail ||
            props.sizePreset == DomposableChartSize.Compact
        ) {
            8
        } else {
            12
        }
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            style {
                property("display", "flex")
                property("flex-direction", "column")
                property("gap", "${gap}px")
                property("width", "100%")
                property("min-width", "0")
                appearance.background?.let { property("background-color", it.css()) }
                appearance.labelAppearance?.let { applyCatalogTextAppearance(it) }
            }
        }) {
            if (props.displayTitle) {
                props.title?.let { title ->
                    TagElement<HTMLElement>("h2", applyAttrs = {
                        style { appearance.titleAppearance?.let { applyCatalogTextAppearance(it) } }
                    }) { Text(title) }
                }
            }
            props.description?.let { description ->
                Span(attrs = { style { appearance.labelAppearance?.let { applyCatalogTextAppearance(it) } } }) {
                    Text(description)
                }
            }
            if (props.series.any { it.values.isNotEmpty() }) {
                HtmlLineChartSvg(props, appearance)
            }
        }
    }
}

@Composable
private fun HtmlLineChartSvg(
    props: DomposableLineChartProps,
    appearance: DomposableChartAppearance,
) {
    val pointCount = chartPointCount(props.series, props.xAxisLabels)
    val currentPointCount by rememberUpdatedState(pointCount)
    val height = props.sizePreset.materialHeight()
    var selectedIndex by remember(props.series) { mutableIntStateOf(-1) }
    val callback by rememberUpdatedState(props.onSelectedIndexChange)
    val svgNode = remember { mutableStateOf<Element?>(null) }
    val tokens = LocalDomposableTokens.current
    Div(attrs = {
        attr("data-domposable-line-chart", "true")
        style {
            property("width", "100%")
            property("min-width", "0")
            property("height", "${height}px")
            property("touch-action", "pan-y")
        }
        ref { host ->
            val svg = document.createElementNS(DATA_CHART_SVG_NAMESPACE, "svg")
            svg.setAttribute("viewBox", "0 0 $DATA_CHART_WIDTH $height")
            svg.setAttribute("width", "100%")
            svg.setAttribute("height", height.toString())
            svg.setAttribute("preserveAspectRatio", "none")
            svg.setAttribute("role", "img")
            svg.setAttribute("tabindex", "0")
            svg.setAttribute("style", "display: block; width: 100%; height: 100%;")
            appearance.background?.let {
                svg.setAttribute("style", "display:block;width:100%;height:100%;background-color:${it.css()};")
            }
            host.appendChild(svg)
            svgNode.value = svg

            val pointerSelect: (Event) -> Unit = { event ->
                val dynamicEvent = event.asDynamic()
                val clientX = dynamicEvent.clientX as? Number
                if (clientX != null) {
                    val rect = svg.asDynamic().getBoundingClientRect()
                    val left = rect.left as Double
                    val width = (rect.width as Double).coerceAtLeast(1.0)
                    val relativeX =
                        ((clientX.toDouble() - left) / width)
                            .coerceIn(0.0, 1.0)
                    val insetX =
                        (relativeX * DATA_CHART_WIDTH - DATA_CHART_LEFT) /
                            (DATA_CHART_RIGHT - DATA_CHART_LEFT)
                    htmlSelectChartIndex(insetX, currentPointCount)?.let { index ->
                        selectedIndex = index
                        callback?.invoke(index)
                    }
                }
            }
            val pointerMove: (Event) -> Unit = { event ->
                if ((event.asDynamic().buttons as? Number)?.toInt()?.and(1) == 1) pointerSelect(event)
            }
            val pointerEnd: (Event) -> Unit = {
                selectedIndex = -1
                callback?.invoke(null)
            }
            val keyDown: (Event) -> Unit = { event ->
                val dynamicEvent = event.asDynamic()
                val key = dynamicEvent.key as? String
                val next =
                    when (key) {
                        "ArrowLeft" -> (selectedIndex.takeIf { it >= 0 } ?: currentPointCount) - 1
                        "ArrowRight" -> (selectedIndex + 1).coerceAtLeast(0)
                        "Enter", " " -> selectedIndex.takeIf { it >= 0 } ?: 0
                        "Escape" -> -1
                        else -> -2
                    }
                if (next != -2) {
                    dynamicEvent.preventDefault()
                    selectedIndex = next.takeIf { it >= 0 }?.coerceIn(0, currentPointCount - 1) ?: -1
                    callback?.invoke(selectedIndex.takeIf { it >= 0 })
                }
            }
            svg.addEventListener("pointerdown", pointerSelect)
            svg.addEventListener("pointermove", pointerMove)
            svg.addEventListener("pointercancel", pointerEnd)
            svg.addEventListener("pointerleave", pointerEnd)
            svg.addEventListener("keydown", keyDown)
            onDispose {
                svg.removeEventListener("pointerdown", pointerSelect)
                svg.removeEventListener("pointermove", pointerMove)
                svg.removeEventListener("pointercancel", pointerEnd)
                svg.removeEventListener("pointerleave", pointerEnd)
                svg.removeEventListener("keydown", keyDown)
                host.removeChild(svg)
                if (svgNode.value == svg) svgNode.value = null
            }
        }
    }) {}
    SideEffect {
        svgNode.value?.let { svg ->
            svg.setAttribute("viewBox", "0 0 $DATA_CHART_WIDTH $height")
            svg.setAttribute("height", height.toString())
            val allValues =
                props.series.joinToString(". ") { series ->
                    "${series.label}: " +
                        series.values.joinToString(", ") { point ->
                            "${point.label}: ${props.yAxisFormatter(point.value)}"
                        }
                }
            svg.setAttribute(
                "aria-label",
                listOfNotNull(props.title, props.accessibilityDescription, allValues.takeIf(String::isNotBlank))
                    .filter(String::isNotBlank)
                    .joinToString(". "),
            )
            drawHtmlChart(
                svg,
                props,
                selectedIndex,
                height,
                tokens.colors.primary,
                tokens.colors.surfaceVariant,
                tokens.colors.outline,
                tokens.colors.error,
                appearance,
                tokens.colors.surfaceVariant,
                tokens.typography.fontFamily,
            )
        }
    }
    val selected =
        selectedIndex
            .takeIf { it >= 0 }
            ?.let { chartPointsAt(props.series, it, pointCount, centerSingleton = true) }
            .orEmpty()
    if (selected.isNotEmpty()) {
        Div(attrs = {
            attr("aria-live", "polite")
            attr("aria-atomic", "true")
            classes("d-chart-selection")
        }) {
            selected.forEach { point ->
                Span { Text("${point.series.label}, ${point.value.label}: ${props.yAxisFormatter(point.value.value)}") }
            }
        }
    }
}

@Composable
internal fun HtmlValueChart(
    props: DomposableValueChartProps,
    modifier: DomposableModifier,
) {
    val tokens = LocalDomposableTokens.current
    val appearance = props.appearance ?: tokens.components.valueChart
    val maxValue =
        props.series
            .flatMap { it.values }
            .maxOfOrNull { abs(it.value) }
            ?.coerceAtLeast(0.001f) ?: 1f
    val gap = if (props.sizePreset == DomposableChartSize.Compact) 8 else 12
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-value-chart")
            style {
                property("display", "flex")
                property("flex-direction", "column")
                property("gap", "${gap}px")
                appearance.background?.let { property("background-color", it.css()) }
                appearance.labelAppearance?.let { applyCatalogTextAppearance(it) }
            }
        }) {
            TagElement<HTMLElement>("h2", applyAttrs = {
                style { appearance.titleAppearance?.let { applyCatalogTextAppearance(it) } }
            }) { Text(props.title) }
            Span(attrs = {
                style { appearance.labelAppearance?.let { applyCatalogTextAppearance(it) } }
            }) { Text(props.description) }
            TagElement<HTMLElement>("h3", applyAttrs = {
                style { appearance.titleAppearance?.let { applyCatalogTextAppearance(it) } }
            }) { Text(props.dataTableLabel) }
            props.series.forEach { series ->
                key(series.key) {
                    Div(attrs = { classes("d-value-chart-series") }) {
                        Div(attrs = {
                            style {
                                property("display", "flex")
                                property("justify-content", "space-between")
                            }
                        }) {
                            Span { Text(series.label) }
                            series.currentValue?.let { Span { Text(props.formatValue(it)) } }
                        }
                        series.values.forEach { point ->
                            key(point.label) {
                                val color =
                                    chartColor(
                                        series,
                                        tokens.colors.primary,
                                        tokens.colors.secondary ?: tokens.colors.surfaceVariant,
                                        tokens.colors.tertiary ?: tokens.colors.outline,
                                        tokens.colors.error,
                                    )
                                Div(attrs = {
                                    style {
                                        property("display", "grid")
                                        property("grid-template-columns", "minmax(80px, 1fr) minmax(80px, 1fr) auto")
                                        property("align-items", "center")
                                        property("gap", "8px")
                                    }
                                }) {
                                    Span { Text(point.label) }
                                    TagElement<HTMLElement>("progress", applyAttrs = {
                                        attr("max", "1")
                                        attr("value", (abs(point.value) / maxValue).coerceIn(0f, 1f).toString())
                                        attr(
                                            "aria-label",
                                            "${series.label}, ${point.label}: ${props.formatValue(point.value)}",
                                        )
                                        style {
                                            property("width", "100%")
                                            property("accent-color", color)
                                            appearance.grid?.let { property("--d-progress-track", it.css()) }
                                        }
                                    }) { }
                                    Span { Text(props.formatValue(point.value)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HtmlDataTable(
    props: DomposableDataTableProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.dataTable
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-data-table-container", "d-data-table-${props.sizePreset.name.lowercase()}")
            style {
                property("max-width", "100%")
                property("overflow-x", if (props.allowHorizontalOverflow) "auto" else "hidden")
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
                    if (props.allowHorizontalOverflow) {
                        property(
                            "min-width",
                            "${(props.columns.size * 160).coerceAtLeast(1)}px",
                        )
                    }
                }
            }) {
                Thead {
                    Tr {
                        props.columns.forEach { column ->
                            key(column.key) {
                                Th(attrs = {
                                    attr("scope", "col")
                                    attr("aria-label", column.accessibleLabel)
                                    style {
                                        property("text-align", column.alignment.htmlTextAlign())
                                        property(
                                            "padding",
                                            appearance.cellPadding?.cssPadding() ?: props.sizePreset.htmlCellPadding(),
                                        )
                                        appearance.headerContainer?.let { property("background-color", it.css()) }
                                        appearance.headerContent?.let { property("color", it.css()) }
                                        appearance.border?.let { property("border-bottom", "1px solid ${it.css()}") }
                                        appearance.headerTextAppearance?.let { applyCatalogTextAppearance(it) }
                                    }
                                }) { Text(column.label) }
                            }
                        }
                    }
                }
                Tbody {
                    props.rows.forEachIndexed { index, row ->
                        key(row.key) {
                            val actions = props.columns.associate { it.key to props.cellAction(row.key, it.key) }
                            val rowAction =
                                props.onRowActivate?.takeIf {
                                    actions.values.none { action ->
                                        action != null
                                    }
                                }
                            Tr(attrs = {
                                attr("data-row-key", row.key)
                                attr("aria-rowindex", (index + 2).toString())
                                attr("aria-selected", row.selected.toString())
                                row.accessibleLabel?.let { attr("aria-label", it) }
                                style {
                                    (if (row.selected) appearance.selectedContainer else appearance.rowContainer)?.let {
                                        property("background-color", it.css())
                                    }
                                    (if (row.selected) appearance.selectedContent else appearance.rowContent)?.let {
                                        property("color", it.css())
                                    }
                                    appearance.border?.let { property("border-bottom", "1px solid ${it.css()}") }
                                }
                                if (rowAction != null) {
                                    attr("tabindex", "0")
                                    onClick { rowAction(row.key) }
                                    onKeyDown { event ->
                                        if (event.key == "Enter" || event.key == " ") {
                                            event.preventDefault()
                                            rowAction(row.key)
                                        }
                                    }
                                }
                            }) {
                                props.columns.forEach { column ->
                                    key(column.key) {
                                        val action = actions[column.key]
                                        val applyCell: @Composable () -> Unit = {
                                            if (action == null) {
                                                props.cellContent(row, column)
                                            } else {
                                                Div(attrs = {
                                                    attr("role", "button")
                                                    attr("tabindex", "0")
                                                    attr("aria-label", column.accessibleLabel)
                                                    onClick { action() }
                                                    onKeyDown { event ->
                                                        if (event.key == "Enter" || event.key == " ") {
                                                            event.preventDefault()
                                                            action()
                                                        }
                                                    }
                                                }) { props.cellContent(row, column) }
                                            }
                                        }
                                        if (column.key == props.rowHeaderColumnKey) {
                                            Th(attrs = {
                                                attr("scope", "row")
                                                attr("data-column-label", column.accessibleLabel)
                                                style {
                                                    property("text-align", column.alignment.htmlTextAlign())
                                                    property(
                                                        "padding",
                                                        appearance.cellPadding?.cssPadding()
                                                            ?: props.sizePreset.htmlCellPadding(),
                                                    )
                                                    val rowContainer =
                                                        if (row.selected) {
                                                            appearance.selectedContainer
                                                        } else {
                                                            appearance.rowContainer
                                                        }
                                                    val rowContent =
                                                        if (row.selected) {
                                                            appearance.selectedContent
                                                        } else {
                                                            appearance.rowContent
                                                        }
                                                    rowContainer?.let { property("background-color", it.css()) }
                                                    rowContent?.let { property("color", it.css()) }
                                                    appearance.border?.let {
                                                        property(
                                                            "border-bottom",
                                                            "1px solid ${it.css()}",
                                                        )
                                                    }
                                                    if (!props.allowHorizontalOverflow) {
                                                        property(
                                                            "overflow-wrap",
                                                            "anywhere",
                                                        )
                                                    }
                                                }
                                            }) { applyCell() }
                                        } else {
                                            Td(attrs = {
                                                attr("data-column-label", column.accessibleLabel)
                                                style {
                                                    property("text-align", column.alignment.htmlTextAlign())
                                                    property(
                                                        "padding",
                                                        appearance.cellPadding?.cssPadding()
                                                            ?: props.sizePreset.htmlCellPadding(),
                                                    )
                                                    val rowContainer =
                                                        if (row.selected) {
                                                            appearance.selectedContainer
                                                        } else {
                                                            appearance.rowContainer
                                                        }
                                                    val rowContent =
                                                        if (row.selected) {
                                                            appearance.selectedContent
                                                        } else {
                                                            appearance.rowContent
                                                        }
                                                    rowContainer?.let { property("background-color", it.css()) }
                                                    rowContent?.let { property("color", it.css()) }
                                                    appearance.border?.let {
                                                        property(
                                                            "border-bottom",
                                                            "1px solid ${it.css()}",
                                                        )
                                                    }
                                                    if (!props.allowHorizontalOverflow) {
                                                        property(
                                                            "overflow-wrap",
                                                            "anywhere",
                                                        )
                                                    }
                                                }
                                            }) { applyCell() }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun htmlChartX(
    index: Int,
    count: Int,
): Double =
    if (count <= 1) {
        (DATA_CHART_LEFT + DATA_CHART_RIGHT) / 2.0
    } else {
        DATA_CHART_LEFT + (DATA_CHART_RIGHT - DATA_CHART_LEFT) * index.coerceIn(0, count - 1) / (count - 1)
    }

private fun htmlSelectChartIndex(
    relativeX: Double,
    pointCount: Int,
): Int? =
    if (pointCount <=
        0
    ) {
        null
    } else {
        (relativeX.coerceIn(0.0, 1.0) * (pointCount - 1)).roundToInt().coerceIn(0, pointCount - 1)
    }

private fun htmlSvgElement(
    parent: Element,
    tag: String,
    attributes: Map<String, String>,
): Element =
    document.createElementNS(DATA_CHART_SVG_NAMESPACE, tag).also { element ->
        attributes.forEach { (name, value) -> element.setAttribute(name, value) }
        parent.appendChild(element)
    }

private fun htmlSvgText(
    parent: Element,
    text: String,
    x: Double,
    y: Double,
    anchor: String,
    fontSize: String,
    color: String,
    appearance: DomposableTextAppearance? = null,
    fontFamily: String = "system-ui, sans-serif",
) {
    val node =
        htmlSvgElement(
            parent,
            "text",
            mapOf(
                "x" to x.toString(),
                "y" to y.toString(),
                "text-anchor" to anchor,
                "font-size" to (appearance?.fontSize?.toString() ?: fontSize),
                "fill" to color,
                "font-family" to fontFamily,
            ),
        )
    appearance?.fontWeight?.let { node.setAttribute("font-weight", it.css()) }
    appearance?.letterSpacing?.let { node.setAttribute("letter-spacing", it.toString()) }
    node.textContent = text
}

private fun DomposableTextAlignment.htmlTextAlign() =
    when (this) {
        DomposableTextAlignment.Start -> "start"
        DomposableTextAlignment.Center -> "center"
        DomposableTextAlignment.End -> "end"
        DomposableTextAlignment.Justify -> "justify"
    }

private fun DomposableDataTableSize.htmlCellPadding() =
    when (this) {
        DomposableDataTableSize.Compact -> "4px 6px"
        DomposableDataTableSize.Standard -> "8px"
        DomposableDataTableSize.Expanded -> "12px"
    }

private fun drawHtmlChart(
    svg: Element,
    props: DomposableLineChartProps,
    selectedIndex: Int,
    height: Int,
    primary: DomposableColor,
    secondary: DomposableColor,
    tertiary: DomposableColor,
    error: DomposableColor,
    appearance: DomposableChartAppearance,
    defaultAxis: DomposableColor,
    fontFamily: String,
) {
    while (svg.firstChild != null) svg.removeChild(svg.firstChild!!)
    val pointCount = chartPointCount(props.series, props.xAxisLabels)
    val range = chartValueRange(props.series)
    val bottom = (height - DATA_CHART_BOTTOM_INSET).toDouble()
    val plotHeight = (bottom - DATA_CHART_TOP).coerceAtLeast(1.0)
    drawHtmlChartGrid(
        svg,
        props,
        range,
        plotHeight,
        appearance.axis?.css() ?: defaultAxis.css(),
        appearance.grid?.css() ?: tertiary.css(),
        appearance.labelAppearance,
        fontFamily,
    )
    drawHtmlSelectedIndex(svg, selectedIndex, pointCount, bottom, (appearance.selection ?: primary).css())
    drawHtmlChartSeries(
        svg,
        props.series,
        pointCount,
        range,
        plotHeight,
        primary,
        secondary,
        tertiary,
        error,
        selectedIndex,
    )
    drawHtmlChartAxisLabels(
        svg,
        props,
        pointCount,
        height,
        appearance.axis?.css() ?: defaultAxis.css(),
        appearance.labelAppearance,
        fontFamily,
    )
}

private fun drawHtmlChartGrid(
    svg: Element,
    props: DomposableLineChartProps,
    range: DomposableChartValueRange,
    plotHeight: Double,
    labelColor: String,
    gridColor: String,
    labelAppearance: DomposableTextAppearance?,
    fontFamily: String,
) {
    repeat(DOMPOSABLE_CHART_GRID_LINE_COUNT) { tick ->
        val fraction = tick.toDouble() / DOMPOSABLE_CHART_GRID_DIVISION_COUNT
        val y = DATA_CHART_TOP + plotHeight * fraction
        htmlSvgElement(
            svg,
            "line",
            mapOf(
                "x1" to DATA_CHART_LEFT.toString(),
                "x2" to DATA_CHART_RIGHT.toString(),
                "y1" to y.toString(),
                "y2" to y.toString(),
                "stroke" to gridColor,
                "stroke-opacity" to DATA_CHART_GRID_OPACITY.toString(),
                "stroke-width" to DATA_CHART_GRID_STROKE_WIDTH.toString(),
            ),
        )
        val value = range.maximum - (range.maximum - range.minimum) * fraction
        htmlSvgText(
            svg,
            props.yAxisFormatter(value.toFloat()),
            DATA_CHART_LEFT - DATA_CHART_AXIS_LABEL_LEFT_OFFSET,
            y + DATA_CHART_GRID_LABEL_BASELINE_OFFSET,
            "end",
            DATA_CHART_FONT_SIZE.toString(),
            labelColor,
            labelAppearance,
            fontFamily,
        )
    }
}

private fun drawHtmlSelectedIndex(
    svg: Element,
    selectedIndex: Int,
    pointCount: Int,
    bottom: Double,
    color: String,
) {
    if (selectedIndex < 0) return
    val x = htmlChartX(selectedIndex, pointCount)
    htmlSvgElement(
        svg,
        "line",
        mapOf(
            "x1" to x.toString(),
            "x2" to x.toString(),
            "y1" to DATA_CHART_TOP.toString(),
            "y2" to bottom.toString(),
            "stroke" to color,
            "stroke-opacity" to DATA_CHART_SELECTION_OPACITY.toString(),
            "stroke-width" to DATA_CHART_SELECTION_STROKE_WIDTH.toString(),
        ),
    )
}

private fun drawHtmlChartSeries(
    svg: Element,
    series: List<DomposableChartSeries>,
    pointCount: Int,
    range: DomposableChartValueRange,
    plotHeight: Double,
    primary: DomposableColor,
    secondary: DomposableColor,
    tertiary: DomposableColor,
    error: DomposableColor,
    selectedIndex: Int,
) {
    series.forEach { item ->
        if (item.values.isEmpty()) return@forEach
        val color = chartColor(item, primary, secondary, tertiary, error)
        val points =
            item.values.mapIndexed { localIndex, point ->
                val index = chartPointIndex(point, localIndex, item.values.size, pointCount, centerSingleton = true)
                val y = DATA_CHART_TOP + ((range.maximum - point.value) / (range.maximum - range.minimum)) * plotHeight
                index to y
            }
        htmlSvgElement(
            svg,
            "polyline",
            mapOf(
                "points" to points.joinToString(" ") { (index, y) -> "${htmlChartX(index, pointCount)},$y" },
                "fill" to "none",
                "stroke" to color,
                "stroke-width" to DATA_CHART_SERIES_STROKE_WIDTH.toString(),
                "stroke-linecap" to "round",
                "stroke-linejoin" to "round",
            ),
        )
        points.forEach { (index, y) ->
            val radius = if (selectedIndex == index) DATA_CHART_SELECTED_POINT_RADIUS else DATA_CHART_POINT_RADIUS
            htmlSvgElement(
                svg,
                "circle",
                mapOf(
                    "cx" to htmlChartX(index, pointCount).toString(),
                    "cy" to y.toString(),
                    "r" to radius.toString(),
                    "fill" to color,
                ),
            )
        }
    }
}

private fun drawHtmlChartAxisLabels(
    svg: Element,
    props: DomposableLineChartProps,
    pointCount: Int,
    height: Int,
    color: String,
    labelAppearance: DomposableTextAppearance?,
    fontFamily: String,
) {
    chartTickIndices(pointCount).forEach { index ->
        chartAxisLabel(props.series, props.xAxisLabels, index, pointCount, centerSingleton = true)?.let { label ->
            val anchor =
                when (index) {
                    0 -> "start"
                    pointCount - 1 -> "end"
                    else -> "middle"
                }
            htmlSvgText(
                svg,
                label,
                htmlChartX(index, pointCount),
                height - DATA_CHART_AXIS_LABEL_BOTTOM_OFFSET,
                anchor,
                DATA_CHART_FONT_SIZE.toString(),
                color,
                labelAppearance,
                fontFamily,
            )
        }
    }
}
