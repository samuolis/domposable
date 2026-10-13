package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableChoiceOption

/** Typed choices use option values, rather than translated labels, as their selection identity. */
@Composable
@Suppress("LongParameterList")
fun <T> DomposableSegmentedButtons(
    options: List<DomposableChoiceOption<T>>,
    selectedValue: T,
    onSelectionChange: (T) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    groupName: String? = null,
    showSelectedIcon: Boolean = true,
    allowReselect: Boolean = false,
) {
    require(options.map { it.value }.distinct().size == options.size) { "Choice values must be unique" }
    DomposableSegmentedButtons(
        props =
            DomposableSegmentedButtonsProps(
                labels = options.map { it.label },
                selectedIndex = options.indexOfFirst { it.value == selectedValue },
                onSelect = { onSelectionChange(options[it].value) },
                label = label,
                enabled = enabled,
                optionEnabled = options.map { it.enabled },
                optionTestTags = options.map { it.testTag },
                groupName = groupName,
                showSelectedIcon = showSelectedIcon,
                allowReselect = allowReselect,
            ),
        modifier = modifier,
    )
}
