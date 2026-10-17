package io.github.samuolis.domposable.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.clearAndSetSemantics

@Composable
actual fun DomposableVisibility(
    visible: Boolean,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val nativeModifier =
        if (visible) {
            modifier.toComposeModifier()
        } else {
            Modifier.clearAndSetSemantics {}.layout { measurable, constraints ->
                measurable.measure(constraints)
                layout(0, 0) {}
            }
        }
    Box(nativeModifier) {
        ProvideDomposableBoxModifierScope(content)
    }
}
