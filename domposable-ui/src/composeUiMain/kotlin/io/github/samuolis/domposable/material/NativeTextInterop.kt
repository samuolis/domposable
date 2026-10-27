package io.github.samuolis.domposable.material

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.toComposeModifier

/** Native rich text interoperability, retaining links, spans and native layout measurements. */
@Composable
fun DomposableText(
    text: AnnotatedString,
    modifier: DomposableModifier = DomposableModifier,
    style: DomposableTextStyle = DomposableTextStyle.Body,
    color: DomposableColor? = null,
    headingLevel: Int? = null,
    appearance: DomposableTextAppearance? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val resolved =
        appearance ?: LocalMaterialButtonTextAppearance.current
            ?: LocalDomposableTokens.current.components.text
    val base =
        when (style) {
            DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge
            DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge
            DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge
            DomposableTextStyle.Inherit -> LocalTextStyle.current
        }
    val nativeModifier =
        modifier.toComposeModifier().semantics {
            if (headingLevel != null || style == DomposableTextStyle.Title) heading()
            resolved.liveRegion?.let { liveRegion = it.native() }
        }
    val render: @Composable () -> Unit = {
        Text(
            text = text,
            modifier = nativeModifier,
            color = color?.nativeColor() ?: Color.Unspecified,
            style = resolved.nativeTextStyle(base),
            minLines = resolved.minLines,
            maxLines = resolved.maxLines ?: Int.MAX_VALUE,
            overflow = resolved.overflow.nativeOverflow(),
            onTextLayout = onTextLayout,
        )
    }
    if (resolved.selectable == true) SelectionContainer { render() } else render()
}
