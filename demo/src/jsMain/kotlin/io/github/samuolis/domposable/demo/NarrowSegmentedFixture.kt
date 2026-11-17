package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableSegmentedButtons
import io.github.samuolis.domposable.ui.fillMaxWidth
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text

@Composable
internal fun NarrowSegmentedFixture() {
    var selected by remember { mutableStateOf(0) }
    DomposableMaterialTheme {
        Div(attrs = {
            style {
                property("font-family", "Arial,sans-serif")
                property("padding", "16px")
            }
        }) {
            H1 { Text("Įskaitomi rezultatai") }
            DomposableSegmentedButtons(
                labels = listOf("Fiksuotas sezonas", "Paskutiniai 12 mėnesių", "Visas laikotarpis"),
                selectedIndex = selected,
                onSelect = { selected = it },
                label = "Įskaitomi rezultatai",
                modifier = DomposableModifier.fillMaxWidth(),
                equalWidth = true,
            )
        }
    }
}
