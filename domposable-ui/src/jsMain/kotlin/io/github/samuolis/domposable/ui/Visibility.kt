package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div

@Composable
actual fun DomposableVisibility(
    visible: Boolean,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    Div(attrs = {
        attr("data-domposable-visibility", "")
        style { property("display", if (visible) "contents" else "none") }
        if (!visible) attr("aria-hidden", "true")
    }) {
        DomposableBox(modifier = modifier, content = content)
    }
}
