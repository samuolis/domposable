package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.ui.DomposableFloatingActionButtonProps
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRangeSliderProps
import io.github.samuolis.domposable.ui.DomposableSegmentedButtonsProps
import io.github.samuolis.domposable.ui.DomposableSelectProps
import io.github.samuolis.domposable.ui.DomposableSnackbarProps
import io.github.samuolis.domposable.ui.DomposableTooltipProps
import io.github.samuolis.domposable.ui.DomposableTopAppBarProps
import io.github.samuolis.domposable.ui.domposableAttributes
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.FocusEvent

private var nextControlId = 0

@Composable
internal fun HtmlTopAppBar(
    props: DomposableTopAppBarProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-app-bar")
        }) {
            props.navigationIcon()
            H2 { Text(props.title) }
            Div(attrs = { classes("d-app-bar-actions") }) { props.actions() }
        }
    }
}

@Composable
internal fun HtmlFloatingActionButton(
    props: DomposableFloatingActionButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    DomposableHtmlModifiers(modifier) {
        Button(attrs = {
            domposableAttributes(modifier)
            attr("type", "button")
            attr("aria-label", props.label)
            classes("d-button", "d-fab")
            onClick { props.onClick() }
        }) { content() }
    }
}

@Composable
internal fun HtmlSnackbar(
    props: DomposableSnackbarProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-snackbar-message")
        }) {
            Span(attrs = { attr("role", "status") }) { Text(props.message) }
            props.onAction?.let { action ->
                Button(attrs = {
                    attr("type", "button")
                    onClick { action() }
                }) { Text(props.actionLabel!!) }
            }
            props.onDismiss?.let { dismiss ->
                Button(attrs = {
                    attr("type", "button")
                    onClick { dismiss() }
                }) { Text("Dismiss") }
            }
        }
    }
}

@Composable
internal fun HtmlSelect(
    props: DomposableSelectProps,
    modifier: DomposableModifier,
) {
    val inputId = remember { "d-select-${nextControlId++}" }
    var element by remember { mutableStateOf<HTMLSelectElement?>(null) }
    SideEffect { element?.value = props.value }
    DomposableHtmlModifiers(modifier) {
        Div(attrs = { classes("d-field") }) {
            Label(forId = inputId) { Text(props.label) }
            Select(attrs = {
                domposableAttributes(modifier)
                id(inputId)
                ref { select ->
                    element = select
                    onDispose { element = null }
                }
                if (!props.enabled) disabled()
                props.error?.let {
                    attr("aria-invalid", "true")
                    attr("aria-describedby", "$inputId-error")
                }
                onChange { props.onValueChange(it.value ?: props.value) }
            }) {
                props.options.forEach { option ->
                    Option(value = option.value, attrs = {
                        if (option.value == props.value) attr("selected", "")
                        if (!option.enabled) disabled()
                    }) { Text(option.label) }
                }
            }
            props.error?.let { message ->
                Span(attrs = {
                    id("$inputId-error")
                    classes("d-error")
                }) { Text(message) }
            }
        }
    }
}

@Composable
internal fun HtmlSegmentedButtons(
    props: DomposableSegmentedButtonsProps,
    modifier: DomposableModifier,
) {
    val group = remember { "d-segments-${nextControlId++}" }
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-segments")
            attr("role", "radiogroup")
            attr("aria-label", props.label)
        }) {
            props.labels.forEachIndexed { index, label ->
                Label(attrs = { classes("d-segment") }) {
                    Input(InputType.Radio, attrs = {
                        attr("name", group)
                        checked(index == props.selectedIndex)
                        if (!props.enabled) disabled()
                        onChange { if (it.value) props.onSelect(index) }
                    })
                    Span { Text(label) }
                }
            }
        }
    }
}

@Composable
internal fun HtmlRangeSlider(
    props: DomposableRangeSliderProps,
    modifier: DomposableModifier,
) {
    val step =
        if (props.steps == 0) {
            "any"
        } else {
            ((props.range.endInclusive - props.range.start) / (props.steps.toDouble() + 1)).toString()
        }
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-range")
        }) {
            listOf(true, false).forEach { start ->
                Label(attrs = { classes("d-field") }) {
                    Text(if (start) props.startLabel else props.endLabel)
                    Input(InputType.Range, attrs = {
                        attr("aria-label", if (start) props.startLabel else props.endLabel)
                        attr("min", props.range.start.toString())
                        attr("max", props.range.endInclusive.toString())
                        attr("step", step)
                        attr("aria-valuemin", (if (start) props.range.start else props.value.start).toString())
                        attr(
                            "aria-valuemax",
                            (if (start) props.value.endInclusive else props.range.endInclusive).toString(),
                        )
                        value((if (start) props.value.start else props.value.endInclusive).toString())
                        if (!props.enabled) disabled()
                        onInput { event ->
                            event.target.value.toFloatOrNull()?.let { value ->
                                props.onValueChange(
                                    if (start) {
                                        value.coerceAtMost(props.value.endInclusive)..props.value.endInclusive
                                    } else {
                                        props.value.start..value.coerceAtLeast(props.value.start)
                                    },
                                )
                            }
                        }
                    })
                }
            }
        }
    }
}

@Composable
internal fun HtmlTooltip(
    props: DomposableTooltipProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val tooltipId = remember { "d-tooltip-${nextControlId++}" }
    var anchor by remember { mutableStateOf<HTMLElement?>(null) }
    var hovering by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf(false) }
    val described = remember { mutableMapOf<HTMLElement, String?>() }
    // Describe the actual control without replacing any application-supplied description.
    SideEffect {
        val controls = anchor?.querySelectorAll("button, a, input, select, textarea, [tabindex]")
        if (controls != null) {
            for (index in 0 until controls.length) {
                val control = controls.item(index) as HTMLElement
                if (!described.containsKey(control)) {
                    val original = control.getAttribute("aria-describedby")
                    described[control] = original
                    control.setAttribute("aria-describedby", listOfNotNull(original, tooltipId).joinToString(" "))
                }
            }
        }
    }
    val visible = (hovering || focused) && !dismissed
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-tooltip-anchor")
            onMouseEnter {
                hovering = true
                dismissed = false
            }
            onMouseLeave { hovering = false }
            onKeyDown {
                if (it.key == "Escape" && visible) {
                    it.preventDefault()
                    it.stopPropagation()
                    dismissed = true
                }
            }
            ref { element ->
                anchor = element
                val focusIn: (org.w3c.dom.events.Event) -> Unit = {
                    focused = true
                    dismissed = false
                }
                val focusOut: (org.w3c.dom.events.Event) -> Unit = {
                    focused = element.contains((it as FocusEvent).relatedTarget as? org.w3c.dom.Node)
                }
                element.addEventListener("focusin", focusIn)
                element.addEventListener("focusout", focusOut)
                onDispose {
                    element.removeEventListener("focusin", focusIn)
                    element.removeEventListener("focusout", focusOut)
                    described.keys.forEach { control ->
                        val remaining =
                            control
                                .getAttribute("aria-describedby")
                                ?.split(" ")
                                ?.filter { it != tooltipId && it.isNotBlank() }
                                ?.joinToString(" ")
                        if (remaining.isNullOrEmpty()) {
                            control.removeAttribute("aria-describedby")
                        } else {
                            control.setAttribute("aria-describedby", remaining)
                        }
                    }
                    described.clear()
                }
            }
        }) {
            content()
            Span(attrs = {
                id(tooltipId)
                attr("role", "tooltip")
                classes("d-tooltip")
                if (!visible) attr("hidden", "")
            }) { Text(props.text) }
        }
    }
}
