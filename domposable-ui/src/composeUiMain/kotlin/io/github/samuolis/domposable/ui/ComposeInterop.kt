package io.github.samuolis.domposable.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableLayoutAxis
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableSize
import kotlin.math.roundToInt

/** Installed by a product provider; explicit call-site renderers take precedence. */
val LocalDomposableNativeModifierRenderer =
    staticCompositionLocalOf<((DomposableModifierElement) -> Modifier?)?> { null }

/** Native host interop for incremental migration. Keep this element inside Compose UI renderers. */
fun Modifier.asDomposableModifier(): DomposableModifier = DomposableModifier.then(NativeModifierElement(this))

/** Appends a native-only operation without replacing the portable operations already in the chain. */
fun DomposableModifier.then(modifier: Modifier): DomposableModifier = then(NativeModifierElement(modifier))

private data class NativeModifierElement(
    val modifier: Modifier,
) : DomposableModifierElement

/** Native-only conversion. Application elements require an explicit renderer and are never dropped. */
@Suppress("ModifierFactoryExtensionFunction")
@Composable
fun DomposableModifier.toComposeModifier(
    operationRenderer: ((DomposableModifierElement) -> Modifier?)? = null,
): Modifier {
    val renderer = operationRenderer ?: LocalDomposableNativeModifierRenderer.current
    var result: Modifier = Modifier
    for (element in elements) {
        val operation = element as? DomposableModifierOperation
        if (operation == null) {
            val native =
                if (element is NativeModifierElement) {
                    element.modifier
                } else {
                    renderer?.invoke(element)
                        ?: error(
                            "No native renderer handled application modifier element ${element::class.qualifiedName}",
                        )
                }
            result = result.then(native)
            continue
        }
        result =
            when (operation) {
                is DomposableModifierOperation.Padding -> {
                    result.padding(
                        start = operation.insets.start.value.dp,
                        top = operation.insets.top.value.dp,
                        end = operation.insets.end.value.dp,
                        bottom = operation.insets.bottom.value.dp,
                    )
                }

                is DomposableModifierOperation.Width -> {
                    result.width(operation.length.value.dp)
                }

                is DomposableModifierOperation.Height -> {
                    result.height(operation.length.value.dp)
                }

                is DomposableModifierOperation.ViewportHeight -> {
                    val viewport =
                        with(LocalDensity.current) {
                            LocalWindowInfo.current.containerSize.height
                                .toDp()
                        }
                    val height = (viewport * operation.fraction - operation.subtract.value.dp).coerceAtLeast(0.dp)
                    when (operation.mode) {
                        DomposableViewportHeightMode.Fixed -> result.height(height)
                        DomposableViewportHeightMode.Minimum -> result.heightIn(min = height)
                        DomposableViewportHeightMode.Maximum -> result.heightIn(max = height)
                    }
                }

                is DomposableModifierOperation.MaxWidth -> {
                    result.widthIn(max = operation.length.value.dp)
                }

                is DomposableModifierOperation.FillWidthFraction -> {
                    result.layout { measurable, constraints ->
                        val requestedMin =
                            operation.minWidth
                                ?.value
                                ?.dp
                                ?.roundToPx() ?: 0
                        val requestedMax =
                            operation.maxWidth
                                ?.value
                                ?.dp
                                ?.roundToPx() ?: constraints.maxWidth
                        val minWidth = maxOf(constraints.minWidth, requestedMin).coerceAtMost(constraints.maxWidth)
                        val maxWidth = minOf(constraints.maxWidth, requestedMax).coerceAtLeast(minWidth)
                        if (constraints.hasBoundedWidth) {
                            val width =
                                (constraints.maxWidth * operation.fraction).roundToInt().coerceIn(minWidth, maxWidth)
                            val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                            layout(width, placeable.height.coerceIn(constraints.minHeight, constraints.maxHeight)) {
                                placeable.placeRelative(0, 0)
                            }
                        } else {
                            val placeable =
                                measurable.measure(constraints.copy(minWidth = minWidth, maxWidth = maxWidth))
                            layout(
                                placeable.width,
                                placeable.height.coerceIn(constraints.minHeight, constraints.maxHeight),
                            ) {
                                placeable.placeRelative(0, 0)
                            }
                        }
                    }
                }

                is DomposableModifierOperation.MinWidth -> {
                    result.widthIn(min = operation.length.value.dp)
                }

                is DomposableModifierOperation.MinHeight -> {
                    result.heightIn(min = operation.length.value.dp)
                }

                is DomposableModifierOperation.MaxHeight -> {
                    result.heightIn(max = operation.length.value.dp)
                }

                DomposableModifierOperation.FillWidth -> {
                    result.fillMaxWidth()
                }

                DomposableModifierOperation.PointerPassthrough -> {
                    result
                }

                DomposableModifierOperation.FillHeight -> {
                    result.fillMaxHeight()
                }

                is DomposableModifierOperation.Background -> {
                    result.background(Color(operation.color.argb))
                }

                is DomposableModifierOperation.ShapeBackground -> {
                    result.background(Color(operation.color.argb), operation.shape.nativeShape())
                }

                is DomposableModifierOperation.GradientBackground -> {
                    val colorStops =
                        operation.gradient.stops
                            .map { it.position to Color(it.color.argb) }
                            .toTypedArray()
                    val brush =
                        when (operation.gradient.axis) {
                            io.github.samuolis.domposable.foundation.DomposableLayoutAxis.Horizontal -> {
                                Brush.horizontalGradient(*colorStops)
                            }

                            io.github.samuolis.domposable.foundation.DomposableLayoutAxis.Vertical -> {
                                Brush.verticalGradient(*colorStops)
                            }
                        }
                    result.background(brush, operation.shape.nativeShape())
                }

                is DomposableModifierOperation.Radius -> {
                    result.clip(RoundedCornerShape(operation.length.value.dp))
                }

                is DomposableModifierOperation.ShapeClip -> {
                    result.clip(operation.shape.nativeShape())
                }

                is DomposableModifierOperation.Border -> {
                    result.border(
                        operation.width.value.dp,
                        Color(operation.color.argb),
                        operation.shape.nativeShape(),
                    )
                }

                is DomposableModifierOperation.EdgeBorder -> {
                    result.drawBehind {
                        val strokeWidth =
                            operation.width.value.dp
                                .toPx()
                        val halfStroke = strokeWidth / 2f
                        val startIsLeft = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Ltr
                        val (start, end) =
                            when (operation.edge) {
                                DomposableBorderEdge.Top -> {
                                    Offset(0f, halfStroke) to Offset(size.width, halfStroke)
                                }

                                DomposableBorderEdge.End -> {
                                    val x = if (startIsLeft) size.width - halfStroke else halfStroke
                                    Offset(x, 0f) to Offset(x, size.height)
                                }

                                DomposableBorderEdge.Bottom -> {
                                    Offset(0f, size.height - halfStroke) to Offset(size.width, size.height - halfStroke)
                                }

                                DomposableBorderEdge.Start -> {
                                    val x = if (startIsLeft) halfStroke else size.width - halfStroke
                                    Offset(x, 0f) to Offset(x, size.height)
                                }
                            }
                        drawLine(Color(operation.color.argb), start, end, strokeWidth)
                    }
                }

                is DomposableModifierOperation.Region -> {
                    result.semantics {
                        isTraversalGroup = true
                        contentDescription = operation.label
                    }
                }

                is DomposableModifierOperation.AspectRatio -> {
                    result.aspectRatio(operation.ratio)
                }

                is DomposableModifierOperation.ZIndex -> {
                    result.zIndex(operation.value)
                }

                is DomposableModifierOperation.Offset -> {
                    result.offset(operation.value.x.dp, operation.value.y.dp)
                }

                is DomposableModifierOperation.WrapContentSize -> {
                    result.wrapContentSize(operation.horizontal.nativeHorizontal(operation.vertical))
                }

                is DomposableModifierOperation.Opacity -> {
                    result.alpha(operation.value)
                }

                is DomposableModifierOperation.Scale -> {
                    result.scale(operation.x, operation.y)
                }

                is DomposableModifierOperation.Rotation -> {
                    result.rotate(operation.degrees)
                }

                is DomposableModifierOperation.Shadow -> {
                    result.shadow(
                        operation.elevation.value.dp,
                        operation.shape.nativeShape(),
                        clip = false,
                    )
                }

                is DomposableModifierOperation.Click -> {
                    result.clickable(
                        onClickLabel = operation.label,
                        role = Role.Button,
                        onClick = operation.onClick,
                    )
                }

                is DomposableModifierOperation.InteractiveClick -> {
                    result.clickable(
                        enabled = operation.enabled,
                        onClickLabel = operation.label,
                        role = operation.role.nativeRole(),
                        onClick = operation.onClick,
                    )
                }

                is DomposableModifierOperation.Description -> {
                    result.semantics { contentDescription = operation.text }
                }

                is DomposableModifierOperation.TestId -> {
                    result.testTag(operation.value)
                }

                is DomposableModifierOperation.Scroll -> {
                    if (operation.orientation == DomposableOrientation.Vertical) {
                        result.verticalScroll(rememberScrollState())
                    } else {
                        result.horizontalScroll(rememberScrollState())
                    }
                }

                is DomposableModifierOperation.StatefulScroll -> {
                    if (operation.orientation == DomposableOrientation.Vertical) {
                        result.verticalScroll(operation.state.nativeState)
                    } else {
                        result.horizontalScroll(operation.state.nativeState)
                    }
                }

                is DomposableModifierOperation.Weight -> {
                    when (val scope = LocalDomposableModifierScope.current) {
                        is NativeModifierScope.Column -> {
                            scope.value.run { result.weight(operation.weight, operation.fill) }
                        }

                        is NativeModifierScope.Row -> {
                            scope.value.run { result.weight(operation.weight, operation.fill) }
                        }

                        else -> {
                            error("DomposableModifier.weight() must be rendered as a Row or Column child")
                        }
                    }
                }

                is DomposableModifierOperation.AxisAlignment -> {
                    when (operation.axis) {
                        DomposableLayoutAxis.Horizontal -> {
                            val column =
                                LocalDomposableModifierScope.current as? NativeModifierScope.Column
                                    ?: error("Horizontal child alignment requires a Column scope")
                            column.value.run { result.align(operation.alignment.nativeHorizontal()) }
                        }

                        DomposableLayoutAxis.Vertical -> {
                            val row =
                                LocalDomposableModifierScope.current as? NativeModifierScope.Row
                                    ?: error("Vertical child alignment requires a Row scope")
                            row.value.run { result.align(operation.alignment.nativeVertical()) }
                        }
                    }
                }

                is DomposableModifierOperation.BoxAlignment -> {
                    val box =
                        LocalDomposableModifierScope.current as? NativeModifierScope.Box
                            ?: error("DomposableModifier.boxAlignment() must be rendered as a Box child")
                    box.value.run { result.align(operation.horizontal.nativeHorizontal(operation.vertical)) }
                }
            }
    }
    return result
}

