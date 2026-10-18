package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
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
import io.github.samuolis.domposable.ui.DomposableIconButton
import io.github.samuolis.domposable.ui.DomposableIconButtonProps
import io.github.samuolis.domposable.ui.DomposableMenu
import io.github.samuolis.domposable.ui.DomposableMenuItem
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
import io.github.samuolis.domposable.ui.pointerPassthrough
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.weight
import io.github.samuolis.domposable.ui.width
import io.github.samuolis.domposable.ui.zIndex
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.MouseEventInit
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlHostIntegrationTest {
    @Test
    fun chipTextLimitsApplyToItsLabelWithoutClippingTheTouchTarget() {
        for (selected in listOf(false, true)) {
            withHost(content = {
                DomposableChip(
                    "A long chip label that must wrap across many lines before it is truncated",
                    {},
                    modifier = DomposableModifier.width(120.dp),
                    selected = selected,
                    appearance =
                        DomposableItemAppearance(
                            textAppearance =
                                DomposableTextAppearance(
                                    maxLines = 1,
                                    lineHeight = 20f,
                                    overflow = DomposableTextOverflow.Ellipsis,
                                ),
                            selectedTextAppearance =
                                DomposableTextAppearance(
                                    maxLines = 2,
                                    lineHeight = 20f,
                                    overflow = DomposableTextOverflow.Ellipsis,
                                ),
                        ),
                )
            }) { root ->
                val chip = root.querySelector(".d-chip") as HTMLElement
                val label = chip.querySelector(".d-chip-label") as HTMLElement
                assertEquals(if (selected) 40.0 else 20.0, label.getBoundingClientRect().height)
                assertTrue(label.scrollHeight > label.clientHeight, "Extra label lines must be truncated")
                assertTrue(chip.getBoundingClientRect().height >= 48.0, "Text limits must not shrink the touch target")
            }
        }
    }

    @Test
    fun selectableTextOverridesItsHostSelectionPolicy() {
        for (selectable in listOf(false, true)) {
            withHost(content = {
                DomposableBox(DomposableModifier.htmlAttributes { style { property("user-select", "none") } }) {
                    DomposableText(
                        "Selectable content",
                        modifier = DomposableModifier.testId("selection-text"),
                        appearance = DomposableTextAppearance(selectable = selectable),
                    )
                }
            }) { root ->
                val text = root.querySelector("[data-testid=selection-text]") as HTMLElement
                assertEquals(
                    if (selectable) "text" else "none",
                    window.getComputedStyle(text).getPropertyValue("user-select"),
                )
            }
        }
    }

    @Test
    fun clippedTextUsesItsRenderedLineHeightForInlineTextAndHeadings() {
        for (heading in listOf(null, 2)) {
            withHost(content = {
                DomposableText(
                    "One\nTwo\nThree",
                    modifier =
                        DomposableModifier.testId("clipped-text").htmlAttributes {
                            style {
                                property("line-height", "10px")
                                property("white-space", "pre-line")
                            }
                        },
                    headingLevel = heading,
                    appearance = DomposableTextAppearance(maxLines = 2, overflow = DomposableTextOverflow.Clip),
                )
            }) { root ->
                val text = root.querySelector("[data-testid=clipped-text]") as HTMLElement
                assertEquals("10px", window.getComputedStyle(text).lineHeight)
                assertEquals(20.0, text.getBoundingClientRect().height)
                assertTrue(text.scrollHeight > text.clientHeight, "Extra lines must be clipped")
            }
        }
    }

    @Test
    fun modalBackdropClicksDismissButContentClicksDoNot() {
        for (sheet in listOf(false, true)) {
            var dismissed = 0
            withHost(content = {
                if (sheet) {
                    DomposableBottomSheet("Options", { dismissed++ }) { DomposableText("Sheet body") }
                } else {
                    DomposableDialog("Options", { dismissed++ }) { DomposableText("Dialog body") }
                }
            }) { root ->
                val dialog = root.querySelector("dialog") as HTMLElement
                (dialog.querySelector("h2") as HTMLElement).click()
                assertEquals(0, dismissed, "Content clicks must leave the modal open")
                dialog.dispatchEvent(MouseEvent("click", MouseEventInit(bubbles = true, clientX = 1, clientY = 1)))
                assertEquals(1, dismissed, "The backdrop must dismiss the modal")
            }
        }
    }

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
    fun chipTouchTargetDoesNotPaintASecondSelectedSurface() {
        for (elevated in listOf(false, true)) {
            withHost(content = {
                DomposableChip("Selected", {}, selected = true, elevated = elevated)
            }) { root ->
                val chip = root.querySelector(".d-chip") as HTMLElement
                val style = window.getComputedStyle(chip)
                assertEquals("rgba(0, 0, 0, 0)", style.backgroundColor)
                assertEquals("0px", style.borderTopWidth)
                assertEquals("none", style.boxShadow)
            }
        }
    }

    @Test
    fun stackingOrderIncludesBackgroundBeforeTheZIndexModifier() =
        withHost(content = {
            DomposableBox(DomposableModifier.width(240.dp).height(120.dp)) {
                DomposableBox(DomposableModifier.fillMaxSize().zIndex(0f).testId("media-layer")) {
                    Div(attrs = {
                        style {
                            property("position", "absolute")
                            property("inset", "0")
                            property("z-index", "999")
                            property("background", "red")
                        }
                    }) {}
                }
                DomposableBox(
                    DomposableModifier
                        .fillMaxSize()
                        .background(DomposableColor(0xFF112233))
                        .then(NonInteractiveContent)
                        .zIndex(2f)
                        .zIndex(-1f)
                        .testId("overlay-layer"),
                ) {}
            }
        }) { root ->
            val media = root.querySelector("[data-testid=media-layer]") as HTMLElement
            val overlay = root.querySelector("[data-testid=overlay-layer]") as HTMLElement
            val bounds = overlay.getBoundingClientRect()
            val hit = document.elementFromPoint(bounds.left + bounds.width / 2, bounds.top + bounds.height / 2)
            assertTrue(hit?.contains(overlay) == true, "The overlay background must remain above media descendants")
            assertTrue(hit?.contains(media) == false, "The host itself must not be the hit target")
        }

    @Test
    fun cardLabelsStayClickableWithoutActivatingTheCard() {
        for (action in listOf("none", "container", "stretched")) {
            var checked = false
            var cardClicks = 0
            val onClick: (() -> Unit)? = if (action == "none") null else ({ cardClicks++ })
            withHost(content = {
                DomposableCard(
                    onClick = onClick,
                    actionLabel = if (action == "stretched") "Open" else null,
                ) {
                    DomposableCheckbox(false, { checked = it }, "Keep selection")
                }
            }) { root ->
                val input = root.querySelector("input[type=checkbox]") as HTMLElement
                val label = input.closest("label") as HTMLElement
                val bounds = label.getBoundingClientRect()
                val hit =
                    document.elementFromPoint(
                        bounds.left + bounds.width / 2,
                        bounds.top + bounds.height / 2,
                    ) as? HTMLElement
                assertEquals(label, hit?.closest("label"), "$action card must not cover its label")
                requireNotNull(hit).click()
                assertTrue(checked, "$action label must toggle its input")
                assertEquals(0, cardClicks, "$action label must not also activate the card")
            }
        }
    }

    @Test
    fun weightedBoxKeepsItsContentHeightInAnUnboundedColumn() =
        withHost(content = {
            Div(attrs = {
                style {
                    property("display", "flex")
                    property("flex-direction", "column")
                }
            }) {
                Div(attrs = { style { property("height", "40px") } }) { Text("Toolbar") }
                DomposableBox(DomposableModifier.weight(1f).fillMaxWidth().then(HostScope)) {
                    Div(attrs = {
                        attr("data-testid", "document-body")
                        style { property("height", "80px") }
                    }) { Text("Body") }
                }
            }
        }) { root ->
            val item = root.querySelector("[data-domposable-layout-item]") as HTMLElement
            assertTrue(
                abs(item.getBoundingClientRect().height - 80.0) < 1,
                "Unbounded weight must preserve the 80px content height:\n${layoutTrace(item)}",
            )
        }

    @Test
    fun weightedNestedBoxKeepsTheHostsAvailableHeight() =
        withHost(content = {
            Div(attrs = {
                style {
                    property("display", "flex")
                    property("flex-direction", "column")
                    property("height", "300px")
                }
            }) {
                DomposableBox(DomposableModifier.weight(1f).fillMaxWidth().then(HostScope)) {
                    DomposableBox(DomposableModifier.fillMaxSize().testId("bounded-body")) {
                        DomposableText("Body")
                    }
                }
            }
        }) { root ->
            val body = root.querySelector("[data-testid=bounded-body]") as HTMLElement
            assertTrue(
                abs(body.getBoundingClientRect().height - 300.0) < 1,
                "Nested Box must keep the 300px host height:\n${layoutTrace(body)}",
            )
            assertEquals(400, body.offsetWidth)
        }

    @Test
    fun passThroughAppliesOutsideEarlierSizingAndStackingWrappers() =
        withHost(content = {
            DomposableBox(DomposableModifier.width(240.dp).height(120.dp)) {
                DomposableButton("Underlay", {}, DomposableModifier.fillMaxSize().testId("underlay"))
                DomposableBox(DomposableModifier.fillMaxSize().zIndex(2f).pointerPassthrough()) {
                    DomposableButton("Overlay", {}, DomposableModifier.width(80.dp).height(40.dp).testId("overlay"))
                }
            }
        }) { root ->
            val underlay = root.querySelector("[data-testid=underlay]") as HTMLElement
            val overlay = root.querySelector("[data-testid=overlay]") as HTMLElement
            val bounds = underlay.getBoundingClientRect()
            val hit = document.elementFromPoint(bounds.right - 4, bounds.top + bounds.height / 2)
            assertEquals(
                underlay,
                hit?.closest("button"),
                "Hit ${hit?.outerHTML?.take(400)} at ${bounds.right - 4}, ${bounds.top + bounds.height / 2}; " +
                    "viewport ${window.innerWidth}x${window.innerHeight}; ${layoutTrace(underlay)}",
            )
            val action = overlay.getBoundingClientRect()
            assertEquals(
                overlay,
                document
                    .elementFromPoint(
                        action.left + action.width / 2,
                        action.top + action.height / 2,
                    )?.closest("button"),
            )
        }

    @Test
    fun embeddedCardRetainsIntrinsicWidthAndAnActionableSurface() =
        withHost(content = {
            Div(attrs = { style { property("display", "flex") } }) {
                Div(attrs = { style { property("flex", "1") } }) { Text("Member") }
                DomposableCard(
                    onClick = {},
                    actionLabel = "Open details",
                    modifier = DomposableModifier.testId("card"),
                ) {
                    DomposableText("1528")
                }
            }
        }) { root ->
            val card = root.querySelector("[data-testid=card]") as HTMLElement
            val action = root.querySelector("button[aria-label='Open details']") as HTMLElement
            assertTrue(card.offsetWidth in 1..120, "An unfilled card must not consume the whole row")
            assertTrue(
                action.offsetWidth > 0 && action.offsetHeight > 0,
                "The stretched action must have visible bounds",
            )
            val bounds = card.getBoundingClientRect()
            assertEquals(
                action,
                document.elementFromPoint(
                    bounds.left + bounds.width / 2,
                    bounds.top + bounds.height / 2,
                ),
            )
        }

    @Test
    fun embeddedCustomFieldActionRemainsAboveTheInput() =
        withHost(content = {
            DomposableCard {
                DomposableTextField(
                    DomposableTextFieldProps(
                        value = "secret",
                        onValueChange = {},
                        label = "Password",
                        trailingIcon = {
                            Button(attrs = { attr("data-testid", "field-action") }) { Text("Show") }
                        },
                    ),
                    DomposableModifier.width(280.dp),
                )
            }
        }) { root ->
            val action = root.querySelector("[data-testid=field-action]") as HTMLElement
            val bounds = action.getBoundingClientRect()
            assertEquals(
                action,
                document
                    .elementFromPoint(
                        bounds.left + bounds.width / 2,
                        bounds.top + bounds.height / 2,
                    )?.closest("button"),
            )
        }

    @Test
    fun embeddedIconButtonKeepsTheMaterialVisualDimensions() =
        withHost(content = {
            DomposableIconButton(
                DomposableIconButtonProps(onClick = {}, label = "Host action"),
                DomposableModifier.testId("host-icon"),
            ) { Text("!") }
        }) { root ->
            val button = root.querySelector("[data-testid=host-icon]") as HTMLElement
            assertEquals(40, button.offsetWidth)
            assertEquals(40, button.offsetHeight)
            assertEquals("relative", window.getComputedStyle(button).position)
        }

    @Test
    fun embeddedSelectInheritsTheHostTypography() =
        withHost(content = {
            Div(attrs = {
                style {
                    property("font-family", "monospace")
                    property("font-size", "20px")
                }
            }) {
                DomposableSelect("one", {}, "Choice", listOf(DomposableSelectOption("one", "Option")))
            }
        }) { root ->
            val select = root.querySelector("select") as HTMLElement
            val style = window.getComputedStyle(select)
            assertEquals("monospace", style.fontFamily)
            assertEquals("20px", style.fontSize)
        }

    @Test
    fun embeddedButtonInheritsTheHostFontAndMaterialTypeScale() =
        withHost(content = {
            Div(attrs = { style { property("font-family", "monospace") } }) {
                DomposableButton("Action", {}, DomposableModifier.testId("host-button"))
            }
        }) { root ->
            val button = root.querySelector("[data-testid=host-button]") as HTMLElement
            val style = window.getComputedStyle(button)
            assertEquals("monospace", style.fontFamily)
            assertEquals("14px", style.fontSize)
            assertEquals("20px", style.lineHeight)
        }
}

