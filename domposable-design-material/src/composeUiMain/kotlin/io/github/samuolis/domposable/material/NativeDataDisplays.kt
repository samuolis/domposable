package io.github.samuolis.domposable.material

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableTextAlignment
import io.github.samuolis.domposable.ui.DomposableChartSeries
import io.github.samuolis.domposable.ui.DomposableChartSize
import io.github.samuolis.domposable.ui.DomposableChartTone
import io.github.samuolis.domposable.ui.DomposableDataTableColumn
import io.github.samuolis.domposable.ui.DomposableDataTableProps
import io.github.samuolis.domposable.ui.DomposableDataTableRow
import io.github.samuolis.domposable.ui.DomposableDataTableSize
import io.github.samuolis.domposable.ui.DomposableLineChartProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableValueChartProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.toComposeModifier
import kotlin.math.abs
import kotlin.math.roundToInt

private const val NATIVE_CHART_INSET_DP = 10
private const val NATIVE_CHART_GRID_ALPHA = 0.28f
private const val NATIVE_CHART_SELECTION_ALPHA = 0.55f
private const val NATIVE_CHART_GRID_STROKE_DP = 1f
private const val NATIVE_CHART_SERIES_STROKE_DP = 2.5f
private const val NATIVE_CHART_SELECTED_POINT_RADIUS_DP = 5f
private const val NATIVE_CHART_POINT_RADIUS_DP = 3f
private val NATIVE_TABLE_COMPACT_GAP = 4.dp
private val NATIVE_TABLE_STANDARD_GAP = 8.dp
private val NATIVE_TABLE_EXPANDED_GAP = 12.dp

@Composable
internal fun NativeLineChart(
    props: DomposableLineChartProps,
    modifier: DomposableModifier,
) {
    val gap =
        when (props.sizePreset) {
            DomposableChartSize.Thumbnail, DomposableChartSize.Compact -> 8.dp
            DomposableChartSize.Standard, DomposableChartSize.Expanded -> 12.dp
        }
    Column(modifier.toComposeModifier().fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
        if (props.displayTitle) props.title?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
        props.description?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (props.series.any { it.values.isNotEmpty() }) {
            NativeLineChartPlot(props)
        }
    }
}

