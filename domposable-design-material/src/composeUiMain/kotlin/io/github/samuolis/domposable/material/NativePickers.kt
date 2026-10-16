@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimeInputDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimeScroll
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.ui.DomposableDatePickerMode
import io.github.samuolis.domposable.ui.DomposableDatePickerProps
import io.github.samuolis.domposable.ui.DomposableDateRangePickerProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTimePickerMode
import io.github.samuolis.domposable.ui.DomposableTimePickerProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableFormEnabled
import io.github.samuolis.domposable.ui.toComposeModifier
import kotlinx.coroutines.flow.drop

/** The selected value comes from the caller even when a change is rejected. */
internal fun controlledDateState(
    base: DatePickerState,
    current: State<DomposableDatePickerProps>,
    isEnabled: () -> Boolean = { true },
): DatePickerState =
    object : DatePickerState by base {
        override var selectedDateMillis: Long?
            get() = current.value.selectedDateMillis
            set(value) {
                val props = current.value
                if (!isEnabled() || value == props.selectedDateMillis) return
                if (value == null || props.isDateSelectable(value)) {
                    base.selectedDateMillis = value
                    props.onDateChange(base.selectedDateMillis)
                }
            }
        override var displayMode: DisplayMode
            get() = if (current.value.onModeChange == null) base.displayMode else current.value.mode.nativeDisplayMode()
            set(value) {
                if (!isEnabled()) return
                val change = current.value.onModeChange
                if (change == null) base.displayMode = value else change(value.sharedDisplayMode())
            }
    }

internal fun controlledRangeState(
    base: DateRangePickerState,
    current: State<DomposableDateRangePickerProps>,
    isEnabled: () -> Boolean = { true },
): DateRangePickerState =
    object : DateRangePickerState by base {
        override val selectedStartDateMillis: Long? get() = current.value.startDateMillis
        override val selectedEndDateMillis: Long? get() = current.value.endDateMillis

        override fun setSelection(
            startDateMillis: Long?,
            endDateMillis: Long?,
        ) {
            val props = current.value
            if (!isEnabled()) return
            if (startDateMillis == props.startDateMillis && endDateMillis == props.endDateMillis) return
            val startSelectable = startDateMillis == null || props.isDateSelectable(startDateMillis)
            val endSelectable = endDateMillis == null || props.isDateSelectable(endDateMillis)
            if (startSelectable && endSelectable) {
                base.setSelection(startDateMillis, endDateMillis)
                props.onDateRangeChange(base.selectedStartDateMillis, base.selectedEndDateMillis)
            }
        }

        override var displayMode: DisplayMode
            get() = if (current.value.onModeChange == null) base.displayMode else current.value.mode.nativeDisplayMode()
            set(value) {
                if (!isEnabled()) return
                val change = current.value.onModeChange
                if (change == null) base.displayMode = value else change(value.sharedDisplayMode())
            }
    }

@Composable
internal fun NativeDatePicker(
    props: DomposableDatePickerProps,
    modifier: DomposableModifier,
) {
    val current = rememberUpdatedState(props)
    val enabled = rememberUpdatedState(domposableFormEnabled())
    val selectable = remember(props.isDateSelectable) { nativeSelectableDates(props.isDateSelectable) }
    val base =
        key(props.yearRange, selectable) {
            rememberDatePickerState(
                props.selectedDateMillis,
                yearRange = props.yearRange,
                initialDisplayMode = props.mode.nativeDisplayMode(),
                selectableDates = selectable,
            )
        }
    val state = remember(base) { controlledDateState(base, current) { enabled.value } }
    LaunchedEffect(base, props.mode) { base.displayMode = props.mode.nativeDisplayMode() }
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.datePicker
    NativePickerSurface(appearance, modifier, "Date picker") {
        DatePicker(state, showModeToggle = props.showModeToggle, colors = appearance.datePickerColors())
    }
}

@Composable
internal fun NativeDateRangePicker(
    props: DomposableDateRangePickerProps,
    modifier: DomposableModifier,
) {
    val current = rememberUpdatedState(props)
    val enabled = rememberUpdatedState(domposableFormEnabled())
    val selectable = remember(props.isDateSelectable) { nativeSelectableDates(props.isDateSelectable) }
    val base =
        key(props.yearRange, selectable) {
            rememberDateRangePickerState(
                props.startDateMillis,
                props.endDateMillis,
                yearRange = props.yearRange,
                initialDisplayMode = props.mode.nativeDisplayMode(),
                selectableDates = selectable,
            )
        }
    val state = remember(base) { controlledRangeState(base, current) { enabled.value } }
    LaunchedEffect(base, props.mode) { base.displayMode = props.mode.nativeDisplayMode() }
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.dateRangePicker
    NativePickerSurface(appearance, modifier, "Date range") {
        DateRangePicker(state, showModeToggle = props.showModeToggle, colors = appearance.datePickerColors())
    }
}

