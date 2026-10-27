package io.github.samuolis.domposable.material

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableTextFieldProps
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTextAreaElement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertTrue

class HtmlTextFieldAutoSizeTest {
    @Test
    fun autoSizeTextareaShrinksBackToItsMinimumLineHeightAfterClearing() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            val root = document.createElement("div") as HTMLElement
            root.style.width = "360px"
            document.body!!.appendChild(root)
            var value by mutableStateOf("")
            val composition =
                renderComposable(root) {
                    DomposableMaterialTheme {
                        DomposableTextField(
                            DomposableTextFieldProps(
                                value = value,
                                onValueChange = { value = it },
                                label = "Notes",
                                multiline = true,
                                singleLine = false,
                                minLines = 1,
                                maxLines = 3,
                                appearance = DomposableTextFieldAppearance(autoSize = true),
                            ),
                        )
                    }
                }
            try {
                val textarea = root.querySelector("textarea") as HTMLTextAreaElement
                val emptyHeight = textarea.heightAfterLayout()
                val style = window.getComputedStyle(textarea)
                val lineHeight = style.lineHeight.removeSuffix("px").toFloatOrNull() ?: 24f
                val verticalChrome =
                    listOf(
                        style.paddingTop,
                        style.paddingBottom,
                        style.borderTopWidth,
                        style.borderBottomWidth,
                    ).sumOf { it.removeSuffix("px").toDoubleOrNull() ?: 0.0 }.toFloat()
                val minimumHeight = lineHeight + verticalChrome
                val maximumHeight = lineHeight * 3 + verticalChrome
                assertTrue(emptyHeight <= minimumHeight + 1f, "An empty one-line field must use its minimum height")

                textarea.value = "team schedule participant details ".repeat(10)
                textarea.dispatchEvent(js("new Event('input', { bubbles: true })") as org.w3c.dom.events.Event)
                val expandedHeight = textarea.heightAfterLayout()
                assertTrue(expandedHeight > emptyHeight + 1f, "Long content must grow the field")
                assertTrue(expandedHeight <= maximumHeight + 1f, "Long content must respect maxLines")

                textarea.value = ""
                textarea.dispatchEvent(js("new Event('input', { bubbles: true })") as org.w3c.dom.events.Event)
                val collapsedHeight = textarea.heightAfterLayout()
                assertTrue(collapsedHeight <= minimumHeight + 1f, "Clearing content must restore minLines")
                assertTrue(collapsedHeight < expandedHeight - 1f, "Clearing content must shrink the field")
            } finally {
                composition.dispose()
                root.parentNode?.removeChild(root)
            }
        }

    private suspend fun HTMLTextAreaElement.heightAfterLayout(): Float {
        repeat(3) {
            suspendCoroutine<Unit> { continuation ->
                window.requestAnimationFrame { continuation.resume(Unit) }
            }
        }
        return getBoundingClientRect().height.toFloat()
    }
}
