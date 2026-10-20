package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.key
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableAutocomplete
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableCardVariant
import io.github.samuolis.domposable.foundation.DomposableCheckboxAppearance
import io.github.samuolis.domposable.foundation.DomposableChipVariant
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableColors
import io.github.samuolis.domposable.foundation.DomposableComponent
import io.github.samuolis.domposable.foundation.DomposableComponentStyles
import io.github.samuolis.domposable.foundation.DomposableControlPlacement
import io.github.samuolis.domposable.foundation.DomposableDividerAppearance
import io.github.samuolis.domposable.foundation.DomposableIconAppearance
import io.github.samuolis.domposable.foundation.DomposableIconButtonVariant
import io.github.samuolis.domposable.foundation.DomposableImageScale
import io.github.samuolis.domposable.foundation.DomposableImeAction
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableOffset
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableProgressAppearance
import io.github.samuolis.domposable.foundation.DomposableProgressVariant
import io.github.samuolis.domposable.foundation.DomposableRadioButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableScaffoldFabPosition
import io.github.samuolis.domposable.foundation.DomposableScreenState
import io.github.samuolis.domposable.foundation.DomposableShapes
import io.github.samuolis.domposable.foundation.DomposableSize
import io.github.samuolis.domposable.foundation.DomposableSliderColors
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableSwitchAppearance
import io.github.samuolis.domposable.foundation.DomposableTabsVariant
import io.github.samuolis.domposable.foundation.DomposableTextAlignment
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.DomposableTypography
import io.github.samuolis.domposable.foundation.dp

data class DomposableLazyItem(
    val key: String,
    val content: @Composable () -> Unit,
)

/** Opt-in rows. In measured mode [rowHeight] estimates rows until their actual height is known. */
data class DomposableListWindow(
    val rowHeight: DomposableLength,
    val viewportHeight: DomposableLength,
    val overscan: Int = 3,
    val measureRows: Boolean = false,
) {
    init {
        require(rowHeight.value.isFinite() && rowHeight.value > 0f)
        require(viewportHeight.value.isFinite() && viewportHeight.value > 0f)
        require(overscan > 0) { "At least one overscan row is required for keyboard traversal" }
    }
}

data class DomposableNavigationItem(
    val label: String,
    val symbol: String = "",
    val href: String? = null,
    val icon: (@Composable () -> Unit)? = null,
    val enabled: Boolean = true,
)

@DomposableComponent("DropdownMenuItem")
data class DomposableMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val checked: Boolean? = null,
    val selected: Boolean? = null,
    val leadingIcon: (@Composable () -> Unit)? = null,
    val selectedIcon: (@Composable () -> Unit)? = null,
    val trailingContent: (@Composable () -> Unit)? = null,
    val supportingText: (@Composable () -> Unit)? = null,
    val appearance: DomposableItemAppearance? = null,
    val textContent: (@Composable () -> Unit)? = null,
) {
    init {
        require(checked == null || selected == null) { "A menu item is checkable or selectable" }
    }
}

@DomposableComponent("Theme")
data class DomposableThemeProps(
    val tokens: DomposableTokens,
)

/** Main-axis placement for children in a row, column, or wrapping row. */
enum class DomposableArrangement { Start, Center, End, SpaceBetween, SpaceAround, SpaceEvenly }

@DomposableComponent("Column")
data class DomposableColumnProps(
    val gap: DomposableLength = 0.dp,
    val alignment: DomposableAlignment = DomposableAlignment.Start,
    val arrangement: DomposableArrangement = DomposableArrangement.Start,
)

@DomposableComponent("Row")
data class DomposableRowProps(
    val gap: DomposableLength = 0.dp,
    val alignment: DomposableAlignment = DomposableAlignment.Start,
    val stretch: Boolean = false,
    val arrangement: DomposableArrangement = DomposableArrangement.Start,
)

@DomposableComponent("FlowRow")
data class DomposableFlowRowProps(
    val gap: DomposableLength = 8.dp,
    val arrangement: DomposableArrangement = DomposableArrangement.Start,
    val alignment: DomposableAlignment = DomposableAlignment.Start,
    val rowGap: DomposableLength = gap,
)

