// The ordered modifier DSL keeps its related fluent operations together.
@file:Suppress("TooManyFunctions")

package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Immutable
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableLayoutAxis
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableLinearGradient
import io.github.samuolis.domposable.foundation.DomposableOffset
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableShape

/** Ordered portable operations. Platform adapters preserve order, including nested padding. */
@Immutable
interface DomposableModifier {
    /** Legacy built-in operation view. Custom elements are available through [elements]. */
    val operations: List<DomposableModifierOperation>

    /** Ordered built-in and application-owned operations. */
    val elements: List<DomposableModifierElement>
        get() = operations

    fun then(other: DomposableModifier): DomposableModifier = Chain(elements + other.elements)

    fun then(element: DomposableModifierElement): DomposableModifier = Chain(elements + element)

    companion object : DomposableModifier {
        override val operations: List<DomposableModifierOperation> = emptyList()
    }
}

private data class Chain(
    override val elements: List<DomposableModifierElement>,
) : DomposableModifier {
    override val operations: List<DomposableModifierOperation>
        get() = elements.filterIsInstance<DomposableModifierOperation>()
}

/** Open marker for app-owned portable modifier behavior. Each renderer must handle it explicitly. */
@Immutable
interface DomposableModifierElement

sealed interface DomposableModifierOperation : DomposableModifierElement {
    data class Padding(
        val insets: DomposableInsets,
    ) : DomposableModifierOperation

    data class Width(
        val length: DomposableLength,
    ) : DomposableModifierOperation

    data class Height(
        val length: DomposableLength,
    ) : DomposableModifierOperation

    data class MaxWidth(
        val length: DomposableLength,
    ) : DomposableModifierOperation

    data class FillWidthFraction(
        val fraction: Float,
        val minWidth: DomposableLength? = null,
        val maxWidth: DomposableLength? = null,
    ) : DomposableModifierOperation {
        init {
            require(fraction.isFinite() && fraction > 0f && fraction <= 1f) {
                "Width fraction must be finite and greater than 0 and at most 1"
            }
            require(minWidth == null || maxWidth == null || minWidth.value <= maxWidth.value) {
                "Minimum width must not exceed maximum width"
            }
        }
    }

    data class MinWidth(
        val length: DomposableLength,
    ) : DomposableModifierOperation

    data class MinHeight(
        val length: DomposableLength,
    ) : DomposableModifierOperation

    data class MaxHeight(
        val length: DomposableLength,
    ) : DomposableModifierOperation

    data object FillWidth : DomposableModifierOperation

    data object PointerPassthrough : DomposableModifierOperation

    data object FillHeight : DomposableModifierOperation

    data class Background(
        val color: DomposableColor,
    ) : DomposableModifierOperation

    data class Radius(
        val length: DomposableLength,
    ) : DomposableModifierOperation

    data class ShapeClip(
        val shape: DomposableShape,
    ) : DomposableModifierOperation

    data class ShapeBackground(
        val color: DomposableColor,
        val shape: DomposableShape,
    ) : DomposableModifierOperation

    data class GradientBackground(
        val gradient: DomposableLinearGradient,
        val shape: DomposableShape,
    ) : DomposableModifierOperation

    data class Border(
        val width: DomposableLength,
        val color: DomposableColor,
        val shape: DomposableShape,
    ) : DomposableModifierOperation

    data class EdgeBorder(
        val width: DomposableLength,
        val color: DomposableColor,
        val edge: DomposableBorderEdge,
    ) : DomposableModifierOperation

    data class Region(
        val label: String,
    ) : DomposableModifierOperation {
        init {
            require(label.isNotBlank()) { "Region labels must not be blank" }
        }
    }

    data class AspectRatio(
        val ratio: Float,
    ) : DomposableModifierOperation

    data class ZIndex(
        val value: Float,
    ) : DomposableModifierOperation

    data class Offset(
        val value: DomposableOffset,
    ) : DomposableModifierOperation

    data class WrapContentSize(
        val horizontal: DomposableAlignment,
        val vertical: DomposableAlignment,
    ) : DomposableModifierOperation

    data class Opacity(
        val value: Float,
    ) : DomposableModifierOperation

    data class Scale(
        val x: Float,
        val y: Float,
    ) : DomposableModifierOperation {
        init {
            require(x.isFinite() && y.isFinite()) { "Scale must be finite" }
        }
    }

    data class Rotation(
        val degrees: Float,
    ) : DomposableModifierOperation {
        init {
            require(degrees.isFinite()) { "Rotation must be finite" }
        }
    }

    data class Shadow(
        val elevation: DomposableLength,
        val shape: DomposableShape,
    ) : DomposableModifierOperation

    data class Click(
        val onClick: () -> Unit,
        val label: String,
    ) : DomposableModifierOperation

    data class InteractiveClick(
        val onClick: () -> Unit,
        val role: DomposableClickRole,
        val enabled: Boolean,
        val label: String?,
    ) : DomposableModifierOperation

