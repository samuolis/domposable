package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.ui.DomposableModalAlignment
import io.github.samuolis.domposable.ui.DomposableModalLayout
import org.jetbrains.compose.web.css.StyleScope

private const val HTML_MODAL_VIEWPORT_PERCENT = 100

internal fun StyleScope.modalLayout(layout: DomposableModalLayout) {
    modalWidth(layout)
    val inset = layout.viewportInset.value
    layout.padding?.let { property("padding", it.cssPadding()) }
    layout.sectionSpacing?.let { property("--d-dialog-gap", "${it.value}px") }
    layout.heightFraction?.let {
        property("height", "calc(${it * HTML_MODAL_VIEWPORT_PERCENT}vh - ${inset * 2}px)")
        property("height", "calc(${it * HTML_MODAL_VIEWPORT_PERCENT}dvh - ${inset * 2}px)")
    }
    val viewportMax =
        layout.maxViewportFraction?.let {
            property(
                "max-height",
                "min(${it * HTML_MODAL_VIEWPORT_PERCENT}vh, calc(${HTML_MODAL_VIEWPORT_PERCENT}vh - ${inset * 2}px))",
            )
            "min(${it * HTML_MODAL_VIEWPORT_PERCENT}dvh, " +
                "calc(${HTML_MODAL_VIEWPORT_PERCENT}dvh - ${inset * 2}px))"
        }
    val maxHeight = layout.maxHeight?.let { "${it.value}px" }
    when {
        viewportMax != null && maxHeight != null -> property("max-height", "min($viewportMax, $maxHeight)")
        viewportMax != null -> property("max-height", viewportMax)
        maxHeight != null -> property("max-height", maxHeight)
    }
    when (layout.alignment) {
        DomposableModalAlignment.Center, DomposableModalAlignment.BottomOnCompact -> Unit
        DomposableModalAlignment.Bottom -> property("margin", "auto auto 0")
    }
}

private fun StyleScope.modalWidth(layout: DomposableModalLayout) {
    val inset = layout.viewportInset.value
    val inlineInset = "var(--d-modal-inline-inset, ${inset * 2}px)"
    layout.maxWidth?.let {
        property(
            "max-width",
            "min(${it.value}px, calc(100vw - $inlineInset))",
        )
        property(
            "max-width",
            "min(${it.value}px, calc(100dvw - $inlineInset))",
        )
    }
    if (layout.maxWidth == null && layout.fillViewportWidth) {
        property("max-width", "calc(100vw - $inlineInset)")
        property("max-width", "calc(100dvw - $inlineInset)")
    }
    if (layout.fillViewportWidth) {
        property("width", "calc(100vw - $inlineInset)")
        property("width", "calc(100dvw - $inlineInset)")
        property("box-sizing", "border-box")
    }
    if (layout.maxWidth != null || layout.fillViewportWidth) property("min-width", "0")
}