/** A browser anchor on HTML; opens a URI with the platform handler on Compose UI. */
@DomposableComponent("Link")
data class DomposableLinkProps(
    val label: String,
    val href: String,
    val onClick: (() -> Unit)? = null,
    val textStyle: DomposableTextStyle = DomposableTextStyle.Body,
    val appearance: DomposableTextAppearance? = null,
    val buttonAppearance: DomposableButtonAppearance? = null,
    val buttonStyle: DomposableButtonStyle = DomposableButtonStyle.Filled,
    val content: (@Composable () -> Unit)? = null,
)

@DomposableComponent("Box")
data class DomposableBoxProps(
    /** Horizontal placement; also supplies the vertical default for existing callers. */
    val alignment: DomposableAlignment = DomposableAlignment.Start,
    val verticalAlignment: DomposableAlignment = alignment,
)

@DomposableComponent("LazyColumn")
data class DomposableLazyColumnProps(
    val items: List<DomposableLazyItem>,
    val gap: DomposableLength = 0.dp,
    val window: DomposableListWindow? = null,
)

@DomposableComponent("LazyRow")
data class DomposableLazyRowProps(
    val items: List<DomposableLazyItem>,
    val gap: DomposableLength = 0.dp,
    val window: DomposableRowWindow? = null,
)

@DomposableComponent("LazyGrid")
data class DomposableLazyGridProps(
    val items: List<DomposableLazyItem>,
    val columns: Int = 2,
    val gap: DomposableLength = 8.dp,
    val window: DomposableGridWindow? = null,
)

private val HTML_HEADING_LEVELS = 1..6

/**
 * Renderer-neutral text content and appearance. New primary-constructor parameters preserve
 * existing Kotlin source calls through defaults; binary consumers should use the next 0.x minor.
 */
@DomposableComponent("Text")
data class DomposableTextProps(
    val text: String,
    val style: DomposableTextStyle = DomposableTextStyle.Body,
    val color: DomposableColor? = null,
    val headingLevel: Int? = null,
    val appearance: DomposableTextAppearance? = null,
    val runs: List<DomposableTextRun> = emptyList(),
) {
    init {
        require(runs.isEmpty() || text == runs.joinToString("") { it.text })
        require(headingLevel == null || headingLevel in HTML_HEADING_LEVELS)
    }
}

@DomposableComponent("Button")
data class DomposableButtonProps(
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val style: DomposableButtonStyle = DomposableButtonStyle.Filled,
    val loading: Boolean = false,
    val loadingLabel: String? = null,
    val expanded: Boolean? = null,
    val appearance: DomposableButtonAppearance? = null,
)

@DomposableComponent("IconButton")
data class DomposableIconButtonProps(
    val onClick: () -> Unit,
    val label: String,
    val enabled: Boolean = true,
    val appearance: DomposableButtonAppearance? = null,
    val variant: DomposableIconButtonVariant = DomposableIconButtonVariant.Standard,
    val checked: Boolean? = null,
)

/**
 * Renderer-neutral text input behavior and content. New primary-constructor parameters preserve
 * existing Kotlin source calls through defaults; binary consumers should use the next 0.x minor.
 */
@Suppress("LongParameterList")
@DomposableComponent("TextField")
data class DomposableTextFieldProps(
    val value: String,
    val onValueChange: (String) -> Unit,
    val label: String,
    val purpose: DomposableInputPurpose = DomposableInputPurpose.Text,
    val enabled: Boolean = true,
    val multiline: Boolean = false,
    val error: String? = null,
    val readOnly: Boolean = false,
    val placeholder: String? = null,
    val id: String? = null,
    val passwordVisible: Boolean = false,
    val autocomplete: DomposableAutocomplete? = null,
    val showLabel: Boolean = true,
    val singleLine: Boolean = !multiline,
    val minLines: Int = 1,
    val maxLines: Int = Int.MAX_VALUE,
    val helperText: String? = null,
    val isError: Boolean = error != null,
    val autofocus: Boolean = false,
    val imeAction: DomposableImeAction = DomposableImeAction.Default,
    val onClick: (() -> Unit)? = null,
    val onImeAction: (() -> Unit)? = null,
    val leadingIcon: (@Composable () -> Unit)? = null,
    val prefix: (@Composable () -> Unit)? = null,
    val trailingIcon: (@Composable () -> Unit)? = null,
    val appearance: DomposableTextFieldAppearance? = null,
    val onFocusChange: ((Boolean) -> Unit)? = null,
) {
    init {
        require(minLines > 0)
        require(maxLines >= minLines)
    }
}

