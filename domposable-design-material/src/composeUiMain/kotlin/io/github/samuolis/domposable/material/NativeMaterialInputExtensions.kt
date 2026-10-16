@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CheckableDropdownMenuItem
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ScrollField
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberScrollFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableItemColors
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.ui.DomposableLabelProps
import io.github.samuolis.domposable.ui.DomposableMenuGroupProps
import io.github.samuolis.domposable.ui.DomposableMenuItem
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableScrollFieldProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableFormEnabled
import io.github.samuolis.domposable.ui.toComposeModifier
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop

@Composable
internal fun DomposableItemAppearance.nativeScrollFieldColors(): androidx.compose.material3.ScrollFieldColors =
    androidx.compose.material3.ScrollFieldDefaults.colors(
        containerColor = colors.container?.nativeColor() ?: MaterialTheme.colorScheme.surfaceContainer,
        contentColor = colors.content?.nativeColor() ?: MaterialTheme.colorScheme.outline,
        selectedContentColor = colors.selectedContent?.nativeColor() ?: MaterialTheme.colorScheme.onSurface,
        disabledContainerColor =
            colors.disabledContainer?.nativeColor() ?: MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.38f),
        disabledContentColor =
            colors.disabledContent?.nativeColor() ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        disabledSelectedContentColor =
            colors.disabledContent?.nativeColor() ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    )

@Composable
internal fun NativeScrollField(
    props: DomposableScrollFieldProps,
    modifier: DomposableModifier,
) {
    NativeWindow { featureRegistry -> NativeScrollFieldContent(props, modifier, featureRegistry) }
}

@Composable
private fun NativeScrollFieldContent(
    props: DomposableScrollFieldProps,
    modifier: DomposableModifier,
    featureRegistry: androidx.compose.runtime.saveable.SaveableStateRegistry?,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.scrollField
    val customItem = props.itemContent
    val state = key(props.itemCount) { rememberScrollFieldState(props.itemCount, props.selectedIndex) }
    val latestCallback by rememberUpdatedState(props.onSelectedIndexChange)
    val selectedIndex by rememberUpdatedState(props.selectedIndex)
    val enabled = props.enabled && domposableFormEnabled()
    LaunchedEffect(state) {
        snapshotFlow { state.selectedOption }.drop(1).collect { index ->
            if (index != selectedIndex) {
                latestCallback(index)
                withFrameNanos { }
                if (selectedIndex != index) state.selectedOption = selectedIndex
            }
        }
    }
    LaunchedEffect(props.selectedIndex, state) {
        if (state.selectedOption != props.selectedIndex) state.selectedOption = props.selectedIndex
    }
    val shape = appearance.shape?.nativeShape() ?: androidx.compose.material3.ScrollFieldDefaults.shape
    val baseModifier =
        modifier
            .toComposeModifier()
            .then(
                appearance.minimumInteractiveSize?.let {
                    Modifier
                        .heightIn(
                            min = it.value.dp,
                        ).widthIn(min = it.value.dp)
                }
                    ?: Modifier,
            ).then(appearance.minHeight?.let { Modifier.heightIn(min = it.value.dp) } ?: Modifier)
            .then(if (!enabled) appearance.disabledOpacity?.let { Modifier.alpha(it) } ?: Modifier else Modifier)
            .clip(shape)
            .then(
                appearance.colors.border?.let {
                    Modifier.border(BorderStroke(appearance.borderWidth.value.dp, it.nativeColor()), shape)
                } ?: Modifier,
            )
    val colors = appearance.nativeScrollFieldColors()
    ScrollField(
        state = state,
        contentDescription = props.label,
        modifier = baseModifier,
        enabled = enabled,
        colors = colors,
        fieldAccessibilityDescription = props.indexDescription,
        field = { index, selected, enabled ->
            val itemColor =
                when {
                    !enabled -> {
                        appearance.colors.disabledContent?.nativeColor()
                            ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }

                    selected -> {
                        appearance.colors.selectedContent?.nativeColor() ?: MaterialTheme.colorScheme.onSurface
                    }

                    else -> {
                        appearance.colors.content?.nativeColor() ?: MaterialTheme.colorScheme.outline
                    }
                }
            val itemBackground =
                when {
                    !enabled -> appearance.colors.disabledContainer?.nativeColor()
                    selected -> appearance.colors.selectedContainer?.nativeColor()
                    else -> appearance.colors.container?.nativeColor()
                }
            val itemBorder =
                when {
                    !enabled -> appearance.colors.disabledBorder
                    selected -> appearance.colors.selectedBorder ?: appearance.colors.border
                    else -> appearance.colors.border
                }
            val itemShape = appearance.shape?.nativeShape() ?: RectangleShape
            Box(
                Modifier
                    .fillMaxSize()
                    .then(itemBackground?.let { Modifier.background(it, itemShape) } ?: Modifier)
                    .then(
                        itemBorder?.let {
                            Modifier.border(
                                BorderStroke(
                                    (
                                        if (selected) {
                                            appearance.selectedBorderWidth ?: appearance.borderWidth
                                        } else {
                                            appearance.borderWidth
                                        }
                                    ).value.dp,
                                    it.nativeColor(),
                                ),
                                itemShape,
                            )
                        } ?: Modifier,
                    ).then(appearance.contentPadding?.let { Modifier.padding(it.nativePadding()) } ?: Modifier),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                val itemTextAppearance =
                    if (selected) {
                        appearance.selectedTextAppearance ?: appearance.textAppearance
                    } else {
                        appearance.textAppearance
                    }
                val textStyle =
                    itemTextAppearance.nativeTextStyle(
                        if (selected) {
                            MaterialTheme.typography.displayLargeEmphasized
                        } else {
                            MaterialTheme.typography.displayMedium
                        },
                    )
                if (customItem != null) {
                    CompositionLocalProvider(
                        LocalSaveableStateRegistry provides featureRegistry,
                        androidx.compose.material3.LocalTextStyle provides textStyle,
                        androidx.compose.material3.LocalContentColor provides itemColor,
                    ) { customItem.invoke(index, selected, enabled) }
                } else if (itemTextAppearance == null) {
                    androidx.compose.material3.ScrollFieldDefaults
                        .Item(index, selected, enabled, colors)
                } else {
                    Text(index.toString().padStart(2, '0'), style = textStyle, color = itemColor)
                }
            }
        },
    )
}

