package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableTextFieldProps
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlTextFieldImeTest {
    @Test
    fun enterOnlySubmitsAfterImeCompositionFinishes() {
        val root = document.createElement("div") as HTMLElement
        document.body!!.appendChild(root)
        var submissions = 0
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableTextField(
                        DomposableTextFieldProps(
                            value = "",
                            label = "Name",
                            onValueChange = {},
                            singleLine = true,
                            onImeAction = { submissions++ },
                        ),
                    )
                }
            }
        try {
            val input = root.querySelector("input")!!
            val composing =
                js(
                    "new KeyboardEvent('keydown', {key: 'Enter', isComposing: true, bubbles: true, cancelable: true})",
                ) as Event
            val finishing =
                js(
                    "new KeyboardEvent('keydown', {key: 'Enter', keyCode: 229, bubbles: true, cancelable: true})",
                ) as Event
            listOf(composing, finishing).forEach { event ->
                input.dispatchEvent(event)
                assertEquals(0, submissions)
                assertFalse(event.defaultPrevented)
            }
            val enter = js("new KeyboardEvent('keydown', {key: 'Enter', bubbles: true, cancelable: true})") as Event
            input.dispatchEvent(enter)
            assertEquals(1, submissions)
            assertTrue(enter.defaultPrevented)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
