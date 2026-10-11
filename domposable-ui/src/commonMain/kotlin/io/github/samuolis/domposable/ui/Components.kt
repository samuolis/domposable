package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableComponent
import io.github.samuolis.domposable.foundation.DomposableImageScale
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableScreenState
import io.github.samuolis.domposable.foundation.DomposableSize
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.dp

data class DomposableLazyItem(
    val key: String,
    val content: @Composable () -> Unit,
)

data class DomposableNavigationItem(
    val label: String,
    val symbol: String = "",
    val href: String? = null,
)

data class DomposableMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

@DomposableComponent("Theme")
data class DomposableThemeProps(
    val tokens: DomposableTokens,
)

@DomposableComponent("Column")
data class DomposableColumnProps(
    val gap: DomposableLength = 0.dp,
    val alignment: DomposableAlignment = DomposableAlignment.Start,
)

@DomposableComponent("Row")
data class DomposableRowProps(
    val gap: DomposableLength = 0.dp,
    val alignment: DomposableAlignment = DomposableAlignment.Start,
)

@DomposableComponent("FlowRow")
data class DomposableFlowRowProps(
    val gap: DomposableLength = 8.dp,
)

/** A browser anchor on HTML; opens a URI with the platform handler on Compose UI. */
@DomposableComponent("Link")
data class DomposableLinkProps(
    val label: String,
    val href: String,
)

@DomposableComponent("Box")
data class DomposableBoxProps(
    val alignment: DomposableAlignment = DomposableAlignment.Start,
)

@DomposableComponent("LazyColumn")
data class DomposableLazyColumnProps(
    val items: List<DomposableLazyItem>,
    val gap: DomposableLength = 0.dp,
)

@DomposableComponent("LazyRow")
data class DomposableLazyRowProps(
    val items: List<DomposableLazyItem>,
    val gap: DomposableLength = 0.dp,
)

@DomposableComponent("LazyGrid")
data class DomposableLazyGridProps(
    val items: List<DomposableLazyItem>,
    val columns: Int = 2,
    val gap: DomposableLength = 8.dp,
)

private val HTML_HEADING_LEVELS = 1..6

@DomposableComponent("Text")
data class DomposableTextProps(
    val text: String,
    val style: DomposableTextStyle = DomposableTextStyle.Body,
    val color: DomposableColor? = null,
    val headingLevel: Int? = null,
) {
    init {
        require(headingLevel == null || headingLevel in HTML_HEADING_LEVELS)
    }
}

@DomposableComponent("Button")
data class DomposableButtonProps(
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val style: DomposableButtonStyle = DomposableButtonStyle.Filled,
)

@DomposableComponent("IconButton")
data class DomposableIconButtonProps(
    val onClick: () -> Unit,
    val label: String,
    val enabled: Boolean = true,
)

@DomposableComponent("TextField")
data class DomposableTextFieldProps(
    val value: String,
    val onValueChange: (String) -> Unit,
    val label: String,
    val purpose: DomposableInputPurpose = DomposableInputPurpose.Text,
    val enabled: Boolean = true,
    val multiline: Boolean = false,
    val error: String? = null,
)

@DomposableComponent("Checkbox")
data class DomposableCheckboxProps(
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val label: String,
    val enabled: Boolean = true,
)

@DomposableComponent("Switch")
data class DomposableSwitchProps(
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val label: String,
    val enabled: Boolean = true,
)

@DomposableComponent("RadioButton")
data class DomposableRadioButtonProps(
    val selected: Boolean,
    val onClick: () -> Unit,
    val label: String,
    val enabled: Boolean = true,
)

@DomposableComponent("Slider")
data class DomposableSliderProps(
    val value: Float,
    val onValueChange: (Float) -> Unit,
    val label: String,
    val range: ClosedFloatingPointRange<Float> =
        0f..1f,
    val steps: Int = 0,
    val enabled: Boolean = true,
)

@DomposableComponent("Card")
data class DomposableCardProps(
    val onClick: (() -> Unit)? = null,
)

