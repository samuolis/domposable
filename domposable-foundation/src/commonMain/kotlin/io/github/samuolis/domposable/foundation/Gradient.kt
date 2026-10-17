package io.github.samuolis.domposable.foundation

import androidx.compose.runtime.Immutable

/** One color and its relative position in a portable linear gradient. */
@Immutable
data class DomposableGradientStop(
    val color: DomposableColor,
    val position: Float,
) {
    init {
        require(
            position.isFinite() && position in 0f..1f,
        ) { "Gradient stop positions must be finite and between 0 and 1" }
    }
}

/** A renderer-neutral linear gradient with ordered stops along one layout axis. */
@Immutable
data class DomposableLinearGradient(
    val axis: DomposableLayoutAxis,
    val stops: List<DomposableGradientStop>,
) {
    init {
        require(stops.size >= 2) { "A linear gradient requires at least two color stops" }
        require(stops.zipWithNext().all { (first, second) -> first.position <= second.position }) {
            "Gradient stops must be ordered by position"
        }
    }
}