@Composable
internal fun NativeTimePicker(
    props: DomposableTimePickerProps,
    modifier: DomposableModifier,
) {
    val state = key(props.is24Hour) { rememberTimePickerState(props.hour, props.minute, props.is24Hour) }
    val current by rememberUpdatedState(props)
    val enabled by rememberUpdatedState(domposableFormEnabled())
    LaunchedEffect(state) {
        snapshotFlow { state.hour to state.minute }.drop(1).collect { (hour, minute) ->
            if (hour != current.hour || minute != current.minute) {
                if (enabled) current.onTimeChange(hour, minute)
                withFrameNanos { }
                if (state.hour != current.hour) state.hour = current.hour
                if (state.minute != current.minute) state.minute = current.minute
            }
        }
    }
    LaunchedEffect(props.hour, props.minute, state) {
        state.hour = props.hour
        state.minute = props.minute
    }
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.timePicker
    val contentColor = appearance.content?.nativeColor() ?: Color.Unspecified
    val colors =
        TimePickerDefaults.colors(
            containerColor = appearance.pickerContainer(),
            clockDialContentColor = contentColor,
            periodSelectorContentColor = contentColor,
            timeSelectorContentColor = contentColor,
        )
    NativePickerSurface(appearance, modifier, "Time picker") {
        when (props.mode) {
            DomposableTimePickerMode.Input -> {
                TimeInput(
                    state,
                    colors =
                        TimeInputDefaults.colors(
                            containerColor = appearance.pickerContainer(),
                            periodSelectorContentColor = contentColor,
                            timeTextFieldColors =
                                TextFieldDefaults.colors(
                                    focusedTextColor = contentColor,
                                    unfocusedTextColor = contentColor,
                                ),
                        ),
                )
            }

            DomposableTimePickerMode.Dial -> {
                TimePicker(state, colors = colors)
            }

            DomposableTimePickerMode.Scroll -> {
                TimeScroll(state, TimePickerDefaults.shapes(), colors = colors)
            }
        }
    }
}

@Composable
private fun DomposableSurfaceAppearance.datePickerColors() =
    DatePickerDefaults.colors(
        containerColor = pickerContainer(),
        titleContentColor = content?.nativeColor() ?: Color.Unspecified,
        headlineContentColor = content?.nativeColor() ?: Color.Unspecified,
        weekdayContentColor = content?.nativeColor() ?: Color.Unspecified,
        subheadContentColor = content?.nativeColor() ?: Color.Unspecified,
        navigationContentColor = content?.nativeColor() ?: Color.Unspecified,
        yearContentColor = content?.nativeColor() ?: Color.Unspecified,
        dayContentColor = content?.nativeColor() ?: Color.Unspecified,
    )

@Composable
private fun DomposableSurfaceAppearance.pickerContainer(): Color =
    container?.nativeColor() ?: tonalElevation?.let { MaterialTheme.colorScheme.surfaceColorAtElevation(it.value.dp) }
        ?: Color.Unspecified

@Composable
private fun NativePickerSurface(
    appearance: DomposableSurfaceAppearance,
    modifier: DomposableModifier,
    label: String,
    content: @Composable () -> Unit,
) {
    val enabled = domposableFormEnabled()
    val nativeModifier =
        modifier.toComposeModifier().then(
            if (enabled) {
                Modifier
            } else {
                Modifier
                    .alpha(0.38f)
                    .clearAndSetSemantics {
                        contentDescription = label
                        disabled()
                    }.onPreviewKeyEvent { true }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                            }
                        }
                    }
            },
        )
    NativeWindow {
        if (appearance.isEmpty()) {
            Box(nativeModifier) { content() }
        } else {
            Surface(
                modifier = nativeModifier,
                shape = appearance.nativeShape(DatePickerDefaults.shape),
                color = appearance.pickerContainer().let { if (it == Color.Unspecified) Color.Transparent else it },
                contentColor = appearance.content?.nativeColor() ?: LocalContentColor.current,
                shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
                border = appearance.border?.let { BorderStroke(appearance.borderWidth.value.dp, it.nativeColor()) },
            ) { StyledContent(appearance.textAppearance, content) }
        }
    }
}

private fun DomposableDatePickerMode.nativeDisplayMode(): DisplayMode =
    if (this ==
        DomposableDatePickerMode.Input
    ) {
        DisplayMode.Input
    } else {
        DisplayMode.Picker
    }

private fun DisplayMode.sharedDisplayMode(): DomposableDatePickerMode =
    if (this ==
        DisplayMode.Input
    ) {
        DomposableDatePickerMode.Input
    } else {
        DomposableDatePickerMode.Calendar
    }

private fun nativeSelectableDates(predicate: (Long) -> Boolean): SelectableDates =
    object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean = predicate(utcTimeMillis)

        override fun isSelectableYear(year: Int): Boolean = true
    }
