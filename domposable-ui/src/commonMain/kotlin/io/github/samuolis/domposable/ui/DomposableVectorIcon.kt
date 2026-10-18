package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Immutable

/**
 * A platform-neutral filled SVG path for an icon. The path is rendered using the icon's
 * inherited content color and the component's requested size.
 */
@Immutable
data class DomposableVectorIcon(
    val pathData: String,
    val viewportWidth: Float = 24f,
    val viewportHeight: Float = 24f,
    val evenOdd: Boolean = false,
) {
    init {
        require(pathData.isNotBlank()) { "Vector icon path data must not be blank" }
        require(
            viewportWidth.isFinite() && viewportWidth > 0f,
        ) { "Vector icon viewport width must be finite and positive" }
        require(
            viewportHeight.isFinite() && viewportHeight > 0f,
        ) { "Vector icon viewport height must be finite and positive" }
    }
}
