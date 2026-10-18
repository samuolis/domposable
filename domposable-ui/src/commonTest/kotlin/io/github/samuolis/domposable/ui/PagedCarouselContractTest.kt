package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PagedCarouselContractTest {
    private val labels =
        DomposableCarouselLabels("Previous", "Next") { position, total ->
            "$position of $total"
        }

    @Test
    fun pagedLayoutRequiresUsableViewportAndCardDimensions() {
        assertFailsWith<IllegalArgumentException> {
            DomposablePagedCarouselLayout(breakpoint = 0.dp, labels = labels)
        }
        assertFailsWith<IllegalArgumentException> {
            DomposablePagedCarouselLayout(compactHeight = 0.dp, labels = labels)
        }
        assertFailsWith<IllegalArgumentException> {
            DomposablePagedCarouselLayout(wideHeight = 0.dp, labels = labels)
        }
        assertFailsWith<IllegalArgumentException> {
            DomposablePagedCarouselLayout(heightTextScaleReference = 0.dp, labels = labels)
        }
        assertEquals(14f, DomposablePagedCarouselLayout(labels = labels).compactTrailingPeek.value)
    }

    @Test
    fun pagedCarouselKeepsStableUniqueKeysAndOptionalEndCallback() {
        val items = listOf(DomposableLazyItem("first") {}, DomposableLazyItem("second") {})
        val layout = DomposablePagedCarouselLayout(labels = labels)
        var approached = -1

        val props =
            DomposableCarouselProps(
                items = items,
                label = "Featured items",
                pagedLayout = layout,
                onApproachingEnd = { approached = it },
            )

        assertEquals(layout, props.pagedLayout)
        props.onApproachingEnd?.invoke(1)
        assertEquals(1, approached)
        assertFailsWith<IllegalArgumentException> {
            DomposableCarouselProps(items = listOf(items.first(), items.first()), label = "Items")
        }
    }
}
