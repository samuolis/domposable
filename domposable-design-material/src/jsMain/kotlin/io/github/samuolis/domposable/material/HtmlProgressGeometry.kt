package io.github.samuolis.domposable.material

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

private const val CIRCULAR_WAVE_SAMPLES = 96
private const val LINEAR_WAVELENGTH = 40.0
private const val LINEAR_AMPLITUDE = 3.0
private const val SAMPLE_INTERVAL = 2.0

internal fun circularWavePath(): String =
    buildString {
        repeat(CIRCULAR_WAVE_SAMPLES + 1) { index ->
            val angle = 2.0 * PI * index / CIRCULAR_WAVE_SAMPLES
            val radius = 42.5 + (10.0 / 3.0) * sin(angle * 8.0)
            val x = 50.0 + radius * cos(angle)
            val y = 50.0 + radius * sin(angle)
            append(if (index == 0) "M" else "L")
            append(x)
            append(',')
            append(y)
        }
        append('Z')
    }

internal fun linearWavePath(
    width: Double,
    height: Double,
    progress: Float?,
): String =
    buildString {
        val samples = ceil(width / SAMPLE_INTERVAL).toInt().coerceAtLeast(1)
        val amplitude = if (progress != null && (progress <= 0.1f || progress >= 0.95f)) 0.0 else LINEAR_AMPLITUDE
        repeat(samples + 1) { index ->
            val x = width * index / samples
            val y = height / 2.0 + amplitude * sin(x * 2.0 * PI / LINEAR_WAVELENGTH)
            append(if (index == 0) "M" else "L")
            append(x)
            append(',')
            append(y)
        }
    }

private const val DETERMINATE_VERTEX_COUNT = 20
private const val DETERMINATE_OUTER_RADIUS = 42.0
private const val DETERMINATE_PETAL_DEPTH = 14.0

internal fun determinateLoadingPath(progress: Float): String {
    val points =
        (0 until DETERMINATE_VERTEX_COUNT).map { index ->
            val angle = 2.0 * PI * index / DETERMINATE_VERTEX_COUNT - PI / 2.0
            val radius =
                if (index % 2 == 0) {
                    DETERMINATE_OUTER_RADIUS
                } else {
                    DETERMINATE_OUTER_RADIUS - DETERMINATE_PETAL_DEPTH * progress
                }
            (50.0 + radius * cos(angle)) to (50.0 + radius * sin(angle))
        }
    val firstMidpoint = midpoint(points.last(), points.first())
    return buildString {
        append("M${firstMidpoint.first},${firstMidpoint.second}")
        points.forEachIndexed { index, point ->
            val next = points[(index + 1) % points.size]
            val middle = midpoint(point, next)
            append(" Q${point.first},${point.second} ${middle.first},${middle.second}")
        }
        append(" Z")
    }
}

private fun midpoint(
    first: Pair<Double, Double>,
    second: Pair<Double, Double>,
): Pair<Double, Double> = ((first.first + second.first) / 2.0) to ((first.second + second.second) / 2.0)
