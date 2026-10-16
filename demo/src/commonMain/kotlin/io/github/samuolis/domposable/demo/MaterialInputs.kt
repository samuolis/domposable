package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableScaffoldFabPosition
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldVariant
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableAutocomplete
import io.github.samuolis.domposable.ui.DomposableBottomAppBar
import io.github.samuolis.domposable.ui.DomposableBottomSheetScaffold
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableDatePicker
import io.github.samuolis.domposable.ui.DomposableDatePickerMode
import io.github.samuolis.domposable.ui.DomposableDateRangePicker
import io.github.samuolis.domposable.ui.DomposableFloatingActionButton
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableScreenScaffold
import io.github.samuolis.domposable.ui.DomposableScrollField
import io.github.samuolis.domposable.ui.DomposableSearchBar
import io.github.samuolis.domposable.ui.DomposableSelectOption
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableTimePicker
import io.github.samuolis.domposable.ui.DomposableTimePickerMode
import io.github.samuolis.domposable.ui.DomposableTopAppBar
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.testId

@Composable
internal fun MaterialInputs() {
    var query by rememberSaveable { mutableStateOf("") }
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var value by rememberSaveable { mutableStateOf("") }
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var date by remember { mutableStateOf<Long?>(0L) }
    var start by remember { mutableStateOf<Long?>(0L) }
    var end by remember { mutableStateOf<Long?>(86_400_000L) }
    var hour by rememberSaveable { mutableStateOf(10) }
    var minute by rememberSaveable { mutableStateOf(30) }
    var dateMode by rememberSaveable(stateSaver = enumSaver<DomposableDatePickerMode>()) {
        mutableStateOf(DomposableDatePickerMode.Calendar)
    }
    var rangeMode by rememberSaveable(stateSaver = enumSaver<DomposableDatePickerMode>()) {
        mutableStateOf(DomposableDatePickerMode.Calendar)
    }
    var timeMode by rememberSaveable(stateSaver = enumSaver<DomposableTimePickerMode>()) {
        mutableStateOf(DomposableTimePickerMode.Dial)
    }
    val options =
        listOf(
            DomposableSelectOption("red", "Red"),
            DomposableSelectOption("green", "Green"),
            DomposableSelectOption("blue", "Blue"),
        )
    DomposableTextField(value, { value = it }, "Outlined field")
    DomposableTextField(value, {
        value = it
    }, "Filled field", appearance = DomposableTextFieldAppearance(variant = DomposableTextFieldVariant.Filled))
    DomposableTextField(value, { value = it }, "Password", purpose = DomposableInputPurpose.Password)
    DomposableTextField(value, {
        value = it
    }, "Field with supporting text", helperText = "A short hint", leadingIcon = { DomposableText("@") })
    DomposableTextField(
        value,
        { value = it },
        "Search documents",
        placeholder = "Search documents",
        showLabel = false,
    )
    DomposableSearchBar(query, {
        query = it
    }, searchExpanded, { searchExpanded = it }, { searchExpanded = false }, "Search products") {
        options.filter { it.label.contains(query, ignoreCase = true) }.forEach { option ->
            DomposableButton(option.label, {
                query = option.label
                searchExpanded = false
            })
        }
    }
    DomposableAutocomplete(value, { value = it }, {
        value = it.label
        dropdownExpanded = false
    }, options.filter { it.label.contains(value, ignoreCase = true) }, dropdownExpanded, {
        dropdownExpanded =
            it
    }, "Favorite color")
    DomposableText("Date selection uses UTC date values.")
    DomposableDatePicker(date, {
        date = it
    }, mode = dateMode, onModeChange = { dateMode = it }, modifier = DomposableModifier.fillMaxWidth())
    DomposableDateRangePicker(start, end, { first, last ->
        start = first
        end = last
    }, mode = rangeMode, onModeChange = {
        rangeMode =
            it
    }, modifier = DomposableModifier.fillMaxWidth().height(480.dp))
    DomposableButton("Switch time entry", {
        timeMode =
            DomposableTimePickerMode.entries[(timeMode.ordinal + 1) % DomposableTimePickerMode.entries.size]
    })
    DomposableTimePicker(hour, minute, { h, m ->
        hour = h
        minute = m
    }, mode = timeMode)
    DomposableScrollField(24, hour, {
        hour = it
    }, "Hour wheel", indexDescription = { "$it hours" }, modifier = DomposableModifier.height(160.dp))
    DomposableText(
        "Selected time: $hour:${minute.toString().padStart(2, '0')}",
        modifier = DomposableModifier.testId("material-selected-time"),
    )
}

@Composable
internal fun MaterialScaffold() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var sheetGestures by rememberSaveable { mutableStateOf(true) }
    DomposableScreenScaffold(
        modifier = DomposableModifier.fillMaxWidth().height(420.dp).testId("material-screen-scaffold"),
        insets = DomposableInsets(16.dp),
        topBar = {
            DomposableTopAppBar(
                "Shared screen",
                actions = { DomposableButton("Menu", { expanded = !expanded }) },
            )
        },
        bottomBar = { DomposableBottomAppBar { DomposableText("Footer actions") } },
        floatingAction = { DomposableFloatingActionButton({}, "Add document") { DomposableText("+") } },
        floatingActionPosition = DomposableScaffoldFabPosition.Center,
    ) {
        DomposableColumn(contentModifier.fillMaxWidth(), gap = 12.dp) {
            DomposableText("The app owns its state, injection and navigation.")
            DomposableText("The scaffold owns only layout and chrome slots.")
        }
    }
    DomposableSwitch(sheetGestures, { sheetGestures = it }, "Sheet gestures")
    DomposableBottomSheetScaffold(
        expanded,
        { expanded = it },
        sheetContent = { DomposableText("Persistent sheet content") },
        swipeEnabled = sheetGestures,
        modifier = DomposableModifier.fillMaxWidth().height(280.dp).testId("material-persistent-sheet"),
    ) { DomposableButton("Toggle persistent sheet", { expanded = !expanded }) }
}
