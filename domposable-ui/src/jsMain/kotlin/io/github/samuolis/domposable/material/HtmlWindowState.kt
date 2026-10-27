package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.FocusEvent
import kotlin.math.abs

private var nextWindowId = 0

/** One owner for focused-item pinning, keyed UI saveables and viewport event cleanup. */
internal class HtmlWindowState(
    val holder: SaveableStateHolder,
) {
    val id = "domposable-window-${nextWindowId++}"
    var offset by mutableDoubleStateOf(0.0)
    var viewport by mutableStateOf<HTMLElement?>(null)
    var focusedKey by mutableStateOf<String?>(null)
    private var focusedElement: HTMLElement? = null
    val retainedKeys = mutableSetOf<String>()
    var keyIndices: Map<String, Int> = emptyMap()

    fun indices(range: IntRange): List<Int> {
        val focused = focusedKey?.let(keyIndices::get)
        return (range.toList() + listOfNotNull(focused)).distinct().sorted()
    }

    fun attach(
        element: HTMLElement,
        horizontal: Boolean,
    ): () -> Unit {
        viewport = element

        fun readOffset() {
            offset = if (horizontal) abs(element.scrollLeft) else element.scrollTop
        }

        fun rowKey(target: Event): String? =
            (target.target as? HTMLElement)?.closest("[data-window-owner='$id']")?.getAttribute("data-window-key")
        val scroll: (Event) -> Unit = { readOffset() }
        val focus: (Event) -> Unit = { event ->
            readOffset()
            focusedElement = event.target as? HTMLElement
            focusedKey = rowKey(event)
        }
        val blur: (Event) -> Unit = { event ->
            if ((event as FocusEvent).relatedTarget != null) {
                focusedKey = null
                focusedElement = null
            }
        }
        val pointer: (Event) -> Unit = { event ->
            if (rowKey(event) != focusedKey) {
                focusedKey = null
                focusedElement = null
            }
        }
        element.addEventListener("scroll", scroll)
        element.addEventListener("focusin", focus)
        element.addEventListener("focusout", blur)
        document.addEventListener("pointerdown", pointer, true)
        return {
            element.removeEventListener("scroll", scroll)
            element.removeEventListener("focusin", focus)
            element.removeEventListener("focusout", blur)
            document.removeEventListener("pointerdown", pointer, true)
            if (viewport == element) viewport = null
        }
    }

    fun restoreFocus(keys: List<String>) {
        val target = focusedElement ?: return
        val lost = document.activeElement == document.body || document.activeElement == null
        if (focusedKey in keys && viewport?.contains(target) == true && lost) {
            target.asDynamic().focus(js("({preventScroll: true})"))
        }
    }
}

@Composable
internal fun rememberHtmlWindowState(keys: List<String>): HtmlWindowState {
    require(keys.distinct().size == keys.size) { "Windowed collections require unique stable keys" }
    val holder = rememberSaveableStateHolder()
    val state = remember(holder) { HtmlWindowState(holder) }
    state.keyIndices = remember(keys) { keys.withIndex().associate { it.value to it.index } }
    DisposableEffect(keys) {
        (state.retainedKeys - keys.toSet()).forEach(holder::removeState)
        state.retainedKeys.clear()
        state.retainedKeys.addAll(keys)
        onDispose { }
    }
    SideEffect { state.restoreFocus(keys) }
    return state
}
