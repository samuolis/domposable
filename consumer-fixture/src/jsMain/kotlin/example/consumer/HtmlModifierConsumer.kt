package example.consumer

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTypographyRole
import io.github.samuolis.domposable.material.domposableTextAppearance
import io.github.samuolis.domposable.ui.DomposableHtmlModifierDecoration
import io.github.samuolis.domposable.ui.DomposableHtmlModifierDecorationRenderer
import io.github.samuolis.domposable.ui.DomposableHtmlModifierElementRenderer
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.htmlAttributes
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun ThemedHtmlTextConsumer(text: String) {
    Span(attrs = {
        domposableTextAppearance(DomposableTextAppearance(role = DomposableTypographyRole.BodyMedium))
    }) { Text(text) }
}

@Composable
fun DecoratedHtmlConsumer(
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val renderer: DomposableHtmlModifierDecorationRenderer = { element ->
        if (element is ConsumerFrame) {
            val decoration: DomposableHtmlModifierDecoration = { inner ->
                Div(attrs = { style { property("border", "2px solid ${element.color.css()}") } }) { inner() }
            }
            decoration
        } else {
            null
        }
    }
    DomposableHtmlModifiers(modifier, decorationRenderer = renderer, content = content)
}

// The existing three-argument call remains source-compatible.
@Composable
fun AttributeHtmlConsumer(
    modifier: DomposableModifier,
    renderer: DomposableHtmlModifierElementRenderer,
    content: @Composable () -> Unit,
) = DomposableHtmlModifiers(modifier, renderer, content)

@Composable
fun HtmlAttributesConsumer(text: String) {
    DomposableText(
        text,
        modifier =
            DomposableModifier.htmlAttributes {
                classes("consumer-message")
                attr("data-consumer", "published-js-api")
                style { property("white-space", "pre-line") }
            },
    )
}

@Composable
fun WrappedHtmlConsumer(
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) = DomposableHtmlModifiers(modifier, wrapSemantics = true, content = content)
