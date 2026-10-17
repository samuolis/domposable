@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedToggleButton
import androidx.compose.material3.ExpandedDockedSearchBar
import androidx.compose.material3.ExpandedDockedSearchBarWithGap
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TonalToggleButton
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldVariant
import io.github.samuolis.domposable.ui.DomposableAutocompleteProps
import io.github.samuolis.domposable.ui.DomposableButtonGroupProps
import io.github.samuolis.domposable.ui.DomposableButtonProps
import io.github.samuolis.domposable.ui.DomposableCarouselProps
import io.github.samuolis.domposable.ui.DomposableCarouselVariant
import io.github.samuolis.domposable.ui.DomposableDragHandleProps
import io.github.samuolis.domposable.ui.DomposableFloatingActionButtonMenuProps
import io.github.samuolis.domposable.ui.DomposableFloatingToolbarProps
import io.github.samuolis.domposable.ui.DomposableLoadingIndicatorProps
import io.github.samuolis.domposable.ui.DomposableMenuProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposablePullToRefreshProps
import io.github.samuolis.domposable.ui.DomposableSearchBarProps
import io.github.samuolis.domposable.ui.DomposableSearchBarVariant
import io.github.samuolis.domposable.ui.DomposableSplitButtonProps
import io.github.samuolis.domposable.ui.DomposableSwipeDirection
import io.github.samuolis.domposable.ui.DomposableSwipeToDismissProps
import io.github.samuolis.domposable.ui.DomposableToggleButtonProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableFormEnabled
import io.github.samuolis.domposable.ui.toComposeModifier
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@Composable
internal fun NativeMenu(
    props: DomposableMenuProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.menu
    NativeWindow { featureRegistry ->
        DropdownMenu(
            expanded = props.expanded,
            onDismissRequest = props.onDismiss,
            modifier = modifier.toComposeModifier(),
            shape = appearance.nativeShape(MenuDefaults.shape),
            containerColor = appearance.container?.nativeColor() ?: MenuDefaults.containerColor,
            tonalElevation = appearance.tonalElevation?.value?.dp ?: MenuDefaults.TonalElevation,
            shadowElevation = appearance.shadowElevation?.value?.dp ?: MenuDefaults.ShadowElevation,
            border =
                appearance.border?.let {
                    androidx.compose.foundation.BorderStroke(appearance.borderWidth.value.dp, it.nativeColor())
                },
        ) {
            CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) {
                props.items.forEach { item ->
                    NativeDropdownMenuItem(
                        item.copy(onClick = {
                            props.onDismiss()
                            item.onClick()
                        }),
                        DomposableModifier,
                        {},
                    )
                }
                content()
            }
        }
    }
}

