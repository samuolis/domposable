package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant
import io.github.samuolis.domposable.foundation.DomposableTypographyRole
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
import kotlinx.browser.document
import kotlinx.browser.window
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
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import org.w3c.dom.events.FocusEvent
import org.w3c.dom.events.KeyboardEvent

private var nextControlId = 0

@Composable
internal fun HtmlTopAppBar(
    props: DomposableTopAppBarProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.topAppBar
    val variant = props.variant
    val titleRole =
        when (variant) {
            DomposableTopAppBarVariant.Small,
            DomposableTopAppBarVariant.CenterAligned,
            -> DomposableTypographyRole.TitleLarge

            DomposableTopAppBarVariant.Medium -> DomposableTypographyRole.HeadlineSmall

            DomposableTopAppBarVariant.LargeFlexible -> DomposableTypographyRole.DisplaySmall

            else -> DomposableTypographyRole.HeadlineMedium
        }
    val subtitleRole =
        when (variant) {
            DomposableTopAppBarVariant.LargeFlexible -> DomposableTypographyRole.TitleMedium

            DomposableTopAppBarVariant.MediumFlexible,
            DomposableTopAppBarVariant.TwoRows,
            -> DomposableTypographyRole.LabelLarge

            else -> DomposableTypographyRole.BodySmall
        }
    val height =
        when (variant) {
            DomposableTopAppBarVariant.Small, DomposableTopAppBarVariant.CenterAligned -> {
                "64px"
            }

            DomposableTopAppBarVariant.Medium -> {
                "112px"
            }

            DomposableTopAppBarVariant.MediumFlexible, DomposableTopAppBarVariant.TwoRows -> {
                if (props.subtitle == null) "112px" else "136px"
            }

            DomposableTopAppBarVariant.LargeFlexible -> {
                if (props.subtitle == null) "120px" else "152px"
            }

            else -> {
                "152px"
            }
        }
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-app-bar", "d-app-bar-${variant.name.lowercase()}")
            if (variant != DomposableTopAppBarVariant.Small && variant != DomposableTopAppBarVariant.CenterAligned) {
                classes("d-app-bar-expanded")
            }
            style {
                surfaceAppearance(appearance)
                val resolvedHeight = props.expandedHeight?.let { "${it.value}px" } ?: height
                property("min-height", resolvedHeight)
                property("--d-app-bar-height", resolvedHeight)
                props.windowInsets?.let { insets ->
                    property("padding-top", "${insets.top.value}px")
                    property("padding-right", "${insets.end.value}px")
                    property("padding-bottom", "${insets.bottom.value}px")
                    property("padding-left", "${insets.start.value}px")
                }
            }
        }) {
            Div(attrs = { classes("d-app-bar-navigation") }) { props.navigationIcon() }
            Div(attrs = { classes("d-app-bar-heading") }) {
                Div(attrs = {
                    classes("d-app-bar-copy")
                    materialAppBarCopyLayout(variant)
                }) {
                    H2(attrs = {
                        classes("d-app-bar-title")
                        attr("data-variant", variant.name.lowercase())
                        style {
                            val text = appearance.textAppearance
                            applyCatalogTextAppearance(
                                text?.copy(role = text.role ?: titleRole)
                                    ?: DomposableTextAppearance(role = titleRole),
                            )
                        }
                    }) { props.titleContent?.invoke() ?: Text(props.title) }
                    props.subtitle?.let { subtitle ->
                        Span(attrs = {
                            classes("d-app-bar-subtitle")
                            style {
                                applyCatalogTextAppearance(DomposableTextAppearance(role = subtitleRole))
                                property("color", appearance.content?.css() ?: "var(--d-on-surface-variant)")
                            }
                        }) { Text(subtitle) }
                    }
                }
            }
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
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.floatingActionButton
    DomposableHtmlModifiers(modifier) {
        val minimumHeight =
            if (!props.extended &&
                props.size == io.github.samuolis.domposable.foundation.DomposableFabSize.Small
            ) {
                40f
            } else {
                56f
            }
        val sizeDimension =
            when (props.size) {
                io.github.samuolis.domposable.foundation.DomposableFabSize.Small -> 40f
                io.github.samuolis.domposable.foundation.DomposableFabSize.Medium -> 80f
                io.github.samuolis.domposable.foundation.DomposableFabSize.Large -> 96f
                io.github.samuolis.domposable.foundation.DomposableFabSize.Standard -> 56f
            }
        MaterialButtonHitTarget(modifier, minimumHeight) {
            Button(attrs = {
                domposableAttributes(modifier)
                attr("type", "button")
                attr("aria-label", props.label)
                attr("data-extended", props.extended.toString())
                classes("d-button", "d-fab", "d-fab-${props.size.name.lowercase()}")
                style {
                    surfaceAppearance(appearance)
                    appearance.container?.let { property("background-color", it.css()) }
                    appearance.content?.let { property("color", it.css()) }
                    property("padding-inline", if (props.extended) "20px" else "0px")
                    property("height", if (props.extended) "56px" else "${sizeDimension}px")
                    property("gap", if (props.extended) "12px" else "0px")
                    property("display", "grid")
                    property("grid-template-columns", if (props.extended) "auto 1fr" else "auto 0fr")
                    property("min-width", if (props.extended) "0px" else "${sizeDimension}px")
                    property("width", "auto")
                }
                onClick { props.onClick() }
            }) {
                Span(attrs = { classes("d-fab-icon") }) { content() }
                Span(attrs = {
                    classes("d-fab-label")
                    attr("aria-hidden", (!props.extended).toString())
                }) { Text(props.label) }
            }
        }
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
            if (props.equalWidth) classes("d-segments-equal")
            attr("role", "radiogroup")
            if (props.label.isNotBlank()) attr("aria-label", props.label)
            style {
                itemAppearance(appearance)
                constrainedControlWidth(modifier)
            }
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
                        val showIcon = props.showSelectedIcon && index == props.selectedIndex
                        if (showIcon) {
                            Span(attrs = {
                                attr("aria-hidden", "true")
                                classes("d-segment-checkmark")
                            }) { props.selectedIcon?.invoke() }
                        } else {
                            Span(attrs = {
                                attr("aria-hidden", "true")
                                classes("d-segment-icon-space")
                            }) {}
                        }
                        Span(attrs = { classes("d-segment-label") }) { Text(label) }
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
    var popup by remember { mutableStateOf<HTMLElement?>(null) }
    var hovering by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf(false) }
    val onDismiss by rememberUpdatedState(props.onDismiss)
    val described = remember { mutableMapOf<HTMLElement, String?>() }
    // Describe the actual control without replacing any application-supplied description.
    SideEffect {
        val controls = anchor?.querySelectorAll("button, a, input, select, textarea, [tabindex]")
        if (controls != null) {
            for (index in 0 until controls.length) {
                val control = controls.item(index) as HTMLElement
                if (popup?.contains(control) != true && !described.containsKey(control)) {
                    val original = control.getAttribute("aria-describedby")
                    described[control] = original
                    control.setAttribute("aria-describedby", listOfNotNull(original, tooltipId).joinToString(" "))
                }
            }
        }
    }
    val visible = props.visible ?: ((hovering || focused) && !dismissed)
    DisposableEffect(visible, props.visible, popup, anchor) {
        val element = popup
        val target = anchor
        if (visible && element != null && target != null) {
            element.asDynamic().showPopover()
            positionHtmlTooltip(element, target)
            val reposition: (Event) -> Unit = { positionHtmlTooltip(element, target) }
            var resizeFrame: Int? = null
            val updatePosition: () -> Unit = {
                if (resizeFrame == null) {
                    resizeFrame =
                        window.requestAnimationFrame {
                            resizeFrame = null
                            positionHtmlTooltip(element, target)
                        }
                }
            }
            val observer: dynamic = js("new ResizeObserver(updatePosition)")
            observer.observe(element)
            var dismissRequested = false
            val dismiss: () -> Unit = {
                if (!dismissRequested) {
                    dismissRequested = true
                    if (props.visible == null) dismissed = true else onDismiss?.invoke()
                }
            }
            val keyDown: (Event) -> Unit = { event ->
                if ((event as KeyboardEvent).key == "Escape") {
                    val eventTarget = event.target as? Node
                    if (eventTarget != null && (target.contains(eventTarget) || element.contains(eventTarget))) {
                        event.preventDefault()
                        event.stopPropagation()
                    }
                    dismiss()
                }
            }
            val outsidePointer: (Event) -> Unit = { event ->
                val node = event.target as? Node
                if (node == null || !target.contains(node)) dismiss()
            }
            val outsideFocus: (Event) -> Unit = { event ->
                val node = event.target as? Node
                if (props.visible != null && (node == null || !target.contains(node))) dismiss()
            }
            window.addEventListener("resize", reposition)
            window.addEventListener("scroll", reposition, true)
            document.addEventListener("keydown", keyDown, true)
            document.addEventListener("pointerdown", outsidePointer, true)
            document.addEventListener("focusin", outsideFocus, true)
            onDispose {
                window.removeEventListener("resize", reposition)
                window.removeEventListener("scroll", reposition, true)
                document.removeEventListener("keydown", keyDown, true)
                document.removeEventListener("pointerdown", outsidePointer, true)
                document.removeEventListener("focusin", outsideFocus, true)
                observer.disconnect()
                resizeFrame?.let(window::cancelAnimationFrame)
                if (element.matches(":popover-open")) element.asDynamic().hidePopover()
            }
        } else {
            onDispose { }
        }
    }
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
            Div(attrs = {
                attr("popover", "manual")
                classes("d-tooltip-popup")
                if (!visible) attr("hidden", "")
                ref { element ->
                    popup = element
                    onDispose { popup = null }
                }
            }) {
                DomposableHtmlModifiers(props.surfaceModifier) {
                    TagElement<HTMLElement>(if (props.action == null) "span" else "div", applyAttrs = {
                        domposableAttributes(props.surfaceModifier)
                        id(tooltipId)
                        attr("role", if (props.action == null) "tooltip" else "dialog")
                        if (props.action != null) attr("aria-label", props.title ?: props.text)
                        classes("d-tooltip")
                        style { surfaceAppearance(appearance) }
                    }) {
                        props.title?.let { H2 { Text(it) } }
                        Span { Text(props.text) }
                        props.action?.invoke()
                    }
                }
            }
        }
    }
}

private fun positionHtmlTooltip(
    popup: HTMLElement,
    anchor: HTMLElement,
) {
    val bounds = anchor.getBoundingClientRect()
    val size = popup.getBoundingClientRect()
    val margin = 8.0
    val maxLeft = (window.innerWidth - size.width - margin).coerceAtLeast(margin)
    val left = (bounds.left + (bounds.width - size.width) / 2).coerceIn(margin, maxLeft)
    val preferredTop = if (bounds.top >= size.height + margin) bounds.top - size.height else bounds.bottom
    val maxTop = (window.innerHeight - size.height - margin).coerceAtLeast(margin)
    popup.style.left = "${left}px"
    popup.style.top = "${preferredTop.coerceIn(margin, maxTop)}px"
}
