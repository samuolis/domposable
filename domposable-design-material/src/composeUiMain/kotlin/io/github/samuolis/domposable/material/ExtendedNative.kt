@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TwoRowsTopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableFabSize
import io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant
import io.github.samuolis.domposable.ui.DomposableFloatingActionButtonProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRangeSliderProps
import io.github.samuolis.domposable.ui.DomposableSegmentedButtonsProps
import io.github.samuolis.domposable.ui.DomposableSelectProps
import io.github.samuolis.domposable.ui.DomposableSnackbarProps
import io.github.samuolis.domposable.ui.DomposableTooltipProps
import io.github.samuolis.domposable.ui.DomposableTopAppBarProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableFormEnabled
import io.github.samuolis.domposable.ui.toComposeModifier

@Composable
internal fun NativeTopAppBar(
    props: DomposableTopAppBarProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.topAppBar
    val container = appearance.container?.nativeColor() ?: Color.Unspecified
    val content = appearance.content?.nativeColor() ?: Color.Unspecified
    val colors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = container,
            scrolledContainerColor = container,
            navigationIconContentColor = content,
            titleContentColor = content,
            actionIconContentColor = content,
        )
    val baseModifier =
        modifier.toComposeModifier().then(
            Modifier.nativeModifier(appearance, MaterialTheme.shapes.small),
        )
    val title: @Composable () -> Unit = {
        Text(props.title, style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.titleLarge))
    }
    when (props.variant) {
        DomposableTopAppBarVariant.Small -> {
            TopAppBar(title, baseModifier, props.navigationIcon, { props.actions() }, colors = colors)
        }

        DomposableTopAppBarVariant.CenterAligned -> {
            CenterAlignedTopAppBar(title, baseModifier, props.navigationIcon, { props.actions() }, colors = colors)
        }

        DomposableTopAppBarVariant.Medium -> {
            MediumTopAppBar(title, baseModifier, props.navigationIcon, { props.actions() }, colors = colors)
        }

        DomposableTopAppBarVariant.Large -> {
            LargeTopAppBar(title, baseModifier, props.navigationIcon, { props.actions() }, colors = colors)
        }

        DomposableTopAppBarVariant.MediumFlexible -> {
            MediumFlexibleTopAppBar(title, baseModifier, props.navigationIcon, { props.actions() }, colors = colors)
        }

        DomposableTopAppBarVariant.LargeFlexible -> {
            LargeFlexibleTopAppBar(title, baseModifier, props.navigationIcon, { props.actions() }, colors = colors)
        }

        DomposableTopAppBarVariant.TwoRows -> {
            TwoRowsTopAppBar(
                title = { expanded ->
                    Text(
                        props.title,
                        style =
                            appearance.textAppearance.nativeTextStyle(
                                if (expanded) {
                                    MaterialTheme.typography.titleLarge
                                } else {
                                    MaterialTheme.typography.titleMedium
                                },
                            ),
                    )
                },
                subtitle =
                    props.subtitle?.let { subtitle ->
                        { _ -> Text(subtitle, style = MaterialTheme.typography.bodyMedium) }
                    },
                modifier = baseModifier,
                navigationIcon = props.navigationIcon,
                actions = { props.actions() },
                colors = colors,
            )
        }
    }
}

