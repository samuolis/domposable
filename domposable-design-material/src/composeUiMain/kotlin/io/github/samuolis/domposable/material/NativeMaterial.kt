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
actual fun rememberDomposableMaterialComponents(): DomposableComponents =
    remember {
        DomposableComponents(
            form = { props, modifier, content -> NativeDomposableForm(props, modifier, content) },
            disclosure = { props, modifier, content -> NativeDomposableDisclosure(props, modifier, content) },
            richHtml = { props, modifier, _ -> NativeDomposableRichHtml(props, modifier) },
            theme = { props, _, content ->
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
            },
            column = {
                props,
                modifier,
                content,
                ->
                Column(
                    modifier.toComposeModifier(),
                    verticalArrangement = props.arrangement.vertical(props.gap.value.dp),
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
                val rowModifier =
                    modifier.toComposeModifier().let { if (props.stretch) it.fillMaxHeight() else it }
                Row(
                    rowModifier,
                    horizontalArrangement = props.arrangement.horizontal(props.gap.value.dp),
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
                    horizontalArrangement = props.arrangement.horizontal(props.gap.value.dp),
                    verticalArrangement = Arrangement.spacedBy(props.rowGap.value.dp),
                    itemVerticalAlignment = props.alignment.vertical(),
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
                Box(modifier.toComposeModifier(), contentAlignment = props.alignment.box(props.verticalAlignment)) {
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
            },
            button = { props, modifier, content -> NativeButton(props, modifier, content) },
            iconButton = { props, modifier, content ->
                NativeIconButton(props, modifier, content)
            },
            textField = { props, modifier, _ -> NativeTextField(props, modifier) },
            checkbox = {
                props,
                modifier,
                _,
                ->
                NativeCheckbox(props, modifier)
            },
            switch = {
                props,
                modifier,
                _,
                ->
                NativeSwitch(props, modifier)
            },
            radioButton = { props, modifier, _ -> NativeRadioButton(props, modifier) },
            slider = { props, modifier, _ ->
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
            },
            card = { props, modifier, content ->
                NativeCard(props, modifier, content)
            },
            surface = { props, modifier, content ->
                NativeSurface(props, modifier, content)
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
                NativeIcon(props, modifier)
            },
            divider = { props, modifier, _ ->
                NativeDivider(props, modifier)
            },
            spacer = { props, modifier, _ -> Spacer(modifier.toComposeModifier().size(props.length.value.dp)) },
            badge = { props, modifier, _ -> NativeBadge(props, modifier) },
            chip = { props, modifier, _ -> NativeChip(props, modifier) },
            avatar = { props, modifier, _ -> NativeAvatar(props, modifier) },
            banner = { props, modifier, _ -> NativeBanner(props, modifier) },
            progress = { props, modifier, _ -> NativeProgress(props, modifier) },
            tabs = { props, modifier, _ -> NativeTabs(props, modifier) },
            navigationBar = { props, modifier, _ -> NativeNavigationBar(props, modifier) },
            menu = { props, modifier, content -> NativeMenu(props, modifier, content) },
            dialog = { props, modifier, content -> NativeDialog(props, modifier, content) },
            bottomSheet = { props, modifier, content -> NativeBottomSheet(props, modifier, content) },
            topAppBar = { props, modifier, _ -> NativeTopAppBar(props, modifier) },
            floatingActionButton = { props, modifier, content -> NativeFloatingActionButton(props, modifier, content) },
            snackbar = { props, modifier, _ -> NativeSnackbar(props, modifier) },
            select = { props, modifier, _ -> NativeSelect(props, modifier) },
            segmentedButtons = { props, modifier, _ -> NativeSegmentedButtons(props, modifier) },
            rangeSlider = { props, modifier, _ -> NativeRangeSlider(props, modifier) },
            tooltip = { props, modifier, content -> NativeTooltip(props, modifier, content) },
            scaffold = { props, modifier, content ->
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
            },
            appBar = { props, modifier, _ -> NativeAppBar(props, modifier) },
            autocomplete = { props, modifier, _ -> NativeAutocomplete(props, modifier) },
            bottomAppBar = { props, modifier, content -> NativeBottomAppBar(props, modifier, content) },
            bottomSheetScaffold = { props, modifier, content -> NativeBottomSheetScaffold(props, modifier, content) },
            buttonGroup = { props, modifier, _ -> NativeButtonGroup(props, modifier) },
            carousel = { props, modifier, _ -> NativeCarousel(props, modifier) },
            datePicker = { props, modifier, _ -> NativeDatePicker(props, modifier) },
            dateRangePicker = { props, modifier, _ -> NativeDateRangePicker(props, modifier) },
            dragHandle = { props, modifier, _ -> NativeDragHandle(props, modifier) },
            floatingActionButtonMenu = { props, modifier, _ -> NativeFloatingActionButtonMenu(props, modifier) },
            floatingToolbar = { props, modifier, content -> NativeFloatingToolbar(props, modifier, content) },
            listItem = { props, modifier, _ -> NativeListItem(props, modifier) },
            loadingIndicator = { props, modifier, _ -> NativeLoadingIndicator(props, modifier) },
            multiChoiceSegmentedButtons = { props, modifier, _ -> NativeMultiChoiceSegmentedButtons(props, modifier) },
            navigationDrawer = { props, modifier, content -> NativeNavigationDrawer(props, modifier, content) },
            navigationDrawerItem = { props, modifier, _ -> NativeNavigationDrawerItem(props, modifier) },
            pullToRefresh = { props, modifier, content -> NativePullToRefresh(props, modifier, content) },
            searchBar = { props, modifier, content -> NativeSearchBar(props, modifier, content) },
            splitButton = { props, modifier, _ -> NativeSplitButton(props, modifier) },
            swipeToDismiss = { props, modifier, content -> NativeSwipeToDismiss(props, modifier, content) },
            timePicker = { props, modifier, _ -> NativeTimePicker(props, modifier) },
            toggleButton = { props, modifier, content -> NativeToggleButton(props, modifier, content) },
            wideNavigationRail = { props, modifier, content -> NativeWideNavigationRail(props, modifier, content) },
            wideNavigationRailItem = { props, modifier, _ -> NativeWideNavigationRailItem(props, modifier) },
            badgedBox = { props, modifier, content -> NativeBadgedBox(props, modifier, content) },
            dropdownMenuItem = { props, modifier, content -> NativeDropdownMenuItem(props, modifier, content) },
            label = { props, modifier, content -> NativeLabel(props, modifier, content) },
            menuGroup = { props, modifier, content -> NativeMenuGroup(props, modifier, content) },
            scrollField = { props, modifier, _ -> NativeScrollField(props, modifier) },
            snackbarHost = { props, modifier, _ -> NativeSnackbarHost(props, modifier) },
            scrim = { props, modifier, content -> NativeScrim(props, modifier, content) },
        )
    }
