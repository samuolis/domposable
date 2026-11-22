package io.github.samuolis.domposable.rive

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.ui.DomposableModifier

/** Draws a local Rive file with the Android, iOS bridge, or semantic HTML runtime. */
@Composable
expect fun DomposableRive(
    props: DomposableRiveProps,
    modifier: DomposableModifier = DomposableModifier,
    fallback: (@Composable () -> Unit)? = null,
)
