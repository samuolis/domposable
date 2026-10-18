@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableCardVariant
import io.github.samuolis.domposable.foundation.DomposableCheckboxAppearance
import io.github.samuolis.domposable.foundation.DomposableChipVariant
import io.github.samuolis.domposable.foundation.DomposableControlPlacement
import io.github.samuolis.domposable.foundation.DomposableIconButtonVariant
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableProgressVariant
import io.github.samuolis.domposable.foundation.DomposableRadioButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableTabsVariant
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.ui.DomposableAvatarProps
import io.github.samuolis.domposable.ui.DomposableBadgeProps
import io.github.samuolis.domposable.ui.DomposableBannerProps
import io.github.samuolis.domposable.ui.DomposableCardProps
import io.github.samuolis.domposable.ui.DomposableCheckboxProps
import io.github.samuolis.domposable.ui.DomposableChipProps
import io.github.samuolis.domposable.ui.DomposableDividerProps
import io.github.samuolis.domposable.ui.DomposableIconButtonProps
import io.github.samuolis.domposable.ui.DomposableIconProps
import io.github.samuolis.domposable.ui.DomposableNavigationBarProps
import io.github.samuolis.domposable.ui.DomposableProgressProps
import io.github.samuolis.domposable.ui.DomposableRadioButtonProps
import io.github.samuolis.domposable.ui.DomposableSegmentedButtonsProps
import io.github.samuolis.domposable.ui.DomposableSurfaceProps
import io.github.samuolis.domposable.ui.DomposableSwitchProps
import io.github.samuolis.domposable.ui.DomposableTabsProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableFormEnabled
import io.github.samuolis.domposable.ui.safeDomposableHref
import io.github.samuolis.domposable.ui.toComposeModifier

