package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable

/** Cross-renderer scroll position. Values are Compose pixels on native and CSS pixels in HTML. */
expect class DomposableScrollState internal constructor(
    initialValue: Int,
) {
    val value: Int

    suspend fun scrollTo(value: Int)

    suspend fun animateScrollTo(value: Int)
}

@Composable
expect fun rememberDomposableScrollState(initialValue: Int = 0): DomposableScrollState
