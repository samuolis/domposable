@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AppBarColumn
import androidx.compose.material3.AppBarMenuState
import androidx.compose.material3.AppBarOverflowIndicator
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.AppBarScope
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.ui.DomposableAppBarProps
import io.github.samuolis.domposable.ui.DomposableBottomAppBarProps
import io.github.samuolis.domposable.ui.DomposableBottomAppBarVariant
import io.github.samuolis.domposable.ui.DomposableBottomSheetScaffoldProps
import io.github.samuolis.domposable.ui.DomposableDrawerVariant
import io.github.samuolis.domposable.ui.DomposableIconButtonProps
import io.github.samuolis.domposable.ui.DomposableListItemPosition
import io.github.samuolis.domposable.ui.DomposableListItemProps
import io.github.samuolis.domposable.ui.DomposableListItemVariant
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableMultiChoiceSegmentedButtonsProps
import io.github.samuolis.domposable.ui.DomposableNavigationDrawerItemProps
import io.github.samuolis.domposable.ui.DomposableNavigationDrawerProps
import io.github.samuolis.domposable.ui.DomposableWideNavigationRailItemProps
import io.github.samuolis.domposable.ui.DomposableWideNavigationRailProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableFormEnabled
import io.github.samuolis.domposable.ui.toComposeModifier
import kotlinx.coroutines.launch

@Composable
internal fun NativeAppBar(
    props: DomposableAppBarProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.appBar
    val formEnabled = domposableFormEnabled()
    val overflow: @Composable (AppBarMenuState) -> Unit = {
        Box(Modifier.semantics { contentDescription = props.overflowLabel }) { AppBarOverflowIndicator(it) }
    }
    val addItems: AppBarScope.() -> Unit = {
        props.items.forEach { item ->
            customItem(
                appbarContent = {
                    val spacing =
                        if (props.orientation ==
                            DomposableOrientation.Horizontal
                        ) {
                            Modifier.padding(end = props.spacing.value.dp)
                        } else {
                            Modifier.padding(bottom = props.spacing.value.dp)
                        }
                    Box(spacing) {
                        NativeIconButton(
                            DomposableIconButtonProps(
                                item.onClick,
                                item.label,
                                item.enabled && formEnabled,
                                appearance,
                            ),
                            DomposableModifier,
                        ) { item.icon?.invoke() ?: Text(item.label.take(1)) }
                    }
                },
                menuContent = { menuState ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                item.label,
                                style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
                            )
                        },
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
    if (props.orientation == DomposableOrientation.Horizontal) {
        AppBarRow(modifier = modifier.toComposeModifier(), overflowIndicator = overflow, content = addItems)
    } else {
        AppBarColumn(modifier = modifier.toComposeModifier(), overflowIndicator = overflow, content = addItems)
    }
}

@Composable
internal fun NativeMultiChoiceSegmentedButtons(
    props: DomposableMultiChoiceSegmentedButtonsProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.multiChoiceSegmentedButtons
    androidx.compose.runtime.CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides (appearance.minimumInteractiveSize?.value?.dp ?: 48.dp),
    ) {
        MultiChoiceSegmentedButtonRow(modifier.toComposeModifier().semantics { contentDescription = props.label }) {
            props.labels.forEachIndexed { index, label ->
                val selected = index in props.selectedIndices
                val enabled = props.enabled && props.optionEnabled[index] && domposableFormEnabled()
                val interaction =
                    remember {
                        androidx.compose.foundation.interaction
                            .MutableInteractionSource()
                    }
                val resolved = appearance.resolveInteractionColors(interaction, selected, enabled)
                val borderWidth =
                    if (selected) {
                        appearance.selectedBorderWidth?.value?.dp
                            ?: appearance.borderWidth.value.dp
                    } else {
                        appearance.borderWidth.value.dp
                    }
                val textAppearance =
                    if (selected) {
                        appearance.selectedTextAppearance ?: appearance.textAppearance
                    } else {
                        appearance.textAppearance
                    }
                SegmentedButton(
                    checked = selected,
                    onCheckedChange = { checked ->
                        props.onSelectionChange(
                            if (checked) {
                                props.selectedIndices + index
                            } else {
                                props.selectedIndices -
                                    index
                            },
                        )
                    },
                    shape =
                        appearance.shape?.nativeShape() ?: SegmentedButtonDefaults.itemShape(index, props.labels.size),
                    enabled = enabled,
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
                            disabledActiveContainerColor =
                                appearance.colors.disabledContainer?.nativeColor() ?: Color.Unspecified,
                            disabledActiveContentColor =
                                appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified,
                            disabledActiveBorderColor =
                                appearance.colors.disabledBorder?.nativeColor() ?: Color.Unspecified,
                            disabledInactiveContainerColor =
                                appearance.colors.disabledContainer?.nativeColor() ?: Color.Unspecified,
                            disabledInactiveContentColor =
                                appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified,
                            disabledInactiveBorderColor =
                                appearance.colors.disabledBorder?.nativeColor() ?: Color.Unspecified,
                        ),
                    border =
                        androidx.compose.foundation.BorderStroke(
                            borderWidth,
                            if (resolved.border ==
                                Color.Unspecified
                            ) {
                                MaterialTheme.colorScheme.outline
                            } else {
                                resolved.border
                            },
                        ),
                    label = {
                        Text(
                            label,
                            style = textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
                        )
                    },
                )
            }
        }
    }
}

