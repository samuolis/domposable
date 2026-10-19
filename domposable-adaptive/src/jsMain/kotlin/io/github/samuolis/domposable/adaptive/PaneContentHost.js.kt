package io.github.samuolis.domposable.adaptive

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.region
import org.jetbrains.compose.web.dom.Div

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
    // Keep responsive sizing on this host so pane descendants never move between modifier wrappers.
    Div(attrs = {
        attr("data-domposable-visibility", "")
        attr("data-domposable-pane-host", pane.name.lowercase())
        if (!visible) attr("aria-hidden", "true")
        style {
            property("box-sizing", "border-box")
            property("min-width", "0")
            property("min-height", "0")
            if (visible) {
                property("display", "grid")
                property("grid-template-columns", "minmax(0,1fr)")
                property("grid-template-rows", "minmax(0,1fr)")
            } else {
                property("display", "none")
            }
            if (expanded) {
                property("height", "100%")
                when (pane) {
                    DomposablePaneScaffoldPane.List -> {
                        property("width", "${widthFraction * 100}%")
                        property("min-width", "min(${minListWidth.value}px, 100%)")
                        maxListWidth?.let { property("max-width", "min(${it.value}px, 100%)") }
                        property("flex", "0 1 auto")
                    }

                    DomposablePaneScaffoldPane.Detail -> {
                        property("flex", "1 1 0%")
                        property("min-width", "0")
                        property("min-height", "0")
                    }
                }
            } else {
                property("width", "100%")
                property("flex", "0 1 auto")
            }
        }
    }) {
        DomposableBox(
            modifier = DomposableModifier.fillMaxHeight().region(label).then(modifier),
            content = content,
        )
    }
}
