// The ordered modifier DSL keeps its related fluent operations together.
@file:Suppress("TooManyFunctions")

package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Immutable
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableOrientation

/** Ordered portable operations. Platform adapters preserve order, including nested padding. */
@Immutable
interface DomposableModifier {
    val operations: List<DomposableModifierOperation>

    fun then(other: DomposableModifier): DomposableModifier = Chain(operations + other.operations)

    companion object : DomposableModifier {
        override val operations: List<DomposableModifierOperation> = emptyList()
    }
}

private data class Chain(
    override val operations: List<DomposableModifierOperation>,
) : DomposableModifier

sealed interface DomposableModifierOperation {
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

    data object FillWidth : DomposableModifierOperation

    data object FillHeight : DomposableModifierOperation

    data class Background(
        val color: DomposableColor,
    ) : DomposableModifierOperation

    data class Radius(
        val length: DomposableLength,
    ) : DomposableModifierOperation

    data class Opacity(
        val value: Float,
    ) : DomposableModifierOperation

    data class Click(
        val onClick: () -> Unit,
        val label: String,
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
}

private fun DomposableModifier.append(operation: DomposableModifierOperation): DomposableModifier =
    Chain(operations + operation)

fun DomposableModifier.padding(all: DomposableLength): DomposableModifier = padding(DomposableInsets(all))

fun DomposableModifier.padding(insets: DomposableInsets): DomposableModifier =
    append(DomposableModifierOperation.Padding(insets))

fun DomposableModifier.width(value: DomposableLength): DomposableModifier =
    append(DomposableModifierOperation.Width(value))

fun DomposableModifier.height(value: DomposableLength): DomposableModifier =
    append(DomposableModifierOperation.Height(value))

fun DomposableModifier.size(value: DomposableLength): DomposableModifier = width(value).height(value)

fun DomposableModifier.constrainContentWidth(value: DomposableLength): DomposableModifier =
    append(DomposableModifierOperation.MaxWidth(value))

fun DomposableModifier.fillMaxWidth(): DomposableModifier = append(DomposableModifierOperation.FillWidth)

fun DomposableModifier.fillMaxHeight(): DomposableModifier = append(DomposableModifierOperation.FillHeight)

fun DomposableModifier.background(value: DomposableColor): DomposableModifier =
    append(DomposableModifierOperation.Background(value))

fun DomposableModifier.clip(radius: DomposableLength): DomposableModifier =
    append(DomposableModifierOperation.Radius(radius))

fun DomposableModifier.alpha(value: Float): DomposableModifier {
    require(value.isFinite() && value in 0f..1f)
    return append(DomposableModifierOperation.Opacity(value))
}

fun DomposableModifier.clickable(
    label: String,
    onClick: () -> Unit,
): DomposableModifier = append(DomposableModifierOperation.Click(onClick, label))

fun DomposableModifier.contentDescription(value: String): DomposableModifier =
    append(DomposableModifierOperation.Description(value))

fun DomposableModifier.testId(value: String): DomposableModifier = append(DomposableModifierOperation.TestId(value))

fun DomposableModifier.verticalScroll(): DomposableModifier =
    append(DomposableModifierOperation.Scroll(DomposableOrientation.Vertical))

fun DomposableModifier.horizontalScroll(): DomposableModifier =
    append(DomposableModifierOperation.Scroll(DomposableOrientation.Horizontal))
