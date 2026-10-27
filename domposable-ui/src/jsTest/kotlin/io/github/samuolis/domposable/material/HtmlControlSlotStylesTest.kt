package io.github.samuolis.domposable.material

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import kotlin.js.asDynamic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlControlSlotStylesTest {
    @Test
    fun selectionDialogAndProviderHooksKeepLayoutAndFocusBehavior() {
        val style = document.createElement("style")
        style.textContent = HTML_CONTROL_SLOT_CSS
        val root = document.createElement("div") as HTMLElement
        root.style.setProperty("--d-primary", "rgb(18, 52, 86)")
        root.innerHTML =
            """
            <label class="d-cell-plain d-selection-row"><span>Choose</span><input type="checkbox"></label>
            <div class="d-select-option d-cell-plain"><input type="radio"></div>
            <div class="d-action-dialog-actions"><button class="d-button d-button-single-line"><span class="d-button-copy">Continue</span></button></div>
            <button class="d-provider-button"><img alt="" src="data:image/svg+xml,%3Csvg xmlns=%27http://www.w3.org/2000/svg%27/%3E"></button>
            """.trimIndent()
        document.head!!.appendChild(style)
        document.body!!.appendChild(root)

        try {
            val row = root.querySelector(".d-selection-row") as HTMLElement
            val input = row.querySelector("input") as HTMLElement
            val option = root.querySelector(".d-select-option") as HTMLElement
            val optionInput = option.querySelector("input") as HTMLElement
            val button = root.querySelector(".d-action-dialog-actions .d-button") as HTMLElement
            val buttonCopy = button.querySelector(".d-button-copy") as HTMLElement
            val providerIcon = root.querySelector(".d-provider-button img") as HTMLElement

            assertEquals("block", window.getComputedStyle(row).display)
            assertEquals("14px", window.getComputedStyle(input).marginLeft)
            optionInput.focus()
            assertEquals("2px", window.getComputedStyle(option).outlineWidth)
            assertEquals("100%", window.getComputedStyle(button).maxWidth)
            assertEquals("normal", window.getComputedStyle(buttonCopy).whiteSpace)
            assertEquals("contain", window.getComputedStyle(providerIcon).objectFit)

            val cssRules = style.asDynamic().sheet.cssRules
            val hasForcedColorsFallback =
                (0 until (cssRules.length as Int)).any { index ->
                    val rule = cssRules.item(index)
                    rule.conditionText?.toString()?.contains("forced-colors") == true
                }
            assertTrue(hasForcedColorsFallback)
        } finally {
            root.parentNode?.removeChild(root)
            style.parentNode?.removeChild(style)
        }
    }
}
