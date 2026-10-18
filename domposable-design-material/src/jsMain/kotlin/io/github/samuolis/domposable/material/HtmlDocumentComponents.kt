package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.foundation.DomposableTextAlignment
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextDecoration
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.ui.DomposableDisclosureProps
import io.github.samuolis.domposable.ui.DomposableFormProps
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableLinkProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRichHtmlNode
import io.github.samuolis.domposable.ui.DomposableRichHtmlProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.ProvideDomposableFormSubmit
import io.github.samuolis.domposable.ui.domposableAttributes
import io.github.samuolis.domposable.ui.parseDomposableRichHtml
import org.jetbrains.compose.web.attributes.ButtonType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.type
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Form
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

@Composable
internal fun HtmlDomposableForm(
    props: DomposableFormProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val currentOnSubmit by rememberUpdatedState(props.onSubmit)
    DomposableHtmlModifiers(modifier) {
        Form(attrs = {
            domposableAttributes(modifier)
            props.label?.let { attr("aria-label", it) }
            if (!props.browserValidationEnabled) attr("novalidate", "")
            addEventListener("submit") { event ->
                event.preventDefault()
                if (props.enabled) currentOnSubmit()
            }
        }) {
            TagElement<HTMLElement>(tagName = "fieldset", applyAttrs = {
                if (!props.enabled) attr("disabled", "")
                style {
                    property("border", "0")
                    property("margin", "0")
                    property("min-width", "0")
                    property("padding", "0")
                }
            }) {
                props.label?.let {
                    TagElement<HTMLElement>(tagName = "legend", applyAttrs = {}) { Text(it) }
                }
                ProvideDomposableFormSubmit(props) { content() }
                Button(attrs = {
                    type(ButtonType.Submit)
                    attr("tabindex", "-1")
                    attr("aria-hidden", "true")
                    attr("data-domposable-implicit-submit", "")
                    style {
                        property("position", "absolute")
                        property("width", "1px")
                        property("height", "1px")
                        property("padding", "0")
                        property("margin", "-1px")
                        property("overflow", "hidden")
                        property("clip", "rect(0,0,0,0)")
                        property("white-space", "nowrap")
                        property("border", "0")
                    }
                }) {}
            }
        }
    }
}

@Composable
internal fun HtmlDomposableDisclosure(
    props: DomposableDisclosureProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val tokens = LocalDomposableTokens.current
    val appearance = props.appearance ?: tokens.components.disclosure
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-disclosure")
            style {
                property("width", "100%")
                property(
                    "border",
                    "${if (appearance.border == null) 0f else appearance.borderWidth.value}px solid " +
                        (appearance.border?.css() ?: "transparent"),
                )
                appearance.shape?.let { materialShape(it) } ?: property("border-radius", "${tokens.radius.value}px")
                property(
                    "background-color",
                    appearance.container?.css() ?: tokens.colors.surfaceContainerHighest?.css()
                        ?: tokens.colors.surfaceVariant.css(),
                )
                property("color", appearance.content?.css() ?: tokens.colors.onSurface.css())
                property("padding", "${tokens.spacing.medium.value}px")
                appearance.tonalElevation?.let { surfaceAppearance(appearance) }
                appearance.shadowElevation?.let { surfaceAppearance(appearance) }
                appearance.textAppearance?.let { applyCatalogTextAppearance(it) }
            }
        }) {
            Button(attrs = {
                type(ButtonType.Button)
                attr("aria-expanded", props.expanded.toString())
                style {
                    property("display", "flex")
                    property("width", "100%")
                    property("min-height", "24px")
                    property("align-items", "center")
                    property("justify-content", "space-between")
                    property("gap", "${tokens.spacing.small.value}px")
                    property("padding", "0")
                    property("border", "0")
                    property("color", "inherit")
                    property("background", "transparent")
                    property("font", "inherit")
                    property("text-align", "start")
                    property("cursor", "pointer")
                    applyCatalogTextAppearance(
                        appearance.textAppearance
                            ?: io.github.samuolis.domposable.foundation.DomposableTextAppearance(
                                role = io.github.samuolis.domposable.foundation.DomposableTypographyRole.TitleSmall,
                            ),
                    )
                }
                onClick { props.onToggle() }
            }) {
                Span { Text(props.title) }
                Span(attrs = { attr("aria-hidden", "true") }) { Text(if (props.expanded) "▴" else "▾") }
            }
            props.description?.let { description ->
                P(attrs = {
                    style {
                        property("font-size", "${appearance.textAppearance?.fontSize ?: tokens.typography.labelSize}px")
                        property(
                            "line-height",
                            appearance.textAppearance?.lineHeight?.let { "${it}px" }
                                ?: tokens.typography.lineHeight.toString(),
                        )
                        property("margin", "${tokens.spacing.small.value}px 0 0")
                        appearance.textAppearance?.let { applyCatalogTextAppearance(it) }
                    }
                }) { Text(description) }
            }
            if (props.expanded) {
                Div(attrs = { style { property("margin-top", "${tokens.spacing.small.value}px") } }) { content() }
            }
        }
    }
}

