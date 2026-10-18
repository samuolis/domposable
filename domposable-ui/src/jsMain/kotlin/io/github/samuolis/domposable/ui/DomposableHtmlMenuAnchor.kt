package io.github.samuolis.domposable.ui

import org.w3c.dom.HTMLElement

/** Resolves a DOM-owned trigger for adapters that already render it outside Compose. */
data class DomposableHtmlMenuAnchor(
    val resolve: (HTMLElement) -> HTMLElement?,
) : DomposableModifierElement

/** Anchors an HTML menu to an existing trigger without adding a wrapper around that trigger. */
fun DomposableModifier.htmlMenuAnchor(resolve: (menuElement: HTMLElement) -> HTMLElement?): DomposableModifier =
    then(DomposableHtmlMenuAnchor(resolve))
