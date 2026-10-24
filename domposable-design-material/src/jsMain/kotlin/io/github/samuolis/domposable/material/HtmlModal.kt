package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.ui.DomposableButtonProps
import io.github.samuolis.domposable.ui.DomposableDialogVariant
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableHtmlWidthDemandContainer
import io.github.samuolis.domposable.ui.DomposableModalAlignment
import io.github.samuolis.domposable.ui.DomposableModalLayout
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.domposableAttributes
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDialogElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.events.Event

private const val HTML_MODAL_SCRIM_Z_INDEX = 10_000
private const val HTML_MODAL_SURFACE_Z_INDEX = 10_001
private const val HTML_MODAL_STACK_Z_INDEX_STEP = 2
private const val HTML_MODAL_SELECTOR =
    "button:not([disabled]),input:not([disabled]),select:not([disabled])," +
        "textarea:not([disabled]),a[href],[tabindex='0']"
private var nextHtmlModalId = 0
private val openHtmlModalOrder = mutableListOf<HTMLElement>()

@Composable
internal fun HtmlModal(
    title: String,
    onDismiss: () -> Unit,
    modifier: DomposableModifier,
    sheet: Boolean,
    appearance: io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance,
    variant: DomposableDialogVariant = DomposableDialogVariant.Basic,
    headerContent: (@Composable () -> Unit)? = null,
    layout: DomposableModalLayout = DomposableModalLayout(),
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    skipPartiallyExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dismiss by rememberUpdatedState(onDismiss)
    val allowBackDismiss by rememberUpdatedState(dismissOnBackPress)
    val allowOutsideDismiss by rememberUpdatedState(dismissOnClickOutside)
    val titleId = remember { "d-dialog-${nextHtmlModalId++}" }
    DomposableHtmlModifiers(modifier) {
        TagElement<HTMLDialogElement>("dialog", applyAttrs = {
            domposableAttributes(modifier)
            if (headerContent == null) attr("aria-labelledby", titleId) else attr("aria-label", title)
            classes("d-dialog", "d-dialog-${variant.name.toKebabCase()}")
            if (sheet) classes("d-sheet")
            if (layout.alignment == DomposableModalAlignment.BottomOnCompact) classes("d-dialog-bottom-on-compact")
            if (layout.alignment == DomposableModalAlignment.Bottom) classes("d-dialog-bottom")
            style {
                surfaceAppearance(appearance)
                modalLayout(layout)
            }
            ref { element ->
                val previousFocus = document.activeElement as? HTMLElement
                val previousTabIndex = element.getAttribute("tabindex")
                val pointersStartedOutside = mutableMapOf<Int, Boolean>()
                // Treat the manual path as active unless showModal succeeds. Older engines may
                // omit showModal entirely or expose it while still rejecting the call.
                var fallback = true
                var fallbackScrim: HTMLElement? = null
                openHtmlModalOrder.remove(element)
                openHtmlModalOrder.add(element)
                val cancel: (org.w3c.dom.events.Event) -> Unit = {
                    it.preventDefault()
                    if (allowBackDismiss && isTopmostModal(element)) dismiss()
                }
                val pointerDown: (org.w3c.dom.events.Event) -> Unit = { event ->
                    val pointer = event.asDynamic()
                    val x = (pointer.clientX as? Number)?.toDouble()
                    val y = (pointer.clientY as? Number)?.toDouble()
                    val pointerId = (pointer.pointerId as? Number)?.toInt()
                    val hasPoint = x != null && y != null
                    val isOutside = hasPoint && isOutsideModal(element, x!!, y!!)
                    if (pointerId != null && hasPoint) pointersStartedOutside[pointerId] = isOutside
                    val shouldBlockFallbackPointer = fallback && isTopmostModal(element) && isOutside
                    if (shouldBlockFallbackPointer) {
                        event.preventDefault()
                        event.stopPropagation()
                    }
                }
                val pointerUp: (org.w3c.dom.events.Event) -> Unit = { event ->
                    val pointer = event.asDynamic()
                    val x = (pointer.clientX as? Number)?.toDouble()
                    val y = (pointer.clientY as? Number)?.toDouble()
                    val pointerId = (pointer.pointerId as? Number)?.toInt()
                    val startedOutside = pointerId?.let(pointersStartedOutside::remove) ?: false
                    val hasPoint = x != null && y != null
                    val endedOutside = hasPoint && isOutsideModal(element, x!!, y!!)
                    val isTopmost = isTopmostModal(element)
                    val outsideRelease = endedOutside && !element.contains(event.target as? Node)
                    if (fallback && isTopmost && outsideRelease) {
                        event.preventDefault()
                        event.stopPropagation()
                    }
                    val dismissOutsideGesture =
                        allowOutsideDismiss && startedOutside && endedOutside && isTopmost
                    if (dismissOutsideGesture) dismiss()
                }
                val pointerCancel: (org.w3c.dom.events.Event) -> Unit = { event ->
                    val pointerId = (event.asDynamic().pointerId as? Number)?.toInt()
                    if (pointerId != null) pointersStartedOutside.remove(pointerId)
                }
                val keydown: (org.w3c.dom.events.Event) -> Unit = { event ->
                    if (fallback && isTopmostModal(element)) {
                        val keyboard = event
                        if (keyboard.asDynamic().key == "Escape") {
                            keyboard.preventDefault()
                            if (allowBackDismiss) dismiss()
                        } else if (keyboard.asDynamic().key == "Tab") {
                            trapModalTab(element, keyboard)
                        }
                    }
                }
                val click: (org.w3c.dom.events.Event) -> Unit = { event ->
                    val outsideTarget = !element.contains(event.target as? Node)
                    // Keyboard-generated clicks have zero coordinates even for an inside control.
                    if (fallback && isTopmostModal(element) && outsideTarget) {
                        event.preventDefault()
                        event.stopPropagation()
                    }
                }
                val focusIn: (org.w3c.dom.events.Event) -> Unit = { event ->
                    if (fallback && isTopmostModal(element) && !element.contains(event.target as? org.w3c.dom.Node)) {
                        focusModal(element)
                    }
                }

                fun addCapture(
                    type: String,
                    listener: (org.w3c.dom.events.Event) -> Unit,
                ) {
                    document.asDynamic().addEventListener(type, listener, true)
                }

                fun removeCapture(
                    type: String,
                    listener: (org.w3c.dom.events.Event) -> Unit,
                ) {
                    document.asDynamic().removeEventListener(type, listener, true)
                }
                addCapture("pointerdown", pointerDown)
                addCapture("pointerup", pointerUp)
                addCapture("pointercancel", pointerCancel)
                addCapture("keydown", keydown)
                addCapture("click", click)
                addCapture("focusin", focusIn)
                element.addEventListener("cancel", cancel)
                if (jsTypeOf(element.asDynamic().showModal) == "function") {
                    try {
                        element.showModal()
                        fallback = false
                        focusModal(element)
                    } catch (_: Throwable) {
                        // Some browsers expose showModal but reject it for an attached dialog.
                    }
                }
                if (fallback) {
                    element.setAttribute("open", "")
                    element.setAttribute("role", "dialog")
                    element.setAttribute("aria-modal", "true")
                    if (previousTabIndex == null) element.setAttribute("tabindex", "-1")
                    element.classList.add("d-dialog-fallback")
                    fallbackScrim = document.createElement("div") as HTMLElement
                    fallbackScrim?.classList?.add("d-dialog-fallback-scrim")
                    fallbackScrim?.style?.zIndex =
                        "${HTML_MODAL_SCRIM_Z_INDEX + openHtmlModalOrder.indexOf(
                            element,
                        ) * HTML_MODAL_STACK_Z_INDEX_STEP}"
                    fallbackScrim?.let { document.body?.appendChild(it) }
                    element.style.zIndex =
                        "${HTML_MODAL_SURFACE_Z_INDEX + openHtmlModalOrder.indexOf(
                            element,
                        ) * HTML_MODAL_STACK_Z_INDEX_STEP}"
                    focusModal(element)
                }
                val removeSwipe =
                    if (sheet && layout.swipeToDismiss) {
                        installModalSheetSwipe(element, layout, skipPartiallyExpanded) { dismiss() }
                    } else {
                        null
                    }

                fun updatePartialAnchor() {
                    if (sheet) {
                        val canPartiallyExpand = !skipPartiallyExpanded && isSheetDragHandleVisible(element)
                        if (canPartiallyExpand && !element.classList.contains("d-sheet-expanded")) {
                            element.style.setProperty("max-height", layout.partialSheetMaxHeightCss())
                        } else {
                            val maxHeight = layout.modalMaxHeightCss()
                            if (maxHeight == null) {
                                element.style.removeProperty("max-height")
                            } else {
                                element.style.setProperty("max-height", maxHeight)
                            }
                        }
                    }
                }

                val resizePartialAnchor: (Event) -> Unit = { updatePartialAnchor() }
                updatePartialAnchor()
                window.addEventListener("resize", resizePartialAnchor)
                onDispose {
                    element.removeEventListener("cancel", cancel)
                    removeCapture("pointerdown", pointerDown)
                    removeCapture("pointerup", pointerUp)
                    removeCapture("pointercancel", pointerCancel)
                    removeCapture("keydown", keydown)
                    removeCapture("click", click)
                    removeCapture("focusin", focusIn)
                    removeSwipe?.invoke()
                    window.removeEventListener("resize", resizePartialAnchor)
                    val wasTopmost = isTopmostModal(element)
                    openHtmlModalOrder.remove(element)
                    if (element.open && jsTypeOf(element.asDynamic().close) == "function") {
                        element.close()
                    } else {
                        element.removeAttribute("open")
                    }
                    element.classList.remove("d-dialog-fallback")
                    if (fallback && previousTabIndex == null) element.removeAttribute("tabindex")
                    fallbackScrim?.let { scrim -> scrim.parentNode?.removeChild(scrim) }
                    element.style.removeProperty("z-index")
                    if (wasTopmost) previousFocus?.takeIf { it.isConnected }?.let(::restoreModalFocus)
                }
            }
        }) {
            if (sheet) {
                Div(attrs = {
                    classes("d-sheet-drag-handle")
                    attr("aria-hidden", "true")
                }) {
                    Div(attrs = { classes("d-sheet-drag-indicator") }) {}
                }
            }
            Div(attrs = { classes("d-dialog-header") }) {
                if (headerContent == null) {
                    H2(attrs = { id(titleId) }) { Text(title) }
                } else {
                    DomposableHtmlWidthDemandContainer(propagateToParent = false) {
                        headerContent()
                    }
                }
            }
            DomposableHtmlWidthDemandContainer(propagateToParent = false) {
                content()
            }
            if (headerContent == null) {
                HtmlButton(
                    DomposableButtonProps({ dismiss() }, style = DomposableButtonStyle.Text),
                    DomposableModifier,
                    { Text("Close") },
                    respectFormEnabled = false,
                )
            }
        }
    }
}

