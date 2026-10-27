package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableTextAlignment
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableButtonProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTheme

private val LocalCustomDemoButtons = staticCompositionLocalOf { false }

@Composable
internal fun DemoTheme(
    customButtons: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalCustomDemoButtons provides customButtons) {
        DomposableTheme(DemoTokens, content = content)
    }
}

@Composable
internal fun DemoButton(
    props: DomposableButtonProps,
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit = {},
) {
    if (LocalCustomDemoButtons.current) {
        CustomDemoButton(props, modifier, content)
    } else {
        DomposableButton(props, modifier, content)
    }
}

@Composable
internal fun DemoButton(
    onClick: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    style: DomposableButtonStyle = DomposableButtonStyle.Filled,
    content: @Composable () -> Unit,
) = DemoButton(DomposableButtonProps(onClick, enabled, style), modifier, content)

@Composable
internal fun DemoButton(
    text: String,
    onClick: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
) {
    if (!LocalCustomDemoButtons.current) {
        DomposableButton(text, onClick, modifier, enabled)
        return
    }
    val button = DomposableTheme.styles.button
    val textAppearance = button.textAppearance ?: DomposableTheme.styles.text
    val alignment =
        textAppearance.alignment ?: when (button.contentAlignment) {
            DomposableAlignment.Start -> DomposableTextAlignment.Start
            DomposableAlignment.Center -> DomposableTextAlignment.Center
            DomposableAlignment.End -> DomposableTextAlignment.End
        }
    DemoButton(DomposableButtonProps(onClick, enabled), modifier) {
        DomposableText(text, appearance = textAppearance.copy(alignment = alignment))
    }
}