@Composable
internal fun NativeIconButton(
    props: DomposableIconButtonProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.iconButton
    val formEnabled = props.enabled && domposableFormEnabled()
    val checkedState = props.checked
    if (checkedState != null) {
        val shape =
            appearance.shape?.nativeShape() ?: when (props.variant) {
                DomposableIconButtonVariant.Standard -> IconButtonDefaults.standardShape
                DomposableIconButtonVariant.Filled -> IconButtonDefaults.filledShape
                DomposableIconButtonVariant.FilledTonal -> IconButtonDefaults.filledShape
                DomposableIconButtonVariant.Outlined -> IconButtonDefaults.outlinedShape
            }
        val commonModifier =
            modifier
                .toComposeModifier()
                .defaultMinSize(
                    minWidth = (appearance.minWidth?.value ?: 0f).dp,
                    minHeight =
                        (
                            appearance.minHeight?.value
                                ?: 0f
                        ).dp,
                ).then(
                    appearance.border?.let {
                        Modifier.border(
                            BorderStroke(appearance.borderWidth?.value?.dp ?: 1.dp, it.nativeColor()),
                            shape,
                        )
                    }
                        ?: Modifier,
                ).semantics { contentDescription = props.label }
        val contentColor = appearance.content?.nativeColor() ?: Color.Unspecified
        val containerColor = appearance.container?.nativeColor() ?: Color.Unspecified
        val disabledContentColor = appearance.disabledContent?.nativeColor() ?: Color.Unspecified
        val disabledContainerColor = appearance.disabledContainer?.nativeColor() ?: Color.Unspecified
        when (props.variant) {
            DomposableIconButtonVariant.Standard -> {
                androidx.compose.material3.IconToggleButton(
                    checked = checkedState,
                    onCheckedChange = { props.onClick() },
                    modifier = commonModifier,
                    enabled = formEnabled,
                    colors =
                        IconButtonDefaults.iconToggleButtonColors(
                            containerColor,
                            contentColor,
                            disabledContainerColor,
                            disabledContentColor,
                            containerColor,
                            contentColor,
                        ),
                    shape = shape,
                ) { content() }
            }

            DomposableIconButtonVariant.Filled -> {
                androidx.compose.material3.FilledIconToggleButton(
                    checked = checkedState,
                    onCheckedChange = { props.onClick() },
                    modifier = commonModifier,
                    enabled = formEnabled,
                    colors =
                        IconButtonDefaults.filledIconToggleButtonColors(
                            containerColor,
                            contentColor,
                            disabledContainerColor,
                            disabledContentColor,
                            containerColor,
                            contentColor,
                        ),
                    shape = shape,
                ) { content() }
            }

            DomposableIconButtonVariant.FilledTonal -> {
                androidx.compose.material3.FilledTonalIconToggleButton(
                    checked = checkedState,
                    onCheckedChange = { props.onClick() },
                    modifier = commonModifier,
                    enabled = formEnabled,
                    colors =
                        IconButtonDefaults.filledTonalIconToggleButtonColors(
                            containerColor,
                            contentColor,
                            disabledContainerColor,
                            disabledContentColor,
                            containerColor,
                            contentColor,
                        ),
                    shape = shape,
                ) { content() }
            }

            DomposableIconButtonVariant.Outlined -> {
                androidx.compose.material3.OutlinedIconToggleButton(
                    checked = checkedState,
                    onCheckedChange = { props.onClick() },
                    modifier = commonModifier,
                    enabled = formEnabled,
                    colors =
                        IconButtonDefaults.outlinedIconToggleButtonColors(
                            containerColor,
                            contentColor,
                            disabledContainerColor,
                            disabledContentColor,
                            containerColor,
                            contentColor,
                        ),
                    shape = shape,
                ) { content() }
            }
        }
        return
    }
    if (appearance.isDefaultNativeButtonAppearance()) {
        val iconModifier = modifier.toComposeModifier().semantics { contentDescription = props.label }
        val iconContent: @Composable () -> Unit = { content() }
        when (props.variant) {
            DomposableIconButtonVariant.Standard -> {
                IconButton(
                    props.onClick,
                    iconModifier,
                    enabled = formEnabled,
                    content = iconContent,
                )
            }

            DomposableIconButtonVariant.Filled -> {
                androidx.compose.material3.FilledIconButton(
                    props.onClick,
                    iconModifier,
                    enabled = formEnabled,
                    content = iconContent,
                )
            }

            DomposableIconButtonVariant.FilledTonal -> {
                androidx.compose.material3.FilledTonalIconButton(
                    props.onClick,
                    iconModifier,
                    enabled = formEnabled,
                    content = iconContent,
                )
            }

            DomposableIconButtonVariant.Outlined -> {
                androidx.compose.material3.OutlinedIconButton(
                    props.onClick,
                    iconModifier,
                    enabled = formEnabled,
                    content = iconContent,
                )
            }
        }
        return
    }
    val shape = appearance.shape?.nativeShape() ?: IconButtonDefaults.standardShape
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val hovered by interaction.collectIsHoveredAsState()
    val scale =
        when {
            pressed -> appearance.pressedScale
            hovered -> appearance.hoveredScale
            else -> 1f
        }
    val colors =
        IconButtonDefaults.iconButtonColors(
            containerColor = appearance.container?.nativeColor() ?: Color.Unspecified,
            contentColor = appearance.content?.nativeColor() ?: Color.Unspecified,
            disabledContainerColor = appearance.disabledContainer?.nativeColor() ?: Color.Unspecified,
            disabledContentColor = appearance.disabledContent?.nativeColor() ?: Color.Unspecified,
        )
    val padding = appearance.contentPadding
    IconButton(
        onClick = props.onClick,
        modifier =
            modifier
                .toComposeModifier()
                .defaultMinSize(
                    minWidth = (appearance.minWidth?.value ?: 0f).dp,
                    minHeight = (appearance.minHeight?.value ?: 0f).dp,
                ).clip(shape)
                .then(
                    appearance.border?.let {
                        Modifier.border(
                            BorderStroke(appearance.borderWidth?.value?.dp ?: 1.dp, it.nativeColor()),
                            shape,
                        )
                    } ?: Modifier,
                ).scale(scale)
                .semantics { contentDescription = props.label },
        enabled = formEnabled,
        colors = colors,
        interactionSource = interaction,
        shape = shape,
    ) {
        CompositionLocalProvider(
            androidx.compose.material3.LocalTextStyle provides
                appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
        ) {
            Row(
                Modifier.padding(
                    start = (padding?.start?.value ?: 0f).dp,
                    top = (padding?.top?.value ?: 0f).dp,
                    end = (padding?.end?.value ?: 0f).dp,
                    bottom = (padding?.bottom?.value ?: 0f).dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(appearance.contentSpacing.value.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) { content() }
        }
    }
}

@Composable
internal fun NativeCheckbox(
    props: DomposableCheckboxProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.checkbox
    val formEnabled = props.enabled && domposableFormEnabled()
    ChoiceRow(
        label = props.label,
        description = props.description,
        showLabel = props.showLabel,
        placement = props.controlPlacement,
        spacing = appearance.labelSpacing.value.dp,
        labelAppearance = appearance.labelAppearance,
        descriptionAppearance = appearance.descriptionAppearance,
        descriptionSpacing = appearance.descriptionSpacing.value.dp,
        modifier =
            (
                if (props.indeterminate) {
                    modifier.toComposeModifier().triStateToggleable(
                        state = ToggleableState.Indeterminate,
                        enabled = formEnabled,
                        role = Role.Checkbox,
                        onClick = { props.onCheckedChange(true) },
                    )
                } else {
                    modifier.toComposeModifier().toggleable(
                        props.checked,
                        enabled = formEnabled,
                        role = Role.Checkbox,
                        onValueChange = props.onCheckedChange,
                    )
                }
            ).semantics(mergeDescendants = true) {
                if (!props.showLabel && props.label.isNotBlank()) contentDescription = props.label
            },
        control = {
            if (props.indeterminate) {
                TriStateCheckbox(
                    state = ToggleableState.Indeterminate,
                    onClick = null,
                    enabled = formEnabled,
                    colors = appearance.nativeCheckboxColors(formEnabled),
                )
            } else {
                Checkbox(
                    checked = props.checked,
                    onCheckedChange = null,
                    enabled = formEnabled,
                    colors = appearance.nativeCheckboxColors(formEnabled),
                )
            }
        },
    )
}

@Composable
internal fun NativeSwitch(
    props: DomposableSwitchProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.switch
    val formEnabled = props.enabled && domposableFormEnabled()
    val interaction = remember { MutableInteractionSource() }
    ChoiceRow(
        label = props.label,
        description = props.description,
        showLabel = props.showLabel,
        placement = props.controlPlacement,
        spacing = appearance.labelSpacing.value.dp,
        labelAppearance = appearance.labelAppearance,
        descriptionAppearance = appearance.descriptionAppearance,
        descriptionSpacing = appearance.descriptionSpacing.value.dp,
        modifier =
            modifier
                .toComposeModifier()
                .toggleable(
                    props.checked,
                    interactionSource = interaction,
                    indication = LocalIndication.current,
                    enabled = formEnabled,
                    role = Role.Switch,
                    onValueChange = props.onCheckedChange,
                ).semantics(mergeDescendants = true) {
                    if (!props.showLabel && props.label.isNotBlank()) contentDescription = props.label
                },
        control = {
            // The labelled row owns feedback; the thumb only consumes its press geometry.
            CompositionLocalProvider(LocalRippleConfiguration provides null) {
                Switch(
                    checked = props.checked,
                    onCheckedChange = null,
                    enabled = formEnabled,
                    colors = appearance.colors.nativeSwitchColors(),
                    interactionSource = interaction,
                )
            }
        },
    )
}

@Composable
internal fun NativeRadioButton(
    props: DomposableRadioButtonProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.radioButton
    val formEnabled = props.enabled && domposableFormEnabled()
    ChoiceRow(
        label = props.label,
        description = props.description,
        showLabel = props.showLabel,
        placement = props.controlPlacement,
        spacing = appearance.labelSpacing.value.dp,
        labelAppearance = appearance.labelAppearance,
        descriptionAppearance = appearance.descriptionAppearance,
        descriptionSpacing = appearance.descriptionSpacing.value.dp,
        modifier =
            modifier
                .toComposeModifier()
                .selectable(
                    selected = props.selected,
                    enabled = formEnabled,
                    role = Role.RadioButton,
                    onClick = props.onClick,
                ).semantics(mergeDescendants = true) {
                    if (!props.showLabel && props.label.isNotBlank()) contentDescription = props.label
                },
        control = {
            RadioButton(
                selected = props.selected,
                onClick = null,
                enabled = formEnabled,
                colors = appearance.nativeRadioButtonColors(),
            )
        },
    )
}

@Composable
private fun ChoiceRow(
    label: String,
    description: String?,
    showLabel: Boolean,
    placement: DomposableControlPlacement,
    spacing: androidx.compose.ui.unit.Dp,
    labelAppearance: DomposableTextAppearance?,
    descriptionAppearance: DomposableTextAppearance?,
    descriptionSpacing: androidx.compose.ui.unit.Dp,
    modifier: Modifier,
    control: @Composable () -> Unit,
) {
    val text: @Composable (Modifier) -> Unit = { textModifier ->
        if (showLabel || description != null) {
            Column(textModifier, verticalArrangement = Arrangement.spacedBy(descriptionSpacing)) {
                if (showLabel) {
                    Text(label, style = labelAppearance.nativeTextStyle(MaterialTheme.typography.bodyLarge))
                }
                description?.let {
                    Text(
                        it,
                        color =
                            descriptionAppearance?.color?.nativeColor() ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        style = descriptionAppearance.nativeTextStyle(MaterialTheme.typography.bodySmall),
                    )
                }
            }
        }
    }
    Row(
        modifier.materialKeyboardFocusRing().minimumInteractiveComponentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (placement == DomposableControlPlacement.Leading) {
            control()
            if (showLabel || description != null) Spacer(Modifier.width(spacing))
            text(Modifier)
        } else {
            text(if (showLabel || description != null) Modifier.weight(1f) else Modifier)
            if (showLabel || description != null) Spacer(Modifier.width(spacing))
            control()
        }
    }
}

@Composable
internal fun NativeCard(
    props: DomposableCardProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.card
    if (props.isDefaultNativeCard(appearance)) {
        Card(modifier = modifier.toComposeModifier()) { content() }
        return
    }
    val defaultShape =
        when (props.variant) {
            DomposableCardVariant.Filled -> CardDefaults.shape
            DomposableCardVariant.Elevated -> CardDefaults.elevatedShape
            DomposableCardVariant.Outlined -> CardDefaults.outlinedShape
        }
    val shape = appearance.nativeShape(defaultShape)
    val tonalContainer =
        appearance.tonalElevation?.value?.dp?.let { elevation ->
            if (appearance.container == null) MaterialTheme.colorScheme.surfaceColorAtElevation(elevation) else null
        }
    val colors =
        when (props.variant) {
            DomposableCardVariant.Filled -> {
                CardDefaults.cardColors(
                    containerColor = appearance.container?.nativeColor() ?: tonalContainer ?: Color.Unspecified,
                    contentColor = appearance.content?.nativeColor() ?: Color.Unspecified,
                )
            }

            DomposableCardVariant.Elevated -> {
                CardDefaults.elevatedCardColors(
                    containerColor = appearance.container?.nativeColor() ?: tonalContainer ?: Color.Unspecified,
                    contentColor = appearance.content?.nativeColor() ?: Color.Unspecified,
                )
            }

            DomposableCardVariant.Outlined -> {
                CardDefaults.outlinedCardColors(
                    containerColor = appearance.container?.nativeColor() ?: tonalContainer ?: Color.Unspecified,
                    contentColor = appearance.content?.nativeColor() ?: Color.Unspecified,
                )
            }
        }
    val elevation =
        when (props.variant) {
            DomposableCardVariant.Filled -> {
                CardDefaults.cardElevation(
                    defaultElevation =
                        appearance.shadowElevation?.value?.dp ?: 0.dp,
                )
            }

            DomposableCardVariant.Elevated -> {
                CardDefaults.elevatedCardElevation(
                    defaultElevation =
                        appearance.shadowElevation?.value?.dp ?: 1.dp,
                )
            }

            DomposableCardVariant.Outlined -> {
                CardDefaults.outlinedCardElevation(
                    defaultElevation =
                        appearance.shadowElevation?.value?.dp ?: 0.dp,
                )
            }
        }
    val defaultBorder = if (props.variant == DomposableCardVariant.Outlined) CardDefaults.outlinedCardBorder() else null
    val border =
        appearance.border?.let { BorderStroke(appearance.borderWidth.value.dp, it.nativeColor()) }
            ?: defaultBorder?.let { BorderStroke(appearance.borderWidth.value.dp, it.brush) }
    val uriHandler = LocalUriHandler.current
    val safeHref = props.href?.let(::safeDomposableHref)
    val onClick = props.onClick ?: safeHref?.let { href -> { uriHandler.openUri(href) } }
    val linkModifier =
        if (safeHref != null || props.actionLabel != null) {
            Modifier.semantics(mergeDescendants = true) {
                contentDescription = props.actionLabel ?: safeHref.orEmpty()
            }
        } else {
            Modifier
        }
    val cardModifier =
        modifier.toComposeModifier().then(linkModifier).then(
            if (props.variant == DomposableCardVariant.Elevated && border != null) {
                Modifier.border(border, shape)
            } else {
                Modifier
            },
        )
    val styleContent: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit = {
        val cardContent: @Composable () -> Unit = { StyledContent(appearance.textAppearance, content) }
        props.contentPadding?.let { contentPadding ->
            Box(Modifier.fillMaxWidth().padding(contentPadding.nativePadding())) {
                cardContent()
            }
        } ?: cardContent()
    }
    if (onClick != null) {
        when (props.variant) {
            DomposableCardVariant.Filled -> {
                Card(onClick, cardModifier, props.enabled, shape, colors, elevation, border, content = styleContent)
            }

            DomposableCardVariant.Elevated -> {
                ElevatedCard(onClick, cardModifier, props.enabled, shape, colors, elevation, content = styleContent)
            }

            DomposableCardVariant.Outlined -> {
                OutlinedCard(
                    onClick,
                    cardModifier,
                    props.enabled,
                    shape,
                    colors,
                    elevation,
                    border ?: CardDefaults.outlinedCardBorder(),
                    content = styleContent,
                )
            }
        }
    } else {
        when (props.variant) {
            DomposableCardVariant.Filled -> {
                Card(cardModifier, shape, colors, elevation, border, styleContent)
            }

            DomposableCardVariant.Elevated -> {
                ElevatedCard(cardModifier, shape, colors, elevation, styleContent)
            }

            DomposableCardVariant.Outlined -> {
                OutlinedCard(
                    cardModifier,
                    shape,
                    colors,
                    elevation,
                    border ?: CardDefaults.outlinedCardBorder(),
                    styleContent,
                )
            }
        }
    }
}

private fun DomposableCardProps.isDefaultNativeCard(appearance: DomposableSurfaceAppearance): Boolean =
    listOf(
        appearance.isEmpty(),
        contentPadding == null,
        variant == DomposableCardVariant.Filled,
        this.appearance == null,
        onClick == null,
        href == null,
    ).all { it }

@Composable
internal fun NativeSurface(
    props: DomposableSurfaceProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.surface
    val color = props.color?.nativeColor() ?: appearance.container?.nativeColor() ?: MaterialTheme.colorScheme.surface
    if (appearance.isEmpty()) {
        Surface(modifier = modifier.toComposeModifier(), color = color) { content() }
        return
    }
    Surface(
        modifier = modifier.toComposeModifier(),
        color = color,
        contentColor = appearance.content?.nativeColor() ?: androidx.compose.material3.contentColorFor(color),
        shape = appearance.nativeShape(androidx.compose.ui.graphics.RectangleShape),
        tonalElevation = appearance.tonalElevation?.value?.dp ?: 0.dp,
        shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
        border = appearance.border?.let { BorderStroke(appearance.borderWidth.value.dp, it.nativeColor()) },
    ) { StyledContent(appearance.textAppearance, content) }
}

@Composable
internal fun NativeIcon(
    props: DomposableIconProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.icon
    val style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.bodyLarge)
    Text(
        props.symbol,
        modifier.toComposeModifier().clearAndSetSemantics { props.description?.let { contentDescription = it } },
        color = appearance.color?.nativeColor() ?: Color.Unspecified,
        style = style,
    )
}

@Composable
internal fun NativeDivider(
    props: DomposableDividerProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.divider
    val native = modifier.toComposeModifier()
    val thickness = appearance.thickness?.value?.dp ?: DividerDefaults.Thickness
    val color = appearance.color?.nativeColor() ?: DividerDefaults.color
    if (props.orientation == io.github.samuolis.domposable.foundation.DomposableOrientation.Horizontal) {
        HorizontalDivider(native, thickness = thickness, color = color)
    } else {
        VerticalDivider(native, thickness = thickness, color = color)
    }
}

@Composable
internal fun NativeBadge(
    props: DomposableBadgeProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.badge
    val nativeModifier =
        modifier.toComposeModifier().then(
            if (props.label ==
                null
            ) {
                Modifier
            } else {
                Modifier.semantics { props.label?.let { contentDescription = it } }
            },
        )
    if (appearance.isEmpty() && !props.hasCustomGeometry()) {
        val content: (@Composable androidx.compose.foundation.layout.RowScope.() -> Unit)? =
            props.content?.let { custom -> { custom() } } ?: props.text?.let { text -> { Text(text) } }
        Badge(nativeModifier, content = content)
    } else {
        val shape = appearance.nativeShape(MaterialTheme.shapes.small)
        Surface(
            modifier = nativeModifier.then(Modifier.nativeModifier(appearance, shape)),
            shape = shape,
            color = appearance.nativeContainer(BadgeDefaults.containerColor),
            contentColor =
                appearance.nativeContent(
                    androidx.compose.material3.contentColorFor(BadgeDefaults.containerColor),
                ),
            tonalElevation = appearance.tonalElevation?.value?.dp ?: 0.dp,
            shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
            border = appearance.border?.let { BorderStroke(appearance.borderWidth.value.dp, it.nativeColor()) },
        ) {
            val padding = props.contentPadding
            Box(
                Modifier
                    .defaultMinSize(
                        minWidth = props.minWidth?.value?.dp ?: 0.dp,
                        minHeight = props.minHeight?.value?.dp ?: 0.dp,
                    ).padding(
                        start = padding?.start?.value?.dp ?: 0.dp,
                        top = padding?.top?.value?.dp ?: 0.dp,
                        end = padding?.end?.value?.dp ?: 0.dp,
                        bottom = padding?.bottom?.value?.dp ?: 0.dp,
                    ),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                androidx.compose.material3.ProvideTextStyle(
                    appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelSmall),
                ) {
                    props.content?.invoke() ?: props.text?.let { Text(it) }
                }
            }
        }
    }
}

