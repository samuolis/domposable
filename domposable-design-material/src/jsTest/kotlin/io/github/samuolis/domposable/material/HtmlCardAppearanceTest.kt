package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableCardVariant
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableCard
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlCardAppearanceTest {
    @Test
    fun explicitPaddingAndBorderDoNotChangeCardOuterWidth() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableCard(
                        modifier = DomposableModifier.width(DomposableLength(320f)),
                        variant = DomposableCardVariant.Outlined,
                        appearance = DomposableSurfaceAppearance(borderWidth = DomposableLength(3f)),
                        contentPadding =
                            DomposableInsets(
                                top = DomposableLength(8f),
                                end = DomposableLength(12f),
                                bottom = DomposableLength(8f),
                                start = DomposableLength(16f),
                            ),
                    ) {
                        DomposableBox(DomposableModifier.fillMaxWidth().testId("card-inner")) { }
                    }
                }
            }

        try {
            val card = root.querySelector(".d-card") as HTMLElement
            val inner = root.querySelector("[data-testid=card-inner]") as HTMLElement
            assertEquals("16px 12px", "${card.style.paddingLeft} ${card.style.paddingRight}")
            assertEquals("3px", card.style.getPropertyValue("--d-card-border-width"))
            assertTrue(card.style.getPropertyValue("--d-card-border-color").isNotEmpty())
            assertEquals(320.0, card.getBoundingClientRect().width)
            assertEquals(292.0, inner.getBoundingClientRect().width)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
