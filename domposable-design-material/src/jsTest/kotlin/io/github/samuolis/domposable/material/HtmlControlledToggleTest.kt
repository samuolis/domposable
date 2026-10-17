package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.ui.DomposableCheckbox
import io.github.samuolis.domposable.ui.DomposableSwitch
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLInputElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class HtmlControlledToggleTest {
    @Test
    fun declinedChangesPreserveTheDeclaredCheckboxAndSwitchState() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val requested = mutableListOf<Boolean>()
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableCheckbox(false, { requested += it }, "Checkbox", showLabel = false)
                    DomposableSwitch(false, { requested += it }, "Switch", showLabel = false)
                }
            }
        try {
            val inputs = root.querySelectorAll("input")
            for (index in 0 until inputs.length) {
                val input = inputs.item(index) as HTMLInputElement
                (input.parentElement as org.w3c.dom.HTMLElement).click()
                assertFalse(input.checked)
            }
            assertEquals(listOf(true, true), requested)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
