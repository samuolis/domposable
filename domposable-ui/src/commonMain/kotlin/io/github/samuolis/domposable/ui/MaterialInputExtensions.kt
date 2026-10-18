package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableComponent
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance

/** A controlled wheel; identity and display labels remain owned by the caller. */
@DomposableComponent("ScrollField")
data class DomposableScrollFieldProps(
    val itemCount: Int,
    val selectedIndex: Int,
    val onSelectedIndexChange: (Int) -> Unit,
    val label: String,
    val indexDescription: (Int) -> String = { it.toString() },
    val enabled: Boolean = true,
    val itemContent: (@Composable (index: Int, selected: Boolean, enabled: Boolean) -> Unit)? = null,
    val appearance: DomposableItemAppearance? = null,
) {
    init {
        require(itemCount > 0 && selectedIndex in 0 until itemCount)
    }
}

@Composable
fun DomposableScrollField(
    itemCount: Int,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    indexDescription: (Int) -> String = { it.toString() },
    enabled: Boolean = true,
    itemContent: (@Composable (index: Int, selected: Boolean, enabled: Boolean) -> Unit)? = null,
    appearance: DomposableItemAppearance? = null,
) = DomposableScrollField(
    DomposableScrollFieldProps(
        itemCount,
        selectedIndex,
        onSelectedIndexChange,
        label,
        indexDescription,
        enabled,
        itemContent,
        appearance,
    ),
    modifier,
)

@DomposableComponent("Label")
data class DomposableLabelProps(
    val label: @Composable () -> Unit,
    val persistent: Boolean = false,
    val appearance: DomposableSurfaceAppearance? = null,
)

@Composable
fun DomposableLabel(
    label: @Composable () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    persistent: Boolean = false,
    appearance: DomposableSurfaceAppearance? = null,
    content: @Composable () -> Unit,
) = DomposableLabel(DomposableLabelProps(label, persistent, appearance), modifier, content)

@DomposableComponent("MenuGroup")
data class DomposableMenuGroupProps(
    val appearance: DomposableSurfaceAppearance? = null,
)

@Composable
fun DomposableMenuGroup(
    modifier: DomposableModifier = DomposableModifier,
    appearance: DomposableSurfaceAppearance? = null,
    content: @Composable () -> Unit,
) = DomposableMenuGroup(DomposableMenuGroupProps(appearance), modifier, content)

@Composable
fun DomposableDropdownMenuItem(
    label: String,
    onClick: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    checked: Boolean? = null,
    selected: Boolean? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    selectedIcon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    appearance: DomposableItemAppearance? = null,
) = DomposableDropdownMenuItem(
    DomposableMenuItem(
        label,
        onClick,
        enabled,
        checked,
        selected,
        leadingIcon,
        selectedIcon,
        trailingContent,
        supportingText,
        appearance,
    ),
    modifier,
)

@Composable
fun DomposableDropdownMenuItem(
    textContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
    enabled: Boolean = true,
    checked: Boolean? = null,
    selected: Boolean? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    selectedIcon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    appearance: DomposableItemAppearance? = null,
) = DomposableDropdownMenuItem(
    DomposableMenuItem(
        "",
        onClick,
        enabled,
        checked,
        selected,
        leadingIcon,
        selectedIcon,
        trailingContent,
        supportingText,
        appearance,
        textContent,
    ),
    modifier,
)
