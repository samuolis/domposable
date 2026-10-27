@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.columns
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Label
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.shape
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.foundation.DomposableImageScale
import io.github.samuolis.domposable.foundation.DomposableImeAction
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableLiveRegion
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableScaffoldFabPosition
import io.github.samuolis.domposable.foundation.DomposableTextAlignment
import io.github.samuolis.domposable.foundation.DomposableTextDecoration
import io.github.samuolis.domposable.foundation.DomposableTextOverflow
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.ui.DomposableArrangement
import io.github.samuolis.domposable.ui.DomposableDataTableProps
import io.github.samuolis.domposable.ui.DomposableLineChartProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableValueChartProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.ProvideDomposableBoxModifierScope
import io.github.samuolis.domposable.ui.ProvideDomposableColumnModifierScope
import io.github.samuolis.domposable.ui.ProvideDomposableRowModifierScope
import io.github.samuolis.domposable.ui.background
import io.github.samuolis.domposable.ui.clickable
import io.github.samuolis.domposable.ui.contentDescription
import io.github.samuolis.domposable.ui.domposableFormEnabled
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.rememberDomposableFormSubmitOrNull
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.toComposeModifier

private fun DomposableAlignment.horizontal() =
    when (this) {
        DomposableAlignment.Start -> Alignment.Start
        DomposableAlignment.Center -> Alignment.CenterHorizontally
        DomposableAlignment.End -> Alignment.End
    }

private fun DomposableAlignment.vertical() =
    when (this) {
        DomposableAlignment.Start -> Alignment.Top
        DomposableAlignment.Center -> Alignment.CenterVertically
        DomposableAlignment.End -> Alignment.Bottom
    }

private fun DomposableAlignment.box(vertical: DomposableAlignment): Alignment =
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

internal fun DomposableFontWeight.native() = FontWeight(weight)

private fun DomposableTextAlignment.native() =
    when (this) {
        DomposableTextAlignment.Start -> TextAlign.Start
        DomposableTextAlignment.Center -> TextAlign.Center
        DomposableTextAlignment.End -> TextAlign.End
        DomposableTextAlignment.Justify -> TextAlign.Justify
    }

private fun DomposableTextDecoration.native() =
    when (this) {
        DomposableTextDecoration.None -> TextDecoration.None
        DomposableTextDecoration.Underline -> TextDecoration.Underline
        DomposableTextDecoration.LineThrough -> TextDecoration.LineThrough
    }

private fun DomposableTextOverflow.native() =
    when (this) {
        DomposableTextOverflow.Clip -> TextOverflow.Clip
        DomposableTextOverflow.Ellipsis -> TextOverflow.Ellipsis
        DomposableTextOverflow.Visible -> TextOverflow.Visible
    }

internal fun DomposableLiveRegion.native() =
    when (this) {
        DomposableLiveRegion.Polite -> LiveRegionMode.Polite
        DomposableLiveRegion.Assertive -> LiveRegionMode.Assertive
    }

internal fun DomposableImeAction.native() =
    when (this) {
        DomposableImeAction.Default -> ImeAction.Default
        DomposableImeAction.Done -> ImeAction.Done
        DomposableImeAction.Go -> ImeAction.Go
        DomposableImeAction.Next -> ImeAction.Next
        DomposableImeAction.Previous -> ImeAction.Previous
        DomposableImeAction.Search -> ImeAction.Search
        DomposableImeAction.Send -> ImeAction.Send
    }

