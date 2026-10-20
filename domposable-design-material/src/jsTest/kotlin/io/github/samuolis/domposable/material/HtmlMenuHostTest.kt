package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableChipVariant
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableOffset
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextOverflow
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBottomSheet
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableCard
import io.github.samuolis.domposable.ui.DomposableCheckbox
import io.github.samuolis.domposable.ui.DomposableChip
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableDropdownMenuItem
import io.github.samuolis.domposable.ui.DomposableHtmlMenuPlacement
import io.github.samuolis.domposable.ui.DomposableIconButton
import io.github.samuolis.domposable.ui.DomposableIconButtonProps
import io.github.samuolis.domposable.ui.DomposableMenu
import io.github.samuolis.domposable.ui.DomposableMenuItem
import io.github.samuolis.domposable.ui.DomposableMenuProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableModifierElement
import io.github.samuolis.domposable.ui.DomposableSelect
import io.github.samuolis.domposable.ui.DomposableSelectOption
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableTextFieldProps
import io.github.samuolis.domposable.ui.LocalDomposableHtmlModifierRenderer
import io.github.samuolis.domposable.ui.background
import io.github.samuolis.domposable.ui.fillMaxSize
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.htmlAttributes
import io.github.samuolis.domposable.ui.htmlMenuAnchor
import io.github.samuolis.domposable.ui.htmlMenuPlacement
import io.github.samuolis.domposable.ui.minWidth
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.pointerPassthrough
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.weight
import io.github.samuolis.domposable.ui.width
import io.github.samuolis.domposable.ui.zIndex
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.MouseEventInit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlMenuHostTest {
    @Test
    fun menuSurfaceIsOpaqueAndAboveHostStackingContexts() =
        withHost(content = {
            Div(attrs = {
                style {
                    property("position", "fixed")
                    property("inset", "0 0 auto")
                    property("height", "160px")
                    property("z-index", "9999")
                    property("background", "red")
                }
            }) { Text("Application header") }
            DomposableBox(DomposableModifier.zIndex(0f)) {
                DomposableMenu(true, {}, listOf(DomposableMenuItem("Action", {})))
            }
        }) { root ->
            val menu = root.querySelector("[role=menu]") as HTMLElement
            assertEquals(menu.querySelector("[role=menuitem]"), document.activeElement, "Menu focus must be immediate")
            val background = window.getComputedStyle(menu).backgroundColor
            assertTrue(background != "transparent" && background != "rgba(0, 0, 0, 0)", "Menus need a surface")
            val bounds = menu.getBoundingClientRect()
            val hit = document.elementFromPoint(bounds.left + bounds.width / 2, bounds.top + bounds.height / 2)
            assertTrue(menu.contains(hit), "Host headers must not paint above a menu")
        }

    @Test
    fun wideMenuUsesTheSameViewportMarginsAsItsAnchorPlacement() =
        withHost(content = {
            DomposableMenu(
                expanded = true,
                onDismiss = {},
                anchor = { Button { Text("Open actions") } },
                items = listOf(DomposableMenuItem("A long action ".repeat(100), {})),
                modifier =
                    DomposableModifier.htmlAttributes {
                        style {
                            property("width", "1000px")
                            property("max-width", "calc(100vw - 16px)")
                        }
                    },
            )
        }) { root ->
            val bounds = (root.querySelector(".d-menu-position") as HTMLElement).getBoundingClientRect()
            assertEquals(window.innerWidth - 16.0, bounds.width, 1.0)
            assertEquals(8.0, bounds.left, 1.0)
            assertEquals(window.innerWidth - 8.0, bounds.right, 1.0)
        }

    @Test
    fun anchoredMenuUsesTriggerGeometryAndRendersRichAccessibleItemText() =
        withHost(content = {
            DomposableMenu(
                expanded = true,
                onDismiss = {},
                anchor = { Button { Text("Open actions") } },
                contentSpacing = 4.dp,
                anchorOffset = DomposableOffset(y = 4f),
            ) {
                DomposableDropdownMenuItem(
                    textContent = { Span { Text("Rich action name") } },
                    onClick = {},
                )
            }
        }) { root ->
            val anchor = root.querySelector(".d-menu-anchor") as HTMLElement
            val menu = root.querySelector(".d-menu-popup") as HTMLElement
            val item = menu.querySelector("[role=menuitem]") as HTMLElement
            assertEquals("Rich action name", item.textContent?.trim())
            val position = root.querySelector(".d-menu-position") as HTMLElement
            assertEquals("fixed", position.style.position)
            val positionStyle = window.getComputedStyle(position)
            val menuStyle = window.getComputedStyle(menu)
            assertEquals("visible", positionStyle.getPropertyValue("overflow"), "Positioning must not clip the popup shadow")
            assertEquals("auto", menuStyle.getPropertyValue("overflow"), "The popup owns scrolling within the viewport")
            assertTrue(menuStyle.boxShadow != "none", "The popup retains its elevation")
            assertTrue(menu.getBoundingClientRect().bottom <= window.innerHeight)
            assertEquals("4px", menu.style.getPropertyValue("gap"))
            assertTrue(
                position.style.left.isNotEmpty() && position.style.top.isNotEmpty(),
                "Anchored menus need a viewport position",
            )
            assertTrue(menu.getBoundingClientRect().top >= anchor.getBoundingClientRect().bottom)
        }

    @Test
    fun externalAnchorResolverKeepsSizingModifiersOnTheMenuSurface() =
        withHost(content = {
            Button(attrs = { attr("data-testid", "external-menu-trigger") }) { Text("Open") }
            DomposableMenu(
                DomposableMenuProps(
                    expanded = true,
                    onDismiss = {},
                    items = listOf(DomposableMenuItem("Action", {})),
                    contentSpacing = 4.dp,
                    anchorOffset = DomposableOffset(x = 10f, y = 4f),
                ),
                DomposableModifier
                    .htmlMenuAnchor { menuElement ->
                        menuElement.parentElement?.querySelector("[data-testid=external-menu-trigger]") as? HTMLElement
                    }.width(192.dp)
                    .minWidth(192.dp)
                    .testId("external-menu-surface"),
                {},
            )
        }) { root ->
            val backdrop = root.querySelector(".d-menu-backdrop") as HTMLElement
            val position = root.querySelector(".d-menu-position") as HTMLElement
            val menu = root.querySelector("[data-testid=external-menu-surface]") as HTMLElement
            val trigger = root.querySelector("[data-testid=external-menu-trigger]") as HTMLElement
            assertEquals("100vw", backdrop.style.width)
            assertTrue(position.style.left.isNotEmpty() && position.style.top.isNotEmpty())
            assertTrue(menu.getBoundingClientRect().width >= 192.0)
            assertTrue(position.getBoundingClientRect().top >= trigger.getBoundingClientRect().bottom)
            assertEquals("4px", menu.style.getPropertyValue("gap"))
        }

    @Test
    fun externalAnchorResolverIsCachedBeforeMenuAutofocus() {
        var resolverCalls = 0
        var resolvedAnchor: HTMLElement? = null
        withHost(content = {
            Button(attrs = {
                attr("data-testid", "focused-external-trigger")
                ref { element ->
                    element.focus()
                    onDispose {}
                }
            }) { Text("Open") }
            DomposableMenu(
                DomposableMenuProps(
                    expanded = true,
                    onDismiss = {},
                    items = listOf(DomposableMenuItem("Action", {})),
                ),
                DomposableModifier.htmlMenuAnchor { _ ->
                    resolverCalls += 1
                    (document.activeElement as? HTMLElement).also { resolvedAnchor = it }
                },
                {},
            )
        }) { root ->
            val trigger = root.querySelector("[data-testid=focused-external-trigger]")
            val position = root.querySelector(".d-menu-position") as HTMLElement
            val initialLeft = position.style.left
            val initialTop = position.style.top
            assertEquals(trigger, resolvedAnchor)
            assertEquals(1, resolverCalls)
            assertEquals(root.querySelector("[role=menuitem]"), document.activeElement)
            window.dispatchEvent(js("new Event('resize')"))
            window.dispatchEvent(js("new Event('scroll')"))
            assertEquals(1, resolverCalls)
            assertEquals(initialLeft, position.style.left)
            assertEquals(initialTop, position.style.top)
        }
    }

    @Test
    fun htmlMenuPlacementPrefersAboveWhenBothSidesFit() =
        withHost(content = {
            Button(attrs = {
                attr("data-testid", "placement-trigger")
                style {
                    property("position", "fixed")
                    property("top", "300px")
                    property("left", "100px")
                }
            }) { Text("Open") }
            DomposableMenu(
                expanded = true,
                onDismiss = {},
                items = listOf(DomposableMenuItem("Action", {})),
                modifier =
                    DomposableModifier
                        .htmlMenuAnchor { menuRoot ->
                            menuRoot.parentElement?.querySelector("[data-testid=placement-trigger]") as? HTMLElement
                        }.htmlMenuPlacement(DomposableHtmlMenuPlacement.Above),
            )
        }) { root ->
            val trigger = root.querySelector("[data-testid=placement-trigger]") as HTMLElement
            val position = root.querySelector(".d-menu-position") as HTMLElement
            val bounds = position.getBoundingClientRect()
            assertTrue(bounds.bottom <= trigger.getBoundingClientRect().top)
        }

    @Test
    fun htmlMenuPlacementFallsBackBelowWhenPreferredAboveDoesNotFit() =
        withHost(content = {
            Button(attrs = {
                attr("data-testid", "placement-trigger")
                style {
                    property("position", "fixed")
                    property("top", "4px")
                    property("left", "100px")
                }
            }) { Text("Open") }
            DomposableMenu(
                expanded = true,
                onDismiss = {},
                items = listOf(DomposableMenuItem("Action", {})),
                modifier =
                    DomposableModifier
                        .htmlMenuAnchor { menuRoot ->
                            menuRoot.parentElement?.querySelector("[data-testid=placement-trigger]") as? HTMLElement
                        }.htmlMenuPlacement(DomposableHtmlMenuPlacement.Above),
            )
        }) { root ->
            val trigger = root.querySelector("[data-testid=placement-trigger]") as HTMLElement
            val position = root.querySelector(".d-menu-position") as HTMLElement
            assertTrue(position.getBoundingClientRect().top >= trigger.getBoundingClientRect().bottom)
        }

    @Test
    fun autoPlacementStillUsesTheAvailableBelowSide() =
        withHost(content = {
            Button(attrs = {
                attr("data-testid", "placement-trigger")
                style {
                    property("position", "fixed")
                    property("top", "200px")
                    property("left", "100px")
                }
            }) { Text("Open") }
            DomposableMenu(
                expanded = true,
                onDismiss = {},
                items = listOf(DomposableMenuItem("Action", {})),
                modifier =
                    DomposableModifier
                        .htmlMenuAnchor { menuRoot ->
                            menuRoot.parentElement?.querySelector("[data-testid=placement-trigger]") as? HTMLElement
                        }.htmlMenuPlacement(DomposableHtmlMenuPlacement.Auto),
            )
        }) { root ->
            val trigger = root.querySelector("[data-testid=placement-trigger]") as HTMLElement
            val position = root.querySelector(".d-menu-position") as HTMLElement
            assertTrue(position.getBoundingClientRect().top >= trigger.getBoundingClientRect().bottom)
        }

    @Test
    fun placementResolverRunsOnceAgainstMenuAncestry() {
        var resolverCalls = 0
        var resolvedTrigger: HTMLElement? = null
        withHost(content = {
            Button(attrs = { attr("data-testid", "placement-trigger") }) { Text("Open") }
            DomposableMenu(
                expanded = true,
                onDismiss = {},
                items = listOf(DomposableMenuItem("Action", {})),
                modifier =
                    DomposableModifier
                        .htmlMenuAnchor { menuRoot ->
                            menuRoot.parentElement?.querySelector("[data-testid=placement-trigger]") as? HTMLElement
                        }.htmlMenuPlacement { menuRoot ->
                            resolverCalls++
                            resolvedTrigger =
                                menuRoot.parentElement?.querySelector("[data-testid=placement-trigger]") as? HTMLElement
                            DomposableHtmlMenuPlacement.Above
                        },
            )
        }) { root ->
            assertEquals(root.querySelector("[data-testid=placement-trigger]"), resolvedTrigger)
            assertEquals(1, resolverCalls)
            window.dispatchEvent(js("new Event('resize')"))
            window.dispatchEvent(js("new Event('scroll')"))
            assertEquals(1, resolverCalls)
        }
    }

    @Test
    fun menuPaddingModifierWrapsTheVisibleSurface() =
        withHost(content = {
            DomposableMenu(
                expanded = true,
                onDismiss = {},
                modifier = DomposableModifier.padding(12.dp).testId("padded-menu"),
                items = listOf(DomposableMenuItem("Action", {})),
            )
        }) { root ->
            val menu = root.querySelector("[data-testid=padded-menu]") as HTMLElement
            val modifierWrapper = menu.parentElement as HTMLElement
            assertEquals("12px", window.getComputedStyle(modifierWrapper).paddingLeft)
            assertEquals(12.0, menu.getBoundingClientRect().left - modifierWrapper.getBoundingClientRect().left)
            assertEquals("100vw", (root.querySelector(".d-menu-backdrop") as HTMLElement).style.width)
        }

    @Test
    fun contentMenuItemsLeaveDismissalUnderCallerControl() {
        var dismissed = 0
        var invoked = 0
        withHost(content = {
            DomposableMenu(expanded = true, onDismiss = { dismissed++ }) {
                DomposableDropdownMenuItem(label = "Action", onClick = { invoked++ })
            }
        }) { root ->
            val item = root.querySelector("[role=menuitem]") as HTMLElement
            item.dispatchEvent(js("new Event('pointerdown', { bubbles: true, cancelable: true })") as Event)
            item.click()
            assertEquals(1, invoked)
            assertEquals(0, dismissed, "Content slots must retain Material's caller-owned dismissal")
        }
    }

    @Test
    fun focusChangeAndBackdropClickRequestDismissalOnlyOnce() {
        var dismissed = 0
        withHost(content = {
            DomposableMenu(true, { dismissed++ }, listOf(DomposableMenuItem("Action", {})))
        }) { root ->
            val backdrop = root.querySelector(".d-menu-backdrop") as HTMLElement
            backdrop.dispatchEvent(js("new FocusEvent('focusout', { bubbles: true, relatedTarget: document.body })"))
            backdrop.click()
            assertEquals(1, dismissed, "A single outside interaction must not consume two dismissals")
        }
    }

    @Test
    fun outsideClickDismissesWithoutCancelingControlActivation() {
        var dismissed = 0
        var outsideClicks = 0
        var clicks = 0
        var activationEvent: Event? = null
        withHost(content = {
            DomposableMenu(true, { dismissed++ }, listOf(DomposableMenuItem("Action", {})))
            Button(attrs = { onClick { clicks++ } }) { Text("Open dialog") }
        }) { root ->
            val backdrop = root.querySelector(".d-menu-backdrop") as HTMLElement
            val popupPosition = root.querySelector(".d-menu-position") as HTMLElement
            assertEquals("none", window.getComputedStyle(backdrop).getPropertyValue("pointer-events"))
            assertEquals("auto", window.getComputedStyle(popupPosition).getPropertyValue("pointer-events"))

            val outside = root.querySelector("button:not([role=menuitem])") as HTMLElement
            outside.addEventListener("click", { event ->
                outsideClicks++
                activationEvent = event
            })
            outside.click()

            assertEquals(1, dismissed)
            assertEquals(1, outsideClicks)
            assertEquals(1, clicks)
            assertEquals(false, activationEvent?.defaultPrevented)
        }
    }

    @Test
    fun focusExitKeepsTheNextControlFocusedAfterMenuDisposal() {
        val trigger = document.createElement("button") as HTMLElement
        val next = document.createElement("button") as HTMLElement
        document.body!!.appendChild(trigger)
        document.body!!.appendChild(next)
        try {
            trigger.focus()
            withHost(content = {
                DomposableMenu(true, {}, listOf(DomposableMenuItem("Action", {})))
            }) { _ ->
                next.focus()
                assertEquals(next, document.activeElement)
            }
            assertEquals(next, document.activeElement, "Dismissal must not steal focus from the next control")
        } finally {
            trigger.parentNode?.removeChild(trigger)
            next.parentNode?.removeChild(next)
        }
    }

    @Test
    fun menuSupportsHomeEndAndDismissesWhenFocusLeaves() {
        var dismissed = 0
        withHost(content = {
            DomposableMenu(
                expanded = true,
                onDismiss = { dismissed++ },
                items = listOf(DomposableMenuItem("First", {}), DomposableMenuItem("Last", {})),
            )
        }) { root ->
            val menu = root.querySelector("[role=menu]") as HTMLElement
            val items = menu.querySelectorAll("[role=menuitem]")
            menu.dispatchEvent(js("new KeyboardEvent('keydown', { key: 'End', bubbles: true })"))
            assertEquals(items.item(1), document.activeElement)
            menu.dispatchEvent(js("new KeyboardEvent('keydown', { key: 'Home', bubbles: true })"))
            assertEquals(items.item(0), document.activeElement)
            menu.dispatchEvent(js("new FocusEvent('focusout', { bubbles: true, relatedTarget: document.body })"))
            assertEquals(1, dismissed)
        }
    }

    @Test
    fun escapeDismissesNestedMenuBeforeBubblingToItsParent() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            var expanded by mutableStateOf(false)
            var menuDismissals = 0
            var parentEscapes = 0
            val root = document.createElement("div") as HTMLElement
            document.body!!.appendChild(root)
            val composition =
                renderComposable(root) {
                    DomposableMaterialTheme {
                        Div(attrs = {
                            onKeyDown { if (it.key == "Escape") parentEscapes++ }
                        }) {
                            DomposableMenu(
                                expanded = expanded,
                                onDismiss = {
                                    menuDismissals++
                                    expanded = false
                                },
                                anchor = {
                                    Button(attrs = {
                                        attr("data-testid", "nested-menu-trigger")
                                        onClick { expanded = true }
                                    }) { Text("Open menu") }
                                },
                                items = listOf(DomposableMenuItem("Action", {})),
                            )
                        }
                    }
                }
            try {
                val trigger = root.querySelector("[data-testid=nested-menu-trigger]") as HTMLElement
                trigger.focus()
                trigger.click()
                settleMenuComposition()
                val menuItem = root.querySelector("[role=menuitem]") as HTMLElement
                assertEquals(menuItem, document.activeElement)

                val escape =
                    js("new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, cancelable: true })") as Event
                menuItem.dispatchEvent(escape)

                assertTrue(escape.defaultPrevented)
                assertEquals(1, menuDismissals)
                assertEquals(0, parentEscapes, "The first Escape should be consumed by the open menu")
                settleMenuComposition()
                assertEquals(null, root.querySelector(".d-menu-backdrop"))
                assertEquals(trigger, document.activeElement, "Closing the menu should restore focus to its trigger")

                trigger.dispatchEvent(js("new KeyboardEvent('keydown', { key: 'Escape', bubbles: true })") as Event)
                assertEquals(1, parentEscapes, "A later Escape should reach the parent after the menu closes")
            } finally {
                composition.dispose()
                root.parentNode?.removeChild(root)
            }
        }

    private suspend fun settleMenuComposition() {
        repeat(2) {
            suspendCoroutine<Unit> { continuation -> window.requestAnimationFrame { continuation.resume(Unit) } }
        }
    }
}
