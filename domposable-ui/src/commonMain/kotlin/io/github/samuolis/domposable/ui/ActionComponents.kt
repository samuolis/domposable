package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableComponent
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableProgressAppearance
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.foundation.dp

/** Stable action identity shared by groups and action menus. Icon rendering remains caller-owned. */
data class DomposableActionItem(
    val key: String,
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val icon: (@Composable () -> Unit)? = null,
) {
    init {
        require(key.isNotBlank() && label.isNotBlank())
    }
}

enum class DomposableListItemVariant { Standard, Segmented }

enum class DomposableListItemPosition { Only, First, Middle, Last }

enum class DomposableDatePickerMode { Calendar, Input }

enum class DomposableTimePickerMode { Dial, Input }

enum class DomposableCarouselVariant { MultiBrowse, Uncontained, CenteredHero }

enum class DomposableSwipeDirection { StartToEnd, EndToStart }

@DomposableComponent("ToggleButton")
data class DomposableToggleButtonProps(
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val enabled: Boolean = true,
    val style: DomposableButtonStyle = DomposableButtonStyle.Filled,
    val appearance: DomposableButtonAppearance? = null,
)

@Composable
fun DomposableToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    style: DomposableButtonStyle = DomposableButtonStyle.Filled,
    appearance: DomposableButtonAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit,
) = DomposableToggleButton(
    DomposableToggleButtonProps(checked, onCheckedChange, enabled, style, appearance),
    modifier,
    content,
)

@DomposableComponent("ButtonGroup")
data class DomposableButtonGroupProps(
    val items: List<DomposableActionItem>,
    val label: String,
    val spacing: DomposableLength = 8.dp,
    val appearance: DomposableButtonAppearance? = null,
) {
    init {
        require(items.map { it.key }.distinct().size == items.size)
    }
}

