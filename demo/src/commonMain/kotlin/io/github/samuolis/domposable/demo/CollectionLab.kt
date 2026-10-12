package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableLazyColumn
import io.github.samuolis.domposable.ui.DomposableListWindow
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableSelect
import io.github.samuolis.domposable.ui.DomposableSelectOption
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width

@Composable
@DomposableScreen(DemoRoute.Collections::class)
fun CollectionLab(onMore: () -> Unit = {}) {
    var count by rememberSaveable { mutableStateOf("10000") }
    var windowed by rememberSaveable { mutableStateOf(true) }
    var reversed by rememberSaveable { mutableStateOf(false) }
    val items = remember(count, reversed) { List(count.toInt()) { if (reversed) count.toInt() - it - 1 else it } }
    DomposableColumn(DomposableModifier.padding(16.dp).fillMaxWidth(), gap = 8.dp) {
        DomposableText("Large collections", headingLevel = 1)
        DomposableSelect(
            count,
            { count = it },
            "Row count",
            listOf("5", "1000", "10000").map { DomposableSelectOption(it, it) },
        )
        DomposableSwitch(windowed, { windowed = it }, "Windowed list")
        DomposableButton("Reverse rows", { reversed = !reversed })
        DomposableLazyColumn(
            items,
            key = { "row-$it" },
            modifier = DomposableModifier.testId("collection-lab-list").fillMaxWidth(),
            gap = 4.dp,
            window = if (windowed) DomposableListWindow(88.dp, 352.dp) else null,
        ) { index ->
            var note by rememberSaveable { mutableStateOf("") }
            DomposableRow(gap = 8.dp) {
                DomposableText("Row ${index + 1}", DomposableModifier.width(90.dp))
                DomposableTextField(note, { note = it }, "Row ${index + 1} note", DomposableModifier.width(220.dp))
            }
        }
        DomposableButton("More collection recipes", onMore)
    }
}