@Composable
internal fun NativeBottomAppBar(
    props: DomposableBottomAppBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.bottomAppBar
    val native = modifier.toComposeModifier()
    val color = appearance.container?.nativeColor()
    val foreground = appearance.content?.nativeColor()
    if (props.variant == DomposableBottomAppBarVariant.Flexible) {
        FlexibleBottomAppBar(
            native,
            containerColor = color ?: BottomAppBarDefaults.containerColor,
            contentColor =
                foreground ?: contentColorFor(color ?: BottomAppBarDefaults.containerColor),
        ) {
            content()
            props.floatingAction()
        }
    } else {
        BottomAppBar(
            modifier = native,
            containerColor = color ?: BottomAppBarDefaults.containerColor,
            contentColor =
                foreground ?: contentColorFor(color ?: BottomAppBarDefaults.containerColor),
            actions = { content() },
            floatingActionButton = { props.floatingAction() },
        )
    }
}

@Composable
internal fun NativeListItem(
    props: DomposableListItemProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.listItem
    val enabled = props.enabled && domposableFormEnabled()
    val interaction =
        remember {
            androidx.compose.foundation.interaction
                .MutableInteractionSource()
        }
    val resolved = appearance.resolveInteractionColors(interaction, props.selected, enabled)
    val selectedBorderWidth =
        if (props.selected) {
            appearance.selectedBorderWidth?.value?.dp
                ?: appearance.borderWidth.value.dp
        } else {
            appearance.borderWidth.value.dp
        }
    val border =
        resolved.border
            .takeUnless {
                it == Color.Unspecified
            }?.let { androidx.compose.foundation.BorderStroke(selectedBorderWidth, it) }
    val defaultShapes = ListItemDefaults.shapes(shape = appearance.shape?.nativeShape())
    val shapeIndexAndCount =
        when (props.position) {
            DomposableListItemPosition.Only -> 0 to 1
            DomposableListItemPosition.First -> 0 to 2
            DomposableListItemPosition.Middle -> 1 to 3
            DomposableListItemPosition.Last -> 2 to 3
        }
    val shapes =
        if (props.variant == DomposableListItemVariant.Segmented) {
            ListItemDefaults.segmentedShapes(shapeIndexAndCount.first, shapeIndexAndCount.second, defaultShapes)
        } else {
            defaultShapes
        }
    val textStyle =
        (
            if (props.selected) {
                appearance.selectedTextAppearance ?: appearance.textAppearance
            } else {
                appearance.textAppearance
            }
        ).nativeTextStyle(MaterialTheme.typography.bodyLarge)
    val contentSpacing = appearance.contentSpacing
    val supporting: (@Composable () -> Unit)? =
        props.supportingText?.let { text ->
            { Text(text, style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.bodyMedium)) }
        }
    val spacedHeadline: @Composable () -> Unit = {
        if (contentSpacing != null && supporting != null) {
            Column(verticalArrangement = Arrangement.spacedBy(contentSpacing.value.dp)) {
                Text(props.headline, style = textStyle)
                supporting()
            }
        } else {
            Text(props.headline, style = textStyle)
        }
    }
    val nativeModifier =
        modifier
            .toComposeModifier()
            .then(appearance.minHeight?.let { Modifier.heightIn(min = it.value.dp) } ?: Modifier)
            .then(
                border?.let { Modifier.border(it, appearance.shape?.nativeShape() ?: MaterialTheme.shapes.small) }
                    ?: Modifier,
            ).semantics {
                selected = props.selected
                if (!enabled) disabled()
            }
    val colors =
        ListItemDefaults.colors(
            containerColor = resolved.container,
            contentColor = resolved.content,
            disabledContainerColor = appearance.colors.disabledContainer?.nativeColor() ?: Color.Unspecified,
            disabledContentColor = appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified,
            selectedContainerColor = resolved.selectedContainer,
            selectedContentColor = resolved.selectedContent,
        )
    val selectedColors =
        ListItemDefaults.segmentedColors(
            containerColor = resolved.container,
            contentColor = resolved.content,
            disabledContainerColor = appearance.colors.disabledContainer?.nativeColor() ?: Color.Unspecified,
            disabledContentColor = appearance.colors.disabledContent?.nativeColor() ?: Color.Unspecified,
            selectedContainerColor = resolved.selectedContainer,
            selectedContentColor = resolved.selectedContent,
        )
    val minimum = appearance.minimumInteractiveSize?.value?.dp ?: 48.dp
    val click = props.onClick
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.material3.LocalMinimumInteractiveComponentSize provides minimum,
    ) {
        if (props.variant == DomposableListItemVariant.Segmented) {
            if (click != null) {
                SegmentedListItem(
                    selected = props.selected,
                    onClick = { click() },
                    shapes = shapes,
                    modifier = nativeModifier,
                    enabled = enabled,
                    leadingContent = { props.leadingContent() },
                    trailingContent = { props.trailingContent() },
                    overlineContent = props.overline?.let { { Text(it) } },
                    supportingContent =
                        if (appearance.contentSpacing ==
                            null
                        ) {
                            supporting
                        } else {
                            null
                        },
                    colors = selectedColors,
                    contentPadding =
                        appearance.contentPadding?.nativePadding() ?: ListItemDefaults.ContentPadding,
                    interactionSource = interaction,
                    content = spacedHeadline,
                )
            } else {
                ListItem(
                    headlineContent = spacedHeadline,
                    modifier = nativeModifier.clip(shapes.shape),
                    leadingContent = { props.leadingContent() },
                    trailingContent = { props.trailingContent() },
                    overlineContent = props.overline?.let { text -> { Text(text) } },
                    supportingContent = if (appearance.contentSpacing == null) supporting else null,
                    colors =
                        selectedColors.copy(
                            containerColor = selectedColors.containerColor(enabled, props.selected, false),
                            contentColor = selectedColors.contentColor(enabled, props.selected, false),
                            leadingContentColor = selectedColors.leadingContentColor(enabled, props.selected, false),
                            trailingContentColor = selectedColors.trailingContentColor(enabled, props.selected, false),
                            overlineContentColor = selectedColors.overlineContentColor(enabled, props.selected, false),
                            supportingContentColor =
                                selectedColors.supportingContentColor(
                                    enabled,
                                    props.selected,
                                    false,
                                ),
                        ),
                )
            }
        } else if (click != null) {
            ListItem(
                onClick = { click() },
                modifier = nativeModifier,
                enabled = enabled,
                leadingContent = { props.leadingContent() },
                trailingContent = { props.trailingContent() },
                overlineContent =
                    props.overline?.let { text ->
                        { Text(text) }
                    },
                supportingContent =
                    if (appearance.contentSpacing ==
                        null
                    ) {
                        supporting
                    } else {
                        null
                    },
                shapes = shapes,
                colors = colors,
                contentPadding = appearance.contentPadding?.nativePadding() ?: ListItemDefaults.ContentPadding,
                interactionSource = interaction,
                content = spacedHeadline,
            )
        } else {
            ListItem(
                headlineContent = spacedHeadline,
                modifier = nativeModifier,
                overlineContent =
                    props.overline?.let { text ->
                        { Text(text) }
                    },
                supportingContent =
                    if (contentSpacing ==
                        null
                    ) {
                        supporting
                    } else {
                        null
                    },
                leadingContent = { props.leadingContent() },
                trailingContent = { props.trailingContent() },
                colors = colors,
            )
        }
    }
}

