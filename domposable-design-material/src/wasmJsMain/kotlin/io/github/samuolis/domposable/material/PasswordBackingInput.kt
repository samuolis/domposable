package io.github.samuolis.domposable.material

import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event

internal actual fun protectPasswordBackingInput(): () -> Unit {
    // CMP 1.12 sets inputmode=password but leaves the backing editor as a plain text input.
    val listener: (Event) -> Unit = { event ->
        val input = event.target as? HTMLInputElement
        if (input?.classList?.contains("compose-backing-field") == true &&
            input.getAttribute("inputmode") == "password"
        ) {
            input.type = "password"
        }
    }
    document.addEventListener("focusin", listener, true)
    return { document.removeEventListener("focusin", listener, true) }
}