@Composable
private fun NativeLineChartPlot(props: DomposableLineChartProps) {
    val pointCount = chartPointCount(props.series, props.xAxisLabels)
    val range = chartValueRange(props.series)
    val height = props.sizePreset.materialHeight()
    var selectedIndex by remember(props.series) { mutableIntStateOf(-1) }
    val onSelectedIndexChange by rememberUpdatedState(props.onSelectedIndexChange)
    val tokens = LocalDomposableTokens.current
    val selectedPoints =
        selectedIndex
            .takeIf { it >= 0 }
            ?.let {
                chartPointsAt(props.series, it, pointCount)
            }.orEmpty()
    val selectionDescription =
        selectedPoints.joinToString(". ") { point ->
            "${point.series.label}: ${props.yAxisFormatter(point.value.value)}"
        }
    val chartDescription =
        listOfNotNull(props.title, props.accessibilityDescription, selectionDescription)
            .filter(String::isNotBlank)
            .joinToString(". ")
    val plotModifier =
        Modifier
            .fillMaxWidth()
            .height(height.dp)
            .semantics {
                contentDescription =
                    chartDescription.ifBlank {
                        props.series.joinToString(". ") { it.label }
                    }
                if (selectedIndex >= 0) selected = true
            }.pointerInput(pointCount) {
                detectTapGestures { offset ->
                    selectNativeChartIndex(offset.x, size.width.toFloat(), pointCount) { selected ->
                        selectedIndex = selected
                        onSelectedIndexChange?.invoke(selected)
                    }
                }
            }.pointerInput(pointCount) {
                detectDragGestures { change, _ ->
                    change.consume()
                    selectNativeChartIndex(change.position.x, size.width.toFloat(), pointCount) { selected ->
                        selectedIndex = selected
                        onSelectedIndexChange?.invoke(selected)
                    }
                }
            }.onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                if (event.key == Key.Escape) {
                    selectedIndex = -1
                    onSelectedIndexChange?.invoke(null)
                    return@onKeyEvent true
                }
                if (pointCount <= 0) return@onKeyEvent false
                val next =
                    when (event.key) {
                        Key.DirectionLeft -> (selectedIndex.takeIf { it >= 0 } ?: pointCount) - 1
                        Key.DirectionRight -> (selectedIndex + 1).coerceAtLeast(0)
                        Key.Enter, Key.Spacebar -> selectedIndex.takeIf { it >= 0 } ?: 0
                        else -> return@onKeyEvent false
                    }.coerceIn(0, pointCount - 1)
                selectedIndex = next
                onSelectedIndexChange?.invoke(next)
                true
            }.focusable()
    Canvas(plotModifier) {
        val inset = NATIVE_CHART_INSET_DP.dp.toPx()
        val plotWidth = (size.width - inset * 2).coerceAtLeast(1f)
        val plotHeight = (size.height - inset * 2).coerceAtLeast(1f)
        val gridColor = Color(tokens.colors.outline.argb).copy(alpha = NATIVE_CHART_GRID_ALPHA)
        repeat(DOMPOSABLE_CHART_GRID_LINE_COUNT) { tick ->
            val y = inset + plotHeight * tick / DOMPOSABLE_CHART_GRID_DIVISION_COUNT
            drawLine(
                gridColor,
                Offset(inset, y),
                Offset(size.width - inset, y),
                strokeWidth = NATIVE_CHART_GRID_STROKE_DP.dp.toPx(),
            )
        }
        selectedIndex.takeIf { it >= 0 }?.let { index ->
            val x = chartX(index, pointCount, inset, plotWidth)
            drawLine(
                Color(tokens.colors.primary.argb).copy(alpha = NATIVE_CHART_SELECTION_ALPHA),
                Offset(x, inset),
                Offset(x, size.height - inset),
                strokeWidth = NATIVE_CHART_GRID_STROKE_DP.dp.toPx(),
            )
        }
        props.series.forEach { series ->
            val color =
                series.nativeColor(
                    tokens.colors.primary.argb,
                    tokens.colors.surfaceVariant.argb,
                    tokens.colors.outline.argb,
                    tokens.colors.error.argb,
                )
            val points =
                series.values.mapIndexed { localIndex, point ->
                    val index = chartPointIndex(point, localIndex, series.values.size, pointCount)
                    Offset(
                        chartX(index, pointCount, inset, plotWidth),
                        chartY(point.value, range, inset, plotHeight),
                    )
                }
            points.zipWithNext().forEach { (start, end) ->
                drawLine(
                    color,
                    start,
                    end,
                    strokeWidth = NATIVE_CHART_SERIES_STROKE_DP.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            points.forEach { point ->
                drawCircle(
                    color = color,
                    radius =
                        if (selectedIndex >= 0 &&
                            point.x == chartX(selectedIndex, pointCount, inset, plotWidth)
                        ) {
                            NATIVE_CHART_SELECTED_POINT_RADIUS_DP.dp.toPx()
                        } else {
                            NATIVE_CHART_POINT_RADIUS_DP.dp.toPx()
                        },
                    center = point,
                )
            }
        }
    }
    val tickIndices = chartTickIndices(pointCount)
    if (tickIndices.isNotEmpty()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            tickIndices.forEach { index ->
                chartAxisLabel(props.series, props.xAxisLabels, index, pointCount)?.let { label ->
                    Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
            }
        }
    }
    if (selectedPoints.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            selectedPoints.forEach { point ->
                Text("${point.value.label}: ${point.series.label} ${props.yAxisFormatter(point.value.value)}")
            }
        }
    }
}

