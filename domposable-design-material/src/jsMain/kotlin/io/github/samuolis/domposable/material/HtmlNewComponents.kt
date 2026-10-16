package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.ui.DomposableAppBarProps
import io.github.samuolis.domposable.ui.DomposableAutocompleteProps
import io.github.samuolis.domposable.ui.DomposableBottomAppBarProps
import io.github.samuolis.domposable.ui.DomposableBottomSheetScaffoldProps
import io.github.samuolis.domposable.ui.DomposableButtonGroupProps
import io.github.samuolis.domposable.ui.DomposableCarouselProps
import io.github.samuolis.domposable.ui.DomposableDatePickerMode
import io.github.samuolis.domposable.ui.DomposableDatePickerProps
import io.github.samuolis.domposable.ui.DomposableDateRangePickerProps
import io.github.samuolis.domposable.ui.DomposableDragHandleProps
import io.github.samuolis.domposable.ui.DomposableDrawerVariant
import io.github.samuolis.domposable.ui.DomposableFloatingActionButtonMenuProps
import io.github.samuolis.domposable.ui.DomposableFloatingToolbarProps
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableListItemProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableModifierOperation
import io.github.samuolis.domposable.ui.DomposableMultiChoiceSegmentedButtonsProps
import io.github.samuolis.domposable.ui.DomposableNavigationDrawerItemProps
import io.github.samuolis.domposable.ui.DomposableNavigationDrawerProps
import io.github.samuolis.domposable.ui.DomposablePullToRefreshProps
import io.github.samuolis.domposable.ui.DomposableSearchBarProps
import io.github.samuolis.domposable.ui.DomposableSearchBarVariant
import io.github.samuolis.domposable.ui.DomposableSplitButtonProps
import io.github.samuolis.domposable.ui.DomposableSwipeDirection
import io.github.samuolis.domposable.ui.DomposableSwipeToDismissProps
import io.github.samuolis.domposable.ui.DomposableTimePickerMode
import io.github.samuolis.domposable.ui.DomposableTimePickerProps
import io.github.samuolis.domposable.ui.DomposableToggleButtonProps
import io.github.samuolis.domposable.ui.DomposableWideNavigationRailItemProps
import io.github.samuolis.domposable.ui.DomposableWideNavigationRailProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.background
import io.github.samuolis.domposable.ui.domposableAttributes
import io.github.samuolis.domposable.ui.domposableFormEnabled
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.minHeight
import io.github.samuolis.domposable.ui.offset
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.width
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

private var nextCatalogId = 0

@Composable
internal fun HtmlToggleButton(
    props: DomposableToggleButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.toggleButton
        val enabled = props.enabled && domposableFormEnabled()
        Button(attrs = {
            domposableAttributes(modifier)
            attr("type", "button")
            attr("aria-pressed", props.checked.toString())
            if (!enabled) disabled()
            classes("d-button", "d-button-toggle", "d-button-${props.style.name.lowercase()}")
            style { styleAppearance(appearance, enabled) }
            onClick { if (enabled) props.onCheckedChange(!props.checked) }
        }) { content() }
    }
}

@Composable
internal fun HtmlButtonGroup(
    props: DomposableButtonGroupProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.buttonGroup
        val formEnabled = domposableFormEnabled()
        Div(attrs = {
            domposableAttributes(modifier)
            attr("role", "group")
            attr("aria-label", props.label)
            classes("d-button-group")
            style { property("gap", "${props.spacing.value}px") }
        }) {
            props.items.forEach { item ->
                Button(attrs = {
                    attr("type", "button")
                    if (!item.enabled || !formEnabled) disabled()
                    classes("d-button")
                    style { styleAppearance(appearance, item.enabled) }
                    onClick { if (item.enabled && formEnabled) item.onClick() }
                }) {
                    item.icon?.let { Span(attrs = { attr("aria-hidden", "true") }) { it() } }
                    Text(item.label)
                }
            }
        }
    }
}