private fun DomposableBadgeProps.hasCustomGeometry(): Boolean =
    contentPadding != null || minWidth != null || minHeight != null

@Composable
internal fun NativeChip(
    props: DomposableChipProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.chip
    val enabled = props.enabled && domposableFormEnabled()
    val interaction = remember { MutableInteractionSource() }
    val resolved = appearance.resolveInteractionColors(interaction, props.selected, enabled)
    val disabledContent = appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified
    val disabledContainer = appearance.colors.disabledContainer?.nativeColor() ?: Color.Unspecified
    val minInteractive = appearance.minimumInteractiveSize?.value?.dp ?: 48.dp
    val chipModifier =
        modifier
            .toComposeModifier()
            .then(appearance.minHeight?.let { Modifier.heightIn(min = it.value.dp) } ?: Modifier)
            .then(
                if (props.elevated &&
                    props.variant == DomposableChipVariant.Input
                ) {
                    Modifier.shadow(
                        1.dp,
                        appearance.shape?.nativeShape() ?: InputChipDefaults.shape,
                    )
                } else {
                    Modifier
                },
            ).minimumInteractiveComponentSize()
    val borderColor = resolved.border
    val borderWidth =
        if (props.selected) {
            appearance.selectedBorderWidth?.value?.dp ?: appearance.borderWidth.value.dp
        } else {
            appearance.borderWidth.value.dp
        }
    val defaultBorder =
        when (props.variant) {
            DomposableChipVariant.Filter -> FilterChipDefaults.filterChipBorder(enabled, props.selected)
            DomposableChipVariant.Assist -> AssistChipDefaults.assistChipBorder(enabled)
            DomposableChipVariant.Suggestion -> SuggestionChipDefaults.suggestionChipBorder(enabled)
            DomposableChipVariant.Input -> InputChipDefaults.inputChipBorder(enabled, props.selected)
        }
    val border =
        if (borderColor == Color.Unspecified) {
            BorderStroke(borderWidth, defaultBorder.brush)
        } else {
            BorderStroke(borderWidth, borderColor)
        }
    val textAppearance =
        if (props.selected) {
            appearance.selectedTextAppearance ?: appearance.textAppearance
        } else {
            appearance.textAppearance
        }
    val label: @Composable () -> Unit = {
        StyledContent(textAppearance) {
            Text(
                text = props.label,
                minLines = textAppearance?.minLines ?: 1,
                maxLines = textAppearance?.maxLines ?: Int.MAX_VALUE,
                overflow = textAppearance?.overflow?.nativeOverflow() ?: TextOverflow.Clip,
            )
        }
    }
    val leading = props.leadingIcon
    val trailing = props.trailingIcon
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides minInteractive) {
        when (props.variant) {
            DomposableChipVariant.Filter -> {
                if (props.elevated) {
                    ElevatedFilterChip(
                        selected = props.selected,
                        onClick = props.onClick,
                        label = label,
                        modifier = chipModifier,
                        enabled = enabled,
                        leadingIcon = leading,
                        trailingIcon = trailing,
                        shape =
                            appearance.shape?.nativeShape() ?: FilterChipDefaults.shape,
                        colors =
                            FilterChipDefaults.elevatedFilterChipColors(
                                containerColor = resolved.container,
                                labelColor = resolved.content,
                                disabledContainerColor =
                                disabledContainer,
                                disabledLabelColor =
                                disabledContent,
                                iconColor = resolved.content,
                                disabledLeadingIconColor = disabledContent,
                                disabledTrailingIconColor = disabledContent,
                                selectedContainerColor = resolved.selectedContainer,
                                disabledSelectedContainerColor = disabledContainer,
                                selectedLabelColor = resolved.selectedContent,
                                selectedLeadingIconColor = resolved.selectedContent,
                                selectedTrailingIconColor = resolved.selectedContent,
                            ),
                        border = border,
                        contentPadding =
                            appearance.contentPadding?.nativePadding() ?: FilterChipDefaults.ContentPadding,
                        horizontalArrangement = Arrangement.spacedBy(appearance.contentSpacing?.value?.dp ?: 4.dp),
                        interactionSource = interaction,
                    )
                } else {
                    FilterChip(
                        selected = props.selected,
                        onClick = props.onClick,
                        label = label,
                        modifier = chipModifier,
                        enabled = enabled,
                        leadingIcon = leading,
                        trailingIcon = trailing,
                        shape =
                            appearance.shape?.nativeShape() ?: FilterChipDefaults.shape,
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = resolved.container,
                                labelColor = resolved.content,
                                disabledContainerColor =
                                disabledContainer,
                                disabledLabelColor =
                                disabledContent,
                                iconColor = resolved.content,
                                disabledLeadingIconColor = disabledContent,
                                disabledTrailingIconColor = disabledContent,
                                selectedContainerColor = resolved.selectedContainer,
                                disabledSelectedContainerColor = disabledContainer,
                                selectedLabelColor = resolved.selectedContent,
                                selectedLeadingIconColor = resolved.selectedContent,
                                selectedTrailingIconColor = resolved.selectedContent,
                            ),
                        border = border,
                        contentPadding =
                            appearance.contentPadding?.nativePadding() ?: FilterChipDefaults.ContentPadding,
                        horizontalArrangement = Arrangement.spacedBy(appearance.contentSpacing?.value?.dp ?: 4.dp),
                        interactionSource = interaction,
                    )
                }
            }

            DomposableChipVariant.Assist -> {
                if (props.elevated) {
                    ElevatedAssistChip(
                        onClick = props.onClick,
                        label = label,
                        modifier = chipModifier,
                        enabled = enabled,
                        leadingIcon = leading,
                        trailingIcon = trailing,
                        shape = appearance.shape?.nativeShape() ?: AssistChipDefaults.shape,
                        colors =
                            AssistChipDefaults.elevatedAssistChipColors(
                                containerColor = resolved.container,
                                labelColor = resolved.content,
                                disabledContainerColor =
                                disabledContainer,
                                disabledLabelColor =
                                disabledContent,
                                leadingIconContentColor = resolved.content,
                                trailingIconContentColor = resolved.content,
                                disabledLeadingIconContentColor = disabledContent,
                                disabledTrailingIconContentColor = disabledContent,
                            ),
                        border = border,
                        contentPadding =
                            appearance.contentPadding?.nativePadding() ?: AssistChipDefaults.ContentPadding,
                        horizontalArrangement = Arrangement.spacedBy(appearance.contentSpacing?.value?.dp ?: 4.dp),
                        interactionSource = interaction,
                    )
                } else {
                    AssistChip(
                        onClick = props.onClick,
                        label = label,
                        modifier = chipModifier,
                        enabled = enabled,
                        leadingIcon = leading,
                        trailingIcon = trailing,
                        shape = appearance.shape?.nativeShape() ?: AssistChipDefaults.shape,
                        colors =
                            AssistChipDefaults.assistChipColors(
                                containerColor = resolved.container,
                                labelColor = resolved.content,
                                disabledContainerColor =
                                disabledContainer,
                                disabledLabelColor =
                                disabledContent,
                                leadingIconContentColor = resolved.content,
                                trailingIconContentColor = resolved.content,
                                disabledLeadingIconContentColor = disabledContent,
                                disabledTrailingIconContentColor = disabledContent,
                            ),
                        border = border,
                        contentPadding =
                            appearance.contentPadding?.nativePadding() ?: AssistChipDefaults.ContentPadding,
                        horizontalArrangement = Arrangement.spacedBy(appearance.contentSpacing?.value?.dp ?: 4.dp),
                        interactionSource = interaction,
                    )
                }
            }

            DomposableChipVariant.Suggestion -> {
                if (props.elevated) {
                    ElevatedSuggestionChip(
                        onClick = props.onClick,
                        label = label,
                        modifier = chipModifier,
                        enabled = enabled,
                        icon = leading,
                        shape = appearance.shape?.nativeShape() ?: SuggestionChipDefaults.shape,
                        colors =
                            SuggestionChipDefaults.elevatedSuggestionChipColors(
                                containerColor = resolved.container,
                                labelColor = resolved.content,
                                disabledContainerColor =
                                disabledContainer,
                                disabledLabelColor =
                                disabledContent,
                                iconContentColor = resolved.content,
                                disabledIconContentColor = disabledContent,
                            ),
                        border = border,
                        contentPadding =
                            appearance.contentPadding?.nativePadding() ?: SuggestionChipDefaults.ContentPadding,
                        horizontalArrangement = Arrangement.spacedBy(appearance.contentSpacing?.value?.dp ?: 4.dp),
                        interactionSource = interaction,
                    )
                } else {
                    SuggestionChip(
                        onClick = props.onClick,
                        label = label,
                        modifier = chipModifier,
                        enabled = enabled,
                        icon = leading,
                        shape = appearance.shape?.nativeShape() ?: SuggestionChipDefaults.shape,
                        colors =
                            SuggestionChipDefaults.suggestionChipColors(
                                containerColor = resolved.container,
                                labelColor = resolved.content,
                                disabledContainerColor =
                                disabledContainer,
                                disabledLabelColor =
                                disabledContent,
                                iconContentColor = resolved.content,
                                disabledIconContentColor = disabledContent,
                            ),
                        border = border,
                        contentPadding =
                            appearance.contentPadding?.nativePadding() ?: SuggestionChipDefaults.ContentPadding,
                        horizontalArrangement = Arrangement.spacedBy(appearance.contentSpacing?.value?.dp ?: 4.dp),
                        interactionSource = interaction,
                    )
                }
            }

            DomposableChipVariant.Input -> {
                InputChip(
                    selected = props.selected,
                    onClick = props.onClick,
                    label = label,
                    modifier = chipModifier,
                    enabled = enabled,
                    leadingIcon = leading,
                    trailingIcon = trailing,
                    shape =
                        appearance.shape?.nativeShape() ?: InputChipDefaults.shape,
                    colors =
                        InputChipDefaults.inputChipColors(
                            containerColor = resolved.container,
                            labelColor = resolved.content,
                            disabledContainerColor =
                            disabledContainer,
                            disabledLabelColor = disabledContent,
                            leadingIconColor = resolved.content,
                            trailingIconColor = resolved.content,
                            disabledLeadingIconColor = disabledContent,
                            disabledTrailingIconColor = disabledContent,
                            selectedContainerColor = resolved.selectedContainer,
                            disabledSelectedContainerColor = disabledContainer,
                            selectedLabelColor = resolved.selectedContent,
                            selectedLeadingIconColor = resolved.selectedContent,
                            selectedTrailingIconColor = resolved.selectedContent,
                        ),
                    border = border,
                    contentPadding =
                        appearance.contentPadding?.nativePadding()
                            ?: InputChipDefaults.contentPadding(
                                hasAvatar = false,
                                hasLeadingIcon = leading != null,
                                hasTrailingIcon = trailing != null,
                            ),
                    horizontalArrangement = Arrangement.spacedBy(appearance.contentSpacing?.value?.dp ?: 4.dp),
                    interactionSource = interaction,
                )
            }
        }
    }
}