@Composable
internal fun NativeValueChart(
    props: DomposableValueChartProps,
    modifier: DomposableModifier,
) {
    val tokens = LocalDomposableTokens.current
    val maximum =
        props.series
            .flatMap { it.values }
            .maxOfOrNull { abs(it.value) }
            ?.coerceAtLeast(0.001f) ?: 1f
    val gap = if (props.sizePreset == DomposableChartSize.Compact) 8.dp else 12.dp
    Column(modifier.toComposeModifier().fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
        Text(props.title, style = MaterialTheme.typography.titleMedium)
        Text(props.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            props.dataTableLabel,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleSmall,
        )
        props.series.forEach { series ->
            key(series.key) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(series.label, style = MaterialTheme.typography.bodyMedium)
                        series.currentValue?.let { Text(props.formatValue(it)) }
                    }
                    series.values.forEach { point ->
                        key(point.label) {
                            val color =
                                series.nativeColor(
                                    tokens.colors.primary.argb,
                                    tokens.colors.surfaceVariant.argb,
                                    tokens.colors.outline.argb,
                                    tokens.colors.error.argb,
                                )
                            Row(
                                Modifier.fillMaxWidth().semantics {
                                    contentDescription =
                                        "${series.label}, ${point.label}: ${props.formatValue(point.value)}"
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(point.label, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                                LinearProgressIndicator(
                                    progress = { (abs(point.value) / maximum).coerceIn(0f, 1f) },
                                    modifier = Modifier.weight(1f),
                                    color = color,
                                    trackColor = Color(tokens.colors.surfaceVariant.argb),
                                )
                                Text(props.formatValue(point.value), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NativeDataTable(
    props: DomposableDataTableProps,
    modifier: DomposableModifier,
) {
    val scroll = rememberScrollState()
    BoxWithConstraints(modifier.toComposeModifier().fillMaxWidth()) {
        val naturalWidth = (props.columns.size.coerceAtLeast(1) * 160).dp
        val tableWidth = if (props.allowHorizontalOverflow) maxOf(maxWidth, naturalWidth) else maxWidth
        Row(Modifier.fillMaxWidth().horizontalScroll(scroll)) {
            Column(
                Modifier.width(tableWidth).semantics {
                    collectionInfo = CollectionInfo(props.rows.size + 1, props.columns.size)
                },
                verticalArrangement = Arrangement.spacedBy(props.sizePreset.nativeGap()),
            ) {
                Row(
                    Modifier.fillMaxWidth().semantics {
                        collectionItemInfo = CollectionItemInfo(0, 1, 0, props.columns.size)
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    props.columns.forEach { column ->
                        key(column.key) {
                            Text(
                                column.label,
                                Modifier.weight(1f).padding(8.dp).semantics {
                                    heading()
                                    contentDescription = column.accessibleLabel
                                },
                                textAlign = column.alignment.nativeTextAlign(),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
                props.rows.forEachIndexed { index, row ->
                    key(row.key) {
                        val actions = props.columns.associate { it.key to props.cellAction(row.key, it.key) }
                        val rowAction = props.onRowActivate?.takeIf { actions.values.none { action -> action != null } }
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .semantics {
                                    collectionItemInfo =
                                        CollectionItemInfo(index + 1, 1, 0, props.columns.size.coerceAtLeast(1))
                                    row.accessibleLabel?.let { contentDescription = it }
                                    selected = row.selected
                                }.then(if (rowAction != null) Modifier.clickable { rowAction(row.key) } else Modifier),
                            verticalArrangement = Arrangement.spacedBy(props.sizePreset.nativeGap()),
                        ) {
                            if (props.sizePreset == DomposableDataTableSize.Compact) {
                                props.columns.forEach { column ->
                                    NativeDataTableCell(
                                        props,
                                        row,
                                        column,
                                        actions[column.key],
                                        Modifier.fillMaxWidth(),
                                        compact = true,
                                    )
                                }
                            } else {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    props.columns.forEach { column ->
                                        NativeDataTableCell(
                                            props,
                                            row,
                                            column,
                                            actions[column.key],
                                            Modifier.weight(1f),
                                            compact = false,
                                        )
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

@Composable
private fun NativeDataTableCell(
    props: DomposableDataTableProps,
    row: DomposableDataTableRow,
    column: DomposableDataTableColumn,
    action: (() -> Unit)?,
    modifier: Modifier,
    compact: Boolean,
) {
    Column(
        modifier
            .padding(8.dp)
            .then(if (action != null) Modifier.clickable(onClick = action) else Modifier)
            .semantics {
                if (column.key == props.rowHeaderColumnKey) heading()
                contentDescription = column.accessibleLabel
            },
    ) {
        if (compact) Text(column.label, style = MaterialTheme.typography.labelMedium)
        props.cellContent(row, column)
    }
}

private fun DomposableDataTableSize.nativeGap() =
    when (this) {
        DomposableDataTableSize.Compact -> NATIVE_TABLE_COMPACT_GAP
        DomposableDataTableSize.Standard -> NATIVE_TABLE_STANDARD_GAP
        DomposableDataTableSize.Expanded -> NATIVE_TABLE_EXPANDED_GAP
    }

private fun DomposableChartSeries.nativeColor(
    primary: Long,
    secondary: Long,
    tertiary: Long,
    error: Long,
) = color?.let { Color(it.argb) } ?: Color(
    when (tone) {
        DomposableChartTone.Primary, DomposableChartTone.Success, DomposableChartTone.Info -> primary
        DomposableChartTone.Secondary -> secondary
        DomposableChartTone.Tertiary, DomposableChartTone.Warning -> tertiary
        DomposableChartTone.Error -> error
    },
)

private fun chartX(
    index: Int,
    count: Int,
    inset: Float,
    width: Float,
): Float = if (count <= 1) inset + width / 2f else inset + width * index.coerceIn(0, count - 1) / (count - 1)

private fun chartY(
    value: Float,
    range: DomposableChartValueRange,
    inset: Float,
    height: Float,
): Float {
    val fraction = ((range.maximum - value) / (range.maximum - range.minimum)).toFloat().coerceIn(0f, 1f)
    return inset + fraction * height
}

private fun selectNativeChartIndex(
    x: Float,
    width: Float,
    count: Int,
    onSelected: (Int) -> Unit,
) {
    if (count <= 0 || width <= 0f) return
    onSelected((x / width * (count - 1)).roundToInt().coerceIn(0, count - 1))
}

private fun DomposableTextAlignment.nativeTextAlign() =
    when (this) {
        DomposableTextAlignment.Start -> TextAlign.Start
        DomposableTextAlignment.Center -> TextAlign.Center
        DomposableTextAlignment.End -> TextAlign.End
        DomposableTextAlignment.Justify -> TextAlign.Justify
    }
