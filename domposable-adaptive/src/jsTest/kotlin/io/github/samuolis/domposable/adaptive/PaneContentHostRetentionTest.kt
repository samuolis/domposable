package io.github.samuolis.domposable.adaptive

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableScrollState
import io.github.samuolis.domposable.ui.border
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.rememberDomposableScrollState
import io.github.samuolis.domposable.ui.verticalScroll
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class PaneContentHostRetentionTest {
    @Test
    @Suppress("LongMethod")
    fun responsivePaneSizingKeepsContentScrollAndFocusNodesInPlace() =
        runTest {
            val root = document.createElement("div") as HTMLElement
            root.style.width = "1000px"
            root.style.height = "400px"
            root.style.display = "flex"
            root.style.alignItems = "flex-start"
            document.body!!.appendChild(root)

            val expanded = mutableStateOf(true)
            val listLabel = mutableStateOf("Directory")
            val renderedListState = mutableStateOf<MutableState<Int>?>(null)
            val renderedScrollState = mutableStateOf<DomposableScrollState?>(null)
            val composition =
                renderComposable(root) {
                    val isExpanded = expanded.value
                    val scrollState = rememberDomposableScrollState()
                    SideEffect { renderedScrollState.value = scrollState }
                    val currentListLabel = listLabel.value
                    val listModifier =
                        DomposableModifier
                            .border(
                                width = if (isExpanded) 1.dp else 0.dp,
                                color = DomposableColor(0xFF777777),
                                edge = io.github.samuolis.domposable.ui.DomposableBorderEdge.End,
                            ).padding(DomposableInsets(end = if (isExpanded) 20.dp else 0.dp))
                            .verticalScroll(scrollState)

                    DomposablePaneContentHost(
                        pane = DomposablePaneScaffoldPane.List,
                        expanded = isExpanded,
                        visible = isExpanded,
                        label = "Organization directory",
                        widthFraction = 0.32f,
                        minListWidth = 260.dp,
                        maxListWidth = 360.dp,
                        modifier = listModifier,
                    ) {
                        val count = remember { mutableStateOf(0) }
                        SideEffect { renderedListState.value = count }
                        Div(attrs = { attr("data-testid", "list-screen") }) {
                            Text("$currentListLabel:${count.value}")
                            Div(attrs = {
                                attr("data-testid", "nested-scroll")
                                style {
                                    property("height", "40px")
                                    property("overflow-y", "auto")
                                }
                            }) {
                                Div(attrs = { style { property("height", "300px") } })
                            }
                        }
                    }

                    DomposablePaneContentHost(
                        pane = DomposablePaneScaffoldPane.Detail,
                        expanded = isExpanded,
                        visible = true,
                        label = "Organization details",
                        widthFraction = 1f,
                        minListWidth = 260.dp,
                        maxListWidth = 360.dp,
                        modifier = DomposableModifier,
                    ) {
                        Div(attrs = { attr("data-testid", "detail-screen") }) {
                            Text("Organization detail")
                            Input(InputType.Text, attrs = { attr("data-testid", "detail-input") })
                        }
                    }
                }

            try {
                settleComposition()
                val listScreen = root.querySelector("[data-testid='list-screen']") as HTMLElement
                val nestedScroll = root.querySelector("[data-testid='nested-scroll']") as HTMLElement
                val detailScreen = root.querySelector("[data-testid='detail-screen']") as HTMLElement
                val detailInput = root.querySelector("[data-testid='detail-input']") as HTMLElement
                val listHost = root.querySelector("[data-domposable-pane-host='list']") as HTMLElement
                val detailHost = root.querySelector("[data-domposable-pane-host='detail']") as HTMLElement
                val statefulScroll =
                    listHost.querySelector("[data-domposable-modifier='StatefulScroll']") as HTMLElement
                val listRegion =
                    listHost.querySelector("[role='region'][aria-label='Organization directory']") as HTMLElement
                val listBorder = listHost.querySelector("[data-domposable-modifier='EdgeBorder']") as HTMLElement
                statefulScroll.style.height = "40px"
                val scrollState = requireNotNull(renderedScrollState.value)
                val rememberedListState = requireNotNull(renderedListState.value)

                assertEquals(320.0, listHost.getBoundingClientRect().width)
                assertEquals(400.0, listHost.getBoundingClientRect().height)
                assertEquals(400.0, listRegion.getBoundingClientRect().height)
                assertEquals(400.0, listBorder.getBoundingClientRect().height)
                assertEquals(680.0, detailHost.getBoundingClientRect().width)
                assertEquals(1, root.querySelectorAll("[role='region'][aria-label='Organization directory']").length)
                assertEquals(1, root.querySelectorAll("[role='region'][aria-label='Organization details']").length)

                nestedScroll.scrollTop = 20.0
                scrollState.scrollTo(10)
                settleComposition()
                assertEquals(20.0, nestedScroll.scrollTop, "before resize")
                assertEquals(10.0, statefulScroll.scrollTop, "before resize")
                detailInput.focus()
                assertSame(detailInput, document.activeElement)

                rememberedListState.value++
                listLabel.value = "Updated directory"
                expanded.value = false
                settleComposition()

                assertEquals("none", listHost.style.display)
                assertEquals("true", listHost.getAttribute("aria-hidden"))
                assertSame(listScreen, root.querySelector("[data-testid='list-screen']"))
                assertSame(nestedScroll, root.querySelector("[data-testid='nested-scroll']"))
                assertSame(statefulScroll, listHost.querySelector("[data-domposable-modifier='StatefulScroll']"))
                assertSame(detailScreen, root.querySelector("[data-testid='detail-screen']"))
                assertSame(detailInput, document.activeElement)
                assertSame(rememberedListState, renderedListState.value)
                assertEquals("Updated directory:1", listScreen.textContent)

                listLabel.value = "Latest directory"
                expanded.value = true
                settleComposition()

                assertEquals("grid", listHost.style.display)
                assertEquals("", listHost.style.position)
                assertEquals("", listHost.style.visibility)
                assertSame(listScreen, root.querySelector("[data-testid='list-screen']"))
                assertSame(nestedScroll, root.querySelector("[data-testid='nested-scroll']"))
                assertSame(statefulScroll, listHost.querySelector("[data-domposable-modifier='StatefulScroll']"))
                assertSame(detailScreen, root.querySelector("[data-testid='detail-screen']"))
                assertSame(detailInput, document.activeElement)
                assertSame(rememberedListState, renderedListState.value)
                assertEquals("Latest directory:1", listScreen.textContent)
                assertEquals(20.0, nestedScroll.scrollTop, "after expand")
                assertEquals(10.0, statefulScroll.scrollTop, "after expand")
                assertEquals(320.0, listHost.getBoundingClientRect().width)
                assertEquals(400.0, listHost.getBoundingClientRect().height)
                assertEquals(400.0, listRegion.getBoundingClientRect().height)
                assertEquals(400.0, listBorder.getBoundingClientRect().height)
                assertEquals(680.0, detailHost.getBoundingClientRect().width)
            } finally {
                composition.dispose()
                root.parentNode?.removeChild(root)
            }
        }

    private suspend fun settleComposition() {
        repeat(2) {
            suspendCoroutine<Unit> { continuation -> window.requestAnimationFrame { continuation.resume(Unit) } }
        }
    }
}
