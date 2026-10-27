package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.ui.DomposableArrangement

internal fun DomposableArrangement.horizontal(gap: Dp): Arrangement.Horizontal {
    val distribution =
        when (this) {
            DomposableArrangement.Start -> return Arrangement.spacedBy(gap)
            DomposableArrangement.Center -> return Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
            DomposableArrangement.End -> return Arrangement.spacedBy(gap, Alignment.End)
            DomposableArrangement.SpaceBetween -> Arrangement.SpaceBetween
            DomposableArrangement.SpaceAround -> Arrangement.SpaceAround
            DomposableArrangement.SpaceEvenly -> Arrangement.SpaceEvenly
        }
    if (gap == 0.dp) return distribution
    return object : Arrangement.Horizontal {
        override val spacing = gap

        override fun Density.arrange(
            totalSize: Int,
            sizes: IntArray,
            layoutDirection: LayoutDirection,
            outPositions: IntArray,
        ) {
            val pixels = gap.roundToPx()
            with(distribution) {
                arrange(
                    (totalSize - pixels * (sizes.size - 1).coerceAtLeast(0)).coerceAtLeast(sizes.sum()),
                    sizes,
                    layoutDirection,
                    outPositions,
                )
            }
            outPositions.indices.forEach { index ->
                val offset = if (layoutDirection == LayoutDirection.Rtl) sizes.lastIndex - index else index
                outPositions[index] += pixels * offset
            }
        }
    }
}

internal fun DomposableArrangement.vertical(gap: Dp): Arrangement.Vertical {
    val distribution =
        when (this) {
            DomposableArrangement.Start -> return Arrangement.spacedBy(gap)
            DomposableArrangement.Center -> return Arrangement.spacedBy(gap, Alignment.CenterVertically)
            DomposableArrangement.End -> return Arrangement.spacedBy(gap, Alignment.Bottom)
            DomposableArrangement.SpaceBetween -> Arrangement.SpaceBetween
            DomposableArrangement.SpaceAround -> Arrangement.SpaceAround
            DomposableArrangement.SpaceEvenly -> Arrangement.SpaceEvenly
        }
    if (gap == 0.dp) return distribution
    return object : Arrangement.Vertical {
        override val spacing = gap

        override fun Density.arrange(
            totalSize: Int,
            sizes: IntArray,
            outPositions: IntArray,
        ) {
            val pixels = gap.roundToPx()
            with(distribution) {
                arrange(
                    (totalSize - pixels * (sizes.size - 1).coerceAtLeast(0)).coerceAtLeast(sizes.sum()),
                    sizes,
                    outPositions,
                )
            }
            outPositions.indices.forEach { outPositions[it] += pixels * it }
        }
    }
}