@Composable
internal fun HtmlAppBar(
    props: DomposableAppBarProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        val id = remember { "d-app-bar-overflow-${nextCatalogId++}" }
        var expanded by remember { mutableStateOf(false) }
        val formEnabled = domposableFormEnabled()
        var visibleCount by remember(props.items.size) { mutableStateOf(props.items.size) }
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.appBar
        Div(attrs = {
            domposableAttributes(modifier)
            attr("role", "toolbar")
            attr("aria-label", props.label)
            classes("d-action-app-bar")
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
                property("gap", "${props.spacing.value}px")
            }
            ref { element ->
                val measure: () -> Unit = {
                    val rootWidth =
                        if (props.orientation ==
                            DomposableOrientation.Horizontal
                        ) {
                            element.clientWidth.toDouble()
                        } else {
                            element.clientHeight.toDouble()
                        }
                    val actions = element.querySelectorAll("[data-appbar-action]")
                    val overflow = element.querySelector("[data-appbar-overflow]")
                    if (rootWidth > 0.0 && actions.length > 0) {
                        val gap = props.spacing.value.toDouble()
                        var used = 0.0
                        var count = 0
                        val widths =
                            (0 until actions.length).map { index ->
                                val action = actions.item(index) as HTMLElement
                                if (props.orientation ==
                                    DomposableOrientation.Horizontal
                                ) {
                                    action.getBoundingClientRect().width
                                } else {
                                    action.getBoundingClientRect().height
                                }
                            }
                        val overflowSize =
                            (overflow as? HTMLElement)?.let {
                                if (props.orientation ==
                                    DomposableOrientation.Horizontal
                                ) {
                                    it.getBoundingClientRect().width
                                } else {
                                    it.getBoundingClientRect().height
                                }
                            } ?: 0.0
                        for (width in widths) {
                            val reserveOverflow = if (count < widths.lastIndex) overflowSize + gap else 0.0
                            if (used + (if (count > 0) gap else 0.0) + width + reserveOverflow > rootWidth) break
                            used += (if (count > 0) gap else 0.0) + width
                            count++
                        }
                        val nextCount = if (count == widths.size) widths.size else count.coerceAtLeast(0)
                        if (visibleCount != nextCount) visibleCount = nextCount
                    }
                }
                measure()
                val observer = js("typeof ResizeObserver !== 'undefined' ? new ResizeObserver(measure) : null")
                if (observer != null) observer.observe(element)
                val resize = { _: org.w3c.dom.events.Event -> measure() }
                if (observer == null) window.addEventListener("resize", resize)
                onDispose {
                    observer?.disconnect()
                    if (observer == null) window.removeEventListener("resize", resize)
                }
            }
        }) {
            props.items.forEachIndexed { index, item ->
                Button(attrs = {
                    attr("type", "button")
                    attr("aria-label", item.label)
                    attr("data-appbar-action", "")
                    if (index >=
                        visibleCount
                    ) {
                        style {
                            property("position", "absolute")
                            property("visibility", "hidden")
                            property("pointer-events", "none")
                        }
                    }
                    if (!item.enabled || !formEnabled) disabled()
                    style { styleAppearance(appearance, item.enabled) }
                    onClick { if (item.enabled && formEnabled) item.onClick() }
                }) { item.icon?.invoke() ?: Text(item.label) }
            }
            Button(attrs = {
                attr("type", "button")
                attr("data-appbar-overflow", "")
                attr("aria-label", props.overflowLabel)
                attr("aria-expanded", expanded.toString())
                attr("aria-controls", id)
                if (props.items.size <= visibleCount) {
                    attr("aria-hidden", "true")
                    attr("tabindex", "-1")
                    style {
                        property("position", "absolute")
                        property("visibility", "hidden")
                        property("pointer-events", "none")
                    }
                }
                if (!formEnabled) disabled()
                style { styleAppearance(appearance) }
                onClick { if (formEnabled) expanded = !expanded }
            }) { Text(props.overflowLabel) }
            if (props.items.size > visibleCount) {
                if (expanded) {
                    Div(attrs = {
                        id(id)
                        attr("role", "group")
                        attr("aria-label", props.overflowLabel)
                        classes("d-app-bar-overflow")
                    }) {
                        props.items.drop(visibleCount).forEach { item ->
                            Button(attrs = {
                                attr("type", "button")
                                if (!item.enabled ||
                                    !formEnabled
                                ) {
                                    disabled()
                                }
                                onClick {
                                    if (item.enabled &&
                                        formEnabled
                                    ) {
                                        item.onClick()
                                        expanded = false
                                    }
                                }
                            }) {
                                item.icon?.invoke()
                                Text(item.label)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HtmlMultiChoiceSegmentedButtons(
    props: DomposableMultiChoiceSegmentedButtonsProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.multiChoiceSegmentedButtons
        val formEnabled = domposableFormEnabled()
        Div(attrs = {
            domposableAttributes(modifier)
            attr("role", "group")
            attr("aria-label", props.label)
            classes("d-multi-segments")
            style { itemAppearance(appearance) }
        }) {
            props.labels.forEachIndexed { index, label ->
                val selected = index in props.selectedIndices
                Button(attrs = {
                    attr("type", "button")
                    attr("aria-pressed", selected.toString())
                    if (!props.enabled || !formEnabled || !props.optionEnabled[index]) disabled()
                    classes("d-multi-segment")
                    style { itemAppearance(appearance, selected) }
                    onClick {
                        if (props.enabled && formEnabled && props.optionEnabled[index]) {
                            props.onSelectionChange(
                                if (selected) {
                                    props.selectedIndices - index
                                } else {
                                    props.selectedIndices +
                                        index
                                },
                            )
                        }
                    }
                }) { Text(label) }
            }
        }
    }
}

@Composable
internal fun HtmlSplitButton(
    props: DomposableSplitButtonProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.splitButton
        val enabled = props.enabled && domposableFormEnabled()
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-split-button")
            attr("role", "group")
            style { property("display", "inline-flex") }
        }) {
            Button(attrs = {
                attr("type", "button")
                if (!enabled) disabled()
                classes("d-button", "d-button-${props.style.name.lowercase()}")
                style { styleAppearance(appearance, props.enabled) }
                onClick { if (enabled) props.onClick() }
            }) { Text(props.label) }
            Button(attrs = {
                attr("type", "button")
                attr("aria-label", props.secondaryLabel)
                attr("aria-expanded", props.expanded.toString())
                if (!enabled) disabled()
                classes("d-button", "d-split-button-trailing", "d-button-${props.style.name.lowercase()}")
                style { styleAppearance(appearance, props.enabled) }
                onClick { if (enabled) props.onSecondaryClick() }
            }) { Text("▾") }
        }
    }
}

@Composable
internal fun HtmlFloatingToolbar(
    props: DomposableFloatingToolbarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.floatingToolbar
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-floating-toolbar")
            attr("data-expanded", props.expanded.toString())
            style {
                surfaceAppearance(appearance)
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
            props.leadingContent()
            content()
            props.trailingContent()
            props.floatingAction()
        }
    }
}

@Composable
internal fun HtmlFloatingActionButtonMenu(
    props: DomposableFloatingActionButtonMenuProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.floatingActionButtonMenu
        val formEnabled = domposableFormEnabled()
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-fab-menu")
        }) {
            if (props.expanded) {
                Div(attrs = {
                    classes("d-fab-menu-items")
                    attr("role", "group")
                    attr("aria-label", props.label)
                    style { surfaceAppearance(appearance) }
                }) {
                    props.items.forEach { item ->
                        Button(attrs = {
                            attr("type", "button")
                            if (!item.enabled || !formEnabled) disabled()
                            onClick {
                                if (item.enabled &&
                                    formEnabled
                                ) {
                                    item.onClick()
                                    props.onExpandedChange(false)
                                }
                            }
                        }) {
                            item.icon?.invoke()
                            Text(item.label)
                        }
                    }
                }
            }
            Button(attrs = {
                attr("type", "button")
                attr("aria-label", props.label)
                attr("aria-expanded", props.expanded.toString())
                if (!formEnabled) disabled()
                onClick { if (formEnabled) props.onExpandedChange(!props.expanded) }
            }) { Text(if (props.expanded) "×" else "+") }
        }
    }
}

@Composable
internal fun HtmlDragHandle(
    props: DomposableDragHandleProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        var startY by remember { mutableStateOf<Double?>(null) }
        var lastY by remember { mutableStateOf<Double?>(null) }
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.dragHandle
        val formEnabled = domposableFormEnabled()
        Div(attrs = {
            domposableAttributes(modifier)
            attr("role", "button")
            attr("aria-label", props.label)
            attr("aria-roledescription", "Resize handle")
            attr("aria-keyshortcuts", "ArrowUp ArrowDown Enter Space")
            attr("tabindex", if (props.enabled && formEnabled) "0" else "-1")
            if (!props.enabled || !formEnabled) attr("aria-disabled", "true")
            classes("d-drag-handle")
            style { surfaceAppearance(appearance) }
            ref { element ->
                val pointerDown: (org.w3c.dom.events.Event) -> Unit = { event ->
                    val dynamicEvent = event.asDynamic()
                    val y = (dynamicEvent.clientY as? Number)?.toDouble()
                    if (props.enabled && formEnabled && y != null) {
                        startY = y
                        lastY = y
                        element.asDynamic().setPointerCapture(dynamicEvent.pointerId)
                    }
                }
                val pointerMove: (org.w3c.dom.events.Event) -> Unit = { event ->
                    val y = (event.asDynamic().clientY as? Number)?.toDouble()
                    val canDrag = props.enabled && formEnabled
                    if (canDrag && startY != null && y != null) {
                        val previous = lastY ?: y
                        if (y != previous) props.onDrag((previous - y).toFloat())
                        lastY = y
                    }
                }
                val pointerEnd: (org.w3c.dom.events.Event) -> Unit = {
                    startY = null
                    lastY = null
                }
                element.addEventListener("pointerdown", pointerDown)
                element.addEventListener("pointermove", pointerMove)
                element.addEventListener("pointerup", pointerEnd)
                element.addEventListener("pointercancel", pointerEnd)
                onDispose {
                    element.removeEventListener("pointerdown", pointerDown)
                    element.removeEventListener("pointermove", pointerMove)
                    element.removeEventListener("pointerup", pointerEnd)
                    element.removeEventListener("pointercancel", pointerEnd)
                }
            }
            onClick { event ->
                if (props.enabled && formEnabled &&
                    (event.nativeEvent as? org.w3c.dom.events.MouseEvent)?.detail == 0
                ) {
                    props.onDrag(1f)
                }
            }
            onKeyDown { event ->
                val resizeKey = event.key in listOf("ArrowUp", "ArrowDown", "Enter", " ")
                if (props.enabled && formEnabled && resizeKey) {
                    event.preventDefault()
                    props.onDrag(if (event.key == "ArrowDown") -1f else 1f)
                }
            }
        }) {
            Span(attrs = {
                attr("aria-hidden", "true")
                classes("d-drag-handle-grip")
            }) {}
        }
    }
}

