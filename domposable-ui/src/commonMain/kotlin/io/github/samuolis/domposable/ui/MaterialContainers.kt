package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableComponent
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.dp

/** Multiple independent selected segments, with stable indices within the supplied labels. */
@DomposableComponent("MultiChoiceSegmentedButtons")
data class DomposableMultiChoiceSegmentedButtonsProps(
    val labels: List<String>,
    val selectedIndices: Set<Int>,
    val onSelectionChange: (Set<Int>) -> Unit,
    val label: String,
    val enabled: Boolean = true,
    val optionEnabled: List<Boolean> = labels.map { true },
    val appearance: DomposableItemAppearance? = null,
) {
    init {
        require(labels.isNotEmpty())
        require(selectedIndices.all { it in labels.indices })
        require(optionEnabled.size == labels.size)
    }
}

@Composable
fun DomposableMultiChoiceSegmentedButtons(
    labels: List<String>,
    selectedIndices: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    optionEnabled: List<Boolean> = labels.map { true },
    appearance: DomposableItemAppearance? = null,
) = DomposableMultiChoiceSegmentedButtons(
    DomposableMultiChoiceSegmentedButtonsProps(
        labels,
        selectedIndices,
        onSelectionChange,
        label,
        enabled,
        optionEnabled,
        appearance,
    ),
    modifier,
)

/** Overflow-aware action container; the application owns each action and its identity. */
@DomposableComponent("AppBar")
data class DomposableAppBarProps(
    val items: List<DomposableActionItem>,
    val label: String,
    val orientation: DomposableOrientation = DomposableOrientation.Horizontal,
    val overflowLabel: String = "More actions",
    val spacing: DomposableLength = 4.dp,
    val appearance: DomposableButtonAppearance? = null,
) {
    init {
        require(items.map { it.key }.distinct().size == items.size)
    }
}

@Composable
fun DomposableAppBar(
    items: List<DomposableActionItem>,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    orientation: DomposableOrientation = DomposableOrientation.Horizontal,
    overflowLabel: String = "More actions",
    spacing: DomposableLength = 4.dp,
    appearance: DomposableButtonAppearance? = null,
) = DomposableAppBar(DomposableAppBarProps(items, label, orientation, overflowLabel, spacing, appearance), modifier)

@DomposableComponent("WideNavigationRailItem")
data class DomposableWideNavigationRailItemProps(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
    val railExpanded: Boolean,
    val enabled: Boolean = true,
    val icon: @Composable () -> Unit = {},
    val appearance: DomposableItemAppearance? = null,
)

@Composable
fun DomposableWideNavigationRailItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    railExpanded: Boolean,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit = {},
    appearance: DomposableItemAppearance? = null,
) = DomposableWideNavigationRailItem(
    DomposableWideNavigationRailItemProps(label, selected, onClick, railExpanded, enabled, icon, appearance),
    modifier,
)

/** All three checkbox states are visible and announced consistently on native and HTML. */
@Composable
fun DomposableTriStateCheckbox(
    state: DomposableToggleState,
    onClick: () -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    appearance: io.github.samuolis.domposable.foundation.DomposableCheckboxAppearance? = null,
) = DomposableCheckbox(
    DomposableCheckboxProps(
        checked = state == DomposableToggleState.On,
        onCheckedChange = { onClick() },
        label = label,
        enabled = enabled,
        appearance = appearance,
        indeterminate = state == DomposableToggleState.Indeterminate,
    ),
    modifier,
)
