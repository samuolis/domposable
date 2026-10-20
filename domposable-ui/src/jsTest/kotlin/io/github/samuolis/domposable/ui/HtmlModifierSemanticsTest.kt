package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.dp
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HtmlModifierSemanticsTest {
    @Test
    fun explicitSemanticWrappersKeepModifierOrderAndRenderRawHtmlAttributes() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val modifier =
            DomposableModifier
                .testId("outer")
                .padding(4.dp)
                .then(TestAppModifier)
                .testId("inner")
                .contentDescription("Modifier description")
                .htmlAttributes { attr("data-raw-attribute", "present") }
        val composition =
            renderComposable(root) {
                DomposableHtmlModifiers(
                    modifier = modifier,
                    wrapSemantics = true,
                    operationRenderer = { element ->
                        if (element == TestAppModifier) {
                            ({ attr("data-custom-modifier", "handled") })
                        } else {
                            null
                        }
                    },
                ) {
                    Div(attrs = { attr("data-testid", "content") }) { Text("Raw content") }
                }
            }

        try {
            val outer = root.querySelector("[data-testid=outer]") as HTMLElement
            val padding = outer.firstElementChild as Element
            val custom = padding.firstElementChild as Element
            val inner = custom.firstElementChild as Element
            val description = inner.firstElementChild as Element
            val htmlAttributes = description.firstElementChild as HTMLElement

            assertEquals("TestId", outer.getAttribute("data-domposable-modifier"))
            assertEquals("Padding", padding.getAttribute("data-domposable-modifier"))
            assertEquals("4px", (padding as HTMLElement).style.getPropertyValue("padding-block-start"))
            assertEquals("present", htmlAttributes.getAttribute("data-raw-attribute"))
            assertEquals("handled", custom.getAttribute("data-custom-modifier"))
            assertEquals("inner", inner.getAttribute("data-testid"))
            assertEquals("Modifier description", description.getAttribute("aria-label"))
            assertEquals("Raw content", htmlAttributes.textContent)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun existingOverloadLeavesSemanticAttributesForComponentRoots() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val modifier =
            DomposableModifier
                .testId("root-tag")
                .contentDescription("Root description")
                .htmlAttributes { attr("data-root-custom", "present") }
        val composition =
            renderComposable(root) {
                DomposableHtmlModifiers(modifier) {
                    Div(attrs = {
                        domposableAttributes(modifier)
                        attr("data-content-root", "true")
                    }) { Text("Content") }
                }
            }

        try {
            val content = root.querySelector("[data-content-root=true]") as HTMLElement
            assertEquals("root-tag", content.getAttribute("data-testid"))
            assertEquals("Root description", content.getAttribute("aria-label"))
            assertEquals("present", content.getAttribute("data-root-custom"))
            assertEquals(content, root.firstElementChild)
            assertNull(root.querySelector("[data-domposable-modifier='TestId']"))
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}

private data object TestAppModifier : DomposableModifierElement
