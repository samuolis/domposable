package io.github.samuolis.domposable.material

import kotlinx.browser.document
import org.w3c.dom.HTMLElement

private val wheelProgrammaticPositions = js("new WeakMap()")

internal fun isProgrammaticWheelScroll(viewport: HTMLElement): Boolean =
    wheelProgrammaticPositions.get(viewport) == viewport.scrollTop

internal fun scrollCarouselItemIntoView(
    id: String,
    index: Int,
) {
    val viewport = document.getElementById(id) as? HTMLElement ?: return
    val item = viewport.children.item(index) as? HTMLElement ?: return
    val bounds = viewport.getBoundingClientRect()
    val target = item.getBoundingClientRect()
    val delta =
        when {
            target.left < bounds.left -> target.left - bounds.left
            target.right > bounds.right -> target.right - bounds.right
            else -> 0.0
        }
    viewport.scrollLeft += delta
}

internal fun scrollWheelItemIntoView(
    id: String,
    index: Int,
) {
    val viewport = document.getElementById("$id-viewport") as? HTMLElement ?: return
    val item = document.getElementById("$id-value-$index") as? HTMLElement ?: return
    val padding = ((viewport.clientHeight - item.offsetHeight) / 2.0).coerceAtLeast(0.0)
    viewport.style.paddingTop = "${padding}px"
    viewport.style.paddingBottom = "${padding}px"
    val bounds = viewport.getBoundingClientRect()
    val target = item.getBoundingClientRect()
    viewport.scrollTop += target.top - bounds.top - (viewport.clientHeight - target.height) / 2.0
    // A controlled update must not echo a scroll event back as a new user selection.
    wheelProgrammaticPositions.set(viewport, viewport.scrollTop)
}
