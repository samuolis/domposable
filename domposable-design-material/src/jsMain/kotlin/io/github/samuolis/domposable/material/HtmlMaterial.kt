package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableAutocomplete
import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.foundation.DomposableImageScale
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableLiveRegion
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableTextAlignment
import io.github.samuolis.domposable.foundation.DomposableTextDecoration
import io.github.samuolis.domposable.foundation.DomposableTextOverflow
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.ui.DomposableComponents
import io.github.samuolis.domposable.ui.DomposableDataTableProps
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableImageProps
import io.github.samuolis.domposable.ui.DomposableLazyItem
import io.github.samuolis.domposable.ui.DomposableLineChartProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableModifierOperation
import io.github.samuolis.domposable.ui.DomposableValueChartProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.background
import io.github.samuolis.domposable.ui.domposableAttributes
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.width
import kotlinx.browser.document
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Aside
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Footer
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Header
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Main
import org.jetbrains.compose.web.dom.Nav
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import org.jetbrains.compose.web.dom.Ul
import org.w3c.dom.HTMLDialogElement
import org.w3c.dom.HTMLElement

private fun DomposableAlignment.css() =
    when (this) {
        DomposableAlignment.Start -> "flex-start"
        DomposableAlignment.Center -> "center"
        DomposableAlignment.End -> "flex-end"
    }

internal fun DomposableFontWeight.css() =
    when (this) {
        DomposableFontWeight.Thin -> "100"
        DomposableFontWeight.ExtraLight -> "200"
        DomposableFontWeight.Light -> "300"
        DomposableFontWeight.Normal -> "400"
        DomposableFontWeight.Medium -> "500"
        DomposableFontWeight.SemiBold -> "600"
        DomposableFontWeight.Bold -> "700"
        DomposableFontWeight.ExtraBold -> "800"
        DomposableFontWeight.Black -> "900"
    }

internal fun DomposableAutocomplete.htmlValue() =
    when (this) {
        DomposableAutocomplete.Off -> "off"
        DomposableAutocomplete.Email -> "email"
        DomposableAutocomplete.Username -> "username"
        DomposableAutocomplete.CurrentPassword -> "current-password"
        DomposableAutocomplete.NewPassword -> "new-password"
        DomposableAutocomplete.OneTimeCode -> "one-time-code"
    }