@Composable
fun DomposableButtonGroup(
    items: List<DomposableActionItem>,
    label: String,
    spacing: DomposableLength = 8.dp,
    appearance: DomposableButtonAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableButtonGroup(
    DomposableButtonGroupProps(items, label, spacing, appearance),
    modifier,
)

@DomposableComponent("SplitButton")
@Suppress("LongParameterList")
data class DomposableSplitButtonProps(
    val label: String,
    val onClick: () -> Unit,
    val secondaryLabel: String,
    val onSecondaryClick: () -> Unit,
    val expanded: Boolean = false,
    val enabled: Boolean = true,
    val style: DomposableButtonStyle = DomposableButtonStyle.Filled,
    val appearance: DomposableButtonAppearance? = null,
)

@Composable
@Suppress("LongParameterList")
fun DomposableSplitButton(
    label: String,
    onClick: () -> Unit,
    secondaryLabel: String,
    onSecondaryClick: () -> Unit,
    expanded: Boolean = false,
    enabled: Boolean = true,
    style: DomposableButtonStyle = DomposableButtonStyle.Filled,
    appearance: DomposableButtonAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableSplitButton(
    DomposableSplitButtonProps(label, onClick, secondaryLabel, onSecondaryClick, expanded, enabled, style, appearance),
    modifier,
)

@DomposableComponent("LoadingIndicator")
data class DomposableLoadingIndicatorProps(
    val label: String = "Loading",
    val contained: Boolean = false,
    val appearance: DomposableProgressAppearance? = null,
    val containerColor: DomposableColor? = null,
    val progress: Float? = null,
) {
    init {
        require(progress == null || (progress.isFinite() && progress in 0f..1f))
    }
}

@Composable
fun DomposableLoadingIndicator(
    label: String = "Loading",
    contained: Boolean = false,
    appearance: DomposableProgressAppearance? = null,
    containerColor: DomposableColor? = null,
    modifier: DomposableModifier = DomposableModifier,
    progress: Float? = null,
) = DomposableLoadingIndicator(
    DomposableLoadingIndicatorProps(label, contained, appearance, containerColor, progress),
    modifier,
)

@DomposableComponent("FloatingToolbar")
data class DomposableFloatingToolbarProps(
    val expanded: Boolean,
    val orientation: DomposableOrientation = DomposableOrientation.Horizontal,
    val leadingContent: @Composable () -> Unit = {},
    val trailingContent: @Composable () -> Unit = {},
    val floatingAction: @Composable () -> Unit = {},
    val appearance: DomposableSurfaceAppearance? = null,
    val variant: DomposableFloatingToolbarVariant = DomposableFloatingToolbarVariant.Standard,
    val contentPadding: DomposableInsets = DomposableInsets(8.dp),
)

/** Standard Material toolbar colors or the high-emphasis expressive treatment. */
enum class DomposableFloatingToolbarVariant { Standard, Vibrant }

@Composable
@Suppress("LongParameterList")
fun DomposableFloatingToolbar(
    expanded: Boolean,
    orientation: DomposableOrientation = DomposableOrientation.Horizontal,
    leadingContent: @Composable () -> Unit = {},
    trailingContent: @Composable () -> Unit = {},
    floatingAction: @Composable () -> Unit = {},
    appearance: DomposableSurfaceAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
    variant: DomposableFloatingToolbarVariant = DomposableFloatingToolbarVariant.Standard,
    contentPadding: DomposableInsets = DomposableInsets(8.dp),
    content: @Composable () -> Unit,
) = DomposableFloatingToolbar(
    DomposableFloatingToolbarProps(
        expanded,
        orientation,
        leadingContent,
        trailingContent,
        floatingAction,
        appearance,
        variant,
        contentPadding,
    ),
    modifier,
    content,
)

@DomposableComponent("FloatingActionButtonMenu")
data class DomposableFloatingActionButtonMenuProps(
    val expanded: Boolean,
    val onExpandedChange: (Boolean) -> Unit,
    val label: String,
    val items: List<DomposableActionItem>,
    val appearance: DomposableSurfaceAppearance? = null,
) {
    init {
        require(items.map { it.key }.distinct().size == items.size)
    }
}

@Composable
fun DomposableFloatingActionButtonMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    label: String,
    items: List<DomposableActionItem>,
    appearance: DomposableSurfaceAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableFloatingActionButtonMenu(
    DomposableFloatingActionButtonMenuProps(expanded, onExpandedChange, label, items, appearance),
    modifier,
)

@DomposableComponent("DragHandle")
data class DomposableDragHandleProps(
    val label: String,
    val onDrag: (Float) -> Unit,
    val enabled: Boolean = true,
    val appearance: DomposableSurfaceAppearance? = null,
)

@Composable
fun DomposableDragHandle(
    label: String,
    onDrag: (Float) -> Unit,
    enabled: Boolean = true,
    appearance: DomposableSurfaceAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableDragHandle(
    DomposableDragHandleProps(label, onDrag, enabled, appearance),
    modifier,
)

/** Controlled icon toggle; the app owns selection just as it does for a checkbox. */
@Composable
fun DomposableIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    variant: io.github.samuolis.domposable.foundation.DomposableIconButtonVariant =
        io.github.samuolis.domposable.foundation.DomposableIconButtonVariant.Standard,
    appearance: DomposableButtonAppearance? = null,
    content: @Composable () -> Unit,
) = DomposableIconButton(
    DomposableIconButtonProps({ onCheckedChange(!checked) }, label, enabled, appearance, variant, checked),
    modifier,
    content,
)

enum class DomposableDrawerVariant { Modal, Dismissible, Permanent }

enum class DomposableSearchBarVariant { FullScreen, Docked, ContainedFullScreen, DockedWithGap, AppBar }

enum class DomposableDialogVariant { Alert, Basic, DatePicker, TimePicker, FullScreen }

enum class DomposableBottomAppBarVariant { Standard, Flexible }

enum class DomposableToggleState { Off, On, Indeterminate }
