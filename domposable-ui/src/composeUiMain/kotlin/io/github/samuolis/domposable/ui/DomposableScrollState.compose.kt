@file:Suppress("MatchingDeclarationName")

package io.github.samuolis.domposable.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.math.max

actual class DomposableScrollState internal actual constructor(
    initialValue: Int,
) {
    internal val nativeState = ScrollState(initialValue)
    actual val value: Int get() = nativeState.value

    actual suspend fun scrollTo(value: Int) {
        nativeState.scrollTo(max(0, value))
    }

    actual suspend fun animateScrollTo(value: Int) {
        nativeState.animateScrollTo(max(0, value))
    }
}

@Composable
actual fun rememberDomposableScrollState(initialValue: Int): DomposableScrollState {
    require(initialValue >= 0) { "Initial scroll position must be non-negative" }
    return remember { DomposableScrollState(initialValue) }
}