internal fun isTopmostModal(element: HTMLElement): Boolean = openHtmlModalOrder.lastOrNull() == element

private fun focusModal(element: HTMLElement) {
    val target =
        element.querySelector(HTML_MODAL_SELECTOR) as? HTMLElement
    (target ?: element).focus()
}

private fun restoreModalFocus(previous: HTMLElement) {
    if (previous == document.body && !previous.hasAttribute("tabindex")) {
        previous.setAttribute("tabindex", "-1")
        previous.asDynamic().focus(js("({preventScroll:true})"))
        previous.removeAttribute("tabindex")
    } else {
        previous.focus()
    }
}

private fun isOutsideModal(
    element: HTMLElement,
    x: Double,
    y: Double,
): Boolean {
    val bounds = element.getBoundingClientRect()
    return x < bounds.left || x > bounds.right || y < bounds.top || y > bounds.bottom
}

private fun trapModalTab(
    element: HTMLElement,
    keyboard: Event,
) {
    val nodes = element.querySelectorAll(HTML_MODAL_SELECTOR)
    val first = nodes.item(0) as? HTMLElement
    val last = nodes.item(nodes.length - 1) as? HTMLElement
    if (first == null || last == null) {
        keyboard.preventDefault()
        element.focus()
    } else if ((keyboard.asDynamic().shiftKey == true) && document.activeElement == first) {
        keyboard.preventDefault()
        last.focus()
    } else if (!(keyboard.asDynamic().shiftKey == true) && document.activeElement == last) {
        keyboard.preventDefault()
        first.focus()
    }
}
