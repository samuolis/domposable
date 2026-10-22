package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.ui.DomposableModalLayout
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val HTML_MODAL_SWIPE_SELECTOR = ".d-sheet-drag-handle,.d-dialog-header"
private const val HTML_MODAL_SWIPE_CONTROL_SELECTOR =
    "button,input,textarea,select,a,[role=button],[contenteditable=true],[tabindex]"
private const val HTML_MODAL_HORIZONTAL_SLOP = 12.0
private const val HTML_MODAL_MIN_SWIPE_DISTANCE = 32.0
private const val HTML_MODAL_MIN_SWIPE_VELOCITY = 0.6
private const val HTML_MODAL_MIN_SWIPE_THRESHOLD = 72.0
private const val HTML_MODAL_MAX_SWIPE_THRESHOLD = 120.0
private const val HTML_MODAL_SWIPE_THRESHOLD_FRACTION = 0.2
private const val HTML_MODAL_PARTIAL_ANCHOR_HEIGHT_FRACTION = 0.49
private const val HTML_MODAL_SHEET_TRANSITION_MS = 180

private data class ModalSwipeStart(
    val pointerId: Int,
    val target: HTMLElement,
    val x: Double,
    val y: Double,
    val time: Double,
)

private fun isHorizontalSwipe(
    dx: Double,
    dy: Double,
): Boolean = abs(dx) > max(HTML_MODAL_HORIZONTAL_SLOP, abs(dy))

private fun isSignificantSwipe(
    distance: Double,
    elapsed: Double,
    height: Double,
): Boolean {
    val threshold =
        min(
            HTML_MODAL_MAX_SWIPE_THRESHOLD,
            max(
                HTML_MODAL_MIN_SWIPE_THRESHOLD,
                height * HTML_MODAL_SWIPE_THRESHOLD_FRACTION,
            ),
        )
    val magnitude = abs(distance)
    val sufficientDistance = magnitude >= threshold
    val sufficientVelocity =
        magnitude >= HTML_MODAL_MIN_SWIPE_DISTANCE && magnitude / elapsed >= HTML_MODAL_MIN_SWIPE_VELOCITY
    return sufficientDistance || sufficientVelocity
}

private fun findModalSwipeStart(
    event: Event,
    sheet: HTMLElement,
): ModalSwipeStart? {
    if (!isSheetDragHandleVisible(sheet)) return null
    val pointer = event.asDynamic()
    val target = event.target as? HTMLElement
    val area = target?.closest(HTML_MODAL_SWIPE_SELECTOR) as? HTMLElement
    val control = target?.closest(HTML_MODAL_SWIPE_CONTROL_SELECTOR)
    val primaryPointer = pointer.isPrimary == true && pointer.button == 0
    val validArea = area != null && sheet.contains(area)
    val startsOnContent = control == null || area?.contains(control) != true
    if (!primaryPointer || !validArea || !startsOnContent) return null
    val pointerId = (pointer.pointerId as? Number)?.toInt() ?: return null
    val x = (pointer.clientX as? Number)?.toDouble() ?: return null
    val y = (pointer.clientY as? Number)?.toDouble() ?: return null
    return ModalSwipeStart(pointerId, area!!, x, y, event.timeStamp.toDouble())
}

internal fun isSheetDragHandleVisible(sheet: HTMLElement): Boolean {
    val handle = sheet.querySelector(".d-sheet-drag-handle") as? HTMLElement ?: return false
    return window.getComputedStyle(handle).display != "none"
}

internal fun installModalSheetSwipe(
    sheet: HTMLElement,
    layout: DomposableModalLayout,
    skipPartiallyExpanded: Boolean,
    dismiss: () -> Unit,
): () -> Unit = ModalSheetSwipeController(sheet, layout, skipPartiallyExpanded, dismiss).install()

