package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableComponent

/** App-owned values stay stable when options are reordered or their labels are translated. */
data class DomposableSelectOption(
    val value: String,
    val label: String,
    val enabled: Boolean = true,
)

@DomposableComponent("TopAppBar")
data class DomposableTopAppBarProps(
    val title: String,
    val navigationIcon: @Composable () -> Unit = {},
    val actions: @Composable () -> Unit = {},
)

@DomposableComponent("FloatingActionButton")
data class DomposableFloatingActionButtonProps(
    val onClick: () -> Unit,
    val label: String,
)

/** Presentation only: the app decides when to show, dismiss or replace a message. */
@DomposableComponent("Snackbar")
data class DomposableSnackbarProps(
    val message: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val onDismiss: (() -> Unit)? = null,
) {
    init {
        require((actionLabel == null) == (onAction == null)) { "Supply both actionLabel and onAction" }
    }
}

@DomposableComponent("Select")
data class DomposableSelectProps(
    val value: String,
    val onValueChange: (String) -> Unit,
    val label: String,
    val options: List<DomposableSelectOption>,
    val enabled: Boolean = true,
    val error: String? = null,
) {
    init {
        require(options.map { it.value }.distinct().size == options.size) { "Select values must be unique" }
        require(options.any { it.value == value }) { "The selected value must be an option" }
    }
}

@DomposableComponent("SegmentedButtons")
data class DomposableSegmentedButtonsProps(
    val labels: List<String>,
    val selectedIndex: Int,
    val onSelect: (Int) -> Unit,
    val label: String,
    val enabled: Boolean = true,
) {
    init {
        require(labels.isNotEmpty()) { "Supply at least one segment" }
        require(selectedIndex in labels.indices) { "The selected segment must exist" }
    }
}

@DomposableComponent("RangeSlider")
data class DomposableRangeSliderProps(
    val value: ClosedFloatingPointRange<Float>,
    val onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    val startLabel: String,
    val endLabel: String,
    val range: ClosedFloatingPointRange<Float> = 0f..1f,
    val steps: Int = 0,
    val enabled: Boolean = true,
) {
    init {
        require(range.start.isFinite() && range.endInclusive.isFinite() && range.start < range.endInclusive)
        require(value.start.isFinite() && value.endInclusive.isFinite())
        require(value.start in range && value.endInclusive in range && value.start <= value.endInclusive)
        require(steps >= 0)
    }
}

/** Supplemental help for an already labelled control; never its only accessible name. */
@DomposableComponent("Tooltip")
data class DomposableTooltipProps(
    val text: String,
)
