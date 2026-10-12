package io.github.samuolis.domposable.material

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.foundation.DomposableTextAlignment
import io.github.samuolis.domposable.foundation.DomposableTextDecoration
import io.github.samuolis.domposable.foundation.DomposableTextOverflow
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.ui.DomposableDisclosureProps
import io.github.samuolis.domposable.ui.DomposableFormProps
import io.github.samuolis.domposable.ui.DomposableLinkProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRichHtmlNode
import io.github.samuolis.domposable.ui.DomposableRichHtmlProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.ProvideDomposableFormSubmit
import io.github.samuolis.domposable.ui.domposableFormEnabled
import io.github.samuolis.domposable.ui.parseDomposableRichHtml
import io.github.samuolis.domposable.ui.toComposeModifier
import androidx.compose.ui.text.font.FontWeight as ComposeFontWeight

@Composable
internal fun NativeDomposableForm(
    props: DomposableFormProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    ProvideDomposableFormSubmit(props) {
        Column(
            modifier =
                modifier.toComposeModifier().semantics {
                    props.label?.let { contentDescription = it }
                    if (!props.enabled) disabled()
                },
            verticalArrangement = Arrangement.Top,
        ) { content() }
    }
}

@Composable
internal fun NativeDomposableLink(
    props: DomposableLinkProps,
    modifier: DomposableModifier,
) {
    val currentOnClick by rememberUpdatedState(props.onClick)
    val uriHandler = LocalUriHandler.current
    val appearance = props.appearance
    // Keep the existing native link's touch target, typography and spacing for default calls.
    if (appearance == null && props.textStyle == DomposableTextStyle.Body) {
        TextButton(
            onClick = { currentOnClick?.invoke() ?: uriHandler.openUri(props.href) },
            modifier = modifier.toComposeModifier(),
        ) { Text(props.label) }
        return
    }
    val baseStyle =
        when (props.textStyle) {
            DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge
            DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge
            DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge
        }
    val style =
        baseStyle.copy(
            fontSize = appearance?.fontSize?.sp ?: baseStyle.fontSize,
            lineHeight = appearance?.lineHeight?.sp ?: baseStyle.lineHeight,
            fontWeight = appearance?.fontWeight?.nativeFontWeight() ?: baseStyle.fontWeight,
            textAlign = appearance?.alignment?.nativeTextAlign() ?: baseStyle.textAlign,
            textDecoration = appearance?.decoration?.nativeTextDecoration() ?: baseStyle.textDecoration,
        )
    val text: @Composable () -> Unit = {
        Text(
            text = props.label,
            modifier =
                modifier.toComposeModifier().clickable(role = Role.Button) {
                    currentOnClick?.invoke() ?: uriHandler.openUri(props.href)
                },
            color = Color(LocalDomposableTokens.current.colors.primary.argb),
            style = style,
            maxLines = appearance?.maxLines ?: Int.MAX_VALUE,
            overflow = appearance?.overflow?.nativeTextOverflow() ?: TextOverflow.Clip,
        )
    }
    if (appearance?.selectable == true) SelectionContainer { text() } else text()
}

@Composable
internal fun NativeDomposableDisclosure(
    props: DomposableDisclosureProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    Card(modifier = modifier.toComposeModifier()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics {
                            role = Role.Button
                            stateDescription = if (props.expanded) "Expanded" else "Collapsed"
                        }.clickable(enabled = domposableFormEnabled(), onClick = props.onToggle),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(props.title, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.width(12.dp))
                Text(if (props.expanded) "▴" else "▾", style = MaterialTheme.typography.labelLarge)
            }
            props.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            if (props.expanded) content()
        }
    }
}

@Composable
internal fun NativeDomposableRichHtml(
    props: DomposableRichHtmlProps,
    modifier: DomposableModifier,
) {
    val currentOnLinkClick by rememberUpdatedState(props.onLinkClick)
    val parsed = remember(props.rawHtml) { parseDomposableRichHtml(props.rawHtml) }
    val baseStyle =
        when (props.style) {
            DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge
            DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge
            DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge
        }
    val headlineSize = MaterialTheme.typography.headlineSmall.fontSize
    val titleSize = MaterialTheme.typography.titleMedium.fontSize
    val annotated =
        remember(parsed, props.style, currentOnLinkClick, headlineSize, titleSize) {
            buildAnnotatedString {
                val writer = NativeRichHtmlWriter(this)
                parsed.forEach {
                    writer.appendNode(
                        node = it,
                        onLinkClick = currentOnLinkClick,
                        headlineSize = headlineSize,
                        titleSize = titleSize,
                    )
                }
            }
        }
    val textModifier =
        modifier.toComposeModifier().semantics {
            if (props.style == DomposableTextStyle.Title) heading()
        }
    val textContent: @Composable () -> Unit = {
        Text(annotated, modifier = textModifier, style = baseStyle)
    }
    if (props.selectable) SelectionContainer { textContent() } else textContent()
}

