package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.ui.DomposableAvatar
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.widthIn
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlAvatarContentTest {
    @Test
    fun callerContentReplacesInitialsAndRetainsAvatarSemantics() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableAvatar("AB", "Alex Brown", content = { DomposableText("Profile photo") })
                }
            }

        try {
            val avatar = root.querySelector(".d-avatar") as HTMLElement
            val style = kotlinx.browser.window.getComputedStyle(avatar)
            assertEquals("hidden", style.getPropertyValue("overflow"))
            assertEquals("relative", style.position)
            assertEquals("img", avatar.getAttribute("role"))
            assertEquals("Alex Brown", avatar.getAttribute("aria-label"))
            assertEquals("Profile photo", avatar.textContent?.trim())
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun modifierSizeAndMaxConstraintsReachAvatarContentWithOverlayBorder() {
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableAvatar(
                        initials = "AB",
                        description = "Alex Brown",
                        modifier = DomposableModifier.size(DomposableLength(64f)),
                        appearance =
                            DomposableSurfaceAppearance(
                                border = DomposableColor(0xFF123456),
                                borderWidth = DomposableLength(4f),
                                shadowElevation = DomposableLength(3f),
                            ),
                        content = {
                            org.jetbrains.compose.web.dom.Div(attrs = {
                                classes("avatar-fill-test")
                                style {
                                    property("width", "100%")
                                    property("height", "100%")
                                }
                            }) { }
                        },
                    )
                    DomposableAvatar(
                        initials = "CD",
                        description = "Casey Doe",
                        modifier = DomposableModifier.widthIn(max = DomposableLength(20f)),
                    )
                }
            }
        try {
            val avatar = root.querySelector("[aria-label='Alex Brown']") as HTMLElement
            val content = root.querySelector(".avatar-fill-test") as HTMLElement
            val border = avatar.querySelector("[aria-hidden=true]") as HTMLElement
            val constrained = root.querySelector("[aria-label='Casey Doe']") as HTMLElement
            assertEquals(64.0, avatar.getBoundingClientRect().width)
            assertEquals(64.0, avatar.getBoundingClientRect().height)
            assertEquals(64.0, content.getBoundingClientRect().width)
            assertEquals(64.0, content.getBoundingClientRect().height)
            assertEquals("4px solid rgb(18, 52, 86)", border.style.border)
            assertEquals("", avatar.style.border)
            assertTrue(avatar.style.boxShadow.isNotEmpty())
            assertEquals(20.0, constrained.getBoundingClientRect().width)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun fillModifiersMakeAvatarFillItsConstrainedHost() {
        val root = document.createElement("div") as HTMLElement
        root.style.width = "128px"
        root.style.height = "72px"
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableMaterialTheme {
                    DomposableBox(DomposableModifier.size(DomposableLength(128f), DomposableLength(72f))) {
                        DomposableAvatar(
                            initials = "AB",
                            description = "Alex Brown",
                            modifier = DomposableModifier.fillMaxWidth().fillMaxHeight(),
                        )
                    }
                }
            }
        try {
            val avatar = root.querySelector("[aria-label='Alex Brown']") as HTMLElement
            assertEquals(128.0, avatar.getBoundingClientRect().width)
            assertEquals(72.0, avatar.getBoundingClientRect().height)
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}
