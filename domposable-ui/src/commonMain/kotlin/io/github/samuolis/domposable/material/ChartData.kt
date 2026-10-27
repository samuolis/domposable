package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.ui.DomposableChartSeries
import io.github.samuolis.domposable.ui.DomposableChartSize
import io.github.samuolis.domposable.ui.DomposableChartTone
import io.github.samuolis.domposable.ui.DomposableChartValue
import kotlin.math.roundToInt

internal const val DOMPOSABLE_CHART_THUMBNAIL_HEIGHT = 108
internal const val DOMPOSABLE_CHART_COMPACT_HEIGHT = 152
internal const val DOMPOSABLE_CHART_STANDARD_HEIGHT = 208
internal const val DOMPOSABLE_CHART_EXPANDED_HEIGHT = 264
internal const val DOMPOSABLE_CHART_GRID_LINE_COUNT = 3
internal const val DOMPOSABLE_CHART_GRID_DIVISION_COUNT = DOMPOSABLE_CHART_GRID_LINE_COUNT - 1
internal const val DOMPOSABLE_CHART_MINIMUM_RANGE = 1.0
internal const val DOMPOSABLE_CHART_RANGE_PADDING = 0.1
internal const val DOMPOSABLE_CHART_ABSOLUTE_PADDING = 0.05
internal const val DOMPOSABLE_CHART_EPSILON = 0.001

internal data class DomposableChartDataPoint(
    val series: DomposableChartSeries,
    val value: DomposableChartValue,
)

internal data class DomposableChartValueRange(
    val minimum: Double,
    val maximum: Double,
)

internal fun DomposableChartSize.materialHeight(): Int =
    when (this) {
        DomposableChartSize.Thumbnail -> DOMPOSABLE_CHART_THUMBNAIL_HEIGHT
        DomposableChartSize.Compact -> DOMPOSABLE_CHART_COMPACT_HEIGHT
        DomposableChartSize.Standard -> DOMPOSABLE_CHART_STANDARD_HEIGHT
        DomposableChartSize.Expanded -> DOMPOSABLE_CHART_EXPANDED_HEIGHT
    }

internal fun chartPointCount(
    series: List<DomposableChartSeries>,
    labels: List<String>?,
): Int {
    val explicitCount = series.flatMap { it.values }.maxOfOrNull { (it.axisIndex ?: -1) + 1 } ?: 0
    return maxOf(labels?.size ?: 0, explicitCount, series.maxOfOrNull { it.values.size } ?: 0, 1)
}

/** [centerSingleton] retains the HTML-centered and native-start singleton policies. */
internal fun chartPointIndex(
    point: DomposableChartValue,
    localIndex: Int,
    localCount: Int,
    globalCount: Int,
    centerSingleton: Boolean = false,
): Int =
    point.axisIndex ?: if (localCount <= 1 || globalCount <= 1) {
        if (centerSingleton && globalCount > 1) (globalCount - 1) / DOMPOSABLE_CHART_GRID_DIVISION_COUNT else 0
    } else {
        (localIndex.toDouble() / (localCount - 1) * (globalCount - 1)).roundToInt()
    }

internal fun chartPointsAt(
    series: List<DomposableChartSeries>,
    axisIndex: Int,
    pointCount: Int,
    centerSingleton: Boolean = false,
): List<DomposableChartDataPoint> =
    series.mapNotNull { item ->
        item.values
            .mapIndexedNotNull { localIndex, point ->
                point
                    .takeIf {
                        chartPointIndex(it, localIndex, item.values.size, pointCount, centerSingleton) == axisIndex
                    }?.let { DomposableChartDataPoint(item, it) }
            }.firstOrNull()
    }

internal fun chartAxisLabel(
    series: List<DomposableChartSeries>,
    labels: List<String>?,
    index: Int,
    pointCount: Int,
    centerSingleton: Boolean = false,
): String? =
    labels?.getOrNull(index)
        ?: chartPointsAt(series, index, pointCount, centerSingleton).firstOrNull()?.value?.label

internal fun chartValueRange(series: List<DomposableChartSeries>): DomposableChartValueRange {
    val values = series.flatMap { it.values }.map { it.value.toDouble() }
    val minimum = values.minOrNull() ?: 0.0
    val maximum = values.maxOrNull() ?: minimum
    val span = (maximum - minimum).coerceAtLeast(DOMPOSABLE_CHART_MINIMUM_RANGE)
    val padding =
        maxOf(
            span * DOMPOSABLE_CHART_RANGE_PADDING,
            maxOf(kotlin.math.abs(minimum), kotlin.math.abs(maximum)) * DOMPOSABLE_CHART_ABSOLUTE_PADDING,
            DOMPOSABLE_CHART_EPSILON,
        )
    return DomposableChartValueRange(minimum - padding, maximum + padding)
}

internal fun chartColor(
    series: DomposableChartSeries,
    primary: DomposableColor,
    secondary: DomposableColor,
    tertiary: DomposableColor,
    error: DomposableColor,
): String =
    series.color?.css() ?: when (series.tone) {
        DomposableChartTone.Primary,
        DomposableChartTone.Success,
        DomposableChartTone.Info,
        -> primary.css()

        DomposableChartTone.Secondary -> secondary.css()

        DomposableChartTone.Tertiary,
        DomposableChartTone.Warning,
        -> tertiary.css()

        DomposableChartTone.Error -> error.css()
    }

internal fun chartTickIndices(count: Int): List<Int> =
    when {
        count <= 0 -> emptyList()
        count == 1 -> listOf(0)
        count == 2 -> listOf(0, 1)
        else -> listOf(0, (count - 1) / DOMPOSABLE_CHART_GRID_DIVISION_COUNT, count - 1)
    }
