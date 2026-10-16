@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableImeAction
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableStateColors
import io.github.samuolis.domposable.foundation.DomposableTextAlignment
import io.github.samuolis.domposable.foundation.DomposableTextDecoration
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldColors
import io.github.samuolis.domposable.foundation.DomposableTextFieldVariant
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTextFieldProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableFormEnabled
import io.github.samuolis.domposable.ui.rememberDomposableFormSubmitOrNull
import io.github.samuolis.domposable.ui.toComposeModifier
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop

@Composable
internal fun NativeTextField(
    props: DomposableTextFieldProps,
    modifier: DomposableModifier,
) {
    val focusRequester = remember { FocusRequester() }
    val formEnabled = domposableFormEnabled()
    val shouldAutofocus = props.autofocus && props.enabled && formEnabled
    val submit = props.onImeAction ?: rememberDomposableFormSubmitOrNull()
    LaunchedEffect(shouldAutofocus) {
        if (shouldAutofocus) focusRequester.requestFocus()
    }
    val onClick = props.onClick
    val interactions = remember { MutableInteractionSource() }
    val latestClick by rememberUpdatedState(onClick)
    LaunchedEffect(interactions) {
        interactions.interactions.collect { if (it is PressInteraction.Release) latestClick?.invoke() }
    }
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.textField
    val nativeModifier =
        modifier
            .toComposeModifier()
            .then(appearance.minHeight?.let { Modifier.heightIn(min = it.value.dp) } ?: Modifier)
            .focusRequester(focusRequester)
            .onFocusChanged { props.onFocusChange?.invoke(it.isFocused) }
            .semantics {
                if (onClick != null && props.enabled && formEnabled) {
                    onClick(action = {
                        focusRequester.requestFocus()
                        onClick()
                        true
                    })
                }
                props.id?.let { testTag = it }
                if (!props.showLabel) contentDescription = props.label
                if (props.isError) props.error?.let { error(it) }
            }
    val supporting = props.error ?: props.helperText
    val defaultColors =
        if (appearance.variant == DomposableTextFieldVariant.Filled) {
            TextFieldDefaults.colors()
        } else {
            OutlinedTextFieldDefaults.colors()
        }
    val colors = appearance.nativeColors(defaultColors)
    val shape =
        appearance.shape?.materialShape() ?: if (appearance.variant == DomposableTextFieldVariant.Filled) {
            TextFieldDefaults.shape
        } else {
            OutlinedTextFieldDefaults.shape
        }
    val textAppearance = appearance.textAppearance
    val baseTextStyle = textAppearance.materialTextStyle(LocalTextStyle.current)
    val textStyle =
        baseTextStyle.copy(
            textAlign =
                when (textAppearance?.alignment) {
                    DomposableTextAlignment.Start -> androidx.compose.ui.text.style.TextAlign.Start
                    DomposableTextAlignment.Center -> androidx.compose.ui.text.style.TextAlign.Center
                    DomposableTextAlignment.End -> androidx.compose.ui.text.style.TextAlign.End
                    DomposableTextAlignment.Justify -> androidx.compose.ui.text.style.TextAlign.Justify
                    null -> baseTextStyle.textAlign
                },
            textDecoration =
                when (textAppearance?.decoration) {
                    DomposableTextDecoration.None -> androidx.compose.ui.text.style.TextDecoration.None
                    DomposableTextDecoration.Underline -> androidx.compose.ui.text.style.TextDecoration.Underline
                    DomposableTextDecoration.LineThrough -> androidx.compose.ui.text.style.TextDecoration.LineThrough
                    null -> baseTextStyle.textDecoration
                },
        )
    val onValueChange: (String) -> Unit = props.onValueChange
    val enabled = props.enabled && formEnabled
    val label: (@Composable () -> Unit)? =
        if (props.showLabel) {
            {
                val style = appearance.labelAppearance
                if (style ==
                    null
                ) {
                    Text(props.label)
                } else {
                    Text(props.label, style = style.materialTextStyle(LocalTextStyle.current))
                }
            }
        } else {
            null
        }
    val placeholder: (@Composable () -> Unit)? =
        props.placeholder?.let { value ->
            {
                val style = appearance.placeholderAppearance
                if (style == null) Text(value) else Text(value, style = style.materialTextStyle(LocalTextStyle.current))
            }
        }
    val leadingIcon = props.leadingIcon
    val trailingIcon = props.trailingIcon
    val supportingText: (@Composable () -> Unit)? =
        supporting?.let { message ->
            {
                val style = appearance.supportingAppearance
                if (style ==
                    null
                ) {
                    Text(message)
                } else {
                    Text(message, style = style.materialTextStyle(LocalTextStyle.current))
                }
            }
        }
    val keyboardOptions =
        KeyboardOptions(
            keyboardType =
                when (props.purpose) {
                    DomposableInputPurpose.Email -> KeyboardType.Email
                    DomposableInputPurpose.Password -> KeyboardType.Password
                    DomposableInputPurpose.Number, DomposableInputPurpose.OneTimeCode -> KeyboardType.Number
                    DomposableInputPurpose.Phone -> KeyboardType.Phone
                    DomposableInputPurpose.Decimal -> KeyboardType.Decimal
                    else -> KeyboardType.Text
                },
            imeAction = props.imeAction.native(),
        )
    val keyboardActions =
        if (submit == null) {
            KeyboardActions.Default
        } else {
            KeyboardActions(
                onDone = { submit() },
                onGo = { submit() },
                onSearch = { submit() },
                onSend = { submit() },
            )
        }
    val visualTransformation =
        if (props.purpose == DomposableInputPurpose.Password && !props.passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        }
    val customNativeDecoration =
        appearance.contentPadding != null || appearance.outlineWidth != null || appearance.focusedOutlineWidth != null
    val securePassword =
        props.purpose == DomposableInputPurpose.Password && props.singleLine && !props.readOnly &&
            !customNativeDecoration
    if (securePassword) {
        val secureState = rememberTextFieldState(props.value)
        val currentValue by rememberUpdatedState(props.value)
        val currentChange by rememberUpdatedState(props.onValueChange)
        LaunchedEffect(secureState) {
            snapshotFlow { secureState.text.toString() }.drop(1).collect { value ->
                if (value !=
                    currentValue
                ) {
                    currentChange(value)
                }
            }
        }
        LaunchedEffect(props.value, secureState) {
            if (secureState.text.toString() != props.value) {
                secureState.edit { replace(0, length, props.value) }
            }
        }
        val secureModifier = nativeModifier
        val labelSlot: @Composable androidx.compose.material3.TextFieldLabelScope.() -> Unit = {
            if (label !=
                null
            ) {
                label()
            }
        }
        val secureKeyboardAction =
            if (submit != null &&
                props.imeAction in
                setOf(
                    DomposableImeAction.Done,
                    DomposableImeAction.Go,
                    DomposableImeAction.Search,
                    DomposableImeAction.Send,
                )
            ) {
                KeyboardActionHandler { submit() }
            } else {
                null
            }
        if (appearance.variant == DomposableTextFieldVariant.Filled) {
            SecureTextField(
                state = secureState,
                modifier = secureModifier,
                enabled = enabled,
                textStyle = textStyle,
                label = if (label == null) null else labelSlot,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = props.prefix,
                supportingText = supportingText,
                isError = props.isError,
                textObfuscationMode =
                    if (props.passwordVisible) TextObfuscationMode.Visible else TextObfuscationMode.System,
                keyboardOptions = keyboardOptions,
                onKeyboardAction = secureKeyboardAction,
                shape = shape,
                colors = colors,
                contentPadding =
                    appearance.contentPadding?.nativePadding()
                        ?: if (label ==
                            null
                        ) {
                            TextFieldDefaults.contentPaddingWithoutLabel()
                        } else {
                            TextFieldDefaults.contentPaddingWithLabel()
                        },
                interactionSource = interactions,
            )
        } else {
            OutlinedSecureTextField(
                state = secureState,
                modifier = secureModifier,
                enabled = enabled,
                textStyle = textStyle,
                label = if (label == null) null else labelSlot,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = props.prefix,
                supportingText = supportingText,
                isError = props.isError,
                textObfuscationMode =
                    if (props.passwordVisible) TextObfuscationMode.Visible else TextObfuscationMode.System,
                keyboardOptions = keyboardOptions,
                onKeyboardAction = secureKeyboardAction,
                shape = shape,
                colors = colors,
                contentPadding =
                    appearance.contentPadding?.nativePadding()
                        ?: if (label ==
                            null
                        ) {
                            OutlinedTextFieldDefaults.contentPaddingWithoutLabel()
                        } else {
                            OutlinedTextFieldDefaults.contentPaddingWithLabel()
                        },
                interactionSource = interactions,
            )
        }
    } else if (customNativeDecoration) {
        val minWidth =
            if (appearance.variant ==
                DomposableTextFieldVariant.Filled
            ) {
                TextFieldDefaults.MinWidth
            } else {
                OutlinedTextFieldDefaults.MinWidth
            }
        val minHeight =
            if (appearance.variant ==
                DomposableTextFieldVariant.Filled
            ) {
                TextFieldDefaults.MinHeight
            } else {
                OutlinedTextFieldDefaults.MinHeight
            }
        val defaultPadding =
            if (appearance.variant == DomposableTextFieldVariant.Filled) {
                if (label ==
                    null
                ) {
                    TextFieldDefaults.contentPaddingWithoutLabel()
                } else {
                    TextFieldDefaults.contentPaddingWithLabel()
                }
            } else {
                if (label ==
                    null
                ) {
                    OutlinedTextFieldDefaults.contentPaddingWithoutLabel()
                } else {
                    OutlinedTextFieldDefaults.contentPaddingWithLabel()
                }
            }
        val contentPadding = appearance.contentPadding?.nativePadding() ?: defaultPadding
        val focusedThickness = appearance.focusedOutlineWidth?.value?.dp
        val unfocusedThickness = appearance.outlineWidth?.value?.dp
        BasicTextField(
            value = props.value,
            onValueChange = props.onValueChange,
            modifier = nativeModifier.defaultMinSize(minWidth = minWidth, minHeight = minHeight),
            enabled = enabled,
            readOnly = props.readOnly,
            textStyle = textStyle,
            cursorBrush = SolidColor(if (props.isError) colors.errorCursorColor else colors.cursorColor),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = props.singleLine,
            minLines = props.minLines,
            maxLines = props.maxLines,
            visualTransformation = visualTransformation,
            interactionSource = interactions,
            decorationBox = { innerTextField ->
                if (appearance.variant == DomposableTextFieldVariant.Filled) {
                    TextFieldDefaults.DecorationBox(
                        value = props.value,
                        innerTextField = innerTextField,
                        enabled = enabled,
                        singleLine = props.singleLine,
                        visualTransformation = visualTransformation,
                        interactionSource = interactions,
                        isError = props.isError,
                        label = label,
                        placeholder = placeholder,
                        leadingIcon = leadingIcon,
                        trailingIcon = trailingIcon,
                        prefix = props.prefix,
                        supportingText = supportingText,
                        colors = colors,
                        contentPadding = contentPadding,
                        container = {
                            TextFieldDefaults.Container(
                                enabled = enabled,
                                isError = props.isError,
                                interactionSource = interactions,
                                colors = colors,
                                shape = shape,
                                focusedIndicatorLineThickness =
                                    focusedThickness ?: TextFieldDefaults.FocusedIndicatorThickness,
                                unfocusedIndicatorLineThickness =
                                    unfocusedThickness ?: TextFieldDefaults.UnfocusedIndicatorThickness,
                            )
                        },
                    )
                } else {
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = props.value,
                        innerTextField = innerTextField,
                        enabled = enabled,
                        singleLine = props.singleLine,
                        visualTransformation = visualTransformation,
                        interactionSource = interactions,
                        isError = props.isError,
                        label = label,
                        placeholder = placeholder,
                        leadingIcon = leadingIcon,
                        trailingIcon = trailingIcon,
                        prefix = props.prefix,
                        supportingText = supportingText,
                        colors = colors,
                        contentPadding = contentPadding,
                        container = {
                            OutlinedTextFieldDefaults.Container(
                                enabled = enabled,
                                isError = props.isError,
                                interactionSource = interactions,
                                colors = colors,
                                shape = shape,
                                focusedBorderThickness =
                                    focusedThickness ?: OutlinedTextFieldDefaults.FocusedBorderThickness,
                                unfocusedBorderThickness =
                                    unfocusedThickness ?: OutlinedTextFieldDefaults.UnfocusedBorderThickness,
                            )
                        },
                    )
                }
            },
        )
    } else if (appearance.variant == DomposableTextFieldVariant.Filled) {
        TextField(
            value = props.value,
            onValueChange = onValueChange,
            modifier = nativeModifier,
            enabled = enabled,
            readOnly = props.readOnly,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            prefix = props.prefix,
            trailingIcon = trailingIcon,
            singleLine = props.singleLine,
            minLines = props.minLines,
            maxLines = props.maxLines,
            isError = props.isError,
            supportingText = supportingText,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            shape = shape,
            colors = colors,
            interactionSource = interactions,
        )
    } else {
        OutlinedTextField(
            value = props.value,
            onValueChange = onValueChange,
            modifier = nativeModifier,
            enabled = enabled,
            readOnly = props.readOnly,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            prefix = props.prefix,
            trailingIcon = trailingIcon,
            singleLine = props.singleLine,
            minLines = props.minLines,
            maxLines = props.maxLines,
            isError = props.isError,
            supportingText = supportingText,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            shape = shape,
            colors = colors,
            interactionSource = interactions,
        )
    }
}