@Composable
actual fun rememberDomposableMaterialComponents(): DomposableComponents =
    remember {
        DomposableComponents(
            form = { props, modifier, content -> HtmlDomposableForm(props, modifier, content) },
            disclosure = { props, modifier, content -> HtmlDomposableDisclosure(props, modifier, content) },
            richHtml = { props, modifier, _ -> HtmlDomposableRichHtml(props, modifier) },
            theme = { props, _, content -> HtmlTheme(props.tokens, content) },
            column = {
                props,
                modifier,
                content,
                ->
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        style {
                            property("display", "flex")
                            property("flex-direction", "column")
                            property("gap", "${props.gap.value}px")
                            property("align-items", props.alignment.css())
                        }
                    }) { content() }
                }
            },
            row = {
                props,
                modifier,
                content,
                ->
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        style {
                            property("display", "flex")
                            property("gap", "${props.gap.value}px")
                            property("align-items", props.alignment.css())
                        }
                    }) { content() }
                }
            },
            flowRow = {
                props,
                modifier,
                content,
                ->
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        style {
                            property("display", "flex")
                            property("flex-wrap", "wrap")
                            property("gap", "${props.gap.value}px")
                        }
                    }) { content() }
                }
            },
            link = { props, modifier, _ -> HtmlDomposableLink(props, modifier) },
            box = {
                props,
                modifier,
                content,
                ->
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        style {
                            property("display", "grid")
                            property(
                                "place-items",
                                when (props.alignment) {
                                    DomposableAlignment.Start -> "start"
                                    DomposableAlignment.Center -> "center"
                                    DomposableAlignment.End -> "end"
                                },
                            )
                        }
                    }) { content() }
                }
            },
            lazyColumn = { props, modifier, _ ->
                if (props.window == null) {
                    HtmlCollection(props.items, modifier, props.gap, "column")
                } else if (props.window?.measureRows == true) {
                    HtmlMeasuredWindow(props, modifier)
                } else {
                    HtmlListWindow(props, modifier)
                }
            },
            lazyRow = { props, modifier, _ ->
                if (props.window == null) {
                    HtmlCollection(props.items, modifier, props.gap, "row")
                } else {
                    HtmlRowWindow(props, modifier)
                }
            },
            lazyGrid = {
                props,
                modifier,
                _,
                ->
                if (props.window == null) {
                    HtmlCollection(props.items, modifier, props.gap, "grid", props.columns)
                } else {
                    HtmlGridWindow(props, modifier)
                }
            },
            table = { props, modifier, _ -> HtmlTable(props, modifier) },
            lineChart = { props, modifier, _ -> HtmlLineChart(props, modifier) },
            valueChart = { props, modifier, _ -> HtmlValueChart(props, modifier) },
            dataTable = { props, modifier, _ -> HtmlDataTable(props, modifier) },
            text = { props, modifier, _ ->
                val appearance = props.appearance ?: LocalMaterialButtonTextAppearance.current
                val tokens = LocalDomposableTokens.current
                val baseFontSize =
                    appearance?.fontSize ?: when (props.style) {
                        DomposableTextStyle.Body -> tokens.typography.bodySize
                        DomposableTextStyle.Title -> tokens.typography.titleSize
                        DomposableTextStyle.Label -> tokens.typography.labelSize
                    }
                val effectiveLineHeight = appearance?.lineHeight ?: baseFontSize * tokens.typography.lineHeight
                val textAlignment =
                    when (appearance?.alignment) {
                        DomposableTextAlignment.Start -> "start"
                        DomposableTextAlignment.Center -> "center"
                        DomposableTextAlignment.End -> "end"
                        DomposableTextAlignment.Justify -> "justify"
                        null -> null
                    }
                val textDecoration =
                    when (appearance?.decoration) {
                        DomposableTextDecoration.None -> "none"
                        DomposableTextDecoration.Underline -> "underline"
                        DomposableTextDecoration.LineThrough -> "line-through"
                        null -> null
                    }
                val liveRegion =
                    when (appearance?.liveRegion) {
                        DomposableLiveRegion.Polite -> "polite"
                        DomposableLiveRegion.Assertive -> "assertive"
                        null -> null
                    }

                fun <T : HTMLElement> AttrsScope<T>.applyAppearance() {
                    textAlignment?.let { alignment -> style { property("text-align", alignment) } }
                    textDecoration?.let { decoration -> style { property("text-decoration", decoration) } }
                    appearance?.letterSpacing?.let { style { property("letter-spacing", "${it}px") } }
                    appearance?.fontSize?.let { size -> style { property("font-size", "${size}px") } }
                    appearance?.lineHeight?.let { height -> style { property("line-height", "${height}px") } }
                    appearance?.fontWeight?.let { weight -> style { property("font-weight", weight.css()) } }
                    appearance?.maxLines?.let { lines ->
                        when (appearance.overflow) {
                            DomposableTextOverflow.Clip -> {
                                style {
                                    property("max-height", "${effectiveLineHeight * lines}px")
                                    property("overflow", "hidden")
                                }
                            }

                            DomposableTextOverflow.Ellipsis -> {
                                style {
                                    property("display", "-webkit-box")
                                    property("-webkit-line-clamp", lines.toString())
                                    property("-webkit-box-orient", "vertical")
                                    property("overflow", "hidden")
                                }
                            }

                            DomposableTextOverflow.Visible -> {
                                style { property("overflow", "visible") }
                            }
                        }
                    }
                    if (appearance?.selectable == false) style { property("user-select", "none") }
                    liveRegion?.let {
                        attr("aria-live", it)
                        attr("aria-atomic", "true")
                    }
                }
                DomposableHtmlModifiers(modifier) {
                    if (props.style == DomposableTextStyle.Title ||
                        props.headingLevel != null
                    ) {
                        TagElement<HTMLElement>("h${props.headingLevel ?: 2}", applyAttrs = {
                            domposableAttributes(modifier)
                            classes("d-title")
                            props.color?.let { style { property("color", it.css()) } }
                            applyAppearance()
                        }) { Text(props.text) }
                    } else {
                        Span(attrs = {
                            domposableAttributes(modifier)
                            classes(
                                if (props.style ==
                                    DomposableTextStyle.Label
                                ) {
                                    "d-label"
                                } else {
                                    "d-text"
                                },
                            )
                            props.color?.let { style { property("color", it.css()) } }
                            applyAppearance()
                        }) { Text(props.text) }
                    }
                }
            },
            button = { props, modifier, content -> HtmlButton(props, modifier, content) },
            iconButton = {
                props,
                modifier,
                content,
                ->
                DomposableHtmlModifiers(modifier) {
                    Button(attrs = {
                        domposableAttributes(modifier)
                        attr("type", "button")
                        attr("aria-label", props.label)
                        classes("d-icon-button")
                        if (!props.enabled) attr("disabled", "")
                        onClick { props.onClick() }
                    }) { content() }
                }
            },
            textField = { props, modifier, _ -> HtmlTextField(props, modifier) },
            checkbox = {
                props,
                modifier,
                _,
                ->
                HtmlToggle(props.checked, props.onCheckedChange, props.label, props.enabled, modifier, false)
            },
            switch = {
                props,
                modifier,
                _,
                ->
                HtmlToggle(props.checked, props.onCheckedChange, props.label, props.enabled, modifier, true)
            },
            radioButton = {
                props,
                modifier,
                _,
                ->
                DomposableHtmlModifiers(modifier) {
                    Label(attrs = { classes("d-toggle") }) {
                        Input(InputType.Radio, attrs = {
                            domposableAttributes(modifier)
                            checked(props.selected)
                            attr("aria-label", props.label)
                            if (!props.enabled) disabled()
                            onChange { props.onClick() }
                        })
                        Text(props.label)
                    }
                }
            },
            slider = { props, modifier, _ ->
                DomposableHtmlModifiers(modifier) {
                    Input(InputType.Range, attrs = {
                        domposableAttributes(modifier)
                        value(props.value.toString())
                        attr("aria-label", props.label)
                        attr("min", props.range.start.toString())
                        attr("max", props.range.endInclusive.toString())
                        attr(
                            "step",
                            if (props.steps ==
                                0
                            ) {
                                "any"
                            } else {
                                ((props.range.endInclusive - props.range.start) / (props.steps + 1)).toString()
                            },
                        )
                        if (!props.enabled) disabled()
                        onInput {
                            it.target.value
                                .toFloatOrNull()
                                ?.let(props.onValueChange)
                        }
                    })
                }
            },
            card = { props, modifier, content ->
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        classes("d-card")
                        props.onClick?.let { click ->
                            attr("role", "button")
                            attr("tabindex", "0")
                            onClick { click() }
                            onKeyDown {
                                if (it.key ==
                                    "Enter" ||
                                    it.key == " "
                                ) {
                                    it.preventDefault()
                                    click()
                                }
                            }
                        }
                    }) { content() }
                }
            },
            surface = {
                props,
                modifier,
                content,
                ->
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        classes("d-surface")
                        props.color?.let { style { property("background", it.css()) } }
                    }) { content() }
                }
            },
            image = { props, modifier, _ -> HtmlImage(props, modifier) },
            icon = { props, modifier, _ ->
                DomposableHtmlModifiers(modifier) {
                    Span(attrs = {
                        domposableAttributes(modifier)
                        if (props.description ==
                            null
                        ) {
                            attr("aria-hidden", "true")
                        } else {
                            attr("role", "img")
                            attr("aria-label", props.description.orEmpty())
                        }
                    }) { Text(props.symbol) }
                }
            },
            divider = {
                props,
                modifier,
                _,
                ->
                DomposableHtmlModifiers(modifier) {
                    Hr(attrs = {
                        domposableAttributes(modifier)
                        attr("aria-orientation", props.orientation.name.lowercase())
                        classes("d-divider")
                    })
                }
            },
            spacer = {
                props,
                modifier,
                _,
                ->
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        attr("aria-hidden", "true")
                        style {
                            property("width", "${props.length.value}px")
                            property("height", "${props.length.value}px")
                        }
                    }) {}
                }
            },
            badge = {
                props,
                modifier,
                _,
                ->
                DomposableHtmlModifiers(modifier) {
                    Span(attrs = {
                        domposableAttributes(modifier)
                        classes("d-badge")
                    }) { Text(props.text) }
                }
            },
            chip = {
                props,
                modifier,
                _,
                ->
                DomposableHtmlModifiers(modifier) {
                    Button(attrs = {
                        domposableAttributes(modifier)
                        attr("type", "button")
                        attr("aria-pressed", props.selected.toString())
                        classes("d-chip")
                        if (!props.enabled) disabled()
                        onClick { props.onClick() }
                    }) { Text(props.label) }
                }
            },
            avatar = {
                props,
                modifier,
                _,
                ->
                DomposableHtmlModifiers(modifier) {
                    Span(attrs = {
                        domposableAttributes(modifier)
                        attr("role", "img")
                        attr("aria-label", props.description.orEmpty())
                        classes("d-avatar")
                    }) { Text(props.initials) }
                }
            },
            banner = {
                props,
                modifier,
                _,
                ->
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        attr("role", if (props.isError) "alert" else "status")
                        classes("d-banner")
                    }) { Text(props.message) }
                }
            },
            progress = {
                props,
                modifier,
                _,
                ->
                DomposableHtmlModifiers(modifier) {
                    TagElement<HTMLElement>("progress", applyAttrs = {
                        domposableAttributes(modifier)
                        attr("aria-label", props.label)
                        attr("max", "1")
                        props.value?.let { attr("value", it.toString()) }
                        classes("d-progress")
                    }) {}
                }
            },
            tabs = { props, modifier, _ ->
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        attr("role", "tablist")
                        attr("aria-label", "Sections")
                        classes("d-tabs")
                    }) {
                        props.labels.forEachIndexed { index, label ->
                            Button(attrs = {
                                attr("type", "button")
                                attr("role", "tab")
                                attr(
                                    "aria-selected",
                                    (
                                        props.selectedIndex ==
                                            index
                                    ).toString(),
                                )
                                attr(
                                    "tabindex",
                                    if (props.selectedIndex ==
                                        index
                                    ) {
                                        "0"
                                    } else {
                                        "-1"
                                    },
                                )
                                onClick { props.onSelect(index) }
                                onKeyDown { event ->
                                    val offset =
                                        when (event.key) {
                                            "ArrowRight" -> 1
                                            "ArrowLeft" -> -1
                                            else -> 0
                                        }
                                    if (offset !=
                                        0
                                    ) {
                                        event.preventDefault()
                                        val next =
                                            (index + offset + props.labels.size) % props.labels.size
                                        props.onSelect(next)
                                        (
                                            (event.target as? HTMLElement)?.parentElement?.children?.item(
                                                next,
                                            ) as? HTMLElement
                                        )?.focus()
                                    }
                                }
                            }) { Text(label) }
                        }
                    }
                }
            },
            navigationBar = { props, modifier, _ ->
                DomposableHtmlModifiers(modifier) {
                    Nav(attrs = {
                        domposableAttributes(modifier)
                        attr("aria-label", "Application")
                        classes("d-navigation")
                        style {
                            property(
                                "flex-direction",
                                if (props.orientation ==
                                    DomposableOrientation.Horizontal
                                ) {
                                    "row"
                                } else {
                                    "column"
                                },
                            )
                        }
                    }) {
                        props.items.forEachIndexed { index, item ->
                            if (item.href != null) {
                                A(href = item.href, attrs = {
                                    if (index == props.selectedIndex) attr("aria-current", "page")
                                    onClick { event ->
                                        val modified =
                                            listOf(
                                                event.ctrlKey,
                                                event.metaKey,
                                                event.shiftKey,
                                                event.altKey,
                                            ).any { it }
                                        if (event.button.toInt() == 0 && !modified) {
                                            event.preventDefault()
                                            props.onSelect(index)
                                        }
                                    }
                                }) { Text("${item.symbol} ${item.label}") }
                            } else {
                                Button(attrs = {
                                    attr("type", "button")
                                    if (index ==
                                        props.selectedIndex
                                    ) {
                                        attr("aria-current", "page")
                                    }
                                    onClick { props.onSelect(index) }
                                }) { Text("${item.symbol} ${item.label}") }
                            }
                        }
                    }
                }
            },
            menu = {
                props,
                modifier,
                _,
                ->
                if (props.expanded) {
                    HtmlModal("Menu", props.onDismiss, modifier, sheet = false) {
                        props.items.forEach { item ->
                            Button(attrs = {
                                attr("type", "button")
                                if (!item.enabled) disabled()
                                onClick {
                                    item.onClick()
                                    props.onDismiss()
                                }
                            }) { Text(item.label) }
                        }
                    }
                }
            },
            dialog = {
                props,
                modifier,
                content,
                ->
                HtmlModal(props.title, props.onDismiss, modifier, sheet = false) {
                    content()
                    props.actions()
                }
            },
            bottomSheet = {
                props,
                modifier,
                content,
                ->
                HtmlModal(props.title, props.onDismiss, modifier, sheet = true, content)
            },
            topAppBar = { props, modifier, _ -> HtmlTopAppBar(props, modifier) },
            floatingActionButton = { props, modifier, content -> HtmlFloatingActionButton(props, modifier, content) },
            snackbar = { props, modifier, _ -> HtmlSnackbar(props, modifier) },
            select = { props, modifier, _ -> HtmlSelect(props, modifier) },
            segmentedButtons = { props, modifier, _ -> HtmlSegmentedButtons(props, modifier) },
            rangeSlider = { props, modifier, _ -> HtmlRangeSlider(props, modifier) },
            tooltip = { props, modifier, content -> HtmlTooltip(props, modifier, content) },
            scaffold = { props, modifier, content ->
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        classes("d-scaffold")
                    }) {
                        Header {
                            A(href = "#main-content", attrs = {
                                classes("d-skip-link")
                                onClick {
                                    it.preventDefault()
                                    (document.getElementById("main-content") as? HTMLElement)?.focus()
                                }
                            }) { Text("Skip to content") }
                            props.topBar()
                        }
                        Div(attrs = { classes("d-scaffold-middle") }) {
                            props.navigation()
                            Main(attrs = {
                                id("main-content")
                                attr("tabindex", "-1")
                                classes("d-scaffold-body")
                            }) {
                                content()
                                Div(attrs = { classes("d-floating") }) { props.floatingAction() }
                                Div(attrs = { classes("d-snackbar") }) { props.snackbar() }
                            }
                            Aside { props.contextual() }
                        }
                        Footer { props.bottomBar() }
                        props.overlays()
                    }
                }
            },
        )
    }