@DomposableComponent("Checkbox")
data class DomposableCheckboxProps(
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val label: String,
    val enabled: Boolean = true,
    val appearance: DomposableCheckboxAppearance? = null,
    val showLabel: Boolean = true,
    val description: String? = null,
    val controlPlacement: DomposableControlPlacement = DomposableControlPlacement.Leading,
    val indeterminate: Boolean = false,
)

@DomposableComponent("Switch")
data class DomposableSwitchProps(
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val label: String,
    val enabled: Boolean = true,
    val appearance: DomposableSwitchAppearance? = null,
    val showLabel: Boolean = true,
    val description: String? = null,
    val controlPlacement: DomposableControlPlacement = DomposableControlPlacement.Leading,
)

@DomposableComponent("RadioButton")
data class DomposableRadioButtonProps(
    val selected: Boolean,
    val onClick: () -> Unit,
    val label: String,
    val enabled: Boolean = true,
    val appearance: DomposableRadioButtonAppearance? = null,
    val showLabel: Boolean = true,
    val description: String? = null,
    val controlPlacement: DomposableControlPlacement = DomposableControlPlacement.Leading,
    val groupName: String? = null,
) {
    init {
        require(groupName == null || groupName.isNotBlank()) { "A radio group name must not be blank" }
    }
}

@DomposableComponent("Slider")
data class DomposableSliderProps(
    val value: Float,
    val onValueChange: (Float) -> Unit,
    val label: String,
    val range: ClosedFloatingPointRange<Float> =
        0f..1f,
    val steps: Int = 0,
    val enabled: Boolean = true,
    val colors: DomposableSliderColors? = null,
    val orientation: DomposableOrientation = DomposableOrientation.Horizontal,
    val topToBottom: Boolean = true,
)

@DomposableComponent("Card")
data class DomposableCardProps(
    val onClick: (() -> Unit)? = null,
    val appearance: DomposableSurfaceAppearance? = null,
    val variant: DomposableCardVariant = DomposableCardVariant.Filled,
    val enabled: Boolean = true,
    val href: String? = null,
    val actionLabel: String? = null,
    val contentPadding: DomposableInsets? = null,
)

@DomposableComponent("Surface")
data class DomposableSurfaceProps(
    val color: DomposableColor? = null,
    val appearance: DomposableSurfaceAppearance? = null,
)

@DomposableComponent("Image")
data class DomposableImageProps(
    val source: String,
    val description: String?,
    val onLoaded: (() -> Unit)? = null,
    val onError: (() -> Unit)? = null,
    val scale: DomposableImageScale = DomposableImageScale.Fit,
)

@DomposableComponent("Icon")
data class DomposableIconProps(
    val symbol: String,
    val description: String?,
    val appearance: DomposableIconAppearance? = null,
    /** Optional filled vector artwork; when present, the renderer uses it instead of [symbol]. */
    val vector: DomposableVectorIcon? = null,
)

@DomposableComponent("Divider")
data class DomposableDividerProps(
    val orientation: DomposableOrientation = DomposableOrientation.Horizontal,
    val appearance: DomposableDividerAppearance? = null,
)

@DomposableComponent("Spacer")
data class DomposableSpacerProps(
    val length: DomposableLength = 8.dp,
)

@DomposableComponent("Badge")
data class DomposableBadgeProps(
    val text: String? = null,
    val appearance: DomposableSurfaceAppearance? = null,
    val label: String? = null,
    val contentPadding: io.github.samuolis.domposable.foundation.DomposableInsets? = null,
    val minWidth: DomposableLength? = null,
    val minHeight: DomposableLength? = null,
    val content: (@Composable () -> Unit)? = null,
)

@DomposableComponent("Chip")
data class DomposableChipProps(
    val label: String,
    val onClick: () -> Unit,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val appearance: DomposableItemAppearance? = null,
    val variant: DomposableChipVariant = DomposableChipVariant.Filter,
    val leadingIcon: (@Composable () -> Unit)? = null,
    val trailingIcon: (@Composable () -> Unit)? = null,
    val elevated: Boolean = false,
)

@DomposableComponent("Avatar")
data class DomposableAvatarProps(
    val initials: String,
    val description: String,
    val appearance: DomposableSurfaceAppearance? = null,
    val content: (@Composable () -> Unit)? = null,
)