@Composable
internal fun NativeLabel(
    props: DomposableLabelProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.label
    val interactionSource = remember { MutableInteractionSource() }
    NativeWindow { featureRegistry ->
        Label(
            label = {
                CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) {
                    if (appearance.isEmpty()) {
                        props.label()
                    } else {
                        Surface(
                            modifier = Modifier.nativeModifier(appearance, MaterialTheme.shapes.extraSmall),
                            shape = appearance.nativeShape(MaterialTheme.shapes.extraSmall),
                            color = appearance.nativeContainer(MaterialTheme.colorScheme.inverseSurface),
                            contentColor = appearance.nativeContent(MaterialTheme.colorScheme.inverseOnSurface),
                            tonalElevation = appearance.tonalElevation?.value?.dp ?: 0.dp,
                            shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
                            border =
                                appearance.border?.let {
                                    androidx.compose.foundation.BorderStroke(
                                        appearance.borderWidth.value.dp,
                                        it.nativeColor(),
                                    )
                                },
                        ) { StyledContent(appearance.textAppearance, props.label) }
                    }
                }
            },
            modifier =
                modifier
                    .toComposeModifier()
                    .hoverable(interactionSource)
                    .observePressInteractions(interactionSource),
            interactionSource = interactionSource,
            isPersistent = props.persistent,
            content = {
                CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) { content() }
            },
        )
    }
}

private fun Modifier.observePressInteractions(interactionSource: MutableInteractionSource): Modifier =
    pointerInput(interactionSource) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val press = PressInteraction.Press(down.position)
            interactionSource.tryEmit(press)
            var released = false
            try {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == down.id }
                    if (change == null || !change.pressed) {
                        released = true
                        break
                    }
                }
            } finally {
                interactionSource.tryEmit(
                    if (released) PressInteraction.Release(press) else PressInteraction.Cancel(press),
                )
            }
        }
    }

@Composable
internal fun NativeMenuGroup(
    props: DomposableMenuGroupProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.menuGroup
    DropdownMenuGroup(
        shapes = MenuDefaults.groupShapes(shape = appearance.shape?.nativeShape()),
        modifier = modifier.toComposeModifier(),
        containerColor = appearance.container?.nativeColor() ?: MenuDefaults.groupStandardContainerColor,
        tonalElevation = appearance.tonalElevation?.value?.dp ?: MenuDefaults.TonalElevation,
        shadowElevation = appearance.shadowElevation?.value?.dp ?: MenuDefaults.ShadowElevation,
        border =
            appearance.border?.let {
                androidx.compose.foundation.BorderStroke(appearance.borderWidth.value.dp, it.nativeColor())
            },
        content = { content() },
    )
}