@Composable
internal fun NativeBottomSheetScaffold(
    props: DomposableBottomSheetScaffoldProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeWindow { featureRegistry ->
        val initial = if (props.expanded) SheetValue.Expanded else SheetValue.PartiallyExpanded
        val sheetState =
            rememberBottomSheetState(
                initialValue = initial,
                enabledValues = setOf(SheetValue.PartiallyExpanded, SheetValue.Expanded),
            )
        val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
        LaunchedEffect(props.expanded) {
            if (props.expanded) sheetState.expand() else sheetState.partialExpand()
        }
        LaunchedEffect(sheetState.currentValue) {
            val expanded = sheetState.currentValue == SheetValue.Expanded
            if (expanded != props.expanded) props.onExpandedChange(expanded)
        }
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.bottomSheetScaffold
        BottomSheetScaffold(
            sheetContent = {
                CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) {
                    props.sheetContent()
                }
            },
            modifier = modifier.toComposeModifier(),
            scaffoldState = scaffoldState,
            sheetPeekHeight = props.peekHeight.value.dp,
            sheetShape = appearance.nativeShape(BottomSheetDefaults.ExpandedShape),
            sheetContainerColor = appearance.container?.nativeColor() ?: BottomSheetDefaults.ContainerColor,
            sheetContentColor = appearance.content?.nativeColor() ?: MaterialTheme.colorScheme.onSurface,
            sheetTonalElevation = appearance.tonalElevation?.value?.dp ?: 0.dp,
            sheetShadowElevation = appearance.shadowElevation?.value?.dp ?: BottomSheetDefaults.Elevation,
            sheetSwipeEnabled = props.swipeEnabled,
            content = {
                CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) { content() }
            },
        )
    }
}

