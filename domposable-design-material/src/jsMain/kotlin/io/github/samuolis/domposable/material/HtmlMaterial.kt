package io.github.samuolis.domposable.material

import androidx.compose.runtime.*
import io.github.samuolis.domposable.foundation.*
import io.github.samuolis.domposable.ui.*
import kotlinx.browser.document
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLDialogElement
import org.w3c.dom.HTMLElement

private fun DomposableAlignment.css() = when (this) {
    DomposableAlignment.Start -> "flex-start"
    DomposableAlignment.Center -> "center"
    DomposableAlignment.End -> "flex-end"
}

@Composable
actual fun rememberDomposableMaterialComponents(): DomposableComponents = remember {
    DomposableComponents(
        theme = { props, _, content -> HtmlTheme(props.tokens, content) },
        column = { props, modifier, content -> DomposableHtmlModifiers(modifier) { Div(attrs = { domposableAttributes(modifier); style { property("display", "flex"); property("flex-direction", "column"); property("gap", "${props.gap.value}px"); property("align-items", props.alignment.css()) } }) { content() } } },
        row = { props, modifier, content -> DomposableHtmlModifiers(modifier) { Div(attrs = { domposableAttributes(modifier); style { property("display", "flex"); property("gap", "${props.gap.value}px"); property("align-items", props.alignment.css()) } }) { content() } } },
        flowRow = { props, modifier, content -> DomposableHtmlModifiers(modifier) { Div(attrs = { domposableAttributes(modifier); style { property("display", "flex"); property("flex-wrap", "wrap"); property("gap", "${props.gap.value}px") } }) { content() } } },
        link = { props, modifier, _ -> DomposableHtmlModifiers(modifier) { A(href = props.href, attrs = { domposableAttributes(modifier); classes("d-link") }) { Text(props.label) } } },
        box = { props, modifier, content -> DomposableHtmlModifiers(modifier) { Div(attrs = { domposableAttributes(modifier); style { property("display", "grid"); property("place-items", when(props.alignment) { DomposableAlignment.Start -> "start"; DomposableAlignment.Center -> "center"; DomposableAlignment.End -> "end" }) } }) { content() } } },
        lazyColumn = { props, modifier, _ -> HtmlCollection(props.items, modifier, props.gap, "column") },
        lazyRow = { props, modifier, _ -> HtmlCollection(props.items, modifier, props.gap, "row") },
        lazyGrid = { props, modifier, _ -> HtmlCollection(props.items, modifier, props.gap, "grid", props.columns) },
        text = { props, modifier, _ -> DomposableHtmlModifiers(modifier) {
            if (props.style == DomposableTextStyle.Title || props.headingLevel != null) TagElement<HTMLElement>("h${props.headingLevel ?: 2}", applyAttrs = { domposableAttributes(modifier); classes("d-title"); props.color?.let { style { property("color", it.css()) } } }) { Text(props.text) }
            else Span(attrs = { domposableAttributes(modifier); classes(if (props.style == DomposableTextStyle.Label) "d-label" else "d-text"); props.color?.let { style { property("color", it.css()) } } }) { Text(props.text) }
        } },
        button = { props, modifier, content -> DomposableHtmlModifiers(modifier) { Button(attrs = { domposableAttributes(modifier); attr("type", "button"); classes("d-button", "d-button-${props.style.name.lowercase()}"); if (!props.enabled) disabled(); onClick { ((it.target as? HTMLElement)?.closest("button") as? HTMLElement)?.focus(); props.onClick() } }) { content() } } },
        iconButton = { props, modifier, content -> DomposableHtmlModifiers(modifier) { Button(attrs = { domposableAttributes(modifier); attr("type", "button"); attr("aria-label", props.label); classes("d-icon-button"); if (!props.enabled) disabled(); onClick { props.onClick() } }) { content() } } },
        textField = { props, modifier, _ -> DomposableHtmlModifiers(modifier) {
            val id = remember { "d-input-${nextInputId++}" }
            Label(attrs = { classes("d-field"); attr("for", id) }) {
                Span { Text(props.label) }
                if (props.multiline) TextArea(value = props.value, attrs = { id(id); domposableAttributes(modifier); if (!props.enabled) disabled(); onInput { props.onValueChange(it.target.value) }; props.error?.let { attr("aria-invalid", "true"); attr("aria-describedby", "$id-error") } })
                else Input(type = when (props.purpose) { DomposableInputPurpose.Email -> InputType.Email; DomposableInputPurpose.Password -> InputType.Password; DomposableInputPurpose.Number -> InputType.Number; DomposableInputPurpose.Search -> InputType.Search; DomposableInputPurpose.Date -> InputType.Date; DomposableInputPurpose.Time -> InputType.Time; else -> InputType.Text }, attrs = {
                    id(id); domposableAttributes(modifier); value(props.value); if (!props.enabled) disabled(); onInput { props.onValueChange(it.target.value) }; props.error?.let { attr("aria-invalid", "true"); attr("aria-describedby", "$id-error") }
                })
                props.error?.let { error -> Span(attrs = { id("$id-error"); classes("d-error") }) { Text(error) } }
            }
        } },
        checkbox = { props, modifier, _ -> HtmlToggle(props.checked, props.onCheckedChange, props.label, props.enabled, modifier, false) },
        switch = { props, modifier, _ -> HtmlToggle(props.checked, props.onCheckedChange, props.label, props.enabled, modifier, true) },
        radioButton = { props, modifier, _ -> DomposableHtmlModifiers(modifier) { Label(attrs = { classes("d-toggle") }) { Input(InputType.Radio, attrs = { domposableAttributes(modifier); checked(props.selected); attr("aria-label", props.label); if (!props.enabled) disabled(); onChange { props.onClick() } }); Text(props.label) } } },
        slider = { props, modifier, _ -> DomposableHtmlModifiers(modifier) { Input(InputType.Range, attrs = { domposableAttributes(modifier); value(props.value.toString()); attr("aria-label", props.label); attr("min", props.range.start.toString()); attr("max", props.range.endInclusive.toString()); attr("step", if (props.steps == 0) "any" else ((props.range.endInclusive - props.range.start)/(props.steps + 1)).toString()); if (!props.enabled) disabled(); onInput { it.target.value.toFloatOrNull()?.let(props.onValueChange) } }) } },
        card = { props, modifier, content -> DomposableHtmlModifiers(modifier) { Div(attrs = { domposableAttributes(modifier); classes("d-card"); props.onClick?.let { click -> attr("role", "button"); attr("tabindex", "0"); onClick { click() }; onKeyDown { if (it.key == "Enter" || it.key == " ") { it.preventDefault(); click() } } } }) { content() } } },
        surface = { props, modifier, content -> DomposableHtmlModifiers(modifier) { Div(attrs = { domposableAttributes(modifier); classes("d-surface"); props.color?.let { style { property("background", it.css()) } } }) { content() } } },
        image = { props, modifier, _ -> HtmlImage(props, modifier) },
        icon = { props, modifier, _ -> DomposableHtmlModifiers(modifier) { Span(attrs = { domposableAttributes(modifier); if (props.description == null) attr("aria-hidden", "true") else { attr("role", "img"); attr("aria-label", props.description.orEmpty()) } }) { Text(props.symbol) } } },
        divider = { props, modifier, _ -> DomposableHtmlModifiers(modifier) { Hr(attrs = { domposableAttributes(modifier); attr("aria-orientation", props.orientation.name.lowercase()); classes("d-divider") }) } },
        spacer = { props, modifier, _ -> DomposableHtmlModifiers(modifier) { Div(attrs = { domposableAttributes(modifier); attr("aria-hidden", "true"); style { property("width", "${props.length.value}px"); property("height", "${props.length.value}px") } }) {} } },
        badge = { props, modifier, _ -> DomposableHtmlModifiers(modifier) { Span(attrs = { domposableAttributes(modifier); classes("d-badge") }) { Text(props.text) } } },
        chip = { props, modifier, _ -> DomposableHtmlModifiers(modifier) { Button(attrs = { domposableAttributes(modifier); attr("type", "button"); attr("aria-pressed", props.selected.toString()); classes("d-chip"); if (!props.enabled) disabled(); onClick { props.onClick() } }) { Text(props.label) } } },
        avatar = { props, modifier, _ -> DomposableHtmlModifiers(modifier) { Span(attrs = { domposableAttributes(modifier); attr("role", "img"); attr("aria-label", props.description.orEmpty()); classes("d-avatar") }) { Text(props.initials) } } },
        banner = { props, modifier, _ -> DomposableHtmlModifiers(modifier) { Div(attrs = { domposableAttributes(modifier); attr("role", if (props.isError) "alert" else "status"); classes("d-banner") }) { Text(props.message) } } },
        progress = { props, modifier, _ -> DomposableHtmlModifiers(modifier) { TagElement<HTMLElement>("progress", applyAttrs = { domposableAttributes(modifier); attr("aria-label", props.label); attr("max", "1"); props.value?.let { attr("value", it.toString()) }; classes("d-progress") }) {} } },
        tabs = { props, modifier, _ -> DomposableHtmlModifiers(modifier) { Div(attrs = { domposableAttributes(modifier); attr("role", "tablist"); attr("aria-label", "Sections"); classes("d-tabs") }) { props.labels.forEachIndexed { index, label ->
            Button(attrs = { attr("type", "button"); attr("role", "tab"); attr("aria-selected", (props.selectedIndex == index).toString()); attr("tabindex", if (props.selectedIndex == index) "0" else "-1"); onClick { props.onSelect(index) }; onKeyDown { event ->
                val offset = when(event.key) { "ArrowRight" -> 1; "ArrowLeft" -> -1; else -> 0 }
                if (offset != 0) { event.preventDefault(); val next = (index + offset + props.labels.size) % props.labels.size; props.onSelect(next); ((event.target as? HTMLElement)?.parentElement?.children?.item(next) as? HTMLElement)?.focus() }
            } }) { Text(label) }
        } } } },
        navigationBar = { props, modifier, _ -> DomposableHtmlModifiers(modifier) { Nav(attrs = { domposableAttributes(modifier); attr("aria-label", "Application"); classes("d-navigation"); style { property("flex-direction", if(props.orientation == DomposableOrientation.Horizontal) "row" else "column") } }) { props.items.forEachIndexed { index, item ->
            if (item.href != null) A(href = item.href, attrs = {
                if (index == props.selectedIndex) attr("aria-current", "page")
                onClick { event ->
                    if (event.button.toInt() == 0 && !event.ctrlKey && !event.metaKey && !event.shiftKey && !event.altKey) {
                        event.preventDefault(); props.onSelect(index)
                    }
                }
            }) { Text("${item.symbol} ${item.label}") }
            else Button(attrs = { attr("type", "button"); if (index == props.selectedIndex) attr("aria-current", "page"); onClick { props.onSelect(index) } }) { Text("${item.symbol} ${item.label}") }
        } } } },
        menu = { props, modifier, _ -> if (props.expanded) HtmlModal("Menu", props.onDismiss, modifier, sheet = false) { props.items.forEach { item -> Button(attrs = { attr("type", "button"); if (!item.enabled) disabled(); onClick { item.onClick(); props.onDismiss() } }) { Text(item.label) } } } },
        dialog = { props, modifier, content -> HtmlModal(props.title, props.onDismiss, modifier, sheet = false) { content(); props.actions() } },
        bottomSheet = { props, modifier, content -> HtmlModal(props.title, props.onDismiss, modifier, sheet = true, content) },
        scaffold = { props, modifier, content -> DomposableHtmlModifiers(modifier) { Div(attrs = { domposableAttributes(modifier); classes("d-scaffold") }) {
            Header {
                A(href = "#main-content", attrs = { classes("d-skip-link"); onClick { it.preventDefault(); (document.getElementById("main-content") as? HTMLElement)?.focus() } }) { Text("Skip to content") }
                props.topBar()
            }
            Div(attrs = { classes("d-scaffold-middle") }) {
                props.navigation()
                Main(attrs = { id("main-content"); attr("tabindex", "-1"); classes("d-scaffold-body") }) { content(); Div(attrs = { classes("d-floating") }) { props.floatingAction() }; Div(attrs = { classes("d-snackbar") }) { props.snackbar() } }
                Aside { props.contextual() }
            }
            Footer { props.bottomBar() }
            props.overlays()
        } } },
    )
}