private class NativeRichHtmlWriter(
    val builder: androidx.compose.ui.text.AnnotatedString.Builder,
) {
    var lastChar: Char? = null
        private set

    fun append(value: String) {
        builder.append(value)
        if (value.isNotEmpty()) lastChar = value.last()
    }

    fun append(value: Char) {
        builder.append(value)
        lastChar = value
    }

    // The tree walk intentionally keeps the rich-text formatting rules together by AST node kind.
    @Suppress("LongMethod")
    fun appendNode(
        node: DomposableRichHtmlNode,
        onLinkClick: ((String) -> Unit)?,
        headlineSize: androidx.compose.ui.unit.TextUnit,
        titleSize: androidx.compose.ui.unit.TextUnit,
    ) {
        when (node) {
            is DomposableRichHtmlNode.Text -> {
                append(node.value)
            }

            is DomposableRichHtmlNode.Element -> {
                fun children() =
                    node.children.forEach {
                        appendNode(it, onLinkClick, headlineSize, titleSize)
                    }
                when (node.tag) {
                    "br" -> {
                        append('\n')
                    }

                    "p", "h1", "h2", "h3" -> {
                        if (builder.length > 0 && lastChar != '\n') append("\n\n")
                        val span =
                            when (node.tag) {
                                "h1" -> SpanStyle(fontSize = headlineSize, fontWeight = ComposeFontWeight.Bold)
                                "h2" -> SpanStyle(fontSize = titleSize, fontWeight = ComposeFontWeight.SemiBold)
                                "h3" -> SpanStyle(fontWeight = ComposeFontWeight.SemiBold)
                                else -> null
                            }
                        if (span == null) children() else builder.withStyle(span) { children() }
                        append("\n\n")
                    }

                    "ul", "ol" -> {
                        if (builder.length > 0 && lastChar != '\n') append('\n')
                        children()
                        if (lastChar != '\n') append('\n')
                    }

                    "li" -> {
                        if (builder.length > 0 && lastChar != '\n') append('\n')
                        append("• ")
                        children()
                        if (lastChar != '\n') append('\n')
                    }

                    "strong", "b" -> {
                        builder.withStyle(SpanStyle(fontWeight = ComposeFontWeight.Bold)) { children() }
                    }

                    "em", "i" -> {
                        builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { children() }
                    }

                    "code" -> {
                        builder.withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { children() }
                    }

                    "blockquote" -> {
                        builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { children() }
                    }

                    "a" -> {
                        val href = node.href
                        if (href == null) {
                            children()
                        } else {
                            val listener =
                                onLinkClick?.let { callback ->
                                    LinkInteractionListener { link ->
                                        (link as? LinkAnnotation.Url)?.let { callback(it.url) }
                                    }
                                }
                            builder.withLink(
                                LinkAnnotation.Url(
                                    href,
                                    TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline)),
                                    listener,
                                ),
                            ) { children() }
                        }
                    }

                    else -> {
                        children()
                    }
                }
            }
        }
    }
}

private fun DomposableFontWeight.nativeFontWeight(): ComposeFontWeight =
    when (this) {
        DomposableFontWeight.Thin -> ComposeFontWeight.Thin
        DomposableFontWeight.ExtraLight -> ComposeFontWeight.ExtraLight
        DomposableFontWeight.Light -> ComposeFontWeight.Light
        DomposableFontWeight.Normal -> ComposeFontWeight.Normal
        DomposableFontWeight.Medium -> ComposeFontWeight.Medium
        DomposableFontWeight.SemiBold -> ComposeFontWeight.SemiBold
        DomposableFontWeight.Bold -> ComposeFontWeight.Bold
        DomposableFontWeight.ExtraBold -> ComposeFontWeight.ExtraBold
        DomposableFontWeight.Black -> ComposeFontWeight.Black
    }

private fun DomposableTextAlignment.nativeTextAlign(): TextAlign =
    when (this) {
        DomposableTextAlignment.Start -> TextAlign.Start
        DomposableTextAlignment.Center -> TextAlign.Center
        DomposableTextAlignment.End -> TextAlign.End
        DomposableTextAlignment.Justify -> TextAlign.Justify
    }

private fun DomposableTextDecoration.nativeTextDecoration(): TextDecoration? =
    when (this) {
        DomposableTextDecoration.None -> TextDecoration.None
        DomposableTextDecoration.Underline -> TextDecoration.Underline
        DomposableTextDecoration.LineThrough -> TextDecoration.LineThrough
    }

private fun DomposableTextOverflow.nativeTextOverflow(): TextOverflow =
    when (this) {
        DomposableTextOverflow.Clip -> TextOverflow.Clip
        DomposableTextOverflow.Ellipsis -> TextOverflow.Ellipsis
        DomposableTextOverflow.Visible -> TextOverflow.Visible
    }
