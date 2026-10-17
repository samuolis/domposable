package io.github.samuolis.domposable.material

import org.jetbrains.compose.web.css.StyleScope

internal fun StyleScope.sliderMarks(
    steps: Int,
    endPercent: Float,
    startPercent: Float? = null,
    vertical: Boolean = false,
    reversed: Boolean = false,
) {
    val fractions =
        if (steps == 0) {
            if (startPercent == null) listOf(1f) else listOf(0f, 1f)
        } else {
            (0..steps + 1).map { it.toFloat() / (steps + 1) }
        }
    val gradients =
        fractions
            .filterNot { fraction ->
                fraction * SliderPercentScale == endPercent ||
                    fraction * SliderPercentScale == startPercent
            }.map { fraction ->
                val percent = fraction * SliderPercentScale
                val active = percent <= endPercent && (startPercent == null || percent >= startPercent)
                val color =
                    if (active) "var(--d-slider-active-tick-current)" else "var(--d-slider-inactive-tick-current)"
                val position = "calc(8px + (100% - 16px) * ${if (reversed) 1f - fraction else fraction})"
                val center = if (vertical) "50% $position" else "$position 50%"
                "radial-gradient(circle 2px at $center,$color 98%,transparent 100%)"
            }
    property("--d-slider-marks", gradients.joinToString(",").ifEmpty { "linear-gradient(transparent,transparent)" })
}

private const val SliderPercentScale = 100f