@DomposableComponent("Banner")
data class DomposableBannerProps(
    val message: String,
    val isError: Boolean = false,
    val appearance: DomposableSurfaceAppearance? = null,
)

@DomposableComponent("Progress")
data class DomposableProgressProps(
    val value: Float? = null,
    val label: String? = "Loading",
    val circular: Boolean = true,
    val appearance: DomposableProgressAppearance? = null,
    val variant: DomposableProgressVariant = DomposableProgressVariant.Standard,
    val liveRegion: io.github.samuolis.domposable.foundation.DomposableLiveRegion? = null,
    val semantics: io.github.samuolis.domposable.foundation.DomposableProgressSemantics =
        io.github.samuolis.domposable.foundation.DomposableProgressSemantics.ProgressBar,
)

@DomposableComponent("Tabs")
data class DomposableTabsProps(
    val labels: List<String>,
    val selectedIndex: Int,
    val onSelect: (Int) -> Unit,
    val appearance: DomposableItemAppearance? = null,
    val variant: DomposableTabsVariant = DomposableTabsVariant.Primary,
    val scrollable: Boolean = false,
    val icons: List<(@Composable () -> Unit)?> = labels.map { null },
    val leadingIcons: Boolean = false,
    /** Keyed items add per-tab semantics and content while keeping the labels API compatible. */
    val items: List<DomposableTabItem>? = null,
    /** Localized name for the tab list. */
    val label: String = "Sections",
    /** Optional shared layout and interaction tuning for tab rows. */
    val tabsAppearance: DomposableTabsAppearance? = null,
) {
    init {
        val itemCount = items?.size ?: labels.size
        require(itemCount > 0 && selectedIndex in 0 until itemCount)
        require(icons.size == labels.size) { "Supply one optional icon for each tab" }
        require(items == null || items.map { it.key }.distinct().size == items.size) {
            "Tab item keys must be unique"
        }
    }
}

/** Portable layout details that are specific to tab rows. Null fields keep Material defaults. */
data class DomposableTabsAppearance(
    val minHeight: DomposableLength? = null,
    val contentPadding: DomposableInsets? = null,
    val indicatorThickness: DomposableLength? = null,
    val indicatorMatchContent: Boolean? = null,
    val scrollableEdgePadding: DomposableLength? = null,
    val scrollableMinWidth: DomposableLength? = null,
    val disabledOpacity: Float? = null,
    val activation: DomposableTabsActivation = DomposableTabsActivation.Automatic,
    val badgeClearance: DomposableLength? = null,
    val badgeOffset: DomposableOffset = DomposableOffset(DEFAULT_TAB_BADGE_OFFSET_X, DEFAULT_TAB_BADGE_OFFSET_Y),
) {
    init {
        require(disabledOpacity == null || (disabledOpacity.isFinite() && disabledOpacity in 0f..1f))
    }
}

private const val DEFAULT_TAB_BADGE_OFFSET_X = 8f
private const val DEFAULT_TAB_BADGE_OFFSET_Y = -4f

/** Whether arrow-key navigation also changes the selected tab. */
enum class DomposableTabsActivation { Automatic, Manual }

/** Stable, accessible tab data with optional generic icon and badge/content slots. */
data class DomposableTabItem(
    val key: String,
    val label: String,
    val contentDescription: String = label,
    val enabled: Boolean = true,
    val testTag: String? = null,
    val icon: (@Composable () -> Unit)? = null,
    val badgeContent: (@Composable () -> Unit)? = null,
    val textAppearance: DomposableTextAppearance? = null,
    val selectedTextAppearance: DomposableTextAppearance? = null,
) {
    init {
        require(key.isNotBlank()) { "Tab item keys must not be blank" }
    }
}

/** HTML navigation presentation; native targets retain Material navigation. */
enum class DomposableWebNavigationLayout { Material, Links }

@DomposableComponent("NavigationBar")
data class DomposableNavigationBarProps(
    val items: List<DomposableNavigationItem>,
    val selectedIndex: Int,
    val onSelect: (Int) -> Unit,
    val orientation: DomposableOrientation = DomposableOrientation.Horizontal,
    val appearance: DomposableItemAppearance? = null,
    val short: Boolean = false,
    val label: String = "Application",
    val webLayout: DomposableWebNavigationLayout = DomposableWebNavigationLayout.Material,
)

