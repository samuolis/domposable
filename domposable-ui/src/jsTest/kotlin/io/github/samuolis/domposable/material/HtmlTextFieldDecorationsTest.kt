package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableTextFieldDecorations
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlTextFieldDecorationsTest {
    @Test
    fun decorationLeavesTheOtherActionSlotInteractive() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableTextFieldDecorations(leading = true) {
                        DomposableTextField(
                            value = "Alice",
                            onValueChange = {},
                            label = "Search",
                            leadingIcon = { Text("search") },
                            trailingIcon = { Text("clear") },
                        )
                    }
                }
            }
        try {
            val input = root.querySelector("input") as HTMLElement
            val leading = root.querySelector(".d-field-leading") as HTMLElement
            val trailing = root.querySelector(".d-field-trailing") as HTMLElement
            assertEquals("48px", window.getComputedStyle(input).paddingLeft)
            assertEquals("40px", window.getComputedStyle(leading).width)
            assertEquals("none", window.getComputedStyle(leading).getPropertyValue("pointer-events"))
            assertEquals("48px", window.getComputedStyle(trailing).width)
            assertEquals("auto", window.getComputedStyle(trailing).getPropertyValue("pointer-events"))
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun trailingDecorationReservesSpaceAndLetsTheInputReceivePointerEvents() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableTextFieldDecorations(trailing = true) {
                        DomposableTextField(
                            value = "Tennis",
                            onValueChange = {},
                            label = "Sport",
                            readOnly = true,
                            onClick = {},
                            trailingIcon = { Text("v") },
                        )
                    }
                }
            }
        try {
            val input = root.querySelector("input") as HTMLElement
            val trailing = root.querySelector(".d-field-trailing") as HTMLElement
            assertEquals("44px", window.getComputedStyle(input).paddingRight)
            assertEquals("40px", window.getComputedStyle(trailing).width)
            assertEquals("none", window.getComputedStyle(trailing).getPropertyValue("pointer-events"))
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