@Composable
private fun HtmlImage(
    props: DomposableImageProps,
    modifier: DomposableModifier,
) {
    val loaded by rememberUpdatedState(props.onLoaded)
    val failed by rememberUpdatedState(props.onError)
    // A sized leaf must fit inside the ordered layout boxes, including inner padding.
    var imageHeight: Float? = null
    modifier.elements.forEach { element ->
        val operation = element as? DomposableModifierOperation ?: return@forEach
        when (operation) {
            is DomposableModifierOperation.Height -> {
                imageHeight = operation.length.value
            }

            is DomposableModifierOperation.Padding -> {
                imageHeight =
                    imageHeight?.let {
                        (it - operation.insets.top.value - operation.insets.bottom.value).coerceAtLeast(
                            0f,
                        )
                    }
            }

            DomposableModifierOperation.FillHeight -> {
                imageHeight = null
            }

            else -> {
                Unit
            }
        }
    }
    DomposableHtmlModifiers(modifier) {
        Img(src = props.source, alt = props.description ?: "", attrs = {
            domposableAttributes(modifier)
            attr("loading", "lazy")
            ref { element ->
                val load: (org.w3c.dom.events.Event) -> Unit = { loaded?.invoke() }
                val error: (org.w3c.dom.events.Event) -> Unit = { failed?.invoke() }
                element.addEventListener("load", load)
                element.addEventListener("error", error)
                if (element.complete && element.naturalWidth > 0) loaded?.invoke()
                onDispose {
                    element.removeEventListener("load", load)
                    element.removeEventListener("error", error)
                }
            }
            style {
                property("max-width", "100%")
                property("display", "block")
                property(
                    "object-fit",
                    when (props.scale) {
                        DomposableImageScale.Fit -> "contain"
                        DomposableImageScale.Crop -> "cover"
                        DomposableImageScale.Fill -> "fill"
                    },
                )
                imageHeight?.let { property("height", "${it}px") }
                if (modifier.elements.any {
                        it is DomposableModifierOperation.Width ||
                            it == DomposableModifierOperation.FillWidth
                    }
                ) {
                    property("width", "100%")
                }
            }
        })
    }
}

