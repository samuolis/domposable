package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableComponent
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableProgressAppearance
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.foundation.dp

@DomposableComponent("SearchBar")
@Suppress("LongParameterList")
data class DomposableSearchBarProps(
    val query: String,
    val onQueryChange: (String) -> Unit,
    val expanded: Boolean,
    val onExpandedChange: (Boolean) -> Unit,
    val onSearch: (String) -> Unit,
    val label: String,
    val placeholder: String? = null,
    val enabled: Boolean = true,
    val leadingIcon: @Composable () -> Unit = {},
    val trailingIcon: @Composable () -> Unit = {},
    val appearance: DomposableTextFieldAppearance? = null,
    val variant: DomposableSearchBarVariant = DomposableSearchBarVariant.FullScreen,
    val navigationIcon: (@Composable () -> Unit)? = null,
    val actions: (@Composable () -> Unit)? = null,
)

@Composable
@Suppress("LongParameterList")
fun DomposableSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSearch: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    enabled: Boolean = true,
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
    appearance: DomposableTextFieldAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
    variant: DomposableSearchBarVariant = DomposableSearchBarVariant.FullScreen,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) = DomposableSearchBar(
    DomposableSearchBarProps(
        query,
        onQueryChange,
        expanded,
        onExpandedChange,
        onSearch,
        label,
        placeholder,
        enabled,
        leadingIcon,
        trailingIcon,
        appearance,
        variant = variant,
        navigationIcon = navigationIcon,
        actions = actions,
    ),
    modifier,
    content,
)

@DomposableComponent("Autocomplete")
@Suppress("LongParameterList")
data class DomposableAutocompleteProps(
    val value: String,
    val onValueChange: (String) -> Unit,
    val onOptionSelected: (DomposableSelectOption) -> Unit,
    val options: List<DomposableSelectOption>,
    val expanded: Boolean,
    val onExpandedChange: (Boolean) -> Unit,
    val label: String,
    val enabled: Boolean = true,
    val readOnly: Boolean = false,
    val appearance: DomposableTextFieldAppearance? = null,
) {
    init {
        require(options.map { it.value }.distinct().size == options.size)
    }
}

@Composable
@Suppress("LongParameterList")
fun DomposableAutocomplete(
    value: String,
    onValueChange: (String) -> Unit,
    onOptionSelected: (DomposableSelectOption) -> Unit,
    options: List<DomposableSelectOption>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    label: String,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    appearance: DomposableTextFieldAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableAutocomplete(
    DomposableAutocompleteProps(
        value,
        onValueChange,
        onOptionSelected,
        options,
        expanded,
        onExpandedChange,
        label,
        enabled,
        readOnly,
        appearance,
    ),
    modifier,
)

private const val DEFAULT_FIRST_PICKER_YEAR = 1900
private const val DEFAULT_LAST_PICKER_YEAR = 2100
private const val LAST_SUPPORTED_PICKER_YEAR = 9999
private const val HOURS_PER_DAY = 24
private const val MINUTES_PER_HOUR = 60

@DomposableComponent("DatePicker")
@Suppress("LongParameterList")
data class DomposableDatePickerProps(
    val selectedDateMillis: Long?,
    val onDateChange: (Long?) -> Unit,
    val yearRange: IntRange = DEFAULT_FIRST_PICKER_YEAR..DEFAULT_LAST_PICKER_YEAR,
    val mode: DomposableDatePickerMode = DomposableDatePickerMode.Calendar,
    val onModeChange: ((DomposableDatePickerMode) -> Unit)? = null,
    val isDateSelectable: (Long) -> Boolean = { true },
    val showModeToggle: Boolean = true,
    val appearance: DomposableSurfaceAppearance? = null,
) {
    init {
        require(!yearRange.isEmpty() && yearRange.first >= 1 && yearRange.last <= LAST_SUPPORTED_PICKER_YEAR)
    }
}

@Composable
@Suppress("LongParameterList")
fun DomposableDatePicker(
    selectedDateMillis: Long?,
    onDateChange: (Long?) -> Unit,
    yearRange: IntRange = DEFAULT_FIRST_PICKER_YEAR..DEFAULT_LAST_PICKER_YEAR,
    mode: DomposableDatePickerMode = DomposableDatePickerMode.Calendar,
    onModeChange: ((DomposableDatePickerMode) -> Unit)? = null,
    isDateSelectable: (Long) -> Boolean = { true },
    showModeToggle: Boolean = true,
    appearance: DomposableSurfaceAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableDatePicker(
    DomposableDatePickerProps(
        selectedDateMillis,
        onDateChange,
        yearRange,
        mode,
        onModeChange,
        isDateSelectable,
        showModeToggle,
        appearance,
    ),
    modifier,
)

@DomposableComponent("DateRangePicker")
@Suppress("LongParameterList")
data class DomposableDateRangePickerProps(
    val startDateMillis: Long?,
    val endDateMillis: Long?,
    val onDateRangeChange: (Long?, Long?) -> Unit,
    val yearRange: IntRange = DEFAULT_FIRST_PICKER_YEAR..DEFAULT_LAST_PICKER_YEAR,
    val mode: DomposableDatePickerMode = DomposableDatePickerMode.Calendar,
    val onModeChange: ((DomposableDatePickerMode) -> Unit)? = null,
    val isDateSelectable: (Long) -> Boolean = { true },
    val showModeToggle: Boolean = true,
    val appearance: DomposableSurfaceAppearance? = null,
) {
    init {
        require(!yearRange.isEmpty() && yearRange.first >= 1 && yearRange.last <= LAST_SUPPORTED_PICKER_YEAR)
        require(endDateMillis == null || (startDateMillis != null && startDateMillis <= endDateMillis))
    }
}

@Composable
@Suppress("LongParameterList")
fun DomposableDateRangePicker(
    startDateMillis: Long?,
    endDateMillis: Long?,
    onDateRangeChange: (Long?, Long?) -> Unit,
    yearRange: IntRange = DEFAULT_FIRST_PICKER_YEAR..DEFAULT_LAST_PICKER_YEAR,
    mode: DomposableDatePickerMode = DomposableDatePickerMode.Calendar,
    onModeChange: ((DomposableDatePickerMode) -> Unit)? = null,
    isDateSelectable: (Long) -> Boolean = { true },
    showModeToggle: Boolean = true,
    appearance: DomposableSurfaceAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableDateRangePicker(
    DomposableDateRangePickerProps(
        startDateMillis,
        endDateMillis,
        onDateRangeChange,
        yearRange,
        mode,
        onModeChange,
        isDateSelectable,
        showModeToggle,
        appearance,
    ),
    modifier,
)

@DomposableComponent("TimePicker")
data class DomposableTimePickerProps(
    val hour: Int,
    val minute: Int,
    val onTimeChange: (Int, Int) -> Unit,
    val is24Hour: Boolean = true,
    val mode: DomposableTimePickerMode = DomposableTimePickerMode.Dial,
    val appearance: DomposableSurfaceAppearance? = null,
) {
    init {
        require(hour in 0 until HOURS_PER_DAY && minute in 0 until MINUTES_PER_HOUR)
    }
}

@Composable
fun DomposableTimePicker(
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit,
    is24Hour: Boolean = true,
    mode: DomposableTimePickerMode = DomposableTimePickerMode.Dial,
    appearance: DomposableSurfaceAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableTimePicker(
    DomposableTimePickerProps(hour, minute, onTimeChange, is24Hour, mode, appearance),
    modifier,
)
