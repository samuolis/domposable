package io.github.samuolis.domposable.material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableShapes
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance

internal fun DomposableShape.nativeShape(): Shape =
    when (this) {
        DomposableShape.Rectangle -> {
            RectangleShape
        }

        DomposableShape.Circle -> {
            CircleShape
        }

        is DomposableShape.Rounded -> {
            RoundedCornerShape(radius.value.dp)
        }

        is DomposableShape.Corners -> {
            RoundedCornerShape(topStart.value.dp, topEnd.value.dp, bottomEnd.value.dp, bottomStart.value.dp)
        }
    }

internal fun DomposableShape.nativeCornerShape(): CornerBasedShape =
    when (this) {
        DomposableShape.Rectangle -> {
            RoundedCornerShape(0.dp)
        }

        DomposableShape.Circle -> {
            CircleShape
        }

        is DomposableShape.Rounded -> {
            RoundedCornerShape(radius.value.dp)
        }

        is DomposableShape.Corners -> {
            RoundedCornerShape(topStart.value.dp, topEnd.value.dp, bottomEnd.value.dp, bottomStart.value.dp)
        }
    }

internal fun DomposableShapes.nativeShapes(defaults: Shapes = Shapes()): Shapes =
    Shapes(
        extraSmall = extraSmall?.nativeCornerShape() ?: defaults.extraSmall,
        small = small?.nativeCornerShape() ?: defaults.small,
        medium = medium?.nativeCornerShape() ?: defaults.medium,
        large = large?.nativeCornerShape() ?: defaults.large,
        extraLarge = extraLarge?.nativeCornerShape() ?: defaults.extraLarge,
        largeIncreased = largeIncreased?.nativeCornerShape() ?: defaults.largeIncreased,
        extraLargeIncreased = extraLargeIncreased?.nativeCornerShape() ?: defaults.extraLargeIncreased,
        extraExtraLarge = extraExtraLarge?.nativeCornerShape() ?: defaults.extraExtraLarge,
    )

internal fun Modifier.nativeModifier(
    appearance: DomposableSurfaceAppearance,
    defaultShape: Shape,
): Modifier {
    val shape = appearance.shape?.nativeShape() ?: defaultShape
    val borderColor = appearance.border
    return this
        .then(appearance.shadowElevation?.let { Modifier.shadow(it.value.dp, shape) } ?: Modifier)
        .clip(shape)
        .then(
            if (borderColor == null) {
                Modifier
            } else {
                Modifier.border(BorderStroke(appearance.borderWidth.value.dp, borderColor.nativeColor()), shape)
            },
        )
}

internal fun Modifier.nativeClipAndBorder(
    appearance: DomposableSurfaceAppearance,
    shape: Shape,
): Modifier =
    this
        .clip(shape)
        .then(
            appearance.border?.let {
                Modifier.border(BorderStroke(appearance.borderWidth.value.dp, it.nativeColor()), shape)
            } ?: Modifier,
        )

internal fun DomposableSurfaceAppearance.nativeShape(defaultShape: Shape): Shape = shape?.nativeShape() ?: defaultShape

internal fun DomposableSurfaceAppearance.nativeContainer(defaultColor: Color): Color =
    container?.nativeColor() ?: defaultColor

internal fun DomposableSurfaceAppearance.nativeContent(defaultColor: Color): Color =
    content?.nativeColor() ?: defaultColor

internal fun DomposableSurfaceAppearance.isEmpty(): Boolean = this == DomposableSurfaceAppearance()
