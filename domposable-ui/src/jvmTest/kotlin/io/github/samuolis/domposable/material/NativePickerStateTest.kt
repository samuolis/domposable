@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package io.github.samuolis.domposable.material

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.runtime.mutableStateOf
import io.github.samuolis.domposable.ui.DomposableDatePickerMode
import io.github.samuolis.domposable.ui.DomposableDatePickerProps
import io.github.samuolis.domposable.ui.DomposableDateRangePickerProps
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class NativePickerStateTest {
    @Test
    fun dateRequestsDoNotReplaceARejectedControlledSelection() {
        var requests = 0
        val props = mutableStateOf(DomposableDatePickerProps(0L, { requests++ }))
        val state = controlledDateState(DatePickerState(Locale.US, 0L), props)
        state.selectedDateMillis = 86_400_000L
        assertEquals(1, requests)
        assertEquals(0L, state.selectedDateMillis)
        props.value = props.value.copy(selectedDateMillis = 172_800_000L)
        assertEquals(172_800_000L, state.selectedDateMillis)
        assertEquals(1, requests)
    }

    @Test
    fun datePredicateRejectsRequestsAndAcceptedValuesComeFromTheCaller() {
        val props = mutableStateOf(DomposableDatePickerProps(0L, {}, isDateSelectable = { it < 172_800_000L }))
        props.value = props.value.copy(onDateChange = { props.value = props.value.copy(selectedDateMillis = it) })
        val state = controlledDateState(DatePickerState(Locale.US, 0L), props)
        state.selectedDateMillis = 172_800_000L
        assertEquals(0L, state.selectedDateMillis)
        state.selectedDateMillis = 86_400_000L
        assertEquals(86_400_000L, state.selectedDateMillis)
    }

    @Test
    fun rangeRequestsRemainControlledAndExternalChangesDoNotEcho() {
        var requests = 0
        val props = mutableStateOf(DomposableDateRangePickerProps(0L, 86_400_000L, { _, _ -> requests++ }))
        val state = controlledRangeState(DateRangePickerState(Locale.US, 0L, 86_400_000L), props)
        state.setSelection(172_800_000L, null)
        assertEquals(1, requests)
        assertEquals(0L, state.selectedStartDateMillis)
        assertEquals(86_400_000L, state.selectedEndDateMillis)
        props.value = props.value.copy(startDateMillis = null, endDateMillis = null)
        assertEquals(null, state.selectedStartDateMillis)
        assertEquals(null, state.selectedEndDateMillis)
        assertEquals(1, requests)
    }

    @Test
    fun disabledPickersRejectSelectionAndModeRequests() {
        var requests = 0
        val date = mutableStateOf(DomposableDatePickerProps(0L, { requests++ }, onModeChange = { requests++ }))
        val dateState = controlledDateState(DatePickerState(Locale.US, 0L), date, isEnabled = { false })
        dateState.selectedDateMillis = 86_400_000L
        dateState.displayMode = DisplayMode.Input
        val range = mutableStateOf(DomposableDateRangePickerProps(0L, 86_400_000L, { _, _ -> requests++ }))
        val rangeState =
            controlledRangeState(DateRangePickerState(Locale.US, 0L, 86_400_000L), range, isEnabled = { false })
        rangeState.setSelection(172_800_000L, null)
        rangeState.displayMode = DisplayMode.Input
        assertEquals(0, requests)
        assertEquals(0L, dateState.selectedDateMillis)
        assertEquals(DisplayMode.Picker, dateState.displayMode)
        assertEquals(0L, rangeState.selectedStartDateMillis)
        assertEquals(86_400_000L, rangeState.selectedEndDateMillis)
        assertEquals(DisplayMode.Picker, rangeState.displayMode)
    }

    @Test
    fun modeIsControlledWithACallbackAndLocallyInteractiveWithoutOne() {
        var requests = 0
        val props = mutableStateOf(DomposableDatePickerProps(null, {}, onModeChange = { requests++ }))
        val state = controlledDateState(DatePickerState(Locale.US), props)
        state.displayMode = DisplayMode.Input
        assertEquals(1, requests)
        assertEquals(DisplayMode.Picker, state.displayMode)
        props.value = props.value.copy(mode = DomposableDatePickerMode.Input)
        assertEquals(DisplayMode.Input, state.displayMode)
        props.value = props.value.copy(onModeChange = null)
        state.displayMode = DisplayMode.Picker
        assertEquals(DisplayMode.Picker, state.displayMode)
        assertEquals(1, requests)
    }
}