@Composable
internal fun NativeAvatar(
    props: DomposableAvatarProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.avatar
    val shape = appearance.nativeShape(CircleShape)
    Surface(
        modifier = modifier.toComposeModifier().size(40.dp).semantics { contentDescription = props.description },
        shape = shape,
        color = appearance.nativeContainer(MaterialTheme.colorScheme.surfaceVariant),
        contentColor = appearance.nativeContent(MaterialTheme.colorScheme.onSurfaceVariant),
        tonalElevation = appearance.tonalElevation?.value?.dp ?: 0.dp,
        shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
        border = appearance.border?.let { BorderStroke(appearance.borderWidth.value.dp, it.nativeColor()) },
    ) {
        Box(contentAlignment = Alignment.Center) {
            val content = props.content
            if (content == null) {
                Text(
                    props.initials,
                    style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
                )
            } else {
                content()
            }
        }
    }
}

@Composable
internal fun NativeBanner(
    props: DomposableBannerProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.banner
    val defaultContent = if (props.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Surface(
        modifier =
            modifier.toComposeModifier().semantics {
                liveRegion =
                    androidx.compose.ui.semantics.LiveRegionMode.Polite
            },
        shape = appearance.nativeShape(MaterialTheme.shapes.medium),
        color = appearance.container?.nativeColor() ?: Color.Transparent,
        contentColor = appearance.content?.nativeColor() ?: defaultContent,
        tonalElevation = appearance.tonalElevation?.value?.dp ?: 0.dp,
        shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
        border = appearance.border?.let { BorderStroke(appearance.borderWidth.value.dp, it.nativeColor()) },
    ) { Text(props.message, style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.bodyLarge)) }
}