private fun DomposableShape.nativeShape(): Shape =
    when (this) {
        DomposableShape.Rectangle -> {
            androidx.compose.ui.graphics.RectangleShape
        }

        DomposableShape.Circle -> {
            CircleShape
        }

        is DomposableShape.Rounded -> {
            RoundedCornerShape(radius.value.dp)
        }

        is DomposableShape.Corners -> {
            RoundedCornerShape(
                topStart = topStart.value.dp,
                topEnd = topEnd.value.dp,
                bottomEnd = bottomEnd.value.dp,
                bottomStart = bottomStart.value.dp,
            )
        }
    }

private fun DomposableAlignment.nativeHorizontal(): Alignment.Horizontal =
    when (this) {
        DomposableAlignment.Start -> Alignment.Start
        DomposableAlignment.Center -> Alignment.CenterHorizontally
        DomposableAlignment.End -> Alignment.End
    }

private fun DomposableAlignment.nativeVertical(): Alignment.Vertical =
    when (this) {
        DomposableAlignment.Start -> Alignment.Top
        DomposableAlignment.Center -> Alignment.CenterVertically
        DomposableAlignment.End -> Alignment.Bottom
    }

private fun DomposableAlignment.nativeHorizontal(vertical: DomposableAlignment): Alignment =
    when (vertical) {
        DomposableAlignment.Start -> {
            when (this) {
                DomposableAlignment.Start -> Alignment.TopStart
                DomposableAlignment.Center -> Alignment.TopCenter
                DomposableAlignment.End -> Alignment.TopEnd
            }
        }

        DomposableAlignment.Center -> {
            when (this) {
                DomposableAlignment.Start -> Alignment.CenterStart
                DomposableAlignment.Center -> Alignment.Center
                DomposableAlignment.End -> Alignment.CenterEnd
            }
        }

        DomposableAlignment.End -> {
            when (this) {
                DomposableAlignment.Start -> Alignment.BottomStart
                DomposableAlignment.Center -> Alignment.BottomCenter
                DomposableAlignment.End -> Alignment.BottomEnd
            }
        }
    }

