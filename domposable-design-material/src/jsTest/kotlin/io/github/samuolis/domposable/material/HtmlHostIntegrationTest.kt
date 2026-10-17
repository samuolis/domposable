package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableCard
import io.github.samuolis.domposable.ui.DomposableIconButton
import io.github.samuolis.domposable.ui.DomposableIconButtonProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableModifierElement
import io.github.samuolis.domposable.ui.DomposableSelect
import io.github.samuolis.domposable.ui.DomposableSelectOption
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableTextFieldProps
import io.github.samuolis.domposable.ui.LocalDomposableHtmlModifierRenderer
import io.github.samuolis.domposable.ui.fillMaxSize
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
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
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlHostIntegrationTest {
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
                        if (element == HostScope) ({ classes("host-scope") }) else null
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