@Composable
internal fun NativeDropdownMenuItem(
    props: DomposableMenuItem,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.dropdownMenuItem
    val enabled = props.enabled && domposableFormEnabled()
    val selectedIcon = props.selectedIcon ?: props.leadingIcon
    val checked = props.checked
    val selected = props.selected
    val interactionSource = remember { MutableInteractionSource() }
    val resolvedColors =
        appearance.resolveInteractionColors(
            interactionSource,
            selected = checked ?: selected ?: false,
            enabled = enabled,
        )
    val borderColor = resolvedColors.border
    val textAppearance =
        if (selected ==
            true
        ) {
            appearance.selectedTextAppearance ?: appearance.textAppearance
        } else {
            appearance.textAppearance
        }
    val text: @Composable () -> Unit = { StyledContent(textAppearance, { Text(props.label) }) }
    val supporting: (@Composable () -> Unit)? =
        props.supportingText?.let { slot ->
            { StyledContent(appearance.textAppearance, slot) }
        }
    val trailing: (@Composable () -> Unit)? =
        props.trailingContent ?: if (props.checked == true || props.selected == true) selectedIcon else null
    val shape = appearance.shape?.nativeShape() ?: MenuDefaults.middleItemShape
    val itemModifier =
        modifier
            .toComposeModifier()
            .then(appearance.minHeight?.let { Modifier.heightIn(min = it.value.dp) } ?: Modifier)
            .then(
                appearance.minimumInteractiveSize?.let {
                    Modifier
                        .heightIn(
                            min = it.value.dp,
                        ).widthIn(min = it.value.dp)
                }
                    ?: Modifier,
            ).then(if (!enabled) appearance.disabledOpacity?.let { Modifier.alpha(it) } ?: Modifier else Modifier)
            .then(
                if (borderColor == Color.Unspecified) {
                    Modifier
                } else {
                    val width =
                        if (selected == true ||
                            checked == true
                        ) {
                            appearance.selectedBorderWidth ?: appearance.borderWidth
                        } else {
                            appearance.borderWidth
                        }
                    Modifier.clip(shape).border(BorderStroke(width.value.dp, borderColor), shape)
                },
            )
    val menuColors =
        MenuDefaults.selectableItemColors(
            textColor = resolvedColors.content,
            containerColor = resolvedColors.container,
            leadingIconColor = resolvedColors.content,
            trailingContentColor = resolvedColors.content,
            disabledTextColor = appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified,
            disabledContainerColor = appearance.colors.disabledContainer?.nativeColor() ?: Color.Unspecified,
            disabledLeadingIconColor = appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified,
            disabledTrailingContentColor = appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified,
            selectedTextColor = resolvedColors.selectedContent,
            selectedContainerColor = resolvedColors.selectedContainer,
            selectedLeadingIconColor = resolvedColors.selectedContent,
            selectedTrailingContentColor = resolvedColors.selectedContent,
        )
    val regularMenuColors =
        MenuDefaults.itemColors().copy(
            textColor = resolvedColors.content,
            leadingIconColor = resolvedColors.content,
            trailingIconColor = resolvedColors.content,
            disabledTextColor = appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified,
            disabledLeadingIconColor = appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified,
            disabledTrailingIconColor = appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified,
            containerColor = resolvedColors.container,
            disabledContainerColor = appearance.colors.disabledContainer?.nativeColor() ?: Color.Unspecified,
        )
    when {
        checked != null -> {
            CheckableDropdownMenuItem(
                checked = checked,
                onCheckedChange = { value -> if (value != checked) props.onClick() },
                text = text,
                shapes = MenuDefaults.itemShapes(shape = shape),
                modifier = itemModifier,
                leadingIcon = props.leadingIcon,
                checkedLeadingIcon = selectedIcon,
                trailingContent = trailing,
                supportingText = supporting,
                enabled = enabled,
                colors = menuColors,
                interactionSource = interactionSource,
                horizontalArrangement = Arrangement.spacedBy(appearance.contentSpacing?.value?.dp ?: 0.dp),
                contentPadding =
                    appearance.contentPadding?.nativePadding() ?: MenuDefaults.DropdownMenuSelectableItemContentPadding,
            )
        }

        selected != null -> {
            SelectableDropdownMenuItem(
                selected = selected,
                onClick = props.onClick,
                text = text,
                shapes = MenuDefaults.itemShapes(shape = shape),
                modifier = itemModifier,
                leadingIcon = props.leadingIcon,
                selectedLeadingIcon = selectedIcon,
                trailingContent = trailing,
                supportingText = supporting,
                enabled = enabled,
                colors = menuColors,
                interactionSource = interactionSource,
                horizontalArrangement = Arrangement.spacedBy(appearance.contentSpacing?.value?.dp ?: 0.dp),
                contentPadding =
                    appearance.contentPadding?.nativePadding() ?: MenuDefaults.DropdownMenuSelectableItemContentPadding,
            )
        }

        else -> {
            androidx.compose.material3.DropdownMenuItem(
                text = text,
                onClick = props.onClick,
                shape = shape,
                modifier = itemModifier,
                leadingIcon = props.leadingIcon,
                trailingContent = props.trailingContent,
                supportingText = supporting,
                enabled = enabled,
                colors = regularMenuColors,
                interactionSource = interactionSource,
                contentPadding =
                    appearance.contentPadding?.nativePadding() ?: MenuDefaults.DropdownMenuItemContentPadding,
            )
        }
    }
}