private var nextInputId = 0

internal fun nextMaterialInputId(): String = "d-input-${nextInputId++}"

@Composable
private fun HtmlToggle(
    checkedValue: Boolean,
    onChange: (Boolean) -> Unit,
    label: String,
    enabled: Boolean,
    modifier: DomposableModifier,
    switch: Boolean,
) {
    DomposableHtmlModifiers(modifier) {
        Label(attrs = { classes("d-toggle") }) {
            Input(InputType.Checkbox, attrs = {
                domposableAttributes(modifier)
                checked(checkedValue)
                if (switch) attr("role", "switch")
                if (!enabled) disabled()
                onChange { onChange(it.value) }
            })
            Text(label)
        }
    }
}

@Composable
private fun HtmlCollection(
    items: List<DomposableLazyItem>,
    modifier: DomposableModifier,
    gap: DomposableLength,
    layout: String,
    columns: Int = 1,
) {
    require(columns > 0)
    require(items.map { it.key }.distinct().size == items.size) { "Collection keys must be unique" }
    DomposableHtmlModifiers(modifier) {
        Ul(attrs = {
            domposableAttributes(modifier)
            classes("d-collection")
            style {
                property(
                    "display",
                    if (layout ==
                        "grid"
                    ) {
                        "grid"
                    } else {
                        "flex"
                    },
                )
                ; property("flex-direction", layout)
                property("gap", "${gap.value}px")
                if (layout ==
                    "grid"
                ) {
                    property("grid-template-columns", "repeat($columns, minmax(0, 1fr))")
                }
            }
        }) {
            items.forEach { item -> key(item.key) { Li { item.content() } } }
        }
    }
}

