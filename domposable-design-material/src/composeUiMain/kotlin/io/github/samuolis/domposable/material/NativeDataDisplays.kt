package io.github.samuolis.domposable.material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableChartAppearance
import io.github.samuolis.domposable.foundation.DomposableInteractionAppearance
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
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.lineChart
    val gap =
        when (props.sizePreset) {
            DomposableChartSize.Thumbnail, DomposableChartSize.Compact -> 8.dp
            DomposableChartSize.Standard, DomposableChartSize.Expanded -> 12.dp
        }
    Column(
        modifier
            .toComposeModifier()
            .fillMaxWidth()
            .then(appearance.background?.let { Modifier.background(it.nativeColor()) } ?: Modifier),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        if (props.displayTitle) {
            props.title?.let {
                Text(it, style = appearance.titleAppearance.nativeTextStyle(MaterialTheme.typography.titleMedium))
            }
        }
        props.description?.let {
            Text(
                it,
                style = appearance.labelAppearance.nativeTextStyle(MaterialTheme.typography.bodyMedium),
            )
        }
        if (props.series.any { it.values.isNotEmpty() }) {
            NativeLineChartPlot(props, appearance)
        }
    }
}

@Composable
private fun NativeLineChartPlot(
    props: DomposableLineChartProps,
    appearance: DomposableChartAppearance,
) {
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
        val gridColor =
            (
                appearance.grid?.nativeColor() ?: Color(
                    tokens.colors.outline.argb,
                )
            ).copy(alpha = NATIVE_CHART_GRID_ALPHA)
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
                (
                    appearance.selection?.nativeColor() ?: Color(
                        tokens.colors.primary.argb,
                    )
                ).copy(alpha = NATIVE_CHART_SELECTION_ALPHA),
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
                    Text(
                        label,
                        color = appearance.axis?.nativeColor() ?: LocalContentColor.current,
                        style = appearance.labelAppearance.nativeTextStyle(MaterialTheme.typography.labelSmall),
                        maxLines = 1,
                    )
                }
            }
        }
    }
    if (selectedPoints.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            selectedPoints.forEach { point ->
                Text(
                    "${point.value.label}: ${point.series.label} ${props.yAxisFormatter(point.value.value)}",
                    style = appearance.labelAppearance.nativeTextStyle(MaterialTheme.typography.bodyMedium),
                )
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
    val appearance = props.appearance ?: tokens.components.valueChart
    val maximum =
        props.series
            .flatMap { it.values }
            .maxOfOrNull { abs(it.value) }
            ?.coerceAtLeast(0.001f) ?: 1f
    val gap = if (props.sizePreset == DomposableChartSize.Compact) 8.dp else 12.dp
    Column(
        modifier
            .toComposeModifier()
            .fillMaxWidth()
            .then(appearance.background?.let { Modifier.background(it.nativeColor()) } ?: Modifier),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        Text(props.title, style = appearance.titleAppearance.nativeTextStyle(MaterialTheme.typography.titleMedium))
        Text(
            props.description,
            style = appearance.labelAppearance.nativeTextStyle(MaterialTheme.typography.bodyMedium),
        )
        Text(
            props.dataTableLabel,
            modifier = Modifier.semantics { heading() },
            style = appearance.titleAppearance.nativeTextStyle(MaterialTheme.typography.titleSmall),
        )
        props.series.forEach { series ->
            key(series.key) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            series.label,
                            style = appearance.labelAppearance.nativeTextStyle(MaterialTheme.typography.bodyMedium),
                        )
                        series.currentValue?.let {
                            Text(
                                props.formatValue(it),
                                style = appearance.labelAppearance.nativeTextStyle(MaterialTheme.typography.bodyMedium),
                            )
                        }
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
                                Text(
                                    point.label,
                                    Modifier.weight(1f),
                                    style =
                                        appearance.labelAppearance.nativeTextStyle(
                                            MaterialTheme.typography.labelMedium,
                                        ),
                                )
                                LinearProgressIndicator(
                                    progress = { (abs(point.value) / maximum).coerceIn(0f, 1f) },
                                    modifier = Modifier.weight(1f),
                                    color = color,
                                    trackColor =
                                        appearance.grid?.nativeColor() ?: Color(tokens.colors.surfaceVariant.argb),
                                )
                                Text(
                                    props.formatValue(point.value),
                                    style =
                                        appearance.labelAppearance.nativeTextStyle(
                                            MaterialTheme.typography.labelMedium,
                                        ),
                                )
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
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.dataTable
    val scroll = rememberScrollState()
    BoxWithConstraints(modifier.toComposeModifier().fillMaxWidth()) {
        val naturalWidth = (props.columns.size.coerceAtLeast(1) * 160).dp
        val tableWidth = if (props.allowHorizontalOverflow) maxOf(maxWidth, naturalWidth) else maxWidth
        val rowModifier =
            if (props.allowHorizontalOverflow) {
                Modifier.fillMaxWidth().horizontalScroll(scroll)
            } else {
                Modifier.fillMaxWidth()
            }
        Row(rowModifier) {
            Column(
                Modifier.width(tableWidth).semantics {
                    collectionInfo = CollectionInfo(props.rows.size + 1, props.columns.size)
                },
                verticalArrangement =
                    Arrangement.spacedBy(appearance.rowSpacing?.value?.dp ?: props.sizePreset.nativeGap()),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(appearance.headerContainer?.nativeColor() ?: Color.Transparent)
                        .drawBottomBorder(appearance.border?.nativeColor())
                        .semantics {
                            collectionItemInfo = CollectionItemInfo(0, 1, 0, props.columns.size)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(appearance.columnSpacing?.value?.dp ?: 0.dp),
                ) {
                    props.columns.forEach { column ->
                        key(column.key) {
                            Text(
                                column.label,
                                Modifier
                                    .weight(
                                        1f,
                                    ).padding(appearance.cellPadding?.nativePadding() ?: PaddingValues(8.dp))
                                    .semantics {
                                        heading()
                                        contentDescription = column.accessibleLabel
                                    },
                                textAlign = column.alignment.nativeTextAlign(),
                                color = appearance.headerContent?.nativeColor() ?: LocalContentColor.current,
                                style =
                                    appearance.headerTextAppearance.nativeTextStyle(
                                        appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
                                    ),
                            )
                        }
                    }
                }
                props.rows.forEachIndexed { index, row ->
                    key(row.key) {
                        val actions = props.columns.associate { it.key to props.cellAction(row.key, it.key) }
                        val rowAction = props.onRowActivate?.takeIf { actions.values.none { action -> action != null } }
                        val compactSurface =
                            props.sizePreset
                                .takeIf { it == DomposableDataTableSize.Compact }
                                ?.let { appearance.compactRowAppearance }
                        val interaction = appearance.rowInteraction
                        val interactionSource = remember(row.key) { MutableInteractionSource() }
                        val pressed =
                            rowAction != null && interaction != null &&
                                interactionSource.collectIsPressedAsState().value
                        val hovered =
                            rowAction != null && interaction != null &&
                                interactionSource.collectIsHoveredAsState().value
                        val focused =
                            rowAction != null && interaction != null &&
                                interactionSource.collectIsFocusedAsState().value
                        val rowShape = compactSurface?.shape?.nativeShape() ?: MaterialTheme.shapes.medium
                        val enabled = rowAction != null
                        val baseContainer =
                            (if (row.selected) appearance.selectedContainer else appearance.rowContainer)?.nativeColor()
                        val compactSurfaceContainer =
                            compactSurface?.let { surface ->
                                surface.container?.nativeColor()
                                    ?: surface.tonalElevation?.let {
                                        MaterialTheme.colorScheme.surfaceColorAtElevation(it.value.dp)
                                    }
                            }
                        val compactContainer =
                            if (row.selected) {
                                appearance.selectedContainer?.nativeColor() ?: compactSurfaceContainer
                            } else {
                                compactSurfaceContainer
                            }
                        val rowContainer =
                            when {
                                !enabled -> {
                                    compactContainer ?: baseContainer
                                }

                                pressed -> {
                                    interaction?.pressedContainer?.nativeColor() ?: compactContainer
                                        ?: baseContainer
                                }

                                hovered -> {
                                    interaction?.hoveredContainer?.nativeColor() ?: compactContainer
                                        ?: baseContainer
                                }

                                focused -> {
                                    compactContainer ?: baseContainer
                                }

                                else -> {
                                    compactContainer ?: baseContainer
                                }
                            }
                        val baseContent =
                            if (row.selected) {
                                appearance.selectedContent ?: appearance.rowContent
                            } else {
                                appearance.rowContent
                            }
                        val rowContent =
                            when {
                                enabled && pressed -> {
                                    interaction?.pressedContent ?: baseContent
                                        ?: compactSurface?.content
                                }

                                enabled && hovered -> {
                                    interaction?.hoveredContent ?: baseContent
                                        ?: compactSurface?.content
                                }

                                else -> {
                                    baseContent ?: compactSurface?.content
                                }
                            }
                        val stateLayer = interaction?.stateLayerOpacity
                        val resolvedContainer =
                            when {
                                !enabled || interaction == null -> {
                                    rowContainer
                                }

                                pressed -> {
                                    interaction.pressedContainer?.nativeColor()
                                        ?: stateLayer?.let {
                                            (rowContent?.nativeColor() ?: LocalContentColor.current)
                                                .copy(alpha = it.pressed)
                                                .compositeOver(rowContainer ?: Color.Transparent)
                                        }
                                        ?: rowContainer
                                }

                                hovered -> {
                                    interaction.hoveredContainer?.nativeColor()
                                        ?: stateLayer?.let {
                                            (rowContent?.nativeColor() ?: LocalContentColor.current)
                                                .copy(alpha = it.hovered)
                                                .compositeOver(rowContainer ?: Color.Transparent)
                                        }
                                        ?: rowContainer
                                }

                                focused -> {
                                    stateLayer?.let {
                                        (rowContent?.nativeColor() ?: LocalContentColor.current)
                                            .copy(alpha = it.focused)
                                            .compositeOver(rowContainer ?: Color.Transparent)
                                    } ?: rowContainer
                                }

                                else -> {
                                    rowContainer
                                }
                            }
                        val focusRing = interaction?.takeIf { enabled && focused }
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .drawButtonFocusRing(
                                    focused && enabled,
                                    focusRing?.focusRingColor?.nativeColor()
                                        ?: if (focusRing != null) MaterialTheme.colorScheme.primary else null,
                                    focusRing?.focusRingWidth?.value ?: if (focusRing != null) 3f else null,
                                    focusRing?.focusRingOffset?.value ?: if (focusRing != null) 3f else null,
                                    rowShape,
                                ).then(
                                    if (compactSurface != null) {
                                        Modifier
                                            .shadow(compactSurface.shadowElevation?.value?.dp ?: 0.dp, rowShape)
                                            .background(resolvedContainer ?: Color.Transparent, rowShape)
                                            .clip(rowShape)
                                            .then(
                                                (if (row.selected) appearance.selectedBorder else compactSurface.border)
                                                    ?.let {
                                                        Modifier.border(
                                                            BorderStroke(
                                                                (
                                                                    if (row.selected) {
                                                                        appearance.selectedBorderWidth
                                                                    } else {
                                                                        null
                                                                    }
                                                                )?.value?.dp
                                                                    ?: compactSurface.borderWidth.value.dp,
                                                                it.nativeColor(),
                                                            ),
                                                            rowShape,
                                                        )
                                                    } ?: Modifier,
                                            )
                                    } else {
                                        resolvedContainer?.let { Modifier.background(it) } ?: Modifier
                                    },
                                ).then(
                                    if (compactSurface == null) {
                                        Modifier
                                            .then(
                                                appearance.selectedBorder
                                                    ?.takeIf { row.selected }
                                                    ?.let { selectedBorder ->
                                                        Modifier.border(
                                                            BorderStroke(
                                                                appearance.selectedBorderWidth?.value?.dp ?: 2.dp,
                                                                selectedBorder.nativeColor(),
                                                            ),
                                                            rowShape,
                                                        )
                                                    } ?: Modifier,
                                            ).drawBottomBorder(appearance.border?.nativeColor())
                                    } else {
                                        Modifier
                                    },
                                ).semantics {
                                    collectionItemInfo =
                                        CollectionItemInfo(index + 1, 1, 0, props.columns.size.coerceAtLeast(1))
                                    row.accessibleLabel?.let { contentDescription = it }
                                    selected = row.selected
                                }.then(
                                    if (rowAction == null) {
                                        Modifier
                                    } else if (interaction?.hasTableStateFeedback() != true) {
                                        Modifier.clickable(
                                            interactionSource = interactionSource,
                                            role = Role.Button,
                                        ) { rowAction(row.key) }
                                    } else {
                                        Modifier.clickable(
                                            interactionSource = interactionSource,
                                            indication = null,
                                            role = Role.Button,
                                        ) { rowAction(row.key) }
                                    },
                                ),
                            verticalArrangement = Arrangement.spacedBy(props.sizePreset.nativeGap()),
                        ) {
                            CompositionLocalProvider(
                                LocalTextStyle provides
                                    (compactSurface?.textAppearance ?: appearance.textAppearance)
                                        .nativeTextStyle(LocalTextStyle.current),
                                LocalContentColor provides (rowContent?.nativeColor() ?: LocalContentColor.current),
                            ) {
                                if (props.sizePreset == DomposableDataTableSize.Compact) {
                                    val compactCells: @Composable () -> Unit = {
                                        props.columns.forEach { column ->
                                            NativeDataTableCell(
                                                props,
                                                row,
                                                column,
                                                actions[column.key],
                                                Modifier.fillMaxWidth(),
                                                compact = true,
                                                compactCard = compactSurface != null,
                                            )
                                        }
                                    }
                                    if (compactSurface != null) {
                                        Column(Modifier.padding(16.dp)) { compactCells() }
                                    } else {
                                        compactCells()
                                    }
                                } else {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                            Arrangement.spacedBy(appearance.columnSpacing?.value?.dp ?: 0.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        props.columns.forEach { column ->
                                            NativeDataTableCell(
                                                props,
                                                row,
                                                column,
                                                actions[column.key],
                                                Modifier.weight(1f),
                                                compact = false,
                                                compactCard = false,
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
}

private fun DomposableInteractionAppearance.hasTableStateFeedback(): Boolean =
    stateLayerOpacity != null ||
        hoveredContainer != null ||
        hoveredContent != null ||
        pressedContainer != null ||
        pressedContent != null

@Composable
private fun NativeDataTableCell(
    props: DomposableDataTableProps,
    row: DomposableDataTableRow,
    column: DomposableDataTableColumn,
    action: (() -> Unit)?,
    modifier: Modifier,
    compact: Boolean,
    compactCard: Boolean,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.dataTable
    val cellPadding =
        if (compact && compactCard) {
            PaddingValues(0.dp)
        } else {
            appearance.cellPadding?.nativePadding() ?: PaddingValues(8.dp)
        }
    val compactTextAppearance =
        if (compact && compactCard) appearance.compactRowAppearance?.textAppearance else null
    val cellModifier =
        modifier
            .padding(cellPadding)
            .then(if (action != null) Modifier.clickable(onClick = action) else Modifier)
            .semantics {
                if (column.key == props.rowHeaderColumnKey) heading()
                contentDescription = column.accessibleLabel
            }
    if (compact) {
        Row(
            cellModifier,
            horizontalArrangement =
                Arrangement.spacedBy(appearance.columnSpacing?.value?.dp ?: 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                column.label,
                Modifier.weight(1f).widthIn(min = appearance.compactLabelMinWidth.value.dp),
                color = appearance.headerContent?.nativeColor() ?: LocalContentColor.current,
                style =
                    appearance.headerTextAppearance.nativeTextStyle(
                        compactTextAppearance.nativeTextStyle(MaterialTheme.typography.labelMedium),
                    ),
            )
            Box(
                Modifier.weight(2f),
                contentAlignment = column.alignment.nativeContentAlignment(),
            ) {
                props.cellContent(row, column)
            }
        }
    } else {
        Box(cellModifier, contentAlignment = column.alignment.nativeContentAlignment()) {
            props.cellContent(row, column)
        }
    }
}

private fun DomposableTextAlignment.nativeContentAlignment() =
    when (this) {
        DomposableTextAlignment.Start, DomposableTextAlignment.Justify -> Alignment.CenterStart
        DomposableTextAlignment.Center -> Alignment.Center
        DomposableTextAlignment.End -> Alignment.CenterEnd
    }

private fun DomposableDataTableSize.nativeGap() =
    when (this) {
        DomposableDataTableSize.Compact -> NATIVE_TABLE_COMPACT_GAP
        DomposableDataTableSize.Standard -> NATIVE_TABLE_STANDARD_GAP
        DomposableDataTableSize.Expanded -> NATIVE_TABLE_EXPANDED_GAP
    }

private fun Modifier.drawBottomBorder(color: Color?): Modifier =
    if (color == null) {
        this
    } else {
        drawBehind {
            drawLine(color, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
        }
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