@DomposableComponent("Menu")
data class DomposableMenuProps(
    val expanded: Boolean,
    val onDismiss: () -> Unit,
    val items: List<DomposableMenuItem>,
    val appearance: DomposableSurfaceAppearance? = null,
    val anchor: (@Composable () -> Unit)? = null,
    val contentSpacing: DomposableLength = 0.dp,
    val anchorOffset: DomposableOffset = DomposableOffset(),
)

/**
 * Bounds of the modal surface itself rather than its modifier wrapper. Native dialogs/sheets
 * retain platform placement; [alignment] adapts the HTML surface to the browser viewport.
 * [padding] controls Basic/FullScreen dialog and sheet content; Material picker/alert variants
 * retain their platform content spacing. [sectionSpacing] controls gaps between arranged sections
 * in HTML modals and native Basic/FullScreen dialogs and sheet content. Native Material
 * picker/alert variants retain their platform section spacing, as does the native sheet drag handle.
 */
data class DomposableModalLayout(
    val maxWidth: DomposableLength? = null,
    val fillViewportWidth: Boolean = false,
    val padding: DomposableInsets? = null,
    val heightFraction: Float? = null,
    val maxHeight: DomposableLength? = null,
    val maxViewportFraction: Float? = null,
    val viewportInset: DomposableLength = 16.dp,
    val alignment: DomposableModalAlignment = DomposableModalAlignment.Center,
    val swipeToDismiss: Boolean = true,
    val sectionSpacing: DomposableLength? = null,
) {
    init {
        require(heightFraction == null || heightFraction in 0f..1f)
        require(maxViewportFraction == null || maxViewportFraction in 0f..1f)
        require(sectionSpacing == null || sectionSpacing.value >= 0f)
    }
}

enum class DomposableModalAlignment { Center, Bottom, BottomOnCompact }

@DomposableComponent("Dialog")
data class DomposableDialogProps(
    val title: String,
    val onDismiss: () -> Unit,
    val actions: @Composable () -> Unit = {},
    val appearance: DomposableSurfaceAppearance? = null,
    val variant: DomposableDialogVariant = DomposableDialogVariant.Alert,
    val headerContent: (@Composable (onDismiss: () -> Unit) -> Unit)? = null,
    val layout: DomposableModalLayout = DomposableModalLayout(),
)

@DomposableComponent("BottomSheet")
data class DomposableBottomSheetProps(
    val title: String,
    val onDismiss: () -> Unit,
    val appearance: DomposableSurfaceAppearance? = null,
    val modal: Boolean = true,
    val headerContent: (@Composable (onDismiss: () -> Unit) -> Unit)? = null,
    val layout: DomposableModalLayout = DomposableModalLayout(),
)

@DomposableComponent("Scaffold")
data class DomposableScaffoldProps(
    val topBar: @Composable () -> Unit = {},
    val bottomBar: @Composable () -> Unit = {},
    val navigation: @Composable () -> Unit = {},
    val contextual: @Composable () -> Unit = {},
    val floatingAction: @Composable () -> Unit = {},
    val snackbar: @Composable () -> Unit = {},
    val overlays: @Composable () -> Unit = {},
    val appearance: DomposableSurfaceAppearance? = null,
    val floatingActionPosition: DomposableScaffoldFabPosition = DomposableScaffoldFabPosition.End,
)

val LocalDomposableTokens = staticCompositionLocalOf { DomposableTokens() }
val LocalDomposableComponents =
    staticCompositionLocalOf<DomposableComponents> {
        error("Install DomposableTheme with a component provider before rendering Domposable UI")
    }

/** Current theme values from the nearest enclosing theme, rather than mutable global branding. */
object DomposableTheme {
    val colors: DomposableColors
        @Composable @ReadOnlyComposable
        get() = LocalDomposableTokens.current.colors
    val typography: DomposableTypography
        @Composable @ReadOnlyComposable
        get() = LocalDomposableTokens.current.typography
    val shapes: DomposableShapes
        @Composable @ReadOnlyComposable
        get() = LocalDomposableTokens.current.shapes
    val styles: DomposableComponentStyles
        @Composable @ReadOnlyComposable
        get() = LocalDomposableTokens.current.components
}

@Composable
fun DomposableTheme(
    tokens: DomposableTokens = LocalDomposableTokens.current,
    components: DomposableComponents,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDomposableTokens provides tokens, LocalDomposableComponents provides components) {
        components.theme.Render(DomposableThemeProps(tokens), DomposableModifier, content)
    }
}

