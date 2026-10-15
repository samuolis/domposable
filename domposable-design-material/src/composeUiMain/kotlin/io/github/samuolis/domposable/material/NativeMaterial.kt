@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.columns
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Label
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.lightColorScheme
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
import io.github.samuolis.domposable.foundation.DomposableTextAlignment
import io.github.samuolis.domposable.foundation.DomposableTextDecoration
import io.github.samuolis.domposable.foundation.DomposableTextOverflow
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.ui.DomposableComponents
import io.github.samuolis.domposable.ui.DomposableDataTableProps
import io.github.samuolis.domposable.ui.DomposableLineChartProps
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

private fun DomposableAlignment.box() =
    when (this) {
        DomposableAlignment.Start -> Alignment.TopStart
        DomposableAlignment.Center -> Alignment.Center
        DomposableAlignment.End -> Alignment.BottomEnd
    }

internal fun DomposableFontWeight.native() =
    when (this) {
        DomposableFontWeight.Thin -> FontWeight.Thin
        DomposableFontWeight.ExtraLight -> FontWeight.ExtraLight
        DomposableFontWeight.Light -> FontWeight.Light
        DomposableFontWeight.Normal -> FontWeight.Normal
        DomposableFontWeight.Medium -> FontWeight.Medium
        DomposableFontWeight.SemiBold -> FontWeight.SemiBold
        DomposableFontWeight.Bold -> FontWeight.Bold
        DomposableFontWeight.ExtraBold -> FontWeight.ExtraBold
        DomposableFontWeight.Black -> FontWeight.Black
    }

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