@Composable
internal fun NativeFloatingActionButton(
    props: DomposableFloatingActionButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.floatingActionButton
    val background = appearance.container?.nativeColor() ?: FloatingActionButtonDefaults.containerColor
    val foreground = appearance.content?.nativeColor() ?: contentColorFor(background)
    val nativeModifier =
        modifier
            .toComposeModifier()
            .then(Modifier.nativeModifier(appearance, FloatingActionButtonDefaults.shape))
            .semantics { contentDescription = props.label }
    val elevation =
        appearance.shadowElevation?.let { FloatingActionButtonDefaults.elevation(defaultElevation = it.value.dp) }
            ?: FloatingActionButtonDefaults.elevation()
    val button: @Composable () -> Unit = {
        when (props.size) {
            DomposableFabSize.Small -> {
                SmallFloatingActionButton(
                    props.onClick,
                    nativeModifier,
                    appearance.nativeShape(FloatingActionButtonDefaults.smallShape),
                    background,
                    foreground,
                    elevation,
                ) {
                    content()
                }
            }

            DomposableFabSize.Medium -> {
                MediumFloatingActionButton(
                    props.onClick,
                    nativeModifier,
                    appearance.nativeShape(FloatingActionButtonDefaults.mediumShape),
                    background,
                    foreground,
                    elevation,
                ) {
                    content()
                }
            }

            DomposableFabSize.Standard -> {
                FloatingActionButton(
                    props.onClick,
                    nativeModifier,
                    appearance.nativeShape(FloatingActionButtonDefaults.shape),
                    background,
                    foreground,
                    elevation,
                ) {
                    content()
                }
            }

            DomposableFabSize.Large -> {
                LargeFloatingActionButton(
                    props.onClick,
                    nativeModifier,
                    appearance.nativeShape(FloatingActionButtonDefaults.largeShape),
                    background,
                    foreground,
                    elevation,
                ) {
                    content()
                }
            }
        }
    }
    if (props.extended) {
        ExtendedFloatingActionButton(
            onClick = props.onClick,
            modifier = nativeModifier,
            icon = content,
            text = {
                Text(props.label)
            },
            shape =
                appearance.nativeShape(
                    FloatingActionButtonDefaults.extendedFabShape,
                ),
            containerColor = background,
            contentColor = foreground,
            elevation = elevation,
        )
    } else {
        button()
    }
}

@Composable
internal fun NativeSnackbar(
    props: DomposableSnackbarProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.snackbar
    val shape = appearance.nativeShape(SnackbarDefaults.shape)
    val actionColor = appearance.content?.nativeColor() ?: SnackbarDefaults.actionColor
    val contentColor = appearance.content?.nativeColor() ?: SnackbarDefaults.contentColor
    val actionColors = ButtonDefaults.textButtonColors(contentColor = actionColor)
    Snackbar(
        modifier.toComposeModifier().then(Modifier.nativeModifier(appearance, shape)).semantics {
            liveRegion =
                LiveRegionMode.Polite
        },
        shape = shape,
        containerColor = appearance.container?.nativeColor() ?: SnackbarDefaults.color,
        contentColor = contentColor,
        actionContentColor = actionColor,
        dismissActionContentColor = appearance.content?.nativeColor() ?: SnackbarDefaults.dismissActionContentColor,
        action =
            props.onAction?.let { action ->
                { TextButton(action, colors = actionColors) { Text(props.actionLabel!!) } }
            },
        dismissAction =
            props.onDismiss?.let { dismiss ->
                { TextButton(dismiss, colors = actionColors) { Text("Dismiss") } }
            },
    ) { Text(props.message, style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.bodyMedium)) }
}

@Composable
internal fun NativeSelect(
    props: DomposableSelectProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.select
    val background = appearance.container?.nativeColor() ?: Color.Unspecified
    val foreground = appearance.content?.nativeColor() ?: Color.Unspecified
    val shape = appearance.shape?.nativeShape() ?: ButtonDefaults.outlinedShape
    val colors =
        ButtonDefaults.outlinedButtonColors(
            containerColor = background,
            contentColor = foreground,
            disabledContainerColor = appearance.disabledContainer?.nativeColor() ?: Color.Unspecified,
            disabledContentColor = appearance.disabledContent?.nativeColor() ?: Color.Unspecified,
        )
    val disabledBorderColor = appearance.disabledBorder
    val borderColor = appearance.border
    val border =
        when {
            !props.enabled && disabledBorderColor != null -> {
                androidx.compose.foundation.BorderStroke(
                    appearance.borderWidth?.value?.dp ?: 1.dp,
                    disabledBorderColor.nativeColor(),
                )
            }

            borderColor != null -> {
                androidx.compose.foundation.BorderStroke(
                    appearance.borderWidth?.value?.dp ?: 1.dp,
                    borderColor.nativeColor(),
                )
            }

            else -> {
                ButtonDefaults.outlinedButtonBorder(enabled = props.enabled)
            }
        }
    var expanded by remember { mutableStateOf(false) }
    Box(modifier.toComposeModifier()) {
        OutlinedButton(
            { expanded = true },
            enabled = props.enabled && domposableFormEnabled(),
            modifier =
                (appearance.minHeight?.let { Modifier.defaultMinSize(minHeight = it.value.dp) } ?: Modifier)
                    .then(appearance.minWidth?.let { Modifier.defaultMinSize(minWidth = it.value.dp) } ?: Modifier)
                    .semantics {
                        contentDescription = props.label
                        props.error?.let { error(it) }
                    },
            colors = colors,
            shape = shape,
            border = border,
            contentPadding = appearance.contentPadding?.nativePadding() ?: ButtonDefaults.ContentPadding,
        ) {
            Text(
                "${props.label}: ${props.options.first { it.value == props.value }.label}",
                style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
            )
        }
        NativeWindow {
            DropdownMenu(expanded && props.enabled && domposableFormEnabled(), { expanded = false }) {
                props.options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            expanded = false
                            props.onValueChange(option.value)
                        },
                        enabled = option.enabled && props.enabled && domposableFormEnabled(),
                    )
                }
            }
        }
    }
    props.error?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
}

