package io.github.samuolis.domposable.demo

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.ui.DomposableModifierElement
import io.github.samuolis.domposable.ui.LocalDomposableNativeModifierRenderer

@Composable
internal actual fun ProvideAppModifierRenderers(content: @Composable () -> Unit) {
    val parent = LocalDomposableNativeModifierRenderer.current
    val renderer =
        remember(parent) {
            { element: DomposableModifierElement ->
                when (element) {
                    is AppAccentFrame -> {
                        Modifier.drawBehind {
                            val stroke = 3.dp.toPx()
                            drawRect(
                                Color(element.color.argb),
                                topLeft = Offset(stroke / 2, stroke / 2),
                                size =
                                    Size(
                                        (size.width - stroke).coerceAtLeast(0f),
                                        (size.height - stroke).coerceAtLeast(0f),
                                    ),
                                style = Stroke(stroke),
                            )
                        }
                    }

                    AppContentMarker -> {
                        Modifier
                    }

                    else -> {
                        parent?.invoke(element)
                    }
                }
            }
        }
    CompositionLocalProvider(LocalDomposableNativeModifierRenderer provides renderer, content = content)
}

@Composable
internal actual fun AppCounterLabel(count: Int) {
    BasicText(
        "Custom counter: $count",
        Modifier.testTag("app-counter-label"),
        style = TextStyle(color = Color(0xFF4059AD)),
    )
}
