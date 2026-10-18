package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableLink
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.testId
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class HtmlControlSizingTest {
    @Test
    fun buttonAndItsTouchTargetFillBoundedWidth() =
        assertFullWidth { modifier ->
            DomposableButton(text = "Action", onClick = {}, modifier = modifier)
        }

    @Test
    fun styledAnchorFillsBoundedWidth() =
        assertFullWidth { modifier ->
            DomposableLink(
                label = "Destination",
                href = "/destination",
                modifier = modifier,
                buttonAppearance = DomposableButtonAppearance(),
            )
        }

    @Test
    fun plainAnchorFillsBoundedWidthWhenRequested() =
        assertFullWidth { modifier ->
            DomposableLink(label = "Destination", href = "/destination", modifier = modifier)
        }

    private fun assertFullWidth(content: @Composable (DomposableModifier) -> Unit) {
        val root = document.createElement("div") as HTMLElement
        root.style.width = "320px"
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    content(DomposableModifier.fillMaxWidth().testId("control"))
                }
            }
        try {
            val control = root.querySelector("[data-testid=control]") as HTMLElement
            assertTrue(abs(control.getBoundingClientRect().width - root.getBoundingClientRect().width) < 1.0)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