@Composable
internal actual fun RenderDomposableForm(
    props: io.github.samuolis.domposable.ui.DomposableFormProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeDomposableForm(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableDisclosure(
    props: io.github.samuolis.domposable.ui.DomposableDisclosureProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeDomposableDisclosure(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableRichHtml(
    props: io.github.samuolis.domposable.ui.DomposableRichHtmlProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeDomposableRichHtml(props, modifier)
}

@Composable
internal actual fun RenderDomposableTheme(
    props: io.github.samuolis.domposable.ui.DomposableThemeProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val colors = props.tokens.colors
    val type = props.tokens.typography
    val colorScheme = colors.nativeMaterialColorScheme()
    val typography =
        type.nativeTypography(LocalDomposableNativeTypography.current ?: MaterialTheme.typography)
    val shapes = props.tokens.shapes.nativeShapes(MaterialTheme.shapes)
    if (props.tokens.designLanguage ==
        io.github.samuolis.domposable.foundation.DomposableDesignLanguage.Expressive
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            typography = typography,
            shapes = shapes,
            content = content,
        )
    } else {
        MaterialTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.standard(),
            typography = typography,
            shapes = shapes,
            content = content,
        )
    }
}

@Composable
internal actual fun RenderDomposableColumn(
    props: io.github.samuolis.domposable.ui.DomposableColumnProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier.toComposeModifier(),
        verticalArrangement = props.arrangement.vertical(props.gap.value.dp),
        horizontalAlignment = props.alignment.horizontal(),
    ) {
        ProvideDomposableColumnModifierScope { content() }
    }
}

@Composable
internal actual fun RenderDomposableRow(
    props: io.github.samuolis.domposable.ui.DomposableRowProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val rowModifier =
        modifier.toComposeModifier().let { if (props.stretch) it.fillMaxHeight() else it }
    Row(
        rowModifier,
        horizontalArrangement = props.arrangement.horizontal(props.gap.value.dp),
        verticalAlignment = props.alignment.vertical(),
    ) {
        ProvideDomposableRowModifierScope { content() }
    }
}

@Composable
internal actual fun RenderDomposableFlowRow(
    props: io.github.samuolis.domposable.ui.DomposableFlowRowProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier.toComposeModifier(),
        horizontalArrangement = props.arrangement.horizontal(props.gap.value.dp),
        verticalArrangement = Arrangement.spacedBy(props.rowGap.value.dp),
        itemVerticalAlignment = props.alignment.vertical(),
    ) {
        ProvideDomposableRowModifierScope { content() }
    }
}

@Composable
internal actual fun RenderDomposableLink(
    props: io.github.samuolis.domposable.ui.DomposableLinkProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeDomposableLink(props, modifier)
}

@Composable
internal actual fun RenderDomposableBox(
    props: io.github.samuolis.domposable.ui.DomposableBoxProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.toComposeModifier(), contentAlignment = props.alignment.box(props.verticalAlignment)) {
        ProvideDomposableBoxModifierScope { content() }
    }
}

@Composable
internal actual fun RenderDomposableLazyColumn(
    props: io.github.samuolis.domposable.ui.DomposableLazyColumnProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val window = props.window
    LazyColumn(
        window?.let { modifier.toComposeModifier().height(it.viewportHeight.value.dp) }
            ?: modifier.toComposeModifier(),
        verticalArrangement = Arrangement.spacedBy(props.gap.value.dp),
    ) {
        items(props.items, key = { it.key }) { item ->
            if (window == null || window.measureRows) {
                item.content()
            } else {
                Box(Modifier.height(window.rowHeight.value.dp).clipToBounds()) { item.content() }
            }
        }
    }
}

@Composable
internal actual fun RenderDomposableLazyRow(
    props: io.github.samuolis.domposable.ui.DomposableLazyRowProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    LazyRow(
        props.window?.let { modifier.toComposeModifier().width(it.viewportWidth.value.dp) }
            ?: modifier.toComposeModifier(),
        horizontalArrangement = Arrangement.spacedBy(props.gap.value.dp),
    ) {
        items(props.items, key = { it.key }) { item ->
            val window = props.window
            if (window == null) {
                item.content()
            } else {
                Box(
                    Modifier
                        .width(
                            window.itemWidth.value.dp,
                        ).height(window.itemHeight.value.dp)
                        .clipToBounds(),
                ) {
                    item.content()
                }
            }
        }
    }
}

