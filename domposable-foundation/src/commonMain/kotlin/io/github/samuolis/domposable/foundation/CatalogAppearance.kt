package io.github.samuolis.domposable.foundation

import androidx.compose.runtime.Immutable

@Immutable
@Suppress("LongParameterList")
data class DomposableSurfaceAppearance(
    val container: DomposableColor? = null,
    val content: DomposableColor? = null,
    val border: DomposableColor? = null,
    val borderWidth: DomposableLength = 1.dp,
    val shape: DomposableShape? = null,
    val tonalElevation: DomposableLength? = null,
    val shadowElevation: DomposableLength? = null,
    val textAppearance: DomposableTextAppearance? = null,
)

@Immutable
data class DomposableSelectionColors(
    val selected: DomposableColor? = null,
    val unselected: DomposableColor? = null,
    val disabledSelected: DomposableColor? = null,
    val disabledUnselected: DomposableColor? = null,
)

@Immutable
data class DomposableCheckboxAppearance(
    val colors: DomposableSelectionColors = DomposableSelectionColors(),
    val checkmark: DomposableColor? = null,
    val disabledCheckmark: DomposableColor? = null,
    val labelAppearance: DomposableTextAppearance? = null,
    val descriptionAppearance: DomposableTextAppearance? = null,
    val labelSpacing: DomposableLength = 8.dp,
    val descriptionSpacing: DomposableLength = 0.dp,
)

@Immutable
@Suppress("LongParameterList")
data class DomposableSwitchColors(
    val checkedThumb: DomposableColor? = null,
    val checkedTrack: DomposableColor? = null,
    val checkedBorder: DomposableColor? = null,
    val checkedIcon: DomposableColor? = null,
    val uncheckedThumb: DomposableColor? = null,
    val uncheckedTrack: DomposableColor? = null,
    val uncheckedBorder: DomposableColor? = null,
    val uncheckedIcon: DomposableColor? = null,
    val disabledCheckedThumb: DomposableColor? = null,
    val disabledCheckedTrack: DomposableColor? = null,
    val disabledCheckedBorder: DomposableColor? = null,
    val disabledCheckedIcon: DomposableColor? = null,
    val disabledUncheckedThumb: DomposableColor? = null,
    val disabledUncheckedTrack: DomposableColor? = null,
    val disabledUncheckedBorder: DomposableColor? = null,
    val disabledUncheckedIcon: DomposableColor? = null,
)

@Immutable
data class DomposableSwitchAppearance(
    val colors: DomposableSwitchColors = DomposableSwitchColors(),
    val labelAppearance: DomposableTextAppearance? = null,
    val descriptionAppearance: DomposableTextAppearance? = null,
    val labelSpacing: DomposableLength = 8.dp,
    val descriptionSpacing: DomposableLength = 0.dp,
)

@Immutable
data class DomposableRadioButtonAppearance(
    val colors: DomposableSelectionColors = DomposableSelectionColors(),
    val labelAppearance: DomposableTextAppearance? = null,
    val descriptionAppearance: DomposableTextAppearance? = null,
    val labelSpacing: DomposableLength = 8.dp,
    val descriptionSpacing: DomposableLength = 0.dp,
)

@Immutable
@Suppress("LongParameterList")
data class DomposableSliderColors(
    val thumb: DomposableColor? = null,
    val activeTrack: DomposableColor? = null,
    val inactiveTrack: DomposableColor? = null,
    val activeTick: DomposableColor? = null,
    val inactiveTick: DomposableColor? = null,
    val disabledThumb: DomposableColor? = null,
    val disabledActiveTrack: DomposableColor? = null,
    val disabledInactiveTrack: DomposableColor? = null,
    val disabledActiveTick: DomposableColor? = null,
    val disabledInactiveTick: DomposableColor? = null,
)

@Immutable
@Suppress("LongParameterList")
data class DomposableItemColors(
    val container: DomposableColor? = null,
    val content: DomposableColor? = null,
    val selectedContainer: DomposableColor? = null,
    val selectedContent: DomposableColor? = null,
    val disabledContainer: DomposableColor? = null,
    val disabledContent: DomposableColor? = null,
    val border: DomposableColor? = null,
    val selectedBorder: DomposableColor? = null,
    val disabledBorder: DomposableColor? = null,
    val focusedContainer: DomposableColor? = null,
    val focusedContent: DomposableColor? = null,
    val focusedBorder: DomposableColor? = null,
    val pressedContainer: DomposableColor? = null,
    val pressedContent: DomposableColor? = null,
    val pressedBorder: DomposableColor? = null,
    val hoveredContainer: DomposableColor? = null,
    val hoveredContent: DomposableColor? = null,
    val hoveredBorder: DomposableColor? = null,
)

