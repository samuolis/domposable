package io.github.samuolis.domposable.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.samuolis.pill.PillBackdrop
import io.github.samuolis.pill.PillBarStyle
import io.github.samuolis.pill.PillMaterial
import io.github.samuolis.pill.PillNavigationBar
import io.github.samuolis.pill.PillNavigationItem
import io.github.samuolis.pill.pillBackdropSource
import io.github.samuolis.pill.rememberPillBackdrop

private val LocalPillBackdrop = staticCompositionLocalOf<PillBackdrop?> { null }

@Composable
actual fun DomposablePillBackdropHost(content: @Composable () -> Unit) {
    val backdrop = rememberPillBackdrop()
    CompositionLocalProvider(LocalPillBackdrop provides backdrop, content = content)
}

@Composable
actual fun DomposablePillBackdropSource(
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    val backdrop = LocalPillBackdrop.current
    val native = modifier.toComposeModifier()
    Box(if (backdrop == null) native else native.pillBackdropSource(backdrop)) { content() }
}

@Composable
actual fun DomposablePillNavigationBar(
    items: List<DomposablePillNavigationItem>,
    selectedKey: String?,
    onSelect: (String) -> Unit,
    label: String,
    modifier: DomposableModifier,
    expanded: Boolean,
    onExpandRequest: () -> Unit,
    scrubEnabled: Boolean,
    motionEnabled: Boolean,
    material: DomposablePillMaterial,
) {
    val colors = DomposableTheme.colors
    val style =
        PillBarStyle(
            material =
                when (material) {
                    DomposablePillMaterial.Solid -> PillMaterial.Solid
                    DomposablePillMaterial.Frosted -> PillMaterial.Frosted
                    DomposablePillMaterial.Glass -> PillMaterial.Glass
                },
            containerColor = Color(colors.surface.argb).copy(alpha = 0.85f),
            contentColor = Color(colors.onSurface.argb),
            selectedColor = Color(colors.primary.argb),
            selectedContentColor = Color(colors.onPrimary.argb),
        )
    PillNavigationBar(
        items =
            items.map { item ->
                PillNavigationItem(
                    item.key,
                    item.label,
                    icon = { item.icon() },
                    enabled = item.enabled,
                    badge = item.badge,
                    contentDescription = item.contentDescription,
                )
            },
        selectedKey = selectedKey,
        onSelect = onSelect,
        modifier = modifier.toComposeModifier().semantics { contentDescription = label },
        backdrop = LocalPillBackdrop.current,
        expanded = expanded,
        onExpandRequest = onExpandRequest,
        scrubEnabled = scrubEnabled,
        motionEnabled = motionEnabled,
        style = style,
    )
}
