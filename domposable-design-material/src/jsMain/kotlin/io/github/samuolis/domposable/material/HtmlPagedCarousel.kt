package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.ui.DomposableButtonProps
import io.github.samuolis.domposable.ui.DomposableCarouselProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.domposableAttributes
import io.github.samuolis.domposable.ui.htmlAttributes
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

private const val PAGED_CAROUSEL_STYLE_ID = "d-paged-carousel-styles"
private var nextPagedCarouselId = 0
private var pagedCarouselStyleUsers = 0

@Composable
internal fun HtmlPagedCarousel(
    props: DomposableCarouselProps,
    modifier: DomposableModifier,
) {
    val layout = props.pagedLayout ?: return
    if (props.items.isEmpty()) return
    InstallPagedCarouselStyles()
    val id = remember { "d-paged-carousel-${nextPagedCarouselId++}" }
    var position by remember(props.items.size) {
        mutableIntStateOf(props.currentItem.coerceIn(props.items.indices))
    }
    var wide by remember { mutableStateOf(false) }
    var rootWidth by remember { mutableStateOf(0.0) }
    var pendingTarget by remember { mutableIntStateOf(-1) }
    var lastApproachingIndex by remember(props.items.size) { mutableIntStateOf(-1) }
    val currentItem by rememberUpdatedState(props.currentItem.coerceIn(props.items.indices))
    val onCurrentItemChange by rememberUpdatedState(props.onCurrentItemChange)
    val onApproachingEnd by rememberUpdatedState(props.onApproachingEnd)
    val updatePosition: (Int) -> Unit = { selected ->
        if (selected != position) {
            position = selected
            onCurrentItemChange?.invoke(selected)
            if (selected < props.items.lastIndex - 1) {
                lastApproachingIndex = -1
            } else if (lastApproachingIndex != selected) {
                lastApproachingIndex = selected
                onApproachingEnd?.invoke(selected)
            }
        }
    }
    LaunchedEffect(currentItem, props.items.size) {
        position = currentItem
        pendingTarget = currentItem
        scrollPagedCarouselItem(id, currentItem)
    }
    LaunchedEffect(rootWidth, layout.breakpoint.value) {
        wide = rootWidth >= layout.breakpoint.value.toDouble()
    }
    LaunchedEffect(wide, position) {
        pendingTarget = position
        scrollPagedCarouselItem(id, position)
    }

    Div(attrs = {
        domposableAttributes(modifier)
        id(id)
        classes("d-carousel", "d-carousel-paged", if (wide) "d-carousel-paged-wide" else "d-carousel-paged-compact")
        if (layout.compactTrailingPeek.value == 0f) classes("d-carousel-paged-no-peek")
        if (props.items.size == 1) attr("data-single-item", "true")
        attr("role", "region")
        attr("aria-roledescription", "carousel")
        attr("aria-label", props.label)
        style {
            property("--d-paged-carousel-inset", "${layout.contentInset.value}px")
            property("--d-paged-carousel-gap", "${layout.itemSpacing.value}px")
            property("--d-paged-carousel-peek", "${layout.compactTrailingPeek.value}px")
            val indicatorHeight =
                if (!layout.showIndicators || props.items.size <= 1) {
                    0
                } else if (wide) {
                    22
                } else {
                    18
                }
            property("--d-paged-carousel-indicator-height", "${indicatorHeight}px")
            layout.compactHeight?.let {
                property("--d-paged-carousel-height", it.scaledCarouselHeight(layout.heightTextScaleReference))
            }
            layout.wideHeight?.let {
                property("--d-paged-carousel-wide-height", it.scaledCarouselHeight(layout.heightTextScaleReference))
            }
            layout.controls.activeIndicator?.let {
                property("--d-paged-carousel-indicator-active", it.css)
            }
            val inactiveIndicator =
                if (wide) {
                    layout.controls.wideInactiveIndicator ?: layout.controls.inactiveIndicator
                } else {
                    layout.controls.inactiveIndicator
                }
            inactiveIndicator?.let { property("--d-paged-carousel-indicator-inactive", it.css) }
        }
        ref { element ->
            val callback: (dynamic, dynamic) -> Unit = { _, _ ->
                rootWidth = element.getBoundingClientRect().width
            }
            rootWidth = element.getBoundingClientRect().width
            val observer = js("typeof ResizeObserver !== 'undefined' ? new ResizeObserver(callback) : null")
            if (observer != null) observer.observe(element)
            val resize: (org.w3c.dom.events.Event) -> Unit = { rootWidth = element.getBoundingClientRect().width }
            if (observer == null) window.addEventListener("resize", resize)
            onDispose {
                observer?.disconnect()
                if (observer == null) window.removeEventListener("resize", resize)
            }
        }
    }) {
        Div(attrs = {
            id("$id-track")
            classes("d-paged-carousel-track")
            attr("tabindex", "0")
            attr("aria-label", props.label)
            ref { element ->
                var settleTimer: Int? = null
                val cancelPendingTarget: (org.w3c.dom.events.Event) -> Unit = { pendingTarget = -1 }
                val listener: (org.w3c.dom.events.Event) -> Unit = {
                    settleTimer?.let(window::clearTimeout)
                    settleTimer =
                        window.setTimeout({
                            val selected =
                                pendingTarget.takeIf { it in props.items.indices }
                                    ?: closestPagedCarouselItem(element)
                            pendingTarget = -1
                            updatePosition(selected)
                        }, 90)
                }
                element.addEventListener("scroll", listener)
                element.addEventListener("wheel", cancelPendingTarget)
                element.addEventListener("pointerdown", cancelPendingTarget)
                element.addEventListener("touchstart", cancelPendingTarget)
                onDispose {
                    settleTimer?.let(window::clearTimeout)
                    element.removeEventListener("scroll", listener)
                    element.removeEventListener("wheel", cancelPendingTarget)
                    element.removeEventListener("pointerdown", cancelPendingTarget)
                    element.removeEventListener("touchstart", cancelPendingTarget)
                }
            }
            onKeyDown { event ->
                if (event.target != event.currentTarget) return@onKeyDown
                val rtl = window.getComputedStyle(event.currentTarget as HTMLElement).direction == "rtl"
                val delta =
                    when (event.key) {
                        "ArrowRight" -> if (rtl) -1 else 1
                        "ArrowLeft" -> if (rtl) 1 else -1
                        else -> 0
                    }
                if (delta != 0) {
                    event.preventDefault()
                    selectPagedCarouselItem(
                        (position + delta).coerceIn(0, props.items.lastIndex),
                        id,
                        props.items.lastIndex,
                        updatePosition = updatePosition,
                        requestTarget = { pendingTarget = it },
                    )
                }
            }
        }) {
            props.items.forEachIndexed { index, item ->
                key(item.key) {
                    Div(attrs = {
                        classes("d-paged-carousel-slide")
                        attr("role", "group")
                        attr("aria-roledescription", "slide")
                        attr("aria-label", layout.labels.itemPosition(index + 1, props.items.size))
                        attr("data-index", index.toString())
                    }) { item.content() }
                }
            }
        }
        if (layout.showNavigation && wide && props.items.size > 1) {
            Div(attrs = { classes("d-paged-carousel-navigation") }) {
                CarouselControl(
                    label = layout.labels.previous,
                    enabled = position > 0,
                    appearance = layout.controls.navigationButton,
                    icon = layout.controls.previousIcon,
                    fallback = "‹",
                ) {
                    selectPagedCarouselItem(
                        (position - 1).coerceAtLeast(0),
                        id,
                        props.items.lastIndex,
                        updatePosition = updatePosition,
                        requestTarget = { pendingTarget = it },
                    )
                }
                CarouselControl(
                    label = layout.labels.next,
                    enabled = position < props.items.lastIndex,
                    appearance = layout.controls.navigationButton,
                    icon = layout.controls.nextIcon,
                    fallback = "›",
                ) {
                    selectPagedCarouselItem(
                        (position + 1).coerceAtMost(props.items.lastIndex),
                        id,
                        props.items.lastIndex,
                        updatePosition = updatePosition,
                        requestTarget = { pendingTarget = it },
                    )
                }
            }
        }
        if (layout.showIndicators && props.items.size > 1) {
            Div(attrs = {
                classes("d-paged-carousel-indicators")
                attr("aria-label", layout.labels.itemPosition(position + 1, props.items.size))
            }) {
                val firstIndicator = (position - 2).coerceIn(0, (props.items.size - 5).coerceAtLeast(0))
                for (index in firstIndicator until minOf(firstIndicator + 5, props.items.size)) {
                    val active = index == position
                    val label = layout.labels.itemPosition(index + 1, props.items.size)
                    Button(attrs = {
                        attr("type", "button")
                        attr("aria-label", label)
                        if (active) attr("aria-current", "true")
                        classes("d-paged-carousel-control", if (active) "is-active" else "is-inactive")
                        onClick {
                            if (index != position) {
                                updatePosition(index)
                                pendingTarget = index
                                scrollPagedCarouselItem(id, index)
                            }
                        }
                    }) {
                        Span(attrs = { classes("d-paged-carousel-visually-hidden") }) { Text(label) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CarouselControl(
    label: String,
    enabled: Boolean,
    appearance: DomposableButtonAppearance?,
    icon: (@Composable () -> Unit)? = null,
    fallback: String? = null,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    if (!enabled) {
        Span(attrs = {
            attr("aria-hidden", "true")
            style {
                property("width", "${appearance?.minWidth?.value ?: 48f}px")
                property("height", "${appearance?.minHeight?.value ?: 48f}px")
            }
        }) {}
        return
    }
    val modifier =
        DomposableModifier.htmlAttributes {
            classes("d-paged-carousel-control", if (active) "is-active" else "is-inactive")
            attr("aria-label", label)
            if (active) attr("aria-current", "true")
        }
    HtmlButton(
        DomposableButtonProps(onClick, enabled, DomposableButtonStyle.Outlined, appearance = appearance),
        modifier,
        content = {
            if (icon != null) {
                icon()
            } else if (fallback != null) {
                Text(fallback)
            }
            Span(attrs = { classes("d-paged-carousel-visually-hidden") }) { Text(label) }
        },
    )
}

private fun closestPagedCarouselItem(track: HTMLElement): Int {
    val rtl = window.getComputedStyle(track).direction == "rtl"
    val bounds = track.getBoundingClientRect()
    val target = if (rtl) bounds.right else bounds.left
    var closest = 0
    var distance = Double.POSITIVE_INFINITY
    for (index in 0 until track.children.length) {
        val child = track.children.item(index) as? HTMLElement ?: continue
        val rect = child.getBoundingClientRect()
        val edge = if (rtl) rect.right else rect.left
        val delta = kotlin.math.abs(edge - target)
        if (delta < distance) {
            distance = delta
            closest = index
        }
    }
    return closest
}

private fun io.github.samuolis.domposable.foundation.DomposableLength.scaledCarouselHeight(
    reference: io.github.samuolis.domposable.foundation.DomposableLength?,
): String =
    reference?.let {
        "max(${value}px, calc(var(--d-type-body-medium-size, ${it.value}px) * ${value / it.value}))"
    } ?: "${value}px"

private fun selectPagedCarouselItem(
    index: Int,
    id: String,
    lastIndex: Int,
    updatePosition: (Int) -> Unit,
    requestTarget: (Int) -> Unit,
) {
    val selected = index.coerceIn(0, lastIndex)
    requestTarget(selected)
    updatePosition(selected)
    scrollPagedCarouselItem(id, selected)
}

private fun scrollPagedCarouselItem(
    id: String,
    index: Int,
) {
    val track = document.getElementById("$id-track") ?: return
    val item = track.children.item(index) as? HTMLElement ?: return
    item.asDynamic().scrollIntoView(js("({block:'nearest',inline:'start'})"))
}

@Composable
private fun InstallPagedCarouselStyles() {
    DisposableEffect(Unit) {
        val style =
            document.getElementById(PAGED_CAROUSEL_STYLE_ID)
                ?: document.createElement("style").also {
                    it.id = PAGED_CAROUSEL_STYLE_ID
                    it.textContent = PAGED_CAROUSEL_CSS
                    document.head!!.appendChild(it)
                }
        pagedCarouselStyleUsers++
        onDispose {
            pagedCarouselStyleUsers--
            if (pagedCarouselStyleUsers == 0) style.remove()
        }
    }
}

private const val PAGED_CAROUSEL_CSS = """
.d-carousel-paged{position:relative;min-width:0;width:100%}
.d-paged-carousel-track{display:flex;min-width:0;width:calc(100% - 2 * var(--d-paged-carousel-inset,16px));margin-inline:var(--d-paged-carousel-inset,16px);overflow-x:auto;overscroll-behavior-inline:contain;scroll-snap-type:x mandatory;scroll-behavior:smooth;scrollbar-width:none;box-sizing:border-box;gap:var(--d-paged-carousel-gap,12px);scroll-padding-inline:0;padding:8px 0}
.d-paged-carousel-track::-webkit-scrollbar{display:none}.d-paged-carousel-track:focus-visible{outline:2px solid var(--d-primary);outline-offset:2px}
.d-paged-carousel-slide{box-sizing:border-box;flex:0 0 calc(100% - var(--d-paged-carousel-gap,12px) - var(--d-paged-carousel-peek,14px));min-width:0;min-height:0;height:var(--d-paged-carousel-height,auto);scroll-snap-align:start}
.d-carousel-paged-compact.d-carousel-paged-no-peek .d-paged-carousel-slide,.d-carousel-paged[data-single-item=true] .d-paged-carousel-slide{flex-basis:100%}
.d-carousel-paged-wide .d-paged-carousel-slide{flex-basis:100%;height:var(--d-paged-carousel-wide-height,var(--d-paged-carousel-height,auto))}
.d-paged-carousel-navigation{position:absolute;z-index:1;inset-inline:calc(var(--d-paged-carousel-inset,16px) + 16px);top:calc((100% - var(--d-paged-carousel-indicator-height,0px))/2);display:flex;align-items:center;justify-content:space-between;transform:translateY(-50%);pointer-events:none}
.d-paged-carousel-control{display:inline-flex;flex:none;align-items:center;justify-content:center;min-width:40px;min-height:44px;padding:0;border:0;background:transparent;color:var(--d-on-surface);font:inherit;cursor:pointer;pointer-events:auto}
.d-paged-carousel-control[disabled]{cursor:default;opacity:.38}.d-paged-carousel-control:focus-visible{outline:2px solid var(--d-primary);outline-offset:2px}
.d-paged-carousel-indicators{display:flex;height:18px;align-items:center;justify-content:center;gap:6px;padding-inline:8px}
.d-carousel-paged-wide .d-paged-carousel-indicators{height:22px}
.d-paged-carousel-indicators .d-paged-carousel-control{position:relative;width:16px;height:6px;min-width:16px;min-height:6px}
.d-paged-carousel-visually-hidden{position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip-path:inset(50%);white-space:nowrap;border:0}
.d-paged-carousel-indicators .d-paged-carousel-control::before{content:"";width:6px;height:6px;border-radius:999px;background:var(--d-paged-carousel-indicator-inactive,var(--d-outline));transition:width 140ms ease}
.d-paged-carousel-indicators .d-paged-carousel-control.is-active::before{width:16px;background:var(--d-paged-carousel-indicator-active,var(--d-primary))}
@media(prefers-reduced-motion:reduce){.d-paged-carousel-track{scroll-behavior:auto}.d-paged-carousel-indicators .d-paged-carousel-control::before{transition:none}}
@media(forced-colors:active){.d-paged-carousel-control:focus-visible{outline-color:Highlight}}
"""
