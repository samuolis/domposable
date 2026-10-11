package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableFloatingActionButton
import io.github.samuolis.domposable.ui.DomposableIcon
import io.github.samuolis.domposable.ui.DomposableIconButton
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRangeSlider
import io.github.samuolis.domposable.ui.DomposableSegmentedButtons
import io.github.samuolis.domposable.ui.DomposableSelect
import io.github.samuolis.domposable.ui.DomposableSelectOption
import io.github.samuolis.domposable.ui.DomposableSnackbar
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTooltip
import io.github.samuolis.domposable.ui.DomposableTopAppBar
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll

/** A runnable shared feature. Every control is rendered by the installed platform provider. */
@Composable
@DomposableScreen(DemoRoute.ExtendedCatalog::class)
fun ExtendedComponentCatalog() {
    var delivery by rememberSaveable { mutableStateOf("standard") }
    var segment by rememberSaveable { mutableIntStateOf(0) }
    var minimum by rememberSaveable { mutableFloatStateOf(20f) }
    var maximum by rememberSaveable { mutableFloatStateOf(80f) }
    var notice by remember { mutableStateOf<String?>("Your preferences are saved") }
    DomposableColumn(DomposableModifier.fillMaxWidth().padding(16.dp).verticalScroll(), gap = 12.dp) {
        DomposableText("More shared components", style = DomposableTextStyle.Title, headingLevel = 2)
        DomposableTopAppBar(
            "Delivery preferences",
            navigationIcon = {
                DomposableIconButton({ notice = "Back action received" }, "Back") { DomposableIcon("‹", null) }
            },
            actions = {
                DomposableTooltip("Your preferences stay on this device") {
                    DomposableIconButton(
                        { notice = "This is a local demo" },
                        "About preferences",
                    ) { DomposableIcon("?", null) }
                }
            },
        )
        DomposableSelect(
            delivery,
            { delivery = it },
            "Delivery",
            listOf(
                DomposableSelectOption("standard", "Standard"),
                DomposableSelectOption("express", "Express"),
                DomposableSelectOption("unavailable", "Same day (unavailable)", enabled = false),
            ),
            modifier = DomposableModifier.testId("delivery-select"),
        )
        DomposableSegmentedButtons(listOf("Home", "Office"), segment, { segment = it }, "Delivery address")
        DomposableText("Deliver ${delivery.uppercase()} to ${if (segment == 0) "Home" else "Office"}")
        DomposableRangeSlider(
            minimum..maximum,
            {
                minimum = it.start
                maximum = it.endInclusive
            },
            "Minimum price",
            "Maximum price",
            range = 0f..100f,
            steps = 9,
        )
        DomposableText("Price range: ${minimum.toInt()}–${maximum.toInt()}")
        DomposableFloatingActionButton(
            { notice = "Preferences saved" },
            "Save preferences",
        ) { DomposableIcon("✓", null) }
        notice?.let { message ->
            DomposableSnackbar(message, actionLabel = "Reset", onAction = {
                delivery = "standard"
                segment = 0
                minimum = 20f
                maximum = 80f
                notice = "Preferences reset"
            }, onDismiss = { notice = null })
        }
    }
}
