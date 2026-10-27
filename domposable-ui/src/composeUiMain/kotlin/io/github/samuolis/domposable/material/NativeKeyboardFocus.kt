package io.github.samuolis.domposable.material

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.unit.dp

private const val FOCUS_RING_DP = 3f

/** Keep keyboard focus visible on the whole hit target, matching the HTML controls. */
@Composable
internal fun Modifier.materialKeyboardFocusRing(): Modifier {
    var focused by remember { mutableStateOf(false) }
    val keyboard = LocalInputModeManager.current.inputMode == InputMode.Keyboard
    val color = MaterialTheme.colorScheme.primary
    return Modifier.onFocusChanged { focused = it.hasFocus }.then(this).drawBehind {
        if (focused && keyboard) {
            val stroke = FOCUS_RING_DP.dp.toPx()
            val outset = stroke + stroke / 2f
            drawRect(
                color,
                topLeft = Offset(-outset, -outset),
                size = Size(size.width + outset * 2f, size.height + outset * 2f),
                style = Stroke(stroke),
            )
        }
    }
}