@Composable fun DomposableText(
    text: String,
    modifier: DomposableModifier = DomposableModifier,
    style: DomposableTextStyle = DomposableTextStyle.Body,
    color: DomposableColor? = null,
    headingLevel: Int? = null,
    appearance: DomposableTextAppearance? = null,
) = DomposableText(DomposableTextProps(text, style, color, headingLevel, appearance), modifier)

@Composable fun DomposableButton(
    onClick: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    style: DomposableButtonStyle = DomposableButtonStyle.Filled,
    content: @Composable () -> Unit,
) = DomposableButton(DomposableButtonProps(onClick, enabled, style), modifier, content)

@Composable fun DomposableButton(
    onClick: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    style: DomposableButtonStyle = DomposableButtonStyle.Filled,
    loading: Boolean = false,
    loadingLabel: String? = null,
    expanded: Boolean? = null,
    appearance: DomposableButtonAppearance? = null,
    content: @Composable () -> Unit,
) = DomposableButton(
    DomposableButtonProps(onClick, enabled, style, loading, loadingLabel, expanded, appearance),
    modifier,
    content,
)

@Composable fun DomposableButton(
    text: String,
    onClick: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
) {
    val textAppearance = buttonLabelTextAppearance()
    DomposableButton(onClick, modifier, enabled) {
        DomposableText(text, appearance = textAppearance)
    }
}

@Composable
internal fun buttonLabelTextAppearance(): DomposableTextAppearance {
    val tokens = LocalDomposableTokens.current
    val button = tokens.components.button
    val textAppearance = button.textAppearance ?: tokens.components.text
    if (textAppearance.alignment != null) return textAppearance
    val alignment =
        when (button.contentAlignment) {
            DomposableAlignment.Start -> DomposableTextAlignment.Start
            DomposableAlignment.Center -> DomposableTextAlignment.Center
            DomposableAlignment.End -> DomposableTextAlignment.End
        }
    return textAppearance.copy(alignment = alignment)
}

@Composable fun DomposableColumn(
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    alignment: DomposableAlignment = DomposableAlignment.Start,
    content: @Composable () -> Unit,
) = DomposableColumn(DomposableColumnProps(gap, alignment), modifier, content)

@Composable fun DomposableColumn(
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    alignment: DomposableAlignment = DomposableAlignment.Start,
    arrangement: DomposableArrangement,
    content: @Composable () -> Unit,
) = DomposableColumn(DomposableColumnProps(gap, alignment, arrangement), modifier, content)

@Composable fun DomposableRow(
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    alignment: DomposableAlignment = DomposableAlignment.Start,
    content: @Composable () -> Unit,
) = DomposableRow(DomposableRowProps(gap, alignment), modifier, content)

@Composable fun DomposableRow(
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    alignment: DomposableAlignment = DomposableAlignment.Start,
    arrangement: DomposableArrangement,
    content: @Composable () -> Unit,
) = DomposableRow(DomposableRowProps(gap, alignment, arrangement = arrangement), modifier, content)

@Composable fun DomposableRow(
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    alignment: DomposableAlignment = DomposableAlignment.Start,
    stretch: Boolean,
    content: @Composable () -> Unit,
) = DomposableRow(DomposableRowProps(gap, alignment, stretch), modifier, content)

@Composable fun DomposableRow(
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    alignment: DomposableAlignment = DomposableAlignment.Start,
    stretch: Boolean,
    arrangement: DomposableArrangement,
    content: @Composable () -> Unit,
) = DomposableRow(DomposableRowProps(gap, alignment, stretch, arrangement), modifier, content)

@Composable fun DomposableFlowRow(
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 8.dp,
    content: @Composable () -> Unit,
) = DomposableFlowRow(DomposableFlowRowProps(gap), modifier, content)

@Composable fun DomposableFlowRow(
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 8.dp,
    arrangement: DomposableArrangement,
    alignment: DomposableAlignment = DomposableAlignment.Start,
    rowGap: DomposableLength = gap,
    content: @Composable () -> Unit,
) = DomposableFlowRow(DomposableFlowRowProps(gap, arrangement, alignment, rowGap), modifier, content)

