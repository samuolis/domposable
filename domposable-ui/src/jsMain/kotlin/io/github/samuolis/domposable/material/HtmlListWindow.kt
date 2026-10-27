package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableLazyColumnProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.domposableAttributes
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Ul
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.FocusEvent

@Composable
internal fun HtmlListWindow(
    props: DomposableLazyColumnProps,
    modifier: DomposableModifier,
) {
    val window = requireNotNull(props.window)
    val keys = remember(props.items) { props.items.map { it.key }.also { require(it.distinct().size == it.size) } }
    val keyIndices = remember(keys) { keys.withIndex().associate { it.value to it.index } }
    var offset by remember { mutableStateOf(0.0) }
    var focusedKey by remember { mutableStateOf<String?>(null) }
    var focusedElement by remember { mutableStateOf<HTMLElement?>(null) }
    var viewportElement by remember { mutableStateOf<HTMLElement?>(null) }
    val holder = rememberSaveableStateHolder()
    val retainedKeys = remember { mutableSetOf<String>() }
    DisposableEffect(keys) {
        (retainedKeys - keys.toSet()).forEach { holder.removeState(it) }
        retainedKeys.clear()
        retainedKeys.addAll(keys)
        onDispose { }
    }
    val range = listWindowRange(props.items.size, window, props.gap, offset)
    val indices = (range.toList() + listOfNotNull(focusedKey?.let(keyIndices::get))).distinct().sorted()
    val extent = listExtent(props.items.size, window, props.gap)
    val step = window.rowHeight.value.toDouble() + props.gap.value
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            attr("data-domposable-window", "column")
            style {
                property("height", "${window.viewportHeight.value}px")
                property("overflow-y", "auto")
                property("overflow-anchor", "none")
            }
            ref { element ->
                viewportElement = element
                val scroll: (org.w3c.dom.events.Event) -> Unit = { offset = element.scrollTop }
                val focus: (org.w3c.dom.events.Event) -> Unit = { event ->
                    // Focus can scroll synchronously before the deferred scroll event.
                    offset = element.scrollTop
                    focusedElement = event.target as? HTMLElement
                    focusedKey =
                        (event.target as? HTMLElement)?.closest("[data-window-key]")?.getAttribute("data-window-key")
                }
                val blur: (org.w3c.dom.events.Event) -> Unit = { event ->
                    // Moving a keyed DOM node can transiently blur it with no next target.
                    // Explicit focus movement is respected; reorders restore after apply.
                    if ((event as FocusEvent).relatedTarget != null) {
                        focusedKey = null
                        focusedElement = null
                    }
                }
                val pointer: (org.w3c.dom.events.Event) -> Unit = { event ->
                    val clickedKey =
                        (event.target as? HTMLElement)
                            ?.closest(
                                "[data-window-key]",
                            )?.getAttribute("data-window-key")
                    if (clickedKey != focusedKey) {
                        focusedKey = null
                        focusedElement = null
                    }
                }
                element.addEventListener("scroll", scroll)
                element.addEventListener("focusin", focus)
                element.addEventListener("focusout", blur)
                document.addEventListener("pointerdown", pointer, true)
                onDispose {
                    element.removeEventListener("scroll", scroll)
                    element.removeEventListener("focusin", focus)
                    element.removeEventListener("focusout", blur)
                    document.removeEventListener("pointerdown", pointer, true)
                    viewportElement = null
                }
            }
        }) {
            Ul(attrs = {
                classes("d-collection")
                attr("data-window-total", props.items.size.toString())
                style {
                    property("position", "relative")
                    property("height", "${extent}px")
                }
            }) {
                indices.forEach { index ->
                    val item = props.items[index]
                    key(item.key) {
                        Li(attrs = {
                            attr("data-window-key", item.key)
                            attr("aria-posinset", (index + 1).toString())
                            attr("aria-setsize", props.items.size.toString())
                            style {
                                property("position", "absolute")
                                property("top", "${index * step}px")
                                property("height", "${window.rowHeight.value}px")
                                property("width", "100%")
                                property("overflow", "hidden")
                            }
                        }) {
                            holder.SaveableStateProvider(item.key) { item.content() }
                        }
                    }
                }
            }
        }
    }
    SideEffect {
        val target = focusedElement
        val stillMounted = target != null && viewportElement?.contains(target) == true
        val focusLost = document.activeElement == document.body || document.activeElement == null
        if (focusedKey in keyIndices && stillMounted && focusLost) {
            target.asDynamic().focus(js("({preventScroll: true})"))
        }
    }
    // A focused item remains mounted even when a pointer scroll moves it outside the window.
    // Removed data keys are evicted from the saveable holder on the next committed composition.
}
