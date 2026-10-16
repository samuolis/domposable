package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable

/**
 * Keeps content composed while hidden, without reserving layout space or exposing its semantics.
 * Useful for retaining an adaptive list's local UI state while showing its detail on a narrow window.
 */
@Composable
expect fun DomposableVisibility(
    visible: Boolean,
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit,
)
