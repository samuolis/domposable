@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package io.github.samuolis.domposable.material

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.ui.*

private fun DomposableAlignment.horizontal() = when (this) {
    DomposableAlignment.Start -> Alignment.Start
    DomposableAlignment.Center -> Alignment.CenterHorizontally
    DomposableAlignment.End -> Alignment.End
}
private fun DomposableAlignment.vertical() = when (this) {
    DomposableAlignment.Start -> Alignment.Top
    DomposableAlignment.Center -> Alignment.CenterVertically
    DomposableAlignment.End -> Alignment.Bottom
}
private fun DomposableAlignment.box() = when (this) {
    DomposableAlignment.Start -> Alignment.TopStart
    DomposableAlignment.Center -> Alignment.Center
    DomposableAlignment.End -> Alignment.BottomEnd
}

@Composable
actual fun rememberDomposableMaterialComponents(): DomposableComponents = remember {
    DomposableComponents(
        theme = { props, _, content ->
            val colors = props.tokens.colors
            val type = props.tokens.typography
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(colors.primary.argb), onPrimary = Color(colors.onPrimary.argb),
                    surface = Color(colors.surface.argb), onSurface = Color(colors.onSurface.argb),
                    surfaceVariant = Color(colors.surfaceVariant.argb), outline = Color(colors.outline.argb),
                    error = Color(colors.error.argb), background = Color(colors.surface.argb),
                ),
                typography = Typography().let { it.copy(
                    bodyLarge = it.bodyLarge.copy(fontSize = type.bodySize.sp, lineHeight = (type.bodySize * type.lineHeight).sp),
                    titleLarge = it.titleLarge.copy(fontSize = type.titleSize.sp),
                    labelLarge = it.labelLarge.copy(fontSize = type.labelSize.sp),
                ) },
                content = content,
            )
        },
        column = { props, modifier, content -> Column(modifier.toComposeModifier(), verticalArrangement = Arrangement.spacedBy(props.gap.value.dp), horizontalAlignment = props.alignment.horizontal()) { content() } },
        row = { props, modifier, content -> Row(modifier.toComposeModifier(), horizontalArrangement = Arrangement.spacedBy(props.gap.value.dp), verticalAlignment = props.alignment.vertical()) { content() } },
        box = { props, modifier, content -> Box(modifier.toComposeModifier(), contentAlignment = props.alignment.box()) { content() } },
        lazyColumn = { props, modifier, _ -> LazyColumn(modifier.toComposeModifier(), verticalArrangement = Arrangement.spacedBy(props.gap.value.dp)) { items(props.items, key = { it.key }) { it.content() } } },
        lazyRow = { props, modifier, _ -> LazyRow(modifier.toComposeModifier(), horizontalArrangement = Arrangement.spacedBy(props.gap.value.dp)) { items(props.items, key = { it.key }) { it.content() } } },
        lazyGrid = { props, modifier, _ -> LazyVerticalGrid(GridCells.Fixed(props.columns), modifier.toComposeModifier(), horizontalArrangement = Arrangement.spacedBy(props.gap.value.dp), verticalArrangement = Arrangement.spacedBy(props.gap.value.dp)) { items(props.items, key = { it.key }) { it.content() } } },
        text = { props, modifier, _ -> Text(props.text, modifier.toComposeModifier().semantics { if (props.headingLevel != null || props.style == DomposableTextStyle.Title) heading() }, color = props.color?.let { Color(it.argb) } ?: Color.Unspecified, style = when (props.style) { DomposableTextStyle.Body -> MaterialTheme.typography.bodyLarge; DomposableTextStyle.Title -> MaterialTheme.typography.titleLarge; DomposableTextStyle.Label -> MaterialTheme.typography.labelLarge }) },
        button = { props, modifier, content -> when (props.style) {
            DomposableButtonStyle.Filled -> Button(props.onClick, modifier.toComposeModifier(), enabled = props.enabled) { content() }
            DomposableButtonStyle.Outlined -> OutlinedButton(props.onClick, modifier.toComposeModifier(), enabled = props.enabled) { content() }
            DomposableButtonStyle.Text -> TextButton(props.onClick, modifier.toComposeModifier(), enabled = props.enabled) { content() }
        } },
        iconButton = { props, modifier, content -> IconButton(props.onClick, modifier.toComposeModifier().semantics { contentDescription = props.label }, enabled = props.enabled, content = content) },
        textField = { props, modifier, _ -> OutlinedTextField(
            props.value, props.onValueChange, modifier.toComposeModifier(), enabled = props.enabled,
            label = { Text(props.label) }, singleLine = !props.multiline, isError = props.error != null,
            supportingText = props.error?.let { message -> { Text(message) } },
            keyboardOptions = KeyboardOptions(keyboardType = when (props.purpose) {
                DomposableInputPurpose.Email -> KeyboardType.Email
                DomposableInputPurpose.Password -> KeyboardType.Password
                DomposableInputPurpose.Number -> KeyboardType.Number
                else -> KeyboardType.Text
            }),
            visualTransformation = if (props.purpose == DomposableInputPurpose.Password) PasswordVisualTransformation() else VisualTransformation.None,
        ) },
        checkbox = { props, modifier, _ -> Row(modifier.toComposeModifier().toggleable(props.checked, enabled = props.enabled, role = Role.Checkbox, onValueChange = props.onCheckedChange).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) { Checkbox(props.checked, null, enabled = props.enabled); Text(props.label) } },
        switch = { props, modifier, _ -> Row(modifier.toComposeModifier().toggleable(props.checked, enabled = props.enabled, role = Role.Switch, onValueChange = props.onCheckedChange).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) { Switch(props.checked, null, enabled = props.enabled); Text(props.label) } },
        radioButton = { props, modifier, _ -> Row(modifier.toComposeModifier().clickable(enabled = props.enabled, role = Role.RadioButton, onClick = props.onClick).semantics(mergeDescendants = true) { selected = props.selected }, verticalAlignment = Alignment.CenterVertically) { RadioButton(props.selected, null, enabled = props.enabled); Text(props.label) } },
        slider = { props, modifier, _ -> Slider(props.value, props.onValueChange, modifier.toComposeModifier().semantics { contentDescription = props.label }, enabled = props.enabled, valueRange = props.range, steps = props.steps) },
        card = { props, modifier, content ->
            val shape = RoundedCornerShape(LocalDomposableTokens.current.radius.value.dp)
            if (props.onClick != null) Card(props.onClick!!, modifier.toComposeModifier(), shape = shape) { content() }
            else Card(modifier.toComposeModifier(), shape = shape) { content() }
        },
        surface = { props, modifier, content -> Surface(modifier.toComposeModifier(), color = props.color?.let { Color(it.argb) } ?: MaterialTheme.colorScheme.surface, content = content) },
        image = { props, modifier, _ -> AsyncImage(model = props.source, contentDescription = props.description, modifier = modifier.toComposeModifier()) },
        icon = { props, modifier, _ -> Text(props.symbol, modifier.toComposeModifier().semantics { if (props.description != null) contentDescription = props.description!! else hideFromAccessibility() }) },
        divider = { props, modifier, _ -> if (props.orientation == DomposableOrientation.Horizontal) HorizontalDivider(modifier.toComposeModifier()) else VerticalDivider(modifier.toComposeModifier()) },
        spacer = { props, modifier, _ -> Spacer(modifier.toComposeModifier().size(props.length.value.dp)) },
        badge = { props, modifier, _ -> Badge(modifier.toComposeModifier()) { Text(props.text) } },
        chip = { props, modifier, _ -> FilterChip(props.selected, props.onClick, label = { Text(props.label) }, modifier.toComposeModifier(), enabled = props.enabled) },
        avatar = { props, modifier, _ -> Surface(modifier.toComposeModifier().size(40.dp).semantics { contentDescription = props.description }, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) { Box(contentAlignment = Alignment.Center) { Text(props.initials) } } },
        banner = { props, modifier, _ -> Text(props.message, modifier.toComposeModifier().semantics { liveRegion = LiveRegionMode.Polite }, color = if (props.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface) },
        progress = { props, modifier, _ ->
            val native = modifier.toComposeModifier().semantics { contentDescription = props.label }
            if (props.circular) { if (props.value == null) CircularProgressIndicator(native) else CircularProgressIndicator(progress = { props.value!! }, modifier = native) }
            else { if (props.value == null) LinearProgressIndicator(native) else LinearProgressIndicator(progress = { props.value!! }, modifier = native) }
        },
        tabs = { props, modifier, _ -> if (props.labels.isNotEmpty()) PrimaryTabRow(props.selectedIndex, modifier.toComposeModifier()) { props.labels.forEachIndexed { index, label -> Tab(props.selectedIndex == index, { props.onSelect(index) }, text = { Text(label) }) } } },
        navigationBar = { props, modifier, _ ->
            if (props.orientation == DomposableOrientation.Horizontal) NavigationBar(modifier.toComposeModifier()) { props.items.forEachIndexed { index, item -> NavigationBarItem(props.selectedIndex == index, { props.onSelect(index) }, icon = { Text(item.symbol) }, label = { Text(item.label) }) } }
            else NavigationRail(modifier.toComposeModifier()) { props.items.forEachIndexed { index, item -> NavigationRailItem(props.selectedIndex == index, { props.onSelect(index) }, icon = { Text(item.symbol) }, label = { Text(item.label) }) } }
        },
        menu = { props, modifier, _ -> DropdownMenu(props.expanded, props.onDismiss, modifier.toComposeModifier()) { props.items.forEach { item -> DropdownMenuItem(text = { Text(item.label) }, onClick = { item.onClick(); props.onDismiss() }, enabled = item.enabled) } } },
        dialog = { props, modifier, content -> AlertDialog(onDismissRequest = props.onDismiss, confirmButton = props.actions, dismissButton = { TextButton(props.onDismiss) { Text("Close") } }, title = { Text(props.title) }, text = content, modifier = modifier.toComposeModifier()) },
        bottomSheet = { props, modifier, content -> ModalBottomSheet(onDismissRequest = props.onDismiss, modifier = modifier.toComposeModifier()) { Text(props.title, style = MaterialTheme.typography.titleLarge); content(); TextButton(props.onDismiss) { Text("Close") } } },
        scaffold = { props, modifier, content -> Column(modifier.toComposeModifier().fillMaxSize()) {
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
        } },
    )
}
