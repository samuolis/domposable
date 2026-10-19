package io.github.samuolis.domposable.adaptive

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableVisibility
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.region
import io.github.samuolis.domposable.ui.weight

@Composable
internal actual fun DomposablePaneContentHost(
    pane: DomposablePaneScaffoldPane,
    expanded: Boolean,
    visible: Boolean,
    label: String,
    widthFraction: Float,
    minListWidth: DomposableLength,
    maxListWidth: DomposableLength?,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val width =
        when (pane) {
            DomposablePaneScaffoldPane.List -> {
                if (expanded) {
                    DomposableModifier.fillMaxWidth(widthFraction, minListWidth, maxListWidth)
                } else {
                    DomposableModifier.fillMaxWidth()
                }
            }

            DomposablePaneScaffoldPane.Detail -> {
                if (expanded) DomposableModifier.weight(1f) else DomposableModifier.fillMaxWidth()
            }
        }
    val height = if (expanded) DomposableModifier.fillMaxHeight() else DomposableModifier
    DomposableVisibility(
        visible = visible,
        modifier = width.then(height).region(label).then(modifier),
        content = content,
    )
}