/** Opacities for the control's content-color interaction layer. */
@Immutable
data class DomposableStateLayerOpacity(
    val hovered: Float = 0.08f,
    val focused: Float = 0.12f,
    val pressed: Float = 0.12f,
    val dragged: Float = 0.16f,
) {
    init {
        require(listOf(hovered, focused, pressed, dragged).all { it.isFinite() && it in 0f..1f })
    }
}

@Immutable
data class DomposableItemAppearance(
    val colors: DomposableItemColors = DomposableItemColors(),
    val shape: DomposableShape? = null,
    val textAppearance: DomposableTextAppearance? = null,
    val borderWidth: DomposableLength = 1.dp,
    val contentPadding: DomposableInsets? = null,
    val minHeight: DomposableLength? = null,
    val contentSpacing: DomposableLength? = null,
    val minimumInteractiveSize: DomposableLength? = null,
    val selectedBorderWidth: DomposableLength? = null,
    val selectedTextAppearance: DomposableTextAppearance? = null,
    val disabledOpacity: Float? = null,
    val stateLayerOpacity: DomposableStateLayerOpacity? = null,
) {
    init {
        require(disabledOpacity == null || (disabledOpacity.isFinite() && disabledOpacity in 0f..1f))
    }
}

@Immutable
data class DomposableProgressAppearance(
    val color: DomposableColor? = null,
    val track: DomposableColor? = null,
    val strokeWidth: DomposableLength? = null,
    val indicatorSize: DomposableLength? = null,
    val waveSpeed: DomposableLength? = null,
)

@Immutable
data class DomposableDividerAppearance(
    val color: DomposableColor? = null,
    val thickness: DomposableLength? = null,
)

@Immutable
data class DomposableIconAppearance(
    val color: DomposableColor? = null,
    val textAppearance: DomposableTextAppearance? = null,
)

@Immutable
@Suppress("LongParameterList")
data class DomposableTableAppearance(
    val headerContainer: DomposableColor? = null,
    val headerContent: DomposableColor? = null,
    val rowContainer: DomposableColor? = null,
    val rowContent: DomposableColor? = null,
    val selectedContainer: DomposableColor? = null,
    val selectedContent: DomposableColor? = null,
    val border: DomposableColor? = null,
    val cellPadding: DomposableInsets? = null,
    val textAppearance: DomposableTextAppearance? = null,
    val headerTextAppearance: DomposableTextAppearance? = null,
)

@Immutable
data class DomposableChartAppearance(
    val background: DomposableColor? = null,
    val axis: DomposableColor? = null,
    val grid: DomposableColor? = null,
    val selection: DomposableColor? = null,
    val titleAppearance: DomposableTextAppearance? = null,
    val labelAppearance: DomposableTextAppearance? = null,
)

