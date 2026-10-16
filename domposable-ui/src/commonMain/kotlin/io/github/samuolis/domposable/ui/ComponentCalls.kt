// Direct component calls intentionally share one consistent public vocabulary.
@file:Suppress("TooManyFunctions")

package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableChipVariant
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableControlPlacement
import io.github.samuolis.domposable.foundation.DomposableDividerAppearance
import io.github.samuolis.domposable.foundation.DomposableFabSize
import io.github.samuolis.domposable.foundation.DomposableIconAppearance
import io.github.samuolis.domposable.foundation.DomposableIconButtonVariant
import io.github.samuolis.domposable.foundation.DomposableImageScale
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableLiveRegion
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableProgressAppearance
import io.github.samuolis.domposable.foundation.DomposableProgressSemantics
import io.github.samuolis.domposable.foundation.DomposableProgressVariant
import io.github.samuolis.domposable.foundation.DomposableRadioButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableSliderColors
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableSwitchAppearance
import io.github.samuolis.domposable.foundation.DomposableTabsVariant
import io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant
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
) = DomposableLazyRow(items, key, modifier, gap, null, itemContent)

@Composable
fun <T> DomposableLazyRow(
    items: List<T>,
    key: (T) -> String,
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    window: DomposableRowWindow?,
    itemContent: @Composable (T) -> Unit,
) = DomposableLazyRow(items.map { item -> DomposableLazyItem(key(item)) { itemContent(item) } }, modifier, gap, window)

@Composable
fun <T> DomposableLazyGrid(
    items: List<T>,
    key: (T) -> String,
    modifier: DomposableModifier = DomposableModifier,
    columns: Int = 2,
    gap: DomposableLength = 8.dp,
    itemContent: @Composable (T) -> Unit,
) = DomposableLazyGrid(items, key, modifier, columns, gap, null, itemContent)

@Composable
fun <T> DomposableLazyGrid(
    items: List<T>,
    key: (T) -> String,
    modifier: DomposableModifier = DomposableModifier,
    columns: Int = 2,
    gap: DomposableLength = 8.dp,
    window: DomposableGridWindow?,
    itemContent: @Composable (T) -> Unit,
) = DomposableLazyGrid(
    items.map { item ->
        DomposableLazyItem(key(item)) { itemContent(item) }
    },
    modifier,
    columns,
    gap,
    window,
)

@Composable
fun DomposableIconButton(
    onClick: () -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    appearance: DomposableButtonAppearance? = null,
    variant: DomposableIconButtonVariant = DomposableIconButtonVariant.Standard,
    checked: Boolean? = null,
    content: @Composable () -> Unit,
) = DomposableIconButton(
    DomposableIconButtonProps(
        onClick,
        label,
        enabled,
        appearance = appearance,
        variant = variant,
        checked = checked,
    ),
    modifier,
    content,
)

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
) = DomposableLazyRow(items, modifier, gap, null)

@Composable
fun DomposableLazyRow(
    items: List<DomposableLazyItem>,
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    window: DomposableRowWindow?,
) = DomposableLazyRow(DomposableLazyRowProps(items, gap, window), modifier)

@Composable
fun DomposableLazyGrid(
    items: List<DomposableLazyItem>,
    modifier: DomposableModifier = DomposableModifier,
    columns: Int = 2,
    gap: DomposableLength = 8.dp,
) = DomposableLazyGrid(items, modifier, columns, gap, null)

@Composable
fun DomposableLazyGrid(
    items: List<DomposableLazyItem>,
    modifier: DomposableModifier = DomposableModifier,
    columns: Int = 2,
    gap: DomposableLength = 8.dp,
    window: DomposableGridWindow?,
) = DomposableLazyGrid(DomposableLazyGridProps(items, columns, gap, window), modifier)

@Composable
fun DomposableSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    appearance: DomposableSwitchAppearance? = null,
    showLabel: Boolean = true,
    description: String? = null,
    controlPlacement: DomposableControlPlacement = DomposableControlPlacement.Leading,
) = DomposableSwitch(
    DomposableSwitchProps(
        checked,
        onCheckedChange,
        label,
        enabled,
        appearance = appearance,
        showLabel = showLabel,
        description = description,
        controlPlacement = controlPlacement,
    ),
    modifier,
)