@Composable
private fun HtmlModal(
    title: String,
    onDismiss: () -> Unit,
    modifier: DomposableModifier,
    sheet: Boolean,
    content: @Composable () -> Unit,
) {
    val dismiss by rememberUpdatedState(onDismiss)
    val titleId = remember { "d-dialog-${nextInputId++}" }
    DomposableHtmlModifiers(modifier) {
        TagElement<HTMLDialogElement>("dialog", applyAttrs = {
            domposableAttributes(modifier)
            attr("aria-labelledby", titleId)
            classes("d-dialog")
            if (sheet) classes("d-sheet")
            ref { element ->
                val previousFocus = document.activeElement as? HTMLElement
                val cancel: (org.w3c.dom.events.Event) -> Unit = {
                    it.preventDefault()
                    dismiss()
                }
                element.addEventListener("cancel", cancel)
                element.showModal()
                onDispose {
                    element.removeEventListener("cancel", cancel)
                    element.close()
                    previousFocus?.focus()
                }
            }
        }) {
            H2(attrs = { id(titleId) }) { Text(title) }
            content()
            Button(attrs = {
                attr("type", "button")
                onClick { dismiss() }
            }) { Text("Close") }
        }
    }
}

@Composable
private fun HtmlTheme(
    tokens: DomposableTokens,
    content: @Composable () -> Unit,
) {
    InstallDomposableMaterialStyles()
    Div(attrs = {
        classes("d-theme")
        domposableTokenVariables(tokens)
    }) { content() }
}