private class ModalSheetSwipeController(
    private val sheet: HTMLElement,
    private val layout: DomposableModalLayout,
    private val skipPartiallyExpanded: Boolean,
    private val dismiss: () -> Unit,
) {
    private var pointerId: Int? = null
    private var captureTarget: HTMLElement? = null
    private var startX = 0.0
    private var startY = 0.0
    private var startTime = 0.0
    private var distance = 0.0

    private val down: (Event) -> Unit = { event -> handleDown(event) }
    private val move: (Event) -> Unit = { event -> handleMove(event) }
    private val up: (Event) -> Unit = { event -> handleUp(event) }
    private val cancel: (Event) -> Unit = { event ->
        if (pointerId != null && event.asDynamic().pointerId == pointerId) reset()
    }

    fun install(): () -> Unit {
        sheet.addEventListener("pointerdown", down)
        sheet.addEventListener("pointermove", move)
        sheet.addEventListener("pointerup", up)
        sheet.addEventListener("pointercancel", cancel)
        sheet.addEventListener("lostpointercapture", cancel)
        return {
            sheet.removeEventListener("pointerdown", down)
            sheet.removeEventListener("pointermove", move)
            sheet.removeEventListener("pointerup", up)
            sheet.removeEventListener("pointercancel", cancel)
            sheet.removeEventListener("lostpointercapture", cancel)
            reset()
        }
    }

    private fun handleDown(event: Event) {
        if (pointerId != null) return
        val start = findModalSwipeStart(event, sheet) ?: return
        pointerId = start.pointerId
        startX = start.x
        startY = start.y
        startTime = start.time
        captureTarget = start.target
        if (jsTypeOf(start.target.asDynamic().setPointerCapture) == "function") {
            start.target.asDynamic().setPointerCapture(start.pointerId)
        }
    }

    private fun handleMove(event: Event) {
        if (pointerId == null || event.asDynamic().pointerId != pointerId) return
        if (!isSheetDragHandleVisible(sheet)) {
            reset()
            return
        }
        val pointer = event.asDynamic()
        val dx = (pointer.clientX as Number).toDouble() - startX
        val dy = (pointer.clientY as Number).toDouble() - startY
        if (distance == 0.0 && isHorizontalSwipe(dx, dy)) {
            reset()
            return
        }
        distance = dy
        sheet.style.setProperty("transition", "none")
        sheet.style.setProperty("transform", "translateY(${max(0.0, distance)}px)")
        if (event.cancelable) event.preventDefault()
    }

    private fun handleUp(event: Event) {
        if (pointerId == null || event.asDynamic().pointerId != pointerId) return
        if (!isSheetDragHandleVisible(sheet)) {
            reset()
            return
        }
        val elapsed = max(1.0, event.timeStamp.toDouble() - startTime)
        val pointer = event.asDynamic()
        val endY = (pointer.clientY as? Number)?.toDouble()
        if (endY != null) {
            val endX = (pointer.clientX as? Number)?.toDouble()
            val dx = endX?.minus(startX) ?: 0.0
            val dy = endY - startY
            if (endX != null && isHorizontalSwipe(dx, dy)) {
                reset()
                return
            }
            distance = dy
        }
        val expanded = sheet.classList.contains("d-sheet-expanded")
        val hasPartialAnchor = hasPartialAnchor()
        val significant = isSignificantSwipe(distance, elapsed, sheet.getBoundingClientRect().height)
        val downward = distance > 0.0
        reset()
        if (!significant || !isTopmostModal(sheet)) return
        when {
            !downward && !expanded && hasPartialAnchor -> expand()
            downward && expanded && hasPartialAnchor -> collapse()
            downward -> dismiss()
        }
    }

    private fun reset() {
        val captured = pointerId
        pointerId = null
        val hasCapture = jsTypeOf(captureTarget?.asDynamic()?.hasPointerCapture) == "function"
        val captureIsHeld =
            captured != null && hasCapture && captureTarget?.asDynamic()?.hasPointerCapture(captured) == true
        if (captureIsHeld) captureTarget?.asDynamic()?.releasePointerCapture(captured!!)
        captureTarget = null
        distance = 0.0
        sheet.style.removeProperty("transform")
        sheet.style.removeProperty("transition")
    }

    private fun expand() {
        sheet.classList.add("d-sheet-expanded")
        sheet.style.setProperty(
            "transition",
            "max-height ${HTML_MODAL_SHEET_TRANSITION_MS}ms ease-out",
        )
        val expandedMaxHeight = layout.modalMaxHeightCss()
        if (expandedMaxHeight == null) {
            sheet.style.removeProperty("max-height")
        } else {
            sheet.style.setProperty("max-height", expandedMaxHeight)
        }
    }

    private fun collapse() {
        sheet.classList.remove("d-sheet-expanded")
        sheet.style.setProperty(
            "transition",
            "max-height ${HTML_MODAL_SHEET_TRANSITION_MS}ms ease-out",
        )
        sheet.style.setProperty("max-height", layout.partialSheetMaxHeightCss())
        window.setTimeout({
            if (!sheet.classList.contains("d-sheet-expanded")) {
                sheet.style.removeProperty("transition")
            }
        }, HTML_MODAL_SHEET_TRANSITION_MS)
    }

    private fun hasPartialAnchor(): Boolean {
        if (skipPartiallyExpanded) return false
        val bounds = sheet.getBoundingClientRect()
        return sheet.scrollHeight > sheet.clientHeight + 1 ||
            bounds.height >= window.innerHeight * HTML_MODAL_PARTIAL_ANCHOR_HEIGHT_FRACTION
    }
}
