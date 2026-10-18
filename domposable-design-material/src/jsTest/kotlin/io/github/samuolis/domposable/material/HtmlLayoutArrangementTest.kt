package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableArrangement
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableFlowRow
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.horizontalScroll
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlLayoutArrangementTest {
    @Test
    fun distributedRowFillsTheAvailableWidth() =
        withLayoutHost(
            content = {
                DomposableRow(
                    modifier = DomposableModifier.width(300.dp),
                    gap = 12.dp,
                    arrangement = DomposableArrangement.SpaceBetween,
                ) {
                    repeat(3) { index ->
                        DomposableBox(DomposableModifier.width(40.dp).height(20.dp).testId("space-$index")) {}
                    }
                }
            },
        ) { root ->
            val divs = root.querySelectorAll("div")
            val row =
                (0 until divs.length)
                    .map { divs.item(it) as HTMLElement }
                    .first { it.style.justifyContent == "space-between" }
            val first = root.querySelector("[data-testid='space-0']") as HTMLElement
            val second = root.querySelector("[data-testid='space-1']") as HTMLElement
            val third = root.querySelector("[data-testid='space-2']") as HTMLElement
            assertEquals(0.0, first.getBoundingClientRect().left - row.getBoundingClientRect().left, 0.5)
            assertEquals(130.0, second.getBoundingClientRect().left - first.getBoundingClientRect().left, 0.5)
            assertEquals(130.0, third.getBoundingClientRect().left - second.getBoundingClientRect().left, 0.5)
        }

    @Test
    fun flowRowSeparatesItemAndLineGapsAndAlignsItemsWithinEachLine() =
        withLayoutHost(
            content = {
                DomposableFlowRow(
                    modifier = DomposableModifier.width(130.dp),
                    gap = 10.dp,
                    arrangement = DomposableArrangement.Start,
                    alignment = DomposableAlignment.End,
                    rowGap = 30.dp,
                ) {
                    DomposableBox(DomposableModifier.width(60.dp).height(40.dp).testId("flow-tall")) {}
                    DomposableBox(DomposableModifier.width(60.dp).height(20.dp).testId("flow-short")) {}
                    DomposableBox(DomposableModifier.width(60.dp).height(10.dp).testId("flow-next-line")) {}
                }
            },
        ) { root ->
            val tall = root.querySelector("[data-testid='flow-tall']") as HTMLElement
            val short = root.querySelector("[data-testid='flow-short']") as HTMLElement
            val nextLine = root.querySelector("[data-testid='flow-next-line']") as HTMLElement
            assertEquals(70.0, short.getBoundingClientRect().left - tall.getBoundingClientRect().left, 0.5)
            assertEquals(20.0, short.getBoundingClientRect().top - tall.getBoundingClientRect().top, 0.5)
            assertEquals(70.0, nextLine.getBoundingClientRect().top - tall.getBoundingClientRect().top, 0.5)
        }

    @Test
    fun horizontalScrollRowKeepsChildrenAtTheirIntrinsicWidths() =
        withLayoutHost(
            content = {
                DomposableRow(
                    modifier = DomposableModifier.width(100.dp).horizontalScroll(),
                    gap = 0.dp,
                ) {
                    DomposableBox(DomposableModifier.width(80.dp).height(20.dp).testId("scroll-first")) {}
                    DomposableBox(DomposableModifier.width(80.dp).height(20.dp).testId("scroll-second")) {}
                }
            },
        ) { root ->
            val divs = root.querySelectorAll("div")
            val first = root.querySelector("[data-testid='scroll-first']") as HTMLElement
            val second = root.querySelector("[data-testid='scroll-second']") as HTMLElement
            val viewport =
                (0 until divs.length)
                    .map { divs.item(it) as HTMLElement }
                    .first { it.style.overflowX == "auto" }
            assertEquals(80, first.offsetWidth)
            assertEquals(80, second.offsetWidth)
            assertEquals(80.0, second.getBoundingClientRect().left - first.getBoundingClientRect().left, 0.5)
            assertEquals(100, viewport.clientWidth)
            assertTrue(viewport.scrollWidth > viewport.clientWidth)
        }
}

private fun withLayoutHost(
    content: @Composable () -> Unit,
    verify: (HTMLElement) -> Unit,
) {
    val root = document.createElement("div") as HTMLElement
    document.body!!.appendChild(root)
    val composition = renderComposable(root) { DomposableMaterialTheme { content() } }
    try {
        verify(root)
    } finally {
        composition.dispose()
        root.parentNode?.removeChild(root)
    }
}
