package io.github.samuolis.domposable.material

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableTextOverflow
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.ui.DomposableButtonProps
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
import io.github.samuolis.domposable.ui.contentDescription as linkDescription

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
    val linkModifier = if (props.content == null) modifier else modifier.linkDescription(props.label)
    props.buttonAppearance?.let { buttonAppearance ->
        NativeButton(
            props =
                DomposableButtonProps(
                    onClick = { currentOnClick?.invoke() ?: uriHandler.openUri(props.href) },
                    style = props.buttonStyle,
                    appearance = buttonAppearance,
                ),
            modifier = linkModifier,
            content = { props.content?.invoke() ?: Text(props.label) },
        )
        return
    }
    // Keep the existing native link's touch target, typography and spacing for default calls.
    if (appearance == null && props.textStyle == DomposableTextStyle.Body) {
        TextButton(
            onClick = { currentOnClick?.invoke() ?: uriHandler.openUri(props.href) },
            modifier = linkModifier.toComposeModifier(),
        ) { props.content?.invoke() ?: Text(props.label) }
        return
    }
    val baseStyle =
        when (props.textStyle) {
            DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge
            DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge
            DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge
            DomposableTextStyle.Inherit -> LocalTextStyle.current
        }
    val style = appearance.nativeTextStyle(baseStyle)
    val customContent = props.content
    if (customContent != null) {
        androidx.compose.foundation.layout.Box(
            modifier =
                linkModifier
                    .toComposeModifier()
                    .clickable(role = Role.Button) {
                        currentOnClick?.invoke() ?: uriHandler.openUri(props.href)
                    }.semantics { contentDescription = props.label },
        ) {
            androidx.compose.material3.ProvideTextStyle(style) {
                CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides
                        (appearance?.color?.nativeColor() ?: Color(LocalDomposableTokens.current.colors.primary.argb)),
                ) { customContent() }
            }
        }
    } else {
        val text: @Composable () -> Unit = {
            Text(
                text = props.label,
                modifier =
                    linkModifier.toComposeModifier().clickable(role = Role.Button) {
                        currentOnClick?.invoke() ?: uriHandler.openUri(props.href)
                    },
                color = appearance?.color?.nativeColor() ?: Color(LocalDomposableTokens.current.colors.primary.argb),
                style = style,
                maxLines = appearance?.maxLines ?: Int.MAX_VALUE,
                overflow = appearance?.overflow?.nativeTextOverflow() ?: TextOverflow.Clip,
            )
        }
        if (appearance?.selectable == true) SelectionContainer { text() } else text()
    }
}

@Composable
internal fun NativeDomposableDisclosure(
    props: DomposableDisclosureProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.disclosure
    val disclosureContent: @Composable () -> Unit = {
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
                Text(
                    props.title,
                    style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.titleSmall),
                    maxLines = appearance.textAppearance?.maxLines ?: Int.MAX_VALUE,
                    overflow = appearance.textAppearance?.overflow?.nativeTextOverflow() ?: TextOverflow.Clip,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    if (props.expanded) "▴" else "▾",
                    modifier = Modifier.clearAndSetSemantics {},
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            props.description?.let {
                Text(
                    it,
                    style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.bodySmall),
                    maxLines = appearance.textAppearance?.maxLines ?: Int.MAX_VALUE,
                    overflow = appearance.textAppearance?.overflow?.nativeTextOverflow() ?: TextOverflow.Clip,
                )
            }
            if (props.expanded) content()
        }
    }
    if (appearance == DomposableSurfaceAppearance()) {
        Card(modifier = modifier.toComposeModifier()) { disclosureContent() }
    } else {
        Surface(
            modifier = modifier.toComposeModifier(),
            shape = appearance.nativeShape(MaterialTheme.shapes.medium),
            color = appearance.nativeContainer(MaterialTheme.colorScheme.surfaceContainerLow),
            contentColor = appearance.nativeContent(MaterialTheme.colorScheme.onSurface),
            tonalElevation = appearance.tonalElevation?.value?.dp ?: 1.dp,
            shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
            border = appearance.border?.let { BorderStroke(appearance.borderWidth.value.dp, it.nativeColor()) },
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides appearance.textAppearance.nativeTextStyle(LocalTextStyle.current),
                content = disclosureContent,
            )
        }
    }
}

@Composable
internal fun NativeDomposableRichHtml(
    props: DomposableRichHtmlProps,
    modifier: DomposableModifier,
) {
    val tokens = LocalDomposableTokens.current
    val appearance = props.appearance ?: tokens.components.text
    val currentOnLinkClick by rememberUpdatedState(props.onLinkClick)
    val parsed = remember(props.rawHtml) { parseDomposableRichHtml(props.rawHtml) }
    val baseStyle =
        appearance.nativeTextStyle(
            when (props.style) {
                DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge
                DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge
                DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge
                DomposableTextStyle.Inherit -> LocalTextStyle.current
            },
        )
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
        Text(
            annotated,
            modifier = textModifier,
            color = props.color?.nativeColor() ?: Color.Unspecified,
            style = baseStyle,
            maxLines = appearance.maxLines ?: Int.MAX_VALUE,
            overflow = appearance.overflow.nativeTextOverflow(),
        )
    }
    if (appearance.selectable ?: props.selectable) SelectionContainer { textContent() } else textContent()
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

private fun DomposableTextOverflow.nativeTextOverflow(): TextOverflow =
    when (this) {
        DomposableTextOverflow.Clip -> TextOverflow.Clip
        DomposableTextOverflow.Ellipsis -> TextOverflow.Ellipsis
        DomposableTextOverflow.Visible -> TextOverflow.Visible
    }