private fun DomposableLiveRegion.native() =
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
actual fun rememberDomposableMaterialComponents(): DomposableComponents =
    remember {
        DomposableComponents(
            form = { props, modifier, content -> NativeDomposableForm(props, modifier, content) },
            disclosure = { props, modifier, content -> NativeDomposableDisclosure(props, modifier, content) },
            richHtml = { props, modifier, _ -> NativeDomposableRichHtml(props, modifier) },
            theme = { props, _, content ->
                val colors = props.tokens.colors
                val type = props.tokens.typography
                MaterialTheme(
                    colorScheme =
                        lightColorScheme(
                            primary = Color(colors.primary.argb),
                            onPrimary = Color(colors.onPrimary.argb),
                            surface = Color(colors.surface.argb),
                            onSurface = Color(colors.onSurface.argb),
                            surfaceVariant = Color(colors.surfaceVariant.argb),
                            outline = Color(colors.outline.argb),
                            error = Color(colors.error.argb),
                            background = Color(colors.surface.argb),
                            surfaceContainer = Color(colors.surfaceVariant.argb),
                            surfaceContainerHighest = Color(colors.surfaceVariant.argb),
                            secondaryContainer = Color(colors.surfaceVariant.argb),
                            onSecondaryContainer = Color(colors.onSurface.argb),
                        ),
                    typography =
                        Typography().let {
                            it.copy(
                                bodyLarge =
                                    it.bodyLarge.copy(
                                        fontSize = type.bodySize.sp,
                                        lineHeight =
                                            (
                                                type.bodySize *
                                                    type.lineHeight
                                            ).sp,
                                    ),
                                titleLarge = it.titleLarge.copy(fontSize = type.titleSize.sp),
                                labelLarge = it.labelLarge.copy(fontSize = type.labelSize.sp),
                            )
                        },
                    content = content,
                )
            },
            column = {
                props,
                modifier,
                content,
                ->
                Column(
                    modifier.toComposeModifier(),
                    verticalArrangement = Arrangement.spacedBy(props.gap.value.dp),
                    horizontalAlignment = props.alignment.horizontal(),
                ) {
                    ProvideDomposableColumnModifierScope { content() }
                }
            },
            row = {
                props,
                modifier,
                content,
                ->
                Row(
                    modifier.toComposeModifier(),
                    horizontalArrangement = Arrangement.spacedBy(props.gap.value.dp),
                    verticalAlignment = props.alignment.vertical(),
                ) {
                    ProvideDomposableRowModifierScope { content() }
                }
            },
            flowRow = {
                props,
                modifier,
                content,
                ->
                FlowRow(
                    modifier.toComposeModifier(),
                    horizontalArrangement = Arrangement.spacedBy(props.gap.value.dp),
                    verticalArrangement = Arrangement.spacedBy(props.gap.value.dp),
                ) {
                    ProvideDomposableRowModifierScope { content() }
                }
            },
            link = { props, modifier, _ -> NativeDomposableLink(props, modifier) },
            box = {
                props,
                modifier,
                content,
                ->
                Box(modifier.toComposeModifier(), contentAlignment = props.alignment.box()) {
                    ProvideDomposableBoxModifierScope { content() }
                }
            },
            lazyColumn = {
                props,
                modifier,
                _,
                ->
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
            },
            lazyRow = {
                props,
                modifier,
                _,
                ->
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
            },
            lazyGrid = {
                props,
                modifier,
                _,
                ->
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
            },
            table = { props, modifier, _ -> NativeTable(props, modifier) },
            lineChart = { props, modifier, _ -> NativeLineChart(props, modifier) },
            valueChart = { props, modifier, _ -> NativeValueChart(props, modifier) },
            dataTable = { props, modifier, _ -> NativeDataTable(props, modifier) },
            text = { props, modifier, _ ->
                val appearance = props.appearance ?: LocalMaterialButtonTextAppearance.current
                val style =
                    when (props.style) {
                        DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge
                        DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge
                        DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge
                    }.copy(
                        fontSize =
                            appearance?.fontSize?.sp ?: when (props.style) {
                                DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge.fontSize
                                DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge.fontSize
                                DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge.fontSize
                            },
                        lineHeight =
                            appearance?.lineHeight?.sp
                                ?: when (props.style) {
                                    DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge.lineHeight
                                    DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge.lineHeight
                                    DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge.lineHeight
                                },
                        fontWeight =
                            appearance?.fontWeight?.native()
                                ?: when (props.style) {
                                    DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge.fontWeight
                                    DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge.fontWeight
                                    DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge.fontWeight
                                },
                        letterSpacing =
                            appearance?.letterSpacing?.sp ?: when (props.style) {
                                DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge.letterSpacing
                                DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge.letterSpacing
                                DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge.letterSpacing
                            },
                        textAlign =
                            appearance?.alignment?.native()
                                ?: when (props.style) {
                                    DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge.textAlign
                                    DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge.textAlign
                                    DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge.textAlign
                                },
                        textDecoration =
                            appearance?.decoration?.native()
                                ?: when (props.style) {
                                    DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge.textDecoration
                                    DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge.textDecoration
                                    DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge.textDecoration
                                },
                    )
                val textModifier =
                    modifier.toComposeModifier().semantics {
                        if (props.headingLevel !=
                            null ||
                            props.style == DomposableTextStyle.Title
                        ) {
                            heading()
                        }
                        appearance?.liveRegion?.let { liveRegion = it.native() }
                    }

                val renderText: @Composable () -> Unit = {
                    Text(
                        props.text,
                        textModifier,
                        color = props.color?.let { Color(it.argb) } ?: Color.Unspecified,
                        style = style,
                        maxLines = appearance?.maxLines ?: Int.MAX_VALUE,
                        overflow = appearance?.overflow?.native() ?: TextOverflow.Clip,
                    )
                }
                if (appearance?.selectable == true) SelectionContainer { renderText() } else renderText()
            },
            button = { props, modifier, content -> NativeButton(props, modifier, content) },
            iconButton = { props, modifier, content ->
                IconButton(
                    props.onClick,
                    modifier.toComposeModifier().semantics {
                        contentDescription =
                            props.label
                    },
                    enabled = props.enabled && domposableFormEnabled(),
                    content = content,
                )
            },
            textField = { props, modifier, _ -> NativeTextField(props, modifier) },
            checkbox = {
                props,
                modifier,
                _,
                ->
                Row(
                    modifier
                        .toComposeModifier()
                        .toggleable(
                            props.checked,
                            enabled = props.enabled && domposableFormEnabled(),
                            role = Role.Checkbox,
                            onValueChange = props.onCheckedChange,
                        ).semantics(mergeDescendants = true) {
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(props.checked, null, enabled = props.enabled && domposableFormEnabled())
                    Text(props.label)
                }
            },
            switch = {
                props,
                modifier,
                _,
                ->
                Row(
                    modifier
                        .toComposeModifier()
                        .toggleable(
                            props.checked,
                            enabled = props.enabled && domposableFormEnabled(),
                            role = Role.Switch,
                            onValueChange = props.onCheckedChange,
                        ).semantics(mergeDescendants = true) {
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Switch(props.checked, null, enabled = props.enabled && domposableFormEnabled())
                    Text(props.label)
                }
            },
            radioButton = { props, modifier, _ ->
                Row(
                    modifier
                        .toComposeModifier()
                        .clickable(
                            enabled = props.enabled && domposableFormEnabled(),
                            role = Role.RadioButton,
                            onClick = props.onClick,
                        ).semantics(mergeDescendants = true) {
                            selected =
                                props.selected
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(props.selected, null, enabled = props.enabled && domposableFormEnabled())
                    Text(props.label)
                }
            },
            slider = { props, modifier, _ ->
                Slider(
                    props.value,
                    props.onValueChange,
                    modifier.toComposeModifier().semantics {
                        contentDescription =
                            props.label
                    },
                    enabled = props.enabled && domposableFormEnabled(),
                    valueRange = props.range,
                    steps = props.steps,
                )
            },
            card = { props, modifier, content ->
                val shape = RoundedCornerShape(LocalDomposableTokens.current.radius.value.dp)
                if (props.onClick != null) {
                    Card(props.onClick!!, modifier.toComposeModifier(), shape = shape) { content() }
                } else {
                    Card(modifier.toComposeModifier(), shape = shape) { content() }
                }
            },
            surface = { props, modifier, content ->
                Surface(
                    modifier.toComposeModifier(),
                    color =
                        props.color?.let { Color(it.argb) } ?: MaterialTheme.colorScheme.surface,
                    content = content,
                )
            },
            image = {
                props,
                modifier,
                _,
                ->
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
            },
            icon = { props, modifier, _ ->
                Text(
                    props.symbol,
                    modifier.toComposeModifier().clearAndSetSemantics {
                        props.description?.let { contentDescription = it }
                    },
                )
            },
            divider = { props, modifier, _ ->
                if (props.orientation ==
                    DomposableOrientation.Horizontal
                ) {
                    HorizontalDivider(modifier.toComposeModifier())
                } else {
                    VerticalDivider(modifier.toComposeModifier())
                }
            },
            spacer = { props, modifier, _ -> Spacer(modifier.toComposeModifier().size(props.length.value.dp)) },
            badge = { props, modifier, _ -> Badge(modifier.toComposeModifier()) { Text(props.text) } },
            chip = {
                props,
                modifier,
                _,
                ->
                FilterChip(props.selected, props.onClick, label = {
                    Text(props.label)
                }, modifier.toComposeModifier(), enabled = props.enabled && domposableFormEnabled())
            },
            avatar = { props, modifier, _ ->
                Surface(
                    modifier.toComposeModifier().size(40.dp).semantics {
                        contentDescription =
                            props.description
                    },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) { Box(contentAlignment = Alignment.Center) { Text(props.initials) } }
            },
            banner = { props, modifier, _ ->
                Text(
                    props.message,
                    modifier.toComposeModifier().semantics {
                        liveRegion =
                            LiveRegionMode.Polite
                    },
                    color = if (props.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            },
            progress = { props, modifier, _ ->
                val native = modifier.toComposeModifier().semantics { contentDescription = props.label }
                if (props.circular) {
                    if (props.value ==
                        null
                    ) {
                        CircularProgressIndicator(native)
                    } else {
                        CircularProgressIndicator(progress = { props.value!! }, modifier = native)
                    }
                } else {
                    if (props.value ==
                        null
                    ) {
                        LinearProgressIndicator(native)
                    } else {
                        LinearProgressIndicator(progress = { props.value!! }, modifier = native)
                    }
                }
            },
            tabs = { props, modifier, _ ->
                if (props.labels.isNotEmpty()) {
                    PrimaryTabRow(props.selectedIndex, modifier.toComposeModifier()) {
                        props.labels.forEachIndexed { index, label ->
                            Tab(props.selectedIndex == index, { props.onSelect(index) }, text = { Text(label) })
                        }
                    }
                }
            },
            navigationBar = { props, modifier, _ ->
                if (props.orientation ==
                    DomposableOrientation.Horizontal
                ) {
                    NavigationBar(modifier.toComposeModifier()) {
                        props.items.forEachIndexed { index, item ->
                            NavigationBarItem(props.selectedIndex == index, {
                                props.onSelect(index)
                            }, icon = { Text(item.symbol) }, label = { Text(item.label) })
                        }
                    }
                } else {
                    NavigationRail(modifier.toComposeModifier()) {
                        props.items.forEachIndexed { index, item ->
                            NavigationRailItem(props.selectedIndex == index, {
                                props.onSelect(index)
                            }, icon = { Text(item.symbol) }, label = { Text(item.label) })
                        }
                    }
                }
            },
            menu = {
                props,
                modifier,
                _,
                ->
                NativeWindow {
                    DropdownMenu(props.expanded, props.onDismiss, modifier.toComposeModifier()) {
                        props.items.forEach { item ->
                            DropdownMenuItem(text = { Text(item.label) }, onClick = {
                                item.onClick()
                                props.onDismiss()
                            }, enabled = item.enabled)
                        }
                    }
                }
            },
            dialog = {
                props,
                modifier,
                content,
                ->
                NativeDialog(props, modifier, content)
            },
            bottomSheet = { props, modifier, content -> NativeBottomSheet(props, modifier, content) },
            topAppBar = { props, modifier, _ -> NativeTopAppBar(props, modifier) },
            floatingActionButton = { props, modifier, content -> NativeFloatingActionButton(props, modifier, content) },
            snackbar = { props, modifier, _ -> NativeSnackbar(props, modifier) },
            select = { props, modifier, _ -> NativeSelect(props, modifier) },
            segmentedButtons = { props, modifier, _ -> NativeSegmentedButtons(props, modifier) },
            rangeSlider = { props, modifier, _ -> NativeRangeSlider(props, modifier) },
            tooltip = { props, modifier, content -> NativeTooltip(props, modifier, content) },
            scaffold = { props, modifier, content ->
                Surface(modifier.toComposeModifier().fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(Modifier.fillMaxSize()) {
                        props.topBar()
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            props.navigation()
                            Box(Modifier.weight(1f).fillMaxHeight()) {
                                content()
                                Box(Modifier.align(Alignment.BottomEnd)) { props.floatingAction() }
                                Box(Modifier.align(Alignment.BottomCenter)) { props.snackbar() }
                            }
                            props.contextual()
                        }
                        props.bottomBar()
                        props.overlays()
                    }
                }
            },
        )
    }
