package io.github.samuolis.domposable.material

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableCarousel
import io.github.samuolis.domposable.ui.DomposableCarouselLabels
import io.github.samuolis.domposable.ui.DomposableLazyItem
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposablePagedCarouselLayout
import io.github.samuolis.domposable.ui.width
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlPagedCarouselTest {
    @Test
    fun keyboardRtlControlledSelectionAndStableKeysStaySynchronized() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            val root = document.createElement("div")
            document.body!!.appendChild(root)
            var selected by mutableStateOf(1)
            var items by mutableStateOf(items())
            val changed = mutableListOf<Int>()
            val composition =
                renderComposable(root) {
                    DomposableMaterialTheme {
                        DomposableCarousel(
                            items = items,
                            label = "Featured cards",
                            modifier = DomposableModifier.width(360.dp),
                            currentItem = selected,
                            onCurrentItemChange = {
                                changed += it
                                selected = it
                            },
                            pagedLayout = layout(compactHeight = 120, wideHeight = 180),
                        )
                    }
                }
            try {
                settle()
                val track = root.querySelector(".d-paged-carousel-track") as HTMLElement
                val indicators = root.querySelectorAll(".d-paged-carousel-indicators button")
                assertEquals(
                    "true",
                    (indicators.item(1) as HTMLElement).getAttribute("aria-current"),
                    "Initial controlled selection",
                )

                track.dispatchEvent(key("ArrowRight"))
                settle()
                assertEquals(2, selected, "LTR ArrowRight selects the next slide")
                assertEquals(listOf(2), changed)

                track.style.direction = "rtl"
                track.dispatchEvent(key("ArrowRight"))
                settle()
                assertEquals(1, selected, "RTL ArrowRight selects the previous slide")
                assertEquals(listOf(2, 1), changed)

                selected = 0
                settleUntil {
                    indicator(root, 0).getAttribute("aria-current") == "true"
                }
                assertEquals("true", indicator(root, 0).getAttribute("aria-current"))

                val firstSlide = root.querySelectorAll(".d-paged-carousel-slide").item(0) as HTMLElement
                items = listOf(items[1], items[0], items[2])
                settle()
                assertEquals("1", firstSlide.getAttribute("data-index"))
                assertEquals(firstSlide, root.querySelectorAll(".d-paged-carousel-slide").item(1))
            } finally {
                composition.dispose()
                root.parentNode?.removeChild(root)
            }
        }

    @Test
    fun responsiveStyleUsesWideHeightAndContentSizedPagesRemainUnforced() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            val root = document.createElement("div")
            document.body!!.appendChild(root)
            var width by mutableStateOf(720)
            val composition =
                renderComposable(root) {
                    DomposableMaterialTheme {
                        DomposableCarousel(
                            items = items().take(1),
                            label = "Single card",
                            modifier = DomposableModifier.width(width.dp),
                            pagedLayout = layout(compactHeight = null, wideHeight = 180),
                        )
                    }
                }
            try {
                settle()
                val carousel = root.querySelector(".d-carousel-paged") as HTMLElement
                assertTrue(
                    carousel.classList.contains("d-carousel-paged-wide"),
                    "720px container uses wide layout: ${carousel.outerHTML}",
                )
                assertEquals("180px", carousel.style.getPropertyValue("--d-paged-carousel-wide-height"))
                val slide = root.querySelector(".d-paged-carousel-slide") as HTMLElement
                assertTrue(slide.offsetHeight > 0)
                assertEquals(180, slide.offsetHeight, "Wide height applies after measurement")
                width = 360
                settle()
                settle()
                assertTrue(
                    carousel.classList.contains("d-carousel-paged-compact"),
                    "360px container returns to compact layout",
                )
                assertTrue(slide.offsetHeight in 1 until 180, "Null compact height preserves content sizing")
                assertEquals(0, root.querySelectorAll(".d-paged-carousel-indicators").length)
                assertEquals(0, root.querySelectorAll(".d-paged-carousel-navigation").length)
            } finally {
                composition.dispose()
                root.parentNode?.removeChild(root)
            }
        }

    private fun indicator(
        root: org.w3c.dom.Element,
        index: Int,
    ): HTMLElement = root.querySelectorAll(".d-paged-carousel-indicators button").item(index) as HTMLElement

    private fun items() =
        listOf("a" to "Alpha", "b" to "Beta", "c" to "Gamma").map { (key, label) ->
            DomposableLazyItem(key) { Text(label) }
        }

    private fun layout(
        compactHeight: Int?,
        wideHeight: Int?,
    ) = DomposablePagedCarouselLayout(
        compactHeight = compactHeight?.dp,
        wideHeight = wideHeight?.dp,
        labels = DomposableCarouselLabels("Previous", "Next") { position, total -> "Slide $position of $total" },
    )

    private fun key(name: String): Event {
        val createKeyboardEvent = js("(key) => new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true })")
        return createKeyboardEvent(name) as Event
    }

    private suspend fun settleUntil(condition: () -> Boolean) {
        repeat(20) {
            if (condition()) return
            settle()
        }
        assertTrue(condition(), "Carousel did not settle within 40 animation frames")
    }

    private suspend fun settle() =
        suspendCoroutine<Unit> { continuation ->
            window.requestAnimationFrame { window.requestAnimationFrame { continuation.resume(Unit) } }
        }
}
