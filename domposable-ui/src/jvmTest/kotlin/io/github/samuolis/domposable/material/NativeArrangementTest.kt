package io.github.samuolis.domposable.material

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.ui.DomposableArrangement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NativeArrangementTest {
    @Test
    fun distributedMixedSizesPreserveLogicalOrderAndAvailableSpace() {
        val sizes = intArrayOf(20, 40, 10)
        val density = Density(1f)
        val arrangement = DomposableArrangement.SpaceBetween.horizontal(12.dp)
        listOf(
            LayoutDirection.Ltr to listOf(0, 35, 90),
            LayoutDirection.Rtl to listOf(80, 25, 0),
        ).forEach { (direction, expected) ->
            val positions = IntArray(sizes.size)
            with(arrangement) { with(density) { arrange(100, sizes, direction, positions) } }
            assertEquals(expected, positions.toList())
        }
    }

    @Test
    fun distributedLayoutsKeepTheirMinimumGapWhenSpaceIsTight() {
        val sizes = intArrayOf(30, 30, 30)
        val density = Density(1f)
        listOf(
            DomposableArrangement.SpaceBetween,
            DomposableArrangement.SpaceAround,
            DomposableArrangement.SpaceEvenly,
        ).forEach { arrangement ->
            LayoutDirection.entries.forEach { direction ->
                val positions = IntArray(sizes.size)
                val horizontal = arrangement.horizontal(12.dp)
                with(horizontal) { with(density) { arrange(110, sizes, direction, positions) } }
                assertEquals(12.dp, horizontal.spacing)
                positions.sorted().zipWithNext().forEach { (first, second) ->
                    assertTrue(second - first >= 42, "$arrangement $direction: ${positions.toList()}")
                }
            }
            val vertical = arrangement.vertical(12.dp)
            val positions = IntArray(sizes.size)
            with(vertical) { with(density) { arrange(110, sizes, positions) } }
            assertEquals(12.dp, vertical.spacing)
            positions.toList().zipWithNext().forEach { (first, second) ->
                assertTrue(second - first >= 42, "$arrangement: ${positions.toList()}")
            }
        }
    }
}
