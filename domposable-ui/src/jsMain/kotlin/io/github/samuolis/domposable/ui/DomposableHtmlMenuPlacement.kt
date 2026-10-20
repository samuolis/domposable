package io.github.samuolis.domposable.ui

import org.w3c.dom.HTMLElement

/** Preferred side for a semantic HTML menu positioned against a DOM-owned anchor. */
enum class DomposableHtmlMenuPlacement {
    Auto,
    Above,
    Below,
}

/** Resolves a preferred HTML menu side from the rendered menu root. */
data class DomposableHtmlMenuPlacementHint(
    val resolve: (menuElement: HTMLElement) -> DomposableHtmlMenuPlacement,
) : DomposableModifierElement

/** Prefers a side when it fits; otherwise the menu uses its normal viewport-aware placement. */
fun DomposableModifier.htmlMenuPlacement(placement: DomposableHtmlMenuPlacement): DomposableModifier =
    then(DomposableHtmlMenuPlacementHint { placement })

/** Resolves the preferred side from the rendered menu root; this modifier is JS-only. */
fun DomposableModifier.htmlMenuPlacement(
    resolve: (menuElement: HTMLElement) -> DomposableHtmlMenuPlacement,
): DomposableModifier = then(DomposableHtmlMenuPlacementHint(resolve))