@Composable
private fun HtmlImage(props: DomposableImageProps, modifier: DomposableModifier) {
    val loaded by rememberUpdatedState(props.onLoaded)
    val failed by rememberUpdatedState(props.onError)
    // A sized leaf must fit inside the ordered layout boxes, including inner padding.
    var imageHeight: Float? = null
    modifier.operations.forEach { operation -> when (operation) {
        is DomposableModifierOperation.Height -> imageHeight = operation.length.value
        is DomposableModifierOperation.Padding -> imageHeight = imageHeight?.let { (it - operation.insets.top.value - operation.insets.bottom.value).coerceAtLeast(0f) }
        DomposableModifierOperation.FillHeight -> imageHeight = null
        else -> Unit
    } }
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
                property("object-fit", when (props.scale) {
                    DomposableImageScale.Fit -> "contain"
                    DomposableImageScale.Crop -> "cover"
                    DomposableImageScale.Fill -> "fill"
                })
                imageHeight?.let { property("height", "${it}px") }
                if (modifier.operations.any { it is DomposableModifierOperation.Width || it == DomposableModifierOperation.FillWidth }) property("width", "100%")
            }
        })
    }
}

private var nextInputId = 0

@Composable
private fun HtmlToggle(checkedValue: Boolean, onChange: (Boolean) -> Unit, label: String, enabled: Boolean, modifier: DomposableModifier, switch: Boolean) {
    DomposableHtmlModifiers(modifier) { Label(attrs = { classes("d-toggle") }) {
        Input(InputType.Checkbox, attrs = { domposableAttributes(modifier); checked(checkedValue); if (switch) attr("role", "switch"); if (!enabled) disabled(); onChange { onChange(it.value) } })
        Text(label)
    } }
}

