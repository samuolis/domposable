package io.github.samuolis.domposable.foundation

import androidx.compose.runtime.Immutable

@Immutable
data class DomposableStateColors(
    val unfocused: DomposableColor? = null,
    val focused: DomposableColor? = null,
    val disabled: DomposableColor? = null,
    val error: DomposableColor? = null,
)

@Immutable
data class DomposableTextFieldColors(
    val text: DomposableStateColors = DomposableStateColors(),
    val container: DomposableStateColors = DomposableStateColors(),
    val label: DomposableStateColors = DomposableStateColors(),
    val placeholder: DomposableStateColors = DomposableStateColors(),
    val outline: DomposableStateColors = DomposableStateColors(),
    val cursor: DomposableStateColors = DomposableStateColors(),
    val leadingIcon: DomposableStateColors = DomposableStateColors(),
    val trailingIcon: DomposableStateColors = DomposableStateColors(),
    val supporting: DomposableStateColors = DomposableStateColors(),
)

@Immutable
data class DomposableTextFieldAppearance(
    val colors: DomposableTextFieldColors = DomposableTextFieldColors(),
    val shape: DomposableShape? = null,
    val textAppearance: DomposableTextAppearance? = null,
    val labelAppearance: DomposableTextAppearance? = null,
    val floatingLabelAppearance: DomposableTextAppearance? = null,
    val placeholderAppearance: DomposableTextAppearance? = null,
    val supportingAppearance: DomposableTextAppearance? = null,
    /** HTML inner geometry; native Material keeps its standard decoration padding. */
    val contentPadding: DomposableInsets? = null,
    val minHeight: DomposableLength? = null,
    /** HTML stroke widths; native Material uses its standard focused/unfocused strokes. */
    val outlineWidth: DomposableLength? = null,
    val focusedOutlineWidth: DomposableLength? = null,
    val floatingLabel: Boolean = false,
    val autoSize: Boolean = false,
    val variant: DomposableTextFieldVariant = DomposableTextFieldVariant.Outlined,
)

enum class DomposableTextFieldVariant {
    Outlined,
    Filled,
}
