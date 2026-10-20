package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBottomAppBar
import io.github.samuolis.domposable.ui.DomposableCarousel
import io.github.samuolis.domposable.ui.DomposableDrawerVariant
import io.github.samuolis.domposable.ui.DomposableLazyItem
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableNavigationDrawer
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.padding
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlCatalogModifierTest {
    @Test
    fun drawerAppliesPaddingOnce() =
        assertSinglePadding { modifier ->
            DomposableNavigationDrawer(
                expanded = true,
                onDismiss = {},
                variant = DomposableDrawerVariant.Permanent,
                modifier = modifier,
            ) { DomposableText("Navigation content") }
        }

    @Test
    fun bottomBarAppliesPaddingOnce() =
        assertSinglePadding { modifier ->
            DomposableBottomAppBar(modifier = modifier) { DomposableText("Bar content") }
        }

    @Test
    fun carouselAppliesPaddingOnce() =
        assertSinglePadding { modifier ->
            DomposableCarousel(
                items = listOf(DomposableLazyItem("item") { DomposableText("Slide") }),
                label = "Slides",
                modifier = modifier,
            )
        }

    private fun assertSinglePadding(content: @Composable (DomposableModifier) -> Unit) {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme { content(DomposableModifier.padding(7.dp)) }
            }
        try {
            val nodes = root.querySelectorAll("[style]")
            val matching =
                (0 until nodes.length).count {
                    (nodes.item(it) as HTMLElement).style.getPropertyValue("padding-block-start") == "7px"
                }
            assertEquals(1, matching, "A component must apply its public modifier exactly once")
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