@Composable
internal fun HtmlDomposableRichHtml(
    props: DomposableRichHtmlProps,
    modifier: DomposableModifier,
) {
    val currentOnLinkClick by rememberUpdatedState(props.onLinkClick)
    val nodes = parseDomposableRichHtml(props.rawHtml)
    val tokens = LocalDomposableTokens.current
    val appearance = props.appearance ?: tokens.components.text
    val effectiveSelectable = appearance.selectable ?: props.selectable
    val fontSize =
        appearance.fontSize ?: when (props.style) {
            DomposableTextStyle.Body -> tokens.typography.bodySize
            DomposableTextStyle.Title -> tokens.typography.titleSize
            DomposableTextStyle.Label -> tokens.typography.labelSize
            DomposableTextStyle.Inherit -> null
        }
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-rich-html", "d-text", "d-text-${props.style.name.lowercase()}")
            attr("data-selectable", effectiveSelectable.toString())
            style {
                if (props.style != DomposableTextStyle.Inherit) {
                    property("font-family", tokens.typography.fontFamily)
                    fontSize?.let { property("font-size", "${it}px") }
                    property("line-height", tokens.typography.lineHeight.toString())
                }
                property("user-select", if (effectiveSelectable) "text" else "none")
                props.color?.let { property("color", it.css()) }
                applyCatalogTextAppearance(appearance)
            }
        }) {
            nodes.forEach { RenderHtmlRichNode(it, currentOnLinkClick) }
        }
    }
}

@Composable
private fun RenderHtmlRichNode(
    node: DomposableRichHtmlNode,
    onLinkClick: ((String) -> Unit)?,
) {
    when (node) {
        is DomposableRichHtmlNode.Text -> {
            Text(node.value)
        }

        is DomposableRichHtmlNode.Element -> {
            when (node.tag) {
                "br" -> {
                    Br()
                }

                "a" -> {
                    val href = node.href
                    if (href == null) {
                        RenderHtmlRichChildren(node.children, onLinkClick)
                    } else {
                        A(href = href, attrs = {
                            classes("d-rich-link")
                            style {
                                property("display", "inline")
                                property("min-height", "0")
                                property("padding", "0")
                                property("color", "var(--d-primary)")
                                property("text-decoration", "underline")
                            }
                            onLinkClick?.let { callback ->
                                onClick { event ->
                                    val mouse = event.nativeEvent.asDynamic()
                                    val modified =
                                        ((mouse.button as? Number)?.toInt() ?: 0) != 0 ||
                                            mouse.ctrlKey == true || mouse.metaKey == true ||
                                            mouse.shiftKey == true || mouse.altKey == true
                                    if (!modified) {
                                        event.preventDefault()
                                        callback(href)
                                    }
                                }
                            }
                        }) { RenderHtmlRichChildren(node.children, onLinkClick) }
                    }
                }

                "p" -> {
                    P { RenderHtmlRichChildren(node.children, onLinkClick) }
                }

                "h1", "h2", "h3", "ul", "ol", "li", "strong", "b", "em", "i", "blockquote", "code" -> {
                    TagElement<HTMLElement>(tagName = node.tag, applyAttrs = {}) {
                        RenderHtmlRichChildren(node.children, onLinkClick)
                    }
                }

                else -> {
                    RenderHtmlRichChildren(node.children, onLinkClick)
                }
            }
        }
    }
}