@Composable
internal fun NativeProgress(
    props: DomposableProgressProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.progress
    val native =
        modifier.toComposeModifier().semantics {
            props.label?.let { contentDescription = it }
            props.liveRegion?.let { liveRegion = it.native() }
        }
    val color =
        appearance.color?.nativeColor() ?: when {
            props.variant == DomposableProgressVariant.Wavy -> WavyProgressIndicatorDefaults.indicatorColor
            props.circular -> ProgressIndicatorDefaults.circularColor
            else -> ProgressIndicatorDefaults.linearColor
        }
    val track =
        appearance.track?.nativeColor()
            ?: if (props.variant == DomposableProgressVariant.Wavy) {
                WavyProgressIndicatorDefaults.trackColor
            } else if (props.circular && props.value == null) {
                ProgressIndicatorDefaults.circularIndeterminateTrackColor
            } else if (props.circular) {
                ProgressIndicatorDefaults.circularDeterminateTrackColor
            } else {
                ProgressIndicatorDefaults.linearTrackColor
            }
    val strokeWidth = appearance.strokeWidth?.value?.dp ?: 4.dp
    val strokePixels = with(androidx.compose.ui.platform.LocalDensity.current) { strokeWidth.toPx() }
    if (props.variant == io.github.samuolis.domposable.foundation.DomposableProgressVariant.Wavy) {
        val wavyModifier = appearance.indicatorSize?.let { native.size(it.value.dp) } ?: native
        val value = props.value
        val wavelength =
            when {
                props.circular -> WavyProgressIndicatorDefaults.CircularWavelength
                value == null -> WavyProgressIndicatorDefaults.LinearIndeterminateWavelength
                else -> WavyProgressIndicatorDefaults.LinearDeterminateWavelength
            }
        val waveSpeed = appearance.waveSpeed?.value?.dp ?: wavelength
        if (props.circular) {
            if (value ==
                null
            ) {
                CircularWavyProgressIndicator(
                    modifier = wavyModifier,
                    color = color,
                    trackColor = track,
                    stroke = Stroke(strokePixels, cap = StrokeCap.Round),
                    waveSpeed = waveSpeed,
                )
            } else {
                CircularWavyProgressIndicator(
                    progress = {
                        value
                    },
                    modifier = wavyModifier,
                    color = color,
                    trackColor = track,
                    stroke =
                        Stroke(
                            strokePixels,
                            cap = StrokeCap.Round,
                        ),
                    waveSpeed = waveSpeed,
                )
            }
        } else {
            if (value ==
                null
            ) {
                LinearWavyProgressIndicator(
                    modifier = wavyModifier,
                    color = color,
                    trackColor = track,
                    stroke = Stroke(strokePixels, cap = StrokeCap.Round),
                    waveSpeed = waveSpeed,
                )
            } else {
                LinearWavyProgressIndicator(
                    progress = {
                        value
                    },
                    modifier = wavyModifier,
                    color = color,
                    trackColor = track,
                    stroke =
                        Stroke(
                            strokePixels,
                            cap = StrokeCap.Round,
                        ),
                    stopSize =
                        if (appearance.showStopIndicator ==
                            false
                        ) {
                            0.dp
                        } else {
                            WavyProgressIndicatorDefaults.LinearTrackStopIndicatorSize
                        },
                    waveSpeed = waveSpeed,
                )
            }
        }
        return
    }
    if (props.circular) {
        val sizedModifier = appearance.indicatorSize?.let { native.size(it.value.dp) } ?: native
        val stroke = strokeWidth
        val value = props.value
        if (value ==
            null
        ) {
            CircularProgressIndicator(
                modifier = sizedModifier,
                color = color,
                trackColor = track,
                strokeWidth = stroke,
            )
        } else {
            CircularProgressIndicator(progress = {
                value
            }, modifier = sizedModifier, color = color, trackColor = track, strokeWidth = stroke)
        }
    } else {
        val linearModifier = appearance.strokeWidth?.let { native.height(it.value.dp) } ?: native
        val value = props.value
        if (value == null) {
            LinearProgressIndicator(modifier = linearModifier, color = color, trackColor = track)
        } else {
            if (appearance.showStopIndicator == false) {
                LinearProgressIndicator(
                    progress = { value },
                    modifier = linearModifier,
                    color = color,
                    trackColor = track,
                    drawStopIndicator = {},
                )
            } else {
                LinearProgressIndicator(
                    progress = { value },
                    modifier = linearModifier,
                    color = color,
                    trackColor = track,
                )
            }
        }
    }
}

