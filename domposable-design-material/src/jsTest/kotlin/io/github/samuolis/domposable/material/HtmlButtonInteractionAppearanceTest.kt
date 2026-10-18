package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableInteractionAppearance
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableStateLayerOpacity
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableButtonProps
import io.github.samuolis.domposable.ui.DomposableIconButton
import io.github.samuolis.domposable.ui.DomposableLink
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.testId
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlButtonInteractionAppearanceTest {
    @Test
    fun typedStatesReachButtonsAndButtonLikeLinksWithoutChangingDefaultTheme() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                StyledInteractionControls()
            }
        try {
            assertStyledControls(root)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Composable
    private fun StyledInteractionControls() {
        DomposableMaterialTheme {
            DomposableButton(
                DomposableButtonProps(
                    onClick = {},
                    appearance =
                        DomposableButtonAppearance(
                            interaction =
                                DomposableInteractionAppearance(
                                    hoveredContainer = DomposableColor(0xFF112233),
                                    pressedContent = DomposableColor(0xFF445566),
                                    stateLayerOpacity =
                                        DomposableStateLayerOpacity(
                                            hovered = .09f,
                                            focused = .11f,
                                            pressed = .16f,
                                        ),
                                    focusRingColor = DomposableColor(0xFF778899),
                                    focusRingWidth = DomposableLength(4f),
                                    focusRingOffset = DomposableLength(6f),
                                ),
                        ),
                ),
                DomposableModifier.testId("styled-button"),
            ) { }
            DomposableIconButton(
                onClick = {},
                label = "Close",
                modifier = DomposableModifier.testId("styled-icon"),
                appearance =
                    DomposableButtonAppearance(
                        interaction =
                            DomposableInteractionAppearance(
                                pressedContainer = DomposableColor(0xFF010203),
                            ),
                    ),
            ) { }
            DomposableLink(
                label = "Action link",
                href = "/next",
                modifier = DomposableModifier.testId("styled-link"),
                buttonAppearance =
                    DomposableButtonAppearance(
                        interaction = DomposableInteractionAppearance(hoveredContent = DomposableColor(0xFF010203)),
                    ),
            )
            DomposableButton(
                DomposableButtonProps(
                    onClick = {},
                    enabled = false,
                    appearance = DomposableButtonAppearance(disabledOpacity = .38f),
                ),
                DomposableModifier.testId("disabled-button"),
            ) { }
        }
    }

    private fun assertStyledControls(root: org.w3c.dom.Element) {
        val button = root.querySelector("[data-testid=styled-button]") as HTMLElement
        assertEquals("true", button.getAttribute("data-d-button-interaction"))
        assertEquals("rgba(17,34,51,1)", button.style.getPropertyValue("--d-button-hovered-container"))
        assertEquals("rgba(68,85,102,1)", button.style.getPropertyValue("--d-button-pressed-content"))
        assertEquals("4px", button.style.getPropertyValue("--d-button-focus-ring-width"))
        assertEquals("6px", button.style.getPropertyValue("--d-button-focus-ring-offset"))
        assertEquals("", button.style.getPropertyValue("--d-button-hover-feedback"))
        assertTrue(button.style.getPropertyValue("--d-button-pressed-feedback").contains("color-mix"))
        val link = root.querySelector("[data-testid=styled-link]") as HTMLElement
        assertEquals("true", link.getAttribute("data-d-button-interaction"))
        assertEquals("rgba(1,2,3,1)", link.style.getPropertyValue("--d-button-hovered-content"))
        val disabled = root.querySelector("[data-testid=disabled-button]") as HTMLElement
        assertEquals("0.38", disabled.style.opacity)
        val icon = root.querySelector("[data-testid=styled-icon]") as HTMLElement
        assertEquals("true", icon.getAttribute("data-d-button-interaction"))
        assertEquals("rgba(1,2,3,1)", icon.style.getPropertyValue("--d-button-pressed-container"))
    }
}
