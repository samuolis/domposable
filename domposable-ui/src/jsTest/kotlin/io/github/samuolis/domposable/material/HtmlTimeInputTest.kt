package io.github.samuolis.domposable.material

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.ui.DomposableTimePicker
import io.github.samuolis.domposable.ui.DomposableTimePickerMode
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlTimeInputTest {
    @Test
    fun twelveHourInputChangesPeriodWithoutChangingTheDisplayedHour() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            val root = document.createElement("div")
            document.body!!.appendChild(root)
            var hour by mutableStateOf(0)
            val composition =
                renderComposable(root) {
                    DomposableMaterialTheme {
                        DomposableTimePicker(
                            hour,
                            15,
                            { h, _ -> hour = h },
                            is24Hour = false,
                            mode = DomposableTimePickerMode.Input,
                        )
                    }
                }

            suspend fun settle() {
                repeat(2) {
                    suspendCoroutine<Unit> { continuation ->
                        window.requestAnimationFrame { continuation.resume(Unit) }
                    }
                }
            }
            try {
                val input = root.querySelector("input[aria-label=Hour]") as HTMLInputElement
                assertEquals("12", input.value)
                val typography = window.getComputedStyle(input)
                assertEquals(typography.getPropertyValue("--d-type-display-large-size").trim(), typography.fontSize)
                (root.querySelector(".d-time-period button:last-child") as HTMLElement).click()
                settle()
                assertEquals(12, hour)
                assertEquals("12", input.value)
                input.value = "3"
                input.dispatchEvent(
                    org.w3c.dom.events
                        .Event("input"),
                )
                settle()
                assertEquals(15, hour)
                (root.querySelector(".d-time-period button:first-child") as HTMLElement).click()
                settle()
                assertEquals(3, hour)
            } finally {
                composition.dispose()
                root.parentNode?.removeChild(root)
            }
        }
}
