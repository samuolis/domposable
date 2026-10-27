package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.ui.DomposableModifier

@Composable
internal expect fun RenderDomposableAppBar(
    props: io.github.samuolis.domposable.ui.DomposableAppBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableAutocomplete(
    props: io.github.samuolis.domposable.ui.DomposableAutocompleteProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableAvatar(
    props: io.github.samuolis.domposable.ui.DomposableAvatarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableBadge(
    props: io.github.samuolis.domposable.ui.DomposableBadgeProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableBadgedBox(
    props: io.github.samuolis.domposable.ui.DomposableBadgedBoxProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableBanner(
    props: io.github.samuolis.domposable.ui.DomposableBannerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableBottomAppBar(
    props: io.github.samuolis.domposable.ui.DomposableBottomAppBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableBottomSheet(
    props: io.github.samuolis.domposable.ui.DomposableBottomSheetProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableBottomSheetScaffold(
    props: io.github.samuolis.domposable.ui.DomposableBottomSheetScaffoldProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableBox(
    props: io.github.samuolis.domposable.ui.DomposableBoxProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableButton(
    props: io.github.samuolis.domposable.ui.DomposableButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableButtonGroup(
    props: io.github.samuolis.domposable.ui.DomposableButtonGroupProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableCard(
    props: io.github.samuolis.domposable.ui.DomposableCardProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableCarousel(
    props: io.github.samuolis.domposable.ui.DomposableCarouselProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableCheckbox(
    props: io.github.samuolis.domposable.ui.DomposableCheckboxProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableChip(
    props: io.github.samuolis.domposable.ui.DomposableChipProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableColumn(
    props: io.github.samuolis.domposable.ui.DomposableColumnProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableDataTable(
    props: io.github.samuolis.domposable.ui.DomposableDataTableProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableDatePicker(
    props: io.github.samuolis.domposable.ui.DomposableDatePickerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableDateRangePicker(
    props: io.github.samuolis.domposable.ui.DomposableDateRangePickerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableDialog(
    props: io.github.samuolis.domposable.ui.DomposableDialogProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableDisclosure(
    props: io.github.samuolis.domposable.ui.DomposableDisclosureProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableDivider(
    props: io.github.samuolis.domposable.ui.DomposableDividerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableDragHandle(
    props: io.github.samuolis.domposable.ui.DomposableDragHandleProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableDropdownMenuItem(
    props: io.github.samuolis.domposable.ui.DomposableMenuItem,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableFloatingActionButton(
    props: io.github.samuolis.domposable.ui.DomposableFloatingActionButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableFloatingActionButtonMenu(
    props: io.github.samuolis.domposable.ui.DomposableFloatingActionButtonMenuProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableFloatingToolbar(
    props: io.github.samuolis.domposable.ui.DomposableFloatingToolbarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableFlowRow(
    props: io.github.samuolis.domposable.ui.DomposableFlowRowProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableForm(
    props: io.github.samuolis.domposable.ui.DomposableFormProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableIcon(
    props: io.github.samuolis.domposable.ui.DomposableIconProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableIconButton(
    props: io.github.samuolis.domposable.ui.DomposableIconButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableImage(
    props: io.github.samuolis.domposable.ui.DomposableImageProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableLabel(
    props: io.github.samuolis.domposable.ui.DomposableLabelProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableLazyColumn(
    props: io.github.samuolis.domposable.ui.DomposableLazyColumnProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableLazyGrid(
    props: io.github.samuolis.domposable.ui.DomposableLazyGridProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableLazyRow(
    props: io.github.samuolis.domposable.ui.DomposableLazyRowProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableLineChart(
    props: io.github.samuolis.domposable.ui.DomposableLineChartProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableLink(
    props: io.github.samuolis.domposable.ui.DomposableLinkProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableListItem(
    props: io.github.samuolis.domposable.ui.DomposableListItemProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableLoadingIndicator(
    props: io.github.samuolis.domposable.ui.DomposableLoadingIndicatorProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableMenu(
    props: io.github.samuolis.domposable.ui.DomposableMenuProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableMenuGroup(
    props: io.github.samuolis.domposable.ui.DomposableMenuGroupProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableMultiChoiceSegmentedButtons(
    props: io.github.samuolis.domposable.ui.DomposableMultiChoiceSegmentedButtonsProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableNavigationBar(
    props: io.github.samuolis.domposable.ui.DomposableNavigationBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableNavigationDrawer(
    props: io.github.samuolis.domposable.ui.DomposableNavigationDrawerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableNavigationDrawerItem(
    props: io.github.samuolis.domposable.ui.DomposableNavigationDrawerItemProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableProgress(
    props: io.github.samuolis.domposable.ui.DomposableProgressProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposablePullToRefresh(
    props: io.github.samuolis.domposable.ui.DomposablePullToRefreshProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableRadioButton(
    props: io.github.samuolis.domposable.ui.DomposableRadioButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableRangeSlider(
    props: io.github.samuolis.domposable.ui.DomposableRangeSliderProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableRichHtml(
    props: io.github.samuolis.domposable.ui.DomposableRichHtmlProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableRow(
    props: io.github.samuolis.domposable.ui.DomposableRowProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableScaffold(
    props: io.github.samuolis.domposable.ui.DomposableScaffoldProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableScrim(
    props: io.github.samuolis.domposable.ui.DomposableScrimProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableScrollField(
    props: io.github.samuolis.domposable.ui.DomposableScrollFieldProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableSearchBar(
    props: io.github.samuolis.domposable.ui.DomposableSearchBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableSegmentedButtons(
    props: io.github.samuolis.domposable.ui.DomposableSegmentedButtonsProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableSelect(
    props: io.github.samuolis.domposable.ui.DomposableSelectProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableSlider(
    props: io.github.samuolis.domposable.ui.DomposableSliderProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableSnackbar(
    props: io.github.samuolis.domposable.ui.DomposableSnackbarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableSnackbarHost(
    props: io.github.samuolis.domposable.ui.DomposableSnackbarHostProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableSpacer(
    props: io.github.samuolis.domposable.ui.DomposableSpacerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableSplitButton(
    props: io.github.samuolis.domposable.ui.DomposableSplitButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableSurface(
    props: io.github.samuolis.domposable.ui.DomposableSurfaceProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableSwipeToDismiss(
    props: io.github.samuolis.domposable.ui.DomposableSwipeToDismissProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableSwitch(
    props: io.github.samuolis.domposable.ui.DomposableSwitchProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableTable(
    props: io.github.samuolis.domposable.ui.DomposableTableProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableTabs(
    props: io.github.samuolis.domposable.ui.DomposableTabsProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableText(
    props: io.github.samuolis.domposable.ui.DomposableTextProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableTextField(
    props: io.github.samuolis.domposable.ui.DomposableTextFieldProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableTheme(
    props: io.github.samuolis.domposable.ui.DomposableThemeProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableTimePicker(
    props: io.github.samuolis.domposable.ui.DomposableTimePickerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableToggleButton(
    props: io.github.samuolis.domposable.ui.DomposableToggleButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableTooltip(
    props: io.github.samuolis.domposable.ui.DomposableTooltipProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableTopAppBar(
    props: io.github.samuolis.domposable.ui.DomposableTopAppBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableValueChart(
    props: io.github.samuolis.domposable.ui.DomposableValueChartProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableWideNavigationRail(
    props: io.github.samuolis.domposable.ui.DomposableWideNavigationRailProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun RenderDomposableWideNavigationRailItem(
    props: io.github.samuolis.domposable.ui.DomposableWideNavigationRailItemProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)