@Composable
internal fun NativeAutocomplete(
    props: DomposableAutocompleteProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.autocomplete
    val enabled = props.enabled && domposableFormEnabled()
    val colors =
        appearance.nativeColors(
            if (appearance.variant ==
                DomposableTextFieldVariant.Filled
            ) {
                TextFieldDefaults.colors()
            } else {
                OutlinedTextFieldDefaults.colors()
            },
        )
    ExposedDropdownMenuBox(
        expanded = props.expanded,
        onExpandedChange = props.onExpandedChange,
        modifier = modifier.toComposeModifier(),
    ) {
        val fieldModifier =
            Modifier.fillMaxWidth().menuAnchor(
                if (props.readOnly) {
                    ExposedDropdownMenuAnchorType.PrimaryNotEditable
                } else {
                    ExposedDropdownMenuAnchorType.PrimaryEditable
                },
                enabled = enabled,
            )
        val fieldLabel: @Composable () -> Unit = {
            Text(props.label, style = appearance.labelAppearance.nativeTextStyle(MaterialTheme.typography.bodyLarge))
        }
        if (appearance.variant == DomposableTextFieldVariant.Filled) {
            TextField(
                props.value,
                props.onValueChange,
                fieldModifier,
                enabled = enabled,
                readOnly = props.readOnly,
                label = fieldLabel,
                placeholder = { Text(props.label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(props.expanded) },
                colors = colors,
                shape = appearance.shape?.nativeShape() ?: TextFieldDefaults.shape,
                textStyle = appearance.textAppearance.nativeTextStyle(LocalTextStyle.current),
                singleLine = true,
            )
        } else {
            OutlinedTextField(
                props.value,
                props.onValueChange,
                fieldModifier,
                enabled = enabled,
                readOnly = props.readOnly,
                label = fieldLabel,
                placeholder = { Text(props.label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(props.expanded) },
                colors = colors,
                shape = appearance.shape?.nativeShape() ?: OutlinedTextFieldDefaults.shape,
                textStyle = appearance.textAppearance.nativeTextStyle(LocalTextStyle.current),
                singleLine = true,
            )
        }
        NativeWindow { featureRegistry ->
            ExposedDropdownMenu(
                expanded = props.expanded,
                onDismissRequest = { props.onExpandedChange(false) },
                shape = appearance.shape?.nativeShape() ?: MenuDefaults.shape,
                containerColor =
                    appearance.colors.container.unfocused
                        ?.nativeColor() ?: MenuDefaults.containerColor,
                tonalElevation = MenuDefaults.TonalElevation,
                shadowElevation = MenuDefaults.ShadowElevation,
                border =
                    appearance.colors.outline.unfocused?.let {
                        androidx.compose.foundation.BorderStroke(
                            appearance.outlineWidth?.value?.dp ?: 1.dp,
                            it.nativeColor(),
                        )
                    },
            ) {
                CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) {
                    props.options.filter { it.label.contains(props.value, ignoreCase = true) }.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            enabled =
                                option.enabled && enabled,
                            onClick = {
                                props.onOptionSelected(option)
                                props.onExpandedChange(false)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun NativeSearchBar(
    props: DomposableSearchBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.searchBar
    val state =
        if (props.variant ==
            DomposableSearchBarVariant.ContainedFullScreen
        ) {
            rememberContainedSearchBarState()
        } else {
            rememberSearchBarState()
        }
    val queryState = rememberTextFieldState(props.query)
    val latestQuery by rememberUpdatedState(props.query)
    val latestQueryChange by rememberUpdatedState(props.onQueryChange)
    val latestExpandedChange by rememberUpdatedState(props.onExpandedChange)
    val shape =
        appearance.shape?.nativeShape()
            ?: if (props.variant in
                setOf(DomposableSearchBarVariant.Docked, DomposableSearchBarVariant.DockedWithGap)
            ) {
                SearchBarDefaults.dockedShape
            } else {
                SearchBarDefaults.inputFieldShape
            }
    val enabled = props.enabled && domposableFormEnabled()
    val defaultColors = SearchBarDefaults.colors()
    val fieldColors = appearance.nativeColors(defaultColors.inputFieldColors)
    val colors =
        SearchBarDefaults.colors(
            containerColor =
                appearance.colors.container.unfocused
                    ?.nativeColor() ?: defaultColors.containerColor,
            inputFieldColors = fieldColors,
        )
    LaunchedEffect(state) {
        snapshotFlow { state.currentValue }.drop(1).collect { latestExpandedChange(it == SearchBarValue.Expanded) }
    }
    LaunchedEffect(props.expanded, state) {
        if (props.expanded && state.targetValue != SearchBarValue.Expanded) {
            state.animateToExpanded()
        } else if (!props.expanded && state.targetValue != SearchBarValue.Collapsed) {
            state.animateToCollapsed()
        }
    }
    LaunchedEffect(queryState) {
        snapshotFlow { queryState.text.toString() }.drop(1).collect { if (it != latestQuery) latestQueryChange(it) }
    }
    LaunchedEffect(props.query, queryState) {
        if (queryState.text.toString() != props.query) queryState.edit { replace(0, length, props.query) }
    }
    val inputField: @Composable () -> Unit = {
        SearchBarDefaults.InputField(
            textFieldState = queryState,
            searchBarState = state,
            onSearch = props.onSearch,
            enabled = enabled,
            textStyle = appearance.textAppearance.nativeTextStyle(LocalTextStyle.current),
            placeholder = { Text(props.placeholder ?: props.label) },
            leadingIcon = props.leadingIcon,
            trailingIcon = props.trailingIcon,
            shape = shape,
            colors = fieldColors,
        )
    }
    val nativeModifier = modifier.toComposeModifier()
    when (props.variant) {
        DomposableSearchBarVariant.AppBar -> {
            AppBarWithSearch(
                state = state,
                inputField = inputField,
                modifier = nativeModifier,
                navigationIcon = props.navigationIcon,
                actions = props.actions?.let { action -> { action() } },
                shape = shape,
                colors = SearchBarDefaults.appBarWithSearchColors(searchBarColors = colors),
            )
        }

        DomposableSearchBarVariant.ContainedFullScreen -> {
            ExpandedFullScreenContainedSearchBar(
                state = state,
                inputField = inputField,
                modifier = nativeModifier,
                collapsedShape = shape,
                colors = SearchBarDefaults.containedColors(state),
            ) { content() }
        }

        DomposableSearchBarVariant.DockedWithGap -> {
            SearchBar(state, inputField, modifier = nativeModifier, shape = shape, colors = colors)
            ExpandedDockedSearchBarWithGap(
                state,
                inputField,
                modifier = nativeModifier,
                shape = shape,
                colors = colors,
            ) {
                content()
            }
        }

        DomposableSearchBarVariant.Docked -> {
            SearchBar(state, inputField, modifier = nativeModifier, shape = shape, colors = colors)
            ExpandedDockedSearchBar(
                state,
                inputField,
                modifier = nativeModifier,
                shape = shape,
                colors = colors,
            ) { content() }
        }

        DomposableSearchBarVariant.FullScreen -> {
            SearchBar(state, inputField, modifier = nativeModifier, shape = shape, colors = colors)
            ExpandedFullScreenSearchBar(
                state,
                inputField,
                modifier = nativeModifier,
                collapsedShape = shape,
                colors = colors,
            ) {
                content()
            }
        }
    }
}

@Composable
internal fun NativeCarousel(
    props: DomposableCarouselProps,
    modifier: DomposableModifier,
) {
    if (props.items.isEmpty()) return
    NativeWindow { featureRegistry -> NativeCarouselContent(props, modifier, featureRegistry) }
}

@Composable
private fun NativeCarouselContent(
    props: DomposableCarouselProps,
    modifier: DomposableModifier,
    featureRegistry: androidx.compose.runtime.saveable.SaveableStateRegistry?,
) {
    val keys = props.items.map { it.key }
    val state =
        key(
            keys,
        ) { rememberCarouselState(initialItem = props.currentItem.coerceIn(props.items.indices)) { props.items.size } }
    val latestChange by rememberUpdatedState(props.onCurrentItemChange)
    LaunchedEffect(state) { snapshotFlow { state.currentItem }.drop(1).collect { latestChange?.invoke(it) } }
    LaunchedEffect(props.currentItem, state) {
        if (state.currentItem !=
            props.currentItem
        ) {
            state.animateScrollToItem(props.currentItem.coerceIn(props.items.indices))
        }
    }
    val item: @Composable CarouselItemScope.(Int) -> Unit = { index ->
        CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) {
            props.items[index].content()
        }
    }
    when (props.variant) {
        DomposableCarouselVariant.MultiBrowse -> {
            HorizontalMultiBrowseCarousel(
                state,
                props.preferredItemWidth.value.dp,
                modifier.toComposeModifier(),
                props.spacing.value.dp,
                minSmallItemWidth = props.minSmallItemWidth.value.dp,
                maxSmallItemWidth = props.maxSmallItemWidth.value.dp,
            ) {
                item(it)
            }
        }

        DomposableCarouselVariant.Uncontained -> {
            HorizontalUncontainedCarousel(
                state,
                props.preferredItemWidth.value.dp,
                modifier.toComposeModifier(),
                props.spacing.value.dp,
            ) {
                item(it)
            }
        }

        DomposableCarouselVariant.CenteredHero -> {
            HorizontalCenteredHeroCarousel(
                state,
                modifier.toComposeModifier(),
                maxItemWidth = props.preferredItemWidth.value.dp,
                itemSpacing = props.spacing.value.dp,
                minSmallItemWidth = props.minSmallItemWidth.value.dp,
                maxSmallItemWidth = props.maxSmallItemWidth.value.dp,
            ) {
                item(it)
            }
        }
    }
}

@Composable
internal fun NativePullToRefresh(
    props: DomposablePullToRefreshProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = props.refreshing,
        onRefresh = props.onRefresh,
        modifier = modifier.toComposeModifier().semantics { contentDescription = props.label },
        enabled = props.enabled && domposableFormEnabled(),
        threshold = props.threshold.value.dp,
    ) { content() }
}

@Composable
internal fun NativeSwipeToDismiss(
    props: DomposableSwipeToDismissProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeWindow { featureRegistry ->
        val state = rememberSwipeToDismissBoxState()
        LaunchedEffect(state.currentValue) {
            when (state.currentValue) {
                SwipeToDismissBoxValue.StartToEnd -> props.onDismiss(DomposableSwipeDirection.StartToEnd)
                SwipeToDismissBoxValue.EndToStart -> props.onDismiss(DomposableSwipeDirection.EndToStart)
                SwipeToDismissBoxValue.Settled -> Unit
            }
        }
        SwipeToDismissBox(
            state = state,
            modifier = modifier.toComposeModifier(),
            enableDismissFromStartToEnd = props.enabled && props.allowStartToEnd,
            enableDismissFromEndToStart = props.enabled && props.allowEndToStart,
            backgroundContent = {
                val direction =
                    when (state.dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> DomposableSwipeDirection.StartToEnd
                        SwipeToDismissBoxValue.EndToStart -> DomposableSwipeDirection.EndToStart
                        SwipeToDismissBoxValue.Settled -> null
                    }
                val action =
                    if (direction == DomposableSwipeDirection.EndToStart) {
                        props.endActionLabel
                    } else {
                        props.startActionLabel
                    }
                Box(
                    Modifier.semantics { if (action.isNotBlank()) contentDescription = action },
                ) {
                    CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) {
                        props.background(direction)
                    }
                }
            },
            content = {
                CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) { content() }
            },
        )
    }
}

@Composable
internal fun NativeToggleButton(
    props: DomposableToggleButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.toggleButton
    val enabled = props.enabled && domposableFormEnabled()
    val shapes =
        when (props.style) {
            DomposableButtonStyle.Elevated -> {
                ToggleButtonDefaults.shapesFor(ButtonDefaults.MinHeight)
            }

            DomposableButtonStyle.FilledTonal -> {
                ToggleButtonDefaults.shapesFor(ButtonDefaults.MinHeight)
            }

            DomposableButtonStyle.Outlined -> {
                ToggleButtonDefaults.shapesFor(ButtonDefaults.MinHeight)
            }

            DomposableButtonStyle.Filled, DomposableButtonStyle.Text -> {
                ToggleButtonDefaults.shapesFor(
                    ButtonDefaults.MinHeight,
                )
            }
        }.let { defaults ->
            appearance.shape?.nativeShape()?.let { defaults.copy(shape = it, pressedShape = it, checkedShape = it) }
                ?: defaults
        }
    val container = appearance.container?.nativeColor() ?: Color.Unspecified
    val contentColor = appearance.content?.nativeColor() ?: Color.Unspecified
    val colors =
        when (props.style) {
            DomposableButtonStyle.Elevated -> {
                ToggleButtonDefaults.elevatedToggleButtonColors(
                    containerColor = container,
                    contentColor = contentColor,
                    disabledContainerColor =
                        appearance.disabledContainer?.nativeColor() ?: Color.Unspecified,
                    disabledContentColor =
                        appearance.disabledContent?.nativeColor() ?: Color.Unspecified,
                    checkedContainerColor = container,
                    checkedContentColor = contentColor,
                )
            }

            DomposableButtonStyle.FilledTonal -> {
                ToggleButtonDefaults.tonalToggleButtonColors(
                    containerColor = container,
                    contentColor = contentColor,
                    disabledContainerColor =
                        appearance.disabledContainer?.nativeColor() ?: Color.Unspecified,
                    disabledContentColor =
                        appearance.disabledContent?.nativeColor() ?: Color.Unspecified,
                    checkedContainerColor = container,
                    checkedContentColor = contentColor,
                )
            }

            DomposableButtonStyle.Outlined -> {
                ToggleButtonDefaults.outlinedToggleButtonColors(
                    containerColor = container,
                    contentColor = contentColor,
                    disabledContainerColor =
                        appearance.disabledContainer?.nativeColor() ?: Color.Unspecified,
                    disabledContentColor =
                        appearance.disabledContent?.nativeColor() ?: Color.Unspecified,
                    checkedContainerColor = container,
                    checkedContentColor = contentColor,
                )
            }

            DomposableButtonStyle.Filled, DomposableButtonStyle.Text -> {
                ToggleButtonDefaults.toggleButtonColors(
                    containerColor = container,
                    contentColor = contentColor,
                    disabledContainerColor =
                        appearance.disabledContainer?.nativeColor() ?: Color.Unspecified,
                    disabledContentColor =
                        appearance.disabledContent?.nativeColor() ?: Color.Unspecified,
                    checkedContainerColor = container,
                    checkedContentColor = contentColor,
                )
            }
        }
    val disabledBorderColor = appearance.disabledBorder
    val borderColor = appearance.border
    val customBorder =
        when {
            !enabled && disabledBorderColor != null -> {
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

            props.style == DomposableButtonStyle.Outlined -> {
                if (!props.checked) ButtonDefaults.outlinedButtonBorder(enabled) else null
            }

            else -> {
                null
            }
        }
    val buttonModifier =
        modifier
            .toComposeModifier()
            .then(appearance.minHeight?.let { Modifier.heightIn(min = it.value.dp) } ?: Modifier)
            .then(appearance.minWidth?.let { Modifier.widthIn(min = it.value.dp) } ?: Modifier)
    val buttonContent: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
        CompositionLocalProvider(
            LocalTextStyle provides appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
        ) {
            content()
        }
    }
    val contentPadding =
        appearance.contentPadding?.nativePadding()
            ?: ButtonDefaults.contentPaddingFor(ButtonDefaults.MinHeight, hasStartIcon = false)
    when (props.style) {
        DomposableButtonStyle.Filled, DomposableButtonStyle.Text -> {
            ToggleButton(
                props.checked,
                props.onCheckedChange,
                buttonModifier,
                enabled = enabled,
                shapes = shapes,
                colors = colors,
                border = customBorder,
                contentPadding = contentPadding,
                content = buttonContent,
            )
        }

        DomposableButtonStyle.Elevated -> {
            ElevatedToggleButton(
                props.checked,
                props.onCheckedChange,
                buttonModifier,
                enabled = enabled,
                shapes = shapes,
                colors = colors,
                border = customBorder,
                contentPadding =
                    appearance.contentPadding?.nativePadding()
                        ?: ButtonDefaults.contentPaddingFor(
                            ButtonDefaults.MinHeight,
                            hasStartIcon = false,
                        ),
                content = buttonContent,
            )
        }

        DomposableButtonStyle.FilledTonal -> {
            TonalToggleButton(
                props.checked,
                props.onCheckedChange,
                buttonModifier,
                enabled = enabled,
                shapes = shapes,
                colors = colors,
                border = customBorder,
                contentPadding =
                    appearance.contentPadding?.nativePadding()
                        ?: ButtonDefaults.contentPaddingFor(
                            ButtonDefaults.MinHeight,
                            hasStartIcon = false,
                        ),
                content = buttonContent,
            )
        }

        DomposableButtonStyle.Outlined -> {
            OutlinedToggleButton(
                props.checked,
                props.onCheckedChange,
                buttonModifier,
                enabled = enabled,
                shapes = shapes,
                colors = colors,
                border = customBorder,
                contentPadding =
                    appearance.contentPadding?.nativePadding()
                        ?: ButtonDefaults.contentPaddingFor(
                            ButtonDefaults.MinHeight,
                            hasStartIcon = false,
                        ),
                content = buttonContent,
            )
        }
    }
}

@Composable
internal fun NativeButtonGroup(
    props: DomposableButtonGroupProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.buttonGroup
    val formEnabled = domposableFormEnabled()
    ButtonGroup(
        overflowIndicator = { state -> ButtonGroupDefaults.OverflowIndicator(state) },
        modifier = modifier.toComposeModifier(),
        horizontalArrangement = Arrangement.spacedBy(props.spacing.value.dp),
    ) {
        props.items.forEach { item ->
            customItem(
                buttonGroupContent = {
                    NativeButton(
                        DomposableButtonProps(item.onClick, item.enabled && formEnabled, appearance = appearance),
                        DomposableModifier,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(appearance.contentSpacing.value.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            item.icon?.invoke()
                            Text(item.label)
                        }
                    }
                },
                menuContent = { menuState ->
                    DropdownMenuItem(
                        text = { Text(item.label) },
                        onClick = {
                            menuState.dismiss()
                            item.onClick()
                        },
                        enabled = item.enabled && formEnabled,
                        leadingIcon = item.icon,
                    )
                },
            )
        }
    }
}

@Composable
internal fun NativeSplitButton(
    props: DomposableSplitButtonProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.splitButton
    val enabled = props.enabled && domposableFormEnabled()
    val colors =
        when (props.style) {
            DomposableButtonStyle.Filled -> {
                ButtonDefaults.buttonColors(
                    containerColor =
                        appearance.container?.nativeColor() ?: Color.Unspecified,
                    contentColor =
                        appearance.content?.nativeColor() ?: Color.Unspecified,
                    disabledContainerColor =
                        appearance.disabledContainer?.nativeColor() ?: Color.Unspecified,
                    disabledContentColor =
                        appearance.disabledContent?.nativeColor() ?: Color.Unspecified,
                )
            }

            DomposableButtonStyle.Elevated -> {
                ButtonDefaults.elevatedButtonColors(
                    containerColor =
                        appearance.container?.nativeColor() ?: Color.Unspecified,
                    contentColor =
                        appearance.content?.nativeColor() ?: Color.Unspecified,
                    disabledContainerColor =
                        appearance.disabledContainer?.nativeColor() ?: Color.Unspecified,
                    disabledContentColor =
                        appearance.disabledContent?.nativeColor() ?: Color.Unspecified,
                )
            }

            DomposableButtonStyle.FilledTonal -> {
                ButtonDefaults.filledTonalButtonColors(
                    containerColor =
                        appearance.container?.nativeColor() ?: Color.Unspecified,
                    contentColor =
                        appearance.content?.nativeColor() ?: Color.Unspecified,
                    disabledContainerColor =
                        appearance.disabledContainer?.nativeColor() ?: Color.Unspecified,
                    disabledContentColor =
                        appearance.disabledContent?.nativeColor() ?: Color.Unspecified,
                )
            }

            DomposableButtonStyle.Outlined -> {
                ButtonDefaults.outlinedButtonColors(
                    containerColor =
                        appearance.container?.nativeColor() ?: Color.Unspecified,
                    contentColor =
                        appearance.content?.nativeColor() ?: Color.Unspecified,
                    disabledContainerColor =
                        appearance.disabledContainer?.nativeColor() ?: Color.Unspecified,
                    disabledContentColor =
                        appearance.disabledContent?.nativeColor() ?: Color.Unspecified,
                )
            }

            DomposableButtonStyle.Text -> {
                ButtonDefaults.textButtonColors(
                    containerColor =
                        appearance.container?.nativeColor() ?: Color.Unspecified,
                    contentColor =
                        appearance.content?.nativeColor() ?: Color.Unspecified,
                    disabledContainerColor =
                        appearance.disabledContainer?.nativeColor() ?: Color.Unspecified,
                    disabledContentColor =
                        appearance.disabledContent?.nativeColor() ?: Color.Unspecified,
                )
            }
        }
    val buttonModifier =
        modifier.toComposeModifier().then(
            appearance.minHeight?.let { Modifier.heightIn(min = it.value.dp) } ?: Modifier,
        )
    val border =
        appearance.border?.let {
            androidx.compose.foundation.BorderStroke(appearance.borderWidth?.value?.dp ?: 1.dp, it.nativeColor())
        }
    val labelContent: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
        CompositionLocalProvider(
            LocalTextStyle provides appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(appearance.contentSpacing.value.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(props.label)
            }
        }
    }
    val trailingContent: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
        Box(
            Modifier.clearAndSetSemantics {
                contentDescription = props.secondaryLabel
                stateDescription =
                    if (props.expanded) "Expanded" else "Collapsed"
            },
        ) {
            Text(if (props.expanded) "▴" else "▾")
        }
    }
    SplitButtonLayout(
        modifier = buttonModifier,
        leadingButton = {
            SplitButtonDefaults.LeadingButton(
                props.onClick,
                enabled = enabled,
                colors = colors,
                border = border,
                contentPadding =
                    appearance.contentPadding?.nativePadding()
                        ?: SplitButtonDefaults.leadingButtonContentPaddingFor(48.dp),
                content = labelContent,
            )
        },
        trailingButton = {
            SplitButtonDefaults.TrailingButton(
                checked = props.expanded,
                onCheckedChange = {
                    props.onSecondaryClick()
                },
                enabled = enabled,
                colors = colors,
                border = border,
                contentPadding =
                    appearance.contentPadding?.nativePadding()
                        ?: SplitButtonDefaults.trailingButtonContentPaddingFor(48.dp),
                content = trailingContent,
            )
        },
    )
}

@Composable
internal fun NativeLoadingIndicator(
    props: DomposableLoadingIndicatorProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.loadingIndicator
    val color =
        appearance.color?.nativeColor() ?: if (props.contained) {
            LoadingIndicatorDefaults.containedIndicatorColor
        } else {
            LoadingIndicatorDefaults.indicatorColor
        }
    val containerColor =
        props.containerColor?.nativeColor() ?: appearance.track?.nativeColor()
            ?: LoadingIndicatorDefaults.containedContainerColor
    val nativeBase = modifier.toComposeModifier().semantics { contentDescription = props.label }
    val native = appearance.indicatorSize?.let { nativeBase.size(it.value.dp) } ?: nativeBase
    val progress = props.progress
    when {
        props.contained && progress != null -> {
            ContainedLoadingIndicator({
                progress
            }, native, containerColor = containerColor, indicatorColor = color)
        }

        props.contained -> {
            ContainedLoadingIndicator(native, containerColor = containerColor, indicatorColor = color)
        }

        progress != null -> {
            LoadingIndicator({ progress }, native, color = color)
        }

        else -> {
            LoadingIndicator(native, color = color)
        }
    }
}

@Composable
internal fun NativeFloatingToolbar(
    props: DomposableFloatingToolbarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.floatingToolbar
    val native = modifier.toComposeModifier()
    val container = appearance.container?.nativeColor() ?: Color.Unspecified
    val foreground = appearance.content?.nativeColor() ?: Color.Unspecified
    val colors =
        FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = container,
            toolbarContentColor = foreground,
            fabContainerColor = container,
            fabContentColor = foreground,
        )
    val shape = appearance.nativeShape(FloatingToolbarDefaults.ContainerShape)
    val elevation = appearance.shadowElevation?.value?.dp
    val toolbarContent: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
        CompositionLocalProvider(
            LocalTextStyle provides appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
        ) {
            content()
        }
    }
    if (props.orientation == DomposableOrientation.Horizontal) {
        HorizontalFloatingToolbar(
            props.expanded,
            native,
            colors = colors,
            shape = shape,
            expandedShadowElevation =
                elevation ?: FloatingToolbarDefaults.ContainerExpandedElevation,
            collapsedShadowElevation =
                elevation ?: FloatingToolbarDefaults.ContainerCollapsedElevation,
            leadingContent = { props.leadingContent() },
            trailingContent = {
                props.trailingContent()
                props.floatingAction()
            },
            content = toolbarContent,
        )
    } else {
        VerticalFloatingToolbar(
            props.expanded,
            native,
            colors = colors,
            shape = shape,
            expandedShadowElevation =
                elevation ?: FloatingToolbarDefaults.ContainerExpandedElevation,
            collapsedShadowElevation =
                elevation ?: FloatingToolbarDefaults.ContainerCollapsedElevation,
            leadingContent = { props.leadingContent() },
            trailingContent = {
                props.trailingContent()
                props.floatingAction()
            },
            content = {
                CompositionLocalProvider(
                    LocalTextStyle provides
                        appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
                ) { content() }
            },
        )
    }
}

