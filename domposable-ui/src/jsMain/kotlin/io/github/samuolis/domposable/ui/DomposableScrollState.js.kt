package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

actual class DomposableScrollState internal actual constructor(
    initialValue: Int,
) {
    private var currentValue by mutableIntStateOf(initialValue)

    actual val value: Int get() = currentValue

    internal fun updateFromElement(value: Int) {
        currentValue = value.coerceAtLeast(0)
    }

    actual suspend fun scrollTo(value: Int) {
        currentValue = value.coerceAtLeast(0)
    }

    actual suspend fun animateScrollTo(value: Int) {
        // Duration and easing remain browser policy; this operation updates the portable target.
        currentValue = value.coerceAtLeast(0)
    }
}

@Composable
actual fun rememberDomposableScrollState(initialValue: Int): DomposableScrollState {
    require(initialValue >= 0) { "Initial scroll position must be non-negative" }
    return remember { DomposableScrollState(initialValue) }
}
