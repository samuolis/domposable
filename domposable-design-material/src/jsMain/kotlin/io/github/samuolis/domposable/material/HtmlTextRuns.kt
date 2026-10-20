package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.ui.DomposableTextProps
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
internal fun HtmlTextRuns(props: DomposableTextProps) {
    if (props.runs.isEmpty()) {
        Text(props.text)
    } else {
        props.runs.forEach { run ->
            Span(attrs = {
                style {
                    run.fontWeight?.let { property("font-weight", it.weight.toString()) }
                    run.color?.let { property("color", it.css()) }
                    if (run.contentAlpha != 1f) {
                        val foreground = run.color?.css() ?: "currentColor"
                        val percentage = run.contentAlpha * FULL_OPACITY_PERCENT
                        property("color", "color-mix(in srgb, $foreground $percentage%, transparent)")
                    }
                }
            }) { Text(run.text) }
        }
    }
}

private const val FULL_OPACITY_PERCENT = 100