@Composable
internal fun NativeFloatingActionButtonMenu(
    props: DomposableFloatingActionButtonMenuProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.floatingActionButtonMenu
    val shape = appearance.nativeShape(FloatingActionButtonDefaults.shape)
    val buttonContainer = appearance.container?.nativeColor() ?: FloatingActionButtonDefaults.containerColor
    val buttonContent = appearance.content?.nativeColor() ?: MaterialTheme.colorScheme.onPrimaryContainer
    val buttonElevation = appearance.shadowElevation?.value?.dp ?: appearance.tonalElevation?.value?.dp ?: 6.dp
    FloatingActionButtonMenu(
        expanded = props.expanded,
        modifier = modifier.toComposeModifier(),
        button = {
            FloatingActionButton(
                onClick = { props.onExpandedChange(!props.expanded) },
                modifier = Modifier.semantics { contentDescription = props.label },
                shape = shape,
                containerColor = buttonContainer,
                contentColor = buttonContent,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = buttonElevation),
            ) { Text(if (props.expanded) "×" else "+") }
        },
    ) {
        props.items.forEach { item ->
            val enabled = item.enabled && domposableFormEnabled()
            FloatingActionButtonMenuItem(
                onClick = { if (enabled) item.onClick() },
                icon = item.icon ?: { Text(item.label.take(1)) },
                modifier =
                    Modifier
                        .then(
                            Modifier.nativeModifier(appearance, shape),
                        ).then(
                            if (enabled) {
                                Modifier
                            } else {
                                Modifier.semantics {
                                    disabled()
                                }
                            },
                        ),
                text = {
                    Text(
                        item.label,
                        style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
                    )
                },
                containerColor =
                    if (enabled) {
                        buttonContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.12f,
                        )
                    },
                contentColor =
                    if (enabled) {
                        buttonContent
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.38f,
                        )
                    },
            )
        }
    }
}

@Composable
internal fun NativeDragHandle(
    props: DomposableDragHandleProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.dragHandle
    Surface(
        modifier =
            modifier.toComposeModifier().semantics { contentDescription = props.label }.draggable(
                state = rememberDraggableState { if (props.enabled) props.onDrag(it) },
                orientation = Orientation.Vertical,
                enabled = props.enabled && domposableFormEnabled(),
            ),
        shape = appearance.nativeShape(MaterialTheme.shapes.extraSmall),
        color = appearance.container?.nativeColor() ?: MaterialTheme.colorScheme.onSurfaceVariant,
        contentColor = appearance.content?.nativeColor() ?: Color.Unspecified,
        tonalElevation = appearance.tonalElevation?.value?.dp ?: 0.dp,
        shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
    ) { Box(Modifier.width(32.dp).padding(vertical = 4.dp)) }
}
