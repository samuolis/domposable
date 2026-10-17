package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import io.github.samuolis.domposable.demo.generated.resources.Res
import kotlinx.browser.document
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
internal actual fun MaterialComparisonFont(content: @Composable () -> Unit) {
    DisposableEffect(Unit) {
        val style = document.createElement("style")
        style.textContent =
            listOf("regular" to 400, "medium" to 500).joinToString("\n") { (name, weight) ->
                val uri = Res.getUri("font/comparison_$name.ttf")
                "@font-face{font-family:DomposableComparison;font-weight:$weight;src:url('$uri') format('truetype')}"
            }
        document.head!!.appendChild(style)
        onDispose { style.remove() }
    }
    content()
}
