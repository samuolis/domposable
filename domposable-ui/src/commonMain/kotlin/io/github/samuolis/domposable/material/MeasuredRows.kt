package io.github.samuolis.domposable.material

import kotlin.math.abs

private const val MEASUREMENT_EPSILON = 0.5

/** Prefix sums provide O(log n) measurement updates and offset lookup. Heights follow stable keys. */
internal class MeasuredRows(
    val keys: List<String>,
    private val estimate: Double,
    private val gap: Double,
    private val measured: MutableMap<String, Double>,
) {
    private val tree = DoubleArray(keys.size + 1)
    private val heights = DoubleArray(keys.size) { measured[keys[it]] ?: estimate }

    init {
        require(keys.distinct().size == keys.size)
        require(estimate.isFinite() && estimate > 0 && gap.isFinite() && gap >= 0)
        keys.indices.forEach { add(it, heights[it] + gap) }
    }

    val extent: Double get() = (offset(keys.size) - if (keys.isEmpty()) 0.0 else gap).coerceAtLeast(0.0)

    fun height(index: Int): Double = heights[index]

    fun update(
        index: Int,
        height: Double,
    ): Boolean {
        require(height.isFinite() && height > 0)
        if (abs(heights[index] - height) < MEASUREMENT_EPSILON) return false
        val difference = height - heights[index]
        heights[index] = height
        measured[keys[index]] = height
        add(index, difference)
        return true
    }

    fun offset(index: Int): Double {
        require(index in 0..keys.size)
        var cursor = index
        var total = 0.0
        while (cursor > 0) {
            total += tree[cursor]
            cursor -= cursor and -cursor
        }
        return total
    }

    fun indexAt(offset: Double): Int {
        require(offset.isFinite())
        if (keys.isEmpty()) return 0
        val target = offset.coerceAtLeast(0.0)
        var cursor = 0
        var accumulated = 0.0
        var step = 1
        while (step <= keys.size / 2) step *= 2
        while (step > 0) {
            val next = cursor + step
            if (next <= keys.size && accumulated + tree[next] <= target) {
                cursor = next
                accumulated += tree[next]
            }
            step /= 2
        }
        return cursor.coerceAtMost(keys.lastIndex)
    }

    fun range(
        offset: Double,
        viewport: Double,
        overscan: Int,
    ): IntRange {
        require(viewport.isFinite() && viewport > 0 && overscan > 0)
        if (keys.isEmpty()) return IntRange.EMPTY
        val clamped = offset.coerceIn(0.0, (extent - viewport).coerceAtLeast(0.0))
        val first = (indexAt(clamped).toLong() - overscan).coerceAtLeast(0).toInt()
        val end = (indexAt(clamped + viewport).toLong() + overscan + 1).coerceAtMost(keys.size.toLong()).toInt()
        return first until end
    }

    private fun add(
        index: Int,
        delta: Double,
    ) {
        var cursor = index + 1
        while (cursor < tree.size) {
            tree[cursor] += delta
            cursor += cursor and -cursor
        }
    }
}