@DomposableComponent("Surface")
data class DomposableSurfaceProps(
    val color: DomposableColor? = null,
)

@DomposableComponent("Image")
data class DomposableImageProps(
    val source: String,
    val description: String?,
    val onLoaded: (() -> Unit)? = null,
    val onError: (() -> Unit)? = null,
    val scale: DomposableImageScale = DomposableImageScale.Fit,
)

@DomposableComponent("Icon")
data class DomposableIconProps(
    val symbol: String,
    val description: String?,
)

@DomposableComponent("Divider")
data class DomposableDividerProps(
    val orientation: DomposableOrientation = DomposableOrientation.Horizontal,
)

@DomposableComponent("Spacer")
data class DomposableSpacerProps(
    val length: DomposableLength = 8.dp,
)

@DomposableComponent("Badge")
data class DomposableBadgeProps(
    val text: String,
)

@DomposableComponent("Chip")
data class DomposableChipProps(
    val label: String,
    val onClick: () -> Unit,
    val selected: Boolean = false,
    val enabled: Boolean = true,
)

@DomposableComponent("Avatar")
data class DomposableAvatarProps(
    val initials: String,
    val description: String,
)

@DomposableComponent("Banner")
data class DomposableBannerProps(
    val message: String,
    val isError: Boolean = false,
)

@DomposableComponent("Progress")
data class DomposableProgressProps(
    val value: Float? = null,
    val label: String = "Loading",
    val circular: Boolean = true,
)

@DomposableComponent("Tabs")
data class DomposableTabsProps(
    val labels: List<String>,
    val selectedIndex: Int,
    val onSelect: (Int) -> Unit,
)

@DomposableComponent("NavigationBar")
data class DomposableNavigationBarProps(
    val items: List<DomposableNavigationItem>,
    val selectedIndex: Int,
    val onSelect: (Int) -> Unit,
    val orientation: DomposableOrientation = DomposableOrientation.Horizontal,
)

@DomposableComponent("Menu")
data class DomposableMenuProps(
    val expanded: Boolean,
    val onDismiss: () -> Unit,
    val items: List<DomposableMenuItem>,
)

@DomposableComponent("Dialog")
data class DomposableDialogProps(
    val title: String,
    val onDismiss: () -> Unit,
    val actions: @Composable () -> Unit = {},
)

@DomposableComponent("BottomSheet")
data class DomposableBottomSheetProps(
    val title: String,
    val onDismiss: () -> Unit,
)

@DomposableComponent("Scaffold")
data class DomposableScaffoldProps(
    val topBar: @Composable () -> Unit = {},
    val bottomBar: @Composable () -> Unit = {},
    val navigation: @Composable () -> Unit = {},
    val contextual: @Composable () -> Unit = {},
    val floatingAction: @Composable () -> Unit = {},
    val snackbar: @Composable () -> Unit = {},
    val overlays: @Composable () -> Unit = {},
)

val LocalDomposableTokens = staticCompositionLocalOf { DomposableTokens() }
val LocalDomposableComponents =
    staticCompositionLocalOf<DomposableComponents> {
        error("Install DomposableTheme with a component provider before rendering Domposable UI")
    }

@Composable
fun DomposableTheme(
    tokens: DomposableTokens = DomposableTokens(),
    components: DomposableComponents,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDomposableTokens provides tokens, LocalDomposableComponents provides components) {
        components.theme.Render(DomposableThemeProps(tokens), DomposableModifier, content)
    }
}

@Composable fun DomposableText(
    text: String,
    modifier: DomposableModifier = DomposableModifier,
    style: DomposableTextStyle = DomposableTextStyle.Body,
    color: DomposableColor? = null,
    headingLevel: Int? = null,
) = DomposableText(DomposableTextProps(text, style, color, headingLevel), modifier)

@Composable fun DomposableButton(
    onClick: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    style: DomposableButtonStyle = DomposableButtonStyle.Filled,
    content: @Composable () -> Unit,
) = DomposableButton(DomposableButtonProps(onClick, enabled, style), modifier, content)

