package io.github.samuolis.domposable.material

import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.ParentNode
import org.w3c.dom.ShadowRoot
import org.w3c.dom.events.Event

internal actual fun protectPasswordBackingInput(): () -> Unit {
    // CMP 1.12 sets inputmode=password but leaves the backing editor as a plain text input.
    val listener: (Event) -> Unit = { event ->
        val input = event.target as? HTMLInputElement
        if (input?.classList?.contains("compose-backing-field") == true &&
            input.getAttribute("inputmode") == "password" && input.type != "password"
        ) {
            input.type = "password"
        }
    }
    val roots = mutableListOf<ShadowRoot>()

    fun attach(parent: ParentNode) {
        val elements = parent.querySelectorAll("*")
        for (index in 0 until elements.length) {
            val root = (elements.item(index) as? Element)?.shadowRoot ?: continue
            // Focus transfers within a shadow root do not reach document listeners.
            root.addEventListener("focus", listener, true)
            roots += root
            attach(root)
        }
    }
    attach(document)
    return { roots.forEach { it.removeEventListener("focus", listener, true) } }
}