@Composable
internal fun InstallDomposableMaterialStyles() {
    DisposableEffect(Unit) {
        val style = document.createElement("style")
        style.textContent = MATERIAL_CSS
        document.head!!.appendChild(style)
        onDispose { style.remove() }
    }
}

/** Applies only Domposable token custom properties to an existing HTML host element. */
fun AttrsScope<HTMLElement>.domposableTokenVariables(tokens: DomposableTokens) {
    style {
        property("--d-primary", tokens.colors.primary.css())
        property("--d-on-primary", tokens.colors.onPrimary.css())
        property("--d-surface", tokens.colors.surface.css())
        property("--d-on-surface", tokens.colors.onSurface.css())
        property("--d-variant", tokens.colors.surfaceVariant.css())
        property("--d-outline", tokens.colors.outline.css())
        property("--d-error", tokens.colors.error.css())
        property("--d-font", tokens.typography.fontFamily)
        property("--d-body", "${tokens.typography.bodySize}px")
        property("--d-title", "${tokens.typography.titleSize}px")
        property("--d-label", "${tokens.typography.labelSize}px")
        property("--d-line-height", tokens.typography.lineHeight.toString())
        property("--d-spacing", "${tokens.spacing.medium.value}px")
        property("--d-radius", "${tokens.radius.value}px")
        property("--d-elevation", "${tokens.elevation.value}px")
        property("--d-motion", "${tokens.motionDurationMillis}ms")
    }
}