@Composable
internal fun NativeNavigationDrawer(
    props: DomposableNavigationDrawerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeWindow { featureRegistry ->
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.navigationDrawer
        val state = rememberDrawerState(if (props.expanded) DrawerValue.Open else DrawerValue.Closed)
        val featureContent: @Composable () -> Unit = {
            CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) { content() }
        }
        LaunchedEffect(props.expanded) { if (props.expanded) state.open() else state.close() }
        LaunchedEffect(state.isOpen) { if (!state.isOpen && props.expanded) props.onDismiss() }
        val drawerContent: @Composable () -> Unit = {
            NativeDrawerSheet(props.label, appearance, DrawerDefaults.modalContainerColor, featureContent)
        }
        when (props.variant) {
            DomposableDrawerVariant.Modal -> {
                ModalNavigationDrawer(
                    drawerContent,
                    modifier.toComposeModifier(),
                    state,
                    props.gesturesEnabled,
                    content = featureContent,
                )
            }

            DomposableDrawerVariant.Dismissible -> {
                DismissibleNavigationDrawer({
                    NativeDrawerSheet(
                        props.label,
                        appearance,
                        DrawerDefaults.standardContainerColor,
                        featureContent,
                        dismissible = true,
                    )
                }, modifier.toComposeModifier(), state, props.gesturesEnabled, featureContent)
            }

            DomposableDrawerVariant.Permanent -> {
                PermanentNavigationDrawer({
                    NativeDrawerSheet(
                        props.label,
                        appearance,
                        DrawerDefaults.standardContainerColor,
                        featureContent,
                        permanent = true,
                    )
                }, modifier.toComposeModifier(), featureContent)
            }
        }
    }
}

