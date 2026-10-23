package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableOffset
import io.github.samuolis.domposable.foundation.DomposableTabsVariant
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTabItem
import io.github.samuolis.domposable.ui.DomposableTabsActivation
import io.github.samuolis.domposable.ui.DomposableTabsProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableAttributes
import io.github.samuolis.domposable.ui.domposableFormEnabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.events.Event

@Composable
internal fun HtmlTabs(
    props: DomposableTabsProps,
    modifier: DomposableModifier,
) {
    val items = props.resolvedItems()
    if (items.isEmpty()) return
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.tabs
    val tabsAppearance = props.tabsAppearance
    val formEnabled = domposableFormEnabled()
    val latestItems = rememberUpdatedState(items)
    val latestSelectedIndex = rememberUpdatedState(props.selectedIndex)
    val latestOnSelect = rememberUpdatedState(props.onSelect)
    var focusedKey by remember { mutableStateOf(items.focusKey(props.selectedIndex)) }
    LaunchedEffect(items.map { it.key }, items.map { it.enabled }, props.selectedIndex) {
        focusedKey = items.focusKey(props.selectedIndex)
    }

    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            attr("role", "tablist")
            attr("aria-label", props.label)
            attr("aria-orientation", "horizontal")
            classes("d-tabs", "d-tabs-${props.variant.name.lowercase()}")
            if (props.leadingIcons) classes("d-tabs-leading-icons")
            if (!props.leadingIcons && items.any { it.icon != null }) classes("d-tabs-stacked-icons")
            tabsAppearance?.indicatorMatchContent?.let { attr("data-indicator-match-content", it.toString()) }
            style { itemAppearance(appearance) }
            style {
                tabsAppearance?.indicatorThickness?.let { property("--d-tabs-indicator-height", "${it.value}px") }
            }
            if (props.scrollable) {
                classes("d-tabs-scrollable")
                style {
                    property("overflow-x", "auto")
                    property("white-space", "nowrap")
                    tabsAppearance?.scrollableEdgePadding?.let { property("padding-inline", "${it.value}px") }
                }
            }
            ref { element ->
                val keyDown: (Event) -> Unit = { rawEvent ->
                    val event = rawEvent.asDynamic()
                    if (formEnabled && hasNoNavigationModifiers(event)) {
                        val currentIndex =
                            (event.target as? HTMLElement)?.getAttribute("data-tab-index")?.toIntOrNull()
                        val rtl =
                            kotlinx.browser.window
                                .getComputedStyle(element)
                                .direction == "rtl"
                        val targetIndex =
                            currentIndex?.let {
                                latestItems.value.nextEnabledTabIndex(it, event.key as String, rtl)
                            }
                        if (targetIndex != null) {
                            val target = element.querySelectorAll("[role='tab']").item(targetIndex) as? HTMLElement
                            if (target != null) {
                                event.preventDefault()
                                target.focus()
                                if (tabsAppearance?.activation != DomposableTabsActivation.Manual) {
                                    latestOnSelect.value(targetIndex)
                                }
                            }
                        }
                    }
                }
                val focusOut: (Event) -> Unit = { rawEvent ->
                    val nextFocus = rawEvent.asDynamic().relatedTarget as? Node
                    if (nextFocus == null || !element.contains(nextFocus)) {
                        focusedKey = latestItems.value.focusKey(latestSelectedIndex.value)
                    }
                }
                element.addEventListener("keydown", keyDown)
                element.addEventListener("focusout", focusOut)
                onDispose {
                    element.removeEventListener("keydown", keyDown)
                    element.removeEventListener("focusout", focusOut)
                }
            }
        }) {
            items.forEachIndexed { index, tab ->
                androidx.compose.runtime.key(tab.key) {
                    val selected = props.selectedIndex == index
                    val textAppearance =
                        (if (selected) tab.selectedTextAppearance else null) ?: tab.textAppearance ?: if (selected) {
                            appearance.selectedTextAppearance ?: appearance.textAppearance
                        } else {
                            appearance.textAppearance
                        }
                    val labelContent = tab.labelContent
                    Button(attrs = {
                        domposableAttributes(tab.modifier)
                        attr("type", "button")
                        classes("d-tab")
                        attr("role", "tab")
                        attr("data-tab-index", index.toString())
                        attr("tabindex", if (tab.enabled && tab.key == focusedKey) "0" else "-1")
                        attr("aria-selected", selected.toString())
                        tab.contentDescription.takeIf(String::isNotBlank)?.let { attr("aria-label", it) }
                        tab.testTag?.let { attr("data-testid", it) }
                        if (selected) attr("data-selected", "true")
                        if (!formEnabled || !tab.enabled) attr("disabled", "")
                        style {
                            itemAppearance(appearance, selected)
                            val minimumHeight = tabsAppearance?.minHeight ?: appearance.minHeight
                            if (!props.leadingIcons && items.any { it.icon != null }) {
                                property("min-height", "max(72px,${minimumHeight?.value ?: 0f}px)")
                            } else {
                                minimumHeight?.let { property("min-height", "${it.value}px") }
                            }
                            if (props.scrollable) {
                                tabsAppearance?.scrollableMinWidth?.let { property("min-width", "${it.value}px") }
                            }
                            tabsAppearance?.contentPadding?.let { property("padding", it.cssPadding()) }
                            if (!formEnabled || !tab.enabled) {
                                property(
                                    "opacity",
                                    (
                                        tabsAppearance?.disabledOpacity ?: appearance.disabledOpacity
                                            ?: .38f
                                    ).toString(),
                                )
                            }
                        }
                        if (tabsAppearance?.indicatorMatchContent == true) {
                            ref { button ->
                                val content = button.querySelector(".d-tab-content") as? HTMLElement

                                @Suppress("UnusedPrivateProperty") // Read by the ResizeObserver constructor.
                                val measure: (dynamic, dynamic) -> Unit = { _, _ ->
                                    content?.let {
                                        button.style.setProperty(
                                            "--d-tab-content-width",
                                            "${it.getBoundingClientRect().width}px",
                                        )
                                    }
                                }
                                val observer = js("new ResizeObserver(measure)")
                                if (content != null) observer.observe(content)
                                measure(null, null)
                                onDispose { observer.disconnect() }
                            }
                        }
                        onFocus { focusedKey = tab.key }
                        onClick { if (formEnabled && tab.enabled) props.onSelect(index) }
                    }) {
                        Div(attrs = {
                            classes("d-tab-content")
                            style {
                                property("position", "relative")
                                property("display", "inline-flex")
                                property("align-items", "center")
                                property("justify-content", "center")
                                property("gap", "8px")
                                if (!props.leadingIcons) property("flex-direction", "column")
                                property("min-width", "0")
                                if (tab.badgeContent != null) {
                                    property("padding-inline-end", "${tabsAppearance?.badgeClearance?.value ?: 16f}px")
                                }
                            }
                        }) {
                            tab.icon?.let { icon ->
                                Span(attrs = {
                                    attr("aria-hidden", "true")
                                    classes("d-tab-icon")
                                    style {
                                        property("display", "inline-flex")
                                        property("flex", "none")
                                    }
                                }) { icon() }
                            }
                            Span(attrs = {
                                classes("d-tab-label")
                                style {
                                    property("min-width", "0")
                                    property("max-width", "100%")
                                    property("overflow", "hidden")
                                    property("overflow-wrap", "anywhere")
                                    property("display", "-webkit-box")
                                    property("-webkit-box-orient", "vertical")
                                    property("-webkit-line-clamp", "2")
                                    property("text-overflow", "ellipsis")
                                    textAppearance?.let { applyCatalogTextAppearance(it) }
                                }
                            }) {
                                if (labelContent != null) {
                                    labelContent()
                                } else {
                                    Text(tab.label)
                                }
                            }
                            tab.badgeContent?.let { badge ->
                                Span(attrs = {
                                    classes("d-tab-badge")
                                    attr("aria-hidden", "true")
                                    style {
                                        property("position", "absolute")
                                        property("inset-inline-end", "0")
                                        property("inset-block-start", "0")
                                        property("z-index", "1")
                                        property("display", "inline-flex")
                                        property("align-items", "center")
                                        property("justify-content", "center")
                                        val offset = tabsAppearance?.badgeOffset ?: DomposableOffset(8f, -4f)
                                        property("transform", "translate(${offset.x}px,${offset.y}px)")
                                        property("pointer-events", "none")
                                    }
                                }) { badge() }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun hasNoNavigationModifiers(event: dynamic): Boolean =
    !(event.altKey as Boolean) &&
        !(event.ctrlKey as Boolean) &&
        !(event.metaKey as Boolean) &&
        !(event.shiftKey as Boolean)

private fun DomposableTabsProps.resolvedItems(): List<DomposableTabItem> =
    items ?: labels.mapIndexed { index, label ->
        DomposableTabItem(key = index.toString(), label = label, icon = icons[index])
    }

private fun List<DomposableTabItem>.focusKey(selectedIndex: Int): String =
    getOrNull(selectedIndex)?.takeIf { it.enabled }?.key ?: firstOrNull { it.enabled }?.key.orEmpty()

private fun List<DomposableTabItem>.nextEnabledTabIndex(
    currentIndex: Int,
    key: String,
    rtl: Boolean,
): Int? {
    val enabledIndices = indices.filter { this[it].enabled }
    val currentEnabledIndex = enabledIndices.indexOf(currentIndex)
    val direction = if ((key == "ArrowRight") xor rtl) 1 else -1
    return when {
        currentEnabledIndex < 0 -> {
            null
        }

        key == "ArrowLeft" || key == "ArrowRight" -> {
            enabledIndices[(currentEnabledIndex + direction + enabledIndices.size) % enabledIndices.size]
        }

        key == "Home" -> {
            enabledIndices.firstOrNull()
        }

        key == "End" -> {
            enabledIndices.lastOrNull()
        }

        else -> {
            null
        }
    }
}