@Composable
fun DomposableRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    appearance: DomposableRadioButtonAppearance? = null,
    showLabel: Boolean = true,
    description: String? = null,
    controlPlacement: DomposableControlPlacement = DomposableControlPlacement.Leading,
) = DomposableRadioButton(
    DomposableRadioButtonProps(
        selected,
        onClick,
        label,
        enabled,
        appearance = appearance,
        showLabel = showLabel,
        description = description,
        controlPlacement = controlPlacement,
    ),
    modifier,
)

@Composable
fun DomposableSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    colors: DomposableSliderColors? = null,
    orientation: DomposableOrientation = DomposableOrientation.Horizontal,
    topToBottom: Boolean = true,
) = DomposableSlider(
    DomposableSliderProps(
        value,
        onValueChange,
        label,
        range,
        steps,
        enabled,
        colors = colors,
        orientation = orientation,
        topToBottom = topToBottom,
    ),
    modifier,
)

@Composable
fun DomposableSurface(
    modifier: DomposableModifier = DomposableModifier,
    color: DomposableColor? = null,
    appearance: DomposableSurfaceAppearance? = null,
    content: @Composable () -> Unit,
) = DomposableSurface(DomposableSurfaceProps(color, appearance = appearance), modifier, content)

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
    appearance: DomposableIconAppearance? = null,
) = DomposableIcon(DomposableIconProps(symbol, description, appearance = appearance), modifier)

@Composable
fun DomposableDivider(
    orientation: DomposableOrientation,
    modifier: DomposableModifier = DomposableModifier,
    appearance: DomposableDividerAppearance? = null,
) = DomposableDivider(DomposableDividerProps(orientation, appearance = appearance), modifier)

@Composable
fun DomposableBadge(
    text: String? = null,
    modifier: DomposableModifier = DomposableModifier,
    appearance: DomposableSurfaceAppearance? = null,
    label: String? = null,
) = DomposableBadge(DomposableBadgeProps(text, appearance = appearance, label = label), modifier)

@Composable
fun DomposableChip(
    label: String,
    onClick: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    appearance: DomposableItemAppearance? = null,
    variant: DomposableChipVariant = DomposableChipVariant.Filter,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    elevated: Boolean = false,
) = DomposableChip(
    DomposableChipProps(
        label,
        onClick,
        selected,
        enabled,
        appearance = appearance,
        variant = variant,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        elevated = elevated,
    ),
    modifier,
)

@Composable
fun DomposableAvatar(
    initials: String,
    description: String,
    modifier: DomposableModifier = DomposableModifier,
    appearance: DomposableSurfaceAppearance? = null,
) = DomposableAvatar(DomposableAvatarProps(initials, description, appearance = appearance), modifier)

@Composable
fun DomposableBanner(
    message: String,
    modifier: DomposableModifier = DomposableModifier,
    isError: Boolean = false,
    appearance: DomposableSurfaceAppearance? = null,
) = DomposableBanner(DomposableBannerProps(message, isError, appearance = appearance), modifier)

@Composable
fun DomposableProgress(
    modifier: DomposableModifier = DomposableModifier,
    value: Float? = null,
    label: String? = "Loading",
    circular: Boolean = true,
    appearance: DomposableProgressAppearance? = null,
    variant: DomposableProgressVariant = DomposableProgressVariant.Standard,
    liveRegion: DomposableLiveRegion? = null,
    semantics: DomposableProgressSemantics = DomposableProgressSemantics.ProgressBar,
) = DomposableProgress(
    DomposableProgressProps(
        value,
        label,
        circular,
        appearance = appearance,
        variant = variant,
        liveRegion = liveRegion,
        semantics = semantics,
    ),
    modifier,
)

@Composable
fun DomposableTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    appearance: DomposableItemAppearance? = null,
    variant: DomposableTabsVariant = DomposableTabsVariant.Primary,
    scrollable: Boolean = false,
    icons: List<(@Composable () -> Unit)?> = labels.map { null },
    leadingIcons: Boolean = false,
) = DomposableTabs(
    DomposableTabsProps(
        labels,
        selectedIndex,
        onSelect,
        appearance = appearance,
        variant = variant,
        scrollable = scrollable,
        icons = icons,
        leadingIcons = leadingIcons,
    ),
    modifier,
)

