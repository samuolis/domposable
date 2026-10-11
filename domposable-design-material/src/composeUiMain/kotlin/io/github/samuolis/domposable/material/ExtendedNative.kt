@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import io.github.samuolis.domposable.ui.DomposableFloatingActionButtonProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRangeSliderProps
import io.github.samuolis.domposable.ui.DomposableSegmentedButtonsProps
import io.github.samuolis.domposable.ui.DomposableSelectProps
import io.github.samuolis.domposable.ui.DomposableSnackbarProps
import io.github.samuolis.domposable.ui.DomposableTooltipProps
import io.github.samuolis.domposable.ui.DomposableTopAppBarProps
import io.github.samuolis.domposable.ui.toComposeModifier

@Composable
internal fun NativeTopAppBar(
    props: DomposableTopAppBarProps,
    modifier: DomposableModifier,
) {
    TopAppBar(
        title = { Text(props.title) },
        modifier = modifier.toComposeModifier(),
        navigationIcon = props.navigationIcon,
        actions = { props.actions() },
    )
}

@Composable
internal fun NativeFloatingActionButton(
    props: DomposableFloatingActionButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    FloatingActionButton(props.onClick, modifier.toComposeModifier().semantics { contentDescription = props.label }) {
        content()
    }
}

@Composable
internal fun NativeSnackbar(
    props: DomposableSnackbarProps,
    modifier: DomposableModifier,
) {
    val actionColors =
        ButtonDefaults.textButtonColors(
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.inversePrimary,
        )
    Snackbar(
        modifier.toComposeModifier().semantics { liveRegion = LiveRegionMode.Polite },
        action =
            props.onAction?.let { action ->
                { TextButton(action, colors = actionColors) { Text(props.actionLabel!!) } }
            },
        dismissAction =
            props.onDismiss?.let { dismiss ->
                { TextButton(dismiss, colors = actionColors) { Text("Dismiss") } }
            },
    ) { Text(props.message) }
}

@Composable
internal fun NativeSelect(
    props: DomposableSelectProps,
    modifier: DomposableModifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier.toComposeModifier()) {
        OutlinedButton(
            { expanded = true },
            enabled = props.enabled,
            modifier =
                Modifier.semantics {
                    contentDescription = props.label
                    props.error?.let { error(it) }
                },
        ) { Text("${props.label}: ${props.options.first { it.value == props.value }.label}") }
        DropdownMenu(expanded && props.enabled, { expanded = false }) {
            props.options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        expanded = false
                        props.onValueChange(option.value)
                    },
                    enabled = option.enabled,
                )
            }
        }
    }
    props.error?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
}

@Composable
internal fun NativeSegmentedButtons(
    props: DomposableSegmentedButtonsProps,
    modifier: DomposableModifier,
) {
    SingleChoiceSegmentedButtonRow(modifier.toComposeModifier().semantics { contentDescription = props.label }) {
        props.labels.forEachIndexed { index, label ->
            SegmentedButton(
                selected = index == props.selectedIndex,
                onClick = { props.onSelect(index) },
                enabled = props.enabled,
                shape = SegmentedButtonDefaults.itemShape(index, props.labels.size),
            ) { Text(label) }
        }
    }
}

@Composable
internal fun NativeRangeSlider(
    props: DomposableRangeSliderProps,
    modifier: DomposableModifier,
) {
    val startInteraction = remember { MutableInteractionSource() }
    val endInteraction = remember { MutableInteractionSource() }
    val state =
        remember(props.range, props.steps) {
            RangeSliderState(props.value.start, props.value.endInclusive, props.steps, trackRange = props.range)
        }
    state.startValue = props.value.start
    state.endValue = props.value.endInclusive
    RangeSlider(
        state = state,
        onValueChange = props.onValueChange,
        modifier = modifier.toComposeModifier(),
        enabled = props.enabled,
        startThumbInteractionSource = startInteraction,
        endThumbInteractionSource = endInteraction,
        startThumb = {
            SliderDefaults.Thumb(
                startInteraction,
                Modifier.semantics {
                    contentDescription = props.startLabel
                },
                enabled = props.enabled,
            )
        },
        endThumb = {
            SliderDefaults.Thumb(
                endInteraction,
                Modifier.semantics {
                    contentDescription = props.endLabel
                },
                enabled = props.enabled,
            )
        },
    )
}

@Composable
internal fun NativeTooltip(
    props: DomposableTooltipProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(props.text) } },
        state = rememberTooltipState(),
        modifier = modifier.toComposeModifier(),
        content = content,
    )
}