@Composable
private fun HtmlCollection(items: List<DomposableLazyItem>, modifier: DomposableModifier, gap: DomposableLength, layout: String, columns: Int = 1) {
    require(columns > 0)
    require(items.map { it.key }.distinct().size == items.size) { "Collection keys must be unique" }
    DomposableHtmlModifiers(modifier) { Ul(attrs = { domposableAttributes(modifier); classes("d-collection"); style { property("display", if (layout == "grid") "grid" else "flex"); property("flex-direction", layout); property("gap", "${gap.value}px"); if (layout == "grid") property("grid-template-columns", "repeat($columns, minmax(0, 1fr))") } }) {
        items.forEach { item -> key(item.key) { Li { item.content() } } }
    } }
}

@Composable
private fun HtmlModal(title: String, onDismiss: () -> Unit, modifier: DomposableModifier, sheet: Boolean, content: @Composable () -> Unit) {
    val dismiss by rememberUpdatedState(onDismiss)
    val titleId = remember { "d-dialog-${nextInputId++}" }
    DomposableHtmlModifiers(modifier) { TagElement<HTMLDialogElement>("dialog", applyAttrs = {
        domposableAttributes(modifier); attr("aria-labelledby", titleId); classes("d-dialog"); if (sheet) classes("d-sheet")
        ref { element ->
            val previousFocus = document.activeElement as? HTMLElement
            val cancel: (org.w3c.dom.events.Event) -> Unit = { it.preventDefault(); dismiss() }
            element.addEventListener("cancel", cancel)
            element.showModal()
            onDispose { element.removeEventListener("cancel", cancel); element.close(); previousFocus?.focus() }
        }
    }) {
        H2(attrs = { id(titleId) }) { Text(title) }
        content()
        Button(attrs = { attr("type", "button"); onClick { dismiss() } }) { Text("Close") }
    } }
}

