package io.github.samuolis.domposable.rive

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.toComposeModifier

@Composable
actual fun DomposableRive(
    props: DomposableRiveProps,
    modifier: DomposableModifier,
    fallback: (@Composable () -> Unit)?,
) {
    Box(modifier.toComposeModifier()) { fallback?.invoke() }
}
