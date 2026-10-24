package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/** A null value means no descendant requested fill; infinity means an uncapped fill request. */
private class DomposableHtmlWidthDemandScope(
    val maxWidthPx: Double?,
    private val parent: DomposableHtmlWidthDemandScope? = null,
    private val propagateToParent: Boolean = true,
    val sumSiblingRequestsWithGapPx: Double? = null,
) {
    private val requests = mutableMapOf<Any, Double>()
    var requestedWidthPx by mutableStateOf<Double?>(null)
        private set

    fun add(
        request: Any,
        requestedWidthPx: Double,
    ) {
        if (requests.put(request, requestedWidthPx) != requestedWidthPx) updateRequestedWidth()
    }

    fun remove(request: Any) {
        if (requests.remove(request) != null) updateRequestedWidth()
    }

    fun dispose() {
        if (propagateToParent) parent?.remove(this)
    }

    private fun updateRequestedWidth() {
        val aggregateWidthPx =
            when {
                requests.isEmpty() -> {
                    null
                }

                requests.values.any { it.isInfinite() } -> {
                    Double.POSITIVE_INFINITY
                }

                sumSiblingRequestsWithGapPx != null -> {
                    requests.values.sum() + sumSiblingRequestsWithGapPx * (requests.size - 1)
                }

                else -> {
                    requests.values.maxOrNull()
                }
            }
        val nextWidthPx = aggregateWidthPx?.let { maxWidthPx?.let { cap -> minOf(it, cap) } ?: it }
        if (requestedWidthPx == nextWidthPx) return
        requestedWidthPx = nextWidthPx
        if (propagateToParent) {
            if (nextWidthPx == null) parent?.remove(this) else parent?.add(this, nextWidthPx)
        }
    }
}

private val LocalDomposableHtmlWidthDemand = staticCompositionLocalOf<DomposableHtmlWidthDemandScope?> { null }
private val LocalDomposableHtmlWidthDemandOwnsModifiers = staticCompositionLocalOf { false }

@Composable
internal fun DomposableHtmlWidthDemandModifierOwner(
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    // A layout shares its own modifier scope; compact children must not reuse that scope.
    if (LocalDomposableHtmlWidthDemandOwnsModifiers.current) {
        CompositionLocalProvider(LocalDomposableHtmlWidthDemandOwnsModifiers provides false, content = content)
    } else {
        DomposableHtmlWidthDemandContainer(modifier = modifier) {
            CompositionLocalProvider(LocalDomposableHtmlWidthDemandOwnsModifiers provides false, content = content)
        }
    }
}

@Composable
internal fun DomposableHtmlWidthDemandRequested(): Boolean {
    val scope = LocalDomposableHtmlWidthDemand.current ?: return false
    val request = scope.requestedWidthPx ?: return false
    return request == Double.POSITIVE_INFINITY || scope.maxWidthPx != null
}

@Composable
internal fun DomposableHtmlWidthDemandValue(): Double? = LocalDomposableHtmlWidthDemand.current?.requestedWidthPx

/**
 * Renderer support for automatic HTML layout roots. Descendant fill-width requests expand this root and
 * propagate through other automatic roots until an explicit width modifier or isolated popup boundary.
 * The callback gets null for intrinsic sizing, positive infinity to fill the current bounds, or a
 * finite minimum width required by a capped fill child.
 */
@Composable
fun DomposableHtmlWidthDemandContainer(
    modifier: DomposableModifier? = null,
    propagateToParent: Boolean = true,
    sumSiblingRequestsWithGapPx: Double? = null,
    content: @Composable (widthDemandPx: Double?) -> Unit,
) {
    val parent = LocalDomposableHtmlWidthDemand.current.takeIf { propagateToParent }
    val demand =
        remember(parent, parent?.maxWidthPx, sumSiblingRequestsWithGapPx, propagateToParent) {
            DomposableHtmlWidthDemandScope(
                maxWidthPx = parent?.maxWidthPx,
                parent = parent,
                propagateToParent = propagateToParent,
                sumSiblingRequestsWithGapPx = sumSiblingRequestsWithGapPx,
            )
        }
    val ownWidthModifier =
        modifier?.elements?.any { element ->
            element == DomposableModifierOperation.FillWidth ||
                element is DomposableModifierOperation.Width ||
                element is DomposableModifierOperation.FillWidthFraction
        } == true
    val descendantWidthDemandPx = demand.requestedWidthPx
    val requestedWidthPx =
        if (descendantWidthDemandPx != null && parent?.maxWidthPx != null) {
            Double.POSITIVE_INFINITY
        } else {
            descendantWidthDemandPx ?: if (ownWidthModifier) Double.POSITIVE_INFINITY else null
        }
    DisposableEffect(demand) { onDispose { demand.dispose() } }
    CompositionLocalProvider(
        LocalDomposableHtmlWidthDemand provides demand,
        LocalDomposableHtmlWidthDemandOwnsModifiers provides (modifier != null),
    ) {
        content(requestedWidthPx)
    }
}

