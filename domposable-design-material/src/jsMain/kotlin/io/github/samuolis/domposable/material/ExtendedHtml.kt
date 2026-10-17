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
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableAttributes
import io.github.samuolis.domposable.ui.domposableFormEnabled
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
import org.jetbrains.compose.web.dom.TagElement
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
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.topAppBar
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-app-bar", "d-app-bar-${props.variant.name.lowercase()}")
            style {
                surfaceAppearance(appearance)
                if (props.variant == io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant.TwoRows) {
                    property("display", "flex")
                    property("flex-direction", "column")
                    property("align-items", "stretch")
                }
                property(
                    "min-height",
                    when (props.variant) {
                        io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant.Small,
                        io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant.CenterAligned,
                        -> "64px"

                        io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant.Medium,
                        io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant.MediumFlexible,
                        -> "112px"

                        io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant.TwoRows -> "120px"

                        else -> "152px"
                    },
                )
                if (props.variant ==
                    io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant.CenterAligned
                ) {
                    property("position", "relative")
                }
            }
        }) {
            if (props.variant == io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant.TwoRows) {
                Div(attrs = { classes("d-app-bar-row") }) {
                    props.navigationIcon()
                    H2(attrs = {
                        classes("d-app-bar-title")
                        attr("data-variant", props.variant.name.lowercase())
                    }) { Text(props.title) }
                    Div(attrs = { classes("d-app-bar-actions") }) { props.actions() }
                }
                Div(attrs = { classes("d-app-bar-second-row") }) {
                    props.subtitle?.let { Span { Text(it) } }
                }
            } else {
                props.navigationIcon()
                Div(attrs = { classes("d-app-bar-heading") }) {
                    H2(attrs = {
                        classes("d-app-bar-title")
                        attr("data-variant", props.variant.name.lowercase())
                    }) { Text(props.title) }
                    props.subtitle?.let { Span(attrs = { classes("d-app-bar-subtitle") }) { Text(it) } }
                }
                Div(attrs = { classes("d-app-bar-actions") }) { props.actions() }
            }
        }
    }
}

@Composable
internal fun HtmlFloatingActionButton(
    props: DomposableFloatingActionButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.floatingActionButton
    DomposableHtmlModifiers(modifier) {
        Button(attrs = {
            domposableAttributes(modifier)
            attr("type", "button")
            attr("aria-label", props.label)
            classes("d-button", "d-fab", "d-fab-${props.size.name.lowercase()}")
            style {
                surfaceAppearance(appearance)
                appearance.container?.let { property("background-color", it.css()) }
                appearance.content?.let { property("color", it.css()) }
                if (props.extended) property("padding-inline", "20px")
                if (props.extended) property("gap", "12px")
                if (props.extended) {
                    property("width", "auto")
                } else {
                    property(
                        "width",
                        when (props.size) {
                            io.github.samuolis.domposable.foundation.DomposableFabSize.Small -> "40px"
                            io.github.samuolis.domposable.foundation.DomposableFabSize.Medium -> "80px"
                            io.github.samuolis.domposable.foundation.DomposableFabSize.Large -> "96px"
                            io.github.samuolis.domposable.foundation.DomposableFabSize.Standard -> "56px"
                        },
                    )
                }
                if (!props.extended) {
                    property(
                        "height",
                        when (props.size) {
                            io.github.samuolis.domposable.foundation.DomposableFabSize.Small -> "40px"
                            io.github.samuolis.domposable.foundation.DomposableFabSize.Medium -> "80px"
                            io.github.samuolis.domposable.foundation.DomposableFabSize.Large -> "96px"
                            io.github.samuolis.domposable.foundation.DomposableFabSize.Standard -> "56px"
                        },
                    )
                }
            }
            onClick { props.onClick() }
        }) { content() }
    }
}