@Composable fun DomposableLink(
    label: String,
    href: String,
    modifier: DomposableModifier = DomposableModifier,
    onClick: (() -> Unit)? = null,
    textStyle: DomposableTextStyle = DomposableTextStyle.Body,
    appearance: DomposableTextAppearance? = null,
    buttonAppearance: DomposableButtonAppearance? = null,
    buttonStyle: DomposableButtonStyle = DomposableButtonStyle.Filled,
    content: (@Composable () -> Unit)? = null,
) = DomposableLink(
    DomposableLinkProps(label, href, onClick, textStyle, appearance, buttonAppearance, buttonStyle, content),
    modifier,
)

@Composable fun DomposableBox(
    modifier: DomposableModifier = DomposableModifier,
    alignment: DomposableAlignment = DomposableAlignment.Start,
    content: @Composable () -> Unit,
) = DomposableBox(DomposableBoxProps(alignment), modifier, content)

@Composable fun DomposableBox(
    modifier: DomposableModifier = DomposableModifier,
    alignment: DomposableAlignment = DomposableAlignment.Start,
    verticalAlignment: DomposableAlignment,
    content: @Composable () -> Unit,
) = DomposableBox(DomposableBoxProps(alignment, verticalAlignment), modifier, content)

@Composable fun DomposableCard(
    modifier: DomposableModifier = DomposableModifier,
    onClick: (() -> Unit)? = null,
    appearance: DomposableSurfaceAppearance? = null,
    variant: DomposableCardVariant = DomposableCardVariant.Filled,
    enabled: Boolean = true,
    href: String? = null,
    actionLabel: String? = null,
    contentPadding: DomposableInsets? = null,
    content: @Composable () -> Unit,
) = DomposableCard(
    DomposableCardProps(
        onClick,
        appearance = appearance,
        variant = variant,
        enabled = enabled,
        href = href,
        actionLabel = actionLabel,
        contentPadding = contentPadding,
    ),
    modifier,
    content,
)

@Composable fun DomposableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    purpose: DomposableInputPurpose = DomposableInputPurpose.Text,
    enabled: Boolean = true,
    multiline: Boolean = false,
    error: String? = null,
    readOnly: Boolean = false,
    placeholder: String? = null,
    id: String? = null,
    passwordVisible: Boolean = false,
    autocomplete: DomposableAutocomplete? = null,
    showLabel: Boolean = true,
    singleLine: Boolean = !multiline,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    helperText: String? = null,
    isError: Boolean = error != null,
    autofocus: Boolean = false,
    imeAction: DomposableImeAction = DomposableImeAction.Default,
    onClick: (() -> Unit)? = null,
    onImeAction: (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    prefix: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    appearance: DomposableTextFieldAppearance? = null,
    onFocusChange: ((Boolean) -> Unit)? = null,
) = DomposableTextField(
    DomposableTextFieldProps(
        value = value,
        onValueChange = onValueChange,
        label = label,
        purpose = purpose,
        enabled = enabled,
        multiline = multiline,
        error = error,
        readOnly = readOnly,
        placeholder = placeholder,
        id = id,
        passwordVisible = passwordVisible,
        autocomplete = autocomplete,
        showLabel = showLabel,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        helperText = helperText,
        isError = isError,
        autofocus = autofocus,
        imeAction = imeAction,
        onClick = onClick,
        onImeAction = onImeAction,
        leadingIcon = leadingIcon,
        prefix = prefix,
        trailingIcon = trailingIcon,
        appearance = appearance,
        onFocusChange = onFocusChange,
    ),
    modifier,
)

@Composable fun DomposableCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    appearance: DomposableCheckboxAppearance? = null,
    showLabel: Boolean = true,
    description: String? = null,
    controlPlacement: DomposableControlPlacement = DomposableControlPlacement.Leading,
    indeterminate: Boolean = false,
) = DomposableCheckbox(
    DomposableCheckboxProps(
        checked,
        onCheckedChange,
        label,
        enabled,
        appearance = appearance,
        showLabel = showLabel,
        description = description,
        controlPlacement = controlPlacement,
        indeterminate = indeterminate,
    ),
    modifier,
)