@Composable
private fun RenderHtmlRichChildren(
    children: List<DomposableRichHtmlNode>,
    onLinkClick: ((String) -> Unit)?,
) {
    children.forEach { RenderHtmlRichNode(it, onLinkClick) }
}

@Composable
internal fun HtmlDomposableLink(
    props: DomposableLinkProps,
    modifier: DomposableModifier,
) {
    val currentOnClick by rememberUpdatedState(props.onClick)
    val tokens = LocalDomposableTokens.current
    val appearance = props.appearance ?: tokens.components.link
    val fontSize =
        appearance?.fontSize ?: when (props.textStyle) {
            DomposableTextStyle.Body -> tokens.typography.bodySize
            DomposableTextStyle.Title -> tokens.typography.titleSize
            DomposableTextStyle.Label -> tokens.typography.labelSize
            DomposableTextStyle.Inherit -> null
        }
    val lineHeight =
        appearance?.lineHeight ?: fontSize?.times(tokens.typography.lineHeight)
    DomposableHtmlModifiers(modifier) {
        A(href = props.href, attrs = {
            domposableAttributes(modifier)
            classes("d-link")
            style {
                property("display", "inline")
                property("min-height", "0")
                property("padding", "0")
                property("align-items", "initial")
            }
            appearance?.maxLines?.let { maxLines ->
                style {
                    property("display", "-webkit-box")
                    property("-webkit-box-orient", "vertical")
                    property("-webkit-line-clamp", maxLines.toString())
                    property("overflow", "hidden")
                }
            }
            style {
                if (props.textStyle != DomposableTextStyle.Inherit) {
                    property("font-family", tokens.typography.fontFamily)
                }
                fontSize?.let { property("font-size", "${it}px") }
                lineHeight?.let { property("line-height", "${it}px") }
                appearance?.fontWeight?.let { property("font-weight", it.cssValue()) }
                when (appearance?.alignment) {
                    DomposableTextAlignment.Start -> property("text-align", "start")
                    DomposableTextAlignment.Center -> property("text-align", "center")
                    DomposableTextAlignment.End -> property("text-align", "end")
                    DomposableTextAlignment.Justify -> property("text-align", "justify")
                    null -> Unit
                }
                when (appearance?.decoration) {
                    DomposableTextDecoration.None -> property("text-decoration", "none")
                    DomposableTextDecoration.Underline -> property("text-decoration", "underline")
                    DomposableTextDecoration.LineThrough -> property("text-decoration", "line-through")
                    null -> Unit
                }
                appearance?.selectable?.let { property("user-select", if (it) "text" else "none") }
            }
            currentOnClick?.let { callback ->
                onClick { event ->
                    val mouse = event.nativeEvent.asDynamic()
                    val modified =
                        ((mouse.button as? Number)?.toInt() ?: 0) != 0 ||
                            mouse.ctrlKey == true || mouse.metaKey == true ||
                            mouse.shiftKey == true || mouse.altKey == true
                    if (!modified) {
                        event.preventDefault()
                        callback()
                    }
                }
            }
        }) { Text(props.label) }
    }
}

private fun DomposableFontWeight.cssValue(): String =
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
