package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.math.max

private const val MEDIUM_BASELINE_PADDING = 24.0
private const val LARGE_BASELINE_PADDING = 28.0
private const val FALLBACK_DESCENT = 0.2

internal fun AttrsScope<HTMLElement>.materialAppBarCopyLayout(variant: DomposableTopAppBarVariant) {
    val padding =
        when (variant) {
            DomposableTopAppBarVariant.Medium, DomposableTopAppBarVariant.MediumFlexible -> MEDIUM_BASELINE_PADDING
            DomposableTopAppBarVariant.Large, DomposableTopAppBarVariant.LargeFlexible -> LARGE_BASELINE_PADDING
            else -> return
        }
    ref { copy ->
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        val context = canvas.getContext("2d") as? CanvasRenderingContext2D
        val measure: () -> Unit = {
            val lastLine = copy.lastElementChild as? HTMLElement
            val slot = copy.parentElement as? HTMLElement
            if (lastLine != null && slot != null && copy.isConnected) {
                val style = window.getComputedStyle(lastLine)
                val size = style.fontSize.removeSuffix("px").toDoubleOrNull() ?: 0.0
                val line = style.lineHeight.removeSuffix("px").toDoubleOrNull() ?: size
                context?.font = "${style.fontWeight} ${style.fontSize} ${style.fontFamily}"
                val metrics = context?.measureText("Mg")?.asDynamic()
                val ascent = (metrics?.fontBoundingBoxAscent as? Number)?.toDouble() ?: size
                val descent = (metrics?.fontBoundingBoxDescent as? Number)?.toDouble() ?: size * FALLBACK_DESCENT
                val belowBaseline = (line - ascent - descent) / 2.0 + descent
                val available = max(0.0, slot.clientHeight.toDouble() - copy.offsetHeight)
                val gap = (padding - belowBaseline).coerceIn(0.0, available)
                slot.style.setProperty("--d-app-bar-baseline-gap", "${gap}px")
            }
        }
        measure()
        val observer = js("typeof ResizeObserver !== 'undefined' ? new ResizeObserver(measure) : null")
        observer?.observe(copy)
        observer?.observe(copy.parentElement)
        val changed: (Event) -> Unit = { measure() }
        val fonts = document.asDynamic().fonts
        fonts?.ready?.then { _: dynamic -> measure() }
        fonts?.addEventListener("loadingdone", changed)
        if (observer == null) window.addEventListener("resize", changed)
        onDispose {
            observer?.disconnect()
            fonts?.removeEventListener("loadingdone", changed)
            if (observer == null) window.removeEventListener("resize", changed)
        }
    }
}
