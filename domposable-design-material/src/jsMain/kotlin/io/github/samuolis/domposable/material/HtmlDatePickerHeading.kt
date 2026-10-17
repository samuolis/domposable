package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.ui.DomposableDatePickerMode
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
internal fun HtmlDatePickerHeading(
    title: String,
    selection: String,
    toggleLabel: String,
    mode: DomposableDatePickerMode,
    enabled: Boolean,
    onToggle: (() -> Unit)?,
) {
    Div(attrs = { classes("d-date-picker-heading") }) {
        Span(attrs = { classes("d-date-picker-title") }) { Text(title) }
        Div(attrs = { classes("d-date-picker-headline") }) {
            Span(attrs = { attr("aria-live", "polite") }) { Text(selection) }
            onToggle?.let { toggle ->
                Button(attrs = {
                    attr("type", "button")
                    attr("aria-label", toggleLabel)
                    attr("data-input-mode", (mode == DomposableDatePickerMode.Input).toString())
                    if (!enabled) disabled()
                    onClick { if (enabled) toggle() }
                }) {}
            }
        }
    }
}
