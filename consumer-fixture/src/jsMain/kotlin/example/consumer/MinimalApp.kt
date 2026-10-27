package example.consumer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableText
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        DomposableMaterialTheme {
            var clicks by remember { mutableIntStateOf(0) }
            DomposableText("Clicks: $clicks")
            AppButton("Add") { clicks++ }
        }
    }
}

@Composable
private fun AppButton(
    label: String,
    onClick: () -> Unit,
) {
    DomposableButton(label, onClick)
}
