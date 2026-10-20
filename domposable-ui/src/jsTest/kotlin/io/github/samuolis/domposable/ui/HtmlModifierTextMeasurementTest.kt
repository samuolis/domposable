package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.dp
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlModifierTextMeasurementTest {
    @Test
    fun stackedTextModifiersDoNotAddInheritedBaselineSpace() {
        val cases =
            listOf(
                Triple("semantic", DomposableModifier, 17),
                Triple("padding", DomposableModifier.padding(4.dp), 25),
                Triple("opacity", DomposableModifier.alpha(0.7f), 17),
                Triple("alignment", DomposableModifier.weight(1f), 17),
                Triple("custom", DomposableModifier.then(TextScope), 17),
            )
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                Div(attrs = {
                    style {
                        property("display", "flex")
                        property("flex-direction", "column")
                        property("font-size", "16px")
                        property("line-height", "24px")
                    }
                }) {
                    cases.forEach { (name, modifier, _) ->
                        val taggedModifier = modifier.testId(name)
                        val semanticWrapper = name == "semantic"
                        DomposableHtmlModifiers(
                            taggedModifier,
                            wrapSemantics = semanticWrapper,
                            operationRenderer = { element ->
                                if (element == TextScope) ({ classes("custom-text") }) else null
                            },
                        ) {
                            Span(attrs = {
                                if (!semanticWrapper) domposableAttributes(taggedModifier)
                                style {
                                    property("font-size", "14px")
                                    property("line-height", "17px")
                                }
                            }) { Text("One line") }
                        }
                    }
                }
            }
        try {
            var previousBottom: Double? = null
            cases.forEach { (name, _, expected) ->
                val tag = root.querySelector("[data-testid=$name]") as HTMLElement
                // Standard components put semantic attributes on their content root, without an
                // inner semantic wrapper that could mask a modifier's anonymous line box.
                val box = if (name == "semantic") tag else tag.parentElement as HTMLElement
                assertEquals(expected, box.offsetHeight, "$name measured text height")
                val bounds = box.getBoundingClientRect()
                previousBottom?.let { assertEquals(it, bounds.top, "$name stack gap") }
                previousBottom = bounds.bottom
            }
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}

private data object TextScope : DomposableModifierElement