@Composable
internal fun DomposableHtmlWidthDemandModifiers(
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    DomposableHtmlWidthDemandModifierScope(modifier.elements, 0, content)
}

@Composable
private fun DomposableHtmlWidthDemandModifierScope(
    elements: List<DomposableModifierElement>,
    startIndex: Int,
    content: @Composable () -> Unit,
) {
    val index =
        elements.indexOfFirstFrom(startIndex) { element ->
            element == DomposableModifierOperation.FillWidth ||
                element is DomposableModifierOperation.Width ||
                element is DomposableModifierOperation.FillWidthFraction ||
                element is DomposableModifierOperation.MaxWidth
        }
    if (index < 0) {
        content()
        return
    }

    val operation = elements[index]
    key(index) {
        when (operation) {
            DomposableModifierOperation.FillWidth -> {
                val demand = LocalDomposableHtmlWidthDemand.current
                val request = remember { Any() }
                DisposableEffect(demand, request) {
                    demand?.add(request, Double.POSITIVE_INFINITY)
                    onDispose { demand?.remove(request) }
                }
                DomposableHtmlWidthDemandModifierScope(elements, index + 1, content)
            }

            is DomposableModifierOperation.Width,
            is DomposableModifierOperation.FillWidthFraction,
            -> {
                DomposableHtmlWidthDemandScopeProvider(propagateToParent = false, maxWidthPx = null) {
                    DomposableHtmlWidthDemandModifierScope(elements, index + 1, content)
                }
            }

            is DomposableModifierOperation.MaxWidth -> {
                val existingCap = LocalDomposableHtmlWidthDemand.current?.maxWidthPx
                val siblingGapPx = LocalDomposableHtmlWidthDemand.current?.sumSiblingRequestsWithGapPx
                val cap =
                    existingCap?.let { minOf(it, operation.length.value.toDouble()) }
                        ?: operation.length.value.toDouble()
                DomposableHtmlWidthDemandScopeProvider(maxWidthPx = cap, sumSiblingRequestsWithGapPx = siblingGapPx) {
                    DomposableHtmlWidthDemandModifierScope(elements, index + 1, content)
                }
            }

            else -> {
                DomposableHtmlWidthDemandModifierScope(elements, index + 1, content)
            }
        }
    }
}

@Composable
private fun DomposableHtmlWidthDemandScopeProvider(
    propagateToParent: Boolean = true,
    maxWidthPx: Double?,
    sumSiblingRequestsWithGapPx: Double? = null,
    content: @Composable () -> Unit,
) {
    val parent = LocalDomposableHtmlWidthDemand.current
    val effectiveMaxWidthPx =
        when {
            parent?.maxWidthPx == null -> maxWidthPx
            maxWidthPx == null -> parent.maxWidthPx
            else -> minOf(parent.maxWidthPx, maxWidthPx)
        }
    val demand =
        remember(parent, effectiveMaxWidthPx, propagateToParent, sumSiblingRequestsWithGapPx) {
            DomposableHtmlWidthDemandScope(
                maxWidthPx = effectiveMaxWidthPx,
                parent = parent,
                propagateToParent = propagateToParent,
                sumSiblingRequestsWithGapPx = sumSiblingRequestsWithGapPx,
            )
        }
    DisposableEffect(demand) { onDispose { demand.dispose() } }
    CompositionLocalProvider(LocalDomposableHtmlWidthDemand provides demand, content = content)
}

private inline fun <T> List<T>.indexOfFirstFrom(
    startIndex: Int,
    predicate: (T) -> Boolean,
): Int {
    for (index in startIndex until size) if (predicate(this[index])) return index
    return -1
}
