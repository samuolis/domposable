package io.github.samuolis.domposable.foundation

import androidx.compose.runtime.Immutable

/** Optional state styling shared by buttons, icon buttons, and button-like links. */
@Immutable
data class DomposableInteractionAppearance(
    val hoveredContainer: DomposableColor? = null,
    val hoveredContent: DomposableColor? = null,
    val pressedContainer: DomposableColor? = null,
    val pressedContent: DomposableColor? = null,
    val stateLayerOpacity: DomposableStateLayerOpacity? = null,
    val focusRingColor: DomposableColor? = null,
    val focusRingWidth: DomposableLength? = null,
    val focusRingOffset: DomposableLength? = null,
)
