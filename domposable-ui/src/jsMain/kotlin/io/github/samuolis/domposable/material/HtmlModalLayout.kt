package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.ui.DomposableModalAlignment
import io.github.samuolis.domposable.ui.DomposableModalLayout
import org.jetbrains.compose.web.css.StyleScope

private const val HTML_MODAL_VIEWPORT_PERCENT = 100

internal fun StyleScope.modalLayout(layout: DomposableModalLayout) {
    modalWidth(layout)
    val inset = layout.viewportInset.value
    layout.padding?.let { property("padding", it.cssPadding()) }
    layout.sectionSpacing?.let { property("gap", "${it.value}px") }
    layout.heightFraction?.let {
        property("height", "calc(${it * HTML_MODAL_VIEWPORT_PERCENT}vh - ${inset * 2}px)")
        property("height", "calc(${it * HTML_MODAL_VIEWPORT_PERCENT}dvh - ${inset * 2}px)")
    }
    modalMaxHeightCss(layout, dynamicViewport = false)?.let { property("max-height", it) }
    layout.modalMaxHeightCss()?.let { property("max-height", it) }
    when (layout.alignment) {
        DomposableModalAlignment.Center, DomposableModalAlignment.BottomOnCompact -> Unit
        DomposableModalAlignment.Bottom -> property("margin", "auto auto 0")
    }
}

internal fun DomposableModalLayout.partialSheetMaxHeightCss(): String =
    modalMaxHeightCss()?.let { "min(50dvh, $it)" } ?: "50dvh"

internal fun DomposableModalLayout.modalMaxHeightCss(): String? = modalMaxHeightCss(this, dynamicViewport = true)

private fun modalMaxHeightCss(
    layout: DomposableModalLayout,
    dynamicViewport: Boolean,
): String? {
    val inset = layout.viewportInset.value
    val unit = if (dynamicViewport) "dvh" else "vh"
    val viewportMax =
        layout.maxViewportFraction?.let {
            "min(${it * HTML_MODAL_VIEWPORT_PERCENT}$unit, " +
                "calc(${HTML_MODAL_VIEWPORT_PERCENT}$unit - ${inset * 2}px))"
        }
    val maxHeight = layout.maxHeight?.let { "${it.value}px" }
    return when {
        viewportMax != null && maxHeight != null -> "min($viewportMax, $maxHeight)"
        viewportMax != null -> viewportMax
        maxHeight != null -> maxHeight
        else -> null
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
