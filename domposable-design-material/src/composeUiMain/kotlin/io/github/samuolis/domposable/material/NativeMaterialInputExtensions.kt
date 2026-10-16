@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val SCROLL_FIELD_PAGE_COUNT = 100_000

@Composable
internal fun DomposableItemAppearance.nativeScrollFieldColors(
    enabled: Boolean = true,
): androidx.compose.material3.ScrollFieldColors =
    androidx.compose.material3.ScrollFieldDefaults.colors(
        containerColor =
            if (enabled) {
                colors.container?.nativeColor() ?: MaterialTheme.colorScheme.surfaceContainer
            } else {
                colors.disabledContainer?.nativeColor()
                    ?: MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.38f)
            },
        unselectedContentColor =
            if (enabled) {
                colors.content?.nativeColor() ?: MaterialTheme.colorScheme.outline
            } else {
                colors.disabledContent?.nativeColor() ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
        selectedContentColor =
            if (enabled) {
                colors.selectedContent?.nativeColor() ?: MaterialTheme.colorScheme.onSurface
            } else {
                colors.disabledContent?.nativeColor() ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
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
    val state =
        key(props.itemCount) {
            val middle = SCROLL_FIELD_PAGE_COUNT / 2
            rememberPagerState(initialPage = middle - middle % props.itemCount + props.selectedIndex) {
                SCROLL_FIELD_PAGE_COUNT
            }
        }
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val latestCallback by rememberUpdatedState(props.onSelectedIndexChange)
    val selectedIndex by rememberUpdatedState(props.selectedIndex)
    val enabled by rememberUpdatedState(props.enabled && domposableFormEnabled())
    LaunchedEffect(state) {
        snapshotFlow { state.currentPage % props.itemCount }.drop(1).collect { index ->
            if (index != selectedIndex) {
                if (enabled) latestCallback(index)
                withFrameNanos { }
                if (selectedIndex != index) state.scrollToOption(selectedIndex, props.itemCount)
            }
        }
    }
    LaunchedEffect(props.selectedIndex, state) {
        if (state.currentPage % props.itemCount != props.selectedIndex) {
            state.scrollToOption(props.selectedIndex, props.itemCount)
        }
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
    val colors = appearance.nativeScrollFieldColors(enabled)
    VerticalPager(
        state = state,
        userScrollEnabled = enabled,
        modifier =
            baseModifier
                .height(androidx.compose.material3.ScrollFieldDefaults.ScrollFieldHeight)
                .indication(interactionSource, ripple())
                .background(colors.containerColor, shape)
                .onKeyEvent { event ->
                    if (!enabled || event.type != KeyEventType.KeyDown) {
                        return@onKeyEvent false
                    }
                    val direction =
                        when (event.key) {
                            Key.DirectionDown -> 1
                            Key.DirectionUp -> -1
                            else -> return@onKeyEvent false
                        }
                    scope.launch {
                        state.animateScrollToPage(
                            (state.targetPage + direction).coerceIn(0, SCROLL_FIELD_PAGE_COUNT - 1),
                        )
                    }
                    true
                }.focusable(enabled = enabled, interactionSource = interactionSource)
                .clearAndSetSemantics {
                    contentDescription = props.label
                    stateDescription = props.indexDescription(state.targetPage % props.itemCount)
                    progressBarRangeInfo =
                        ProgressBarRangeInfo(
                            state.targetPage.toFloat(),
                            0f..(SCROLL_FIELD_PAGE_COUNT - 1).toFloat(),
                            SCROLL_FIELD_PAGE_COUNT - 2,
                        )
                    if (!enabled) disabled()
                    setProgress { target ->
                        if (!enabled || !target.isFinite()) {
                            false
                        } else {
                            val page = target.roundToInt().coerceIn(0, SCROLL_FIELD_PAGE_COUNT - 1)
                            if (page == state.targetPage) {
                                false
                            } else {
                                scope.launch { state.animateScrollToPage(page) }
                                true
                            }
                        }
                    }
                },
        pageSize = PageSize.Fixed(androidx.compose.material3.ScrollFieldDefaults.ScrollFieldHeight / 3),
        horizontalAlignment = Alignment.CenterHorizontally,
        snapPosition = SnapPosition.Center,
    ) { page ->
        val index = page % props.itemCount
        val selected = state.currentPage == page
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
                .focusProperties { canFocus = false }
                .clickable(enabled = enabled) { scope.launch { state.animateScrollToPage(page) } }
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
                    .Item(index, selected, colors)
            } else {
                Text(index.toString().padStart(2, '0'), style = textStyle, color = itemColor)
            }
        }
    }
}

private suspend fun PagerState.scrollToOption(
    index: Int,
    itemCount: Int,
) {
    val page = currentPage - currentPage % itemCount + index
    scrollToPage(if (page >= SCROLL_FIELD_PAGE_COUNT) page - itemCount else page)
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
        MenuDefaults.selectableItemColors().copy(
            textColor = resolvedColors.content,
            containerColor = resolvedColors.container,
            leadingIconColor = resolvedColors.content,
            trailingIconColor = resolvedColors.content,
            disabledTextColor = appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified,
            disabledContainerColor = appearance.colors.disabledContainer?.nativeColor() ?: Color.Unspecified,
            disabledLeadingIconColor = appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified,
            disabledTrailingIconColor = appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified,
            selectedTextColor = resolvedColors.selectedContent,
            selectedContainerColor = resolvedColors.selectedContainer,
            selectedLeadingIconColor = resolvedColors.selectedContent,
            selectedTrailingIconColor = resolvedColors.selectedContent,
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
            androidx.compose.material3.DropdownMenuItem(
                checked = checked,
                onCheckedChange = { value -> if (value != checked) props.onClick() },
                text = text,
                shapes = MenuDefaults.itemShapes(shape = shape),
                modifier = itemModifier,
                leadingIcon = props.leadingIcon,
                checkedLeadingIcon = selectedIcon,
                trailingIcon = trailing,
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
            androidx.compose.material3.DropdownMenuItem(
                selected = selected,
                onClick = props.onClick,
                text = text,
                shapes = MenuDefaults.itemShapes(shape = shape),
                modifier = itemModifier,
                leadingIcon = props.leadingIcon,
                selectedLeadingIcon = selectedIcon,
                trailingIcon = trailing,
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
                trailingIcon = trailing,
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
