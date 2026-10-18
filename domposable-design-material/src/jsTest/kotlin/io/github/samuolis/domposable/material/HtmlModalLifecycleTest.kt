package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBottomSheet
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableModalAlignment
import io.github.samuolis.domposable.ui.DomposableModalLayout
import io.github.samuolis.domposable.ui.DomposableText
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlModalLifecycleTest {
    @Test
    fun fallbackWithoutControlsKeepsFocusInside() {
        withFallbackDialogs {
            withHost(content = {
                DomposableDialog(
                    title = "Text-only modal",
                    onDismiss = {},
                    headerContent = { DomposableText("Custom heading") },
                ) { DomposableText("Informational content") }
            }) { root ->
                val dialog = root.querySelector("dialog") as HTMLElement
                assertEquals("-1", dialog.getAttribute("tabindex"))
                assertEquals(dialog, document.activeElement)
                val tab = keyboardEvent("Tab")
                document.dispatchEvent(tab)
                assertTrue(tab.defaultPrevented)
                assertEquals(dialog, document.activeElement)
            }
        }
    }

    @Test
    fun fallbackControlsAcceptKeyboardGeneratedClicks() {
        withFallbackDialogs {
            var actions = 0
            withHost(content = {
                DomposableDialog("Keyboard action", {}) {
                    Button(attrs = {
                        attr("id", "fallback-action")
                        onClick { actions++ }
                    }) { DomposableText("Action") }
                }
            }) { root ->
                val button = root.querySelector("#fallback-action") as HTMLElement
                button.dispatchEvent(js("new MouseEvent('click', {bubbles:true, cancelable:true, detail:0})") as Event)
                assertEquals(1, actions)
            }
        }
    }

    @Test
    fun onlyTopmostModalHandlesOutsidePointerAndEscape() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            withFallbackDialogs {
                val parentVisible = mutableStateOf(true)
                val childVisible = mutableStateOf(true)
                var parentDismissals = 0
                var childDismissals = 0
                withModalHost(content = {
                    if (parentVisible.value) {
                        DomposableDialog("Parent", {
                            parentDismissals++
                            parentVisible.value = false
                        }) { DomposableText("Parent content") }
                    }
                    if (childVisible.value) {
                        DomposableDialog("Child", {
                            childDismissals++
                            childVisible.value = false
                        }) { DomposableText("Child content") }
                    }
                }) { root ->
                    assertEquals(2, root.querySelectorAll("dialog[open]").length)
                    val background = document.createElement("button") as HTMLElement
                    document.body!!.appendChild(background)
                    var backgroundActions = 0
                    background.addEventListener("click", { backgroundActions++ })
                    try {
                        background.dispatchEvent(pointerEvent("pointerdown", 10, -1.0, -1.0))
                        background.dispatchEvent(pointerEvent("pointerup", 10, -1.0, -1.0))
                        background.dispatchEvent(mouseClick(-1.0, -1.0))
                        assertEquals(0, backgroundActions)
                        assertEquals(0, parentDismissals)
                        assertEquals(1, childDismissals)
                        settleModals()
                        assertEquals(1, root.querySelectorAll("dialog[open]").length)

                        val escape = keyboardEvent("Escape")
                        document.dispatchEvent(escape)
                        assertEquals(1, parentDismissals)
                        assertEquals(1, childDismissals)
                        settleModals()
                        assertEquals(0, root.querySelectorAll("dialog[open]").length)
                    } finally {
                        background.parentNode?.removeChild(background)
                    }
                }
            }
        }

    @Test
    fun closingParentUnderChildDoesNotRestoreFocusOverTheChild() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            withFallbackDialogs {
                val parentVisible = mutableStateOf(true)
                val childVisible = mutableStateOf(true)
                withModalHost(content = {
                    if (parentVisible.value) {
                        DomposableDialog("Parent", { parentVisible.value = false }) {
                            DomposableText("Parent content")
                        }
                    }
                    if (childVisible.value) {
                        DomposableDialog("Child", { childVisible.value = false }) { DomposableText("Child content") }
                    }
                }) { root ->
                    val child = root.querySelectorAll("dialog").item(1) as HTMLElement
                    assertTrue(child.contains(document.activeElement as? org.w3c.dom.Node))
                    parentVisible.value = false
                    settleModals()
                    assertEquals(1, root.querySelectorAll("dialog[open]").length)
                    assertTrue(child.contains(document.activeElement as? org.w3c.dom.Node))
                }
            }
        }

    @Test
    fun sheetDragIgnoresInteractiveHeaderControls() {
        withFallbackDialogs {
            var dismissals = 0
            withHost(content = {
                DomposableBottomSheet(
                    "Options",
                    { dismissals++ },
                    headerContent = { dismiss ->
                        Button(attrs = {
                            attr("aria-label", "Dismiss options")
                            onClick { dismiss() }
                        }) { DomposableText("Close") }
                    },
                ) { DomposableText("Sheet content") }
            }) { root ->
                val closeButton = root.querySelector(".d-dialog-header button") as HTMLElement
                val bounds = closeButton.getBoundingClientRect()
                closeButton.dispatchEvent(pointerEvent("pointerdown", 21, bounds.left + 2, bounds.top + 2))
                closeButton.dispatchEvent(pointerEvent("pointermove", 21, bounds.left + 2, bounds.top + 220))
                closeButton.dispatchEvent(pointerEvent("pointerup", 21, bounds.left + 2, bounds.top + 220))
                assertEquals(0, dismissals)
            }
        }
    }

    @Test
    fun bottomSheetDragHandleFollowsCompactBreakpoint() {
        withHost(content = {
            DomposableBottomSheet(
                "Adaptive sheet",
                {},
                layout = DomposableModalLayout(alignment = DomposableModalAlignment.BottomOnCompact),
            ) { DomposableText("Sheet content") }
        }) { root ->
            val sheet = root.querySelector("dialog") as HTMLElement
            val handle = sheet.querySelector(".d-sheet-drag-handle") as HTMLElement
            val compactViewport = window.matchMedia("(max-width: 40rem)").matches
            assertEquals(
                if (compactViewport) "flex" else "none",
                window.getComputedStyle(handle).display,
            )
        }
    }

    @Test
    fun hiddenSheetDragHandleDisablesHeaderDrag() {
        withFallbackDialogs {
            var dismissals = 0
            withHost(content = {
                DomposableBottomSheet(
                    "Adaptive sheet",
                    { dismissals++ },
                    layout = DomposableModalLayout(alignment = DomposableModalAlignment.BottomOnCompact),
                ) { DomposableText("Sheet content") }
            }) { root ->
                val sheet = root.querySelector("dialog") as HTMLElement
                val handle = sheet.querySelector(".d-sheet-drag-handle") as HTMLElement
                handle.style.display = "none"
                assertEquals("none", window.getComputedStyle(handle).display)

                val header = sheet.querySelector(".d-dialog-header") as HTMLElement
                val bounds = header.getBoundingClientRect()
                header.dispatchEvent(pointerEvent("pointerdown", 22, bounds.left + 2, bounds.top + 2))
                header.dispatchEvent(pointerEvent("pointermove", 22, bounds.left + 2, bounds.top + 220))
                header.dispatchEvent(pointerEvent("pointerup", 22, bounds.left + 2, bounds.top + 220))
                assertEquals(0, dismissals)
            }
        }
    }

    @Test
    fun modalLayoutBoundsTheDialogSurface() {
        withHost(content = {
            DomposableDialog(
                "Bounded",
                {},
                layout = DomposableModalLayout(maxWidth = 240.dp, fillViewportWidth = true),
            ) { DomposableText("Dialog body") }
        }) { root ->
            val dialog = root.querySelector("dialog") as HTMLElement
            val style = window.getComputedStyle(dialog)
            assertEquals("240px", style.maxWidth)
            assertEquals("calc(100dvw - 32px)", dialog.style.width)
        }
    }

    @Test
    fun defaultCloseRemainsAfterDialogContentAndFirstActionGetsFocus() {
        withHost(content = {
            DomposableDialog(
                "Legacy order",
                {},
                actions = { DomposableButton("Confirm", {}) },
            ) {
                DomposableButton("First action", {})
            }
        }) { root ->
            val dialog = root.querySelector("dialog") as HTMLElement
            val firstAction = dialog.querySelector("button") as HTMLElement
            val defaultClose = dialog.lastElementChild as HTMLElement

            assertEquals("Legacy order", dialog.firstElementChild?.textContent)
            assertEquals("First action", firstAction.textContent)
            assertEquals("Close", defaultClose.textContent)
            assertEquals(dialog, defaultClose.parentElement)
            assertEquals(null, defaultClose.getAttribute("aria-label"))
            assertEquals(firstAction, document.activeElement)
        }
    }

    @Test
    fun nativeAndFallbackScrimsUseTranslucentMaterialColor() {
        withHost(content = {
            DomposableDialog("Native scrim", {}) { DomposableText("Dialog content") }
        }) { root ->
            val dialog = root.querySelector("dialog") as HTMLElement
            val backdropColor = window.asDynamic().getComputedStyle(dialog, "::backdrop").backgroundColor as String
            assertTrue(
                backdropColor.contains("0.32"),
                "Native backdrop should apply the Material scrim alpha: $backdropColor",
            )
        }

        withFallbackDialogs {
            withHost(content = {
                DomposableDialog("Fallback scrim", {}) { DomposableText("Dialog content") }
            }) { root ->
                val scrim = document.querySelector(".d-dialog-fallback-scrim") as HTMLElement
                val backgroundColor = window.getComputedStyle(scrim).backgroundColor
                assertTrue(
                    backgroundColor.contains("0.32"),
                    "Fallback scrim should apply the Material scrim alpha: $backgroundColor",
                )
            }
        }
    }
}