@Composable
internal actual fun RenderDomposableLazyGrid(
    props: io.github.samuolis.domposable.ui.DomposableLazyGridProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    LazyVerticalGrid(
        GridCells.Fixed(props.columns),
        props.window?.let { modifier.toComposeModifier().height(it.viewportHeight.value.dp) }
            ?: modifier.toComposeModifier(),
        horizontalArrangement = Arrangement.spacedBy(props.gap.value.dp),
        verticalArrangement = Arrangement.spacedBy(props.gap.value.dp),
    ) {
        items(props.items, key = { it.key }) { item ->
            val window = props.window
            if (window == null) {
                item.content()
            } else {
                Box(Modifier.height(window.rowHeight.value.dp).clipToBounds()) { item.content() }
            }
        }
    }
}

@Composable
internal actual fun RenderDomposableTable(
    props: io.github.samuolis.domposable.ui.DomposableTableProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeTable(props, modifier)
}

@Composable
internal actual fun RenderDomposableLineChart(
    props: io.github.samuolis.domposable.ui.DomposableLineChartProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeLineChart(props, modifier)
}

@Composable
internal actual fun RenderDomposableValueChart(
    props: io.github.samuolis.domposable.ui.DomposableValueChartProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeValueChart(props, modifier)
}

@Composable
internal actual fun RenderDomposableDataTable(
    props: io.github.samuolis.domposable.ui.DomposableDataTableProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeDataTable(props, modifier)
}

@Composable
internal actual fun RenderDomposableText(
    props: io.github.samuolis.domposable.ui.DomposableTextProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance =
        props.appearance ?: LocalMaterialButtonTextAppearance.current
            ?: LocalDomposableTokens.current.components.text
    val baseStyle =
        when (props.style) {
            DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge
            DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge
            DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge
            DomposableTextStyle.Inherit -> LocalTextStyle.current
        }
    val style = appearance.nativeTextStyle(baseStyle)
    DomposableText(
        text =
            props.annotatedText(
                if (style.color == Color.Unspecified) {
                    androidx.compose.material3.LocalContentColor.current
                } else {
                    style.color
                },
            ),
        modifier = modifier,
        style = props.style,
        color = props.color,
        headingLevel = props.headingLevel,
        appearance = appearance,
    )
}

