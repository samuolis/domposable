package io.github.samuolis.domposable.material

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTooltip
import io.github.samuolis.domposable.ui.htmlAttributes
import io.github.samuolis.domposable.ui.padding
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlTooltipTest {
    @Test
    @Suppress("LongMethod") // One popup lifetime covers activation, resizing and dismissal.
    fun tapHelpUsesTopLayerBoundsAndDismissesWithEscapeAndOutsidePointer() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            var open by mutableStateOf(false)
            var text by mutableStateOf("Saved estimate. Source: Elo.")
            var dismissals = 0
            val root = document.createElement("div") as HTMLElement
            document.body!!.appendChild(root)
            val composition =
                renderComposable(root) {
                    DomposableMaterialTheme {
                        Div(attrs = {
                            style {
                                property("position", "fixed")
                                property("right", "0")
                                property("bottom", "0")
                                property("overflow", "hidden")
                            }
                        }) {
                            DomposableTooltip(
                                text = text,
                                visible = open,
                                onDismiss = {
                                    dismissals++
                                    open = false
                                },
                                surfaceModifier =
                                    DomposableModifier.padding(4.dp).htmlAttributes {
                                        attr("data-help-surface", "")
                                    },
                            ) { DomposableButton("Win probability", { open = true }) }
                        }
                    }
                }
            try {
                settleTooltip()
                val button = root.querySelector("button") as HTMLElement
                val surface = root.querySelector("[data-help-surface]") as HTMLElement
                val popup = surface.closest(".d-tooltip-popup") as HTMLElement
                button.focus()
                settleTooltip()
                assertFalse(popup.matches(":popover-open"), "Controlled help must wait for activation")
                button.click()
                settleTooltip()
                assertTrue(popup.matches(":popover-open"), "Activation should open the top-layer host")
                assertEquals(surface.id, button.getAttribute("aria-describedby"))
                text = "Saved estimate. Source: Elo. ".repeat(20)
                settleTooltip()
                val bounds = popup.getBoundingClientRect()
                assertTrue(
                    bounds.left >= 7 && bounds.right <= window.innerWidth - 7,
                    "Horizontal bounds ${bounds.left}..${bounds.right}",
                )
                assertTrue(
                    bounds.top >= 7 && bounds.bottom <= window.innerHeight - 7,
                    "Vertical bounds ${bounds.top}..${bounds.bottom}",
                )
                assertTrue(
                    popup.contains(document.elementFromPoint(bounds.left + 10, bounds.top + 10)),
                    "Popup must paint above its clipped anchor",
                )

                button.dispatchEvent(
                    js("new KeyboardEvent('keydown', {key:'Escape', bubbles:true, cancelable:true})") as Event,
                )
                settleTooltip()
                assertFalse(popup.matches(":popover-open"))
                assertEquals(button, document.activeElement)
                assertEquals(1, dismissals)
                button.click()
                settleTooltip()
                document.body!!.dispatchEvent(js("new PointerEvent('pointerdown', {bubbles:true})") as Event)
                settleTooltip()
                assertFalse(popup.matches(":popover-open"))
                assertEquals(2, dismissals)
            } finally {
                composition.dispose()
                root.remove()
            }
        }

    private suspend fun settleTooltip() {
        repeat(3) {
            suspendCoroutine<Unit> { continuation -> window.requestAnimationFrame { continuation.resume(Unit) } }
        }
    }
}
