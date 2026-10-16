@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.isPm
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.ui.DomposableScrollFieldProps
import io.github.samuolis.domposable.ui.asDomposableModifier
import io.github.samuolis.domposable.ui.domposableFormEnabled

private const val HOURS_PER_PERIOD = 12

@Composable
internal fun NativeTimeScroll(
    state: TimePickerState,
    colors: TimePickerColors,
) {
    val enabled = domposableFormEnabled()
    Row(
        modifier = Modifier.semantics { isTraversalGroup = true },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NativeScrollField(
            DomposableScrollFieldProps(
                itemCount = if (state.is24hour) 24 else HOURS_PER_PERIOD,
                selectedIndex =
                    if (state.is24hour) {
                        state.hour
                    } else {
                        (state.hour + HOURS_PER_PERIOD - 1) %
                            HOURS_PER_PERIOD
                    },
                onSelectedIndexChange = { index ->
                    state.hour =
                        if (state.is24hour) {
                            index
                        } else {
                            (index + 1) % HOURS_PER_PERIOD + if (state.isPm) HOURS_PER_PERIOD else 0
                        }
                },
                label = "Hour",
                indexDescription = { index -> hourValue(index, state.is24hour) },
                enabled = enabled,
                itemContent = { index, selected, itemEnabled ->
                    Text(
                        text = hourValue(index, state.is24hour),
                        color =
                            when {
                                !itemEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                selected -> colors.timeSelectorSelectedContentColor
                                else -> colors.timeSelectorUnselectedContentColor
                            },
                    )
                },
            ),
            Modifier.size(100.dp, ScrollFieldDefaults.ScrollFieldHeight).asDomposableModifier(),
        )
        Spacer(Modifier.width(8.dp))
        Text(":", style = MaterialTheme.typography.displayLarge, color = colors.timeSelectorSelectedContentColor)
        Spacer(Modifier.width(8.dp))
        NativeScrollField(
            DomposableScrollFieldProps(
                itemCount = 60,
                selectedIndex = state.minute,
                onSelectedIndexChange = { state.minute = it },
                label = "Minute",
                indexDescription = { it.toString().padStart(2, '0') },
                enabled = enabled,
                itemContent = { index, selected, itemEnabled ->
                    Text(
                        text = index.toString().padStart(2, '0'),
                        color =
                            when {
                                !itemEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                selected -> colors.timeSelectorSelectedContentColor
                                else -> colors.timeSelectorUnselectedContentColor
                            },
                    )
                },
            ),
            Modifier.size(100.dp, ScrollFieldDefaults.ScrollFieldHeight).asDomposableModifier(),
        )
        if (!state.is24hour) {
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !state.isPm,
                    onClick = { if (state.isPm) state.hour -= HOURS_PER_PERIOD },
                    enabled = enabled,
                    label = { Text("AM") },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            containerColor = colors.periodSelectorUnselectedContainerColor,
                            labelColor = colors.periodSelectorUnselectedContentColor,
                            selectedContainerColor = colors.periodSelectorSelectedContainerColor,
                            selectedLabelColor = colors.periodSelectorSelectedContentColor,
                        ),
                    border =
                        FilterChipDefaults.filterChipBorder(
                            enabled = enabled,
                            selected = !state.isPm,
                            borderColor = colors.periodSelectorBorderColor,
                            selectedBorderColor = colors.periodSelectorBorderColor,
                        ),
                )
                FilterChip(
                    selected = state.isPm,
                    onClick = { if (!state.isPm) state.hour += HOURS_PER_PERIOD },
                    enabled = enabled,
                    label = { Text("PM") },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            containerColor = colors.periodSelectorUnselectedContainerColor,
                            labelColor = colors.periodSelectorUnselectedContentColor,
                            selectedContainerColor = colors.periodSelectorSelectedContainerColor,
                            selectedLabelColor = colors.periodSelectorSelectedContentColor,
                        ),
                    border =
                        FilterChipDefaults.filterChipBorder(
                            enabled = enabled,
                            selected = state.isPm,
                            borderColor = colors.periodSelectorBorderColor,
                            selectedBorderColor = colors.periodSelectorBorderColor,
                        ),
                )
            }
        }
    }
}

private fun hourValue(
    index: Int,
    is24hour: Boolean,
): String =
    if (is24hour) {
        index.toString().padStart(2, '0')
    } else {
        ((index + 1) % HOURS_PER_PERIOD).let { if (it == 0) HOURS_PER_PERIOD else it }.toString()
    }