@Composable
internal fun NativeRangeSlider(
    props: DomposableRangeSliderProps,
    modifier: DomposableModifier,
) {
    val startInteraction = remember { MutableInteractionSource() }
    val endInteraction = remember { MutableInteractionSource() }
    val state =
        remember(props.range, props.steps) {
            RangeSliderState(props.value.start, props.value.endInclusive, props.steps, trackRange = props.range)
        }
    state.startValue = props.value.start
    state.endValue = props.value.endInclusive
    RangeSlider(
        state = state,
        onValueChange = props.onValueChange,
        modifier = modifier.toComposeModifier(),
        enabled = props.enabled && domposableFormEnabled(),
        startThumbInteractionSource = startInteraction,
        endThumbInteractionSource = endInteraction,
        startThumb = {
            SliderDefaults.Thumb(
                startInteraction,
                Modifier.semantics {
                    contentDescription = props.startLabel
                },
                enabled = props.enabled && domposableFormEnabled(),
            )
        },
        endThumb = {
            SliderDefaults.Thumb(
                endInteraction,
                Modifier.semantics {
                    contentDescription = props.endLabel
                },
                enabled = props.enabled && domposableFormEnabled(),
            )
        },
        colors = (props.colors ?: LocalDomposableTokens.current.components.rangeSlider).nativeSliderColors(),
    )
}

@Composable
internal fun NativeTooltip(
    props: DomposableTooltipProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.tooltip
    val shape = appearance.nativeShape(TooltipDefaults.plainTooltipContainerShape)
    NativeWindow { featureRegistry ->
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                if (props.title != null || props.action != null) {
                    RichTooltip(
                        modifier = Modifier.then(Modifier.nativeClipAndBorder(appearance, shape)),
                        title =
                            props.title?.let { title ->
                                {
                                    Text(
                                        title,
                                        style =
                                            appearance.textAppearance.nativeTextStyle(
                                                MaterialTheme.typography.titleSmall,
                                            ),
                                    )
                                }
                            },
                        action = props.action,
                        shape = shape,
                        colors =
                            TooltipDefaults.richTooltipColors(
                                containerColor = appearance.container?.nativeColor() ?: Color.Unspecified,
                                contentColor = appearance.content?.nativeColor() ?: Color.Unspecified,
                                titleContentColor = appearance.content?.nativeColor() ?: Color.Unspecified,
                                actionContentColor = appearance.content?.nativeColor() ?: Color.Unspecified,
                            ),
                        tonalElevation = appearance.tonalElevation?.value?.dp ?: 0.dp,
                        shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
                    ) {
                        Text(
                            props.text,
                            style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.bodySmall),
                        )
                    }
                } else {
                    PlainTooltip(
                        modifier = Modifier.then(Modifier.nativeClipAndBorder(appearance, shape)),
                        shape = shape,
                        contentColor = appearance.content?.nativeColor() ?: TooltipDefaults.plainTooltipContentColor,
                        containerColor =
                            appearance.container?.nativeColor() ?: TooltipDefaults.plainTooltipContainerColor,
                        tonalElevation = appearance.tonalElevation?.value?.dp ?: 0.dp,
                        shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
                    ) {
                        Text(
                            props.text,
                            style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.bodySmall),
                        )
                    }
                }
            },
            state = rememberTooltipState(),
            modifier = modifier.toComposeModifier(),
            content = { CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) { content() } },
        )
    }
}