private fun DomposableClickRole.nativeRole(): Role =
    when (this) {
        DomposableClickRole.Button, DomposableClickRole.Link -> Role.Button
        DomposableClickRole.Checkbox -> Role.Checkbox
        DomposableClickRole.RadioButton -> Role.RadioButton
        DomposableClickRole.Switch -> Role.Switch
        DomposableClickRole.Image -> Role.Image
    }

private sealed interface NativeModifierScope {
    data class Column(
        val value: ColumnScope,
    ) : NativeModifierScope

    data class Row(
        val value: RowScope,
    ) : NativeModifierScope

    data class Box(
        val value: BoxScope,
    ) : NativeModifierScope
}

private val LocalDomposableModifierScope = staticCompositionLocalOf<NativeModifierScope?> { null }

/** Install inside a native Column content lambda so child weights and horizontal alignment resolve natively. */
@Composable
fun ColumnScope.ProvideDomposableColumnModifierScope(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDomposableModifierScope provides NativeModifierScope.Column(this), content = content)
}

/** Install inside a native Row content lambda so child weights and vertical alignment resolve natively. */
@Composable
fun RowScope.ProvideDomposableRowModifierScope(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDomposableModifierScope provides NativeModifierScope.Row(this), content = content)
}

/** Install inside a native Box content lambda so child alignment resolves natively. */
@Composable
fun BoxScope.ProvideDomposableBoxModifierScope(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDomposableModifierScope provides NativeModifierScope.Box(this), content = content)
}

@Composable
actual fun DomposableMeasure(
    modifier: DomposableModifier,
    content: @Composable (DomposableSize) -> Unit,
) {
    var size by remember { mutableStateOf(DomposableSize(0f, 0f)) }
    val density = LocalDensity.current.density
    Box(
        modifier.toComposeModifier().fillMaxWidth().onSizeChanged {
            size = DomposableSize(it.width / density, it.height / density)
        },
    ) { content(size) }
}
