package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableOrientation
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.coroutines.resume

@Suppress("TooManyFunctions") // Keep one element's scroll commands, listeners and cancellation in its owner.
actual class DomposableScrollState internal actual constructor(
    initialValue: Int,
) {
    private var currentValue by mutableIntStateOf(initialValue)
    private var scrollElement: HTMLElement? = null
    private var orientation: DomposableOrientation? = null
    private var activeAnimation: ScrollAnimation? = null
    private var commandVersion = 0L

    actual val value: Int get() = currentValue

    internal fun updateFromElement(
        element: HTMLElement,
        orientation: DomposableOrientation,
    ) {
        if (scrollElement !== element || this.orientation != orientation) return
        currentValue = readPosition(element, orientation)
    }

    internal fun attach(
        element: HTMLElement,
        orientation: DomposableOrientation,
    ) {
        val shouldRestore = scrollElement !== element || this.orientation != orientation
        if (scrollElement !== element) {
            scrollElement?.let(::detach)
            scrollElement = element
        }
        this.orientation = orientation
        if (shouldRestore) scheduleRestore(element, orientation, commandVersion)
    }

    internal fun detach(element: HTMLElement) {
        if (scrollElement !== element) return
        activeAnimation?.let { cancelAnimation(it, stopAtCurrentPosition = true) }
        scrollElement = null
        orientation = null
    }

    actual suspend fun scrollTo(value: Int) = scrollImmediately(value)

    internal fun scrollFromKey(
        element: HTMLElement,
        key: String,
        shift: Boolean,
    ): Boolean {
        val axis = orientation
        val target = if (scrollElement === element && axis != null) keyboardTarget(element, axis, key, shift) else null
        if (target == null) return false
        scrollImmediately(target)
        return true
    }

    private fun keyboardTarget(
        element: HTMLElement,
        orientation: DomposableOrientation,
        key: String,
        shift: Boolean,
    ): Int? {
        val position = readPosition(element, orientation)
        val vertical = orientation == DomposableOrientation.Vertical
        val viewport = if (vertical) element.clientHeight else element.clientWidth
        val pageStep = (viewport * KEY_PAGE_FRACTION).toInt().coerceAtLeast(KEY_SCROLL_STEP)
        val rtl = window.getComputedStyle(element).direction == "rtl"
        return when (key) {
            "Home" -> 0
            "End" -> maxOffset(element, orientation)
            "PageUp" -> position - pageStep
            "PageDown" -> position + pageStep
            " " -> position + if (shift) -pageStep else pageStep
            else -> keyboardArrowDelta(vertical, rtl, key)?.let { position + it }
        }
    }

    private fun keyboardArrowDelta(
        vertical: Boolean,
        rtl: Boolean,
        key: String,
    ): Int? =
        if (vertical) {
            when (key) {
                "ArrowUp" -> -KEY_SCROLL_STEP
                "ArrowDown" -> KEY_SCROLL_STEP
                else -> null
            }
        } else {
            val forward = if (rtl) -KEY_SCROLL_STEP else KEY_SCROLL_STEP
            when (key) {
                "ArrowLeft" -> -forward
                "ArrowRight" -> forward
                else -> null
            }
        }

    private fun scrollImmediately(value: Int) {
        cancelActiveAnimation(stopAtCurrentPosition = true)
        commandVersion++
        val element = scrollElement
        val axis = orientation
        currentValue =
            if (element != null && axis != null) clampTarget(element, axis, value) else value.coerceAtLeast(0)
        if (element != null && axis != null) writeInstant(element, axis, currentValue)
    }

    actual suspend fun animateScrollTo(value: Int) {
        val element = scrollElement
        val axis = orientation
        if (element == null || axis == null) {
            cancelActiveAnimation(stopAtCurrentPosition = true)
            currentValue = value.coerceAtLeast(0)
            return
        }

        cancelActiveAnimation(stopAtCurrentPosition = true)
        val version = ++commandVersion
        val requested = value.coerceAtLeast(0)
        val target = clampTarget(element, axis, requested)
        if (target == readPosition(element, axis) || window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
            currentValue = target
            writeInstant(element, axis, target)
            return
        }

        suspendCancellableCoroutine { continuation ->
            val animation = ScrollAnimation(element, axis, target, version, continuation)
            activeAnimation = animation
            installAnimationListeners(animation)
            continuation.invokeOnCancellation {
                if (activeAnimation === animation) cancelAnimation(animation, stopAtCurrentPosition = true)
            }
            scrollToWithBehavior(element, axis, target, "smooth")
            scheduleAnimationCheck(animation)
        }
    }

    private fun installAnimationListeners(animation: ScrollAnimation) {
        val element = animation.element
        animation.onScrollEnd = {
            val target = clampTarget(element, animation.orientation, animation.target)
            if (readPosition(element, animation.orientation) == target) finishAnimation(animation)
        }
        animation.onUserInput = { event ->
            val key = (event.asDynamic().key as? String).orEmpty()
            if (event.type != "keydown" || key in SCROLL_KEYS) {
                cancelAnimation(animation, stopAtCurrentPosition = true)
            }
        }
        element.addEventListener("scrollend", animation.onScrollEnd)
        USER_INPUT_EVENTS.forEach { element.addEventListener(it, animation.onUserInput) }
    }

    private fun scheduleAnimationCheck(animation: ScrollAnimation) {
        animation.frameId =
            window.requestAnimationFrame {
                if (activeAnimation !== animation) return@requestAnimationFrame
                val position = readPosition(animation.element, animation.orientation)
                val now = window.performance.now()
                val target = clampTarget(animation.element, animation.orientation, animation.target)
                if (position != animation.startPosition) animation.hasMoved = true
                if (position == target) {
                    finishAnimation(animation)
                } else {
                    if (position == animation.lastPosition) animation.stableFrames++ else animation.stableFrames = 0
                    animation.lastPosition = position
                    // `scrollend` is the normal completion signal. This fallback covers older browsers.
                    val nearTarget =
                        animation.hasMoved && kotlin.math.abs(position - target) <= SETTLED_TARGET_TOLERANCE
                    if (nearTarget &&
                        now - animation.startedAt >= FALLBACK_MIN_DURATION_MS &&
                        animation.stableFrames >= FALLBACK_STABLE_FRAMES
                    ) {
                        finishAnimation(animation)
                    } else {
                        scheduleAnimationCheck(animation)
                    }
                }
            }
    }

    private fun finishAnimation(animation: ScrollAnimation) {
        if (activeAnimation !== animation || animation.version != commandVersion) return
        removeAnimationListeners(animation)
        activeAnimation = null
        currentValue = readPosition(animation.element, animation.orientation)
        animation.frameId?.let(window::cancelAnimationFrame)
        animation.frameId = null
        if (animation.continuation.isActive) animation.continuation.resume(Unit)
    }

    private fun cancelActiveAnimation(stopAtCurrentPosition: Boolean) {
        activeAnimation?.let { cancelAnimation(it, stopAtCurrentPosition) }
    }

    private fun cancelAnimation(
        animation: ScrollAnimation,
        stopAtCurrentPosition: Boolean,
    ) {
        if (activeAnimation !== animation) return
        removeAnimationListeners(animation)
        activeAnimation = null
        animation.frameId?.let(window::cancelAnimationFrame)
        animation.frameId = null
        if (stopAtCurrentPosition) {
            val position = readPosition(animation.element, animation.orientation)
            // A zero-distance instant request can leave an existing compositor animation running.
            // Move one pixel and restore in the same task, before the browser can paint a frame.
            val alternate =
                if (position >
                    0
                ) {
                    position - 1
                } else {
                    minOf(1, maxOffset(animation.element, animation.orientation))
                }
            if (alternate != position) writeInstant(animation.element, animation.orientation, alternate)
            writeInstant(animation.element, animation.orientation, position)
            currentValue = position
        }
        if (animation.continuation.isActive) animation.continuation.cancel()
    }

    private fun removeAnimationListeners(animation: ScrollAnimation) {
        animation.element.removeEventListener("scrollend", animation.onScrollEnd)
        USER_INPUT_EVENTS.forEach { animation.element.removeEventListener(it, animation.onUserInput) }
    }

    private fun writeInstant(
        element: HTMLElement,
        orientation: DomposableOrientation,
        target: Int,
    ) {
        scrollToWithBehavior(element, orientation, target, "instant")
    }

    private fun scrollToWithBehavior(
        element: HTMLElement,
        orientation: DomposableOrientation,
        target: Int,
        behavior: String,
    ) {
        val options = js("({})")
        options.behavior = behavior
        if (orientation == DomposableOrientation.Vertical) {
            options.top = target
            options.left = element.scrollLeft
        } else {
            options.left = physicalHorizontalPosition(element, target)
            options.top = element.scrollTop
        }
        element.asDynamic().scrollTo(options)
    }

    private fun readPosition(
        element: HTMLElement,
        orientation: DomposableOrientation,
    ): Int =
        if (orientation == DomposableOrientation.Vertical) {
            element.scrollTop.toInt().coerceIn(0, maxOffset(element, orientation))
        } else {
            logicalHorizontalPosition(element)
        }

    private fun clampTarget(
        element: HTMLElement,
        orientation: DomposableOrientation,
        value: Int,
    ): Int = value.coerceIn(0, maxOffset(element, orientation))

    private fun maxOffset(
        element: HTMLElement,
        orientation: DomposableOrientation,
    ): Int =
        (
            if (orientation == DomposableOrientation.Vertical) {
                element.scrollHeight - element.clientHeight
            } else {
                element.scrollWidth - element.clientWidth
            }
        ).coerceAtLeast(0)

    private fun logicalHorizontalPosition(element: HTMLElement): Int {
        val max = maxOffset(element, DomposableOrientation.Horizontal)
        val raw = element.scrollLeft.toInt()
        if (window.getComputedStyle(element).direction != "rtl") return raw.coerceIn(0, max)
        val logical =
            when (rtlScrollModel()) {
                RtlScrollModel.Negative -> -raw
                RtlScrollModel.Reverse -> raw
                RtlScrollModel.Default -> max - raw
            }
        return logical.coerceIn(0, max)
    }

    private fun physicalHorizontalPosition(
        element: HTMLElement,
        logical: Int,
    ): Int {
        if (window.getComputedStyle(element).direction != "rtl") return logical
        val max = maxOffset(element, DomposableOrientation.Horizontal)
        return when (rtlScrollModel()) {
            RtlScrollModel.Negative -> -logical
            RtlScrollModel.Reverse -> logical
            RtlScrollModel.Default -> max - logical
        }
    }

    private fun rtlScrollModel(): RtlScrollModel =
        rtlModel ?: document.body?.let { detectRtlScrollModel(it).also { model -> rtlModel = model } }
            ?: RtlScrollModel.Negative

    private fun detectRtlScrollModel(body: HTMLElement): RtlScrollModel {
        val outer = document.createElement("div") as HTMLElement
        val inner = document.createElement("div") as HTMLElement
        outer.setAttribute("dir", "rtl")
        outer.style.position = "absolute"
        outer.style.width = "4px"
        outer.style.height = "1px"
        outer.style.setProperty("overflow", "scroll")
        outer.style.visibility = "hidden"
        inner.style.width = "8px"
        inner.style.height = "1px"
        outer.appendChild(inner)
        body.appendChild(outer)
        val initial = outer.scrollLeft
        val model =
            when {
                initial > 0 -> {
                    RtlScrollModel.Default
                }

                else -> {
                    outer.scrollLeft = 1.0
                    if (outer.scrollLeft == 0.0) RtlScrollModel.Negative else RtlScrollModel.Reverse
                }
            }
        body.removeChild(outer)
        return model
    }

    private fun scheduleRestore(
        element: HTMLElement,
        orientation: DomposableOrientation,
        version: Long,
        framesRemaining: Int = RESTORE_LAYOUT_FRAMES,
    ) {
        window.requestAnimationFrame {
            if (scrollElement !== element || this.orientation != orientation || commandVersion != version) {
                return@requestAnimationFrame
            }
            if (framesRemaining > 1) {
                scheduleRestore(element, orientation, version, framesRemaining - 1)
            } else {
                val max = maxOffset(element, orientation)
                currentValue = currentValue.coerceAtMost(max)
                writeInstant(element, orientation, currentValue)
            }
        }
    }

    private inner class ScrollAnimation(
        val element: HTMLElement,
        val orientation: DomposableOrientation,
        val target: Int,
        val version: Long,
        val continuation: CancellableContinuation<Unit>,
    ) {
        val startedAt: Double = window.performance.now()
        val startPosition: Int = readPosition(element, orientation)
        var lastPosition: Int = startPosition
        var hasMoved: Boolean = false
        var stableFrames: Int = 0
        var frameId: Int? = null
        lateinit var onScrollEnd: (Event) -> Unit
        lateinit var onUserInput: (Event) -> Unit
    }

    private companion object {
        const val FALLBACK_MIN_DURATION_MS = 180.0
        const val FALLBACK_STABLE_FRAMES = 6
        const val SETTLED_TARGET_TOLERANCE = 1
        const val RESTORE_LAYOUT_FRAMES = 2
        const val KEY_SCROLL_STEP = 40
        const val KEY_PAGE_FRACTION = 0.9
        val USER_INPUT_EVENTS = listOf("wheel", "touchstart", "pointerdown", "keydown")
        val SCROLL_KEYS =
            setOf("ArrowUp", "ArrowDown", "ArrowLeft", "ArrowRight", "PageUp", "PageDown", "Home", "End", " ")
        var rtlModel: RtlScrollModel? = null
    }
}

private enum class RtlScrollModel { Negative, Reverse, Default }

@Composable
actual fun rememberDomposableScrollState(initialValue: Int): DomposableScrollState {
    require(initialValue >= 0) { "Initial scroll position must be non-negative" }
    return remember { DomposableScrollState(initialValue) }
}
