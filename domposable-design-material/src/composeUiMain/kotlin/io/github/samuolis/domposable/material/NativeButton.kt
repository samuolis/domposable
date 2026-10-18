package io.github.samuolis.domposable.material

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.ui.DomposableButtonProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableFormEnabled
import io.github.samuolis.domposable.ui.toComposeModifier

@Composable
internal fun NativeButton(
    props: DomposableButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.button
    val formEnabled = domposableFormEnabled()
    if (appearance == DomposableButtonAppearance() && !props.loading && props.expanded == null) {
        when (props.style) {
            DomposableButtonStyle.Filled -> {
                Button(
                    props.onClick,
                    modifier.toComposeModifier(),
                    enabled =
                        props.enabled && formEnabled,
                ) { content() }
            }

            DomposableButtonStyle.Outlined -> {
                OutlinedButton(
                    props.onClick,
                    modifier.toComposeModifier(),
                    enabled =
                        props.enabled && formEnabled,
                ) { content() }
            }

            DomposableButtonStyle.Text -> {
                TextButton(
                    props.onClick,
                    modifier.toComposeModifier(),
                    enabled =
                        props.enabled && formEnabled,
                ) { content() }
            }

            DomposableButtonStyle.Elevated -> {
                ElevatedButton(
                    props.onClick,
                    modifier.toComposeModifier(),
                    enabled = props.enabled && formEnabled,
                ) { content() }
            }

            DomposableButtonStyle.FilledTonal -> {
                FilledTonalButton(
                    props.onClick,
                    modifier.toComposeModifier(),
                    enabled = props.enabled && formEnabled,
                ) { content() }
            }
        }
        return
    }
    val defaults =
        when (props.style) {
            DomposableButtonStyle.Filled -> ButtonDefaults.buttonColors()
            DomposableButtonStyle.Elevated -> ButtonDefaults.elevatedButtonColors()
            DomposableButtonStyle.FilledTonal -> ButtonDefaults.filledTonalButtonColors()
            DomposableButtonStyle.Outlined -> ButtonDefaults.outlinedButtonColors()
            DomposableButtonStyle.Text -> ButtonDefaults.textButtonColors()
        }
    val useEnabledColors = (props.enabled && formEnabled) || props.loading
    val containerColor =
        if (useEnabledColors) {
            appearance.container?.let { Color(it.argb) } ?: defaults.containerColor
        } else {
            appearance.disabledContainer?.let { Color(it.argb) } ?: defaults.disabledContainerColor
        }
    val contentColor =
        if (useEnabledColors) {
            appearance.content?.let { Color(it.argb) } ?: defaults.contentColor
        } else {
            appearance.disabledContent?.let { Color(it.argb) } ?: defaults.disabledContentColor
        }
    val borderColor = (if (useEnabledColors) appearance.border else appearance.disabledBorder)?.let { Color(it.argb) }
    val defaultBorder =
        if (props.style ==
            DomposableButtonStyle.Outlined
        ) {
            ButtonDefaults.outlinedButtonBorder(useEnabledColors)
        } else {
            null
        }
    val border =
        when {
            borderColor != null -> {
                BorderStroke((appearance.borderWidth?.value ?: 1f).dp, borderColor)
            }

            defaultBorder != null -> {
                BorderStroke(
                    appearance.borderWidth?.value?.dp ?: defaultBorder.width,
                    defaultBorder.brush,
                )
            }

            else -> {
                null
            }
        }
    val shape = appearance.shape?.materialShape() ?: androidx.compose.material3.ButtonDefaults.shape
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val interactionEnabled = props.enabled && formEnabled && !props.loading
    val interaction = appearance.interaction
    val resolvedContainer =
        when {
            !interactionEnabled -> {
                containerColor
            }

            pressed -> {
                interaction?.pressedContainer?.let { Color(it.argb) }
                    ?: interaction?.stateLayerOpacity?.let {
                        contentColor
                            .copy(
                                alpha = it.pressed,
                            ).compositeOver(containerColor)
                    }
                    ?: containerColor
            }

            hovered -> {
                interaction?.hoveredContainer?.let { Color(it.argb) }
                    ?: interaction?.stateLayerOpacity?.let {
                        contentColor
                            .copy(
                                alpha = it.hovered,
                            ).compositeOver(containerColor)
                    }
                    ?: containerColor
            }

            focused -> {
                interaction?.stateLayerOpacity?.let {
                    contentColor
                        .copy(
                            alpha = it.focused,
                        ).compositeOver(containerColor)
                }
                    ?: containerColor
            }

            else -> {
                containerColor
            }
        }
    val resolvedContent =
        when {
            !interactionEnabled -> contentColor
            pressed -> interaction?.pressedContent?.let { Color(it.argb) } ?: contentColor
            hovered -> interaction?.hoveredContent?.let { Color(it.argb) } ?: contentColor
            else -> contentColor
        }
    val scale by animateFloatAsState(
        targetValue =
            when {
                interactionEnabled && pressed -> appearance.pressedScale
                interactionEnabled && hovered -> appearance.hoveredScale
                else -> 1f
            },
        animationSpec =
            tween(
                durationMillis = LocalDomposableTokens.current.motionDurationMillis,
                easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
            ),
    )
    val padding = appearance.contentPadding
    val buttonModifier =
        modifier
            .toComposeModifier()
            .alpha(if (!interactionEnabled && !props.loading) appearance.disabledOpacity ?: 1f else 1f)
            .drawButtonFocusRing(
                focused && interactionEnabled,
                interaction?.let {
                    it.focusRingColor?.let { color -> Color(color.argb) }
                        ?: MaterialTheme.colorScheme.primary
                },
                interaction?.focusRingWidth?.value ?: if (interaction != null) 3f else null,
                interaction?.focusRingOffset?.value ?: if (interaction != null) 3f else null,
                shape,
            ).scale(scale)
            .minimumInteractiveComponentSize()
            .shadow(if (props.style == DomposableButtonStyle.Elevated && useEnabledColors) 1.dp else 0.dp, shape)
            .heightIn(min = (appearance.minHeight?.value ?: 40f).dp)
            .clip(shape)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                props.expanded?.let { stateDescription = if (it) "Expanded" else "Collapsed" }
                if (props.loading) {
                    props.loadingLabel?.let { contentDescription = it }
                    disabled()
                }
            }.background(resolvedContainer, shape)
            .then(
                border?.let { Modifier.border(it, shape) }
                    ?: Modifier,
            ).clickable(
                interactionSource = interactionSource,
                indication = if (interaction == null) LocalIndication.current else null,
                enabled =
                    props.enabled && formEnabled && !props.loading,
                role = Role.Button,
                onClick = props.onClick,
            ).defaultMinSize(
                minWidth = (appearance.minWidth?.value ?: 0f).dp,
                minHeight =
                    (
                        appearance.minHeight?.value
                            ?: 40f
                    ).dp,
            ).padding(
                start = (padding?.start?.value ?: 24f).dp,
                top = (padding?.top?.value ?: 8f).dp,
                end =
                    (
                        padding?.end?.value
                            ?: 24f
                    ).dp,
                bottom = (padding?.bottom?.value ?: 8f).dp,
            )
    Box(buttonModifier, contentAlignment = Alignment.Center) {
        CompositionLocalProvider(
            LocalContentColor provides resolvedContent,
            LocalMaterialButtonTextAppearance provides appearance.textAppearance,
        ) {
            ProvideTextStyle(appearance.textAppearance.materialTextStyle(MaterialTheme.typography.labelLarge)) {
                Row(
                    Modifier.alpha(if (props.loading) 0f else 1f),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            appearance.contentSpacing.value.dp,
                            Alignment.CenterHorizontally,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    content()
                }
            }
            if (props.loading) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(
                            (
                                appearance.indicatorSize?.value
                                    ?: 20f
                            ).dp,
                        ),
                    color = contentColor,
                    strokeWidth = (appearance.indicatorStrokeWidth?.value ?: 2f).dp,
                )
            }
        }
    }
}

internal fun Modifier.drawButtonFocusRing(
    focused: Boolean,
    color: Color?,
    widthDp: Float?,
    offsetDp: Float?,
    shape: Shape,
): Modifier {
    if (!focused || color == null || widthDp == null) return this
    if (widthDp == 0f) return this
    val ringOffset = offsetDp ?: return this
    return drawBehind {
        val strokeWidth = widthDp.dp.toPx()
        val expansion = ringOffset.dp.toPx() + strokeWidth / 2f
        translate(-expansion, -expansion) {
            drawOutline(
                shape.createOutline(
                    Size(size.width + expansion * 2f, size.height + expansion * 2f),
                    layoutDirection,
                    this,
                ),
                color,
                style = Stroke(strokeWidth),
            )
        }
    }
}

internal fun DomposableShape.materialShape(): Shape =
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
            RoundedCornerShape(
                topStart.value.dp,
                topEnd.value.dp,
                bottomEnd.value.dp,
                bottomStart.value.dp,
            )
        }
    }

@Composable
internal fun DomposableTextAppearance?.materialTextStyle(base: TextStyle): TextStyle = nativeTextStyle(base)