private const val MATERIAL_CSS = """
.d-theme *{box-sizing:border-box}.d-link{color:var(--d-primary);padding:8px;display:inline-flex;align-items:center;min-height:44px}.d-theme{color:var(--d-on-surface);background:var(--d-surface);font:var(--d-body)/var(--d-line-height) var(--d-font);min-height:100%;box-sizing:border-box}
.d-theme button,.d-theme input,.d-theme textarea{font:inherit;accent-color:var(--d-primary)}
.d-theme button{cursor:pointer}.d-theme button:disabled{cursor:default;opacity:.5}
.d-theme :focus-visible{outline:3px solid var(--d-primary);outline-offset:3px}
.d-skip-link{position:absolute;left:-999px}.d-skip-link:focus{left:12px;top:12px;z-index:10}
.d-theme h1,.d-theme h2,.d-theme h3{margin:0}.d-title{font-size:var(--d-title)}.d-label{font-size:var(--d-label)}
.d-button,.d-chip,.d-icon-button{border:1px solid transparent;padding:10px 20px;border-radius:var(--d-radius);min-height:44px;background:var(--d-primary);color:var(--d-on-primary);transition:background var(--d-motion)}
.d-button{cursor:pointer;transition:box-shadow 120ms ease,filter 120ms ease,transform 80ms ease;position:relative;display:inline-flex;align-items:center;justify-content:center}.d-button-single-line .d-button-copy{white-space:nowrap}.d-button-copy{display:inline-flex;align-items:center;justify-content:center}.d-button:not(:disabled):hover{transform:scale(var(--d-button-hovered-scale,1))}.d-button:not(:disabled):active{transform:scale(var(--d-button-pressed-scale,1))}.d-button-spinner{position:absolute;width:var(--d-button-indicator-size,20px);height:var(--d-button-indicator-size,20px);border:var(--d-button-indicator-stroke,2px) solid currentColor;border-right-color:transparent;border-radius:50%;animation:d-button-spin .8s linear infinite}@keyframes d-button-spin{to{transform:rotate(360deg)}}
@media(hover:hover){.d-button:not(:disabled):hover{filter:brightness(.95)}.d-button-outlined:not(:disabled):hover,.d-button-text:not(:disabled):hover{--d-button-feedback:inset 0 0 0 999px color-mix(in srgb,currentColor 8%,transparent);box-shadow:var(--d-button-feedback)}}
.d-button:not(:disabled):active{filter:brightness(.9)}.d-button:not(:disabled):focus-visible{outline:none;box-shadow:var(--d-button-feedback,0 0 0 0 transparent),0 0 0 3px var(--d-primary)}
.d-button:disabled{cursor:default}
.d-button-outlined{background:transparent;color:var(--d-primary);border-color:var(--d-outline)}.d-button-text{background:transparent;color:var(--d-primary)}
.d-icon-button{border-radius:50%;padding:10px}.d-chip{background:var(--d-variant);color:var(--d-on-surface)}.d-chip[aria-pressed=true]{border-color:var(--d-primary)}
.d-card{padding:var(--d-spacing);border-radius:var(--d-radius);background:var(--d-variant);box-shadow:0 var(--d-elevation) 8px #0001}
.d-field{display:flex;flex-direction:column;gap:6px}.d-field input,.d-field textarea,.d-field select{padding:12px;border:1px solid var(--d-outline);border-radius:var(--d-radius);color:var(--d-on-surface);background:var(--d-surface)}.d-field input::placeholder,.d-field textarea::placeholder{color:var(--d-field-placeholder)}
.d-field-control{display:flex;align-items:center;gap:8px}.d-field-control input,.d-field-control textarea{flex:1;min-width:0}
.d-error{color:var(--d-error)}.d-toggle{display:flex;gap:10px;align-items:center;min-height:44px}.d-toggle input{width:20px;height:20px}
.d-avatar{display:inline-grid;place-items:center;width:40px;height:40px;border-radius:50%;background:var(--d-variant)}.d-badge{border-radius:12px;padding:2px 8px;background:var(--d-primary);color:var(--d-on-primary)}
.d-divider{border:0;border-top:1px solid var(--d-outline);width:100%}.d-banner{padding:12px;border-radius:var(--d-radius);background:var(--d-variant)}
.d-collection{list-style:none;margin:0;padding:0}.d-tabs,.d-navigation{display:flex;gap:8px}.d-tabs button,.d-navigation button,.d-navigation a{text-decoration:none;display:inline-flex;align-items:center;padding:12px;border:0;background:transparent;color:var(--d-on-surface);min-height:44px}
.d-tabs [aria-selected=true],.d-navigation [aria-current=page]{color:var(--d-primary);border-bottom:3px solid var(--d-primary)}
.d-dialog{border:1px solid var(--d-outline);border-radius:var(--d-radius);padding:24px;color:var(--d-on-surface);background:var(--d-surface);max-width:min(600px,90vw)}
.d-dialog::backdrop{background:#0006}.d-sheet{margin:auto auto 0;min-width:min(600px,90vw)}
.d-scaffold{display:flex;flex-direction:column;height:100dvh}.d-scaffold-middle{display:flex;flex:1;min-height:0}.d-scaffold-body{flex:1;min-width:0;min-height:0;position:relative;overflow:auto}.d-floating{position:absolute;bottom:16px;right:16px}.d-snackbar{position:absolute;bottom:16px;left:16px}
.d-app-bar{display:flex;align-items:center;gap:12px;padding:12px 16px;min-height:64px;background:var(--d-surface)}.d-app-bar h2{font-size:var(--d-title);flex:1}.d-app-bar-actions{display:flex;gap:8px}.d-fab{min-width:56px;min-height:56px;border-radius:16px;box-shadow:0 3px 8px #0003}
.d-snackbar-message{display:flex;align-items:center;flex-wrap:wrap;gap:12px;padding:12px 16px;border-radius:var(--d-radius);background:var(--d-on-surface);color:var(--d-surface)}.d-snackbar-message button{border:0;background:transparent;color:inherit;min-height:44px;padding:8px}
.d-segments{display:flex;flex-wrap:wrap;gap:0}.d-segment{position:relative;cursor:pointer}.d-segment input{position:absolute;opacity:0;width:1px;height:1px}.d-segment span{display:flex;align-items:center;min-height:44px;padding:10px 16px;border:1px solid var(--d-outline)}.d-segment:first-child span{border-radius:var(--d-radius) 0 0 var(--d-radius)}.d-segment:last-child span{border-radius:0 var(--d-radius) var(--d-radius) 0}.d-segment input:checked+span{background:var(--d-variant);color:var(--d-primary)}.d-segment input:focus-visible+span{outline:3px solid var(--d-primary);outline-offset:3px}.d-segment input:disabled+span{opacity:.5;cursor:default}
.d-range{display:flex;flex-direction:column;gap:12px}.d-tooltip-anchor{display:inline-block;position:relative}.d-tooltip{position:absolute;z-index:5;bottom:100%;left:0;min-width:160px;max-width:280px;border-radius:4px;padding:8px 12px;background:var(--d-on-surface);color:var(--d-surface);font-size:var(--d-label)}
@media(prefers-reduced-motion:reduce){.d-theme *{transition:none!important;animation:none!important}}
.d-button,.d-chip,.d-icon-button,.d-field input,.d-field textarea,.d-navigation button,.d-navigation a{font:inherit;accent-color:var(--d-primary);box-sizing:border-box}
.d-button:focus-visible,.d-chip:focus-visible,.d-icon-button:focus-visible,.d-field :focus-visible,.d-navigation :focus-visible,.d-tabs :focus-visible,.d-toggle :focus-visible,.d-segment :focus-visible{outline:3px solid var(--d-primary);outline-offset:3px}
.d-field *,.d-toggle *,.d-segments *,.d-tabs *,.d-navigation *,.d-scaffold *,.d-dialog *{box-sizing:border-box}
@media(prefers-reduced-motion:reduce){.d-button,.d-chip,.d-icon-button,.d-field *,.d-tabs *,.d-navigation *,.d-scaffold *{transition:none!important;animation:none!important}.d-button-spinner{animation:none!important}}
@media(forced-colors:active){.d-button:not(:disabled):focus-visible{outline:2px solid Highlight;outline-offset:3px;box-shadow:none}}
"""
