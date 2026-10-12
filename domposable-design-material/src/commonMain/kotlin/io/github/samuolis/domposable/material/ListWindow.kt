package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.ui.DomposableListWindow
import kotlin.math.ceil
import kotlin.math.floor

internal fun listExtent(
    count: Int,
    window: DomposableListWindow,
    gap: DomposableLength,
): Double {
    require(count >= 0)
    require(gap.value.isFinite() && gap.value >= 0f)
    return if (count == 0) 0.0 else count * window.rowHeight.value.toDouble() + (count - 1) * gap.value.toDouble()
}

internal fun listWindowRange(
    count: Int,
    window: DomposableListWindow,
    gap: DomposableLength,
    offset: Double,
): IntRange {
    val extent = listExtent(count, window, gap)
    require(offset.isFinite())
    if (count == 0) return IntRange.EMPTY
    val step = window.rowHeight.value.toDouble() + gap.value
    val clamped = offset.coerceIn(0.0, (extent - window.viewportHeight.value).coerceAtLeast(0.0))
    val first = (floor(clamped / step).toLong() - window.overscan).coerceAtLeast(0L)
    val end =
        (
            ceil(
                (clamped + window.viewportHeight.value) / step,
            ).toLong() + window.overscan
        ).coerceAtMost(count.toLong())
    return first.toInt() until end.toInt()
}
