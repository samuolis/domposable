package io.github.samuolis.domposable.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableSize
import io.github.samuolis.domposable.foundation.DomposableOrientation

/** Native-only interop. Compose UI modifiers stay outside shared feature signatures. */
@Composable
fun DomposableModifier.toComposeModifier(): Modifier {
    var result: Modifier = Modifier
    for (operation in operations) result = when (operation) {
        is DomposableModifierOperation.Padding -> result.padding(
            start = operation.insets.start.value.dp, top = operation.insets.top.value.dp,
            end = operation.insets.end.value.dp, bottom = operation.insets.bottom.value.dp,
        )
        is DomposableModifierOperation.Width -> result.width(operation.length.value.dp)
        is DomposableModifierOperation.Height -> result.height(operation.length.value.dp)
        is DomposableModifierOperation.MaxWidth -> result.widthIn(max = operation.length.value.dp)
        DomposableModifierOperation.FillWidth -> result.fillMaxWidth()
        DomposableModifierOperation.FillHeight -> result.fillMaxHeight()
        is DomposableModifierOperation.Background -> result.background(Color(operation.color.argb))
        is DomposableModifierOperation.Radius -> result.clip(RoundedCornerShape(operation.length.value.dp))
        is DomposableModifierOperation.Opacity -> result.alpha(operation.value)
        is DomposableModifierOperation.Click -> result.clickable(onClickLabel = operation.label, role = Role.Button, onClick = operation.onClick)
        is DomposableModifierOperation.Description -> result.semantics { contentDescription = operation.text }
        is DomposableModifierOperation.TestId -> result.testTag(operation.value)
        is DomposableModifierOperation.Scroll -> if (operation.orientation == DomposableOrientation.Vertical) {
            result.verticalScroll(rememberScrollState())
        } else result.horizontalScroll(rememberScrollState())
    }
    return result
}

@Composable
actual fun DomposableMeasure(modifier: DomposableModifier, content: @Composable (DomposableSize) -> Unit) {
    var size by remember { mutableStateOf(DomposableSize(0f, 0f)) }
    val density = LocalDensity.current.density
    Box(modifier.toComposeModifier().fillMaxWidth().onSizeChanged {
        size = DomposableSize(it.width / density, it.height / density)
    }) { content(size) }
}