@Composable
internal fun NativeTabs(
    props: DomposableTabsProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
) {
    if (props.labels.isEmpty()) return
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.tabs
    val item = appearance.colors
    val primaryContainer = item.container?.nativeColor() ?: TabRowDefaults.primaryContainerColor
    val primaryContent = item.content?.nativeColor() ?: TabRowDefaults.primaryContentColor
    val secondaryContainer = item.container?.nativeColor() ?: TabRowDefaults.secondaryContainerColor
    val secondaryContent = item.content?.nativeColor() ?: TabRowDefaults.secondaryContentColor
    val defaultTabContent = if (props.variant == DomposableTabsVariant.Secondary) secondaryContent else primaryContent
    val renderTabs: @Composable () -> Unit = {
        props.labels.forEachIndexed { index, label ->
            val selected = props.selectedIndex == index
            val selectedTextAppearance =
                if (selected) {
                    appearance.selectedTextAppearance ?: appearance.textAppearance
                } else {
                    appearance.textAppearance
                }
            val background = if (selected) item.selectedContainer else item.container
            val border = if (selected) item.selectedBorder ?: item.border else item.border
            val tabModifier =
                Modifier
                    .then(appearance.minHeight?.let { Modifier.heightIn(min = it.value.dp) } ?: Modifier)
                    .then(appearance.shape?.let { Modifier.clip(it.nativeShape()) } ?: Modifier)
                    .then(background?.let { Modifier.background(it.nativeColor()) } ?: Modifier)
                    .then(
                        border?.let {
                            Modifier.border(
                                (
                                    if (selected) {
                                        appearance.selectedBorderWidth
                                            ?: appearance.borderWidth
                                    } else {
                                        appearance.borderWidth
                                    }
                                ).value.dp,
                                it.nativeColor(),
                                appearance.shape?.nativeShape() ?: MaterialTheme.shapes.small,
                            )
                        }
                            ?: Modifier,
                    )
            val text: @Composable () -> Unit = {
                Text(label, style = selectedTextAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge))
            }
            val icon = props.icons[index]
            if (props.leadingIcons && icon != null) {
                androidx.compose.material3.LeadingIconTab(
                    selected = selected,
                    onClick = { props.onSelect(index) },
                    text = text,
                    icon = icon,
                    modifier = tabModifier,
                    selectedContentColor = item.selectedContent?.nativeColor() ?: defaultTabContent,
                    unselectedContentColor = item.content?.nativeColor() ?: defaultTabContent,
                )
            } else {
                Tab(
                    selected = selected,
                    onClick = { props.onSelect(index) },
                    modifier = tabModifier,
                    selectedContentColor = item.selectedContent?.nativeColor() ?: defaultTabContent,
                    unselectedContentColor = item.content?.nativeColor() ?: defaultTabContent,
                    text = text,
                    icon = icon,
                )
            }
        }
    }
    val baseModifier = modifier.toComposeModifier()
    when {
        props.variant == DomposableTabsVariant.Primary && props.scrollable -> {
            PrimaryScrollableTabRow(
                props.selectedIndex,
                baseModifier,
                containerColor = primaryContainer,
                contentColor = primaryContent,
                tabs = renderTabs,
            )
        }

        props.variant == DomposableTabsVariant.Secondary && props.scrollable -> {
            SecondaryScrollableTabRow(
                props.selectedIndex,
                baseModifier,
                containerColor = secondaryContainer,
                contentColor = secondaryContent,
                tabs = renderTabs,
            )
        }

        props.variant == DomposableTabsVariant.Secondary -> {
            SecondaryTabRow(
                props.selectedIndex,
                baseModifier,
                containerColor = secondaryContainer,
                contentColor = secondaryContent,
                tabs = renderTabs,
            )
        }

        else -> {
            PrimaryTabRow(
                props.selectedIndex,
                baseModifier,
                containerColor = primaryContainer,
                contentColor = primaryContent,
                tabs = renderTabs,
            )
        }
    }
}

