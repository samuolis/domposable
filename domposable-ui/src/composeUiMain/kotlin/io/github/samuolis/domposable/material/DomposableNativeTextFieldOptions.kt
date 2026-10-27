package io.github.samuolis.domposable.material

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.VisualTransformation
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTextFieldProps

/** Native input integration beyond portable purpose/IME options; shared appearance remains authoritative. */
data class DomposableNativeTextFieldOptions(
    val keyboardOptions: KeyboardOptions? = null,
    val keyboardActions: KeyboardActions? = null,
    val visualTransformation: VisualTransformation? = null,
)

@Composable
fun DomposableTextField(
    props: DomposableTextFieldProps,
    nativeOptions: DomposableNativeTextFieldOptions,
    modifier: DomposableModifier = DomposableModifier,
) = NativeTextField(props, modifier, nativeOptions)