@Composable
internal fun HtmlListItem(
    props: DomposableListItemProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.listItem
        val enabled = props.enabled && domposableFormEnabled()
        val content: @Composable () -> Unit = {
            props.leadingContent()
            Div(attrs = { classes("d-list-item-copy") }) {
                props.overline?.let { Span(attrs = { classes("d-list-item-overline") }) { Text(it) } }
                Span(attrs = { classes("d-list-item-headline") }) { Text(props.headline) }
                props.supportingText?.let { Span(attrs = { classes("d-list-item-supporting") }) { Text(it) } }
            }
            props.trailingContent()
        }
        val enabledAction = props.onClick
        if (enabledAction != null) {
            Button(attrs = {
                domposableAttributes(modifier)
                attr("type", "button")
                if (!enabled) disabled()
                if (props.selected) attr("aria-current", "true")
                classes(
                    "d-list-item",
                    "d-list-item-${props.variant.name.lowercase()}",
                    "d-list-item-${props.position.name.lowercase()}",
                )
                style { itemAppearance(appearance, props.selected) }
                onClick { if (enabled) enabledAction() }
            }) { content() }
        } else {
            Div(attrs = {
                domposableAttributes(modifier)
                if (props.selected) attr("aria-current", "true")
                classes(
                    "d-list-item",
                    "d-list-item-${props.variant.name.lowercase()}",
                    "d-list-item-${props.position.name.lowercase()}",
                )
                style { itemAppearance(appearance, props.selected) }
            }) { content() }
        }
    }
}

@Composable
internal fun HtmlBottomAppBar(
    props: DomposableBottomAppBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.bottomAppBar
        Div(attrs = {
            domposableAttributes(modifier)
            attr("role", "toolbar")
            classes("d-bottom-app-bar", "d-bottom-app-bar-${props.variant.name.lowercase()}")
            style { surfaceAppearance(appearance) }
        }) {
            content()
            props.floatingAction()
        }
    }
}

@Composable
internal fun HtmlNavigationDrawer(
    props: DomposableNavigationDrawerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    if (!props.expanded && props.variant != DomposableDrawerVariant.Permanent) return
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.navigationDrawer
        val modal = props.variant == DomposableDrawerVariant.Modal
        val formEnabled = domposableFormEnabled()
        Div(attrs = {
            domposableAttributes(modifier)
            if (modal) {
                classes(
                    "d-drawer-scrim",
                )
            } else {
                classes("d-navigation-drawer-host", "d-navigation-drawer-${props.variant.name.lowercase()}")
            }
            if (modal) attr("aria-modal", "true")
            if (props.gesturesEnabled) onClick { if (modal && formEnabled) props.onDismiss() }
            if (modal && props.gesturesEnabled) onKeyDown { if (it.key == "Escape" && formEnabled) props.onDismiss() }
        }) {
            TagElement<HTMLElement>(if (modal) "section" else "nav", applyAttrs = {
                attr("aria-label", props.label)
                if (modal) attr("role", "dialog") else attr("role", "navigation")
                classes("d-navigation-drawer")
                style { surfaceAppearance(appearance) }
                if (modal) onClick { it.stopPropagation() }
            }) { content() }
        }
    }
}

@Composable
internal fun HtmlNavigationDrawerItem(
    props: DomposableNavigationDrawerItemProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.navigationDrawerItem
        val enabled = props.enabled && domposableFormEnabled()
        Button(attrs = {
            domposableAttributes(modifier)
            attr("type", "button")
            attr("aria-current", if (props.selected) "page" else "false")
            if (!enabled) disabled()
            classes("d-navigation-drawer-item")
            style { itemAppearance(appearance, props.selected) }
            onClick { if (enabled) props.onClick() }
        }) {
            props.icon()
            Span { Text(props.label) }
            props.badge()
        }
    }
}

@Composable
internal fun HtmlWideNavigationRail(
    props: DomposableWideNavigationRailProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    if (props.modal && !props.expanded) return
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.wideNavigationRail
        val formEnabled = domposableFormEnabled()
        if (props.modal) {
            Div(attrs = {
                classes("d-drawer-scrim")
                onClick { if (formEnabled) props.onDismiss() }
            }) {
                TagElement<HTMLElement>("nav", applyAttrs = {
                    domposableAttributes(modifier)
                    attr("aria-label", "Primary navigation")
                    attr("role", "dialog")
                    style { surfaceAppearance(appearance) }
                    onClick { it.stopPropagation() }
                }) {
                    props.header()
                    content()
                }
            }
        } else {
            TagElement<HTMLElement>("nav", applyAttrs = {
                domposableAttributes(modifier)
                attr("aria-label", "Primary navigation")
                classes("d-wide-navigation-rail")
                if (props.expanded) classes("d-wide-navigation-rail-expanded")
                style { surfaceAppearance(appearance) }
            }) {
                props.header()
                content()
            }
        }
    }
}

@Composable
internal fun HtmlWideNavigationRailItem(
    props: DomposableWideNavigationRailItemProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.navigationDrawerItem
        val enabled = props.enabled && domposableFormEnabled()
        val railItemClass =
            if (props.railExpanded) {
                "d-wide-navigation-rail-item-expanded"
            } else {
                "d-wide-navigation-rail-item-compact"
            }
        Button(attrs = {
            domposableAttributes(modifier)
            attr("type", "button")
            attr("aria-current", if (props.selected) "page" else "false")
            if (!props.railExpanded) attr("aria-label", props.label)
            if (!enabled) disabled()
            classes("d-wide-navigation-rail-item", railItemClass)
            style { itemAppearance(appearance, props.selected) }
            onClick { if (enabled) props.onClick() }
        }) {
            props.icon()
            if (props.railExpanded) Span { Text(props.label) }
        }
    }
}

