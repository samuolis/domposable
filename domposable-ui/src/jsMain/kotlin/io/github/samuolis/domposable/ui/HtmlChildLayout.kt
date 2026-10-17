package io.github.samuolis.domposable.ui

import org.jetbrains.compose.web.css.StyleScope

internal fun StyleScope.childLayoutSizing(elements: List<DomposableModifierElement>) {
    property("display", "grid")
    property("grid-template-columns", "minmax(0,1fr)")
    property("grid-template-rows", "minmax(0,1fr)")
    val width =
        elements.firstOrNull {
            it == DomposableModifierOperation.FillWidth || it is DomposableModifierOperation.Width ||
                it is DomposableModifierOperation.FillWidthFraction || it is DomposableModifierOperation.WrapContentSize
        }
    val height =
        elements.firstOrNull {
            it == DomposableModifierOperation.FillHeight || it is DomposableModifierOperation.Height ||
                it is DomposableModifierOperation.WrapContentSize
        }
    // A flex/grid item must carry fill constraints before its ordered decoration wrappers.
    if (width == DomposableModifierOperation.FillWidth) property("width", "100%")
    if (height == DomposableModifierOperation.FillHeight) property("height", "100%")
}
