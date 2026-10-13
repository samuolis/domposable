package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableChoiceOption
import io.github.samuolis.domposable.foundation.DomposableImeAction
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableChartSeries
import io.github.samuolis.domposable.ui.DomposableChartValue
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableDataTable
import io.github.samuolis.domposable.ui.DomposableDataTableColumn
import io.github.samuolis.domposable.ui.DomposableDataTableRow
import io.github.samuolis.domposable.ui.DomposableDisclosure
import io.github.samuolis.domposable.ui.DomposableFlowRow
import io.github.samuolis.domposable.ui.DomposableForm
import io.github.samuolis.domposable.ui.DomposableLineChart
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRichHtml
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableSegmentedButtons
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableValueChart
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.horizontalScroll
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.rememberDomposableFormSubmit
import io.github.samuolis.domposable.ui.rememberDomposableScrollState
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll
import io.github.samuolis.domposable.ui.width
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** A consumer feature built from document and data controls on native, HTML and Wasm. */
@Composable
@DomposableScreen(DemoRoute.ProductComponents::class)
internal fun ProductComponentLab() {
    var email by rememberSaveable { mutableStateOf("") }
    var shortNote by rememberSaveable { mutableStateOf("") }
    var longNote by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("No preferences saved") }
    var delivery by rememberSaveable { mutableStateOf("standard") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedPoint by remember { mutableStateOf<Int?>(null) }
    var rowMessage by remember { mutableStateOf("Choose an order") }
    var scrollMessage by remember { mutableStateOf("Scroll idle") }
    var scrollMounted by remember { mutableStateOf(true) }
    var alternateScrollState by remember { mutableStateOf(false) }
    var scrollContentVisible by remember { mutableStateOf(true) }
    val initialScrollState = rememberDomposableScrollState(initialValue = 80)
    val replacementScrollState = rememberDomposableScrollState(initialValue = 130)
    val scrollState = if (alternateScrollState) replacementScrollState else initialScrollState
    val horizontalScrollState = rememberDomposableScrollState()
    val scrollScope = rememberCoroutineScope()
    var activeScroll by remember { mutableStateOf<Job?>(null) }
    val series =
        remember {
            listOf(
                DomposableChartSeries(
                    "orders",
                    "Orders",
                    listOf(
                        DomposableChartValue("Mon", 12f),
                        DomposableChartValue("Tue", 18f),
                        DomposableChartValue("Wed", 15f),
                    ),
                ),
            )
        }

    fun animateTo(position: Int) {
        activeScroll?.cancel()
        activeScroll =
            scrollScope.launch {
                scrollMessage = "Scroll animating"
                try {
                    scrollState.animateScrollTo(position)
                    scrollMessage = "Scroll animation completed"
                } catch (_: CancellationException) {
                    scrollMessage = "Scroll animation cancelled"
                }
            }
    }
    DomposableColumn(DomposableModifier.fillMaxWidth().padding(16.dp).verticalScroll(), gap = 16.dp) {
        DomposableText("Documents and data lab", style = DomposableTextStyle.Title, headingLevel = 1)
        DomposableForm(onSubmit = { message = "Saved preferences for $email" }, label = "Delivery preferences") {
            DomposableColumn(gap = 12.dp) {
                DomposableTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Contact email",
                    purpose = DomposableInputPurpose.Email,
                    placeholder = "you@example.com",
                    helperText = "Used only in this local demo",
                    imeAction = DomposableImeAction.Done,
                    modifier = DomposableModifier.testId("product-email"),
                )
                DomposableSegmentedButtons(
                    options =
                        listOf(
                            DomposableChoiceOption("standard", "Standard"),
                            DomposableChoiceOption("express", "Express"),
                            DomposableChoiceOption("same-day", "Same day", enabled = false),
                        ),
                    selectedValue = delivery,
                    onSelectionChange = { delivery = it },
                    label = "Delivery speed",
                )
                val submit = rememberDomposableFormSubmit()
                DomposableButton("Save preferences", submit, enabled = email.isNotBlank())
            }
        }
        DomposableText(message, modifier = DomposableModifier.testId("product-form-status"))
        DomposableDisclosure("Shipping details", expanded, { expanded = !expanded }) {
            DomposableRichHtml(
                "<p><strong>Free shipping</strong> on orders over 50. " +
                    "<a href=\"https://example.com/shipping\">Shipping policy</a>.</p>",
                selectable = true,
                modifier = DomposableModifier.testId("product-rich-html"),
            )
        }
        DomposableLineChart(
            series = series,
            title = "Orders this week",
            description = "Select a day with touch, pointer or arrow keys",
            onSelectedIndexChange = { selectedPoint = it },
            modifier = DomposableModifier.testId("product-line-chart"),
        )
        DomposableText(
            selectedPoint?.let { "Selected day ${it + 1}" } ?: "No selected day",
            modifier = DomposableModifier.testId("product-chart-status"),
        )
        DomposableValueChart(
            title = "Order totals",
            description = "Three days of sample data",
            dataTableLabel = "Daily values",
            series = series,
            formatValue = { it.toInt().toString() },
        )
        DomposableDataTable(
            columns = listOf(DomposableDataTableColumn("order", "Order"), DomposableDataTableColumn("total", "Total")),
            rows = listOf(DomposableDataTableRow("A-101"), DomposableDataTableRow("A-102")),
            rowHeaderColumnKey = "order",
            onRowActivate = { rowMessage = "Opened order $it" },
            modifier = DomposableModifier.testId("product-orders"),
        ) { row, column ->
            DomposableText(
                if (column.key == "order") {
                    row.key
                } else if (row.key == "A-101") {
                    "42"
                } else {
                    "64"
                },
            )
        }
        DomposableText(rowMessage, modifier = DomposableModifier.testId("product-row-status"))

        DomposableText("Scroll behavior lab", style = DomposableTextStyle.Title)
        DomposableFlowRow(gap = 8.dp) {
            DomposableButton("Animate down", onClick = { animateTo(520) })
            DomposableButton("Animate up", onClick = { animateTo(0) })
            DomposableButton("Scroll to top", onClick = {
                scrollScope.launch { scrollState.scrollTo(0) }
            })
            DomposableButton("Cancel scroll", onClick = {
                activeScroll?.cancel()
                scrollMessage = "Scroll animation cancelled"
            })
            DomposableButton("Scroll right", onClick = {
                scrollScope.launch { horizontalScrollState.scrollTo(180) }
            })
            DomposableButton("Remove scroll container", onClick = { scrollMounted = false })
            DomposableButton("Replace scroll state", onClick = { alternateScrollState = !alternateScrollState })
            DomposableButton("Reset empty scroll area", onClick = {
                scrollContentVisible = false
                alternateScrollState = !alternateScrollState
            })
            DomposableButton("Load scroll content", onClick = { scrollContentVisible = true })
        }
        DomposableText(scrollMessage, modifier = DomposableModifier.testId("product-scroll-status"))
        DomposableText(
            "Scroll position: ${scrollState.value}",
            modifier = DomposableModifier.testId("product-scroll-position"),
        )
        if (scrollMounted) {
            DomposableColumn(
                modifier =
                    DomposableModifier
                        .width(240.dp)
                        .height(120.dp)
                        .verticalScroll(scrollState)
                        .testId("product-scroll-window"),
                gap = 8.dp,
            ) {
                if (scrollContentVisible) {
                    for (index in 1..24) DomposableText("Scrollable item $index")
                }
            }
        }
        DomposableText("Horizontal scroll behavior")
        DomposableRow(
            modifier =
                DomposableModifier
                    .width(240.dp)
                    .height(64.dp)
                    .horizontalScroll(horizontalScrollState)
                    .testId("product-horizontal-scroll-window"),
            gap = 8.dp,
        ) {
            for (index in 1..10) {
                DomposableText("Horizontal $index", modifier = DomposableModifier.width(100.dp))
            }
        }
        DomposableText(
            "Horizontal scroll position: ${horizontalScrollState.value}",
            modifier = DomposableModifier.testId("product-horizontal-scroll-position"),
        )
        DomposableText("Multiline input bounds", style = DomposableTextStyle.Title)
        DomposableTextField(
            value = shortNote,
            onValueChange = { shortNote = it },
            label = "One-row note",
            multiline = true,
            minLines = 1,
            maxLines = 1,
        )
        DomposableTextField(
            value = longNote,
            onValueChange = { longNote = it },
            label = "Longer note",
            multiline = true,
            minLines = 3,
            maxLines = 4,
        )
    }
}