@Composable
internal actual fun RenderDomposableButton(
    props: io.github.samuolis.domposable.ui.DomposableButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeButton(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableIconButton(
    props: io.github.samuolis.domposable.ui.DomposableIconButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeIconButton(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableTextField(
    props: io.github.samuolis.domposable.ui.DomposableTextFieldProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeTextField(props, modifier)
}

@Composable
internal actual fun RenderDomposableCheckbox(
    props: io.github.samuolis.domposable.ui.DomposableCheckboxProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeCheckbox(props, modifier)
}

@Composable
internal actual fun RenderDomposableSwitch(
    props: io.github.samuolis.domposable.ui.DomposableSwitchProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeSwitch(props, modifier)
}

@Composable
internal actual fun RenderDomposableRadioButton(
    props: io.github.samuolis.domposable.ui.DomposableRadioButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeRadioButton(props, modifier)
}

@Composable
internal actual fun RenderDomposableSlider(
    props: io.github.samuolis.domposable.ui.DomposableSliderProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.colors ?: LocalDomposableTokens.current.components.slider
    val enabled = props.enabled && domposableFormEnabled()
    val changeValue: (Float) -> Unit = { if (enabled) props.onValueChange(it) }
    val sliderModifier =
        modifier.toComposeModifier().materialKeyboardFocusRing().semantics {
            if (props.label.isNotBlank()) contentDescription = props.label
        }
    if (props.orientation == DomposableOrientation.Vertical) {
        val state =
            remember(
                props.range,
                props.steps,
            ) { androidx.compose.material3.SliderState(props.value, props.steps, valueRange = props.range) }
        state.onValueChange = changeValue
        LaunchedEffect(props.value, state) { if (state.value != props.value) state.value = props.value }
        androidx.compose.material3.VerticalSlider(
            state = state,
            modifier = sliderModifier,
            enabled = enabled,
            reverseDirection = !props.topToBottom,
            colors = appearance.nativeSliderColors(),
        )
    } else {
        Slider(
            props.value,
            changeValue,
            sliderModifier,
            enabled = enabled,
            valueRange = props.range,
            steps = props.steps,
            colors = appearance.nativeSliderColors(),
        )
    }
}

@Composable
internal actual fun RenderDomposableCard(
    props: io.github.samuolis.domposable.ui.DomposableCardProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeCard(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableSurface(
    props: io.github.samuolis.domposable.ui.DomposableSurfaceProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeSurface(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableImage(
    props: io.github.samuolis.domposable.ui.DomposableImageProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    AsyncImage(
        contentScale =
            when (props.scale) {
                DomposableImageScale.Fit -> ContentScale.Fit
                DomposableImageScale.Crop -> ContentScale.Crop
                DomposableImageScale.Fill -> ContentScale.FillBounds
            },
        model = props.source,
        contentDescription = props.description,
        modifier = modifier.toComposeModifier(),
        onSuccess = {
            props.onLoaded?.invoke()
        },
        onError = { props.onError?.invoke() },
    )
}

@Composable
internal actual fun RenderDomposableIcon(
    props: io.github.samuolis.domposable.ui.DomposableIconProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeIcon(props, modifier)
}

@Composable
internal actual fun RenderDomposableDivider(
    props: io.github.samuolis.domposable.ui.DomposableDividerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeDivider(props, modifier)
}

@Composable
internal actual fun RenderDomposableSpacer(
    props: io.github.samuolis.domposable.ui.DomposableSpacerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    Spacer(modifier.toComposeModifier().size(props.length.value.dp))
}

@Composable
internal actual fun RenderDomposableBadge(
    props: io.github.samuolis.domposable.ui.DomposableBadgeProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeBadge(props, modifier)
}

@Composable
internal actual fun RenderDomposableChip(
    props: io.github.samuolis.domposable.ui.DomposableChipProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeChip(props, modifier)
}

@Composable
internal actual fun RenderDomposableAvatar(
    props: io.github.samuolis.domposable.ui.DomposableAvatarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeAvatar(props, modifier)
}

@Composable
internal actual fun RenderDomposableBanner(
    props: io.github.samuolis.domposable.ui.DomposableBannerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeBanner(props, modifier)
}

@Composable
internal actual fun RenderDomposableProgress(
    props: io.github.samuolis.domposable.ui.DomposableProgressProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeProgress(props, modifier)
}

@Composable
internal actual fun RenderDomposableTabs(
    props: io.github.samuolis.domposable.ui.DomposableTabsProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeTabs(props, modifier)
}

@Composable
internal actual fun RenderDomposableNavigationBar(
    props: io.github.samuolis.domposable.ui.DomposableNavigationBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeNavigationBar(props, modifier)
}

@Composable
internal actual fun RenderDomposableMenu(
    props: io.github.samuolis.domposable.ui.DomposableMenuProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeMenu(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableDialog(
    props: io.github.samuolis.domposable.ui.DomposableDialogProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeDialog(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableBottomSheet(
    props: io.github.samuolis.domposable.ui.DomposableBottomSheetProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeBottomSheet(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableTopAppBar(
    props: io.github.samuolis.domposable.ui.DomposableTopAppBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeTopAppBar(props, modifier)
}

@Composable
internal actual fun RenderDomposableFloatingActionButton(
    props: io.github.samuolis.domposable.ui.DomposableFloatingActionButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeFloatingActionButton(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableSnackbar(
    props: io.github.samuolis.domposable.ui.DomposableSnackbarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeSnackbar(props, modifier)
}

@Composable
internal actual fun RenderDomposableSelect(
    props: io.github.samuolis.domposable.ui.DomposableSelectProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeSelect(props, modifier)
}

@Composable
internal actual fun RenderDomposableSegmentedButtons(
    props: io.github.samuolis.domposable.ui.DomposableSegmentedButtonsProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeSegmentedButtons(props, modifier)
}

@Composable
internal actual fun RenderDomposableRangeSlider(
    props: io.github.samuolis.domposable.ui.DomposableRangeSliderProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeRangeSlider(props, modifier)
}

@Composable
internal actual fun RenderDomposableTooltip(
    props: io.github.samuolis.domposable.ui.DomposableTooltipProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeTooltip(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableScaffold(
    props: io.github.samuolis.domposable.ui.DomposableScaffoldProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.scaffold
    val shape = appearance.nativeShape(androidx.compose.ui.graphics.RectangleShape)
    Surface(
        modifier = modifier.toComposeModifier().fillMaxSize(),
        color = appearance.nativeContainer(MaterialTheme.colorScheme.background),
        contentColor = appearance.nativeContent(MaterialTheme.colorScheme.onBackground),
        shape = shape,
        tonalElevation = appearance.tonalElevation?.value?.dp ?: 0.dp,
        shadowElevation = appearance.shadowElevation?.value?.dp ?: 0.dp,
        border =
            appearance.border?.let {
                androidx.compose.foundation.BorderStroke(appearance.borderWidth.value.dp, it.nativeColor())
            },
    ) {
        StyledContent(appearance.textAppearance) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { Column { props.topBar() } },
                bottomBar = { Column { props.bottomBar() } },
                snackbarHost = props.snackbar,
                floatingActionButton = props.floatingAction,
                floatingActionButtonPosition =
                    when (props.floatingActionPosition) {
                        DomposableScaffoldFabPosition.Start -> FabPosition.Start
                        DomposableScaffoldFabPosition.Center -> FabPosition.Center
                        DomposableScaffoldFabPosition.End -> FabPosition.End
                    },
                containerColor = Color.Transparent,
                contentColor = appearance.nativeContent(MaterialTheme.colorScheme.onBackground),
                contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
            ) { padding ->
                Row(Modifier.fillMaxSize().padding(padding)) {
                    ProvideDomposableRowModifierScope {
                        props.navigation()
                        Box(Modifier.weight(1f).fillMaxHeight()) {
                            ProvideDomposableBoxModifierScope { content() }
                        }
                        props.contextual()
                    }
                }
                props.overlays()
            }
        }
    }
}

@Composable
internal actual fun RenderDomposableAppBar(
    props: io.github.samuolis.domposable.ui.DomposableAppBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeAppBar(props, modifier)
}

@Composable
internal actual fun RenderDomposableAutocomplete(
    props: io.github.samuolis.domposable.ui.DomposableAutocompleteProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeAutocomplete(props, modifier)
}

@Composable
internal actual fun RenderDomposableBottomAppBar(
    props: io.github.samuolis.domposable.ui.DomposableBottomAppBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeBottomAppBar(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableBottomSheetScaffold(
    props: io.github.samuolis.domposable.ui.DomposableBottomSheetScaffoldProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeBottomSheetScaffold(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableButtonGroup(
    props: io.github.samuolis.domposable.ui.DomposableButtonGroupProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeButtonGroup(props, modifier)
}

@Composable
internal actual fun RenderDomposableCarousel(
    props: io.github.samuolis.domposable.ui.DomposableCarouselProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeCarousel(props, modifier)
}

@Composable
internal actual fun RenderDomposableDatePicker(
    props: io.github.samuolis.domposable.ui.DomposableDatePickerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeDatePicker(props, modifier)
}

@Composable
internal actual fun RenderDomposableDateRangePicker(
    props: io.github.samuolis.domposable.ui.DomposableDateRangePickerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeDateRangePicker(props, modifier)
}

@Composable
internal actual fun RenderDomposableDragHandle(
    props: io.github.samuolis.domposable.ui.DomposableDragHandleProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeDragHandle(props, modifier)
}

@Composable
internal actual fun RenderDomposableFloatingActionButtonMenu(
    props: io.github.samuolis.domposable.ui.DomposableFloatingActionButtonMenuProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeFloatingActionButtonMenu(props, modifier)
}

@Composable
internal actual fun RenderDomposableFloatingToolbar(
    props: io.github.samuolis.domposable.ui.DomposableFloatingToolbarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeFloatingToolbar(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableListItem(
    props: io.github.samuolis.domposable.ui.DomposableListItemProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeListItem(props, modifier)
}

@Composable
internal actual fun RenderDomposableLoadingIndicator(
    props: io.github.samuolis.domposable.ui.DomposableLoadingIndicatorProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeLoadingIndicator(props, modifier)
}

@Composable
internal actual fun RenderDomposableMultiChoiceSegmentedButtons(
    props: io.github.samuolis.domposable.ui.DomposableMultiChoiceSegmentedButtonsProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeMultiChoiceSegmentedButtons(props, modifier)
}

@Composable
internal actual fun RenderDomposableNavigationDrawer(
    props: io.github.samuolis.domposable.ui.DomposableNavigationDrawerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeNavigationDrawer(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableNavigationDrawerItem(
    props: io.github.samuolis.domposable.ui.DomposableNavigationDrawerItemProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeNavigationDrawerItem(props, modifier)
}

@Composable
internal actual fun RenderDomposablePullToRefresh(
    props: io.github.samuolis.domposable.ui.DomposablePullToRefreshProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativePullToRefresh(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableSearchBar(
    props: io.github.samuolis.domposable.ui.DomposableSearchBarProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeSearchBar(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableSplitButton(
    props: io.github.samuolis.domposable.ui.DomposableSplitButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeSplitButton(props, modifier)
}

@Composable
internal actual fun RenderDomposableSwipeToDismiss(
    props: io.github.samuolis.domposable.ui.DomposableSwipeToDismissProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeSwipeToDismiss(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableTimePicker(
    props: io.github.samuolis.domposable.ui.DomposableTimePickerProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeTimePicker(props, modifier)
}

@Composable
internal actual fun RenderDomposableToggleButton(
    props: io.github.samuolis.domposable.ui.DomposableToggleButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeToggleButton(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableWideNavigationRail(
    props: io.github.samuolis.domposable.ui.DomposableWideNavigationRailProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeWideNavigationRail(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableWideNavigationRailItem(
    props: io.github.samuolis.domposable.ui.DomposableWideNavigationRailItemProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeWideNavigationRailItem(props, modifier)
}

@Composable
internal actual fun RenderDomposableBadgedBox(
    props: io.github.samuolis.domposable.ui.DomposableBadgedBoxProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeBadgedBox(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableDropdownMenuItem(
    props: io.github.samuolis.domposable.ui.DomposableMenuItem,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeDropdownMenuItem(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableLabel(
    props: io.github.samuolis.domposable.ui.DomposableLabelProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeLabel(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableMenuGroup(
    props: io.github.samuolis.domposable.ui.DomposableMenuGroupProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeMenuGroup(props, modifier, content)
}

@Composable
internal actual fun RenderDomposableScrollField(
    props: io.github.samuolis.domposable.ui.DomposableScrollFieldProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeScrollField(props, modifier)
}

@Composable
internal actual fun RenderDomposableSnackbarHost(
    props: io.github.samuolis.domposable.ui.DomposableSnackbarHostProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeSnackbarHost(props, modifier)
}

@Composable
internal actual fun RenderDomposableScrim(
    props: io.github.samuolis.domposable.ui.DomposableScrimProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    NativeScrim(props, modifier, content)
}