private data object HostScope : DomposableModifierElement

private data object NonInteractiveContent : DomposableModifierElement

private fun layoutTrace(element: HTMLElement): String =
    generateSequence(element) { it.parentElement as? HTMLElement }.take(10).joinToString("\n") {
        val style = window.getComputedStyle(it)
        "${it.tagName}.${it.className}: ${it.getBoundingClientRect().width}x${it.getBoundingClientRect().height}px; " +
            "height=${style.height}; rows=${style.getPropertyValue(
                "grid-template-rows",
            )}; align=${style.alignItems}; ${it.getAttribute("style")}"
    }

private fun withHost(
    content: @Composable () -> Unit,
    verify: (HTMLElement) -> Unit,
) {
    val root = document.createElement("div") as HTMLElement
    document.body!!.appendChild(root)
    val composition =
        renderComposable(root) {
            val tokens = DomposableTokens()
            Div(attrs = {
                domposableTokenVariables(tokens)
                style { property("width", "400px") }
            }) {
                CompositionLocalProvider(
                    LocalDomposableHtmlModifierRenderer provides { element ->
                        when (element) {
                            HostScope -> ({ classes("host-scope") })
                            NonInteractiveContent -> ({ style { property("pointer-events", "none") } })
                            else -> null
                        }
                    },
                ) {
                    ProvideDomposableMaterialComponents(tokens, content)
                }
            }
        }
    try {
        verify(root)
    } finally {
        composition.dispose()
        root.parentNode?.removeChild(root)
    }
}