@Composable
internal fun NativeNavigationBar(
    props: DomposableNavigationBarProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.navigationBar
    val item = appearance.colors
    val uriHandler = LocalUriHandler.current
    val rootModifier =
        modifier.toComposeModifier().semantics {
            contentDescription = props.label
        }
    val navigationBarContainer = item.container?.nativeColor() ?: NavigationBarDefaults.containerColor
    val navigationBarContent =
        item.content?.nativeColor()
            ?: androidx.compose.material3.contentColorFor(navigationBarContainer)
    val shortNavigationBarContainer = item.container?.nativeColor() ?: ShortNavigationBarDefaults.containerColor
    val shortNavigationBarContent =
        item.content?.nativeColor()
            ?: androidx.compose.material3.contentColorFor(shortNavigationBarContainer)
    val navigationRailContainer = item.container?.nativeColor() ?: NavigationRailDefaults.ContainerColor
    val navigationRailContent =
        item.content?.nativeColor()
            ?: androidx.compose.material3.contentColorFor(navigationRailContainer)
    if (props.orientation == io.github.samuolis.domposable.foundation.DomposableOrientation.Horizontal) {
        val standardItems: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
            props.items.forEachIndexed { index, itemData ->
                val selected = props.selectedIndex == index
                val safeHref = itemData.href?.let(::safeDomposableHref)
                val onClick = {
                    props.onSelect(index)
                    if (safeHref != null) uriHandler.openUri(safeHref)
                }
                val label = @Composable {
                    Text(
                        itemData.label,
                        style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelMedium),
                    )
                }
                val colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = item.selectedContent?.nativeColor() ?: Color.Unspecified,
                        selectedTextColor = item.selectedContent?.nativeColor() ?: Color.Unspecified,
                        indicatorColor = item.selectedContainer?.nativeColor() ?: Color.Unspecified,
                        unselectedIconColor = item.content?.nativeColor() ?: Color.Unspecified,
                        unselectedTextColor = item.content?.nativeColor() ?: Color.Unspecified,
                        disabledIconColor = item.disabledContent?.nativeColor() ?: Color.Unspecified,
                        disabledTextColor = item.disabledContent?.nativeColor() ?: Color.Unspecified,
                    )
                NavigationBarItem(
                    selected = selected,
                    onClick = onClick,
                    enabled =
                        itemData.enabled && domposableFormEnabled(),
                    icon = {
                        itemData.icon?.invoke()
                            ?: Text(itemData.symbol)
                    },
                    label = label,
                    colors = colors,
                )
            }
        }
        if (props.short) {
            ShortNavigationBar(
                modifier = rootModifier,
                containerColor = shortNavigationBarContainer,
                contentColor = shortNavigationBarContent,
            ) {
                props.items.forEachIndexed { index, itemData ->
                    val selected = props.selectedIndex == index
                    val safeHref = itemData.href?.let(::safeDomposableHref)
                    ShortNavigationBarItem(
                        selected,
                        {
                            props.onSelect(index)
                            safeHref?.let(uriHandler::openUri)
                        },
                        {
                            itemData.icon?.invoke()
                                ?: Text(itemData.symbol)
                        },
                        {
                            Text(
                                itemData.label,
                                style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelMedium),
                            )
                        },
                        enabled =
                            itemData.enabled && domposableFormEnabled(),
                        colors =
                            ShortNavigationBarItemDefaults.colors(
                                selectedIconColor = item.selectedContent?.nativeColor() ?: Color.Unspecified,
                                selectedTextColor =
                                    item.selectedContent?.nativeColor() ?: Color.Unspecified,
                                selectedIndicatorColor = item.selectedContainer?.nativeColor() ?: Color.Unspecified,
                                unselectedIconColor = item.content?.nativeColor() ?: Color.Unspecified,
                                unselectedTextColor = item.content?.nativeColor() ?: Color.Unspecified,
                            ),
                    )
                }
            }
        } else {
            NavigationBar(
                modifier = rootModifier,
                containerColor = navigationBarContainer,
                contentColor = navigationBarContent,
                content = standardItems,
            )
        }
    } else {
        NavigationRail(
            modifier = rootModifier,
            containerColor = navigationRailContainer,
            contentColor = navigationRailContent,
        ) {
            props.items.forEachIndexed { index, itemData ->
                NavigationRailItem(
                    selected = props.selectedIndex == index,
                    onClick = {
                        props.onSelect(index)
                        itemData.href?.let(::safeDomposableHref)?.let(uriHandler::openUri)
                    },
                    enabled = itemData.enabled && domposableFormEnabled(),
                    icon = { itemData.icon?.invoke() ?: Text(itemData.symbol) },
                    label = {
                        Text(
                            itemData.label,
                            style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelMedium),
                        )
                    },
                    colors =
                        NavigationRailItemDefaults.colors(
                            selectedIconColor = item.selectedContent?.nativeColor() ?: Color.Unspecified,
                            selectedTextColor = item.selectedContent?.nativeColor() ?: Color.Unspecified,
                            indicatorColor = item.selectedContainer?.nativeColor() ?: Color.Unspecified,
                            unselectedIconColor = item.content?.nativeColor() ?: Color.Unspecified,
                            unselectedTextColor = item.content?.nativeColor() ?: Color.Unspecified,
                            disabledIconColor = item.disabledContent?.nativeColor() ?: Color.Unspecified,
                            disabledTextColor = item.disabledContent?.nativeColor() ?: Color.Unspecified,
                        ),
                )
            }
        }
    }
}

