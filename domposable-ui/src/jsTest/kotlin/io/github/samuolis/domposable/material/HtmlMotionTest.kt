package io.github.samuolis.domposable.material

import androidx.compose.runtime.mutableStateOf
import io.github.samuolis.domposable.foundation.DomposableFabSize
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableFloatingActionButton
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposablePullToRefresh
import io.github.samuolis.domposable.ui.testId
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlMotionTest {
    @Test
    fun floatingActionButtonKeepsItsStaticSizeAndCanCollapseItsLabel() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            val root = document.createElement("div")
            document.body!!.appendChild(root)
            val extended = mutableStateOf(true)
            val composition =
                renderComposable(root) {
                    DomposableMaterialTheme {
                        DomposableFloatingActionButton(
                            onClick = {},
                            label = "Add item",
                            modifier = DomposableModifier.testId("extended-fab"),
                            extended = extended.value,
                        ) { Text("+") }
                        DomposableFloatingActionButton(
                            onClick = {},
                            label = "Add item",
                            size = DomposableFabSize.Medium,
                            modifier = DomposableModifier.testId("medium-fab"),
                        ) { Text("+") }
                    }
                }
            try {
                val fab = root.querySelector("[data-testid=extended-fab]") as HTMLElement
                val expandedWidth = assertExtendedFabStartsExpanded(fab)
                val reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches
                assertFabMotionPreference(fab, reducedMotion)
                assertFabCollapses(fab, root, expandedWidth, reducedMotion) { extended.value = false }

                val medium = root.querySelector("[data-testid=medium-fab]") as HTMLElement
                assertEquals("80px", medium.style.minWidth)
                assertEquals("80px", medium.style.height)
            } finally {
                composition.dispose()
                root.parentNode?.removeChild(root)
            }
        }

    @Test
    fun pullIndicatorFollowsThePointerAndRemainsAtThresholdWhileRefreshing() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            val root = document.createElement("div")
            document.body!!.appendChild(root)
            val refreshing = mutableStateOf(false)
            val composition =
                renderComposable(root) {
                    DomposableMaterialTheme {
                        DomposablePullToRefresh(
                            refreshing = refreshing.value,
                            onRefresh = { refreshing.value = true },
                            threshold = 80.dp,
                            modifier = DomposableModifier.testId("pull-refresh"),
                            indicatorContent = { state ->
                                Span(attrs = { attr("data-progress", state.progress.toString()) }) { Text("Refresh") }
                            },
                        ) { }
                    }
                }
            try {
                val pullRoot = root.querySelector(".d-pull-to-refresh") as HTMLElement
                val indicator = pullRoot.querySelector(".d-pull-refresh-indicator") as HTMLElement
                var seenPointerMove: Double? = null
                var seenPointerDown: Double? = null
                pullRoot.addEventListener("pointerdown") { event ->
                    seenPointerDown = (event.asDynamic().clientY as Number).toDouble()
                }
                pullRoot.addEventListener("pointermove") { event ->
                    seenPointerMove = (event.asDynamic().clientY as Number).toDouble()
                }

                fun pointer(
                    type: String,
                    y: Int,
                ) {
                    val options = js("({ bubbles: true, cancelable: true, isPrimary: true, button: 0 })")
                    options.clientY = y
                    val createEvent = js("(type, options) => new PointerEvent(type, options)")
                    pullRoot.dispatchEvent(createEvent(type, options) as org.w3c.dom.events.Event)
                }

                settleDom()
                pointer("pointerdown", 0)
                settleDom()
                pointer("pointermove", 60)
                settleDom()
                assertEquals(0.0, seenPointerDown)
                assertEquals(60.0, seenPointerMove)
                assertEquals(
                    "0.75",
                    indicator.querySelector("[data-progress]")?.getAttribute("data-progress"),
                    "Indicator should follow a 60px pointer drag",
                )
                assertEquals("", indicator.style.getPropertyValue("transform"))
                val indicatorBounds = indicator.getBoundingClientRect()
                val containerBounds = pullRoot.getBoundingClientRect()
                val centerDifference =
                    indicatorBounds.left + indicatorBounds.width / 2 -
                        containerBounds.left - containerBounds.width / 2
                assertTrue(kotlin.math.abs(centerDifference) < 1.0)
                assertTrue(kotlin.math.abs(indicatorBounds.top - containerBounds.top - 60.0) < 1.0)

                pointer("pointermove", 80)
                pointer("pointerup", 80)
                settleDom()
                assertTrue(indicator.hasAttribute("aria-busy"))
                assertEquals("1", indicator.querySelector("[data-progress]")?.getAttribute("data-progress"))
                assertEquals("80px", pullRoot.style.getPropertyValue("--d-pull-distance"))
            } finally {
                composition.dispose()
                root.parentNode?.removeChild(root)
            }
        }
}

private fun assertExtendedFabStartsExpanded(fab: HTMLElement): Double {
    assertEquals("auto", fab.style.width)
    assertEquals("56px", fab.style.height)
    assertEquals("1fr", fab.style.getPropertyValue("grid-template-columns").substringAfterLast(' '))
    assertEquals("Add item", fab.querySelector(".d-fab-label")?.textContent)
    val expandedWidth = fab.getBoundingClientRect().width
    assertTrue(expandedWidth > 56.0, "Extended FAB should fit its label; width was $expandedWidth")
    return expandedWidth
}

private fun assertFabMotionPreference(
    fab: HTMLElement,
    reducedMotion: Boolean,
) {
    val style = window.getComputedStyle(fab)
    if (reducedMotion) {
        assertTrue(style.transitionDuration.split(", ").all { it == "0s" })
    } else {
        assertTrue(style.transitionProperty.contains("grid-template-columns"))
    }
}

private suspend fun assertFabCollapses(
    fab: HTMLElement,
    root: org.w3c.dom.Element,
    expandedWidth: Double,
    reducedMotion: Boolean,
    collapse: () -> Unit,
) {
    val animationStart = nextFrameTime()
    collapse()
    var frameTime = animationStart
    var sawIntermediateWidth = false
    while (frameTime - animationStart < 300) {
        frameTime = nextFrameTime()
        val width = fab.getBoundingClientRect().width
        if (width > 56.0 && width < expandedWidth) sawIntermediateWidth = true
    }
    assertEquals("false", fab.getAttribute("data-extended"))
    assertEquals("56px", fab.style.minWidth)
    assertEquals("0fr", fab.style.getPropertyValue("grid-template-columns").substringAfterLast(' '))
    assertTrue(root.querySelector("[data-testid=extended-fab]") === fab)
    if (!reducedMotion) {
        assertTrue(sawIntermediateWidth, "FAB width should animate between its extended and fixed sizes")
    }
    val collapsedWidth = fab.getBoundingClientRect().width
    assertTrue(kotlin.math.abs(collapsedWidth - 56.0) < 1.0, "Collapsed standard FAB width was $collapsedWidth")
}

private suspend fun nextFrame() {
    suspendCoroutine<Unit> { continuation -> window.requestAnimationFrame { continuation.resume(Unit) } }
}

private suspend fun nextFrameTime(): Double =
    suspendCoroutine { continuation -> window.requestAnimationFrame { continuation.resume(it) } }

private suspend fun settleDom() {
    repeat(2) { nextFrame() }
}
