package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBottomSheet
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableModalAlignment
import io.github.samuolis.domposable.ui.DomposableModalLayout
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxSize
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.minHeight
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll
import io.github.samuolis.domposable.ui.weight
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
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
    fun sheetSwipeUsesPointerUpPositionWhenMoveEventsAreCoalesced() {
        withFallbackDialogs {
            var dismissals = 0
            withHost(content = {
                DomposableBottomSheet("Swipe", { dismissals++ }) { DomposableText("Sheet content") }
            }) { root ->
                val sheet = root.querySelector("dialog") as HTMLElement
                val handle = sheet.querySelector(".d-sheet-drag-handle") as HTMLElement
                assertEquals("-1", sheet.getAttribute("tabindex"))
                handle.asDynamic().setPointerCapture = js("(pointerId) => {}")
                handle.asDynamic().hasPointerCapture = js("(pointerId) => false")
                val bounds = handle.getBoundingClientRect()
                val x = bounds.left + bounds.width / 2
                val y = bounds.top + bounds.height / 2

                handle.dispatchEvent(pointerEvent("pointerdown", 23, x, y))
                handle.dispatchEvent(pointerEvent("pointerup", 23, x + 180, y + 130))
                assertEquals(0, dismissals, "A horizontally dominant release must not dismiss")

                handle.dispatchEvent(pointerEvent("pointerdown", 24, x, y))
                handle.dispatchEvent(pointerEvent("pointermove", 24, x, y + 180))
                handle.dispatchEvent(pointerEvent("pointerup", 24, x, y + 20))
                assertEquals(0, dismissals, "The release position, not the earlier peak, determines distance")

                handle.dispatchEvent(pointerEvent("pointerdown", 25, x, y))
                handle.dispatchEvent(pointerEvent("pointerup", 25, x, y + 180))

                assertEquals(1, dismissals)
            }
        }
    }

    @Test
    fun longSheetExpandsThenCollapsesBeforeDismissal() {
        withFallbackDialogs {
            var dismissals = 0
            val layout = DomposableModalLayout(maxViewportFraction = 1f, viewportInset = 16.dp)
            withHost(content = {
                DomposableBottomSheet(
                    "Long sheet",
                    { dismissals++ },
                    layout = layout,
                ) { DomposableText("Long content") }
            }) { root ->
                val sheet = root.querySelector("dialog") as HTMLElement
                val handle = sheet.querySelector(".d-sheet-drag-handle") as HTMLElement
                handle.style.display = "flex"
                val setScrollMetrics: dynamic = js("(element) => { Object.defineProperty(element, 'scrollHeight', {configurable: true, get: () => 900}); Object.defineProperty(element, 'clientHeight', {configurable: true, get: () => 400}); }")
                setScrollMetrics(sheet)
                handle.asDynamic().setPointerCapture = js("(pointerId) => {}")
                handle.asDynamic().hasPointerCapture = js("(pointerId) => false")
                val bounds = handle.getBoundingClientRect()
                val x = bounds.left + bounds.width / 2
                val y = bounds.top + bounds.height / 2

                handle.dispatchEvent(pointerEvent("pointerdown", 31, x, y))
                handle.dispatchEvent(pointerEvent("pointerup", 31, x, y - 160))
                assertTrue(sheet.classList.contains("d-sheet-expanded"))
                assertEquals(layout.modalMaxHeightCss(), sheet.style.getPropertyValue("max-height"))
                assertEquals(0, dismissals)

                handle.dispatchEvent(pointerEvent("pointerdown", 32, x, y))
                handle.dispatchEvent(pointerEvent("pointerup", 32, x, y + 160))
                assertTrue(!sheet.classList.contains("d-sheet-expanded"))
                assertEquals(layout.partialSheetMaxHeightCss(), sheet.style.getPropertyValue("max-height"))
                assertEquals(0, dismissals)

                handle.dispatchEvent(pointerEvent("pointerdown", 33, x, y))
                handle.dispatchEvent(pointerEvent("pointerup", 33, x, y + 160))
                assertEquals(1, dismissals)
            }
        }
    }

    @Test
    fun fullHeightRespectsTheExplicitPartialExpansionOption() {
        val layout = DomposableModalLayout(heightFraction = 1f, maxViewportFraction = 1f, viewportInset = 16.dp)
        withHost(content = {
            DomposableBottomSheet("Partial full-height", {}, layout = layout) { DomposableText("Body") }
        }) { root ->
            val sheet = root.querySelector("dialog") as HTMLElement
            val handle = sheet.querySelector(".d-sheet-drag-handle") as HTMLElement
            handle.style.display = "flex"
            window.dispatchEvent(js("new Event('resize')") as Event)
            assertEquals(layout.partialSheetMaxHeightCss(), sheet.style.getPropertyValue("max-height"))
        }
        withHost(content = {
            DomposableBottomSheet(
                "Skip partial",
                {},
                layout = layout,
                skipPartiallyExpanded = true,
            ) { DomposableText("Body") }
        }) { root ->
            val sheet = root.querySelector("dialog") as HTMLElement
            val handle = sheet.querySelector(".d-sheet-drag-handle") as HTMLElement
            handle.style.display = "flex"
            window.dispatchEvent(js("new Event('resize')") as Event)
            assertEquals(layout.modalMaxHeightCss(), sheet.style.getPropertyValue("max-height"))
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
    fun fullSizeBodyWrapperDoesNotConsumeHandleSpace() {
        assertTallSheetGeometry()
        withFallbackDialogs { assertTallSheetGeometry() }
    }

    @Test
    fun fullHeightWrappersPreserveExplicitMinimumHeightInEitherOrder() {
        withHost(content = { HeightConstraintFixtures() }) { root ->
            assertDefaultFillHeight(root)
            assertExplicitMinimumHeight(root, "explicit-height-after-fill")
            assertExplicitMinimumHeight(root, "explicit-height-before-fill")
            assertFixedHeight(root)
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
            assertEquals("calc(100dvw - var(--d-modal-inline-inset, 32px))", dialog.style.width)
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

@Composable
private fun TallSheetFixture() {
    DomposableBottomSheet(
        "Sponsors",
        {},
        headerContent = { dismiss -> SponsorSheetHeader(dismiss) },
        layout =
            DomposableModalLayout(
                maxWidth = 560.dp,
                fillViewportWidth = true,
                padding = DomposableInsets(DomposableLength(0f)),
                heightFraction = 1f,
                maxViewportFraction = 1f,
                viewportInset = 32.dp,
                alignment = DomposableModalAlignment.BottomOnCompact,
            ),
    ) {
        DomposableHtmlModifiers(
            DomposableModifier.fillMaxSize().testId("full-size-sheet-body"),
            wrapSemantics = true,
        ) { TallSheetBody() }
    }
}

@Composable
private fun SponsorSheetHeader(dismiss: () -> Unit) {
    DomposableRow(
        modifier = DomposableModifier.fillMaxWidth().padding(20.dp, 16.dp),
        gap = 16.dp,
    ) {
        DomposableText("Sponsors")
        DomposableButton(
            "Close",
            { dismiss() },
            DomposableButtonStyle.Filled,
            modifier = DomposableModifier.height(48.dp),
        )
    }
}

@Composable
private fun TallSheetBody() {
    DomposableColumn {
        DomposableColumn(modifier = DomposableModifier.fillMaxWidth().weight(1f).minHeight(0.dp)) {
            DomposableHtmlModifiers(
                DomposableModifier
                    .fillMaxWidth()
                    .weight(1f)
                    .minHeight(0.dp)
                    .verticalScroll()
                    .padding(16.dp)
                    .testId("full-size-sheet-list"),
                wrapSemantics = true,
            ) {
                DomposableColumn {
                    DomposableTextField(
                        value = "",
                        onValueChange = {},
                        label = "Search sponsors",
                        autofocus = true,
                    )
                    repeat(48) { DomposableText("Sponsor $it") }
                }
            }
        }
    }
}

private fun assertTallSheetGeometry() {
    withHost(content = { TallSheetFixture() }) { root ->
        val dialog = root.querySelector("dialog") as HTMLElement
        val handle = dialog.querySelector(".d-sheet-drag-handle") as HTMLElement
        val header = dialog.querySelector(".d-dialog-header") as HTMLElement
        val body = dialog.querySelector("[data-testid='full-size-sheet-body']") as HTMLElement
        val listWrapper = dialog.querySelector("[data-testid='full-size-sheet-list']") as HTMLElement
        val list =
            listWrapper.parentElement?.parentElement as? HTMLElement
                ?: error("Missing inner scrolling list")
        val bodyFlexItem =
            body.parentElement?.parentElement as? HTMLElement
                ?: error("Missing full-size flex item")
        sizeTallSheetViewport(dialog, handle)

        assertEquals(dialog, bodyFlexItem.parentElement)
        assertEquals(24.0, handle.getBoundingClientRect().height)
        assertEquals(80.0, header.getBoundingClientRect().height)
        assertEquals(812, dialog.scrollHeight)
        assertEquals(
            676.0,
            bodyFlexItem.getBoundingClientRect().height,
            "The body must fit the space left after the handle, header, and gaps",
        )
        assertTrue(bodyFlexItem.getBoundingClientRect().bottom <= dialog.getBoundingClientRect().bottom)
        assertTrue(list.scrollHeight > list.clientHeight, "The inner list must own scrolling")
        assertTrue(handle.getBoundingClientRect().top >= dialog.getBoundingClientRect().top)
    }
}

private fun sizeTallSheetViewport(
    dialog: HTMLElement,
    handle: HTMLElement,
) {
    dialog.style.position = "fixed"
    dialog.style.left = "0"
    dialog.style.top = "32px"
    dialog.style.width = "390px"
    dialog.style.height = "812px"
    dialog.style.maxHeight = "none"
    dialog.style.margin = "0"
    dialog.style.border = "0"
    dialog.style.setProperty("overflow", "hidden")
    handle.style.display = "flex"
}

@Composable
private fun HeightConstraintFixtures() {
    HeightConstraintFixture("default-height", "Fill available height", DomposableModifier.fillMaxHeight())
    HeightConstraintFixture(
        "explicit-height-after-fill",
        "Explicit minimum",
        DomposableModifier.fillMaxHeight().minHeight(128.dp),
    )
    HeightConstraintFixture(
        "explicit-height-before-fill",
        "Explicit minimum before fill",
        DomposableModifier.minHeight(128.dp).fillMaxHeight(),
    )
    HeightConstraintFixture("fixed-height", "Fixed height", DomposableModifier.height(80.dp))
}

@Composable
private fun HeightConstraintFixture(
    id: String,
    label: String,
    modifier: DomposableModifier,
) {
    Div(attrs = {
        attr("id", "$id-parent")
        style {
            property("display", "flex")
            property("height", "200px")
        }
    }) {
        DomposableHtmlModifiers(modifier.testId("$id-wrapper"), wrapSemantics = true) {
            DomposableText(label)
        }
    }
}

private fun assertDefaultFillHeight(root: HTMLElement) {
    val semantic = root.querySelector("[data-testid='default-height-wrapper']") as HTMLElement
    val fill = semantic.parentElement as HTMLElement
    assertEquals("0px", window.getComputedStyle(fill).minHeight)
    assertEquals(200.0, fill.getBoundingClientRect().height)
}

private fun assertExplicitMinimumHeight(
    root: HTMLElement,
    testId: String,
) {
    val semantic = root.querySelector("[data-testid='$testId-wrapper']") as HTMLElement
    var minimum = semantic.parentElement as? HTMLElement
    while (minimum != null && minimum.getAttribute("data-domposable-modifier") != "MinHeight") {
        minimum = minimum.parentElement as? HTMLElement
    }
    assertEquals("128px", window.getComputedStyle(minimum as HTMLElement).minHeight)
}

private fun assertFixedHeight(root: HTMLElement) {
    val semantic = root.querySelector("[data-testid='fixed-height-wrapper']") as HTMLElement
    val fixed = semantic.parentElement as HTMLElement
    assertEquals("80px", window.getComputedStyle(fixed).height)
    assertEquals("auto", window.getComputedStyle(fixed).minHeight)
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
