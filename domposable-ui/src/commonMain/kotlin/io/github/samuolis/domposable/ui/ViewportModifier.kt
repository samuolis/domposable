package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.dp

/** Browser chrome changes dynamic height; small height keeps document layouts stable while scrolling. */
enum class DomposableViewportUnit { Dynamic, Small }

enum class DomposableViewportHeightMode { Fixed, Minimum, Maximum }

/**
 * Bounds content using the host viewport, rather than its parent's available height.
 * [subtract] reserves app-owned chrome. Native uses the current window size for both units;
 * HTML uses `dvh`/`svh`. Neither includes additional safe-area padding automatically.
 */
fun DomposableModifier.viewportHeight(
    fraction: Float = 1f,
    subtract: DomposableLength = 0.dp,
    unit: DomposableViewportUnit = DomposableViewportUnit.Dynamic,
): DomposableModifier = viewportHeightOperation(DomposableViewportHeightMode.Fixed, fraction, subtract, unit)

/** A content-sized document that fills at least the chosen viewport height. */
fun DomposableModifier.minViewportHeight(
    fraction: Float = 1f,
    subtract: DomposableLength = 0.dp,
    unit: DomposableViewportUnit = DomposableViewportUnit.Dynamic,
): DomposableModifier = viewportHeightOperation(DomposableViewportHeightMode.Minimum, fraction, subtract, unit)

/** Caps a scrollable region without expanding shorter content. */
fun DomposableModifier.maxViewportHeight(
    fraction: Float = 1f,
    subtract: DomposableLength = 0.dp,
    unit: DomposableViewportUnit = DomposableViewportUnit.Dynamic,
): DomposableModifier = viewportHeightOperation(DomposableViewportHeightMode.Maximum, fraction, subtract, unit)

private fun DomposableModifier.viewportHeightOperation(
    mode: DomposableViewportHeightMode,
    fraction: Float,
    subtract: DomposableLength,
    unit: DomposableViewportUnit,
): DomposableModifier = then(DomposableModifierOperation.ViewportHeight(mode, fraction, subtract, unit))