@Composable
internal fun HtmlBottomSheetScaffold(
    props: DomposableBottomSheetScaffoldProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.bottomSheetScaffold
        val formEnabled = domposableFormEnabled()
        val swipeEnabled by rememberUpdatedState(props.swipeEnabled && formEnabled)
        val expanded by rememberUpdatedState(props.expanded)
        val onExpandedChange by rememberUpdatedState(props.onExpandedChange)
        var dragStartY by remember { mutableStateOf<Double?>(null) }
        var suppressNextClick by remember { mutableStateOf(false) }
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-bottom-sheet-scaffold")
        }) {
            Div(attrs = { classes("d-bottom-sheet-content") }) { content() }
            Button(attrs = {
                attr("type", "button")
                attr("aria-expanded", props.expanded.toString())
                classes("d-bottom-sheet-handle")
                if (!formEnabled) disabled()
                if (props.swipeEnabled && formEnabled) style { property("touch-action", "none") }
                ref { element ->
                    val pointerDown: (org.w3c.dom.events.Event) -> Unit = { event ->
                        val dynamicEvent = event.asDynamic()
                        val y = (dynamicEvent.clientY as? Number)?.toDouble()
                        if (formEnabled && y != null) {
                            dragStartY = y
                            element.asDynamic().setPointerCapture(dynamicEvent.pointerId)
                        }
                    }
                    val pointerMove: (org.w3c.dom.events.Event) -> Unit = { event ->
                        if (swipeEnabled && dragStartY != null) event.preventDefault()
                    }
                    val pointerEnd: (org.w3c.dom.events.Event) -> Unit = { event ->
                        val y = (event.asDynamic().clientY as? Number)?.toDouble()
                        val start = dragStartY
                        if (y != null && start != null && kotlin.math.abs(y - start) >= 36) {
                            val nextExpanded = if (y < start) true else false
                            if (swipeEnabled && nextExpanded != expanded) onExpandedChange(nextExpanded)
                            suppressNextClick = true
                            window.setTimeout({ suppressNextClick = false }, 0)
                        }
                        dragStartY = null
                    }
                    val pointerCancel: (org.w3c.dom.events.Event) -> Unit = { dragStartY = null }
                    element.addEventListener("pointerdown", pointerDown)
                    element.addEventListener("pointermove", pointerMove)
                    element.addEventListener("pointerup", pointerEnd)
                    element.addEventListener("pointercancel", pointerCancel)
                    onDispose {
                        element.removeEventListener("pointerdown", pointerDown)
                        element.removeEventListener("pointermove", pointerMove)
                        element.removeEventListener("pointerup", pointerEnd)
                        element.removeEventListener("pointercancel", pointerCancel)
                    }
                }
                onClick {
                    if (suppressNextClick) {
                        suppressNextClick = false
                    } else if (formEnabled) {
                        props.onExpandedChange(!props.expanded)
                    }
                }
            }) { Text(if (props.expanded) "Collapse" else "Expand") }
            if (props.expanded) {
                Div(attrs = {
                    attr("role", "region")
                    attr("aria-label", "Bottom sheet")
                    classes("d-bottom-sheet")
                    style {
                        surfaceAppearance(appearance)
                        property("min-height", "${props.peekHeight.value}px")
                    }
                }) { props.sheetContent() }
            }
        }
    }
}

@Composable
internal fun HtmlSearchBar(
    props: DomposableSearchBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    DomposableHtmlModifiers(modifier) {
        val id = remember { "d-search-${nextCatalogId++}" }
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.searchBar
        val enabled = props.enabled && domposableFormEnabled()
        val fullScreen =
            props.variant == DomposableSearchBarVariant.FullScreen ||
                props.variant == DomposableSearchBarVariant.ContainedFullScreen
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-search-bar", "d-search-${props.variant.name.toKebabCase()}")
            if (fullScreen && props.expanded) attr("role", "search")
        }) {
            Div(attrs = {
                classes("d-search-control")
                style { textFieldAppearance(appearance) }
            }) {
                props.navigationIcon?.invoke()
                props.leadingIcon()
                Input(InputType.Search, attrs = {
                    id(id)
                    attr("aria-label", props.label)
                    attr("role", "combobox")
                    attr("aria-haspopup", "dialog")
                    attr("aria-expanded", props.expanded.toString())
                    attr("aria-controls", "$id-results")
                    props.placeholder?.let { attr("placeholder", it) }
                    value(props.query)
                    if (!enabled) disabled()
                    onFocus { if (enabled) props.onExpandedChange(true) }
                    onInput { if (enabled) props.onQueryChange(it.value) }
                    onKeyDown { event ->
                        if (event.key == "Enter" && enabled) {
                            event.preventDefault()
                            props.onSearch(props.query)
                            props.onExpandedChange(false)
                        } else if (event.key == "Escape" && enabled && props.expanded) {
                            props.onExpandedChange(false)
                        }
                    }
                })
                Button(attrs = {
                    attr("type", "button")
                    attr("aria-label", "Search")
                    if (!enabled) disabled()
                    onClick {
                        if (enabled) {
                            props.onSearch(props.query)
                            props.onExpandedChange(false)
                        }
                    }
                }) { Text("⌕") }
                props.trailingIcon()
                props.actions?.invoke()
            }
            if (props.expanded) {
                Div(attrs = {
                    id("$id-results")
                    attr("role", "dialog")
                    attr("aria-label", "Search results")
                    classes("d-search-results")
                }) { content() }
            }
        }
    }
}

