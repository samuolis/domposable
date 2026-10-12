package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import io.github.samuolis.domposable.ui.DomposableForm
import io.github.samuolis.domposable.ui.DomposableLineChart
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRichHtml
import io.github.samuolis.domposable.ui.DomposableSegmentedButtons
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableValueChart
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.rememberDomposableFormSubmit
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll

/** A consumer feature built from document and data controls on native, HTML and Wasm. */
@Composable
@DomposableScreen(DemoRoute.ProductComponents::class)
internal fun ProductComponentLab() {
    var email by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("No preferences saved") }
    var delivery by rememberSaveable { mutableStateOf("standard") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedPoint by remember { mutableStateOf<Int?>(null) }
    var rowMessage by remember { mutableStateOf("Choose an order") }
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
    }
}
