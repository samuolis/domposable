package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import io.github.samuolis.domposable.ui.DomposableHtmlModifierDecoration
import io.github.samuolis.domposable.ui.DomposableHtmlModifierDecorationRenderer
import io.github.samuolis.domposable.ui.DomposableHtmlModifierElementRenderer
import io.github.samuolis.domposable.ui.LocalDomposableHtmlModifierDecorationRenderer
import io.github.samuolis.domposable.ui.LocalDomposableHtmlModifierRenderer
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

@Composable
internal actual fun ProvideAppModifierRenderers(content: @Composable () -> Unit) {
    val parentDecoration = LocalDomposableHtmlModifierDecorationRenderer.current
    val parentAttributes = LocalDomposableHtmlModifierRenderer.current
    val decoration: DomposableHtmlModifierDecorationRenderer =
        remember(parentDecoration) {
            { element ->
                if (element is AppAccentFrame) {
                    val render: DomposableHtmlModifierDecoration = { inner ->
                        Div(attrs = {
                            attr("data-app-frame", "true")
                            style { property("position", "relative") }
                        }) {
                            Div(attrs = {
                                attr("aria-hidden", "true")
                                attr("data-app-frame-paint", "true")
                                style {
                                    property("position", "absolute")
                                    property("inset", "0")
                                    property("border", "3px solid ${element.color.css()}")
                                    property("pointer-events", "none")
                                }
                            })
                            inner()
                        }
                    }
                    render
                } else {
                    parentDecoration?.invoke(element)
                }
            }
        }
    val attributes: DomposableHtmlModifierElementRenderer =
        remember(parentAttributes) {
            { element ->
                if (element == AppContentMarker) {
                    val render: AttrsScope<HTMLElement>.() -> Unit = { attr("data-app-content-marker", "true") }
                    render
                } else {
                    parentAttributes?.invoke(element)
                }
            }
        }
    CompositionLocalProvider(
        LocalDomposableHtmlModifierDecorationRenderer provides decoration,
        LocalDomposableHtmlModifierRenderer provides attributes,
        content = content,
    )
}

@Composable
internal actual fun AppCounterLabel(count: Int) {
    Span(attrs = {
        attr("data-testid", "app-counter-label")
        style {
            property("color", "#4059ad")
            property("font-weight", "600")
        }
    }) { Text("Custom counter: $count") }
}