@Composable
internal fun DomposableTextFieldAppearance.nativeColors(defaults: TextFieldColors): TextFieldColors =
    colors.nativeColors(defaults)

private fun DomposableTextFieldColors.nativeColors(defaults: TextFieldColors): TextFieldColors =
    defaults.copy(
        focusedTextColor = text.focused.native(defaults.focusedTextColor),
        unfocusedTextColor = text.unfocused.native(defaults.unfocusedTextColor),
        disabledTextColor = text.disabled.native(defaults.disabledTextColor),
        errorTextColor = text.error.native(defaults.errorTextColor),
        focusedContainerColor = container.focused.native(defaults.focusedContainerColor),
        unfocusedContainerColor = container.unfocused.native(defaults.unfocusedContainerColor),
        disabledContainerColor = container.disabled.native(defaults.disabledContainerColor),
        errorContainerColor = container.error.native(defaults.errorContainerColor),
        focusedIndicatorColor = outline.focused.native(defaults.focusedIndicatorColor),
        unfocusedIndicatorColor = outline.unfocused.native(defaults.unfocusedIndicatorColor),
        disabledIndicatorColor = outline.disabled.native(defaults.disabledIndicatorColor),
        errorIndicatorColor = outline.error.native(defaults.errorIndicatorColor),
        focusedLabelColor = label.focused.native(defaults.focusedLabelColor),
        unfocusedLabelColor = label.unfocused.native(defaults.unfocusedLabelColor),
        disabledLabelColor = label.disabled.native(defaults.disabledLabelColor),
        errorLabelColor = label.error.native(defaults.errorLabelColor),
        focusedPlaceholderColor = placeholder.focused.native(defaults.focusedPlaceholderColor),
        unfocusedPlaceholderColor = placeholder.unfocused.native(defaults.unfocusedPlaceholderColor),
        disabledPlaceholderColor = placeholder.disabled.native(defaults.disabledPlaceholderColor),
        errorPlaceholderColor = placeholder.error.native(defaults.errorPlaceholderColor),
        focusedLeadingIconColor = leadingIcon.focused.native(defaults.focusedLeadingIconColor),
        unfocusedLeadingIconColor = leadingIcon.unfocused.native(defaults.unfocusedLeadingIconColor),
        disabledLeadingIconColor = leadingIcon.disabled.native(defaults.disabledLeadingIconColor),
        errorLeadingIconColor = leadingIcon.error.native(defaults.errorLeadingIconColor),
        focusedTrailingIconColor = trailingIcon.focused.native(defaults.focusedTrailingIconColor),
        unfocusedTrailingIconColor = trailingIcon.unfocused.native(defaults.unfocusedTrailingIconColor),
        disabledTrailingIconColor = trailingIcon.disabled.native(defaults.disabledTrailingIconColor),
        errorTrailingIconColor = trailingIcon.error.native(defaults.errorTrailingIconColor),
        focusedSupportingTextColor = supporting.focused.native(defaults.focusedSupportingTextColor),
        unfocusedSupportingTextColor = supporting.unfocused.native(defaults.unfocusedSupportingTextColor),
        disabledSupportingTextColor = supporting.disabled.native(defaults.disabledSupportingTextColor),
        errorSupportingTextColor = supporting.error.native(defaults.errorSupportingTextColor),
        cursorColor = cursor.focused.native(defaults.cursorColor),
        errorCursorColor = cursor.error.native(defaults.errorCursorColor),
    )

private fun DomposableColor?.native(fallback: Color): Color = this?.let { Color(it.argb) } ?: fallback
