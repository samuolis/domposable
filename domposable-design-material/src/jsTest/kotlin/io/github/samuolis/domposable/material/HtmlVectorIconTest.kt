package io.github.samuolis.domposable.material

import androidx.compose.runtime.mutableStateOf
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableIconAppearance
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableIcon
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableVectorIcon
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.widthIn
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HtmlVectorIconTest {
    @Test
    fun vectorPathUsesSvgNamespaceFillRuleCurrentColorAndCallerSize() =
        CoroutineScope(Dispatchers.Unconfined).promise {
            val root = document.createElement("div")
            document.body!!.appendChild(root)
            val vector =
                mutableStateOf(
                    DomposableVectorIcon(
                        pathData = "M0 0h24v24H0z",
                        viewportWidth = 24f,
                        viewportHeight = 20f,
                        evenOdd = true,
                    ),
                )
            var iconElement: HTMLElement? = null
            val composition =
                renderComposable(root) {
                    DomposableMaterialTheme {
                        DomposableIcon(
                            vector = vector.value,
                            description = "Map marker",
                            modifier = DomposableModifier.size(32.dp),
                            appearance = DomposableIconAppearance(color = DomposableColor(0xFF336699)),
                        )
                    }
                }
            try {
                val icon = root.querySelector(".d-icon-vector") as HTMLElement
                iconElement = icon
                val svg = assertNotNull(icon.querySelector("svg"))
                val path = assertNotNull(svg.querySelector("path"))
                assertEquals("img", icon.getAttribute("role"))
                assertEquals("Map marker", icon.getAttribute("aria-label"))
                assertEquals("true", svg.getAttribute("aria-hidden"))
                assertEquals(listOf(0f, 0f, 24f, 20f), svg.getAttribute("viewBox")!!.split(" ").map(String::toFloat))
                assertEquals("100%", svg.getAttribute("width"))
                assertEquals("100%", svg.getAttribute("height"))
                assertEquals("M0 0h24v24H0z", path.getAttribute("d"))
                assertEquals("currentColor", path.getAttribute("fill"))
                assertEquals("evenodd", path.getAttribute("fill-rule"))
                assertEquals("rgb(51, 102, 153)", window.getComputedStyle(path).getPropertyValue("fill"))
                assertEquals("100%", icon.style.width)
                assertEquals("100%", icon.style.height)
                assertEquals(32.0, icon.getBoundingClientRect().width)
                assertEquals(32.0, icon.getBoundingClientRect().height)

                vector.value = DomposableVectorIcon("M1 2L3 4Z", viewportWidth = 10f, viewportHeight = 12f)
                repeat(2) {
                    suspendCoroutine<Unit> { continuation ->
                        window.requestAnimationFrame { continuation.resume(Unit) }
                    }
                }
                assertEquals(
                    listOf(0f, 0f, 10f, 12f),
                    svg.getAttribute("viewBox")!!.split(" ").map(String::toFloat),
                )
                assertEquals("M1 2L3 4Z", path.getAttribute("d"))
            } finally {
                composition.dispose()
                assertNull(iconElement?.querySelector("svg"), "Imperative SVG child should be removed with its ref")
                root.parentNode?.removeChild(root)
            }
        }

    @Test
    fun widthConstraintsClampTheIntrinsicVectorViewport() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableIcon(
                        vector = DomposableVectorIcon("M0 0h24v24H0z"),
                        description = "Small icon",
                        modifier = DomposableModifier.widthIn(max = 12.dp),
                    )
                }
            }
        try {
            val icon = root.querySelector(".d-icon-vector") as HTMLElement
            val svg = assertNotNull(icon.querySelector("svg"))
            assertEquals(12.0, icon.getBoundingClientRect().width)
            assertEquals(12.0, svg.getBoundingClientRect().width)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun fillModifiersSizeTheHostAndItsVectorChild() {
        val root = document.createElement("div") as HTMLElement
        root.style.width = "128px"
        root.style.height = "72px"
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableBox(DomposableModifier.size(128.dp, 72.dp)) {
                        DomposableIcon(
                            vector = DomposableVectorIcon("M0 0h24v24H0z"),
                            description = "Filling icon",
                            modifier = DomposableModifier.fillMaxWidth().fillMaxHeight(),
                        )
                    }
                }
            }
        try {
            val icon = root.querySelector(".d-icon-vector") as HTMLElement
            val svg = assertNotNull(icon.querySelector("svg"))
            assertEquals("100%", svg.getAttribute("width"))
            assertEquals("100%", svg.getAttribute("height"))
            assertEquals(128.0, icon.getBoundingClientRect().width)
            assertEquals(72.0, icon.getBoundingClientRect().height)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun decorativeVectorIsHiddenFromAccessibilityTree() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableIcon(DomposableVectorIcon("M1 1L2 2Z"), description = null)
                }
            }
        try {
            val icon = root.querySelector(".d-icon-vector") as HTMLElement
            assertEquals("true", icon.getAttribute("aria-hidden"))
            assertEquals(null, icon.getAttribute("role"))
            assertEquals(null, icon.getAttribute("aria-label"))
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
