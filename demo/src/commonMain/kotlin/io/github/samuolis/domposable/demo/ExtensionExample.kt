package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableModifierElement
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.background
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.rotate
import io.github.samuolis.domposable.ui.scale
import io.github.samuolis.domposable.ui.shadow
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll

/** App-owned element: the library does not know about this decoration. */
internal data class AppAccentFrame(
    val color: DomposableColor,
) : DomposableModifierElement

/** A separate element exercises the attribute adapter alongside content decoration. */
internal data object AppContentMarker : DomposableModifierElement

internal fun DomposableModifier.appAccentFrame(color: DomposableColor): DomposableModifier = then(AppAccentFrame(color))

@Composable
internal expect fun ProvideAppModifierRenderers(content: @Composable () -> Unit)

@Composable
internal expect fun AppCounterLabel(count: Int)

@Composable
@DomposableScreen(DemoRoute.Extensions::class)
internal fun ModifierExtensionExample() {
    var alternate by remember { mutableStateOf(false) }
    DomposableColumn(DomposableModifier.fillMaxWidth().padding(16.dp).verticalScroll(), gap = 16.dp) {
        DomposableText("App-owned components and modifiers", style = DomposableTextStyle.Title, headingLevel = 1)
        DomposableText("Shared feature code. Custom native drawing and HTML decoration. No library fork.")
        DomposableButton("Change frame color", { alternate = !alternate })
        ProvideAppModifierRenderers {
            // Padding stays before and after the custom drawing operation on both renderers.
            DomposableColumn(
                DomposableModifier
                    .padding(8.dp)
                    .appAccentFrame(DomposableColor(if (alternate) 0xFF946749 else 0xFF4059AD))
                    .padding(12.dp)
                    .then(AppContentMarker)
                    .testId("app-extension-content"),
                gap = 8.dp,
            ) {
                AppCounterContent()
            }
        }
        DomposableText("Portable graphics", headingLevel = 2)
        DomposableBox(
            DomposableModifier
                .size(160.dp, 48.dp)
                .shadow(4.dp, DomposableShape.Rounded(8.dp))
                .rotate(5f)
                .scale(0.9f)
                .background(DomposableColor(0xFFECEFF8), DomposableShape.Rounded(8.dp))
                .testId("modifier-graphics"),
        ) { DomposableText("Shared transforms") }
    }
}

@Composable
private fun AppCounterContent() {
    var count by remember { mutableStateOf(0) }
    var draft by remember { mutableStateOf("") }
    AppCounterLabel(count)
    DomposableTextField(draft, {
        draft = it
    }, "Extension draft", modifier = DomposableModifier.testId("app-extension-draft"))
    DomposableButton("Increment custom counter", { count++ })
}