@Composable fun DomposableButton(
    text: String,
    onClick: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
) = DomposableButton(onClick, modifier, enabled) { DomposableText(text) }

@Composable fun DomposableColumn(
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    alignment: DomposableAlignment = DomposableAlignment.Start,
    content: @Composable () -> Unit,
) = DomposableColumn(DomposableColumnProps(gap, alignment), modifier, content)

@Composable fun DomposableRow(
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 0.dp,
    alignment: DomposableAlignment = DomposableAlignment.Start,
    content: @Composable () -> Unit,
) = DomposableRow(DomposableRowProps(gap, alignment), modifier, content)

@Composable fun DomposableFlowRow(
    modifier: DomposableModifier = DomposableModifier,
    gap: DomposableLength = 8.dp,
    content: @Composable () -> Unit,
) = DomposableFlowRow(DomposableFlowRowProps(gap), modifier, content)

@Composable fun DomposableLink(
    label: String,
    href: String,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableLink(DomposableLinkProps(label, href), modifier)

@Composable fun DomposableBox(
    modifier: DomposableModifier = DomposableModifier,
    alignment: DomposableAlignment = DomposableAlignment.Start,
    content: @Composable () -> Unit,
) = DomposableBox(DomposableBoxProps(alignment), modifier, content)

@Composable fun DomposableCard(
    modifier: DomposableModifier = DomposableModifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) = DomposableCard(DomposableCardProps(onClick), modifier, content)

@Composable fun DomposableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    purpose: DomposableInputPurpose = DomposableInputPurpose.Text,
    enabled: Boolean = true,
    multiline: Boolean = false,
    error: String? = null,
) = DomposableTextField(
    DomposableTextFieldProps(value, onValueChange, label, purpose, enabled, multiline, error),
    modifier,
)

@Composable fun DomposableCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
) = DomposableCheckbox(DomposableCheckboxProps(checked, onCheckedChange, label, enabled), modifier)

@Composable fun DomposableSpacer(
    length: DomposableLength = 8.dp,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableSpacer(DomposableSpacerProps(length), modifier)

@Composable fun DomposableDivider(modifier: DomposableModifier = DomposableModifier) =
    DomposableDivider(DomposableDividerProps(), modifier)

class DomposableScaffoldScope internal constructor(
    val contentModifier: DomposableModifier,
    val insets: DomposableInsets,
    val availableContentSize: DomposableSize,
)

@Composable
fun DomposableScaffold(
    modifier: DomposableModifier = DomposableModifier,
    insets: DomposableInsets = DomposableInsets(),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    navigation: @Composable () -> Unit = {},
    contextual: @Composable () -> Unit = {},
    floatingAction: @Composable () -> Unit = {},
    snackbar: @Composable () -> Unit = {},
    overlays: @Composable () -> Unit = {},
    content: @Composable DomposableScaffoldScope.() -> Unit,
) {
    DomposableScaffold(
        DomposableScaffoldProps(topBar, bottomBar, navigation, contextual, floatingAction, snackbar, overlays),
        modifier,
    ) {
        DomposableMeasure { size ->
            val available =
                DomposableSize(
                    (size.width - insets.start.value - insets.end.value).coerceAtLeast(0f),
                    (size.height - insets.top.value - insets.bottom.value).coerceAtLeast(0f),
                )
            DomposableScaffoldScope(DomposableModifier.padding(insets), insets, available).content()
        }
    }
}

@Composable
fun <T> DomposableScreenScaffold(
    state: DomposableScreenState<T>,
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable DomposableScaffoldScope.(T) -> Unit,
) {
    DomposableScaffold(modifier) {
        when (state) {
            DomposableScreenState.Loading -> {
                DomposableProgress(DomposableProgressProps())
            }

            is DomposableScreenState.Error -> {
                DomposableBanner(DomposableBannerProps(state.message, isError = true))
                state.retry?.let { DomposableButton("Retry", it) }
            }

            is DomposableScreenState.Content -> {
                content(state.value)
            }
        }
    }
}

@Composable
expect fun DomposableMeasure(
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable (DomposableSize) -> Unit,
)