@Composable
private fun HtmlTheme(tokens: DomposableTokens, content: @Composable () -> Unit) {
    DisposableEffect(Unit) {
        val style = document.createElement("style")
        style.textContent = MATERIAL_CSS
        document.head!!.appendChild(style)
        onDispose { style.remove() }
    }
    Div(attrs = { classes("d-theme"); style {
        property("--d-primary", tokens.colors.primary.css()); property("--d-on-primary", tokens.colors.onPrimary.css())
        property("--d-surface", tokens.colors.surface.css()); property("--d-on-surface", tokens.colors.onSurface.css())
        property("--d-variant", tokens.colors.surfaceVariant.css()); property("--d-outline", tokens.colors.outline.css()); property("--d-error", tokens.colors.error.css())
        property("--d-font", tokens.typography.fontFamily); property("--d-body", "${tokens.typography.bodySize}px"); property("--d-title", "${tokens.typography.titleSize}px")
        property("--d-label", "${tokens.typography.labelSize}px"); property("--d-line-height", tokens.typography.lineHeight.toString())
        property("--d-spacing", "${tokens.spacing.medium.value}px"); property("--d-radius", "${tokens.radius.value}px")
        property("--d-elevation", "${tokens.elevation.value}px"); property("--d-motion", "${tokens.motionDurationMillis}ms")
    } }) { content() }
}