@Composable
internal fun HtmlAutocomplete(
    props: DomposableAutocompleteProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        val id = remember { "d-autocomplete-${nextCatalogId++}" }
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.autocomplete
        val enabled = props.enabled && domposableFormEnabled()
        val matches = props.options.filter { it.label.contains(props.value, ignoreCase = true) }
        var activeIndex by remember(props.value, matches.size) { mutableStateOf(0) }
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-autocomplete")
        }) {
            Label(forId = id) { Text(props.label) }
            Div(attrs = {
                classes("d-autocomplete-control")
                style { textFieldAppearance(appearance) }
            }) {
                Input(InputType.Text, attrs = {
                    id(id)
                    attr("role", "combobox")
                    attr("aria-autocomplete", "list")
                    attr("aria-expanded", (props.expanded && matches.isNotEmpty()).toString())
                    attr("aria-controls", "$id-options")
                    if (props.expanded &&
                        matches.isNotEmpty()
                    ) {
                        attr("aria-activedescendant", "$id-option-${activeIndex.coerceIn(0, matches.lastIndex)}")
                    }
                    attr("aria-label", props.label)
                    value(props.value)
                    if (!enabled) disabled()
                    if (props.readOnly) attr("readonly", "")
                    onInput {
                        if (enabled &&
                            !props.readOnly
                        ) {
                            activeIndex = 0
                            props.onValueChange(it.value)
                            props.onExpandedChange(true)
                        }
                    }
                    onFocus { if (enabled) props.onExpandedChange(true) }
                    onKeyDown { event ->
                        if (enabled) {
                            when (event.key) {
                                "Escape" -> {
                                    props.onExpandedChange(false)
                                }

                                "ArrowDown" -> {
                                    if (matches.isNotEmpty()) {
                                        event.preventDefault()
                                        props.onExpandedChange(true)
                                        activeIndex =
                                            (activeIndex + 1).coerceAtMost(matches.lastIndex)
                                    }
                                }

                                "ArrowUp" -> {
                                    if (matches.isNotEmpty()) {
                                        event.preventDefault()
                                        props.onExpandedChange(true)
                                        activeIndex =
                                            (activeIndex - 1).coerceAtLeast(0)
                                    }
                                }

                                "Enter" -> {
                                    val option = matches.getOrNull(activeIndex)
                                    if (props.expanded && option?.enabled == true) {
                                        event.preventDefault()
                                        props.onOptionSelected(option)
                                        props.onExpandedChange(false)
                                    }
                                }
                            }
                        }
                    }
                })
            }
            if (props.expanded && matches.isNotEmpty()) {
                Div(attrs = {
                    id("$id-options")
                    attr("role", "listbox")
                    classes("d-autocomplete-options")
                }) {
                    matches.forEachIndexed { index, option ->
                        Button(attrs = {
                            id("$id-option-$index")
                            attr("type", "button")
                            attr("role", "option")
                            attr("aria-selected", (option.value == props.value).toString())
                            if (!option.enabled || !enabled) disabled()
                            onClick {
                                if (enabled && option.enabled) {
                                    props.onOptionSelected(option)
                                    props.onExpandedChange(false)
                                }
                            }
                        }) { Text(option.label) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HtmlDatePicker(
    props: DomposableDatePickerProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.datePicker
        var localMode by remember(props.mode) { mutableStateOf(props.mode) }
        val mode = if (props.onModeChange == null) localMode else props.mode
        val modeChange: (DomposableDatePickerMode) -> Unit = { next ->
            val change = props.onModeChange
            if (change == null) localMode = next else change(next)
        }
        val formEnabled = domposableFormEnabled()
        var visibleMonth by remember(props.selectedDateMillis) {
            mutableStateOf(
                monthStart(props.selectedDateMillis ?: currentDayMillis()),
            )
        }
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-date-picker")
            attr("role", "group")
            attr("aria-label", "Date picker")
            style { surfaceAppearance(appearance) }
        }) {
            if (props.showModeToggle) {
                Button(attrs = {
                    attr("type", "button")
                    if (!formEnabled) disabled()
                    onClick {
                        if (formEnabled) {
                            modeChange.invoke(
                                if (mode ==
                                    DomposableDatePickerMode.Calendar
                                ) {
                                    DomposableDatePickerMode.Input
                                } else {
                                    DomposableDatePickerMode.Calendar
                                },
                            )
                        }
                    }
                }) {
                    Text(if (mode == DomposableDatePickerMode.Calendar) "Enter date" else "Choose date")
                }
            }
            if (mode == DomposableDatePickerMode.Input) {
                val id = remember { "d-date-${nextCatalogId++}" }
                Input(InputType.Date, attrs = {
                    id(id)
                    attr("aria-label", "Date")
                    if (!formEnabled) disabled()
                    attr("min", "${props.yearRange.first.toString().padStart(4, '0')}-01-01")
                    attr("max", "${props.yearRange.last.toString().padStart(4, '0')}-12-31")
                    value(props.selectedDateMillis?.let(::epochToDate).orEmpty())
                    onChange { event ->
                        val raw = event.value?.takeIf(String::isNotBlank)?.let(::dateToEpoch)
                        if (event.value.isNullOrBlank() && formEnabled) {
                            props.onDateChange(null)
                        } else if (formEnabled) {
                            raw
                                ?.takeIf { utcYear(it) in props.yearRange && props.isDateSelectable(it) }
                                ?.let(props.onDateChange)
                        }
                    }
                })
            } else {
                HtmlCalendar(
                    month = visibleMonth,
                    selected = props.selectedDateMillis,
                    yearRange = props.yearRange,
                    selectable = props.isDateSelectable,
                    onMonthChange = { visibleMonth = it },
                    onSelect = { if (formEnabled) props.onDateChange(it) },
                )
            }
        }
    }
}

@Composable
internal fun HtmlDateRangePicker(
    props: DomposableDateRangePickerProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.dateRangePicker
        var localMode by remember(props.mode) { mutableStateOf(props.mode) }
        val mode = if (props.onModeChange == null) localMode else props.mode
        val modeChange: (DomposableDatePickerMode) -> Unit = { next ->
            val change = props.onModeChange
            if (change == null) localMode = next else change(next)
        }
        val formEnabled = domposableFormEnabled()
        val startId = remember { "d-date-start-${nextCatalogId++}" }
        val endId = remember { "d-date-end-${nextCatalogId++}" }
        var visibleMonth by remember(props.startDateMillis) {
            mutableStateOf(
                monthStart(props.startDateMillis ?: currentDayMillis()),
            )
        }
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-date-range-picker")
            attr("role", "group")
            attr("aria-label", "Date range")
            style { surfaceAppearance(appearance) }
        }) {
            if (props.showModeToggle) {
                Button(attrs = {
                    attr("type", "button")
                    if (!formEnabled) disabled()
                    onClick {
                        if (formEnabled) {
                            modeChange.invoke(
                                if (mode ==
                                    DomposableDatePickerMode.Calendar
                                ) {
                                    DomposableDatePickerMode.Input
                                } else {
                                    DomposableDatePickerMode.Calendar
                                },
                            )
                        }
                    }
                }) {
                    Text(
                        if (mode ==
                            DomposableDatePickerMode.Calendar
                        ) {
                            "Enter dates"
                        } else {
                            "Choose dates"
                        },
                    )
                }
            }
            if (mode == DomposableDatePickerMode.Input) {
                Label(forId = startId) { Text("Start date") }
                Input(InputType.Date, attrs = {
                    id(startId)
                    attr("aria-label", "Start date")
                    if (!formEnabled) disabled()
                    attr("min", "${props.yearRange.first.toString().padStart(4, '0')}-01-01")
                    attr(
                        "max",
                        props.endDateMillis?.let(::epochToDate)
                            ?: "${props.yearRange.last.toString().padStart(4, '0')}-12-31",
                    )
                    value(props.startDateMillis?.let(::epochToDate).orEmpty())
                    onChange { event ->
                        val raw = event.value?.takeIf(String::isNotBlank)?.let(::dateToEpoch)
                        if (event.value.isNullOrBlank() && formEnabled) {
                            props.onDateRangeChange(null, null)
                        } else if (formEnabled) {
                            raw
                                ?.takeIf { utcYear(it) in props.yearRange && props.isDateSelectable(it) }
                                ?.let { start ->
                                    props.onDateRangeChange(start, props.endDateMillis?.takeIf { it >= start })
                                }
                        }
                    }
                })
                Label(forId = endId) { Text("End date") }
                Input(InputType.Date, attrs = {
                    id(endId)
                    attr("aria-label", "End date")
                    if (!formEnabled || props.startDateMillis == null) disabled()
                    attr(
                        "min",
                        props.startDateMillis?.let(::epochToDate)
                            ?: "${props.yearRange.first.toString().padStart(4, '0')}-01-01",
                    )
                    attr("max", "${props.yearRange.last.toString().padStart(4, '0')}-12-31")
                    value(props.endDateMillis?.let(::epochToDate).orEmpty())
                    onChange { event ->
                        val raw = event.value?.takeIf(String::isNotBlank)?.let(::dateToEpoch)
                        val currentStart = props.startDateMillis
                        if (event.value.isNullOrBlank() && formEnabled && currentStart != null) {
                            props.onDateRangeChange(currentStart, null)
                        } else if (formEnabled && currentStart != null) {
                            raw
                                ?.takeIf {
                                    it >= currentStart && utcYear(it) in props.yearRange && props.isDateSelectable(it)
                                }?.let { props.onDateRangeChange(currentStart, it) }
                        }
                    }
                })
            } else {
                Div(attrs = { classes("d-date-range-fields") }) {
                    Span { Text("Start date: ${props.startDateMillis?.let(::dateLabel) ?: "Choose a date"}") }
                    Span { Text("End date: ${props.endDateMillis?.let(::dateLabel) ?: "Choose a date"}") }
                }
                HtmlCalendar(
                    month = visibleMonth,
                    selected = null,
                    yearRange = props.yearRange,
                    selectable = props.isDateSelectable,
                    rangeStart = props.startDateMillis,
                    rangeEnd = props.endDateMillis,
                    onMonthChange = { visibleMonth = it },
                    onSelect = { day ->
                        if (formEnabled) {
                            when {
                                props.startDateMillis == null || props.endDateMillis != null ||
                                    day < (props.startDateMillis ?: Long.MAX_VALUE) -> {
                                    props.onDateRangeChange(
                                        day,
                                        null,
                                    )
                                }

                                else -> {
                                    props.onDateRangeChange(props.startDateMillis, day)
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
internal fun HtmlTimePicker(
    props: DomposableTimePickerProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.timePicker
        val id = remember { "d-time-${nextCatalogId++}" }
        val formEnabled = domposableFormEnabled()
        var selectingHour by remember { mutableStateOf(true) }
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-time-picker")
            attr("role", "group")
            attr("aria-label", "Time picker")
            style { surfaceAppearance(appearance) }
        }) {
            if (props.mode == DomposableTimePickerMode.Input) {
                Label(forId = id) { Text("Time") }
                Input(InputType.Time, attrs = {
                    id(id)
                    attr("aria-label", "Time")
                    attr("step", "60")
                    if (!formEnabled) disabled()
                    value("${props.hour.toString().padStart(2, '0')}:${props.minute.toString().padStart(2, '0')}")
                    onChange { event ->
                        val pieces = event.value.split(":")
                        if (pieces.size >= 2) {
                            val hour = pieces[0].toIntOrNull()
                            val minute = pieces[1].toIntOrNull()
                            if (hour != null && minute != null && formEnabled) {
                                if (hour in 0..23 && minute in 0..59) {
                                    props.onTimeChange(hour, minute)
                                }
                            }
                        }
                    }
                })
            } else {
                Span(attrs = {
                    classes("d-time-value")
                    attr("aria-live", "polite")
                }) {
                    Text(
                        if (props.is24Hour) {
                            "${props.hour.toString().padStart(
                                2,
                                '0',
                            )}:${props.minute.toString().padStart(
                                2,
                                '0',
                            )}"
                        } else {
                            "${((props.hour + 11) % 12) + 1}:${props.minute.toString().padStart(
                                2,
                                '0',
                            )} ${if (props.hour < 12) "AM" else "PM"}"
                        },
                    )
                }
                Div(attrs = { classes("d-time-dial") }) {
                    Div(attrs = {
                        classes("d-time-dial-controls")
                        attr("role", "group")
                        attr("aria-label", "Select ${if (selectingHour) "hour" else "minute"}")
                    }) {
                        val values = if (selectingHour) (if (props.is24Hour) 0..23 else 1..12) else (0..55 step 5)
                        values.forEachIndexed { index, value ->
                            val selected =
                                if (selectingHour) {
                                    (
                                        if (props.is24Hour) {
                                            value == props.hour
                                        } else {
                                            value ==
                                                ((props.hour + 11) % 12) + 1
                                        }
                                    )
                                } else {
                                    value == props.minute
                                }
                            val angle = if (values.count() == 0) 0 else index * 360.0 / values.count()
                            val radius = if (selectingHour && props.is24Hour && value >= 12) 68 else 108
                            Button(attrs = {
                                attr("type", "button")
                                attr(
                                    "aria-label",
                                    if (selectingHour) {
                                        "Hour ${value.toString().padStart(
                                            2,
                                            '0',
                                        )}"
                                    } else {
                                        "Minute ${value.toString().padStart(2, '0')}"
                                    },
                                )
                                if (!formEnabled) disabled()
                                attr("aria-pressed", selected.toString())
                                classes("d-time-dial-value")
                                if (selected) attr("aria-current", "true")
                                style {
                                    property("--d-time-dial-angle", "${angle}deg")
                                    property("--d-time-dial-radius", "${radius}px")
                                }
                                onClick {
                                    if (formEnabled) {
                                        if (selectingHour) {
                                            val hour =
                                                if (props.is24Hour) {
                                                    value
                                                } else {
                                                    (value % 12) +
                                                        if (props.hour >= 12) 12 else 0
                                                }
                                            props.onTimeChange(hour, props.minute)
                                            selectingHour = false
                                        } else {
                                            props.onTimeChange(props.hour, value)
                                        }
                                    }
                                }
                                onKeyDown { event ->
                                    if (formEnabled) {
                                        val step = if (selectingHour) 1 else 5
                                        val direction =
                                            when (event.key) {
                                                "ArrowUp", "ArrowRight" -> -1
                                                "ArrowDown", "ArrowLeft" -> 1
                                                else -> 0
                                            }
                                        if (direction != 0) {
                                            event.preventDefault()
                                            if (selectingHour) {
                                                props.onTimeChange((props.hour + direction + 24) % 24, props.minute)
                                            } else {
                                                props.onTimeChange(
                                                    props.hour,
                                                    (props.minute + direction * step + 60) % 60,
                                                )
                                            }
                                        }
                                    }
                                }
                            }) { Text(value.toString().padStart(2, '0')) }
                        }
                    }
                    Button(attrs = {
                        attr("type", "button")
                        if (!formEnabled) disabled()
                        attr("aria-label", if (selectingHour) "Select minutes" else "Select hours")
                        onClick { if (formEnabled) selectingHour = !selectingHour }
                    }) { Text(if (selectingHour) "Minutes" else "Hours") }
                    if (!props.is24Hour) {
                        Button(attrs = {
                            attr("type", "button")
                            if (!formEnabled) disabled()
                            attr("aria-label", if (props.hour < 12) "Switch to PM" else "Switch to AM")
                            onClick { if (formEnabled) props.onTimeChange((props.hour + 12) % 24, props.minute) }
                        }) { Text(if (props.hour < 12) "AM" else "PM") }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HtmlCarousel(
    props: DomposableCarouselProps,
    modifier: DomposableModifier,
) {
    DomposableHtmlModifiers(modifier) {
        val id = remember { "d-carousel-${nextCatalogId++}" }
        val formEnabled = domposableFormEnabled()
        var position by remember {
            mutableStateOf(
                props.currentItem.coerceIn(0, props.items.lastIndex.coerceAtLeast(0)),
            )
        }
        val currentPosition by rememberUpdatedState(position)
        val onCurrentItemChange by rememberUpdatedState(props.onCurrentItemChange)
        LaunchedEffect(props.currentItem, props.items.size) {
            position = props.currentItem.coerceIn(0, props.items.lastIndex.coerceAtLeast(0))
            scrollCarouselItemIntoView(id, position)
        }
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-carousel", "d-carousel-${props.variant.name.lowercase()}")
            attr("role", "region")
            attr("aria-roledescription", "carousel")
            attr("aria-label", props.label)
            style {
                property("--d-carousel-gap", "${props.spacing.value}px")
                property("--d-carousel-item-width", "${props.preferredItemWidth.value}px")
                property("--d-carousel-min-width", "${props.minSmallItemWidth.value}px")
                property("--d-carousel-max-width", "${props.maxSmallItemWidth.value}px")
                property("--d-carousel-preferred-width", "${props.preferredItemWidth.value}px")
            }
        }) {
            Div(attrs = {
                id(id)
                classes("d-carousel-track")
                attr("tabindex", "0")
                ref { element ->
                    val onScroll: (org.w3c.dom.events.Event) -> Unit = {
                        val track = element.getBoundingClientRect()
                        val selected = (element.children.item(currentPosition) as? HTMLElement)?.getBoundingClientRect()
                        val selectedVisible =
                            selected != null && selected.left >= track.left - 1 && selected.right <= track.right + 1
                        val rtl = window.getComputedStyle(element).direction == "rtl"
                        var closest = currentPosition
                        var distance = Double.POSITIVE_INFINITY
                        for (index in 0 until element.children.length) {
                            val child = element.children.item(index) as? HTMLElement ?: continue
                            val rect = child.getBoundingClientRect()
                            val delta = kotlin.math.abs(if (rtl) rect.right - track.right else rect.left - track.left)
                            if (delta < distance) {
                                distance = delta
                                closest = index
                            }
                        }
                        if (!selectedVisible && closest != currentPosition) {
                            position = closest
                            onCurrentItemChange?.invoke(closest)
                        }
                    }
                    element.addEventListener("scroll", onScroll)
                    onDispose { element.removeEventListener("scroll", onScroll) }
                }
                onKeyDown { event ->
                    if (formEnabled) {
                        val rtl = window.getComputedStyle(event.currentTarget as HTMLElement).direction == "rtl"
                        val delta =
                            when (event.key) {
                                "ArrowRight" -> if (rtl) -1 else 1
                                "ArrowLeft" -> if (rtl) 1 else -1
                                else -> 0
                            }
                        if (delta != 0) {
                            event.preventDefault()
                            position = (position + delta).coerceIn(0, props.items.lastIndex.coerceAtLeast(0))
                            props.onCurrentItemChange?.invoke(position)
                            scrollCarouselItemIntoView(id, position)
                        }
                    }
                }
            }) {
                props.items.forEachIndexed { index, item ->
                    key(item.key) {
                        Div(attrs = {
                            classes("d-carousel-item")
                            attr("role", "group")
                            attr("aria-roledescription", "slide")
                            attr("aria-label", "${index + 1} of ${props.items.size}")
                            style {
                                // shortcut: Fixed-width HTML slides; add keyline masking if web needs small-item bounds.
                                property(
                                    "flex-basis",
                                    "min(100%,var(--d-carousel-preferred-width))",
                                )
                                property("max-width", "var(--d-carousel-preferred-width)")
                            }
                        }) { item.content() }
                    }
                }
            }
            Div(attrs = { classes("d-carousel-controls") }) {
                Button(attrs = {
                    attr("type", "button")
                    attr("aria-label", "Previous item")
                    if (position == 0 ||
                        !formEnabled
                    ) {
                        disabled()
                    }
                    onClick {
                        if (formEnabled &&
                            position > 0
                        ) {
                            position--
                            props.onCurrentItemChange?.invoke(position)
                            scrollCarouselItemIntoView(id, position)
                        }
                    }
                }) { Text("‹") }
                Span(attrs = {
                    attr("aria-live", "polite")
                }) { Text(if (props.items.isEmpty()) "0 of 0" else "${position + 1} of ${props.items.size}") }
                Button(attrs = {
                    attr("type", "button")
                    attr("aria-label", "Next item")
                    if (position >=
                        props.items.lastIndex ||
                        !formEnabled
                    ) {
                        disabled()
                    }
                    onClick {
                        if (formEnabled &&
                            position < props.items.lastIndex
                        ) {
                            position++
                            props.onCurrentItemChange?.invoke(position)
                            scrollCarouselItemIntoView(id, position)
                        }
                    }
                }) { Text("›") }
            }
        }
    }
}

@Composable
internal fun HtmlPullToRefresh(
    props: DomposablePullToRefreshProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    DomposableHtmlModifiers(modifier) {
        var originY by remember { mutableStateOf<Double?>(null) }
        var distance by remember { mutableStateOf(0f) }
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.pullToRefresh
        val enabled = props.enabled && domposableFormEnabled()
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-pull-to-refresh")
            style { property("--d-pull-distance", "${distance.coerceAtMost(props.threshold.value)}px") }
            ref { element ->
                val pointerDown: (org.w3c.dom.events.Event) -> Unit = { event ->
                    val y = (event.asDynamic().clientY as? Number)?.toDouble()
                    if (enabled) originY = y
                }
                val pointerMove: (org.w3c.dom.events.Event) -> Unit = { event ->
                    val y = (event.asDynamic().clientY as? Number)?.toDouble()
                    val start = originY
                    if (enabled && start != null) {
                        if (y != null && y > start) {
                            distance = (y - start).toFloat()
                            event.preventDefault()
                        }
                    }
                }
                val pointerEnd: (org.w3c.dom.events.Event) -> Unit = {
                    if (enabled && distance >= props.threshold.value && !props.refreshing) props.onRefresh()
                    originY = null
                    distance = 0f
                }
                element.addEventListener("pointerdown", pointerDown)
                element.addEventListener("pointermove", pointerMove)
                element.addEventListener("pointerup", pointerEnd)
                element.addEventListener("pointercancel", pointerEnd)
                onDispose {
                    element.removeEventListener("pointerdown", pointerDown)
                    element.removeEventListener("pointermove", pointerMove)
                    element.removeEventListener("pointerup", pointerEnd)
                    element.removeEventListener("pointercancel", pointerEnd)
                }
            }
        }) {
            Div(attrs = {
                classes("d-pull-refresh-indicator")
                attr("role", "status")
                attr("aria-label", props.label)
                if (props.refreshing) attr("aria-busy", "true")
                style {
                    appearance.color?.let { property("color", it.css()) }
                    property("transform", "translateY(var(--d-pull-distance,0px))")
                }
            }) { if (props.refreshing) Text("◌ ${props.label}") else Text(props.label) }
            content()
        }
    }
}

@Composable
internal fun HtmlSwipeToDismiss(
    props: DomposableSwipeToDismissProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    DomposableHtmlModifiers(modifier) {
        var downX by remember { mutableStateOf<Double?>(null) }
        var offset by remember { mutableStateOf(0.0) }
        var direction by remember { mutableStateOf<DomposableSwipeDirection?>(null) }
        val enabled = props.enabled && domposableFormEnabled()
        val startToEndShortcut = "ArrowRight: ${props.startActionLabel}"
        val endToStartShortcut = "ArrowLeft: ${props.endActionLabel}"
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-swipe-dismiss")
            attr("role", "group")
            attr("tabindex", "0")
            attr("aria-label", "Swipe to dismiss. $startToEndShortcut. $endToStartShortcut.")
            attr("aria-keyshortcuts", "ArrowRight ArrowLeft")
            attr("data-direction", direction?.name?.lowercase() ?: "")
            style { property("--d-swipe-offset", "${offset}px") }
            ref { element ->
                val pointerDown: (org.w3c.dom.events.Event) -> Unit = { event ->
                    val x = (event.asDynamic().clientX as? Number)?.toDouble()
                    if (enabled) {
                        downX = x
                        direction = null
                    }
                }
                val pointerMove: (org.w3c.dom.events.Event) -> Unit = { event ->
                    val x = (event.asDynamic().clientX as? Number)?.toDouble()
                    val start = downX
                    if (enabled && start != null && x != null) {
                        val raw = x - start
                        val swipeDirection =
                            if (raw <
                                0
                            ) {
                                DomposableSwipeDirection.EndToStart
                            } else {
                                DomposableSwipeDirection.StartToEnd
                            }
                        val allowed =
                            if (swipeDirection ==
                                DomposableSwipeDirection.StartToEnd
                            ) {
                                props.allowStartToEnd
                            } else {
                                props.allowEndToStart
                            }
                        if (allowed) {
                            offset = raw
                            direction = swipeDirection
                        }
                    }
                }
                val pointerEnd: (org.w3c.dom.events.Event) -> Unit = {
                    direction?.let { if (enabled && kotlin.math.abs(offset) >= 96) props.onDismiss(it) }
                    downX = null
                    offset = 0.0
                    direction = null
                }
                element.addEventListener("pointerdown", pointerDown)
                element.addEventListener("pointermove", pointerMove)
                element.addEventListener("pointerup", pointerEnd)
                element.addEventListener("pointercancel", pointerEnd)
                onDispose {
                    element.removeEventListener("pointerdown", pointerDown)
                    element.removeEventListener("pointermove", pointerMove)
                    element.removeEventListener("pointerup", pointerEnd)
                    element.removeEventListener("pointercancel", pointerEnd)
                }
            }
            onKeyDown { event ->
                if (enabled && event.target == event.currentTarget) {
                    val swipeDirection =
                        when (event.key) {
                            "ArrowRight" -> DomposableSwipeDirection.StartToEnd
                            "ArrowLeft" -> DomposableSwipeDirection.EndToStart
                            else -> null
                        }
                    val allowed =
                        when (swipeDirection) {
                            DomposableSwipeDirection.StartToEnd -> props.allowStartToEnd
                            DomposableSwipeDirection.EndToStart -> props.allowEndToStart
                            null -> false
                        }
                    if (swipeDirection != null && allowed) {
                        event.preventDefault()
                        direction = swipeDirection
                        props.onDismiss(swipeDirection)
                    }
                }
            }
        }) {
            Span(attrs = {
                attr("aria-live", "polite")
                attr("aria-atomic", "true")
                classes("d-visually-hidden")
            }) {
                direction?.let {
                    val actionLabel =
                        if (it == DomposableSwipeDirection.StartToEnd) props.startActionLabel else props.endActionLabel
                    Text(actionLabel)
                }
            }
            Div(attrs = {
                classes("d-swipe-background")
                attr("aria-hidden", "true")
            }) { props.background(direction) }
            Div(attrs = { classes("d-swipe-content") }) { content() }
        }
    }
}

