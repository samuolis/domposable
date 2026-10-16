package io.github.samuolis.domposable.material

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val CIRCULAR_WAVE_SAMPLES = 96
private const val LINEAR_WAVE_SAMPLES = 80

internal fun circularWavePath(): String =
    buildString {
        repeat(CIRCULAR_WAVE_SAMPLES + 1) { index ->
            val angle = 2.0 * PI * index / CIRCULAR_WAVE_SAMPLES
            val radius = 42.0 + 2.2 * sin(angle * 8.0)
            val x = 50.0 + radius * cos(angle)
            val y = 50.0 + radius * sin(angle)
            append(if (index == 0) "M" else "L")
            append(x)
            append(',')
            append(y)
        }
        append('Z')
    }

internal fun linearWavePath(): String =
    buildString {
        repeat(LINEAR_WAVE_SAMPLES + 1) { index ->
            val x = 100.0 * index / LINEAR_WAVE_SAMPLES
            val y = 6.0 + 2.1 * sin(x * 2.0 * PI / 25.0)
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

internal fun progressRing(value: Float?): String {
    val angle = (value ?: 0.25f).coerceIn(0f, 1f) * 360f
    return "conic-gradient(var(--d-progress-color,var(--d-primary)) ${angle}deg," +
        "var(--d-progress-track,var(--d-variant)) 0deg)"
}

internal fun progressMask(stroke: Float): String =
    "radial-gradient(farthest-side,transparent calc(100% - ${stroke}px)," +
        "#000 calc(100% - ${stroke}px + 1px))"
