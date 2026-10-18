package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.samuolis.domposable.foundation.DomposableIconAppearance
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableIconProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableModifierOperation
import io.github.samuolis.domposable.ui.DomposableViewportHeightMode
import io.github.samuolis.domposable.ui.domposableAttributes
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Span
import org.w3c.dom.Element

private const val SVG_NAMESPACE = "http://www.w3.org/2000/svg"

internal fun DomposableModifier.hasExplicitHtmlWidth(): Boolean =
    elements.any {
        it is DomposableModifierOperation.Width ||
            it is DomposableModifierOperation.FillWidthFraction ||
            it == DomposableModifierOperation.FillWidth
    }

internal fun DomposableModifier.hasExplicitHtmlHeight(): Boolean =
    elements.any {
        it is DomposableModifierOperation.Height ||
            (it is DomposableModifierOperation.ViewportHeight && it.mode == DomposableViewportHeightMode.Fixed) ||
            it == DomposableModifierOperation.FillHeight
    }

@Composable
internal fun HtmlDomposableVectorIcon(
    props: DomposableIconProps,
    modifier: DomposableModifier,
    appearance: DomposableIconAppearance,
) {
    val vector = requireNotNull(props.vector)
    val svgAndPath = remember { mutableStateOf<Pair<Element, Element>?>(null) }
    val widthIsExplicit = modifier.hasExplicitHtmlWidth()
    val heightIsExplicit = modifier.hasExplicitHtmlHeight()
    DomposableHtmlModifiers(modifier) {
        Span(attrs = {
            domposableAttributes(modifier)
            classes("d-icon", "d-icon-vector")
            if (props.description == null) {
                attr("aria-hidden", "true")
            } else {
                attr("role", "img")
                attr("aria-label", props.description.orEmpty())
            }
            style {
                property("display", "inline-flex")
                property("align-items", "center")
                property("justify-content", "center")
                property("line-height", "1")
                property("width", if (widthIsExplicit) "100%" else "${vector.viewportWidth}px")
                property("height", if (heightIsExplicit) "100%" else "${vector.viewportHeight}px")
                property("max-width", "100%")
                property("max-height", "100%")
                appearance.color?.let { property("color", it.css()) }
                appearance.textAppearance?.let { applyCatalogTextAppearance(it) }
            }
            ref { host ->
                val svg = document.createElementNS(SVG_NAMESPACE, "svg")
                val path = document.createElementNS(SVG_NAMESPACE, "path")
                svg.setAttribute("viewBox", "0 0 ${vector.viewportWidth} ${vector.viewportHeight}")
                svg.setAttribute("width", if (widthIsExplicit) "100%" else "${vector.viewportWidth}")
                svg.setAttribute("height", if (heightIsExplicit) "100%" else "${vector.viewportHeight}")
                svg.setAttribute("aria-hidden", "true")
                svg.setAttribute("focusable", "false")
                svg.setAttribute("style", "display:block;max-width:100%;max-height:100%")
                path.setAttribute("fill", "currentColor")
                svg.appendChild(path)
                host.appendChild(svg)
                svgAndPath.value = svg to path
                onDispose {
                    if (svg.parentNode === host) host.removeChild(svg)
                    if (svgAndPath.value?.first === svg) svgAndPath.value = null
                }
            }
        }) {}
    }
    SideEffect {
        svgAndPath.value?.let { (svg, path) ->
            svg.setAttribute("viewBox", "0 0 ${vector.viewportWidth} ${vector.viewportHeight}")
            svg.setAttribute("width", if (widthIsExplicit) "100%" else "${vector.viewportWidth}")
            svg.setAttribute("height", if (heightIsExplicit) "100%" else "${vector.viewportHeight}")
            path.setAttribute("d", vector.pathData)
            path.setAttribute("fill-rule", if (vector.evenOdd) "evenodd" else "nonzero")
            path.setAttribute("clip-rule", if (vector.evenOdd) "evenodd" else "nonzero")
        }
    }
}
