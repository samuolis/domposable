// Direct component calls intentionally share one consistent public vocabulary.
@file:Suppress("TooManyFunctions")

package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableImageScale
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.dp

/** Stable keys preserve each item's composition when the application reorders its data. */
@Composable
fun <T> DomposableLazyColumn(
    items: List<T>,
    key: (T) -> String,
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    itemContent: @Composable (T) -> Unit,
) = DomposableLazyColumn(items, key, modifier, gap, null, itemContent)

@Composable
fun <T> DomposableLazyColumn(
    items: List<T>,
    key: (T) -> String,
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    window: DomposableListWindow?,
    itemContent: @Composable (T) -> Unit,
) = DomposableLazyColumn(
    items.map { item ->
        DomposableLazyItem(key(item)) { itemContent(item) }
    },
    modifier,
    gap,
    window,
)

@Composable
fun <T> DomposableLazyRow(
    items: List<T>,
    key: (T) -> String,
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    itemContent: @Composable (T) -> Unit,
) = DomposableLazyRow(items.map { item -> DomposableLazyItem(key(item)) { itemContent(item) } }, modifier, gap)

@Composable
fun <T> DomposableLazyGrid(
    items: List<T>,
    key: (T) -> String,
    modifier: DomposableModifier = DomposableModifier,
    columns: Int = 2,
    gap: DomposableLength = 8.dp,
    itemContent: @Composable (T) -> Unit,
) = DomposableLazyGrid(
    items.map { item ->
        DomposableLazyItem(key(item)) { itemContent(item) }
    },
    modifier,
    columns,
    gap,
)

@Composable
fun DomposableIconButton(
    onClick: () -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) = DomposableIconButton(DomposableIconButtonProps(onClick, label, enabled), modifier, content)

@Composable
fun DomposableLazyColumn(
    items: List<DomposableLazyItem>,
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
) = DomposableLazyColumn(items, modifier, gap, null)

@Composable
fun DomposableLazyColumn(
    items: List<DomposableLazyItem>,
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    window: DomposableListWindow?,
) = DomposableLazyColumn(DomposableLazyColumnProps(items, gap, window), modifier)

@Composable
fun DomposableLazyRow(
    items: List<DomposableLazyItem>,
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
) = DomposableLazyRow(DomposableLazyRowProps(items, gap), modifier)

@Composable
fun DomposableLazyGrid(
    items: List<DomposableLazyItem>,
    modifier: DomposableModifier = DomposableModifier,
    columns: Int = 2,
    gap: DomposableLength = 8.dp,
) = DomposableLazyGrid(DomposableLazyGridProps(items, columns, gap), modifier)

@Composable
fun DomposableSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
) = DomposableSwitch(DomposableSwitchProps(checked, onCheckedChange, label, enabled), modifier)

@Composable
fun DomposableRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
) = DomposableRadioButton(DomposableRadioButtonProps(selected, onClick, label, enabled), modifier)

@Composable
fun DomposableSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
) = DomposableSlider(DomposableSliderProps(value, onValueChange, label, range, steps, enabled), modifier)

@Composable
fun DomposableSurface(
    modifier: DomposableModifier = DomposableModifier,
    color: DomposableColor? = null,
    content: @Composable () -> Unit,
) = DomposableSurface(DomposableSurfaceProps(color), modifier, content)

@Composable
fun DomposableImage(
    source: String,
    description: String?,
    modifier: DomposableModifier = DomposableModifier,
    onLoaded: (() -> Unit)? = null,
    onError: (() -> Unit)? = null,
    scale: DomposableImageScale = DomposableImageScale.Fit,
) = DomposableImage(DomposableImageProps(source, description, onLoaded, onError, scale), modifier)

@Composable
fun DomposableIcon(
    symbol: String,
    description: String?,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableIcon(DomposableIconProps(symbol, description), modifier)

@Composable
fun DomposableDivider(
    orientation: DomposableOrientation,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableDivider(DomposableDividerProps(orientation), modifier)

@Composable
fun DomposableBadge(
    text: String,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableBadge(DomposableBadgeProps(text), modifier)

@Composable
fun DomposableChip(
    label: String,
    onClick: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    selected: Boolean = false,
    enabled: Boolean = true,
) = DomposableChip(DomposableChipProps(label, onClick, selected, enabled), modifier)

@Composable
fun DomposableAvatar(
    initials: String,
    description: String,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableAvatar(DomposableAvatarProps(initials, description), modifier)

@Composable
fun DomposableBanner(
    message: String,
    modifier: DomposableModifier = DomposableModifier,
    isError: Boolean = false,
) = DomposableBanner(DomposableBannerProps(message, isError), modifier)

@Composable
fun DomposableProgress(
    modifier: DomposableModifier = DomposableModifier,
    value: Float? = null,
    label: String = "Loading",
    circular: Boolean = true,
) = DomposableProgress(DomposableProgressProps(value, label, circular), modifier)

@Composable
fun DomposableTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableTabs(DomposableTabsProps(labels, selectedIndex, onSelect), modifier)

@Composable
fun DomposableNavigationBar(
    items: List<DomposableNavigationItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    orientation: DomposableOrientation = DomposableOrientation.Horizontal,
) = DomposableNavigationBar(DomposableNavigationBarProps(items, selectedIndex, onSelect, orientation), modifier)

@Composable
fun DomposableMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<DomposableMenuItem>,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableMenu(DomposableMenuProps(expanded, onDismiss, items), modifier)

@Composable
fun DomposableDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    actions: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) = DomposableDialog(DomposableDialogProps(title, onDismiss, actions), modifier, content)

@Composable
fun DomposableBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit,
) = DomposableBottomSheet(DomposableBottomSheetProps(title, onDismiss), modifier, content)

@Composable
fun DomposableTopAppBar(
    title: String,
    modifier: DomposableModifier = DomposableModifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
) = DomposableTopAppBar(DomposableTopAppBarProps(title, navigationIcon, actions), modifier)

@Composable
fun DomposableFloatingActionButton(
    onClick: () -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit,
) = DomposableFloatingActionButton(DomposableFloatingActionButtonProps(onClick, label), modifier, content)

@Composable
fun DomposableSnackbar(
    message: String,
    modifier: DomposableModifier = DomposableModifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) = DomposableSnackbar(DomposableSnackbarProps(message, actionLabel, onAction, onDismiss), modifier)

@Composable
fun DomposableSelect(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<DomposableSelectOption>,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    error: String? = null,
) = DomposableSelect(DomposableSelectProps(value, onValueChange, label, options, enabled, error), modifier)

@Composable
fun DomposableSegmentedButtons(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
) = DomposableSegmentedButtons(
    DomposableSegmentedButtonsProps(labels, selectedIndex, onSelect, label, enabled),
    modifier,
)

@Composable
fun DomposableRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    startLabel: String,
    endLabel: String,
    modifier: DomposableModifier = DomposableModifier,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
) = DomposableRangeSlider(
    DomposableRangeSliderProps(value, onValueChange, startLabel, endLabel, range, steps, enabled),
    modifier,
)

@Composable
fun DomposableTooltip(
    text: String,
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit,
) = DomposableTooltip(DomposableTooltipProps(text), modifier, content)

@Composable
fun DomposableButton(
    text: String,
    onClick: () -> Unit,
    style: DomposableButtonStyle,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
) = DomposableButton(onClick, modifier, enabled, style) { DomposableText(text) }
