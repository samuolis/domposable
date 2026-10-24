package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableAutocomplete
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableChipVariant
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableControlPlacement
import io.github.samuolis.domposable.foundation.DomposableDesignLanguage
import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.foundation.DomposableIconButtonVariant
import io.github.samuolis.domposable.foundation.DomposableImageScale
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableLiveRegion
import io.github.samuolis.domposable.foundation.DomposableOffset
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableScaffoldFabPosition
import io.github.samuolis.domposable.foundation.DomposableSelectionColors
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableSwitchColors
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextOverflow
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.DomposableTypographyRole
import io.github.samuolis.domposable.ui.DomposableArrangement
import io.github.samuolis.domposable.ui.DomposableBadgedBoxProps
import io.github.samuolis.domposable.ui.DomposableButtonProps
import io.github.samuolis.domposable.ui.DomposableComponents
import io.github.samuolis.domposable.ui.DomposableDataTableProps
import io.github.samuolis.domposable.ui.DomposableHtmlMenuAnchor
import io.github.samuolis.domposable.ui.DomposableHtmlMenuPlacement
import io.github.samuolis.domposable.ui.DomposableHtmlMenuPlacementHint
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableImageProps
import io.github.samuolis.domposable.ui.DomposableLabelProps
import io.github.samuolis.domposable.ui.DomposableLazyItem
import io.github.samuolis.domposable.ui.DomposableLineChartProps
import io.github.samuolis.domposable.ui.DomposableMenuGroupProps
import io.github.samuolis.domposable.ui.DomposableMenuItem
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableModifierOperation
import io.github.samuolis.domposable.ui.DomposableNavigationItem
import io.github.samuolis.domposable.ui.DomposableScrollFieldProps
import io.github.samuolis.domposable.ui.DomposableSnackbarHostProps
import io.github.samuolis.domposable.ui.DomposableValueChartProps
import io.github.samuolis.domposable.ui.DomposableWebNavigationLayout
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.background
import io.github.samuolis.domposable.ui.contentDescription
import io.github.samuolis.domposable.ui.domposableAttributes
import io.github.samuolis.domposable.ui.domposableFormEnabled
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.safeDomposableHref
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.width
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Aside
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Nav
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import org.jetbrains.compose.web.dom.Ul
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.events.Event

@Composable
private fun HtmlSurfaceBorderOverlay(appearance: DomposableSurfaceAppearance) {
    val border = appearance.border ?: return
    if (appearance.borderWidth.value <= 0f) return
    Span(attrs = {
        attr("aria-hidden", "true")
        style {
            property("position", "absolute")
            property("inset", "0")
            property("box-sizing", "border-box")
            property("pointer-events", "none")
            property("border", "${appearance.borderWidth.value}px solid ${border.css()}")
            property("border-radius", "inherit")
        }
    }) { }
}

private const val HTML_MENU_VIEWPORT_MARGIN = 8.0

private fun positionHtmlMenu(
    overlay: HTMLElement,
    anchor: HTMLElement?,
    offset: DomposableOffset,
    alignToLogicalEnd: Boolean,
    placement: DomposableHtmlMenuPlacement = DomposableHtmlMenuPlacement.Auto,
) {
    val menu = overlay.querySelector(".d-menu-position") as? HTMLElement ?: return
    if (anchor == null) return
    val anchorBounds = anchor.getBoundingClientRect()
    val viewportWidth = window.innerWidth.toDouble()
    val viewportHeight = window.innerHeight.toDouble()
    val menuWidth = menu.getBoundingClientRect().width
    val menuHeight =
        menu.getBoundingClientRect().height.coerceAtMost(
            (viewportHeight - 2 * HTML_MENU_VIEWPORT_MARGIN).coerceAtLeast(0.0),
        )
    val rtl = window.getComputedStyle(anchor).direction == "rtl"
    val anchorLeft =
        if (alignToLogicalEnd || rtl) {
            anchorBounds.right - menuWidth
        } else {
            anchorBounds.left
        }
    val horizontalOffset = if (rtl) -offset.x else offset.x
    val left =
        (anchorLeft + horizontalOffset).coerceIn(
            HTML_MENU_VIEWPORT_MARGIN,
            (viewportWidth - menuWidth - HTML_MENU_VIEWPORT_MARGIN).coerceAtLeast(HTML_MENU_VIEWPORT_MARGIN),
        )
    val below = viewportHeight - anchorBounds.bottom - offset.y
    val above = anchorBounds.top - offset.y
    val placeBelowAutomatically =
        below >= menuHeight + HTML_MENU_VIEWPORT_MARGIN ||
            above < menuHeight + HTML_MENU_VIEWPORT_MARGIN
    val placeBelow =
        when {
            placement == DomposableHtmlMenuPlacement.Above && above >= menuHeight + HTML_MENU_VIEWPORT_MARGIN -> false
            placement == DomposableHtmlMenuPlacement.Below && below >= menuHeight + HTML_MENU_VIEWPORT_MARGIN -> true
            else -> placeBelowAutomatically
        }
    val verticalOffset = if (placeBelow) offset.y else -offset.y
    val maxTop =
        (viewportHeight - menuHeight - HTML_MENU_VIEWPORT_MARGIN).coerceAtLeast(HTML_MENU_VIEWPORT_MARGIN)
    val top =
        if (placeBelow) {
            anchorBounds.bottom + verticalOffset
        } else {
            anchorBounds.top - menuHeight + verticalOffset
        }.coerceIn(HTML_MENU_VIEWPORT_MARGIN, maxTop)
    menu.style.setProperty("position", "fixed")
    menu.style.setProperty("margin", "0")
    menu.style.setProperty("left", "${left}px")
    menu.style.setProperty("right", "auto")
    menu.style.setProperty("inset-inline-end", "auto")
    menu.style.setProperty("top", "${top}px")
    menu.style.setProperty("bottom", "auto")
    menu.style.setProperty("inset-block-end", "auto")
}

internal fun DomposableFontWeight.css() = weight.toString()

