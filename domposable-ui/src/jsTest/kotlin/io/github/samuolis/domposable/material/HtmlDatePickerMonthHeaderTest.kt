package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.ui.DomposableDatePicker
import io.github.samuolis.domposable.ui.DomposableDatePickerMonthHeader
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlDatePickerMonthHeaderTest {
    @Test
    fun monthHeaderNavigatesWithoutChangingTheSelectedDate() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            val root = document.createElement("div") as HTMLElement
            document.body!!.appendChild(root)
            var displayed = -1L
            var selected: Long? = 0L
            val composition =
                renderComposable(root) {
                    DomposableMaterialTheme {
                        DomposableDatePickerMonthHeader(header = { month, showMonth ->
                            displayed = month
                            Button(attrs = {
                                id("jump")
                                onClick { showMonth(2_678_400_000L) }
                            }) { Text("February") }
                            Button(attrs = {
                                id("outside")
                                onClick { showMonth(31_536_000_000L) }
                            }) { Text("Outside range") }
                        }) {
                            DomposableDatePicker(selected, { selected = it }, yearRange = 1970..1970)
                        }
                    }
                }
            try {
                (root.querySelector("#jump") as HTMLElement).click()
                repeat(3) {
                    suspendCoroutine<Unit> { continuation ->
                        window.requestAnimationFrame { continuation.resume(Unit) }
                    }
                }
                assertEquals(2_678_400_000L, displayed)
                (root.querySelector("#outside") as HTMLElement).click()
                repeat(3) {
                    suspendCoroutine<Unit> { continuation ->
                        window.requestAnimationFrame { continuation.resume(Unit) }
                    }
                }
                assertEquals(2_678_400_000L, displayed)
                assertEquals(0L, selected)
            } finally {
                composition.dispose()
                root.parentNode?.removeChild(root)
            }
        }
}
