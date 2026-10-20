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
    /** Optional whole-control opacity while disabled; loading keeps enabled styling. */
    val disabledOpacity: Float? = null,
    val interaction: DomposableInteractionAppearance? = null,
    /** Logical horizontal alignment of the content inside a wider control. */
    val contentAlignment: DomposableAlignment = DomposableAlignment.Center,
    /** Optional track behind the loading indicator; null keeps each renderer's default. */
    val indicatorTrackColor: DomposableColor? = null,
) {
    init {
        require(pressedScale.isFinite() && pressedScale > 0f && pressedScale <= 1f)
        require(hoveredScale.isFinite() && hoveredScale > 0f)
        require(disabledOpacity == null || (disabledOpacity.isFinite() && disabledOpacity in 0f..1f))
    }
}
