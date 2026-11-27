package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

internal data class TextFieldDecorations(
    val leading: Boolean = false,
    val trailing: Boolean = false,
)

internal val LocalTextFieldDecorations = staticCompositionLocalOf { TextFieldDecorations() }

/** Marks text field icon slots that are decorative rather than independent actions. */
@Composable
fun DomposableTextFieldDecorations(
    leading: Boolean = false,
    trailing: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalTextFieldDecorations provides TextFieldDecorations(leading, trailing),
        content = content,
    )
}
