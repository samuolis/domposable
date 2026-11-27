package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalDatePickerMonthHeader =
    staticCompositionLocalOf<(@Composable (Long, (Long) -> Unit) -> Unit)?> { null }

/** Adds controls for the visible calendar month, expressed as UTC epoch milliseconds. */
@Composable
fun DomposableDatePickerMonthHeader(
    header: @Composable (displayedMonthMillis: Long, showMonth: (Long) -> Unit) -> Unit,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDatePickerMonthHeader provides header, content = content)
}