@Composable
private fun HtmlCalendar(
    month: Long,
    selected: Long?,
    yearRange: IntRange,
    selectable: (Long) -> Boolean,
    rangeStart: Long? = null,
    rangeEnd: Long? = null,
    onMonthChange: (Long) -> Unit,
    onSelect: (Long) -> Unit,
) {
    val calendarId = remember { "d-calendar-${nextCatalogId++}" }
    val formEnabled = domposableFormEnabled()
    var focusedDay by remember(month, selected, rangeStart) {
        mutableStateOf(selected ?: rangeStart ?: month)
    }
    val first = monthStart(month)
    val offset = (utcWeekday(first) + 6) % 7
    val dayCount = daysInMonth(month)
    val totalCells = ((offset + dayCount + 6) / 7) * 7
    val firstDay = first - offset * 86_400_000L
    val gridLabel = monthLabel(month)

    fun moveFocus(
        target: Long,
        origin: Long,
    ) {
        val direction = if (target < origin) -1 else 1
        var candidate = target
        var attempts = 0
        while (utcYear(candidate) in yearRange && !selectable(candidate) && attempts < 370) {
            candidate = shiftDay(candidate, direction)
            attempts++
        }
        if (utcYear(candidate) in yearRange && selectable(candidate)) {
            focusedDay = candidate
            val targetMonth = monthStart(candidate)
            if (targetMonth != month) onMonthChange(targetMonth)
            window.setTimeout({ document.getElementById("$calendarId-day-$candidate")?.asDynamic()?.focus() }, 0)
        }
    }

    Div(attrs = { classes("d-calendar-header") }) {
        Button(attrs = {
            attr("type", "button")
            attr("aria-label", "Previous month")
            if (!formEnabled || utcYear(monthStart(shiftMonth(month, -1))) !in yearRange) disabled()
            onClick {
                val next = shiftMonth(month, -1)
                if (formEnabled &&
                    utcYear(next) in yearRange
                ) {
                    onMonthChange(next)
                }
            }
        }) { Text("‹") }
        Span(attrs = { attr("aria-live", "polite") }) { Text(gridLabel) }
        Button(attrs = {
            attr("type", "button")
            attr("aria-label", "Next month")
            if (!formEnabled || utcYear(monthStart(shiftMonth(month, 1))) !in yearRange) disabled()
            onClick {
                val next = shiftMonth(month, 1)
                if (formEnabled &&
                    utcYear(next) in yearRange
                ) {
                    onMonthChange(next)
                }
            }
        }) { Text("›") }
    }
    Div(attrs = {
        classes("d-calendar-grid")
        attr("role", "grid")
        attr("aria-label", gridLabel)
    }) {
        Div(attrs = {
            attr("role", "row")
            classes("d-calendar-week")
        }) {
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday").forEach { day ->
                Span(attrs = {
                    attr("role", "columnheader")
                    attr("aria-label", day)
                }) { Text(day.take(3)) }
            }
        }
        repeat(totalCells / 7) { row ->
            Div(attrs = {
                attr("role", "row")
                classes("d-calendar-week")
            }) {
                repeat(7) { column ->
                    val day = firstDay + (row * 7 + column) * 86_400_000L
                    if (day < first || day >= first + dayCount * 86_400_000L) {
                        Span(attrs = {
                            attr("role", "gridcell")
                            attr("aria-hidden", "true")
                        }) {}
                    } else {
                        val inYear = utcYear(day) in yearRange
                        val allowed = inYear && selectable(day)
                        val rangeSelected =
                            (rangeStart != null && day == rangeStart) || (rangeEnd != null && day == rangeEnd)
                        val inSelectedRange = rangeStart != null && rangeEnd != null && day in rangeStart..rangeEnd
                        val dayId = "$calendarId-day-$day"
                        TagElement<HTMLElement>("div", applyAttrs = {
                            attr("role", "gridcell")
                            if (day == selected || rangeSelected) attr("aria-selected", "true")
                            if (inSelectedRange) classes("d-calendar-day-in-range")
                            if (rangeStart == day) classes("d-calendar-day-range-start")
                            if (rangeEnd == day) classes("d-calendar-day-range-end")
                        }) {
                            Button(attrs = {
                                id(dayId)
                                attr("type", "button")
                                attr("aria-label", dateLabel(day))
                                if (day == selected || rangeSelected) attr("aria-pressed", "true")
                                attr(
                                    "tabindex",
                                    if (day == focusedDay ||
                                        (
                                            focusedDay !in (first until first + dayCount * 86_400_000L) &&
                                                day == (selected ?: first)
                                        )
                                    ) {
                                        "0"
                                    } else {
                                        "-1"
                                    },
                                )
                                if (!allowed || !formEnabled) disabled()
                                classes("d-calendar-day")
                                onClick {
                                    if (allowed && formEnabled) {
                                        focusedDay = day
                                        onSelect(day)
                                    }
                                }
                                onKeyDown { event ->
                                    if (formEnabled) {
                                        val delta =
                                            when (event.key) {
                                                "ArrowLeft" -> {
                                                    -1
                                                }

                                                "ArrowRight" -> {
                                                    1
                                                }

                                                "ArrowUp" -> {
                                                    -7
                                                }

                                                "ArrowDown" -> {
                                                    7
                                                }

                                                "Home" -> {
                                                    -(
                                                        (
                                                            utcWeekday(day) +
                                                                6
                                                        ) %
                                                            7
                                                    )
                                                }

                                                "End" -> {
                                                    6 - ((utcWeekday(day) + 6) % 7)
                                                }

                                                else -> {
                                                    0
                                                }
                                            }
                                        val target =
                                            when (event.key) {
                                                "PageUp" -> shiftMonthDay(day, if (event.shiftKey) -12 else -1)
                                                "PageDown" -> shiftMonthDay(day, if (event.shiftKey) 12 else 1)
                                                else -> if (delta != 0) shiftDay(day, delta) else null
                                            }
                                        if (target != null &&
                                            event.key in
                                            setOf(
                                                "ArrowLeft",
                                                "ArrowRight",
                                                "ArrowUp",
                                                "ArrowDown",
                                                "Home",
                                                "End",
                                                "PageUp",
                                                "PageDown",
                                            )
                                        ) {
                                            event.preventDefault()
                                            moveFocus(target, day)
                                        }
                                    }
                                }
                            }) { Text((day - first).div(86_400_000L).plus(1).toString()) }
                        }
                    }
                }
            }
        }
    }
}