    data class Description(
        val text: String,
    ) : DomposableModifierOperation

    data class TestId(
        val value: String,
    ) : DomposableModifierOperation

    data class Scroll(
        val orientation: DomposableOrientation,
    ) : DomposableModifierOperation

    data class StatefulScroll(
        val orientation: DomposableOrientation,
        val state: DomposableScrollState,
    ) : DomposableModifierOperation

    data class Weight(
        val weight: Float,
        val fill: Boolean,
    ) : DomposableModifierOperation

    data class AxisAlignment(
        val axis: DomposableLayoutAxis,
        val alignment: DomposableAlignment,
    ) : DomposableModifierOperation

    data class BoxAlignment(
        val horizontal: DomposableAlignment,
        val vertical: DomposableAlignment,
    ) : DomposableModifierOperation
}

enum class DomposableClickRole { Button, Link, Checkbox, RadioButton, Switch, Image }

enum class DomposableBorderEdge { Top, End, Bottom, Start }

private fun DomposableModifier.append(operation: DomposableModifierOperation): DomposableModifier = then(operation)

fun DomposableModifier.padding(all: DomposableLength): DomposableModifier = padding(DomposableInsets(all))

fun DomposableModifier.padding(insets: DomposableInsets): DomposableModifier =
    append(DomposableModifierOperation.Padding(insets))

fun DomposableModifier.width(value: DomposableLength): DomposableModifier =
    append(DomposableModifierOperation.Width(value))

fun DomposableModifier.height(value: DomposableLength): DomposableModifier =
    append(DomposableModifierOperation.Height(value))

fun DomposableModifier.size(value: DomposableLength): DomposableModifier = width(value).height(value)

fun DomposableModifier.maxWidth(value: DomposableLength): DomposableModifier = constrainContentWidth(value)

fun DomposableModifier.widthIn(
    min: DomposableLength? = null,
    max: DomposableLength? = null,
): DomposableModifier {
    require(min == null || max == null || min.value <= max.value) { "Minimum width must not exceed maximum width" }
    var result = this
    min?.let { result = result.minWidth(it) }
    max?.let { result = result.maxWidth(it) }
    return result
}

fun DomposableModifier.heightIn(
    min: DomposableLength? = null,
    max: DomposableLength? = null,
): DomposableModifier {
    require(min == null || max == null || min.value <= max.value) { "Minimum height must not exceed maximum height" }
    var result = this
    min?.let { result = result.minHeight(it) }
    max?.let { result = result.maxHeight(it) }
    return result
}

fun DomposableModifier.constrainContentWidth(value: DomposableLength): DomposableModifier =
    append(DomposableModifierOperation.MaxWidth(value))

fun DomposableModifier.fillMaxWidth(
    fraction: Float,
    min: DomposableLength? = null,
    max: DomposableLength? = null,
): DomposableModifier = append(DomposableModifierOperation.FillWidthFraction(fraction, min, max))

fun DomposableModifier.minWidth(value: DomposableLength): DomposableModifier =
    append(DomposableModifierOperation.MinWidth(value))

fun DomposableModifier.minHeight(value: DomposableLength): DomposableModifier =
    append(DomposableModifierOperation.MinHeight(value))

fun DomposableModifier.maxHeight(value: DomposableLength): DomposableModifier =
    append(DomposableModifierOperation.MaxHeight(value))

fun DomposableModifier.fillMaxWidth(): DomposableModifier = append(DomposableModifierOperation.FillWidth)

fun DomposableModifier.pointerPassthrough(): DomposableModifier = append(DomposableModifierOperation.PointerPassthrough)

fun DomposableModifier.fillMaxHeight(): DomposableModifier = append(DomposableModifierOperation.FillHeight)

fun DomposableModifier.background(value: DomposableColor): DomposableModifier =
    append(DomposableModifierOperation.Background(value))

fun DomposableModifier.background(
    value: DomposableColor,
    shape: DomposableShape,
): DomposableModifier = append(DomposableModifierOperation.ShapeBackground(value, shape))

fun DomposableModifier.background(
    gradient: DomposableLinearGradient,
    shape: DomposableShape = DomposableShape.Rectangle,
): DomposableModifier = append(DomposableModifierOperation.GradientBackground(gradient, shape))

fun DomposableModifier.clip(radius: DomposableLength): DomposableModifier =
    append(DomposableModifierOperation.Radius(radius))

fun DomposableModifier.clip(shape: DomposableShape): DomposableModifier =
    append(DomposableModifierOperation.ShapeClip(shape))

fun DomposableModifier.border(
    width: DomposableLength,
    color: DomposableColor,
    shape: DomposableShape = DomposableShape.Rectangle,
): DomposableModifier = append(DomposableModifierOperation.Border(width, color, shape))