@Composable
private fun navItemContent(item: DomposableNavigationItem) {
    item.icon?.invoke() ?: item.symbol.takeIf(String::isNotEmpty)?.let { symbol ->
        Span(attrs = { attr("aria-hidden", "true") }) { Text(symbol) }
    }
    Span { Text(item.label) }
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
                        classes("d-layout-column")
                        style {
                            property("display", "flex")
                            property("flex-direction", "column")
                            property(
                                "row-gap",
                                "${props.gap.value}px",
                            )
                            property("align-items", props.alignment.css())
                            property("justify-content", props.arrangement.css())
                        }
                    }) { content() }
                }
            },
            row = {
                props,
                modifier,
                content,
                ->
                val horizontalScroll =
                    modifier.elements.any {
                        when (it) {
                            is DomposableModifierOperation.Scroll -> {
                                it.orientation == DomposableOrientation.Horizontal
                            }

                            is DomposableModifierOperation.StatefulScroll -> {
                                it.orientation ==
                                    DomposableOrientation.Horizontal
                            }

                            else -> {
                                false
                            }
                        }
                    }
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        classes("d-row")
                        if (horizontalScroll) attr("data-domposable-horizontal-scroll", "true")
                        if (props.stretch) classes("d-row-stretch")
                        style {
                            property("display", "flex")
                            property("min-width", "0")
                            if (horizontalScroll) {
                                property("width", "max-content")
                                property("min-width", "100%")
                            }
                            property(
                                "column-gap",
                                "${props.gap.value}px",
                            )
                            property("align-items", if (props.stretch) "stretch" else props.alignment.css())
                            property("justify-content", props.arrangement.css())
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
                        classes("d-row", "d-flow-row")
                        style {
                            property("display", "flex")
                            property("min-width", "0")
                            property("flex-wrap", "wrap")
                            property(
                                "column-gap",
                                "${props.gap.value}px",
                            )
                            property("row-gap", "${props.rowGap.value}px")
                            property("justify-content", props.arrangement.css())
                            property("align-items", props.alignment.css())
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
                        classes("d-box")
                        style {
                            property("display", "grid")
                            property("position", "relative")
                            property("grid-template-columns", "minmax(0,1fr)")
                            property("justify-items", props.alignment.css())
                            property("align-items", props.verticalAlignment.css())
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
                val tokens = LocalDomposableTokens.current
                val appearance = props.appearance ?: LocalMaterialButtonTextAppearance.current ?: tokens.components.text
                val role =
                    appearance?.role ?: when (props.style) {
                        DomposableTextStyle.Body -> DomposableTypographyRole.BodyLarge
                        DomposableTextStyle.Title -> DomposableTypographyRole.TitleLarge
                        DomposableTextStyle.Label -> DomposableTypographyRole.LabelLarge
                        DomposableTextStyle.Inherit -> null
                    }
                val liveRegion =
                    when (appearance?.liveRegion) {
                        DomposableLiveRegion.Polite -> "polite"
                        DomposableLiveRegion.Assertive -> "assertive"
                        null -> null
                    }

                fun <T : HTMLElement> AttrsScope<T>.applyAppearance() {
                    val inlineText = props.style != DomposableTextStyle.Title && props.headingLevel == null
                    style {
                        if (props.style != DomposableTextStyle.Inherit && appearance?.alignment == null) {
                            property("text-align", "start")
                        }
                        appearance?.let { applyCatalogTextAppearance(it) }
                    }
                    (props.color ?: appearance?.color)?.let { style { property("color", it.css()) } }
                    appearance?.minLines?.takeIf { it > 1 }?.let { lines ->
                        style {
                            val clampsLines =
                                appearance.maxLines != null && appearance.overflow == DomposableTextOverflow.Ellipsis
                            if (inlineText && !clampsLines) property("display", "inline-block")
                            property("min-height", "${lines}lh")
                        }
                    }
                    if (appearance?.maxLines != null && appearance.overflow == DomposableTextOverflow.Clip &&
                        inlineText
                    ) {
                        style { property("display", "inline-block") }
                    }
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
                            style { applyCatalogTextAppearance(DomposableTextAppearance(role = role)) }
                            domposableAttributes(modifier)
                            if (props.style == DomposableTextStyle.Inherit) {
                                classes("d-text", "d-text-inherit")
                            } else {
                                classes("d-title")
                            }
                            props.color?.let { style { property("color", it.css()) } }
                            applyAppearance()
                        }) { HtmlTextRuns(props) }
                    } else {
                        Span(attrs = {
                            style { applyCatalogTextAppearance(DomposableTextAppearance(role = role)) }
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
                            if (props.style == DomposableTextStyle.Inherit) classes("d-text-inherit")
                            props.color?.let { style { property("color", it.css()) } }
                            applyAppearance()
                        }) { HtmlTextRuns(props) }
                    }
                }
            },
            button = { props, modifier, content -> HtmlButton(props, modifier, content) },
            iconButton = {
                props,
                modifier,
                content,
                ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.iconButton
                val tokens = LocalDomposableTokens.current
                val enabled = props.enabled && domposableFormEnabled()
                DomposableHtmlModifiers(modifier) {
                    MaterialButtonHitTarget(modifier, appearance.minHeight?.value) {
                        Button(attrs = {
                            domposableAttributes(modifier)
                            attr("type", "button")
                            attr("aria-label", props.label)
                            classes("d-icon-button")
                            if (appearance.interaction != null) attr("data-d-button-interaction", "true")
                            classes(
                                "d-icon-button-${props.variant.name.lowercase().replace(
                                    "filledtonal",
                                    "filled-tonal",
                                )}",
                            )
                            props.checked?.let { attr("aria-pressed", it.toString()) }
                            if (!enabled) attr("disabled", "")
                            style {
                                val checked = props.checked == true
                                val variantContainer =
                                    when (props.variant) {
                                        DomposableIconButtonVariant.Standard -> {
                                            if (checked) {
                                                tokens.colors.secondaryContainer
                                                    ?: tokens.colors.surfaceVariant
                                            } else {
                                                null
                                            }
                                        }

                                        DomposableIconButtonVariant.Filled -> {
                                            if (props.checked == false) {
                                                tokens.colors.surfaceContainerHighest
                                                    ?: tokens.colors.surfaceVariant
                                            } else {
                                                tokens.colors.primary
                                            }
                                        }

                                        DomposableIconButtonVariant.FilledTonal -> {
                                            tokens.colors.secondaryContainer ?: tokens.colors.surfaceVariant
                                        }

                                        DomposableIconButtonVariant.Outlined -> {
                                            null
                                        }
                                    }
                                val variantContent =
                                    when (props.variant) {
                                        DomposableIconButtonVariant.Standard -> {
                                            if (checked) {
                                                tokens.colors.onSecondaryContainer
                                                    ?: tokens.colors.onSurface
                                            } else {
                                                tokens.colors.onSurface
                                            }
                                        }

                                        DomposableIconButtonVariant.Filled -> {
                                            if (props.checked == false) {
                                                tokens.colors.onSurfaceVariant ?: tokens.colors.onSurface
                                            } else {
                                                tokens.colors.onPrimary
                                            }
                                        }

                                        DomposableIconButtonVariant.FilledTonal -> {
                                            tokens.colors.onSecondaryContainer ?: tokens.colors.onSurface
                                        }

                                        DomposableIconButtonVariant.Outlined -> {
                                            tokens.colors.onSurface
                                        }
                                    }
                                appearance.container?.let { property("background", it.css()) }
                                    ?: variantContainer?.let { property("background", it.css()) }
                                appearance.content?.let { property("color", it.css()) }
                                    ?: property("color", variantContent.css())
                                appearance.border?.let { property("border-color", it.css()) }
                                if (appearance.border == null &&
                                    props.variant ==
                                    DomposableIconButtonVariant.Outlined
                                ) {
                                    property("border-color", tokens.colors.outline.css())
                                }
                                if (props.variant ==
                                    DomposableIconButtonVariant.Outlined &&
                                    appearance.borderWidth == null
                                ) {
                                    property("border-width", "1px")
                                }
                                (if (enabled) appearance.container else appearance.disabledContainer)?.let {
                                    property(
                                        "background",
                                        it.css(),
                                    )
                                }
                                (if (enabled) appearance.content else appearance.disabledContent)?.let {
                                    property(
                                        "color",
                                        it.css(),
                                    )
                                }
                                (if (enabled) appearance.border else appearance.disabledBorder)?.let {
                                    property("border", "${appearance.borderWidth?.value ?: 1f}px solid ${it.css()}")
                                }
                                appearance.borderWidth?.let { property("border-width", "${it.value}px") }
                                appearance.shape?.let { materialShape(it) }
                                appearance.minHeight?.let { property("min-height", "${it.value}px") }
                                appearance.minWidth?.let { property("min-width", "${it.value}px") }
                                appearance.contentPadding?.let { property("padding", it.cssPadding()) }
                                appearance.textAppearance?.let { applyCatalogTextAppearance(it) }
                                buttonInteractionAppearance(appearance)
                                if (!enabled) appearance.disabledOpacity?.let { property("opacity", it.toString()) }
                            }
                            onClick { if (enabled) props.onClick() }
                        }) { content() }
                    }
                }
            },
            textField = { props, modifier, _ -> HtmlTextField(props, modifier) },
            checkbox = {
                props,
                modifier,
                _,
                ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.checkbox
                HtmlToggle(
                    props.checked,
                    props.onCheckedChange,
                    props.label,
                    props.enabled,
                    modifier,
                    false,
                    props.indeterminate,
                    props.showLabel,
                    props.description,
                    props.controlPlacement,
                    appearance.labelSpacing,
                    appearance.colors,
                    appearance.labelAppearance,
                    appearance.descriptionAppearance,
                    appearance.checkmark,
                    appearance.disabledCheckmark,
                    descriptionSpacing = appearance.descriptionSpacing,
                )
            },
            switch = {
                props,
                modifier,
                _,
                ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.switch
                HtmlToggle(
                    props.checked,
                    props.onCheckedChange,
                    props.label,
                    props.enabled,
                    modifier,
                    true,
                    false,
                    props.showLabel,
                    props.description,
                    props.controlPlacement,
                    appearance.labelSpacing,
                    labelAppearance = appearance.labelAppearance,
                    descriptionAppearance = appearance.descriptionAppearance,
                    descriptionSpacing = appearance.descriptionSpacing,
                    switchColors = appearance.colors,
                )
            },
            radioButton = {
                props,
                modifier,
                _,
                ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.radioButton
                val onClick = props.onClick
                val descriptionId = remember { "d-radio-description-${nextInputId++}" }
                val labelId = remember { "d-radio-label-${nextInputId++}" }
                val enabled = props.enabled && domposableFormEnabled()
                DomposableHtmlModifiers(modifier) {
                    TagElement<HTMLElement>(
                        if (props.showLabel) {
                            "label"
                        } else {
                            "span"
                        },
                        applyAttrs = {
                            classes("d-toggle")
                            selectionContainerClick()
                            style { property("gap", "${appearance.labelSpacing.value}px") }
                        },
                    ) {
                        @Composable fun control() {
                            Input(InputType.Radio, attrs = {
                                domposableAttributes(modifier)
                                checked(props.selected)
                                props.groupName?.let { attr("name", it) }
                                if (props.label.isNotEmpty()) attr("aria-labelledby", labelId)
                                props.description?.let { attr("aria-describedby", descriptionId) }
                                if (!enabled) disabled()
                                style {
                                    property(
                                        "--d-control-selected",
                                        appearance.colors.selected.cssOr("var(--d-primary)"),
                                    )
                                    property(
                                        "--d-control-unselected",
                                        appearance.colors.unselected.cssOr(
                                            "var(--d-on-surface-variant,var(--d-outline))",
                                        ),
                                    )
                                    property(
                                        "--d-control-disabled-selected",
                                        appearance.colors.disabledSelected.cssOr(
                                            "color-mix(in srgb,var(--d-on-surface) 38%,transparent)",
                                        ),
                                    )
                                    property(
                                        "--d-control-disabled-unselected",
                                        appearance.colors.disabledUnselected.cssOr(
                                            "color-mix(in srgb,var(--d-on-surface) 38%,transparent)",
                                        ),
                                    )
                                    if (appearance.colors !=
                                        io.github.samuolis.domposable.foundation
                                            .DomposableSelectionColors()
                                    ) {
                                        val selectedColor =
                                            if (enabled) {
                                                appearance.colors.selected
                                            } else {
                                                appearance.colors.disabledSelected
                                            }
                                        property("accent-color", selectedColor.cssOr("var(--d-primary)"))
                                    }
                                }
                                if (appearance.colors != DomposableSelectionColors()) attr("data-custom-colors", "true")
                                if (onClick == null) attr("tabindex", "-1")
                                onChange { if (enabled) onClick?.invoke() }
                                onClick {
                                    if (onClick == null) {
                                        it.preventDefault()
                                    } else if (enabled && props.selected) {
                                        onClick()
                                    }
                                }
                            })
                        }

                        @Composable fun label() {
                            Span(attrs = {
                                classes("d-toggle-copy")
                                style { property("gap", "${appearance.descriptionSpacing.value}px") }
                                if (props.showLabel || props.description != null) {
                                    classes("d-toggle-copy-visible")
                                } else {
                                    classes("d-visually-hidden")
                                }
                            }) {
                                Span(attrs = {
                                    id(labelId)
                                    if (!props.showLabel) classes("d-visually-hidden")
                                    style { appearance.labelAppearance?.let { applyCatalogTextAppearance(it) } }
                                }) { Text(props.label) }
                                props.description?.let {
                                    Span(attrs = {
                                        id(descriptionId)
                                        style {
                                            appearance.descriptionAppearance?.let {
                                                applyCatalogTextAppearance(
                                                    it,
                                                )
                                            }
                                        }
                                    }) { Text(it) }
                                }
                            }
                        }
                        if (props.controlPlacement == DomposableControlPlacement.Leading) {
                            control()
                            label()
                        } else {
                            label()
                            control()
                        }
                    }
                }
            },
            slider = { props, modifier, _ ->
                val colors = props.colors ?: LocalDomposableTokens.current.components.slider
                val enabled = props.enabled && domposableFormEnabled()
                val percent =
                    ((props.value - props.range.start) / (props.range.endInclusive - props.range.start) * 100f)
                        .coerceIn(
                            0f,
                            100f,
                        )
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        classes("d-slider-surface")
                        if (props.orientation == DomposableOrientation.Vertical) classes("d-slider-surface-vertical")
                        style {
                            sliderColors(colors, enabled)
                            property("--d-slider-progress", "$percent%")
                            sliderMarks(
                                props.steps,
                                percent,
                                vertical =
                                    props.orientation == DomposableOrientation.Vertical,
                                reversed =
                                    props.orientation == DomposableOrientation.Vertical && !props.topToBottom,
                            )
                            if (props.orientation == DomposableOrientation.Vertical) {
                                property("--d-slider-direction", if (props.topToBottom) "bottom" else "top")
                            }
                        }
                    }) {
                        Input(InputType.Range, attrs = {
                            domposableAttributes(modifier)
                            classes("d-slider")
                            if (props.orientation == DomposableOrientation.Vertical) {
                                classes("d-slider-vertical")
                                attr("aria-orientation", "vertical")
                            } else {
                                attr("aria-orientation", "horizontal")
                            }
                            value(props.value.toString())
                            if (props.label.isNotBlank()) attr("aria-label", props.label)
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
                            if (!enabled) disabled()
                            style {
                                if (props.orientation == DomposableOrientation.Vertical) {
                                    property("writing-mode", "vertical-lr")
                                    property("direction", if (props.topToBottom) "ltr" else "rtl")
                                }
                            }
                            onInput {
                                it.target.value
                                    .toFloatOrNull()
                                    ?.takeIf { enabled }
                                    ?.let(props.onValueChange)
                            }
                        })
                    }
                }
            },
            card = { props, modifier, content ->
                val tokens = LocalDomposableTokens.current
                val appearance = props.appearance ?: tokens.components.card
                val enabled = props.enabled && domposableFormEnabled()
                val cardContentId = remember { "d-card-content-${nextInputId++}" }
                val colors = tokens.colors.withMaterialColorDefaults()
                val container =
                    when (props.variant) {
                        io.github.samuolis.domposable.foundation.DomposableCardVariant.Filled -> {
                            requireNotNull(colors.surfaceContainerHighest)
                        }

                        io.github.samuolis.domposable.foundation.DomposableCardVariant.Elevated -> {
                            tokens.colors.surfaceContainerLow ?: tokens.colors.surface
                        }

                        io.github.samuolis.domposable.foundation.DomposableCardVariant.Outlined -> {
                            tokens.colors.surface
                        }
                    }
                val cardContainer = appearance.container ?: container
                val borderColor =
                    appearance.border
                        ?: if (props.variant ==
                            io.github.samuolis.domposable.foundation.DomposableCardVariant.Outlined
                        ) {
                            tokens.colors.outlineVariant ?: tokens.colors.outline
                        } else {
                            null
                        }
                val borderWidth = if (borderColor != null) appearance.borderWidth.value else 0f
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        classes("d-card", "d-card-${props.variant.name.lowercase()}")
                        style {
                            if (modifier.hasExplicitHtmlHeight()) property("height", "100%")
                            property("background-color", cardContainer.css())
                            property("--d-card-container", cardContainer.css())
                            property("--d-card-border-color", borderColor?.css() ?: "transparent")
                            property("--d-card-border-width", "${borderWidth}px")
                            props.contentPadding?.let { property("padding", it.cssPadding()) }
                            appearance.content?.let { property("color", it.css()) }
                            when (props.variant) {
                                io.github.samuolis.domposable.foundation.DomposableCardVariant.Filled -> {
                                    Unit
                                }

                                io.github.samuolis.domposable.foundation.DomposableCardVariant.Elevated -> {
                                    property(
                                        "box-shadow",
                                        "0 1px 3px rgb(0 0 0 / .24)",
                                    )
                                }

                                io.github.samuolis.domposable.foundation.DomposableCardVariant.Outlined -> {
                                    Unit
                                }
                            }
                            appearance.shape?.let { materialShape(it) }
                                ?: tokens.shapes.medium?.let { materialShape(it) }
                            appearance.shadowElevation?.let {
                                property(
                                    "box-shadow",
                                    if (it.value ==
                                        0f
                                    ) {
                                        "none"
                                    } else {
                                        "var(--d-card-shadow,0 ${it.value}px ${it.value * 2f}px rgb(0 0 0 / .12))"
                                    },
                                )
                            }
                            appearance.textAppearance?.let { applyCatalogTextAppearance(it) }
                        }
                        if (props.href != null && !enabled) {
                            attr("role", "link")
                            attr("aria-disabled", "true")
                            attr("tabindex", "-1")
                        }
                        if (props.href == null && props.actionLabel.isNullOrEmpty()) {
                            props.onClick?.let { click ->
                                attr("role", "button")
                                if (enabled) attr("tabindex", "0") else attr("aria-disabled", "true")
                                onClick { event ->
                                    if (enabled) {
                                        val target = event.target as? Element
                                        val interactive =
                                            target?.closest(
                                                "a[href],button,input,select,textarea,label,summary," +
                                                    "[contenteditable='true'],[role=button],[role=link]," +
                                                    "[role=checkbox],[role=radio],[role=switch]," +
                                                    "[role=menuitem],[role=tab]",
                                            ) as? Element
                                        if (interactive == null ||
                                            interactive == event.currentTarget as? HTMLElement
                                        ) {
                                            click()
                                        }
                                    }
                                }
                                onKeyDown {
                                    if ((it.target as? HTMLElement) == (it.currentTarget as? HTMLElement)) {
                                        when (it.key) {
                                            "Enter" -> {
                                                it.preventDefault()
                                                if (enabled) click()
                                            }

                                            " " -> {
                                                it.preventDefault()
                                            }
                                        }
                                    }
                                }
                                onKeyUp {
                                    if ((it.target as? HTMLElement) == (it.currentTarget as? HTMLElement) &&
                                        it.key == " "
                                    ) {
                                        it.preventDefault()
                                        if (enabled) click()
                                    }
                                }
                            }
                        }
                    }) {
                        if (props.href == null && !props.actionLabel.isNullOrEmpty()) {
                            props.onClick?.let { click ->
                                Button(attrs = {
                                    attr("type", "button")
                                    props.actionLabel?.let { label -> attr("aria-label", label) }
                                    if (!enabled) disabled()
                                    classes("d-card-stretched-action")
                                    onClick { if (enabled) click() }
                                }) { }
                            }
                        }
                        props.href?.let(::safeDomposableHref)?.takeIf { enabled }?.let { href ->
                            val action = props.onClick
                            A(href = href, attrs = {
                                classes("d-card-stretched-link")
                                props.actionLabel?.takeIf(String::isNotEmpty)?.let { attr("aria-label", it) }
                                    ?: attr("aria-labelledby", cardContentId)
                                onClick { event ->
                                    val modified =
                                        listOf(
                                            event.ctrlKey,
                                            event.metaKey,
                                            event.shiftKey,
                                            event.altKey,
                                        ).any { it }
                                    val unmodifiedPrimaryClick = !modified && event.button.toInt() == 0
                                    if (enabled && unmodifiedPrimaryClick && action != null) {
                                        event.preventDefault()
                                        action()
                                    }
                                }
                            }) { }
                        }
                        Div(attrs = {
                            id(cardContentId)
                            classes("d-card-content")
                            if (modifier.hasExplicitHtmlHeight()) style { property("height", "100%") }
                            if (props.href == null && (props.onClick == null || props.actionLabel.isNullOrEmpty())) {
                                style { property("pointer-events", "auto") }
                            }
                        }) { content() }
                    }
                }
            },
            surface = {
                props,
                modifier,
                content,
                ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.surface
                val hasBorder = appearance.border != null && appearance.borderWidth.value > 0f
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        classes("d-surface")
                        style {
                            if (hasBorder) property("position", "relative")
                            surfaceAppearance(if (hasBorder) appearance.copy(border = null) else appearance)
                            (props.color ?: appearance.container)?.let { property("background-color", it.css()) }
                        }
                    }) {
                        content()
                        if (hasBorder) HtmlSurfaceBorderOverlay(appearance)
                    }
                }
            },
            image = { props, modifier, _ -> HtmlImage(props, modifier) },
            icon = { props, modifier, _ ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.icon
                if (props.vector != null) {
                    HtmlDomposableVectorIcon(props, modifier, appearance)
                } else {
                    DomposableHtmlModifiers(modifier) {
                        Span(attrs = {
                            domposableAttributes(modifier)
                            if (props.description == null) {
                                attr("aria-hidden", "true")
                            } else {
                                attr("role", "img")
                                attr("aria-label", props.description.orEmpty())
                            }
                            style {
                                appearance.color?.let { property("color", it.css()) }
                                appearance.textAppearance?.let { applyCatalogTextAppearance(it) }
                            }
                        }) { Text(props.symbol) }
                    }
                }
            },
            divider = {
                props,
                modifier,
                _,
                ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.divider
                DomposableHtmlModifiers(modifier) {
                    Hr(attrs = {
                        domposableAttributes(modifier)
                        attr("aria-orientation", props.orientation.name.lowercase())
                        classes("d-divider")
                        style {
                            appearance.color?.let { property("border-color", it.css()) }
                            if (props.orientation == DomposableOrientation.Vertical) {
                                val thickness = appearance.thickness?.value ?: 1f
                                property("border-top", "0")
                                property(
                                    "border-inline-start",
                                    "${thickness}px solid ${appearance.color?.css() ?: "var(--d-outline)"}",
                                )
                                property("height", "100%")
                                property("width", "${thickness}px")
                            } else {
                                appearance.thickness?.let { property("border-top-width", "${it.value}px") }
                            }
                        }
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
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.badge
                DomposableHtmlModifiers(modifier) {
                    Span(attrs = {
                        domposableAttributes(modifier)
                        classes("d-badge")
                        if (props.text == null && props.content == null) classes("d-badge-dot")
                        props.label?.let {
                            attr("role", "img")
                            attr("aria-label", it)
                        }
                        if (props.text == null && props.content == null &&
                            props.label == null
                        ) {
                            attr("aria-hidden", "true")
                        }
                        style {
                            surfaceAppearance(appearance)
                            props.contentPadding?.let { property("padding", it.cssPadding()) }
                            props.minWidth?.let { property("min-width", "${it.value}px") }
                            props.minHeight?.let { property("min-height", "${it.value}px") }
                            if (props.contentPadding != null || props.minHeight != null) property("height", "auto")
                        }
                    }) {
                        props.content?.invoke() ?: props.text?.let { Text(it) }
                    }
                }
            },
            chip = {
                props,
                modifier,
                _,
                ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.chip
                val enabled = props.enabled && domposableFormEnabled()
                val labelAppearance =
                    if (props.selected) {
                        appearance.selectedTextAppearance ?: appearance.textAppearance
                    } else {
                        appearance.textAppearance
                    }
                DomposableHtmlModifiers(modifier) {
                    Button(attrs = {
                        domposableAttributes(modifier)
                        attr("type", "button")
                        attr("aria-label", props.label)
                        attr("data-selected", props.selected.toString())
                        if (props.variant == DomposableChipVariant.Filter ||
                            props.variant == DomposableChipVariant.Input
                        ) {
                            attr("aria-pressed", props.selected.toString())
                        }
                        classes("d-chip", "d-chip-${props.variant.name.lowercase()}")
                        if (props.variant == DomposableChipVariant.Input && appearance.contentPadding == null) {
                            attr("data-input-padding", "true")
                        }
                        if (props.elevated) classes("d-chip-elevated")
                        if (appearance.minimumInteractiveSize?.value != 0f) classes("d-item-hittarget")
                        if (!enabled) disabled()
                        style {
                            itemAppearance(appearance, props.selected, includeTextLineLimits = false)
                            if (props.variant == DomposableChipVariant.Input) {
                                val start = if (props.leadingIcon == null) 3 else 7
                                val end = if (props.trailingIcon == null) 3 else 7
                                property("--d-chip-padding-start", "${start}px")
                                property("--d-chip-padding-end", "${end}px")
                            }
                            if (appearance.minimumInteractiveSize?.value != 0f) property("border-width", "0")
                        }
                        onClick { if (enabled) props.onClick() }
                    }) {
                        Span(attrs = { classes("d-chip-surface") }) {
                            props.leadingIcon?.let { icon ->
                                Span(attrs = { classes("d-chip-icon") }) { icon() }
                            }
                            Span(attrs = {
                                classes("d-chip-label")
                                style {
                                    labelAppearance?.let {
                                        applyCatalogTextAppearance(it)
                                        if (it.minLines > 1) property("min-height", "${it.minLines}lh")
                                    }
                                }
                            }) { Text(props.label) }
                            props.trailingIcon?.let { icon ->
                                Span(attrs = { classes("d-chip-icon") }) { icon() }
                            }
                        }
                    }
                }
            },
            avatar = {
                props,
                modifier,
                _,
                ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.avatar
                val hasBorder = appearance.border != null && appearance.borderWidth.value > 0f
                DomposableHtmlModifiers(modifier) {
                    Span(attrs = {
                        domposableAttributes(modifier)
                        attr("role", "img")
                        attr("aria-label", props.description.orEmpty())
                        classes("d-avatar")
                        style {
                            if (hasBorder) property("position", "relative")
                            if (modifier.hasExplicitHtmlWidth()) property("width", "100%")
                            if (modifier.hasExplicitHtmlHeight()) property("height", "100%")
                            property("max-width", "100%")
                            property("max-height", "100%")
                            surfaceAppearance(if (hasBorder) appearance.copy(border = null) else appearance)
                        }
                    }) {
                        props.content?.invoke() ?: Text(props.initials)
                        if (hasBorder) HtmlSurfaceBorderOverlay(appearance)
                    }
                }
            },
            banner = {
                props,
                modifier,
                _,
                ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.banner
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        attr("role", if (props.isError) "alert" else "status")
                        classes("d-banner")
                        style {
                            if (props.isError) property("color", "var(--d-error)")
                            surfaceAppearance(appearance)
                        }
                    }) { Text(props.message) }
                }
            },
            progress = { props, modifier, _ ->
                HtmlProgress(props, modifier)
            },
            tabs = { props, modifier, _ -> HtmlTabs(props, modifier) },
            navigationBar = { props, modifier, _ ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.navigationBar
                val formEnabled = domposableFormEnabled()
                DomposableHtmlModifiers(modifier) {
                    Nav(attrs = {
                        domposableAttributes(modifier)
                        attr("aria-label", props.label)
                        classes("d-navigation")
                        if (props.orientation == DomposableOrientation.Vertical) classes("d-navigation-rail")
                        if (props.short) classes("d-navigation-short")
                        if (props.webLayout == DomposableWebNavigationLayout.Links) classes("d-navigation-links")
                        style { itemAppearance(appearance) }
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
                            val safeHref = item.href?.let(::safeDomposableHref)
                            if (safeHref != null && formEnabled && item.enabled) {
                                A(href = safeHref, attrs = {
                                    if (index == props.selectedIndex) attr("aria-current", "page")
                                    style { itemAppearance(appearance, index == props.selectedIndex) }
                                    onClick { event ->
                                        val modified =
                                            listOf(
                                                event.ctrlKey,
                                                event.metaKey,
                                                event.shiftKey,
                                                event.altKey,
                                            ).any { it }
                                        if (formEnabled && event.button.toInt() == 0 && !modified) {
                                            event.preventDefault()
                                            props.onSelect(index)
                                        }
                                    }
                                }) { navItemContent(item) }
                            } else {
                                Button(attrs = {
                                    attr("type", "button")
                                    style { itemAppearance(appearance, index == props.selectedIndex) }
                                    if (!formEnabled || !item.enabled) disabled()
                                    if (index ==
                                        props.selectedIndex
                                    ) {
                                        attr("aria-current", "page")
                                    }
                                    onClick { if (formEnabled && item.enabled) props.onSelect(index) }
                                }) { navItemContent(item) }
                            }
                        }
                    }
                }
            },
            menu = {
                props,
                modifier,
                content,
                ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.menu
                val externalAnchor =
                    modifier.elements
                        .filterIsInstance<DomposableHtmlMenuAnchor>()
                        .lastOrNull()
                        ?.resolve
                val placementHint =
                    modifier.elements
                        .filterIsInstance<DomposableHtmlMenuPlacementHint>()
                        .lastOrNull()
                        ?.resolve
                val menuModifier =
                    modifier.elements
                        .filterNot { it is DomposableHtmlMenuAnchor || it is DomposableHtmlMenuPlacementHint }
                        .fold(DomposableModifier as DomposableModifier) { result, element -> result.then(element) }
                var slotAnchor by remember { mutableStateOf<HTMLElement?>(null) }
                val dismissMenu by rememberUpdatedState(props.onDismiss)
                props.anchor?.let { anchorContent ->
                    TagElement<HTMLElement>("span", applyAttrs = {
                        classes("d-menu-anchor")
                        style { property("display", "inline-flex") }
                        ref { element ->
                            slotAnchor = element
                            onDispose { slotAnchor = null }
                        }
                    }) { anchorContent() }
                }
                if (props.expanded) {
                    val requestDismiss =
                        remember {
                            var requested = false
                            val request: () -> Unit = {
                                if (!requested) {
                                    requested = true
                                    dismissMenu()
                                }
                            }
                            request
                        }
                    val previousFocus = remember { document.activeElement as? HTMLElement }
                    val opener =
                        previousFocus?.takeUnless {
                            it == document.body || it == document.documentElement
                        }

                    fun isMenuAnchor(target: Node?): Boolean =
                        target != null &&
                            (opener?.contains(target) == true || slotAnchor?.contains(target) == true)
                    TagElement<HTMLElement>("div", applyAttrs = {
                        classes("d-menu-backdrop")
                        attr("popover", "manual")
                        attr("aria-label", "Menu")
                        attr("tabindex", "-1")
                        style {
                            property("position", "fixed")
                            property("inset", "0")
                            property("z-index", "1000")
                            property("margin", "0")
                            property("padding", "0")
                            property("border", "0")
                            property("width", "100vw")
                            property("height", "100dvh")
                            property("max-width", "none")
                            property("max-height", "none")
                            property("background", "transparent")
                            property("color", "inherit")
                            // The visible popup remains interactive; outside hits reach app controls.
                            property("pointer-events", "none")
                        }
                        onClick { event ->
                            val target = event.target as? Node
                            val popup = (event.currentTarget as HTMLElement).querySelector(".d-menu-position")
                            if (target == null || popup == null || !popup.contains(target)) requestDismiss()
                        }
                        onKeyDown { event ->
                            when (event.key) {
                                "Escape" -> {
                                    event.preventDefault()
                                    event.stopPropagation()
                                    requestDismiss()
                                }

                                "ArrowDown", "ArrowUp" -> {
                                    event.preventDefault()
                                    val items =
                                        (event.currentTarget as HTMLElement).querySelectorAll(
                                            "[role^=menuitem]:not(:disabled)",
                                        )
                                    if (items.length > 0) {
                                        val active = document.activeElement
                                        val current =
                                            (0 until items.length).firstOrNull { items.item(it) == active } ?: -1
                                        val delta = if (event.key == "ArrowDown") 1 else -1
                                        items
                                            .item(
                                                (current + delta + items.length) % items.length,
                                            )?.let { (it as? HTMLElement)?.focus() }
                                    }
                                }

                                "Home", "End" -> {
                                    event.preventDefault()
                                    val items =
                                        (event.currentTarget as HTMLElement).querySelectorAll(
                                            "[role^=menuitem]:not(:disabled)",
                                        )
                                    val index = if (event.key == "Home") 0 else items.length - 1
                                    if (index >= 0) (items.item(index) as? HTMLElement)?.focus()
                                }
                            }
                        }
                        ref { element ->
                            // The browser top layer keeps menus above application stacking contexts.
                            element.asDynamic().showPopover()
                            val externalAnchorElement = externalAnchor?.invoke(element)
                            val placement = placementHint?.invoke(element) ?: DomposableHtmlMenuPlacement.Auto
                            val resolveAnchor = { slotAnchor ?: externalAnchorElement }
                            positionHtmlMenu(
                                element,
                                resolveAnchor(),
                                props.anchorOffset,
                                alignToLogicalEnd = props.anchor == null,
                                placement = placement,
                            )
                            val reposition: (Event) -> Unit = {
                                positionHtmlMenu(
                                    element,
                                    resolveAnchor(),
                                    props.anchorOffset,
                                    alignToLogicalEnd = props.anchor == null,
                                    placement = placement,
                                )
                            }
                            window.addEventListener("resize", reposition)
                            window.addEventListener("scroll", reposition, true)
                            var restorePreviousFocus = true
                            val focusOut: (Event) -> Unit = { event ->
                                val next = event.asDynamic().relatedTarget as? Node
                                if (next == null || (!element.contains(next) && !isMenuAnchor(next))) {
                                    if (next != null && next != document.body && next != document.documentElement) {
                                        restorePreviousFocus = false
                                    }
                                    requestDismiss()
                                }
                            }
                            val outsideClick: (Event) -> Unit = { event ->
                                val target = event.target as? Node
                                val popup = element.querySelector(".d-menu-position")
                                val insidePopup = target?.let { popup?.contains(it) } == true
                                if (!insidePopup && !isMenuAnchor(target)) {
                                    val active = document.activeElement?.takeUnless { element.contains(it) }
                                    if (active != null && active != document.body &&
                                        active != document.documentElement
                                    ) {
                                        restorePreviousFocus = false
                                    }
                                    requestDismiss()
                                }
                            }
                            document.addEventListener("click", outsideClick)
                            element.addEventListener("focusout", focusOut)
                            val focusTarget =
                                element.querySelector("[role^=menuitem]:not(:disabled)") as? HTMLElement ?: element
                            focusTarget.focus()
                            onDispose {
                                window.removeEventListener("resize", reposition)
                                window.removeEventListener("scroll", reposition, true)
                                document.removeEventListener("click", outsideClick)
                                element.removeEventListener("focusout", focusOut)
                                if (element.matches(":popover-open")) element.asDynamic().hidePopover()
                                if (restorePreviousFocus) previousFocus?.takeIf { it.isConnected }?.focus()
                            }
                        }
                    }) {
                        Div(attrs = {
                            classes("d-menu-position")
                            style {
                                property("position", "fixed")
                                property("display", "inline-flex")
                                property("width", "max-content")
                                property("max-width", "calc(100vw - ${HTML_MENU_VIEWPORT_MARGIN * 2}px)")
                                property("pointer-events", "auto")
                            }
                        }) {
                            DomposableHtmlModifiers(menuModifier) {
                                Div(attrs = {
                                    attr("role", "menu")
                                    attr("aria-label", "Menu")
                                    domposableAttributes(modifier)
                                    classes("d-menu-popup")
                                    style {
                                        property("gap", "${props.contentSpacing.value}px")
                                        surfaceAppearance(appearance)
                                    }
                                }) {
                                    props.items.forEach { item ->
                                        HtmlDropdownMenuItem(
                                            item.copy(onClick = {
                                                requestDismiss()
                                                item.onClick()
                                            }),
                                            DomposableModifier,
                                        )
                                    }
                                    content()
                                }
                            }
                        }
                    }
                }
            },
            dialog = {
                props,
                modifier,
                content,
                ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.dialog
                HtmlModal(
                    props.title,
                    props.onDismiss,
                    modifier,
                    sheet = false,
                    appearance = appearance,
                    variant = props.variant,
                    headerContent = props.headerContent?.let { header -> { header(props.onDismiss) } },
                    layout = props.layout,
                    dismissOnBackPress = props.dismissOnBackPress,
                    dismissOnClickOutside = props.dismissOnClickOutside,
                ) {
                    content()
                    props.actions()
                }
            },
            bottomSheet = {
                props,
                modifier,
                content,
                ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.bottomSheet
                if (props.modal) {
                    HtmlModal(
                        props.title,
                        props.onDismiss,
                        modifier,
                        sheet = true,
                        appearance = appearance,
                        headerContent = props.headerContent?.let { header -> { header(props.onDismiss) } },
                        layout = props.layout,
                        skipPartiallyExpanded = props.skipPartiallyExpanded,
                        content = content,
                    )
                } else {
                    val titleId = remember { "d-nonmodal-sheet-title-${nextInputId++}" }
                    DomposableHtmlModifiers(modifier) {
                        Section(attrs = {
                            domposableAttributes(modifier)
                            if (props.headerContent == null) {
                                attr("aria-labelledby", titleId)
                            } else {
                                attr("aria-label", props.title)
                            }
                            classes("d-sheet", "d-bottom-sheet-nonmodal")
                            style {
                                surfaceAppearance(appearance)
                                modalLayout(props.layout)
                            }
                        }) {
                            if (props.headerContent == null) {
                                H2(attrs = { attr("id", titleId) }) { Text(props.title) }
                            } else {
                                Div(attrs = { classes("d-dialog-header") }) {
                                    props.headerContent?.invoke(props.onDismiss)
                                }
                            }
                            content()
                            if (props.headerContent == null) {
                                HtmlButton(
                                    DomposableButtonProps(props.onDismiss, style = DomposableButtonStyle.Text),
                                    DomposableModifier.contentDescription("Close ${props.title}"),
                                    { Text("Close") },
                                    respectFormEnabled = false,
                                )
                            }
                        }
                    }
                }
            },
            topAppBar = { props, modifier, _ -> HtmlTopAppBar(props, modifier) },
            floatingActionButton = { props, modifier, content -> HtmlFloatingActionButton(props, modifier, content) },
            snackbar = { props, modifier, _ -> HtmlSnackbar(props, modifier) },
            snackbarHost = { props, modifier, _ -> HtmlSnackbarHost(props, modifier) },
            badgedBox = { props, modifier, content -> HtmlBadgedBox(props, modifier, content) },
            scrim = { props, modifier, _ -> HtmlScrim(props, modifier) },
            label = { props, modifier, content -> HtmlLabel(props, modifier, content) },
            menuGroup = { props, modifier, content -> HtmlMenuGroup(props, modifier, content) },
            scrollField = { props, modifier, _ -> HtmlScrollField(props, modifier) },
            dropdownMenuItem = { props, modifier, _ -> HtmlDropdownMenuItem(props, modifier) },
            select = { props, modifier, _ -> HtmlSelect(props, modifier) },
            segmentedButtons = { props, modifier, _ -> HtmlSegmentedButtons(props, modifier) },
            rangeSlider = { props, modifier, _ -> HtmlRangeSlider(props, modifier) },
            tooltip = { props, modifier, content -> HtmlTooltip(props, modifier, content) },
            toggleButton = {
                props,
                modifier,
                content,
                ->
                HtmlToggleButton(props, modifier, content)
            },
            buttonGroup = {
                props,
                modifier,
                _,
                ->
                HtmlButtonGroup(props, modifier)
            },
            appBar = { props, modifier, _ -> HtmlAppBar(props, modifier) },
            multiChoiceSegmentedButtons = {
                props,
                modifier,
                _,
                ->
                HtmlMultiChoiceSegmentedButtons(props, modifier)
            },
            splitButton = {
                props,
                modifier,
                _,
                ->
                HtmlSplitButton(props, modifier)
            },
            loadingIndicator = {
                props,
                modifier,
                _,
                ->
                DomposableHtmlModifiers(modifier) { HtmlLoadingIndicator(props, modifier) }
            },
            floatingToolbar = {
                props,
                modifier,
                content,
                ->
                HtmlFloatingToolbar(props, modifier, content)
            },
            floatingActionButtonMenu = {
                props,
                modifier,
                _,
                ->
                HtmlFloatingActionButtonMenu(props, modifier)
            },
            dragHandle = {
                props,
                modifier,
                _,
                ->
                HtmlDragHandle(props, modifier)
            },
            listItem = { props, modifier, _ -> HtmlListItem(props, modifier) },
            bottomAppBar = {
                props,
                modifier,
                content,
                ->
                HtmlBottomAppBar(props, modifier, content)
            },
            navigationDrawer = {
                props,
                modifier,
                content,
                ->
                HtmlNavigationDrawer(props, modifier, content)
            },
            navigationDrawerItem = {
                props,
                modifier,
                _,
                ->
                HtmlNavigationDrawerItem(props, modifier)
            },
            wideNavigationRail = {
                props,
                modifier,
                content,
                ->
                HtmlWideNavigationRail(props, modifier, content)
            },
            wideNavigationRailItem = {
                props,
                modifier,
                _,
                ->
                HtmlWideNavigationRailItem(props, modifier)
            },
            bottomSheetScaffold = {
                props,
                modifier,
                content,
                ->
                HtmlBottomSheetScaffold(props, modifier, content)
            },
            searchBar = {
                props,
                modifier,
                content,
                ->
                HtmlSearchBar(props, modifier, content)
            },
            autocomplete = {
                props,
                modifier,
                _,
                ->
                HtmlAutocomplete(props, modifier)
            },
            datePicker = {
                props,
                modifier,
                _,
                ->
                HtmlDatePicker(props, modifier)
            },
            dateRangePicker = {
                props,
                modifier,
                _,
                ->
                HtmlDateRangePicker(props, modifier)
            },
            timePicker = {
                props,
                modifier,
                _,
                ->
                HtmlTimePicker(props, modifier)
            },
            carousel = { props, modifier, _ -> HtmlCarousel(props, modifier) },
            pullToRefresh = {
                props,
                modifier,
                content,
                ->
                HtmlPullToRefresh(props, modifier, content)
            },
            swipeToDismiss = {
                props,
                modifier,
                content,
                ->
                HtmlSwipeToDismiss(props, modifier, content)
            },
            scaffold = { props, modifier, content ->
                val appearance = props.appearance ?: LocalDomposableTokens.current.components.scaffold
                val scaffoldDepth = LocalHtmlScaffoldDepth.current
                val nested = scaffoldDepth > 0
                val constrained =
                    nested ||
                        modifier.elements.any {
                            it is DomposableModifierOperation.Height || it == DomposableModifierOperation.FillHeight
                        }
                CompositionLocalProvider(LocalHtmlScaffoldDepth provides scaffoldDepth + 1) {
                    DomposableHtmlModifiers(modifier) {
                        Div(attrs = {
                            domposableAttributes(modifier)
                            classes("d-scaffold")
                            if (nested) classes("d-scaffold-nested")
                            if (constrained) classes("d-scaffold-constrained")
                            style { surfaceAppearance(appearance) }
                        }) {
                            TagElement<HTMLElement>(if (nested) "div" else "header", applyAttrs = {}) {
                                if (!nested) {
                                    A(href = "#main-content", attrs = {
                                        classes("d-skip-link")
                                        onClick {
                                            it.preventDefault()
                                            (document.getElementById("main-content") as? HTMLElement)?.focus()
                                        }
                                    }) { Text("Skip to content") }
                                }
                                props.topBar()
                            }
                            Div(attrs = { classes("d-scaffold-middle") }) {
                                props.navigation()
                                TagElement<HTMLElement>(if (nested) "div" else "main", applyAttrs = {
                                    if (!nested) {
                                        id("main-content")
                                        attr("tabindex", "-1")
                                    }
                                    classes("d-scaffold-body")
                                }) {
                                    content()
                                    Div(attrs = {
                                        classes(
                                            "d-floating",
                                            "d-floating-${props.floatingActionPosition.name.lowercase()}",
                                        )
                                        style {
                                            when (props.floatingActionPosition) {
                                                DomposableScaffoldFabPosition.Start -> {
                                                    property("inset-inline-start", "16px")
                                                    property("inset-inline-end", "auto")
                                                }

                                                DomposableScaffoldFabPosition.Center -> {
                                                    property("left", "50%")
                                                    property("right", "auto")
                                                    property("transform", "translateX(-50%)")
                                                }

                                                DomposableScaffoldFabPosition.End -> {
                                                    property("inset-inline-end", "16px")
                                                    property("inset-inline-start", "auto")
                                                }
                                            }
                                        }
                                    }) { props.floatingAction() }
                                    Div(attrs = { classes("d-snackbar") }) { props.snackbar() }
                                }
                                Aside { props.contextual() }
                            }
                            TagElement<HTMLElement>(if (nested) "div" else "footer", applyAttrs = {}) {
                                props.bottomBar()
                            }
                            props.overlays()
                        }
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
    var fillsHeight = false
    modifier.elements.forEach { element ->
        val operation = element as? DomposableModifierOperation ?: return@forEach
        when (operation) {
            is DomposableModifierOperation.Height -> {
                imageHeight = operation.length.value
                fillsHeight = false
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
                fillsHeight = true
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
                if (fillsHeight) property("height", "100%")
                if (modifier.elements.any {
                        it is DomposableModifierOperation.Width ||
                            it is DomposableModifierOperation.FillWidthFraction ||
                            it == DomposableModifierOperation.FillWidth
                    }
                ) {
                    property("width", "100%")
                }
            }
        })
    }
}

private val LocalHtmlScaffoldDepth = staticCompositionLocalOf { 0 }

private var nextInputId = 0

internal fun nextMaterialInputId(): String = "d-input-${nextInputId++}"

private fun AttrsScope<HTMLElement>.selectionContainerClick() {
    onClick { event ->
        event.stopPropagation()
        val host = event.currentTarget as? HTMLElement
        if (host?.tagName != "LABEL" && event.target != host?.querySelector("input")) {
            event.preventDefault()
            (host?.querySelector("input") as? HTMLElement)?.click()
        }
    }
}

@Composable
private fun HtmlToggle(
    checkedValue: Boolean,
    onChange: ((Boolean) -> Unit)?,
    label: String,
    enabled: Boolean,
    modifier: DomposableModifier,
    switch: Boolean,
    indeterminate: Boolean,
    showLabel: Boolean,
    description: String?,
    controlPlacement: DomposableControlPlacement,
    labelSpacing: DomposableLength,
    colors: DomposableSelectionColors = DomposableSelectionColors(),
    labelAppearance: DomposableTextAppearance? = null,
    descriptionAppearance: DomposableTextAppearance? = null,
    checkmark: DomposableColor? = null,
    disabledCheckmark: DomposableColor? = null,
    descriptionSpacing: DomposableLength = DomposableLength(0f),
    switchColors: DomposableSwitchColors? = null,
) {
    val effectiveEnabled = enabled && domposableFormEnabled()
    val inputRef = remember { arrayOfNulls<HTMLElement>(1) }
    SideEffect { inputRef[0]?.asDynamic()?.indeterminate = indeterminate }
    val descriptionId = remember { "d-toggle-description-${nextInputId++}" }
    val labelId = remember { "d-toggle-label-${nextInputId++}" }
    val customColors =
        if (switch) {
            switchColors != null && switchColors != DomposableSwitchColors()
        } else {
            colors !=
                DomposableSelectionColors()
        }
    DomposableHtmlModifiers(modifier) {
        TagElement<HTMLElement>(if (showLabel) "label" else "span", applyAttrs = {
            classes("d-toggle")
            selectionContainerClick()
            style { property("gap", "${labelSpacing.value}px") }
        }) {
            @Composable fun control() {
                Input(InputType.Checkbox, attrs = {
                    domposableAttributes(modifier)
                    checked(checkedValue)
                    ref { input ->
                        inputRef[0] = input
                        input.asDynamic().indeterminate = indeterminate
                        onDispose { inputRef[0] = null }
                    }
                    if (switch) attr("role", "switch")
                    if (!effectiveEnabled) disabled()
                    if (onChange == null) {
                        attr("tabindex", "-1")
                        attr("aria-readonly", "true")
                        onClick { it.preventDefault() }
                    }
                    if (label.isNotEmpty()) attr("aria-labelledby", labelId)
                    description?.let { attr("aria-describedby", descriptionId) }
                    if (customColors) attr("data-custom-colors", "true")
                    style {
                        if (switch) {
                            switchColors?.let {
                                property("--d-switch-checked-thumb", it.checkedThumb.cssOr("var(--d-on-primary)"))
                                property("--d-switch-checked-track", it.checkedTrack.cssOr("var(--d-primary)"))
                                property("--d-switch-checked-border", it.checkedBorder.cssOr("transparent"))
                                property("--d-switch-checked-icon", it.checkedIcon.cssOr("var(--d-on-primary)"))
                                property("--d-switch-unchecked-thumb", it.uncheckedThumb.cssOr("var(--d-outline)"))
                                property(
                                    "--d-switch-unchecked-track",
                                    it.uncheckedTrack.cssOr("var(--d-surface-container-highest,var(--d-variant))"),
                                )
                                property("--d-switch-unchecked-border", it.uncheckedBorder.cssOr("var(--d-outline)"))
                                property("--d-switch-unchecked-icon", it.uncheckedIcon.cssOr("var(--d-on-surface)"))
                                property(
                                    "--d-switch-disabled-checked-thumb",
                                    it.disabledCheckedThumb.cssOr("var(--d-surface)"),
                                )
                                property(
                                    "--d-switch-disabled-checked-track",
                                    it.disabledCheckedTrack.cssOr(
                                        "color-mix(in srgb,var(--d-on-surface) 12%,var(--d-surface))",
                                    ),
                                )
                                property(
                                    "--d-switch-disabled-checked-border",
                                    it.disabledCheckedBorder.cssOr("transparent"),
                                )
                                property(
                                    "--d-switch-disabled-checked-icon",
                                    it.disabledCheckedIcon.cssOr(
                                        "color-mix(in srgb,var(--d-on-surface) 38%,var(--d-surface))",
                                    ),
                                )
                                property(
                                    "--d-switch-disabled-unchecked-thumb",
                                    it.disabledUncheckedThumb.cssOr(
                                        "color-mix(in srgb,var(--d-on-surface) 38%,var(--d-surface))",
                                    ),
                                )
                                property(
                                    "--d-switch-disabled-unchecked-track",
                                    it.disabledUncheckedTrack.cssOr(
                                        "color-mix(in srgb," +
                                            "var(--d-surface-container-highest,var(--d-variant)) 12%,var(--d-surface))",
                                    ),
                                )
                                property(
                                    "--d-switch-disabled-unchecked-border",
                                    it.disabledUncheckedBorder.cssOr(
                                        "color-mix(in srgb,var(--d-on-surface) 12%,var(--d-surface))",
                                    ),
                                )
                                property(
                                    "--d-switch-disabled-unchecked-icon",
                                    it.disabledUncheckedIcon.cssOr(
                                        "color-mix(in srgb," +
                                            "var(--d-surface-container-highest,var(--d-variant)) 38%,var(--d-surface))",
                                    ),
                                )
                            }
                        } else {
                            property("--d-control-selected", colors.selected.cssOr("var(--d-primary)"))
                            property(
                                "--d-control-unselected",
                                colors.unselected.cssOr("var(--d-on-surface-variant,var(--d-outline))"),
                            )
                            property(
                                "--d-control-disabled-selected",
                                colors.disabledSelected.cssOr("color-mix(in srgb,var(--d-on-surface) 38%,transparent)"),
                            )
                            property(
                                "--d-control-disabled-unselected",
                                colors.disabledUnselected.cssOr(
                                    "color-mix(in srgb,var(--d-on-surface) 38%,transparent)",
                                ),
                            )
                            property("--d-control-checkmark", checkmark.cssOr("var(--d-on-primary)"))
                            property("--d-control-disabled-checkmark", disabledCheckmark.cssOr("var(--d-on-primary)"))
                            property(
                                "accent-color",
                                (if (effectiveEnabled) colors.selected else colors.disabledSelected).cssOr(
                                    "var(--d-primary)",
                                ),
                            )
                        }
                    }
                    onChange {
                        // Restore before the callback so synchronous recomposition can commit a new mixed state.
                        it.target.asDynamic().indeterminate = indeterminate
                        if (effectiveEnabled) onChange?.invoke(indeterminate || it.value)
                    }
                })
            }

            @Composable fun labelText() {
                Span(attrs = {
                    classes("d-toggle-copy")
                    style { property("gap", "${descriptionSpacing.value}px") }
                    if (showLabel || description != null) {
                        classes("d-toggle-copy-visible")
                    } else {
                        classes("d-visually-hidden")
                    }
                }) {
                    Span(attrs = {
                        id(labelId)
                        if (!showLabel) classes("d-visually-hidden")
                        style {
                            applyCatalogTextAppearance(
                                labelAppearance?.copy(role = labelAppearance.role ?: DomposableTypographyRole.BodyLarge)
                                    ?: DomposableTextAppearance(role = DomposableTypographyRole.BodyLarge),
                            )
                        }
                    }) { Text(label) }
                    description?.let {
                        Span(attrs = {
                            id(descriptionId)
                            classes("d-toggle-description")
                            style {
                                property("color", "var(--d-on-surface-variant)")
                                applyCatalogTextAppearance(
                                    descriptionAppearance
                                        ?: DomposableTextAppearance(role = DomposableTypographyRole.BodySmall),
                                )
                            }
                        }) { Text(it) }
                    }
                }
            }
            if (controlPlacement == DomposableControlPlacement.Leading) {
                control()
                labelText()
            } else {
                labelText()
                control()
            }
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
private fun HtmlTheme(
    tokens: DomposableTokens,
    content: @Composable () -> Unit,
) {
    InstallDomposableMaterialStyles()
    Div(attrs = {
        classes(
            "d-theme",
            if (tokens.designLanguage ==
                DomposableDesignLanguage.Expressive
            ) {
                "d-theme-expressive"
            } else {
                "d-theme-standard"
            },
        )
        domposableTokenVariables(tokens)
    }) { content() }
}

@Composable
internal fun InstallDomposableMaterialStyles() {
    DisposableEffect(Unit) {
        val style = document.createElement("style")
        style.textContent = MATERIAL_CSS + MATERIAL_DEFAULTS_CSS +
            HTML_RICH_CONTENT_CSS + HTML_CONTROL_SLOT_CSS + DOMPOSABLE_HTML_GEOMETRY_CSS +
            htmlAdaptiveMediaStylesCss() + DOMPOSABLE_HTML_CHROME_SLOT_CSS + HTML_CONTENT_SLOT_CSS +
            DOMPOSABLE_HTML_SHELL_SLOT_CSS
        document.head!!.appendChild(style)
        onDispose { style.remove() }
    }
}

private const val MATERIAL_CSS = """
/* A stretched flex item needs auto height; its inner fill wrappers then resolve against that height. */
.d-row-stretch>[data-domposable-fill-height-ahead],.d-row-stretch>[data-domposable-modifier=FillHeight],.d-row-stretch>[data-domposable-layout-item],.d-row-stretch>[data-domposable-visibility]>[data-domposable-fill-height-ahead],.d-row-stretch>[data-domposable-visibility]>[data-domposable-modifier=FillHeight],.d-row-stretch>[data-domposable-visibility]>[data-domposable-layout-item]{height:auto!important}
.d-button,.d-chip,.d-icon-button,.d-field input,.d-field textarea,.d-field select{font:inherit}
.d-box>*{grid-area:1/1;min-width:0;min-height:0}
.d-theme *{box-sizing:border-box}.d-link{color:var(--d-primary);padding:8px;display:inline-flex;align-items:center;min-height:44px}.d-theme{color:var(--d-on-surface);background:var(--d-surface);font:var(--d-body)/var(--d-line-height) var(--d-font);min-height:100%;box-sizing:border-box}
.d-theme button,.d-theme input,.d-theme textarea{font:inherit;accent-color:var(--d-primary)}
.d-theme button{cursor:pointer}.d-theme button:disabled{cursor:default;opacity:.5}
.d-theme :focus-visible{outline:3px solid var(--d-primary);outline-offset:3px}
.d-skip-link{position:absolute;left:-999px}.d-skip-link:focus{left:12px;top:12px;z-index:10}
.d-theme h1,.d-theme h2,.d-theme h3,.d-theme h4,.d-theme h5,.d-theme h6{margin:0}.d-title{font-size:var(--d-title)}.d-label{font-size:var(--d-label)}:where(.d-text-inherit){font:inherit}
.d-button,.d-chip,.d-icon-button{border:1px solid transparent;padding:10px 20px;border-radius:9999px;min-height:44px;background:var(--d-primary);color:var(--d-on-primary);transition:background var(--d-motion)}
.d-button{cursor:pointer;transition:box-shadow 120ms ease,filter 120ms ease,transform 80ms ease;position:relative;display:inline-flex;align-items:center;justify-content:center}.d-button-single-line .d-button-copy{white-space:nowrap}.d-button-copy{display:inline-flex;flex:1;min-width:0;align-items:center;justify-content:inherit}.d-button:not(:disabled):hover{transform:scale(var(--d-button-hovered-scale,1))}.d-button:not(:disabled):active{transform:scale(var(--d-button-pressed-scale,1))}.d-button-spinner{position:absolute;width:var(--d-button-indicator-size,20px);height:var(--d-button-indicator-size,20px);border:var(--d-button-indicator-stroke,2px) solid var(--d-button-indicator-track,currentColor);border-top-color:currentColor;border-right-color:var(--d-button-indicator-track,transparent);border-radius:50%;animation:d-button-spin .8s linear infinite}@keyframes d-button-spin{to{transform:rotate(360deg)}}
@media(hover:hover){.d-button:not(:disabled):hover{filter:brightness(.95)}.d-button-outlined:not(:disabled):hover,.d-button-text:not(:disabled):hover{--d-button-feedback:inset 0 0 0 999px color-mix(in srgb,currentColor 8%,transparent);box-shadow:var(--d-button-feedback)}}
.d-button:not(:disabled):active{filter:brightness(.9)}.d-button:not(:disabled):focus-visible{outline:none;box-shadow:var(--d-button-feedback,0 0 0 0 transparent),0 0 0 3px var(--d-primary)}
.d-button[data-d-button-interaction=true]:not(:disabled):hover,.d-button[data-d-button-interaction=true]:not(:disabled):active,.d-icon-button[data-d-button-interaction=true]:not(:disabled):hover,.d-icon-button[data-d-button-interaction=true]:not(:disabled):active{filter:none}
.d-button[data-d-button-interaction=true]:not(:disabled):hover,.d-icon-button[data-d-button-interaction=true]:not(:disabled):hover{box-shadow:var(--d-button-hover-feedback,none)}
.d-button[data-d-button-interaction=true]:not(:disabled):active,.d-icon-button[data-d-button-interaction=true]:not(:disabled):active{box-shadow:var(--d-button-pressed-feedback,none)}
.d-button[data-d-button-interaction=true][style*="--d-button-hovered-container"]:not(:disabled):hover,.d-icon-button[data-d-button-interaction=true][style*="--d-button-hovered-container"]:not(:disabled):hover{background:var(--d-button-hovered-container)!important}
.d-button[data-d-button-interaction=true][style*="--d-button-hovered-content"]:not(:disabled):hover,.d-icon-button[data-d-button-interaction=true][style*="--d-button-hovered-content"]:not(:disabled):hover{color:var(--d-button-hovered-content)!important}
.d-button[data-d-button-interaction=true][style*="--d-button-pressed-container"]:not(:disabled):active,.d-icon-button[data-d-button-interaction=true][style*="--d-button-pressed-container"]:not(:disabled):active{background:var(--d-button-pressed-container)!important}
.d-button[data-d-button-interaction=true][style*="--d-button-pressed-content"]:not(:disabled):active,.d-icon-button[data-d-button-interaction=true][style*="--d-button-pressed-content"]:not(:disabled):active{color:var(--d-button-pressed-content)!important}
.d-button[data-d-button-interaction=true]:not(:disabled):focus-visible,.d-icon-button[data-d-button-interaction=true]:not(:disabled):focus-visible{outline:var(--d-button-focus-ring-width,3px) solid var(--d-button-focus-ring-color,var(--d-primary));outline-offset:var(--d-button-focus-ring-offset,3px);box-shadow:var(--d-button-focus-feedback,none)}
.d-button:disabled{cursor:default}
.d-button-outlined{background:transparent;color:var(--d-primary);border-color:var(--d-outline)}.d-button-text{background:transparent;color:var(--d-primary)}.d-button-elevated{background:var(--d-surface-container-low,var(--d-surface));color:var(--d-primary);box-shadow:0 1px 3px rgb(0 0 0 / .24)}.d-button-filledtonal{background:var(--d-secondary-container,var(--d-variant));color:var(--d-on-secondary-container,var(--d-on-surface))}
.d-icon-button{display:inline-flex;align-items:center;justify-content:center;border-radius:50%;padding:10px}.d-chip{min-height:var(--d-item-min-height,32px);padding:var(--d-item-padding,6px 12px);border-radius:var(--d-item-radius,var(--d-shape-small,8px));background:var(--d-item-container,transparent);color:var(--d-item-content,var(--d-on-surface));border-color:var(--d-item-border,var(--d-outline))}.d-chip[data-selected=true]{border-color:var(--d-primary)}
.d-card{position:relative;min-width:0;padding:var(--d-spacing);border-radius:var(--d-radius);background:var(--d-variant);box-shadow:0 var(--d-elevation) 8px #0001}.d-card::after{content:"";position:absolute;z-index:3;inset:0;box-sizing:border-box;pointer-events:none;border:var(--d-card-border-width,0px) solid var(--d-card-border-color,transparent);border-radius:inherit}:root .d-card .d-field{--d-surface:var(--d-card-container,var(--d-variant))}
.d-data-table-container{min-width:0;max-width:100%;overflow-x:auto;container-type:inline-size}.d-data-table{width:100%;border-collapse:collapse;table-layout:fixed}.d-data-table th,.d-data-table td{border-bottom:1px solid var(--d-outline);text-align:start}.d-data-table tbody tr:last-child>th,.d-data-table tbody tr:last-child>td{border-bottom:0}.d-data-table-row[tabindex="0"]{cursor:pointer}.d-data-table-row[data-d-table-interaction=true]:hover{background:var(--d-data-hovered-container,inherit)!important;color:var(--d-data-hovered-content,inherit)!important}.d-data-table-row[data-d-table-interaction=true]:hover>td,.d-data-table-row[data-d-table-interaction=true]:hover>th[scope=row]{background:var(--d-data-hovered-container,inherit)!important;color:var(--d-data-hovered-content,inherit)!important;box-shadow:var(--d-data-hover-feedback,none)}.d-data-table-row[data-d-table-interaction=true]:active{background:var(--d-data-pressed-container,inherit)!important;color:var(--d-data-pressed-content,inherit)!important}.d-data-table-row[data-d-table-interaction=true]:active>td,.d-data-table-row[data-d-table-interaction=true]:active>th[scope=row]{background:var(--d-data-pressed-container,inherit)!important;color:var(--d-data-pressed-content,inherit)!important;box-shadow:var(--d-data-pressed-feedback,none)}.d-data-table-row[data-d-table-interaction=true]:focus-visible{outline:var(--d-data-focus-ring-width,3px) solid var(--d-data-focus-ring-color,var(--d-primary))!important;outline-offset:var(--d-data-focus-ring-offset,3px)}.d-data-table-row[data-d-table-interaction=true]:focus-visible>td,.d-data-table-row[data-d-table-interaction=true]:focus-visible>th[scope=row]{box-shadow:var(--d-data-focus-feedback,none)}
.d-data-table-container.d-data-table-compact-active table,.d-data-table-container.d-data-table-compact-active thead,.d-data-table-container.d-data-table-compact-active tbody,.d-data-table-container.d-data-table-compact-active tr,.d-data-table-container.d-data-table-compact-active td,.d-data-table-container.d-data-table-compact-active th[scope=row]{display:block;width:100%}.d-data-table-container.d-data-table-compact-active thead{position:absolute;width:1px;height:1px;overflow:hidden;clip-path:inset(50%);white-space:nowrap}.d-data-table-container.d-data-table-compact-active .d-data-table-row{margin-block:8px;border:var(--d-data-compact-border-width,1px) solid var(--d-data-compact-border,var(--d-outline));border-radius:var(--d-data-compact-radius,12px);overflow:visible;background:var(--d-data-compact-container,transparent);color:var(--d-data-compact-content,inherit);box-shadow:var(--d-data-compact-shadow,none)}.d-data-table-container.d-data-table-compact-active .d-data-table-row>td,.d-data-table-container.d-data-table-compact-active .d-data-table-row>th[scope=row]{display:grid;grid-template-columns:minmax(var(--d-data-compact-label-width,6rem),1fr) 2fr;gap:8px;padding:8px;border-bottom:0!important}.d-data-table-container.d-data-table-compact-active .d-data-table-row>td::before,.d-data-table-container.d-data-table-compact-active .d-data-table-row>th[scope=row]::before{content:attr(data-column-label);color:var(--d-on-surface-variant)}
.d-data-table-container.d-data-table-spaced:not(.d-data-table-compact-active) .d-data-table{display:grid;grid-template-columns:minmax(0,1fr);row-gap:var(--d-data-row-spacing,0px);border-collapse:separate}.d-data-table-container.d-data-table-spaced:not(.d-data-table-compact-active) .d-data-table>thead,.d-data-table-container.d-data-table-spaced:not(.d-data-table-compact-active) .d-data-table>tbody{display:contents}.d-data-table-container.d-data-table-spaced:not(.d-data-table-compact-active) .d-data-table tr{display:grid;grid-template-columns:repeat(var(--d-data-column-count),minmax(0,1fr));column-gap:var(--d-data-column-spacing,0px);min-width:0;border-bottom:1px solid var(--d-outline)}.d-data-table-container.d-data-table-spaced:not(.d-data-table-compact-active) .d-data-table th,.d-data-table-container.d-data-table-spaced:not(.d-data-table-compact-active) .d-data-table td{box-sizing:border-box;min-width:0;border-bottom:0!important}.d-data-table-container.d-data-table-spaced:not(.d-data-table-compact-active) .d-data-table tbody>tr:last-child{border-bottom:0}.d-data-table-container.d-data-table-spaced.d-data-table-compact-active table,.d-data-table-container.d-data-table-spaced.d-data-table-compact-active thead,.d-data-table-container.d-data-table-spaced.d-data-table-compact-active tbody,.d-data-table-container.d-data-table-spaced.d-data-table-compact-active tr{display:block;width:100%}.d-data-table-container.d-data-table-spaced.d-data-table-compact-active .d-data-table-row>td,.d-data-table-container.d-data-table-spaced.d-data-table-compact-active .d-data-table-row>th[scope=row]{gap:var(--d-data-column-spacing,8px)}.d-data-table-container.d-data-table-row-spacing.d-data-table-compact-active tbody{display:flex;flex-direction:column;gap:var(--d-data-row-spacing)}.d-data-table-container.d-data-table-row-spacing.d-data-table-compact-active .d-data-table-row{display:flex;flex-direction:column;gap:var(--d-data-row-spacing);margin-block:0}
@media(forced-colors:active){.d-data-table-row[aria-selected=true]{outline:2px solid Highlight!important}.d-data-table-row[data-d-table-interaction=true]:focus-visible{outline:2px solid Highlight!important;outline-offset:3px;box-shadow:none}.d-data-table-row[data-d-table-interaction=true]:focus-visible>td,.d-data-table-row[data-d-table-interaction=true]:focus-visible>th[scope=row]{box-shadow:none!important}}
.d-field{display:flex;flex-direction:column;gap:6px}.d-field input,.d-field textarea,.d-field select{box-sizing:border-box;min-height:56px;padding:16px;border:1px solid var(--d-outline);border-radius:4px;color:var(--d-on-surface);background:var(--d-surface);line-height:24px}.d-field input::placeholder,.d-field textarea::placeholder{color:var(--d-field-placeholder);font-size:var(--d-field-placeholder-size,inherit);line-height:var(--d-field-placeholder-line,inherit);font-weight:var(--d-field-placeholder-weight,inherit);letter-spacing:var(--d-field-placeholder-spacing,inherit)}
.d-field input[type=search]{appearance:none;-webkit-appearance:none}
.d-field input[type=search]::-webkit-search-decoration,.d-field input[type=search]::-webkit-search-cancel-button,.d-field input[type=search]::-webkit-search-results-button,.d-field input[type=search]::-webkit-search-results-decoration{display:none}
.d-field-control{display:flex;align-items:center;gap:8px;min-height:56px}.d-field-control input,.d-field-control textarea{flex:1;min-width:0;height:100%;min-height:0;padding:0;border:0;border-radius:0;background:transparent}
.d-error{color:var(--d-error)}.d-toggle{position:relative;display:flex;gap:10px;align-items:center;min-height:48px;min-width:48px}.d-toggle:not(:has(.d-toggle-copy-visible)){justify-content:center}.d-toggle input{width:20px;height:20px}
.d-toggle::before{content:"";position:absolute;inset:0;pointer-events:none;background:var(--d-on-surface);opacity:0}.d-toggle:has(input:enabled):hover::before{opacity:.08}.d-toggle:has(input:enabled:focus-visible)::before,.d-toggle:has(input:enabled):active::before{opacity:.1}
.d-surface{background:var(--d-surface);color:var(--d-on-surface)}.d-avatar{position:relative;display:inline-grid;place-items:center;overflow:hidden;flex:none;width:40px;height:40px;border-radius:50%;background:var(--d-variant)}.d-badge{display:inline-grid;place-items:center;box-sizing:border-box;min-width:16px;width:max-content;height:16px;padding:0 4px;border-radius:999px;background:var(--d-error);color:var(--d-on-error);font-size:11px;line-height:16px;font-weight:500;font-variant-numeric:tabular-nums}.d-badge-dot{width:6px;min-width:6px;height:6px;padding:0}
.d-divider{margin:0;padding:0;flex:none;align-self:stretch;border:0;border-top:1px solid var(--d-outline-variant);width:100%}.d-banner{border-radius:var(--d-radius);background:transparent}
.d-collection{list-style:none;margin:0;padding:0}.d-tabs,.d-navigation{display:flex;gap:8px}.d-tabs button,.d-navigation button,.d-navigation a{text-decoration:none;display:inline-flex;align-items:center;padding:12px;border:0;background:transparent;color:var(--d-on-surface);min-height:44px}
.d-tabs [aria-selected=true],.d-navigation [aria-current=page]{color:var(--d-primary);border-bottom:3px solid var(--d-primary)}
.d-dialog{box-sizing:border-box;max-width:min(600px,90vw);display:flex;flex-direction:column;gap:16px;overflow-y:auto;border:1px solid var(--d-outline);border-radius:var(--d-radius);padding:24px;color:var(--d-on-surface);background:var(--d-surface)}.d-dialog-header{display:flex;align-items:center;justify-content:space-between;gap:12px}
.d-dialog::backdrop{background:color-mix(in srgb,var(--d-scrim,rgb(0 0 0)) 32%,transparent)}.d-sheet{margin:auto auto 0;min-width:min(600px,90vw);overflow:hidden}.d-sheet .d-dialog-header{touch-action:none}.d-sheet-drag-handle{display:flex;align-items:center;justify-content:center;min-height:24px;touch-action:none}.d-sheet-drag-indicator{width:32px;height:4px;border-radius:999px;background:var(--d-on-surface-variant,var(--d-outline));opacity:.6}.d-sheet{transition:transform 180ms ease-out}.d-dialog-bottom{margin:auto auto 0}.d-dialog-bottom-on-compact{margin:auto}.d-dialog-bottom-on-compact.d-sheet .d-sheet-drag-handle{display:none}.d-dialog-bottom-on-compact.d-sheet .d-dialog-header{touch-action:auto}.d-dialog-fallback{position:fixed;inset:50% auto auto 50%;z-index:2147483647;transform:translate(-50%,-50%)}.d-dialog-fallback-scrim{position:fixed;inset:0;background:color-mix(in srgb,var(--d-scrim,rgb(0 0 0)) 32%,transparent)}
@media(max-width:40rem){.d-dialog-bottom-on-compact{--d-modal-inline-inset:0px;position:fixed;inset:auto 0 0;margin:0 auto;max-width:100%}.d-dialog-bottom-on-compact.d-sheet .d-sheet-drag-handle{display:flex}.d-dialog-bottom-on-compact.d-sheet .d-dialog-header{touch-action:none}}
.d-scaffold{display:flex;flex-direction:column;height:100dvh}.d-scaffold-constrained{height:100%;min-height:0;min-width:0;overflow:hidden}.d-scaffold-middle{display:flex;flex:1;min-height:0}.d-scaffold-constrained>.d-scaffold-middle{min-height:0;overflow:hidden}.d-scaffold-body{flex:1;min-width:0;min-height:0;position:relative;overflow:auto}.d-scaffold-constrained>.d-scaffold-middle>.d-scaffold-body{height:100%;min-height:0}.d-floating{position:absolute;bottom:16px;right:16px}.d-snackbar{position:absolute;bottom:16px;left:16px}
.d-app-bar{display:flex;align-items:center;gap:12px;padding:12px 16px;min-height:64px;background:var(--d-surface)}.d-app-bar h2{font-size:var(--d-title);flex:1}.d-app-bar-actions{display:flex;gap:8px}.d-fab{min-width:56px;min-height:56px;border-radius:16px;box-shadow:0 3px 8px #0003}
.d-snackbar-message{display:flex;align-items:center;flex-wrap:wrap;gap:8px;min-height:48px;padding:0 8px 0 16px;border-radius:4px;background:var(--d-inverse-surface);color:var(--d-inverse-on-surface)}.d-snackbar-message>span{flex:1;padding-block:14px;font-size:14px;line-height:20px;letter-spacing:.25px}.d-snackbar-message button{border:0;background:transparent;color:var(--d-inverse-primary);min-height:48px;padding:0 12px;font-size:14px;line-height:20px;font-weight:500;letter-spacing:.1px}
.d-segments{display:flex;flex-wrap:wrap;gap:0}.d-segment{position:relative;cursor:pointer;display:flex;align-items:center;min-width:var(--d-item-min-interactive,0px);min-height:var(--d-item-min-interactive,0px)}.d-segment input{position:absolute;inset:0;z-index:1;margin:0;opacity:0;width:100%;height:100%;cursor:inherit}.d-segment input:disabled{cursor:default}.d-segment-surface{display:flex;align-items:center;gap:var(--d-item-gap,0px);min-height:var(--d-item-min-height,44px);padding:var(--d-item-padding,10px 16px);border:1px solid var(--d-outline)}.d-segment:first-child .d-segment-surface{border-radius:var(--d-radius) 0 0 var(--d-radius)}.d-segment:last-child .d-segment-surface{border-radius:0 var(--d-radius) var(--d-radius) 0}.d-segment input:checked+.d-segment-surface{background:var(--d-variant);color:var(--d-primary)}.d-segment input:focus-visible+.d-segment-surface{outline:3px solid var(--d-primary);outline-offset:3px}.d-segment input:disabled+.d-segment-surface{opacity:.5;cursor:default}
.d-range{position:relative;width:100%;height:48px;min-width:144px}.d-tooltip-anchor{display:inline-block;position:relative}.d-tooltip{position:absolute;z-index:5;bottom:100%;left:0;min-width:160px;max-width:280px;border-radius:4px;padding:8px 12px;background:var(--d-on-surface);color:var(--d-surface);font-size:var(--d-label)}
@media(prefers-reduced-motion:reduce){.d-theme *{transition:none!important;animation:none!important}}
.d-button,.d-chip,.d-icon-button,.d-field input,.d-field textarea,.d-navigation button,.d-navigation a{font:inherit;accent-color:var(--d-primary);box-sizing:border-box}
.d-button:focus-visible,.d-chip:focus-visible,.d-icon-button:focus-visible,.d-card:focus-visible,.d-field :focus-visible,.d-navigation :focus-visible,.d-tabs :focus-visible,.d-toggle :focus-visible,.d-segment :focus-visible{outline:3px solid var(--d-primary);outline-offset:3px}
.d-field *,.d-toggle *,.d-segments *,.d-tabs *,.d-navigation *,.d-scaffold *,.d-dialog *{box-sizing:border-box}
@media(prefers-reduced-motion:reduce){.d-button,.d-chip,.d-icon-button,.d-field *,.d-tabs *,.d-navigation *,.d-scaffold *{transition:none!important;animation:none!important}.d-button-spinner{animation:none!important}}
@media(forced-colors:active){.d-button:not(:disabled):focus-visible{outline:2px solid Highlight;outline-offset:3px;box-shadow:none}}
@media(forced-colors:active){.d-button[data-d-button-interaction=true]:not(:disabled):focus-visible,.d-icon-button[data-d-button-interaction=true]:not(:disabled):focus-visible{outline:2px solid Highlight;outline-offset:3px;box-shadow:none}}
.d-visually-hidden{position:absolute!important;width:1px!important;height:1px!important;padding:0!important;margin:-1px!important;overflow:hidden!important;clip:rect(0,0,0,0)!important;white-space:nowrap!important;border:0!important}
.d-toggle input[type=checkbox]:not([role=switch]),.d-toggle input[type=radio]{appearance:none;display:inline-grid;flex:0 0 20px;width:20px;height:20px;margin:0;border:2px solid var(--d-control-unselected,var(--d-on-surface-variant,var(--d-outline)));border-radius:2px;background:transparent;background-clip:padding-box;color:var(--d-control-checkmark,var(--d-on-primary));accent-color:var(--d-control-selected,var(--d-primary));transition:background-color 120ms ease,border-color 120ms ease}
.d-toggle input[type=checkbox]:not([role=switch]){position:relative}.d-toggle input[type=checkbox]:not([role=switch]),.d-toggle input[type=radio]{margin:2px}
.d-toggle input[type=checkbox]:not([role=switch]):checked{border-color:var(--d-control-selected,var(--d-primary));background-color:var(--d-control-selected,var(--d-primary))}
.d-toggle input[type=checkbox]:not([role=switch]):checked::after,.d-toggle input[type=checkbox]:not([role=switch]):indeterminate::after{content:"";position:absolute;inset:-2px;pointer-events:none;background:var(--d-control-checkmark,var(--d-on-primary));mask:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20'%3E%3Cpath d='M4 10 8 14 16 6' fill='none' stroke='white' stroke-width='2' stroke-linecap='square'/%3E%3C/svg%3E") center/20px 20px no-repeat}
.d-toggle input[type=checkbox]:not([role=switch]):indeterminate{border-color:var(--d-control-selected,var(--d-primary));background-color:var(--d-control-selected,var(--d-primary))}
.d-toggle input[type=checkbox]:not([role=switch]):indeterminate::after{mask:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20'%3E%3Cpath d='M4 10H16' stroke='white' stroke-width='2' stroke-linecap='square'/%3E%3C/svg%3E") center/20px 20px no-repeat}
.d-toggle input[type=radio]{border-radius:50%;background-color:transparent}.d-toggle input[type=radio]:checked{border-color:var(--d-control-selected,var(--d-primary));background:radial-gradient(circle,var(--d-control-selected,var(--d-primary)) 0 42%,transparent 46%)}
.d-toggle input[type=checkbox]:not([role=switch]):disabled,.d-toggle input[type=radio]:disabled{border-color:var(--d-control-disabled-unselected,color-mix(in srgb,var(--d-on-surface) 38%,transparent));opacity:1}
.d-toggle input[type=checkbox]:not([role=switch]):checked:disabled{border-color:var(--d-control-disabled-selected,var(--d-primary));background-color:var(--d-control-disabled-selected,var(--d-primary))}
.d-toggle input[type=checkbox]:not([role=switch]):disabled::after{background:var(--d-control-disabled-checkmark,var(--d-surface))}
.d-toggle input[type=radio]:checked:disabled{border-color:var(--d-control-disabled-selected,var(--d-primary));background:radial-gradient(circle,var(--d-control-disabled-selected,var(--d-primary)) 0 42%,transparent 46%)}
.d-toggle input[role=switch]{appearance:none;flex:0 0 52px;width:52px;height:32px;margin:0;border:2px solid var(--d-switch-unchecked-border,var(--d-outline));border-radius:999px;background-color:var(--d-switch-unchecked-track,var(--d-surface-container-highest,var(--d-variant)));background-image:radial-gradient(circle closest-side,var(--d-switch-unchecked-thumb,var(--d-outline)) 0 98%,transparent 100%);background-size:16px 16px;background-position:6px center;background-repeat:no-repeat;transition:background-color 150ms ease,border-color 150ms ease,background-position 150ms ease,background-size 150ms ease}
.d-toggle input[role=switch]:checked{border-color:var(--d-switch-checked-border,transparent);background-color:var(--d-switch-checked-track,var(--d-primary));background-image:radial-gradient(circle closest-side,var(--d-switch-checked-thumb,var(--d-on-primary)) 0 98%,transparent 100%);background-size:24px 24px;background-position:calc(100% - 2px) center}
.d-toggle input[role=switch]:disabled{opacity:1;border-color:var(--d-switch-disabled-unchecked-border);background-color:var(--d-switch-disabled-unchecked-track);background-image:radial-gradient(circle closest-side,var(--d-switch-disabled-unchecked-thumb) 0 98%,transparent 100%)}
.d-toggle input[role=switch]:checked:disabled{border-color:var(--d-switch-disabled-checked-border);background-color:var(--d-switch-disabled-checked-track);background-image:radial-gradient(circle closest-side,var(--d-switch-disabled-checked-thumb) 0 98%,transparent 100%)}
.d-toggle input[type=checkbox]:not([role=switch]):indeterminate:disabled{border-color:var(--d-control-disabled-selected);background-color:var(--d-control-disabled-selected)}
.d-slider{appearance:none;width:100%;height:48px;margin:0;background:transparent;accent-color:var(--d-slider-active-current,var(--d-primary));touch-action:pan-y}
.d-slider-surface{--d-slider-track-paint:var(--d-slider-marks,linear-gradient(transparent,transparent)),linear-gradient(to var(--d-slider-direction),var(--d-slider-active-current,var(--d-primary)) 0,var(--d-slider-active-current,var(--d-primary)) calc(var(--d-slider-progress,50%) - 8px),transparent calc(var(--d-slider-progress,50%) - 8px),transparent calc(var(--d-slider-progress,50%) + 8px),var(--d-slider-inactive-current,var(--d-variant)) calc(var(--d-slider-progress,50%) + 8px),var(--d-slider-inactive-current,var(--d-variant)) 100%)}
.d-slider-surface{position:relative;width:100%;height:48px;--d-slider-direction:right}.d-slider-surface::before{content:"";position:absolute;inset:16px 2px;border-radius:8px;background:var(--d-slider-track-paint);pointer-events:none}.d-slider-surface-vertical{width:48px;height:160px}.d-slider-surface-vertical::before{inset:2px 16px}.d-slider-surface>.d-slider{display:block;position:absolute;inset:0}
.d-slider::-webkit-slider-runnable-track{height:16px;border-radius:8px;background:transparent}
.d-slider::-moz-range-track{height:16px;border-radius:8px;background:transparent}.d-slider::-moz-range-progress{background:transparent}
.d-slider-vertical{height:160px;width:48px;touch-action:pan-x}.d-slider-vertical::-webkit-slider-runnable-track{height:100%;width:16px}.d-slider-vertical::-moz-range-track{height:100%;width:16px}
.d-slider::-webkit-slider-thumb{appearance:none;width:4px;height:44px;margin-top:-14px;border:0;border-radius:999px;background:var(--d-slider-thumb-current,var(--d-primary))}
.d-slider::-moz-range-thumb{width:4px;height:44px;border:0;border-radius:999px;background:var(--d-slider-thumb-current,var(--d-primary))}
.d-slider:active::-webkit-slider-thumb{width:2px}.d-slider:active::-moz-range-thumb{width:2px}
.d-range::before{content:"";position:absolute;inset:16px 2px;border-radius:8px;background:var(--d-slider-marks,linear-gradient(transparent,transparent)),linear-gradient(to right,var(--d-slider-inactive-current) 0,var(--d-slider-inactive-current) calc(var(--d-range-start) - 8px),transparent calc(var(--d-range-start) - 8px),transparent calc(var(--d-range-start) + 8px),var(--d-slider-active-current) calc(var(--d-range-start) + 8px),var(--d-slider-active-current) calc(var(--d-range-end) - 8px),transparent calc(var(--d-range-end) - 8px),transparent calc(var(--d-range-end) + 8px),var(--d-slider-inactive-current) calc(var(--d-range-end) + 8px))}
.d-range::before{mask-image:linear-gradient(to right,black 0,black calc(var(--d-range-start) - 8px),transparent calc(var(--d-range-start) - 8px),transparent calc(var(--d-range-start) + 8px),black calc(var(--d-range-start) + 8px),black calc(var(--d-range-end) - 8px),transparent calc(var(--d-range-end) - 8px),transparent calc(var(--d-range-end) + 8px),black calc(var(--d-range-end) + 8px))}
.d-range .d-slider{position:absolute;inset:0;width:100%}.d-range-start{clip-path:inset(0 calc(100% - var(--d-range-midpoint)) 0 0)}.d-range-end{clip-path:inset(0 0 0 var(--d-range-midpoint))}
.d-range .d-slider::-webkit-slider-runnable-track{background:transparent;mask-image:none}.d-range .d-slider::-moz-range-track,.d-range .d-slider::-moz-range-progress{background:transparent;mask-image:none}

.d-slider-vertical::-webkit-slider-thumb{width:44px;height:4px;margin-top:0;margin-left:-14px}.d-slider-vertical::-moz-range-thumb{width:44px;height:4px}

.d-chip{background:var(--d-item-container,var(--d-variant));color:var(--d-item-content,var(--d-on-surface));border-color:var(--d-item-border,transparent);border-width:var(--d-item-border-width,1px);border-radius:var(--d-item-radius,var(--d-radius))}
.d-chip[data-selected=true]{background:var(--d-item-selected-container,var(--d-variant));color:var(--d-item-selected-content,var(--d-primary));border-color:var(--d-item-selected-border,var(--d-primary))}
.d-chip:disabled{background:var(--d-item-disabled-container,var(--d-variant));color:var(--d-item-disabled-content,var(--d-on-surface));border-color:var(--d-item-disabled-border,var(--d-outline))}
.d-tabs button,.d-navigation button,.d-navigation a{background:var(--d-item-container,transparent);color:var(--d-item-content,var(--d-on-surface));border:0;border-radius:var(--d-item-radius,0)}
.d-tabs [aria-selected=true],.d-navigation [aria-current=page]{background:var(--d-item-selected-container,transparent);color:var(--d-item-selected-content,var(--d-primary));border-bottom:3px solid var(--d-item-selected-border,var(--d-primary))}
.d-segment-surface{background:var(--d-item-container,transparent);color:var(--d-item-content,var(--d-on-surface));border-color:var(--d-item-border,var(--d-outline));border-width:var(--d-item-border-width,1px)}
.d-segment:first-child .d-segment-surface{border-top-left-radius:var(--d-item-radius,var(--d-radius));border-bottom-left-radius:var(--d-item-radius,var(--d-radius))}.d-segment:last-child .d-segment-surface{border-top-right-radius:var(--d-item-radius,var(--d-radius));border-bottom-right-radius:var(--d-item-radius,var(--d-radius))}
.d-segment input:checked+.d-segment-surface{border-width:var(--d-item-selected-border-width,var(--d-item-border-width,1px));background:var(--d-item-selected-container,var(--d-variant));color:var(--d-item-selected-content,var(--d-primary));border-color:var(--d-item-selected-border,var(--d-outline))}
.d-segment input:disabled+.d-segment-surface{background:var(--d-item-disabled-container,var(--d-item-container,transparent));color:var(--d-item-disabled-content,var(--d-item-content,var(--d-on-surface)));border-color:var(--d-item-disabled-border,var(--d-outline))}
.d-progress{display:block;appearance:none;accent-color:var(--d-progress-color,var(--d-primary));height:var(--d-progress-stroke,4px);border:0;border-radius:999px;overflow:hidden;background:var(--d-progress-track,var(--d-surface-container-highest,var(--d-variant)))}.d-progress::-webkit-progress-bar{border-radius:999px;background:var(--d-progress-track,var(--d-surface-container-highest,var(--d-variant)))}.d-progress::-webkit-progress-value{border-radius:999px;background:var(--d-progress-color,var(--d-primary))}.d-progress::-moz-progress-bar{border-radius:999px;background:var(--d-progress-color,var(--d-primary))}.d-standard-active{stroke:var(--d-progress-color,var(--d-primary))}.d-standard-track{stroke:var(--d-progress-track,var(--d-secondary-container))}.d-progress-circular{width:40px;height:40px;vertical-align:middle}@keyframes d-progress-spin{to{transform:rotate(360deg)}}@keyframes d-wave-travel{to{stroke-dashoffset:-100}}@keyframes d-wave-circular-travel{to{stroke-dashoffset:-100;transform:rotate(270deg)}}@keyframes d-loading-rotate{to{transform:rotate(360deg)}}@keyframes d-loading-shape-one{0%,100%{opacity:1;transform:rotate(0deg) scale(.78)}33%{opacity:0;transform:rotate(120deg) scale(1)}66%{opacity:0;transform:rotate(240deg) scale(.9)}}@keyframes d-loading-shape-two{0%,33%,100%{opacity:0;transform:rotate(-30deg) scale(.9)}38%,61%{opacity:1;transform:rotate(90deg) scale(1)}68%{opacity:0;transform:rotate(150deg) scale(.8)}}@keyframes d-loading-shape-three{0%,60%,100%{opacity:0;transform:rotate(20deg) scale(.8)}66%,92%{opacity:1;transform:rotate(140deg) scale(1)}96%{opacity:0;transform:rotate(180deg) scale(.9)}}
:root .d-card{border-radius:var(--d-shape-medium,var(--d-radius))}:root .d-field input,:root .d-field textarea,:root .d-field select,.d-search-control,.d-autocomplete-control{border-radius:var(--d-shape-extra-small,var(--d-radius))}
:root .d-chip,:root .d-segment-surface,.d-navigation-drawer-item,.d-list-item{border-radius:var(--d-item-radius,var(--d-shape-small,var(--d-radius)))}
.d-theme-expressive .d-button,.d-theme-expressive .d-chip,.d-theme-expressive .d-loading-indicator,.d-theme-expressive .d-floating-toolbar{transition-timing-function:cubic-bezier(.2,.8,.2,1)}
.d-card-outlined{box-shadow:none}.d-card-elevated{box-shadow:0 1px 3px rgb(0 0 0 / .24)}.d-card-filled{box-shadow:none}.d-card[aria-disabled=true]{opacity:.5}
.d-card-stretched-link,.d-card-stretched-action{position:absolute;inset:0;z-index:1;border-radius:inherit}.d-card-stretched-action{width:100%;height:100%;padding:0;border:0;background:transparent;color:transparent}.d-card-content{position:relative;z-index:2;pointer-events:none}.d-card-content label,.d-card-content a,.d-card-content button,.d-card-content input,.d-card-content select,.d-card-content textarea,.d-card-content [tabindex],.d-card-content [role=button],.d-card-content [role=link]{pointer-events:auto}.d-card[aria-disabled=true]{opacity:.5}
.d-chip-assist,.d-chip-suggestion{border-radius:var(--d-shape-small,var(--d-radius))}.d-chip-input{border-radius:var(--d-shape-small,var(--d-radius))}
.d-chip-surface{display:contents}.d-chip-label{display:inline-flex;align-items:center;gap:var(--d-item-gap,8px)}.d-chip-icon{display:inline-flex;align-items:center;justify-content:center}.d-chip.d-item-hittarget{min-width:var(--d-item-min-interactive,48px);min-height:var(--d-item-min-interactive,48px);padding:0;border-width:0;border-color:transparent;background:transparent;box-shadow:none}.d-chip.d-item-hittarget .d-chip-surface{display:inline-flex;align-items:center;justify-content:center;gap:var(--d-item-gap,8px);min-height:var(--d-item-min-height,32px);padding:var(--d-item-padding,6px 12px);border:var(--d-item-border-width,1px) solid var(--d-item-border,transparent);border-radius:var(--d-item-radius,var(--d-shape-small,var(--d-radius)));background:var(--d-item-container,var(--d-variant));color:var(--d-item-content,var(--d-on-surface))}.d-chip.d-item-hittarget[data-selected=true] .d-chip-surface{border-width:var(--d-item-selected-border-width,var(--d-item-border-width,1px));background:var(--d-item-selected-container,var(--d-variant));color:var(--d-item-selected-content,var(--d-primary));border-color:var(--d-item-selected-border,var(--d-primary))}.d-chip.d-item-hittarget:disabled .d-chip-surface{background:var(--d-item-disabled-container,var(--d-variant));color:var(--d-item-disabled-content,var(--d-on-surface));border-color:var(--d-item-disabled-border,var(--d-outline))}
.d-icon-button-standard{background:transparent;color:var(--d-on-surface)}.d-icon-button-filled{background:var(--d-primary);color:var(--d-on-primary)}.d-icon-button-filled-tonal{background:var(--d-secondary-container);color:var(--d-on-secondary-container)}.d-icon-button-outlined{background:transparent;color:var(--d-on-surface);border-color:var(--d-outline)}
.d-tabs-secondary [aria-selected=true]{border-bottom:0;border-top:2px solid var(--d-item-selected-border,var(--d-primary))}.d-tabs-primary [aria-selected=true]{border-bottom-width:var(--d-tabs-indicator-height,3px)}
.d-navigation-short{min-height:56px}.d-bottom-app-bar-flexible{min-height:120px;padding-bottom:24px}.d-search-fullscreen[role=search]{position:fixed;z-index:40;inset:0;display:flex;flex-direction:column;padding:16px;background:var(--d-surface)}.d-carousel-centeredhero .d-carousel-item{flex-basis:min(80vw,600px);max-width:min(80vw,600px);min-height:420px}
.d-multi-segments{display:flex;gap:0}.d-multi-segment{min-height:var(--d-item-min-height,44px);padding:var(--d-item-padding,10px 16px);border:var(--d-item-border-width,1px) solid var(--d-item-border,var(--d-outline));border-radius:var(--d-item-radius,0);background:var(--d-item-container,transparent);color:var(--d-item-content,var(--d-on-surface))}.d-multi-segment[aria-pressed=true]{background:var(--d-item-selected-container,var(--d-variant));color:var(--d-item-selected-content,var(--d-primary));border-color:var(--d-item-selected-border,var(--d-outline))}.d-action-app-bar{position:relative;display:flex;align-items:center}.d-app-bar-overflow{position:absolute;z-index:15;inset-block-start:100%;inset-inline-end:0;display:flex;flex-direction:column;padding:8px;border-radius:var(--d-shape-small,var(--d-radius));background:var(--d-surface);box-shadow:0 4px 12px rgb(0 0 0 / .2)}.d-app-bar-overflow button{min-height:44px;border:0;background:transparent;color:var(--d-on-surface);text-align:start}.d-wide-navigation-rail-item{display:flex;align-items:center;justify-content:center;gap:12px;min-height:56px;border:0;border-radius:var(--d-item-radius,999px);background:var(--d-item-container,transparent);color:var(--d-item-content,var(--d-on-surface))}.d-wide-navigation-rail-item[aria-current=page]{background:var(--d-item-selected-container,var(--d-secondary-container));color:var(--d-item-selected-content,var(--d-on-secondary-container))}.d-wide-navigation-rail-item-compact{width:56px}
.d-progress-wavy{display:inline-flex;align-items:center;justify-content:center;overflow:visible;border-radius:0;background:transparent}.d-progress-wavy svg{display:block;overflow:visible}.d-wave-track,.d-wave-active{fill:none;stroke-linecap:round;stroke-linejoin:round;vector-effect:non-scaling-stroke}.d-wave-track{stroke:var(--d-progress-track,var(--d-variant));stroke-width:var(--d-progress-stroke,4px)}.d-wave-active{stroke:var(--d-progress-color,var(--d-primary));stroke-width:var(--d-progress-stroke,4px)}.d-progress-wavy-circular .d-wave-active{transform:rotate(-90deg);transform-origin:50% 50%}.d-wave-indeterminate{stroke-dasharray:32 68;animation:d-wave-travel var(--d-wave-duration,1s) linear infinite}.d-wave-static .d-wave-indeterminate{animation:none}.d-progress-wavy-linear .d-wave-indeterminate{stroke-dasharray:none;clip-path:inset(-50% 68% -50% 0);animation-name:d-wave-linear-travel}@keyframes d-wave-linear-travel{from{clip-path:inset(-50% 100% -50% 0)}50%{clip-path:inset(-50% 25% -50% 25%)}to{clip-path:inset(-50% 0 -50% 100%)}}.d-progress-wavy-circular .d-wave-indeterminate{animation-name:d-wave-circular-travel}
.d-app-bar-center-aligned .d-app-bar-title{position:absolute;left:50%;transform:translateX(-50%);text-align:center}.d-app-bar-medium,.d-app-bar-large,.d-app-bar-mediumflexible,.d-app-bar-largeflexible{align-items:flex-end;padding-bottom:24px}.d-app-bar-large .d-app-bar-title,.d-app-bar-largeflexible .d-app-bar-title{font-size:var(--d-title)}
.d-fab-small{min-width:40px;min-height:40px;border-radius:var(--d-shape-medium,16px)}.d-fab-medium{min-width:80px;min-height:80px;border-radius:var(--d-shape-large,28px)}.d-fab-large{min-width:96px;min-height:96px;border-radius:var(--d-shape-extra-large,32px)}
.d-button-group{display:flex;flex-wrap:wrap;align-items:center}.d-button-toggle[aria-pressed=true]{background:var(--d-secondary-container);color:var(--d-on-secondary-container)}.d-split-button>.d-button:first-child{border-start-end-radius:0;border-end-end-radius:0}.d-split-button>.d-button:last-child{border-start-start-radius:0;border-end-start-radius:0;border-inline-start:1px solid color-mix(in srgb,currentColor 20%,transparent)}
.d-loading-indicator{position:relative;display:grid;place-items:center;overflow:hidden;border-radius:50%;color:var(--d-progress-color,var(--d-primary))}.d-loading-indicator svg{display:block;overflow:visible}.d-loading-contained{background:var(--d-loading-container,var(--d-progress-track,var(--d-primary-container)));padding:8px}.d-loading-container-shape{fill:var(--d-loading-container,var(--d-progress-track,var(--d-primary-container)))}.d-loading-morph-shape{fill:var(--d-progress-color,var(--d-primary));stroke:none}.d-loading-determinate{fill:var(--d-progress-color,var(--d-primary));stroke:none}.d-loading-morph-contained{fill:var(--d-progress-color,var(--d-primary));stroke:none}.d-loading-morph-outline{fill:var(--d-progress-color,var(--d-primary));stroke:none}.d-loading-morph-shape{transform-box:fill-box;transform-origin:center;animation:d-loading-rotate 3.2s linear infinite}.d-loading-morph-0{animation:d-loading-shape-one 3.2s ease-in-out infinite}.d-loading-morph-1{animation:d-loading-shape-two 3.2s ease-in-out infinite}.d-loading-morph-2{animation:d-loading-shape-three 3.2s ease-in-out infinite}.d-loading-determinate{fill:var(--d-progress-color,var(--d-primary));stroke:none}
.d-floating-toolbar{position:relative;z-index:auto;display:flex;align-items:center;gap:12px;padding:12px;border-radius:var(--d-shape-large,var(--d-radius));background:var(--d-surface-container,var(--d-surface));color:var(--d-on-surface);box-shadow:0 4px 12px rgb(0 0 0 / .2)}.d-floating-toolbar[data-expanded=false]{gap:0}.d-fab-menu{position:relative;z-index:auto;display:flex;flex-direction:column;align-items:flex-start;gap:12px;width:max-content;max-width:100%}.d-fab-menu-items{display:flex;flex-direction:column;gap:8px;max-width:100%;padding:12px;border-radius:var(--d-shape-large,var(--d-radius));background:var(--d-surface-container,var(--d-surface));color:var(--d-on-surface);box-shadow:0 4px 12px rgb(0 0 0 / .2)}
.d-drag-handle{display:grid;place-items:center;min-width:48px;min-height:24px;touch-action:none;cursor:grab}.d-drag-handle:active{cursor:grabbing}.d-drag-handle-grip{display:block;width:32px;height:4px;border-radius:999px;background:currentColor;opacity:.45}
.d-list-item{display:flex;width:100%;align-items:center;gap:var(--d-item-gap,16px);min-height:var(--d-item-min-height,56px);padding:var(--d-item-padding,12px 16px);border:0;text-align:start;background:var(--d-item-container,transparent);color:var(--d-item-content,var(--d-on-surface))}.d-list-item-copy{display:flex;min-width:0;flex:1;flex-direction:column;gap:var(--d-item-gap,0px)}.d-list-item-headline{font-weight:400}.d-list-item-supporting{font-size:14px;line-height:20px;letter-spacing:.25px;color:var(--d-on-surface-variant)}.d-list-item-overline{font-size:11px;line-height:16px;letter-spacing:.5px;font-weight:500;color:var(--d-on-surface-variant)}.d-list-item:has(.d-list-item-supporting),.d-list-item:has(.d-list-item-overline){min-height:var(--d-item-min-height,72px)}.d-list-item:has(.d-list-item-supporting):has(.d-list-item-overline){min-height:var(--d-item-min-height,88px)}.d-list-item[aria-current=true]{background:var(--d-item-selected-container,var(--d-secondary-container));color:var(--d-item-selected-content,var(--d-on-secondary-container))}.d-list-item:disabled{background:var(--d-item-disabled-container,transparent);color:var(--d-item-disabled-content,var(--d-on-surface));opacity:.5}.d-list-item-first{border-end-start-radius:0;border-end-end-radius:0}.d-list-item-middle{border-radius:0}.d-list-item-last{border-start-start-radius:0;border-start-end-radius:0}
.d-bottom-app-bar{display:flex;align-items:center;gap:12px;min-height:80px;padding:12px 16px;border-radius:var(--d-shape-large,var(--d-radius)) var(--d-shape-large,var(--d-radius)) 0 0;background:var(--d-surface-container,var(--d-surface));color:var(--d-on-surface)}.d-navigation-drawer{position:absolute;inset-block:0;inset-inline-start:0;width:min(360px,90vw);padding:16px;overflow:auto;border-radius:0 var(--d-shape-large,var(--d-radius)) var(--d-shape-large,var(--d-radius));box-shadow:0 8px 24px rgb(0 0 0 / .24)}.d-drawer-scrim{position:fixed;inset:0;z-index:30;background:rgb(0 0 0 / .32)}.d-navigation-drawer-item{display:flex;width:100%;align-items:center;gap:12px;min-height:56px;padding:8px 16px;border:0;background:var(--d-item-container,transparent);color:var(--d-item-content,var(--d-on-surface));text-align:start}.d-navigation-drawer-item[aria-current=page]{background:var(--d-item-selected-container,var(--d-secondary-container));color:var(--d-item-selected-content,var(--d-on-secondary-container))}.d-wide-navigation-rail{display:flex;flex:0 0 88px;flex-direction:column;align-items:center;gap:12px;overflow:hidden}.d-wide-navigation-rail-expanded{flex-basis:280px;align-items:stretch;padding:16px}
.d-bottom-sheet-scaffold{display:flex;min-height:100%;flex-direction:column}.d-bottom-sheet-content{flex:1}.d-bottom-sheet{position:sticky;bottom:0;max-height:80vh;overflow:auto;border-radius:var(--d-shape-large,var(--d-radius)) var(--d-shape-large,var(--d-radius)) 0 0;padding:16px}.d-bottom-sheet-handle{align-self:center;min-height:44px}
.d-search-bar,.d-autocomplete{position:relative;display:flex;flex-direction:column;gap:6px}.d-search-control,.d-autocomplete-control{display:flex;align-items:center;gap:8px;min-height:var(--d-input-min-height,56px);padding:var(--d-input-padding,0 16px);border:var(--d-input-outline-width,1px) solid var(--d-input-outline,var(--d-outline));background:var(--d-input-container,var(--d-surface));color:var(--d-input-text,var(--d-on-surface))}.d-search-control:focus-within,.d-autocomplete-control:focus-within{border-width:var(--d-input-outline-focused-width,2px);border-color:var(--d-input-outline-focused,var(--d-primary));background:var(--d-input-container-focused,var(--d-surface))}.d-search-control input,.d-autocomplete-control input{flex:1;min-width:0;min-height:inherit;border:0;outline:0;padding:0;background:transparent;color:inherit;caret-color:var(--d-input-cursor,var(--d-primary))}.d-search-control input::placeholder,.d-autocomplete-control input::placeholder{font-size:var(--d-input-placeholder-size,inherit)}.d-search-results,.d-autocomplete-options{position:absolute;z-index:15;top:100%;inset-inline:0;display:flex;flex-direction:column;max-height:50vh;overflow:auto;padding:8px;border-radius:0 0 var(--d-shape-medium,var(--d-radius)) var(--d-shape-medium,var(--d-radius));background:var(--d-surface);box-shadow:0 4px 12px rgb(0 0 0 / .2)}.d-search-results:empty{display:none}.d-autocomplete-options button{text-align:start;min-height:44px;border:0;background:transparent;color:var(--d-on-surface)}.d-autocomplete-options button[aria-selected=true]{background:var(--d-secondary-container);color:var(--d-on-secondary-container)}
.d-date-picker,.d-date-range-picker,.d-time-picker{display:flex;flex-direction:column;gap:8px;padding:16px;border-radius:var(--d-shape-medium,var(--d-radius));background:var(--d-surface)}.d-calendar-header{display:flex;align-items:center;justify-content:space-between;gap:8px}.d-calendar-grid{display:grid;grid-template-columns:repeat(7,minmax(36px,1fr));gap:4px;text-align:center}.d-calendar-grid>[role=columnheader]{font-size:var(--d-label);padding:8px 0}.d-calendar-day{min-width:36px;min-height:36px;padding:4px;border:0;border-radius:50%;background:transparent;color:var(--d-on-surface)}.d-calendar-day[aria-pressed=true]{background:var(--d-primary);color:var(--d-on-primary)}.d-calendar-day:disabled{opacity:.4}.d-date-range-picker input,.d-date-picker input,.d-time-picker input{min-height:48px;padding:8px;border:1px solid var(--d-outline);border-radius:var(--d-shape-extra-small,var(--d-radius));background:var(--d-surface);color:var(--d-on-surface)}
.d-carousel{position:relative;min-width:0}.d-carousel-track{display:flex;gap:var(--d-carousel-gap,8px);overflow-x:auto;scroll-snap-type:x mandatory;overscroll-behavior-inline:contain}.d-carousel-item{flex:0 0 var(--d-carousel-item-width,240px);min-width:var(--d-carousel-min-width,40px);max-width:var(--d-carousel-item-width,240px);scroll-snap-align:start}.d-carousel-fullscreen .d-carousel-item{flex-basis:100%;max-width:100%;min-height:70vh}.d-carousel-controls{display:flex;align-items:center;justify-content:center;gap:12px;padding:8px}.d-carousel-controls button{min-width:44px;min-height:44px}.d-pull-to-refresh{position:relative;touch-action:pan-x}.d-pull-refresh-indicator{position:absolute;z-index:1;top:0;left:50%;transform:translate(-50%,var(--d-pull-distance,0px));min-height:40px}.d-swipe-dismiss{position:relative;overflow:hidden;touch-action:pan-y}.d-swipe-background{position:absolute;inset:0;background:var(--d-error-container,var(--d-error));color:var(--d-on-error-container,var(--d-on-error))}.d-swipe-content{position:relative;background:var(--d-surface);color:var(--d-on-surface);transform:translateX(var(--d-swipe-offset,0px));transition:transform 120ms ease}
.d-floating-end{right:16px}.d-floating-start{left:16px}.d-floating-center{right:auto}
@media(prefers-reduced-motion:reduce){.d-toggle input[role=switch][data-custom-colors],.d-slider{transition:none!important;animation:none!important}}
@media(forced-colors:active){.d-toggle input[data-custom-colors],.d-slider,.d-chip,.d-tabs button,.d-navigation button,.d-navigation a,.d-segment-surface{forced-color-adjust:auto}.d-toggle input[data-custom-colors]:checked,.d-chip:not(.d-item-hittarget)[data-selected=true],.d-chip.d-item-hittarget[data-selected=true] .d-chip-surface,.d-tabs [aria-selected=true],.d-navigation [aria-current=page],.d-segment input:checked+.d-segment-surface{color:HighlightText;background:Highlight;border-color:Highlight}.d-toggle input[data-custom-colors]:focus-visible,.d-slider:focus-visible{outline:2px solid Highlight;outline-offset:3px}}
.d-calendar-grid{display:flex;flex-direction:column;gap:4px}.d-calendar-week{display:grid;grid-template-columns:repeat(7,minmax(36px,1fr));gap:4px}.d-calendar-week>[role=columnheader]{font-size:var(--d-label);padding:8px 0}.d-calendar-day-in-range{border-radius:0;background:var(--d-secondary-container);color:var(--d-on-secondary-container)}.d-calendar-day-range-start{border-start-start-radius:50%;border-end-start-radius:50%}.d-calendar-day-range-end{border-start-end-radius:50%;border-end-end-radius:50%}.d-calendar-day[aria-pressed=true]{background:var(--d-primary);color:var(--d-on-primary)}
.d-time-dial{display:flex;flex-direction:column;align-items:center;gap:12px}.d-time-value{font-variant-numeric:tabular-nums;font-size:var(--d-title)}.d-time-dial-controls{position:relative;display:grid;place-items:center;width:280px;height:280px;border-radius:50%;background:var(--d-secondary-container)}.d-time-dial-value{position:absolute;top:50%;left:50%;width:40px;height:40px;margin:-20px;border:0;border-radius:50%;transform:rotate(var(--d-time-dial-angle)) translateY(calc(var(--d-time-dial-radius) * -1)) rotate(calc(var(--d-time-dial-angle) * -1));background:transparent;color:var(--d-on-secondary-container)}.d-time-dial-value[aria-current=true]{background:var(--d-primary);color:var(--d-on-primary)}.d-date-range-fields{display:flex;flex-direction:column;gap:4px}
.d-calendar-day:focus-visible,.d-time-dial-value:focus-visible,.d-action-app-bar button:focus-visible,.d-autocomplete-options button:focus-visible{outline:3px solid var(--d-primary);outline-offset:2px}.d-app-bar-overflow button:disabled,.d-time-dial button:disabled{opacity:.38}
@media(prefers-reduced-motion:reduce){.d-loading-morph-shape,.d-wave-indeterminate,.d-loading-indicator-track,.d-swipe-content,.d-carousel-track{animation:none!important;transition:none!important;scroll-behavior:auto!important}}
@media(forced-colors:active){.d-calendar-day,.d-time-dial-value,.d-multi-segment,.d-action-app-bar button,.d-autocomplete-options button,.d-list-item,.d-navigation-drawer-item,.d-wide-navigation-rail-item,.d-bottom-sheet-handle{forced-color-adjust:auto;border:1px solid ButtonText}.d-calendar-day[aria-pressed=true],.d-calendar-day-in-range,.d-time-dial-value[aria-current=true],.d-multi-segment[aria-pressed=true],.d-navigation-drawer-item[aria-current=page],.d-wide-navigation-rail-item[aria-current=page]{background:Highlight;color:HighlightText;border-color:Highlight}.d-calendar-day:focus-visible,.d-time-dial-value:focus-visible{outline:2px solid Highlight;outline-offset:2px}}
.d-chip:not(.d-item-hittarget):hover:not(:disabled):not([data-selected=true]),.d-chip.d-item-hittarget:hover:not(:disabled):not([data-selected=true]) .d-chip-surface,.d-multi-segment:hover:not(:disabled):not([aria-pressed=true]),.d-tabs button:hover:not(:disabled):not([aria-selected=true]),.d-navigation button:hover:not(:disabled):not([aria-current=page]),.d-navigation a:hover:not([aria-current=page]),.d-list-item:hover:not(:disabled):not([aria-current=true]),.d-navigation-drawer-item:hover:not(:disabled):not([aria-current=page]),.d-wide-navigation-rail-item:hover:not(:disabled):not([aria-current=page]),.d-segment:hover:not(:has(input:checked)) .d-segment-surface{background:var(--d-item-hovered-container,var(--d-item-container,transparent));color:var(--d-item-hovered-content,var(--d-item-content,var(--d-on-surface)));border-color:var(--d-item-hovered-border,var(--d-item-border,var(--d-outline)))}
.d-chip:not(.d-item-hittarget):hover:not(:disabled)[data-selected=true],.d-chip.d-item-hittarget:hover:not(:disabled)[data-selected=true] .d-chip-surface,.d-multi-segment:hover:not(:disabled)[aria-pressed=true],.d-tabs button:hover:not(:disabled)[aria-selected=true],.d-navigation [aria-current=page]:hover:not(:disabled),.d-list-item:hover:not(:disabled)[aria-current=true],.d-navigation-drawer-item:hover:not(:disabled)[aria-current=page],.d-wide-navigation-rail-item:hover:not(:disabled)[aria-current=page],.d-segment:hover:has(input:checked) .d-segment-surface{background:var(--d-item-selected-container,var(--d-item-container,transparent));color:var(--d-item-selected-content,var(--d-item-content,var(--d-on-surface)));border-color:var(--d-item-selected-border,var(--d-item-border,var(--d-outline)))}
.d-chip:not(.d-item-hittarget):focus-visible:not(:disabled):not([data-selected=true]),.d-chip.d-item-hittarget:focus-visible:not(:disabled):not([data-selected=true]) .d-chip-surface,.d-multi-segment:focus-visible:not(:disabled):not([aria-pressed=true]),.d-tabs button:focus-visible:not(:disabled):not([aria-selected=true]),.d-navigation button:focus-visible:not(:disabled):not([aria-current=page]),.d-navigation a:focus-visible:not([aria-current=page]),.d-list-item:focus-visible:not(:disabled):not([aria-current=true]),.d-navigation-drawer-item:focus-visible:not(:disabled):not([aria-current=page]),.d-wide-navigation-rail-item:focus-visible:not(:disabled):not([aria-current=page]),.d-segment:has(input:focus-visible):not(:has(input:checked)) .d-segment-surface{background:var(--d-item-focused-container,var(--d-item-container,transparent));color:var(--d-item-focused-content,var(--d-item-content,var(--d-on-surface)));border-color:var(--d-item-focused-border,var(--d-item-border,var(--d-outline)))}
.d-chip:not(.d-item-hittarget):focus-visible:not(:disabled)[data-selected=true],.d-chip.d-item-hittarget:focus-visible:not(:disabled)[data-selected=true] .d-chip-surface,.d-multi-segment:focus-visible:not(:disabled)[aria-pressed=true],.d-tabs button:focus-visible:not(:disabled)[aria-selected=true],.d-navigation [aria-current=page]:focus-visible:not(:disabled),.d-list-item:focus-visible:not(:disabled)[aria-current=true],.d-navigation-drawer-item:focus-visible:not(:disabled)[aria-current=page],.d-wide-navigation-rail-item:focus-visible:not(:disabled)[aria-current=page],.d-segment:has(input:focus-visible:checked) .d-segment-surface{background:var(--d-item-selected-container,var(--d-item-container,transparent));color:var(--d-item-selected-content,var(--d-item-content,var(--d-on-surface)));border-color:var(--d-item-selected-border,var(--d-item-border,var(--d-outline)))}
.d-chip:not(.d-item-hittarget):active:not(:disabled):not([data-selected=true]),.d-chip.d-item-hittarget:active:not(:disabled):not([data-selected=true]) .d-chip-surface,.d-multi-segment:active:not(:disabled):not([aria-pressed=true]),.d-tabs button:active:not(:disabled):not([aria-selected=true]),.d-navigation button:active:not(:disabled):not([aria-current=page]),.d-navigation a:active:not([aria-current=page]),.d-list-item:active:not(:disabled):not([aria-current=true]),.d-navigation-drawer-item:active:not(:disabled):not([aria-current=page]),.d-wide-navigation-rail-item:active:not(:disabled):not([aria-current=page]),.d-segment:active:not(:has(input:checked)) .d-segment-surface{background:var(--d-item-pressed-container,var(--d-item-hovered-container,var(--d-item-container,transparent)));color:var(--d-item-pressed-content,var(--d-item-hovered-content,var(--d-item-content,var(--d-on-surface))));border-color:var(--d-item-pressed-border,var(--d-item-hovered-border,var(--d-item-border,var(--d-outline))))}
.d-chip:not(.d-item-hittarget):active:not(:disabled)[data-selected=true],.d-chip.d-item-hittarget:active:not(:disabled)[data-selected=true] .d-chip-surface,.d-multi-segment:active:not(:disabled)[aria-pressed=true],.d-tabs button:active:not(:disabled)[aria-selected=true],.d-navigation [aria-current=page]:active:not(:disabled),.d-list-item:active:not(:disabled)[aria-current=true],.d-navigation-drawer-item:active:not(:disabled)[aria-current=page],.d-wide-navigation-rail-item:active:not(:disabled)[aria-current=page],.d-segment:active:has(input:checked) .d-segment-surface{background:var(--d-item-pressed-container,var(--d-item-hovered-container,var(--d-item-selected-container,var(--d-item-container,transparent))));color:var(--d-item-pressed-content,var(--d-item-hovered-content,var(--d-item-selected-content,var(--d-item-content,var(--d-on-surface)))));border-color:var(--d-item-pressed-border,var(--d-item-hovered-border,var(--d-item-selected-border,var(--d-item-border,var(--d-outline)))))}
.d-chip:disabled,.d-multi-segment:disabled,.d-tabs button:disabled,.d-navigation button:disabled,.d-list-item:disabled,.d-navigation-drawer-item:disabled,.d-wide-navigation-rail-item:disabled,.d-segment input:disabled+.d-segment-surface{background:var(--d-item-disabled-container,var(--d-item-container,transparent));color:var(--d-item-disabled-content,var(--d-item-content,var(--d-on-surface)));border-color:var(--d-item-disabled-border,var(--d-item-border,var(--d-outline)))}
@media(forced-colors:active){.d-chip:hover,.d-chip:focus-visible,.d-chip:active,.d-multi-segment:hover,.d-multi-segment:focus-visible,.d-multi-segment:active,.d-tabs button:hover,.d-tabs button:focus-visible,.d-tabs button:active,.d-navigation button:hover,.d-navigation button:focus-visible,.d-navigation button:active,.d-list-item:hover,.d-list-item:focus-visible,.d-list-item:active,.d-navigation-drawer-item:hover,.d-navigation-drawer-item:focus-visible,.d-navigation-drawer-item:active,.d-wide-navigation-rail-item:hover,.d-wide-navigation-rail-item:focus-visible,.d-wide-navigation-rail-item:active{forced-color-adjust:auto}.d-chip:not(.d-item-hittarget)[data-selected=true],.d-chip.d-item-hittarget[data-selected=true] .d-chip-surface,.d-multi-segment[aria-pressed=true],.d-tabs [aria-selected=true],.d-navigation [aria-current=page],.d-list-item[aria-current=true],.d-navigation-drawer-item[aria-current=page],.d-wide-navigation-rail-item[aria-current=page]{background:Highlight!important;color:HighlightText!important;border-color:Highlight!important}.d-chip:disabled,.d-multi-segment:disabled,.d-tabs button:disabled,.d-navigation button:disabled,.d-list-item:disabled,.d-navigation-drawer-item:disabled,.d-wide-navigation-rail-item:disabled{color:GrayText!important;border-color:GrayText!important}.d-chip:focus-visible,.d-multi-segment:focus-visible,.d-tabs button:focus-visible,.d-navigation :focus-visible,.d-list-item:focus-visible,.d-navigation-drawer-item:focus-visible,.d-wide-navigation-rail-item:focus-visible{outline:2px solid Highlight!important;outline-offset:2px}}
@media(forced-colors:active){.d-chip.d-item-hittarget:focus-visible{outline:none!important}.d-chip.d-item-hittarget:focus-visible .d-chip-surface{outline:2px solid Highlight!important;outline-offset:2px}}
.d-chip.d-item-hittarget:disabled .d-chip-surface{background:var(--d-item-disabled-container,var(--d-item-container,var(--d-variant)));color:var(--d-item-disabled-content,var(--d-item-content,var(--d-on-surface)));border-color:var(--d-item-disabled-border,var(--d-item-border,var(--d-outline)))}
.d-chip:not(.d-item-hittarget):disabled[data-selected=true],.d-chip.d-item-hittarget:disabled[data-selected=true] .d-chip-surface,.d-multi-segment:disabled[aria-pressed=true],.d-tabs button:disabled[aria-selected=true],.d-navigation button:disabled[aria-current=page],.d-navigation a[aria-disabled=true][aria-current=page],.d-list-item:disabled[aria-current=true],.d-navigation-drawer-item:disabled[aria-current=page],.d-wide-navigation-rail-item:disabled[aria-current=page],.d-segment input:disabled:checked+.d-segment-surface{background:var(--d-item-disabled-selected-container,var(--d-item-disabled-container,var(--d-item-container,transparent)));color:var(--d-item-disabled-selected-content,var(--d-item-disabled-content,var(--d-item-content,var(--d-on-surface))));border-color:var(--d-item-disabled-selected-border,var(--d-item-disabled-border,var(--d-item-border,var(--d-outline))))}
.d-chip:not(.d-item-hittarget)[data-selected=true],.d-chip[data-selected=true] .d-chip-surface,.d-multi-segment[aria-pressed=true],.d-tabs [aria-selected=true],.d-navigation [aria-current=page],.d-list-item[aria-current=true],.d-navigation-drawer-item[aria-current=page],.d-wide-navigation-rail-item[aria-current=page],.d-segment input:checked+.d-segment-surface{border-width:var(--d-item-selected-border-width,var(--d-item-border-width,1px))}
.d-chip:not(.d-item-hittarget):hover:not(:disabled),.d-chip.d-item-hittarget:hover:not(:disabled) .d-chip-surface,.d-multi-segment:hover:not(:disabled),.d-tabs button:hover:not(:disabled),.d-navigation button:hover:not(:disabled),.d-navigation a:hover,.d-list-item:hover:not(:disabled),.d-navigation-drawer-item:hover:not(:disabled),.d-wide-navigation-rail-item:hover:not(:disabled),.d-segment:hover .d-segment-surface{box-shadow:inset 0 0 0 999px color-mix(in srgb,currentColor var(--d-item-hover-opacity,8%),transparent)}
.d-chip:not(.d-item-hittarget):focus-visible:not(:disabled),.d-chip.d-item-hittarget:focus-visible:not(:disabled) .d-chip-surface,.d-multi-segment:focus-visible:not(:disabled),.d-tabs button:focus-visible:not(:disabled),.d-navigation button:focus-visible:not(:disabled),.d-navigation a:focus-visible,.d-list-item:focus-visible:not(:disabled),.d-navigation-drawer-item:focus-visible:not(:disabled),.d-wide-navigation-rail-item:focus-visible:not(:disabled),.d-segment:has(input:focus-visible) .d-segment-surface{box-shadow:inset 0 0 0 999px color-mix(in srgb,currentColor var(--d-item-focus-opacity,12%),transparent)}
.d-chip.d-item-hittarget:focus-visible{outline:none}.d-chip.d-item-hittarget:focus-visible .d-chip-surface{outline:3px solid var(--d-primary);outline-offset:3px}
.d-chip:not(.d-item-hittarget):active:not(:disabled),.d-chip.d-item-hittarget:active:not(:disabled) .d-chip-surface,.d-multi-segment:active:not(:disabled),.d-tabs button:active:not(:disabled),.d-navigation button:active:not(:disabled),.d-navigation a:active,.d-list-item:active:not(:disabled),.d-navigation-drawer-item:active:not(:disabled),.d-wide-navigation-rail-item:active:not(:disabled),.d-segment:active .d-segment-surface{box-shadow:inset 0 0 0 999px color-mix(in srgb,currentColor var(--d-item-pressed-opacity,12%),transparent)}
.d-chip:disabled,.d-multi-segment:disabled,.d-tabs button:disabled,.d-navigation button:disabled,.d-list-item:disabled,.d-navigation-drawer-item:disabled,.d-wide-navigation-rail-item:disabled,.d-segment input:disabled+.d-segment-surface{opacity:var(--d-item-disabled-opacity,1)}
.d-search-full-screen[role=search]{position:fixed;z-index:40;inset:0;display:flex;flex-direction:column;padding:16px;background:var(--d-surface)}.d-search-contained-full-screen[role=search]{position:fixed;z-index:40;inset:16px;display:flex;flex-direction:column;padding:16px;border-radius:var(--d-shape-large,var(--d-radius));background:var(--d-surface);box-shadow:0 8px 28px rgb(0 0 0 / .22)} .d-search-full-screen[role=search] .d-search-results,
.d-search-contained-full-screen[role=search] .d-search-results{position:static;flex:1;min-height:0;max-height:none}
.d-search-docked-with-gap .d-search-control{gap:16px}.d-search-app-bar{min-width:0}.d-search-app-bar .d-search-control{min-height:48px;border-radius:var(--d-shape-extra-large,999px)}
.d-dropdown-menu-item{display:flex;align-items:center;gap:var(--d-item-gap,12px);width:100%;min-height:var(--d-item-min-height,48px);padding:var(--d-item-padding,8px 12px);border:var(--d-item-border-width,0) solid var(--d-item-border,transparent);border-radius:var(--d-item-radius,0);background:var(--d-item-container,transparent);color:var(--d-item-content,var(--d-on-surface));text-align:start}.d-dropdown-menu-item[aria-checked=true]{background:var(--d-item-selected-container,var(--d-secondary-container));color:var(--d-item-selected-content,var(--d-on-secondary-container))}.d-dropdown-menu-item:disabled{opacity:var(--d-item-disabled-opacity,1)}.d-dropdown-menu-labels{display:flex;flex:1;flex-direction:column;gap:2px}.d-dropdown-menu-supporting{font-size:var(--d-label);opacity:.72}.d-dropdown-menu-leading,.d-dropdown-menu-trailing{display:inline-flex;flex:0 0 auto;align-items:center;justify-content:center}.d-menu-group{gap:2px;padding:8px;border-radius:var(--d-shape-small,var(--d-radius))}.d-component-label{display:block}.d-component-label-content{display:flex;flex-direction:column;gap:4px}.d-component-label-text{position:absolute;z-index:1;inset-inline-start:12px;inset-block-start:8px;max-width:calc(100% - 24px);padding:0 4px;overflow:hidden;background:var(--d-surface);color:var(--d-primary);font-size:var(--d-label);text-overflow:ellipsis;white-space:nowrap;opacity:0;pointer-events:none;transform:translateY(6px);transition:opacity 120ms ease,transform 120ms ease}.d-component-label:hover .d-component-label-text,.d-component-label:focus-within .d-component-label-text,.d-component-label:active .d-component-label-text,.d-component-label-persistent .d-component-label-text{opacity:1;transform:translateY(0)}.d-component-label-persistent .d-component-label-text{position:static;display:block;max-width:none;padding:0;background:transparent;transform:none}.d-scroll-field{border:var(--d-item-border-width,1px) solid var(--d-item-border,var(--d-outline));border-radius:var(--d-item-radius,var(--d-shape-small,var(--d-radius)));background:var(--d-item-container,transparent);color:var(--d-item-content,var(--d-on-surface));padding:var(--d-item-padding,4px)}.d-scroll-field:focus-visible{outline:2px solid var(--d-primary);outline-offset:2px}.d-scroll-field[aria-disabled=true]{opacity:var(--d-item-disabled-opacity,1)}.d-scroll-field-viewport{width:min(160px,40vw);height:168px;overflow-y:auto;overscroll-behavior:contain;scroll-snap-type:y mandatory;scrollbar-width:none}.d-scroll-field-viewport::-webkit-scrollbar{display:none}.d-scroll-field-option{display:grid;min-height:44px;place-items:center;scroll-snap-align:center;cursor:pointer}.d-scroll-field-option-selected{font-weight:600}.d-app-bar-row,.d-app-bar-second-row{display:flex;align-items:center;gap:12px;width:100%}.d-app-bar-second-row{padding-inline-start:56px;font-size:var(--d-body)}.d-app-bar-heading{display:flex;flex:1;flex-direction:column;gap:2px}.d-app-bar-subtitle{font-size:var(--d-label);opacity:.8}.d-dialog-full-screen{width:100vw;height:100dvh;max-width:none;max-height:none;margin:0;border-radius:0}.d-dialog-date-picker,.d-dialog-time-picker{max-width:min(560px,94vw)}
.d-dropdown-menu-item{transition:background-color 120ms ease,color 120ms ease}.d-dropdown-menu-item:not(:disabled):hover{background:var(--d-item-hovered-container,var(--d-item-container,transparent));color:var(--d-item-hovered-content,var(--d-item-content,var(--d-on-surface)));box-shadow:inset 0 0 0 999px color-mix(in srgb,currentColor var(--d-item-hover-opacity,8%),transparent)}.d-dropdown-menu-item:not(:disabled):focus-visible{background:var(--d-item-focused-container,var(--d-item-container,transparent));color:var(--d-item-focused-content,var(--d-item-content,var(--d-on-surface)));border-color:var(--d-item-focused-border,var(--d-item-border,transparent));outline:2px solid var(--d-primary);outline-offset:-2px;box-shadow:inset 0 0 0 999px color-mix(in srgb,currentColor var(--d-item-focus-opacity,12%),transparent)}.d-dropdown-menu-item:not(:disabled):active{background:var(--d-item-pressed-container,var(--d-item-container,transparent));color:var(--d-item-pressed-content,var(--d-item-content,var(--d-on-surface)));border-color:var(--d-item-pressed-border,var(--d-item-border,transparent));box-shadow:inset 0 0 0 999px color-mix(in srgb,currentColor var(--d-item-pressed-opacity,16%),transparent)}.d-dropdown-menu-item:disabled{background:var(--d-item-disabled-container,var(--d-item-container,transparent));color:var(--d-item-disabled-content,var(--d-item-content,var(--d-on-surface)));border-color:var(--d-item-disabled-border,var(--d-item-border,transparent))}
.d-dropdown-menu-item[aria-checked=true]:not(:disabled):hover{background:var(--d-item-hovered-container,var(--d-item-selected-container,var(--d-secondary-container)));color:var(--d-item-hovered-content,var(--d-item-selected-content,var(--d-on-secondary-container)))}.d-dropdown-menu-item[aria-checked=true]:not(:disabled):focus-visible{background:var(--d-item-focused-container,var(--d-item-selected-container,var(--d-secondary-container)));color:var(--d-item-focused-content,var(--d-item-selected-content,var(--d-on-secondary-container)))}.d-dropdown-menu-item[aria-checked=true]:not(:disabled):active{background:var(--d-item-pressed-container,var(--d-item-selected-container,var(--d-secondary-container)));color:var(--d-item-pressed-content,var(--d-item-selected-content,var(--d-on-secondary-container)))}
.d-dropdown-menu-item:disabled[aria-checked=true]{background:var(--d-item-disabled-selected-container,var(--d-item-disabled-container,var(--d-item-container,transparent)));color:var(--d-item-disabled-selected-content,var(--d-item-disabled-content,var(--d-item-content,var(--d-on-surface))));border-color:var(--d-item-disabled-selected-border,var(--d-item-disabled-border,var(--d-item-border,transparent)))}
@media(prefers-reduced-motion:reduce){.d-dropdown-menu-item{transition:none}}
@media(forced-colors:active){.d-dropdown-menu-item:focus-visible{outline:2px solid Highlight;outline-offset:-2px;box-shadow:none}.d-dropdown-menu-item[aria-checked=true]{background:Highlight;color:HighlightText;border-color:Highlight}}
.d-menu-position{inset-block-start:16px;inset-inline-end:16px;max-width:calc(100vw - 32px);overflow:visible}
.d-menu-popup{background:var(--d-surface-container,var(--d-surface));color:var(--d-on-surface);display:flex;flex-direction:column;box-sizing:border-box;width:100%;min-width:112px;max-width:var(--d-menu-max-width,min(280px,calc(100vw - 32px)));max-height:calc(100vh - 32px);overflow:auto;padding:8px;border-radius:var(--d-shape-small,var(--d-radius));box-shadow:var(--d-menu-shadow,0 4px 12px rgb(0 0 0 / .2))}
.d-chip.d-item-hittarget .d-chip-surface,.d-segment-surface{border-start-start-radius:var(--d-item-radius-start-start,var(--d-item-radius,0));border-start-end-radius:var(--d-item-radius-start-end,var(--d-item-radius,0));border-end-end-radius:var(--d-item-radius-end-end,var(--d-item-radius,0));border-end-start-radius:var(--d-item-radius-end-start,var(--d-item-radius,0))}
.d-segment:first-child .d-segment-surface{border-start-start-radius:var(--d-item-radius-start-start,var(--d-item-radius,var(--d-radius)));border-end-start-radius:var(--d-item-radius-end-start,var(--d-item-radius,var(--d-radius)))}.d-segment:last-child .d-segment-surface{border-start-end-radius:var(--d-item-radius-start-end,var(--d-item-radius,var(--d-radius)));border-end-end-radius:var(--d-item-radius-end-end,var(--d-item-radius,var(--d-radius)))}
.d-snackbar-host>.d-snackbar-message{max-width:min(720px,100%);min-height:48px;box-shadow:0 3px 8px rgb(0 0 0 / .24);pointer-events:auto}
.d-scroll-field-viewport::before,.d-scroll-field-viewport::after{content:"";display:block;height:62px;scroll-snap-align:none;pointer-events:none}
:root .d-toggle{min-height:48px;gap:10px;align-items:center}.d-toggle-copy{display:flex;flex-direction:column;min-width:0}.d-toggle:has(>input:last-child)>.d-toggle-copy-visible{flex:1}.d-toggle:has(>.d-toggle-copy-visible:first-child){width:100%}
:root .d-chip.d-item-hittarget .d-chip-surface{border-radius:var(--d-item-radius,var(--d-shape-small,8px))}
:root .d-progress-wavy-linear{width:100%}
:root .d-navigation:not(.d-navigation-links):not([style*="column"]){display:flex;width:100%;min-height:80px;align-items:stretch;justify-content:space-around;gap:0;background:var(--d-surface-container,var(--d-surface));color:var(--d-on-surface)}
:root .d-navigation:not(.d-navigation-links):not([style*="column"])>button,:root .d-navigation:not(.d-navigation-links):not([style*="column"])>a{display:flex;flex:1 1 0;flex-direction:column;align-items:center;justify-content:center;gap:4px;min-width:0;min-height:80px;padding:8px 12px;border:0;border-radius:0;background:transparent;color:var(--d-item-content,var(--d-on-surface));font-size:12px;line-height:16px;text-align:center}
:root .d-navigation:not(.d-navigation-links):not([style*="column"])>button[aria-current=page],:root .d-navigation:not(.d-navigation-links):not([style*="column"])>a[aria-current=page]{border:0;background:transparent;color:var(--d-item-selected-content,var(--d-on-secondary-container,var(--d-primary)))}
:root .d-navigation:not(.d-navigation-links):not([style*="column"])>button>svg:first-child,:root .d-navigation:not(.d-navigation-links):not([style*="column"])>a>svg:first-child{display:block;box-sizing:content-box;width:24px;height:24px;padding:4px 20px;border-radius:999px;transition:background-color 150ms ease}
:root .d-navigation:not(.d-navigation-links):not([style*="column"])>button>span[aria-hidden=true]:first-child,:root .d-navigation:not(.d-navigation-links):not([style*="column"])>a>span[aria-hidden=true]:first-child{display:grid;width:64px;height:32px;place-items:center;border-radius:999px;transition:background-color 150ms ease}
:root .d-navigation:not(.d-navigation-links):not([style*="column"])>button[aria-current=page]>svg:first-child,:root .d-navigation:not(.d-navigation-links):not([style*="column"])>a[aria-current=page]>svg:first-child,:root .d-navigation:not(.d-navigation-links):not([style*="column"])>button[aria-current=page]>span[aria-hidden=true]:first-child,:root .d-navigation:not(.d-navigation-links):not([style*="column"])>a[aria-current=page]>span[aria-hidden=true]:first-child{background:var(--d-item-selected-container,var(--d-secondary-container,var(--d-variant)))}
:root .d-navigation:not(.d-navigation-links):not([style*="column"]).d-navigation-short{min-height:56px}
:root .d-navigation:not(.d-navigation-links):not([style*="column"]).d-navigation-short>button,:root .d-navigation-short:not([style*="column"])>a{min-height:56px}
:root .d-navigation[style*="column"]{min-width:80px;align-items:stretch;gap:4px;padding:12px;background:var(--d-surface-container,var(--d-surface))}
:root .d-navigation[style*="column"]>button,:root .d-navigation[style*="column"]>a{min-height:56px;border-radius:var(--d-item-radius,28px);text-align:start}
@media(forced-colors:active){:root .d-navigation:not(.d-navigation-links):not([style*="column"])>button[aria-current=page],:root .d-navigation:not(.d-navigation-links):not([style*="column"])>a[aria-current=page]{outline:1px solid Highlight}:root .d-navigation:not(.d-navigation-links):not([style*="column"])>button[aria-current=page]>svg:first-child,:root .d-navigation:not(.d-navigation-links):not([style*="column"])>a[aria-current=page]>svg:first-child,:root .d-navigation:not(.d-navigation-links):not([style*="column"])>button[aria-current=page]>span[aria-hidden=true]:first-child,:root .d-navigation:not(.d-navigation-links):not([style*="column"])>a[aria-current=page]>span[aria-hidden=true]:first-child{background:Highlight;color:HighlightText}}
:root .d-navigation.d-navigation-links{width:auto;min-height:0;padding:0;align-items:center;gap:8px;background:transparent}
:root .d-navigation.d-navigation-links>a,:root .d-navigation.d-navigation-links>button{flex:0 0 auto;min-height:44px;padding:12px}
:root .d-navigation:not(.d-navigation-links):not([style*="column"])>button:disabled[aria-current=page],:root .d-navigation:not(.d-navigation-links):not([style*="column"])>a[aria-disabled=true][aria-current=page],:root .d-navigation[style*="column"]>button:disabled[aria-current=page],:root .d-navigation[style*="column"]>a[aria-disabled=true][aria-current=page]{background:var(--d-item-disabled-selected-container,var(--d-item-disabled-container,var(--d-item-container,transparent)));color:var(--d-item-disabled-selected-content,var(--d-item-disabled-content,var(--d-item-content,var(--d-on-surface))));border-color:var(--d-item-disabled-selected-border,var(--d-item-disabled-border,var(--d-item-border,var(--d-outline))))}
:root .d-navigation:not(.d-navigation-links):not([style*="column"])>button:disabled[aria-current=page]>svg:first-child,:root .d-navigation:not(.d-navigation-links):not([style*="column"])>a[aria-disabled=true][aria-current=page]>svg:first-child,:root .d-navigation:not(.d-navigation-links):not([style*="column"])>button:disabled[aria-current=page]>span[aria-hidden=true]:first-child,:root .d-navigation:not(.d-navigation-links):not([style*="column"])>a[aria-disabled=true][aria-current=page]>span[aria-hidden=true]:first-child{background:var(--d-item-disabled-selected-container,var(--d-item-disabled-container,var(--d-item-container,transparent)))}
@media(forced-colors:active){:root .d-toggle input[type=checkbox],:root .d-toggle input[type=radio],:root .d-toggle input[role=switch]{appearance:auto;forced-color-adjust:auto;background-image:none;background-color:ButtonFace;border-color:ButtonText}:root .d-toggle input:checked{accent-color:Highlight}:root .d-toggle input:focus-visible{outline:2px solid Highlight;outline-offset:3px}}
"""
