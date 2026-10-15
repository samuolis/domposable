package io.github.samuolis.domposable.foundation

import androidx.compose.runtime.Immutable

/** Component styling belongs here; layout and positioning remain modifier operations. */
@Immutable
data class DomposableButtonAppearance(
    val container: DomposableColor? = null,
    val content: DomposableColor? = null,
    val disabledContainer: DomposableColor? = null,
    val disabledContent: DomposableColor? = null,
    val border: DomposableColor? = null,
    val disabledBorder: DomposableColor? = null,
    val borderWidth: DomposableLength? = null,
    val shape: DomposableShape? = null,
    val minHeight: DomposableLength? = null,
    val minWidth: DomposableLength? = null,
    val contentPadding: DomposableInsets? = null,
    val contentSpacing: DomposableLength = 0.dp,
    val textAppearance: DomposableTextAppearance? = null,
    val indicatorSize: DomposableLength? = null,
    val indicatorStrokeWidth: DomposableLength? = null,
    val pressedScale: Float = 1f,
    val hoveredScale: Float = 1f,
) {
    init {
        require(pressedScale.isFinite() && pressedScale > 0f && pressedScale <= 1f)
        require(hoveredScale.isFinite() && hoveredScale > 0f)
    }
}
