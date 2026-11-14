package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Nav
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
actual fun DomposablePillBackdropHost(content: @Composable () -> Unit) = content()

@Composable
actual fun DomposablePillBackdropSource(
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    DomposableBox(modifier = modifier, content = content)
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
    require(items.all { it.key.isNotBlank() && it.label.isNotBlank() && it.contentDescription.isNotBlank() })
    require(items.map { it.key }.distinct().size == items.size)
    require(selectedKey == null || items.any { it.key == selectedKey })
    if (items.isEmpty()) return
    val formEnabled = domposableFormEnabled()
    DomposableHtmlModifiers(modifier) {
        Nav(attrs = {
            domposableAttributes(modifier)
            attr("aria-label", label)
            classes("d-navigation")
        }) {
            items.forEach { item ->
                val href = item.href?.let(::safeDomposableHref)
                val itemContent: @Composable () -> Unit = {
                    Span(attrs = { attr("aria-hidden", "true") }) {
                        item.icon()
                        item.badge?.invoke()
                    }
                    Span { Text(item.label) }
                }
                if (href != null && formEnabled && item.enabled) {
                    A(href = href, attrs = {
                        attr("aria-label", item.contentDescription)
                        if (item.key == selectedKey) attr("aria-current", "page")
                        onClick { event ->
                            val modified = event.ctrlKey || event.metaKey || event.shiftKey || event.altKey
                            if (event.button.toInt() == 0 && !modified) {
                                event.preventDefault()
                                onSelect(item.key)
                            }
                        }
                    }) { itemContent() }
                } else {
                    Button(attrs = {
                        attr("type", "button")
                        attr("aria-label", item.contentDescription)
                        if (item.key == selectedKey) attr("aria-current", "page")
                        if (!formEnabled || !item.enabled) disabled()
                        onClick { if (formEnabled && item.enabled) onSelect(item.key) }
                    }) { itemContent() }
                }
            }
        }
    }
}