fun DomposableModifier.border(
    width: DomposableLength,
    color: DomposableColor,
    edge: DomposableBorderEdge,
): DomposableModifier = append(DomposableModifierOperation.EdgeBorder(width, color, edge))

fun DomposableModifier.region(label: String): DomposableModifier = append(DomposableModifierOperation.Region(label))

fun DomposableModifier.aspectRatio(ratio: Float): DomposableModifier {
    require(ratio.isFinite() && ratio > 0f) { "Aspect ratio must be finite and positive" }
    return append(DomposableModifierOperation.AspectRatio(ratio))
}

fun DomposableModifier.zIndex(value: Float): DomposableModifier {
    require(value.isFinite()) { "Z index must be finite" }
    return append(DomposableModifierOperation.ZIndex(value))
}

fun DomposableModifier.offset(
    x: Float,
    y: Float,
): DomposableModifier = append(DomposableModifierOperation.Offset(DomposableOffset(x, y)))

fun DomposableModifier.wrapContentSize(
    horizontal: DomposableAlignment = DomposableAlignment.Center,
    vertical: DomposableAlignment = DomposableAlignment.Center,
): DomposableModifier = append(DomposableModifierOperation.WrapContentSize(horizontal, vertical))

fun DomposableModifier.alpha(value: Float): DomposableModifier {
    require(value.isFinite() && value in 0f..1f)
    return append(DomposableModifierOperation.Opacity(value))
}

/** Visual transform around the center; measured layout size is unchanged. Negative values mirror content. */
fun DomposableModifier.scale(
    x: Float,
    y: Float = x,
): DomposableModifier = append(DomposableModifierOperation.Scale(x, y))

/** Clockwise visual rotation in degrees, without changing measured layout size. */
fun DomposableModifier.rotate(degrees: Float): DomposableModifier =
    append(DomposableModifierOperation.Rotation(degrees))

/** Native elevation or a CSS shadow. Does not clip content; use clip separately when needed. */
fun DomposableModifier.shadow(
    elevation: DomposableLength,
    shape: DomposableShape = DomposableShape.Rectangle,
): DomposableModifier = append(DomposableModifierOperation.Shadow(elevation, shape))

fun DomposableModifier.clickable(
    label: String,
    onClick: () -> Unit,
): DomposableModifier = append(DomposableModifierOperation.Click(onClick, label))

fun DomposableModifier.clickable(
    onClick: () -> Unit,
    role: DomposableClickRole = DomposableClickRole.Button,
    enabled: Boolean = true,
    onClickLabel: String? = null,
): DomposableModifier = append(DomposableModifierOperation.InteractiveClick(onClick, role, enabled, onClickLabel))

fun DomposableModifier.contentDescription(value: String): DomposableModifier =
    append(DomposableModifierOperation.Description(value))

fun DomposableModifier.testId(value: String): DomposableModifier = append(DomposableModifierOperation.TestId(value))

fun DomposableModifier.verticalScroll(): DomposableModifier =
    append(DomposableModifierOperation.Scroll(DomposableOrientation.Vertical))

fun DomposableModifier.horizontalScroll(): DomposableModifier =
    append(DomposableModifierOperation.Scroll(DomposableOrientation.Horizontal))

fun DomposableModifier.verticalScroll(state: DomposableScrollState): DomposableModifier =
    append(DomposableModifierOperation.StatefulScroll(DomposableOrientation.Vertical, state))

fun DomposableModifier.horizontalScroll(state: DomposableScrollState): DomposableModifier =
    append(DomposableModifierOperation.StatefulScroll(DomposableOrientation.Horizontal, state))

/** Must be consumed by a row or column child renderer. */
fun DomposableModifier.weight(
    weight: Float,
    fill: Boolean = true,
): DomposableModifier {
    require(weight.isFinite() && weight > 0f) { "Weight must be finite and positive" }
    return append(DomposableModifierOperation.Weight(weight, fill))
}

fun DomposableModifier.align(
    axis: DomposableLayoutAxis,
    alignment: DomposableAlignment,
): DomposableModifier = append(DomposableModifierOperation.AxisAlignment(axis, alignment))

fun DomposableModifier.boxAlignment(
    horizontal: DomposableAlignment = DomposableAlignment.Center,
    vertical: DomposableAlignment = DomposableAlignment.Center,
): DomposableModifier = append(DomposableModifierOperation.BoxAlignment(horizontal, vertical))

/** Insets are logical: start/end follow the platform's layout direction. */
fun DomposableModifier.padding(
    horizontal: DomposableLength,
    vertical: DomposableLength,
): DomposableModifier =
    padding(DomposableInsets(start = horizontal, end = horizontal, top = vertical, bottom = vertical))

fun DomposableModifier.size(
    width: DomposableLength,
    height: DomposableLength,
): DomposableModifier = width(width).height(height)

fun DomposableModifier.fillMaxSize(): DomposableModifier = fillMaxWidth().fillMaxHeight()