@Composable
internal fun HtmlSnackbar(
    props: DomposableSnackbarProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.snackbar
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-snackbar-message")
            style { surfaceAppearance(appearance) }
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
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.select
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
                style {
                    (if (props.enabled) appearance.container else appearance.disabledContainer)?.let {
                        property("background-color", it.css())
                    }
                    (if (props.enabled) appearance.content else appearance.disabledContent)?.let {
                        property(
                            "color",
                            it.css(),
                        )
                    }
                    (if (props.enabled) appearance.border else appearance.disabledBorder)?.let {
                        property("border", "${appearance.borderWidth?.value ?: 1f}px solid ${it.css()}")
                    }
                    appearance.borderWidth?.let { property("border-width", "${it.value}px") }
                    appearance.shape?.let { materialShape(it) }
                    appearance.minHeight?.let { property("min-height", "${it.value}px") }
                    appearance.minWidth?.let { property("min-width", "${it.value}px") }
                    appearance.contentPadding?.let { property("padding", it.cssPadding()) }
                    appearance.textAppearance?.let { applyCatalogTextAppearance(it) }
                    property("accent-color", appearance.container?.css() ?: "var(--d-primary)")
                }
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
    val generatedGroup = remember { "d-segments-${nextControlId++}" }
    val group = props.groupName ?: generatedGroup
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.segmentedButtons
    val formEnabled =
        io.github.samuolis.domposable.ui
            .domposableFormEnabled()
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-segments")
            attr("role", "radiogroup")
            attr("aria-label", props.label)
            style { itemAppearance(appearance) }
        }) {
            props.labels.forEachIndexed { index, label ->
                val canSelect = formEnabled && props.enabled && props.optionEnabled[index]
                Label(attrs = {
                    classes("d-segment")
                    if (props.equalWidth) {
                        style {
                            property("flex", "1 1 0")
                            property(
                                "min-width",
                                "${appearance.minimumInteractiveSize?.value ?: 0f}px",
                            )
                        }
                    }
                    if (index > 0) {
                        style {
                            property("margin-inline-start", "-${appearance.borderWidth.value}px")
                        }
                    }
                }) {
                    Input(InputType.Radio, attrs = {
                        attr("name", group)
                        checked(index == props.selectedIndex)
                        if (!canSelect) disabled()
                        props.optionTestTags[index]?.let { attr("data-testid", it) }
                        onClick {
                            if (canSelect && props.allowReselect && index == props.selectedIndex) {
                                props.onSelect(index)
                            }
                        }
                        onChange {
                            val selectionChanged = props.allowReselect || index != props.selectedIndex
                            if (canSelect && it.value && selectionChanged) {
                                props.onSelect(index)
                            }
                        }
                    })
                    Span(attrs = {
                        classes("d-segment-surface")
                        style {
                            itemAppearance(appearance, index == props.selectedIndex)
                            if (props.equalWidth) {
                                property("flex", "1")
                                property("justify-content", "center")
                            }
                        }
                    }) {
                        if (props.showSelectedIcon && index == props.selectedIndex) {
                            Span(attrs = {
                                attr("aria-hidden", "true")
                                classes("d-segment-checkmark")
                            }) {
                                props.selectedIcon?.invoke() ?: Text("✓")
                            }
                        }
                        Text(label)
                    }
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
    val colors = props.colors ?: LocalDomposableTokens.current.components.rangeSlider
    val enabled = props.enabled && domposableFormEnabled()
    val extent = props.range.endInclusive - props.range.start
    val startPercent = ((props.value.start - props.range.start) / extent * 100f).coerceIn(0f, 100f)
    val endPercent = ((props.value.endInclusive - props.range.start) / extent * 100f).coerceIn(0f, 100f)
    val midpoint = (startPercent + endPercent) / 2f
    val step = if (props.steps == 0) "any" else (extent / (props.steps.toDouble() + 1)).toString()
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-range")
            style {
                sliderColors(colors, enabled)
                property("--d-range-start", "$startPercent%")
                property("--d-range-end", "$endPercent%")
                property("--d-range-midpoint", "$midpoint%")
                sliderMarks(props.steps, endPercent, startPercent)
            }
        }) {
            listOf(true, false).forEach { start ->
                Input(InputType.Range, attrs = {
                    classes("d-slider", if (start) "d-range-start" else "d-range-end")
                    attr("aria-label", if (start) props.startLabel else props.endLabel)
                    attr("min", props.range.start.toString())
                    attr("max", props.range.endInclusive.toString())
                    attr("step", step)
                    attr("aria-valuemin", (if (start) props.range.start else props.value.start).toString())
                    attr(
                        "aria-valuemax",
                        (if (start) props.value.endInclusive else props.range.endInclusive).toString(),
                    )
                    val current = if (start) props.value.start else props.value.endInclusive
                    value(current.toString())
                    if (!enabled) disabled()
                    onInput { event ->
                        event.target.value.toFloatOrNull()?.takeIf { enabled }?.let { value ->
                            val next =
                                if (start) {
                                    value.coerceAtMost(props.value.endInclusive)..props.value.endInclusive
                                } else {
                                    props.value.start..value.coerceAtLeast(props.value.start)
                                }
                            props.onValueChange(next)
                        }
                        // Preserve controlled values, including a clamped or declined request.
                        event.target.value = current.toString()
                    }
                })
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
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.tooltip
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
            TagElement<HTMLElement>(if (props.action == null) "span" else "div", applyAttrs = {
                id(tooltipId)
                attr("role", if (props.action == null) "tooltip" else "dialog")
                classes("d-tooltip")
                style { surfaceAppearance(appearance) }
                if (!visible) attr("hidden", "")
            }) {
                props.title?.let { H2 { Text(it) } }
                Span { Text(props.text) }
                props.action?.invoke()
            }
        }
    }
}
