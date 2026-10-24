package io.github.samuolis.domposable.material

import androidx.compose.runtime.mutableStateOf
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableCard
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.constrainContentWidth
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlNestedColumnWidthTest {
    @Test
    fun modalHeaderAndBodyFillDoNotResizeTheUnderlyingColumn() =
        runTest {
            val root = document.createElement("div") as HTMLElement
            document.body!!.appendChild(root)
            val open = mutableStateOf(false)
            val composition =
                renderComposable(root) {
                    DomposableMaterialTheme {
                        DomposableBox(modifier = DomposableModifier.width(DomposableLength(578f))) {
                            DomposableColumn(
                                modifier =
                                    DomposableModifier
                                        .constrainContentWidth(DomposableLength(200f))
                                        .testId("background-column"),
                                alignment = DomposableAlignment.Start,
                            ) {
                                DomposableText("i")
                                if (open.value) {
                                    DomposableDialog("Dialog", {}, headerContent = {
                                        DomposableColumn(
                                            modifier =
                                                DomposableModifier
                                                    .constrainContentWidth(DomposableLength(1000f))
                                                    .fillMaxWidth()
                                                    .testId("modal-wide-card"),
                                        ) { DomposableText("Header") }
                                    }) {
                                        DomposableCard(
                                            modifier = DomposableModifier.fillMaxWidth(),
                                        ) { DomposableText("Body") }
                                    }
                                }
                            }
                        }
                    }
                }
            try {
                settleWidthDemand(root)
                val background = root.querySelector("[data-testid=background-column]") as HTMLElement
                val initialWidth = background.offsetWidth
                assertTrue(initialWidth in 1..199, "background starts intrinsic")
                open.value = true
                settleWidthDemand(root)
                assertTrue(root.querySelector("dialog[open]") != null, "dialog is open")
                val modalCard = root.querySelector("[data-testid=modal-wide-card]") as HTMLElement
                assertTrue(modalCard.offsetWidth > 200, "modal content does not inherit its trigger's width cap")
                assertEquals(initialWidth, background.offsetWidth, "modal fill stays isolated from its caller")
                open.value = false
                settleWidthDemand(root)
                assertEquals(initialWidth, background.offsetWidth, "closing the modal leaves no stale width demand")
            } finally {
                composition.dispose()
                root.parentNode?.removeChild(root)
            }
        }

    @Test
    fun fillWidthCardUsesNearestBoundWhileIntrinsicColumnStaysContentSized() =
        runTest {
            val root = document.createElement("div") as HTMLElement
            document.body!!.appendChild(root)
            val showFill = mutableStateOf(true)
            val composition = renderComposable(root) { NestedColumnWidthFixture(showFill.value) }

            try {
                settleWidthDemand(root)
                nestedColumnWidthExpectations.forEach { expectation ->
                    val taggedElement = root.querySelector("[data-testid=${expectation.testId}]") as HTMLElement
                    val measuredElement =
                        if (expectation.measurePaddingAncestor) {
                            taggedElement.closest("[data-domposable-modifier=Padding]") as HTMLElement
                        } else {
                            taggedElement
                        }
                    assertEquals(expectation.expectedWidthDp, measuredElement.offsetWidth, expectation.label)
                }
                intrinsicColumnWidthTestIds.forEach { testId ->
                    val element = root.querySelector("[data-testid=$testId]") as HTMLElement
                    assertTrue(element.offsetWidth > 0 && element.offsetWidth < 200, "$testId should stay intrinsic")
                }

                showFill.value = false
                settleWidthDemand(root)
                assertTrue(
                    root.querySelector("[data-testid=dynamic-fill-card]") == null,
                    "removed fill child leaves no stale card",
                )
                assertTrue(
                    (root.querySelector("[data-testid=dynamic-demand-column]") as HTMLElement).offsetWidth < 200,
                    "removing the final fill child releases the automatic width demand",
                )
            } finally {
                composition.dispose()
                root.parentNode?.removeChild(root)
            }
        }
}

private suspend fun settleWidthDemand(root: HTMLElement) {
    var previousWidths = ""
    var stableFrames = 0
    repeat(30) {
        suspendCoroutine<Unit> { continuation -> window.requestAnimationFrame { continuation.resume(Unit) } }
        val nodes = root.querySelectorAll(".d-layout-column,.d-box,.d-row,.d-card")
        val widths = (0 until nodes.length).joinToString(",") { (nodes.item(it) as HTMLElement).offsetWidth.toString() }
        if (widths == previousWidths) {
            stableFrames += 1
            if (stableFrames >= 3) return
        } else {
            previousWidths = widths
            stableFrames = 0
        }
    }
    error("width demand did not settle within 30 animation frames")
}