@Composable
private fun NativeDrawerSheet(
    label: String,
    appearance: DomposableSurfaceAppearance,
    defaultColor: Color,
    content: @Composable () -> Unit,
    dismissible: Boolean = false,
    permanent: Boolean = false,
) {
    val shape = appearance.nativeShape(DrawerDefaults.shape)
    val color = appearance.container?.nativeColor() ?: defaultColor
    when {
        permanent -> {
            PermanentDrawerSheet(
                drawerShape = shape,
                drawerContainerColor = color,
                drawerContentColor =
                    appearance.content?.nativeColor() ?: contentColorFor(color),
                drawerTonalElevation =
                    appearance.tonalElevation?.value?.dp ?: 0.dp,
            ) {
                Column {
                    Text(label, Modifier.padding(16.dp))
                    content()
                }
            }
        }

        dismissible -> {
            DismissibleDrawerSheet(
                drawerShape = shape,
                drawerContainerColor = color,
                drawerContentColor =
                    appearance.content?.nativeColor() ?: contentColorFor(color),
                drawerTonalElevation =
                    appearance.tonalElevation?.value?.dp ?: 0.dp,
            ) {
                Column {
                    Text(label, Modifier.padding(16.dp))
                    content()
                }
            }
        }

        else -> {
            ModalDrawerSheet(
                drawerShape = shape,
                drawerContainerColor = color,
                drawerContentColor =
                    appearance.content?.nativeColor() ?: contentColorFor(color),
                drawerTonalElevation =
                    appearance.tonalElevation?.value?.dp ?: DrawerDefaults.ModalDrawerElevation,
            ) {
                Column {
                    Text(label, Modifier.padding(16.dp))
                    content()
                }
            }
        }
    }
}

@Composable
internal fun NativeNavigationDrawerItem(
    props: DomposableNavigationDrawerItemProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.navigationDrawerItem
    val formEnabled = domposableFormEnabled()
    NavigationDrawerItem(
        label = {
            Text(
                props.label,
                style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
            )
        },
        selected = props.selected,
        onClick = { if (props.enabled && formEnabled) props.onClick() },
        modifier = modifier.toComposeModifier(),
        icon = props.icon,
        badge = props.badge,
    )
}

@Composable
internal fun NativeWideNavigationRail(
    props: DomposableWideNavigationRailProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeWindow { featureRegistry ->
        val appearance = props.appearance ?: LocalDomposableTokens.current.components.wideNavigationRail
        val state =
            rememberWideNavigationRailState(
                if (props.expanded) WideNavigationRailValue.Expanded else WideNavigationRailValue.Collapsed,
            )
        val featureContent: @Composable () -> Unit = {
            CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) { content() }
        }
        val featureHeader: @Composable () -> Unit = {
            CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) { props.header() }
        }
        LaunchedEffect(props.expanded) { if (props.expanded) state.expand() else state.collapse() }
        LaunchedEffect(state.targetValue) {
            if (props.modal && state.targetValue == WideNavigationRailValue.Collapsed && props.expanded) {
                props.onDismiss()
            }
        }
        val colors =
            WideNavigationRailDefaults.colors(
                containerColor = appearance.container?.nativeColor() ?: Color.Unspecified,
                contentColor = appearance.content?.nativeColor() ?: Color.Unspecified,
            )
        if (props.modal) {
            ModalWideNavigationRail(
                modifier = modifier.toComposeModifier(),
                state = state,
                expandedShape = appearance.nativeShape(WideNavigationRailDefaults.modalExpandedShape),
                colors = colors,
                header = featureHeader,
                content = featureContent,
            )
        } else {
            WideNavigationRail(
                modifier = modifier.toComposeModifier(),
                state = state,
                shape = appearance.nativeShape(WideNavigationRailDefaults.shape),
                colors = colors,
                header = featureHeader,
                content = featureContent,
            )
        }
    }
}

@Composable
internal fun NativeWideNavigationRailItem(
    props: DomposableWideNavigationRailItemProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.wideNavigationRailItem
    WideNavigationRailItem(
        selected = props.selected,
        onClick = props.onClick,
        icon = props.icon,
        label = {
            Text(
                props.label,
                style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
            )
        },
        railExpanded = props.railExpanded,
        modifier = modifier.toComposeModifier(),
        enabled = props.enabled && domposableFormEnabled(),
    )
}