private const val MATERIAL_CSS = """
.d-theme *{box-sizing:border-box}.d-link{color:var(--d-primary);padding:8px;display:inline-flex;align-items:center;min-height:44px}.d-theme{color:var(--d-on-surface);background:var(--d-surface);font:var(--d-body)/var(--d-line-height) var(--d-font);min-height:100%;box-sizing:border-box}
.d-theme button,.d-theme input,.d-theme textarea{font:inherit;accent-color:var(--d-primary)}
.d-theme button{cursor:pointer}.d-theme button:disabled{cursor:default;opacity:.5}
.d-theme :focus-visible{outline:3px solid var(--d-primary);outline-offset:3px}
.d-skip-link{position:absolute;left:-999px}.d-skip-link:focus{left:12px;top:12px;z-index:10}
.d-theme h1,.d-theme h2,.d-theme h3{margin:0}.d-title{font-size:var(--d-title)}.d-label{font-size:var(--d-label)}
.d-button,.d-chip,.d-icon-button{border:1px solid transparent;padding:10px 20px;border-radius:var(--d-radius);min-height:44px;background:var(--d-primary);color:var(--d-on-primary);transition:background var(--d-motion)}
.d-button-outlined{background:transparent;color:var(--d-primary);border-color:var(--d-outline)}.d-button-text{background:transparent;color:var(--d-primary)}
.d-icon-button{border-radius:50%;padding:10px}.d-chip{background:var(--d-variant);color:var(--d-on-surface)}.d-chip[aria-pressed=true]{border-color:var(--d-primary)}
.d-card{padding:var(--d-spacing);border-radius:var(--d-radius);background:var(--d-variant);box-shadow:0 var(--d-elevation) 8px #0001}
.d-field{display:flex;flex-direction:column;gap:6px}.d-field input,.d-field textarea{padding:12px;border:1px solid var(--d-outline);border-radius:var(--d-radius);color:var(--d-on-surface);background:var(--d-surface)}
.d-error{color:var(--d-error)}.d-toggle{display:flex;gap:10px;align-items:center;min-height:44px}.d-toggle input{width:20px;height:20px}
.d-avatar{display:inline-grid;place-items:center;width:40px;height:40px;border-radius:50%;background:var(--d-variant)}.d-badge{border-radius:12px;padding:2px 8px;background:var(--d-primary);color:var(--d-on-primary)}
.d-divider{border:0;border-top:1px solid var(--d-outline);width:100%}.d-banner{padding:12px;border-radius:var(--d-radius);background:var(--d-variant)}
.d-collection{list-style:none;margin:0;padding:0}.d-tabs,.d-navigation{display:flex;gap:8px}.d-tabs button,.d-navigation button,.d-navigation a{text-decoration:none;display:inline-flex;align-items:center;padding:12px;border:0;background:transparent;color:var(--d-on-surface);min-height:44px}
.d-tabs [aria-selected=true],.d-navigation [aria-current=page]{color:var(--d-primary);border-bottom:3px solid var(--d-primary)}
.d-dialog{border:1px solid var(--d-outline);border-radius:var(--d-radius);padding:24px;color:var(--d-on-surface);background:var(--d-surface);max-width:min(600px,90vw)}
.d-dialog::backdrop{background:#0006}.d-sheet{margin:auto auto 0;min-width:min(600px,90vw)}
.d-scaffold{display:flex;flex-direction:column;height:100dvh}.d-scaffold-middle{display:flex;flex:1;min-height:0}.d-scaffold-body{flex:1;min-width:0;min-height:0;position:relative;overflow:auto}.d-floating{position:absolute;bottom:16px;right:16px}.d-snackbar{position:absolute;bottom:16px;left:16px}
@media(prefers-reduced-motion:reduce){.d-theme *{transition:none!important;animation:none!important}}
"""
