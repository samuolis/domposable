package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.ui.DomposableTimePickerProps
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
internal fun HtmlTimeInput(
    props: DomposableTimePickerProps,
    id: String,
    enabled: Boolean,
) {
    Div(attrs = { classes("d-time-input-fields") }) {
        HtmlTimeNumber(
            "$id-hour",
            "Hour",
            if (props.is24Hour) props.hour else ((props.hour + 11) % 12) + 1,
            if (props.is24Hour) 0..23 else 1..12,
            enabled,
        ) { value ->
            props.onTimeChange(
                if (props.is24Hour) {
                    value
                } else {
                    value % 12 + if (props.hour >= 12) 12 else 0
                },
                props.minute,
            )
        }
        Span(attrs = {
            classes("d-time-separator")
            attr("aria-hidden", "true")
        }) { Text(":") }
        HtmlTimeNumber("$id-minute", "Minute", props.minute, 0..59, enabled) { props.onTimeChange(props.hour, it) }
        if (!props.is24Hour) HtmlTimePeriod(props, enabled)
    }
}

@Composable
private fun HtmlTimeNumber(
    id: String,
    label: String,
    value: Int,
    range: IntRange,
    enabled: Boolean,
    onChange: (Int) -> Unit,
) {
    var draft by remember { mutableStateOf(value.toString().padStart(2, '0')) }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(value, focused, enabled) {
        if (!focused || !enabled) draft = value.toString().padStart(2, '0')
    }
    Label(forId = id, attrs = { classes("d-time-number") }) {
        Input(InputType.Text, attrs = {
            id(id)
            attr("aria-label", label)
            attr("inputmode", "numeric")
            attr("maxlength", "2")
            attr("autocomplete", "off")
            attr("aria-invalid", (draft.isNotBlank() && (draft.toIntOrNull()?.let { it in range } != true)).toString())
            value(draft)
            if (!enabled) disabled()
            onFocus { focused = true }
            onBlur {
                focused = false
                draft = value.toString().padStart(2, '0')
            }
            onInput {
                if (enabled) {
                    draft = it.value.filter(Char::isDigit).take(2)
                    draft.toIntOrNull()?.takeIf { it in range }?.let(onChange)
                }
            }
        })
        Span { Text(label) }
    }
}

@Composable
internal fun HtmlTimeHeading(
    props: DomposableTimePickerProps,
    selectingHour: Boolean,
    enabled: Boolean,
    onSelect: (Boolean) -> Unit,
) {
    Div(attrs = { classes("d-time-value") }) {
        for (hour in listOf(true, false)) {
            if (!hour) {
                Span(attrs = {
                    classes("d-time-separator")
                    attr("aria-hidden", "true")
                }) { Text(":") }
            }
            Button(attrs = {
                attr("type", "button")
                attr("aria-label", if (hour) "Select hours" else "Select minutes")
                attr("aria-pressed", (hour == selectingHour).toString())
                if (!enabled) disabled()
                onClick { if (enabled) onSelect(hour) }
            }) {
                val number =
                    if (!hour) {
                        props.minute
                    } else if (props.is24Hour) {
                        props.hour
                    } else {
                        ((props.hour + 11) % 12) +
                            1
                    }
                Text(number.toString().padStart(2, '0'))
            }
        }
        if (!props.is24Hour) HtmlTimePeriod(props, enabled)
    }
}

@Composable
private fun HtmlTimePeriod(
    props: DomposableTimePickerProps,
    enabled: Boolean,
) {
    Div(attrs = {
        classes("d-time-period")
        attr("role", "group")
        attr("aria-label", "Period")
    }) {
        for (afternoon in listOf(false, true)) {
            Button(attrs = {
                attr("type", "button")
                attr("aria-pressed", ((props.hour >= 12) == afternoon).toString())
                if (!enabled) disabled()
                onClick {
                    if (enabled) props.onTimeChange(props.hour % 12 + if (afternoon) 12 else 0, props.minute)
                }
            }) { Text(if (afternoon) "PM" else "AM") }
        }
    }
}