@Composable
internal fun NativeSegmentedButtons(
    props: DomposableSegmentedButtonsProps,
    modifier: io.github.samuolis.domposable.ui.DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.segmentedButtons
    val c = appearance.colors
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.material3.LocalMinimumInteractiveComponentSize provides
            (appearance.minimumInteractiveSize?.value?.dp ?: 48.dp),
    ) {
        SingleChoiceSegmentedButtonRow(modifier.toComposeModifier().semantics { contentDescription = props.label }) {
            props.labels.forEachIndexed { index, label ->
                val selected = index == props.selectedIndex
                val interaction = remember { MutableInteractionSource() }
                val renderButton: @Composable () -> Unit = {
                    val enabled = props.enabled && props.optionEnabled[index] && domposableFormEnabled()
                    val resolved = appearance.resolveInteractionColors(interaction, selected, enabled)
                    val borderColor = if (selected) resolved.selectedBorder else resolved.border
                    val borderWidth =
                        if (selected) {
                            appearance.selectedBorderWidth?.value?.dp
                                ?: appearance.borderWidth.value.dp
                        } else {
                            appearance.borderWidth.value.dp
                        }
                    val labelAppearance =
                        if (selected) {
                            appearance.selectedTextAppearance ?: appearance.textAppearance
                        } else {
                            appearance.textAppearance
                        }
                    SegmentedButton(
                        selected = selected,
                        onClick = { if (props.allowReselect || !selected) props.onSelect(index) },
                        enabled = enabled,
                        modifier =
                            (if (props.equalWidth) Modifier.weight(1f) else Modifier)
                                .then(props.optionTestTags[index]?.let { Modifier.testTag(it) } ?: Modifier)
                                .then(appearance.minHeight?.let { Modifier.heightIn(min = it.value.dp) } ?: Modifier),
                        shape =
                            appearance.shape?.nativeShape()
                                ?: SegmentedButtonDefaults.itemShape(index, props.labels.size),
                        interactionSource = interaction,
                        contentPadding =
                            appearance.contentPadding?.nativePadding() ?: SegmentedButtonDefaults.ContentPadding,
                        colors =
                            SegmentedButtonDefaults.colors(
                                activeContainerColor = resolved.selectedContainer,
                                activeContentColor = resolved.selectedContent,
                                activeBorderColor = resolved.selectedBorder,
                                inactiveContainerColor = resolved.container,
                                inactiveContentColor = resolved.content,
                                inactiveBorderColor = resolved.border,
                                disabledActiveContainerColor = c.disabledContainer?.nativeColor() ?: Color.Unspecified,
                                disabledActiveContentColor = c.disabledContent?.nativeColor() ?: Color.Unspecified,
                                disabledActiveBorderColor = c.disabledBorder?.nativeColor() ?: Color.Unspecified,
                                disabledInactiveContainerColor =
                                    c.disabledContainer?.nativeColor() ?: Color.Unspecified,
                                disabledInactiveContentColor = c.disabledContent?.nativeColor() ?: Color.Unspecified,
                                disabledInactiveBorderColor = c.disabledBorder?.nativeColor() ?: Color.Unspecified,
                            ),
                        border =
                            if (borderColor ==
                                Color.Unspecified
                            ) {
                                BorderStroke(borderWidth, MaterialTheme.colorScheme.outline)
                            } else {
                                BorderStroke(borderWidth, borderColor)
                            },
                        icon = {
                            if (props.showSelectedIcon) {
                                if (selected) {
                                    props.selectedIcon?.invoke() ?: SegmentedButtonDefaults.Icon(true)
                                } else {
                                    SegmentedButtonDefaults.Icon(false)
                                }
                            }
                        },
                    ) { Text(label, style = labelAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge)) }
                }
                // Material's segment installs weight itself; a Box makes unequal-width items intrinsic.
                if (props.equalWidth) {
                    renderButton()
                } else {
                    val pressed by interaction.collectIsPressedAsState()
                    val hovered by interaction.collectIsHoveredAsState()
                    val focused by interaction.collectIsFocusedAsState()
                    val interactionCount = listOf(pressed, hovered, focused).count { it }
                    val selectedLayer = if (selected) SELECTED_SEGMENT_Z_INDEX else 0f
                    Box(
                        Modifier
                            .width(androidx.compose.foundation.layout.IntrinsicSize.Max)
                            .zIndex(selectedLayer + interactionCount),
                    ) { renderButton() }
                }
            }
        }
    }
}

@Composable
internal fun StyledContent(
    appearance: DomposableTextAppearance?,
    content: @Composable () -> Unit,
) {
    if (appearance == null) {
        content()
    } else {
        CompositionLocalProvider(
            LocalTextStyle provides appearance.nativeTextStyle(LocalTextStyle.current),
        ) { content() }
    }
}

private fun DomposableButtonAppearance.isDefaultNativeButtonAppearance(): Boolean =
    listOf(
        container == null,
        content == null,
        disabledContainer == null,
        disabledContent == null,
        border == null,
        disabledBorder == null,
        borderWidth == null,
        shape == null,
        minHeight == null,
        minWidth == null,
        contentPadding == null,
        contentSpacing.value == 0f,
        textAppearance == null,
        indicatorSize == null,
        indicatorStrokeWidth == null,
        pressedScale == 1f,
        hoveredScale == 1f,
    ).all { it }

// Material raises selected borders above overlapping neighbouring segments.
private const val SELECTED_SEGMENT_Z_INDEX = 5f
