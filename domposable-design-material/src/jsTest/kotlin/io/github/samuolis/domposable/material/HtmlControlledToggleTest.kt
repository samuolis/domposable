package io.github.samuolis.domposable.material

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.ui.DomposableCheckbox
import io.github.samuolis.domposable.ui.DomposableRadioButton
import io.github.samuolis.domposable.ui.DomposableSlider
import io.github.samuolis.domposable.ui.DomposableSwitch
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLInputElement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlControlledToggleTest {
    @Test
    fun acceptingMixedSelectionClearsTheCustomDomProperty() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            val root = document.createElement("div")
            document.body!!.appendChild(root)
            var checked by mutableStateOf(false)
            var mixed by mutableStateOf(true)
            val composition =
                renderComposable(root) {
                    DomposableMaterialTheme {
                        DomposableCheckbox(checked, {
                            checked = it
                            mixed = false
                        }, "Mixed", indeterminate = mixed)
                    }
                }
            try {
                val input = root.querySelector("input") as HTMLInputElement
                input.click()
                settleControls()
                assertTrue(input.checked)
                assertFalse(input.asDynamic().indeterminate as Boolean)
            } finally {
                composition.dispose()
                root.parentNode?.removeChild(root)
            }
        }

    @Test
    fun declinedChangesPreserveTheDeclaredSelectionStates() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            val root = document.createElement("div")
            document.body!!.appendChild(root)
            val requested = mutableListOf<Boolean>()
            var radioRequests = 0
            val sliderRequests = mutableListOf<Float>()
            val composition =
                renderComposable(root) {
                    DomposableMaterialTheme {
                        DomposableCheckbox(false, { requested += it }, "Checkbox", showLabel = false)
                        DomposableSwitch(false, { requested += it }, "Switch", showLabel = false)
                        DomposableCheckbox(false, {
                            requested += it
                        }, "Mixed", showLabel = false, indeterminate = true, description = "External description")
                        DomposableRadioButton(true, { radioRequests++ }, "First", groupName = "controlled")
                        DomposableRadioButton(false, { radioRequests++ }, "Second", groupName = "controlled")
                        DomposableSlider(0.5f, { sliderRequests += it }, "Slider")
                    }
                }
            try {
                val inputs = root.querySelectorAll("input[type=checkbox]")
                for (index in 0 until inputs.length) {
                    val input = inputs.item(index) as HTMLInputElement
                    assertEquals("SPAN", input.parentElement?.tagName)
                    (input.parentElement as org.w3c.dom.HTMLElement).click()
                    settleControls()
                    assertFalse(input.checked)
                }
                assertEquals(listOf(true, true, true), requested)
                assertTrue((inputs.item(2) as HTMLInputElement).asDynamic().indeterminate as Boolean)
                val radios = root.querySelectorAll("input[type=radio]")
                val first = radios.item(0) as HTMLInputElement
                val second = radios.item(1) as HTMLInputElement
                second.click()
                settleControls()
                assertTrue(first.checked)
                assertFalse(second.checked)
                assertEquals(1, radioRequests)
                val slider = root.querySelector("input[type=range]") as HTMLInputElement
                slider.value = "0.9"
                slider.dispatchEvent(
                    org.w3c.dom.events
                        .Event("input"),
                )
                settleControls()
                assertEquals("0.5", slider.value)
                assertEquals(listOf(0.9f), sliderRequests)
            } finally {
                composition.dispose()
                root.parentNode?.removeChild(root)
            }
        }
}

private suspend fun settleControls() {
    repeat(2) {
        suspendCoroutine<Unit> { continuation -> window.requestAnimationFrame { continuation.resume(Unit) } }
    }
}
