package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableLabelProps
import io.github.samuolis.domposable.ui.DomposableMenuGroupProps
import io.github.samuolis.domposable.ui.DomposableMenuItem
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableScrollFieldProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableAttributes
import io.github.samuolis.domposable.ui.domposableFormEnabled
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

internal val LocalHtmlMenuDismiss = staticCompositionLocalOf<(() -> Unit)?> { null }
private var nextExtensionId = 0

@Composable
internal fun HtmlLabel(
    props: DomposableLabelProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.label
    DomposableHtmlModifiers(modifier) {
        TagElement<HTMLElement>("div", applyAttrs = {
            domposableAttributes(modifier)
            classes("d-component-label")
            if (props.persistent) classes("d-component-label-persistent")
            style {
                property("position", "relative")
                surfaceAppearance(appearance)
            }
        }) {
            Span(attrs = { classes("d-component-label-text") }) { props.label() }
            Div(attrs = { classes("d-component-label-content") }) { content() }
        }
    }
}

@Composable
internal fun HtmlMenuGroup(
    props: DomposableMenuGroupProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.menuGroup
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            attr("role", "group")
            classes("d-menu-group")
            style {
                property("display", "flex")
                property("flex-direction", "column")
                surfaceAppearance(appearance)
            }
        }) { content() }
    }
}

@Composable
internal fun HtmlScrollField(
    props: DomposableScrollFieldProps,
    modifier: DomposableModifier,
) {
    val fieldId = remember { "d-scroll-field-${nextExtensionId++}" }
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.scrollField
    val enabled = props.enabled && domposableFormEnabled()

    fun select(index: Int) {
        if (enabled && index in 0 until props.itemCount &&
            index != props.selectedIndex
        ) {
            props.onSelectedIndexChange(index)
        }
    }
    val currentProps by rememberUpdatedState(props)
    val currentEnabled by rememberUpdatedState(enabled)
    var reportedIndex by remember { mutableStateOf(props.selectedIndex) }
    LaunchedEffect(props.selectedIndex) {
        reportedIndex = props.selectedIndex
        scrollWheelItemIntoView(fieldId, props.selectedIndex)
    }
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-scroll-field")
            attr("role", "spinbutton")
            attr("aria-label", props.label)
            attr("aria-valuemin", "0")
            attr("aria-valuemax", (props.itemCount - 1).toString())
            attr("aria-valuenow", props.selectedIndex.toString())
            attr("aria-valuetext", props.indexDescription(props.selectedIndex))
            attr("aria-disabled", (!enabled).toString())
            style { itemAppearance(appearance) }
            attr("tabindex", if (enabled) "0" else "-1")
            onKeyDown { event ->
                if (enabled) {
                    val target =
                        when (event.key) {
                            "ArrowUp", "ArrowRight" -> props.selectedIndex + 1
                            "ArrowDown", "ArrowLeft" -> props.selectedIndex - 1
                            "PageUp" -> props.selectedIndex + 5
                            "PageDown" -> props.selectedIndex - 5
                            "Home" -> 0
                            "End" -> props.itemCount - 1
                            else -> null
                        }
                    if (target != null) {
                        event.preventDefault()
                        val next = target.coerceIn(0, props.itemCount - 1)
                        select(next)
                        reportedIndex = next
                        scrollWheelItemIntoView(fieldId, next)
                    }
                }
            }
        }) {
            Div(attrs = {
                id("$fieldId-viewport")
                classes("d-scroll-field-viewport")
                attr("aria-hidden", "true")
                ref { viewport ->
                    val onScroll: (org.w3c.dom.events.Event) -> Unit = {
                        if (currentEnabled && viewport.children.length > 0 && !isProgrammaticWheelScroll(viewport)) {
                            val center = viewport.getBoundingClientRect().top + viewport.clientHeight / 2.0
                            var nearest = currentProps.selectedIndex
                            var distance = Double.POSITIVE_INFINITY
                            for (index in 0 until viewport.children.length) {
                                val child = viewport.children.item(index) as? HTMLElement ?: continue
                                val rect = child.getBoundingClientRect()
                                val delta = kotlin.math.abs((rect.top + rect.bottom) / 2.0 - center)
                                if (delta < distance) {
                                    distance = delta
                                    nearest = index
                                }
                            }
                            if (nearest != reportedIndex) {
                                reportedIndex = nearest
                                currentProps.onSelectedIndexChange(nearest)
                            }
                        }
                    }
                    viewport.addEventListener("scroll", onScroll)
                    onDispose { viewport.removeEventListener("scroll", onScroll) }
                }
            }) {
                repeat(props.itemCount) { index ->
                    val selected = index == props.selectedIndex
                    Div(attrs = {
                        id("$fieldId-value-$index")
                        classes("d-scroll-field-option")
                        attr("data-scroll-index", index.toString())
                        if (selected) classes("d-scroll-field-option-selected")
                        style { itemAppearance(appearance, selected) }
                        onClick {
                            if (enabled) {
                                select(index)
                                reportedIndex = index
                            }
                        }
                    }) {
                        props.itemContent?.invoke(index, selected, enabled)
                            ?: Text(props.indexDescription(index))
                    }
                }
            }
        }
    }
}

@Composable
internal fun HtmlDropdownMenuItem(
    props: DomposableMenuItem,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.dropdownMenuItem
    val enabled = props.enabled && domposableFormEnabled()
    val dismiss = LocalHtmlMenuDismiss.current
    val role =
        when {
            props.checked != null -> "menuitemcheckbox"
            props.selected != null -> "menuitemradio"
            else -> "menuitem"
        }
    DomposableHtmlModifiers(modifier) {
        Button(attrs = {
            domposableAttributes(modifier)
            attr("type", "button")
            attr("role", role)
            props.checked?.let { attr("aria-checked", it.toString()) }
            props.selected?.let { attr("aria-checked", it.toString()) }
            if (!enabled) attr("disabled", "")
            classes("d-dropdown-menu-item")
            style { itemAppearance(appearance, props.checked == true || props.selected == true) }
            onClick {
                if (enabled) {
                    props.onClick()
                    dismiss?.invoke()
                }
            }
        }) {
            val isSelected = props.checked == true || props.selected == true
            val icon = if (isSelected) props.selectedIcon ?: props.leadingIcon else props.leadingIcon
            icon?.let {
                Div(attrs = {
                    attr("aria-hidden", "true")
                    classes("d-dropdown-menu-leading")
                }) { it() }
            }
            Div(attrs = { classes("d-dropdown-menu-labels") }) {
                Span { props.textContent?.invoke() ?: Text(props.label) }
                props.supportingText?.let { Span(attrs = { classes("d-dropdown-menu-supporting") }) { it() } }
            }
            props.trailingContent?.let { Div(attrs = { classes("d-dropdown-menu-trailing") }) { it() } }
        }
    }
}