@Composable fun DomposableSpacer(
    length: DomposableLength = 8.dp,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableSpacer(DomposableSpacerProps(length), modifier)

@Composable fun DomposableDivider(
    modifier: DomposableModifier = DomposableModifier,
    appearance: DomposableDividerAppearance? = null,
) = DomposableDivider(DomposableDividerProps(appearance = appearance), modifier)

class DomposableScaffoldScope internal constructor(
    val contentModifier: DomposableModifier,
    val insets: DomposableInsets,
    val availableContentSize: DomposableSize,
)

@Composable
fun DomposableScaffold(
    modifier: DomposableModifier = DomposableModifier,
    insets: DomposableInsets = DomposableInsets(),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    navigation: @Composable () -> Unit = {},
    contextual: @Composable () -> Unit = {},
    floatingAction: @Composable () -> Unit = {},
    floatingActionPosition: DomposableScaffoldFabPosition = DomposableScaffoldFabPosition.End,
    snackbar: @Composable () -> Unit = {},
    overlays: @Composable () -> Unit = {},
    appearance: DomposableSurfaceAppearance? = null,
    content: @Composable DomposableScaffoldScope.() -> Unit,
) {
    DomposableScaffold(
        DomposableScaffoldProps(
            topBar,
            bottomBar,
            navigation,
            contextual,
            floatingAction,
            snackbar,
            overlays,
            appearance = appearance,
            floatingActionPosition = floatingActionPosition,
        ),
        modifier,
    ) {
        DomposableMeasure { size ->
            val available =
                DomposableSize(
                    (size.width - insets.start.value - insets.end.value).coerceAtLeast(0f),
                    (size.height - insets.top.value - insets.bottom.value).coerceAtLeast(0f),
                )
            DomposableScaffoldScope(DomposableModifier.padding(insets), insets, available).content()
        }
    }
}

/** Screen chrome without imposing a state model, ViewModel, DI container, or navigator. */
@Composable
@Suppress("LongParameterList")
fun DomposableScreenScaffold(
    modifier: DomposableModifier = DomposableModifier,
    insets: DomposableInsets = DomposableInsets(),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    navigation: @Composable () -> Unit = {},
    contextual: @Composable () -> Unit = {},
    floatingAction: @Composable () -> Unit = {},
    floatingActionPosition: DomposableScaffoldFabPosition = DomposableScaffoldFabPosition.End,
    snackbar: @Composable () -> Unit = {},
    overlays: @Composable () -> Unit = {},
    appearance: DomposableSurfaceAppearance? = null,
    content: @Composable DomposableScaffoldScope.() -> Unit,
) = DomposableScaffold(
    modifier = modifier,
    insets = insets,
    topBar = topBar,
    bottomBar = bottomBar,
    navigation = navigation,
    contextual = contextual,
    floatingAction = floatingAction,
    floatingActionPosition = floatingActionPosition,
    snackbar = snackbar,
    overlays = overlays,
    appearance = appearance,
    content = content,
)

/** Optional loading/error convenience; applications can supply their own state and chrome. */
@Composable
@Suppress("LongParameterList")
fun <T> DomposableScreenScaffold(
    state: DomposableScreenState<T>,
    modifier: DomposableModifier = DomposableModifier,
    insets: DomposableInsets = DomposableInsets(),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    navigation: @Composable () -> Unit = {},
    contextual: @Composable () -> Unit = {},
    floatingAction: @Composable () -> Unit = {},
    floatingActionPosition: DomposableScaffoldFabPosition = DomposableScaffoldFabPosition.End,
    snackbar: @Composable () -> Unit = {},
    overlays: @Composable () -> Unit = {},
    appearance: DomposableSurfaceAppearance? = null,
    loading: @Composable DomposableScaffoldScope.() -> Unit = {
        DomposableProgress(DomposableProgressProps())
    },
    error: @Composable DomposableScaffoldScope.(DomposableScreenState.Error) -> Unit = { failure ->
        DomposableBanner(DomposableBannerProps(failure.message, isError = true))
        failure.retry?.let { DomposableButton("Retry", it) }
    },
    content: @Composable DomposableScaffoldScope.(T) -> Unit,
) {
    DomposableScreenScaffold(
        modifier,
        insets,
        topBar,
        bottomBar,
        navigation,
        contextual,
        floatingAction,
        floatingActionPosition,
        snackbar,
        overlays,
        appearance,
    ) {
        when (state) {
            DomposableScreenState.Loading -> loading()
            is DomposableScreenState.Error -> error(state)
            is DomposableScreenState.Content -> content(state.value)
        }
    }
}

@Composable
expect fun DomposableMeasure(
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable (DomposableSize) -> Unit,
)