/** Defaults for built-in renderers. Per-call appearances replace these values; use copy to inherit. */
@Immutable
@Suppress("LongParameterList")
data class DomposableComponentStyles(
    val button: DomposableButtonAppearance = DomposableButtonAppearance(),
    val textField: DomposableTextFieldAppearance = DomposableTextFieldAppearance(),
    val text: DomposableTextAppearance = DomposableTextAppearance(),
    val link: DomposableTextAppearance = DomposableTextAppearance(),
    val icon: DomposableIconAppearance = DomposableIconAppearance(),
    val iconButton: DomposableButtonAppearance = DomposableButtonAppearance(),
    val checkbox: DomposableCheckboxAppearance = DomposableCheckboxAppearance(),
    val switch: DomposableSwitchAppearance = DomposableSwitchAppearance(),
    val radioButton: DomposableRadioButtonAppearance = DomposableRadioButtonAppearance(),
    val slider: DomposableSliderColors = DomposableSliderColors(),
    val rangeSlider: DomposableSliderColors = DomposableSliderColors(),
    val select: DomposableButtonAppearance = DomposableButtonAppearance(),
    val progress: DomposableProgressAppearance = DomposableProgressAppearance(),
    val divider: DomposableDividerAppearance = DomposableDividerAppearance(),
    val table: DomposableTableAppearance = DomposableTableAppearance(),
    val dataTable: DomposableTableAppearance = DomposableTableAppearance(),
    val lineChart: DomposableChartAppearance = DomposableChartAppearance(),
    val valueChart: DomposableChartAppearance = DomposableChartAppearance(),
    val card: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val surface: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val badge: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val avatar: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val banner: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val menu: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val dialog: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val bottomSheet: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val topAppBar: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val floatingActionButton: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val snackbar: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val tooltip: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val scaffold: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val disclosure: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val chip: DomposableItemAppearance = DomposableItemAppearance(),
    val tabs: DomposableItemAppearance = DomposableItemAppearance(),
    val navigationBar: DomposableItemAppearance = DomposableItemAppearance(),
    val segmentedButtons: DomposableItemAppearance = DomposableItemAppearance(),
    val toggleButton: DomposableButtonAppearance = DomposableButtonAppearance(),
    val buttonGroup: DomposableButtonAppearance = DomposableButtonAppearance(),
    val splitButton: DomposableButtonAppearance = DomposableButtonAppearance(),
    val loadingIndicator: DomposableProgressAppearance = DomposableProgressAppearance(),
    val floatingToolbar: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val floatingActionButtonMenu: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val listItem: DomposableItemAppearance = DomposableItemAppearance(),
    val bottomAppBar: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val navigationDrawer: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val navigationDrawerItem: DomposableItemAppearance = DomposableItemAppearance(),
    val wideNavigationRail: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val bottomSheetScaffold: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val searchBar: DomposableTextFieldAppearance = DomposableTextFieldAppearance(),
    val autocomplete: DomposableTextFieldAppearance = DomposableTextFieldAppearance(),
    val datePicker: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val dateRangePicker: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val timePicker: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val pullToRefresh: DomposableProgressAppearance = DomposableProgressAppearance(),
    val dragHandle: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val scrollField: DomposableItemAppearance = DomposableItemAppearance(),
    val label: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val dropdownMenuItem: DomposableItemAppearance = DomposableItemAppearance(),
    val menuGroup: DomposableSurfaceAppearance = DomposableSurfaceAppearance(),
    val multiChoiceSegmentedButtons: DomposableItemAppearance = DomposableItemAppearance(),
    val appBar: DomposableButtonAppearance = DomposableButtonAppearance(),
    val wideNavigationRailItem: DomposableItemAppearance = DomposableItemAppearance(),
)

/** Position of the selection control relative to its label. */
enum class DomposableControlPlacement {
    Leading,
    Trailing,
}

/** Selects Material theme defaults; expressive components and variants remain independently selectable. */
enum class DomposableDesignLanguage { Standard, Expressive }

enum class DomposableCardVariant { Elevated, Filled, Outlined }

enum class DomposableChipVariant { Assist, Suggestion, Input, Filter }

enum class DomposableIconButtonVariant { Standard, Filled, FilledTonal, Outlined }

enum class DomposableTopAppBarVariant { Small, CenterAligned, Medium, Large, MediumFlexible, LargeFlexible, TwoRows }

enum class DomposableFabSize { Small, Standard, Medium, Large }

enum class DomposableTabsVariant { Primary, Secondary }

enum class DomposableProgressVariant { Standard, Wavy }

enum class DomposableScaffoldFabPosition { Start, Center, End }

/** Shape scale shared by standard and expressive Material defaults. */
@Immutable
@Suppress("LongParameterList")
data class DomposableShapes(
    val extraSmall: DomposableShape? = null,
    val small: DomposableShape? = null,
    val medium: DomposableShape? = null,
    val large: DomposableShape? = null,
    val extraLarge: DomposableShape? = null,
    val largeIncreased: DomposableShape? = null,
    val extraLargeIncreased: DomposableShape? = null,
    val extraExtraLarge: DomposableShape? = null,
)

enum class DomposableTypographyRole {
    DisplayLarge,
    DisplayMedium,
    DisplaySmall,
    HeadlineLarge,
    HeadlineMedium,
    HeadlineSmall,
    TitleLarge,
    TitleMedium,
    TitleSmall,
    BodyLarge,
    BodyMedium,
    BodySmall,
    LabelLarge,
    LabelMedium,
    LabelSmall,
}

enum class DomposableProgressSemantics { ProgressBar, Status }