@Composable
fun DomposableNavigationBar(
    items: List<DomposableNavigationItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    orientation: DomposableOrientation = DomposableOrientation.Horizontal,
    appearance: DomposableItemAppearance? = null,
    short: Boolean = false,
    label: String = "Application",
    webLayout: DomposableWebNavigationLayout = DomposableWebNavigationLayout.Material,
) = DomposableNavigationBar(
    DomposableNavigationBarProps(
        items,
        selectedIndex,
        onSelect,
        orientation,
        appearance = appearance,
        short = short,
        label = label,
        webLayout = webLayout,
    ),
    modifier,
)

@Composable
fun DomposableMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<DomposableMenuItem> = emptyList(),
    modifier: DomposableModifier = DomposableModifier,
    appearance: DomposableSurfaceAppearance? = null,
    content: @Composable () -> Unit = {},
) = DomposableMenu(DomposableMenuProps(expanded, onDismiss, items, appearance = appearance), modifier, content)

@Composable
fun DomposableDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    actions: @Composable () -> Unit = {},
    appearance: DomposableSurfaceAppearance? = null,
    variant: DomposableDialogVariant = DomposableDialogVariant.Alert,
    content: @Composable () -> Unit,
) = DomposableDialog(
    DomposableDialogProps(title, onDismiss, actions, appearance = appearance, variant = variant),
    modifier,
    content,
)

@Composable
fun DomposableBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    appearance: DomposableSurfaceAppearance? = null,
    modal: Boolean = true,
    content: @Composable () -> Unit,
) = DomposableBottomSheet(
    DomposableBottomSheetProps(title, onDismiss, appearance = appearance, modal = modal),
    modifier,
    content,
)

@Composable
fun DomposableTopAppBar(
    title: String,
    modifier: DomposableModifier = DomposableModifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
    appearance: DomposableSurfaceAppearance? = null,
    variant: DomposableTopAppBarVariant = DomposableTopAppBarVariant.Small,
    subtitle: String? = null,
) = DomposableTopAppBar(
    DomposableTopAppBarProps(
        title,
        navigationIcon,
        actions,
        appearance = appearance,
        variant = variant,
        subtitle = subtitle,
    ),
    modifier,
)

@Composable
fun DomposableFloatingActionButton(
    onClick: () -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    appearance: DomposableSurfaceAppearance? = null,
    size: DomposableFabSize = DomposableFabSize.Standard,
    extended: Boolean = false,
    content: @Composable () -> Unit,
) = DomposableFloatingActionButton(
    DomposableFloatingActionButtonProps(onClick, label, appearance = appearance, size = size, extended = extended),
    modifier,
    content,
)

@Composable
fun DomposableSnackbar(
    message: String,
    modifier: DomposableModifier = DomposableModifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    appearance: DomposableSurfaceAppearance? = null,
) = DomposableSnackbar(
    DomposableSnackbarProps(message, actionLabel, onAction, onDismiss, appearance = appearance),
    modifier,
)

@Composable
fun DomposableSelect(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<DomposableSelectOption>,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    error: String? = null,
    appearance: DomposableButtonAppearance? = null,
) = DomposableSelect(
    DomposableSelectProps(value, onValueChange, label, options, enabled, error, appearance = appearance),
    modifier,
)

@Composable
fun DomposableSegmentedButtons(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    appearance: DomposableItemAppearance? = null,
) = DomposableSegmentedButtons(
    DomposableSegmentedButtonsProps(labels, selectedIndex, onSelect, label, enabled, appearance = appearance),
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
    colors: DomposableSliderColors? = null,
) = DomposableRangeSlider(
    DomposableRangeSliderProps(value, onValueChange, startLabel, endLabel, range, steps, enabled, colors = colors),
    modifier,
)

@Composable
fun DomposableTooltip(
    text: String,
    modifier: DomposableModifier = DomposableModifier,
    appearance: DomposableSurfaceAppearance? = null,
    title: String? = null,
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) = DomposableTooltip(
    DomposableTooltipProps(text, appearance = appearance, title = title, action = action),
    modifier,
    content,
)

@Composable
fun DomposableButton(
    text: String,
    onClick: () -> Unit,
    style: DomposableButtonStyle,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
) = DomposableButton(onClick, modifier, enabled, style) { DomposableText(text) }
