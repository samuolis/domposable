package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableComponent
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableProgressAppearance
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.foundation.dp

@DomposableComponent("ListItem")
@Suppress("LongParameterList")
data class DomposableListItemProps(
    val headline: String,
    val supportingText: String? = null,
    val overline: String? = null,
    val onClick: (() -> Unit)? = null,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val leadingContent: @Composable () -> Unit = {},
    val trailingContent: @Composable () -> Unit = {},
    val variant: DomposableListItemVariant = DomposableListItemVariant.Standard,
    val position: DomposableListItemPosition = DomposableListItemPosition.Only,
    val appearance: DomposableItemAppearance? = null,
)

@Composable
@Suppress("LongParameterList")
fun DomposableListItem(
    headline: String,
    supportingText: String? = null,
    overline: String? = null,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    leadingContent: @Composable () -> Unit = {},
    trailingContent: @Composable () -> Unit = {},
    variant: DomposableListItemVariant = DomposableListItemVariant.Standard,
    position: DomposableListItemPosition = DomposableListItemPosition.Only,
    appearance: DomposableItemAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableListItem(
    DomposableListItemProps(
        headline,
        supportingText,
        overline,
        onClick,
        selected,
        enabled,
        leadingContent,
        trailingContent,
        variant,
        position,
        appearance,
    ),
    modifier,
)

@DomposableComponent("BottomAppBar")
data class DomposableBottomAppBarProps(
    val floatingAction: @Composable () -> Unit = {},
    val appearance: DomposableSurfaceAppearance? = null,
    val variant: DomposableBottomAppBarVariant = DomposableBottomAppBarVariant.Standard,
)

@Composable
fun DomposableBottomAppBar(
    floatingAction: @Composable () -> Unit = {},
    appearance: DomposableSurfaceAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
    variant: DomposableBottomAppBarVariant = DomposableBottomAppBarVariant.Standard,
    content: @Composable () -> Unit,
) = DomposableBottomAppBar(
    DomposableBottomAppBarProps(floatingAction, appearance, variant = variant),
    modifier,
    content,
)

@DomposableComponent("NavigationDrawer")
data class DomposableNavigationDrawerProps(
    val expanded: Boolean,
    val onDismiss: () -> Unit,
    val variant: DomposableDrawerVariant = DomposableDrawerVariant.Modal,
    val gesturesEnabled: Boolean = true,
    val label: String = "Navigation",
    val appearance: DomposableSurfaceAppearance? = null,
)

@Composable
@Suppress("LongParameterList")
fun DomposableNavigationDrawer(
    expanded: Boolean,
    onDismiss: () -> Unit,
    variant: DomposableDrawerVariant = DomposableDrawerVariant.Modal,
    gesturesEnabled: Boolean = true,
    label: String = "Navigation",
    appearance: DomposableSurfaceAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit,
) = DomposableNavigationDrawer(
    DomposableNavigationDrawerProps(expanded, onDismiss, variant, gesturesEnabled, label, appearance),
    modifier,
    content,
)

@DomposableComponent("NavigationDrawerItem")
data class DomposableNavigationDrawerItemProps(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val icon: @Composable () -> Unit = {},
    val badge: @Composable () -> Unit = {},
    val appearance: DomposableItemAppearance? = null,
)

@Composable
@Suppress("LongParameterList")
fun DomposableNavigationDrawerItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: @Composable () -> Unit = {},
    badge: @Composable () -> Unit = {},
    appearance: DomposableItemAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
) = DomposableNavigationDrawerItem(
    DomposableNavigationDrawerItemProps(label, selected, onClick, enabled, icon, badge, appearance),
    modifier,
)

@DomposableComponent("WideNavigationRail")
data class DomposableWideNavigationRailProps(
    val expanded: Boolean,
    val modal: Boolean = false,
    val onDismiss: () -> Unit = {},
    val header: @Composable () -> Unit = {},
    val appearance: DomposableSurfaceAppearance? = null,
)

@Composable
fun DomposableWideNavigationRail(
    expanded: Boolean,
    modal: Boolean = false,
    onDismiss: () -> Unit = {},
    header: @Composable () -> Unit = {},
    appearance: DomposableSurfaceAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit,
) = DomposableWideNavigationRail(
    DomposableWideNavigationRailProps(expanded, modal, onDismiss, header, appearance),
    modifier,
    content,
)

@DomposableComponent("BottomSheetScaffold")
data class DomposableBottomSheetScaffoldProps(
    val expanded: Boolean,
    val onExpandedChange: (Boolean) -> Unit,
    val peekHeight: DomposableLength = 56.dp,
    val swipeEnabled: Boolean = true,
    val sheetContent: @Composable () -> Unit,
    val appearance: DomposableSurfaceAppearance? = null,
)

@Composable
@Suppress("LongParameterList")
fun DomposableBottomSheetScaffold(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    peekHeight: DomposableLength = 56.dp,
    swipeEnabled: Boolean = true,
    sheetContent: @Composable () -> Unit,
    appearance: DomposableSurfaceAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit,
) = DomposableBottomSheetScaffold(
    DomposableBottomSheetScaffoldProps(expanded, onExpandedChange, peekHeight, swipeEnabled, sheetContent, appearance),
    modifier,
    content,
)
