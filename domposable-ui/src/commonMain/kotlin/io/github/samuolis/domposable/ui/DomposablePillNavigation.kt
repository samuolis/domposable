package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable

/** A stable destination used by the portable floating navigation adapter. */
class DomposablePillNavigationItem(
    val key: String,
    val label: String,
    val icon: @Composable () -> Unit,
    val enabled: Boolean = true,
    val badge: (@Composable () -> Unit)? = null,
    val contentDescription: String = label,
    val href: String? = null,
) {
    init {
        require(key.isNotBlank()) { "Destination keys cannot be blank" }
        require(label.isNotBlank()) { "Destination labels cannot be blank" }
        require(contentDescription.isNotBlank()) { "Destination descriptions cannot be blank" }
    }
}

enum class DomposablePillMaterial { Solid, Frosted, Glass }

/** Shares a backdrop between the marked page source and floating native chrome. */
@Composable
expect fun DomposablePillBackdropHost(content: @Composable () -> Unit)

@Composable
expect fun DomposablePillBackdropSource(
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
expect fun DomposablePillNavigationBar(
    items: List<DomposablePillNavigationItem>,
    selectedKey: String?,
    onSelect: (String) -> Unit,
    label: String,
    modifier: DomposableModifier = DomposableModifier,
    expanded: Boolean = true,
    onExpandRequest: () -> Unit = {},
    scrubEnabled: Boolean = false,
    motionEnabled: Boolean = true,
    material: DomposablePillMaterial = DomposablePillMaterial.Frosted,
)
