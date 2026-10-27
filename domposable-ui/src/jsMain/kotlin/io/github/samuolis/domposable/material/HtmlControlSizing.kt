package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableModifierOperation
import org.jetbrains.compose.web.css.StyleScope

/** A bounded native control consumes its parent's width; its HTML leaf must do the same. */
internal fun StyleScope.constrainedControlWidth(modifier: DomposableModifier) {
    val bounded =
        modifier.elements.any {
            when (it) {
                is DomposableModifierOperation.Width,
                is DomposableModifierOperation.FillWidthFraction,
                is DomposableModifierOperation.Weight,
                DomposableModifierOperation.FillWidth,
                -> true

                else -> false
            }
        }
    if (bounded) {
        property("display", "inline-flex")
        property("width", "100%")
    }
}