private inline fun withFallbackDialogs(block: () -> Unit) {
    val prototype = js("HTMLDialogElement.prototype")
    val originalShowModal = prototype.showModal
    prototype.showModal = null
    try {
        block()
    } finally {
        prototype.showModal = originalShowModal
    }
}

private fun pointerEvent(
    type: String,
    pointerId: Int,
    x: Double,
    y: Double,
): Event {
    val options = js("({ bubbles: true, cancelable: true })")
    options.pointerId = pointerId
    options.isPrimary = true
    options.button = 0
    options.clientX = x
    options.clientY = y
    val createEvent = js("(type, options) => new PointerEvent(type, options)")
    return createEvent(type, options) as Event
}

private fun mouseClick(
    x: Double,
    y: Double,
): Event {
    val createClick =
        js("(x, y) => new MouseEvent('click', { bubbles: true, cancelable: true, clientX: x, clientY: y })")
    return createClick(x, y) as Event
}

private fun keyboardEvent(key: String): Event {
    val createKeyboardEvent = js("(key) => new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true })")
    return createKeyboardEvent(key) as Event
}

private suspend fun withModalHost(
    content: @Composable () -> Unit,
    verify: suspend (HTMLElement) -> Unit,
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

private suspend fun settleModals() {
    repeat(2) {
        suspendCoroutine<Unit> { continuation -> window.requestAnimationFrame { continuation.resume(Unit) } }
    }
}
