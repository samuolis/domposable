package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.ui.DomposableCarousel
import io.github.samuolis.domposable.ui.DomposableCarouselLabels
import io.github.samuolis.domposable.ui.DomposableLazyItem
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposablePagedCarouselLayout
import io.github.samuolis.domposable.ui.fillMaxWidth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import io.github.samuolis.domposable.foundation.dp as portableDp

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NativePagedCarouselTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun compactAndWidePagesUseConfiguredHeightsAndWideNavigation() {
        var wide by mutableStateOf(false)
        compose.setContent {
            DomposableMaterialTheme {
                Box(Modifier.requiredWidth(if (wide) 720.dp else 320.dp)) {
                    DomposableCarousel(
                        items = pages(),
                        label = "Featured pages",
                        modifier = DomposableModifier.fillMaxWidth(),
                        pagedLayout = layout(compactHeight = 100, wideHeight = 140),
                    )
                }
            }
        }

        compose.onNodeWithContentDescription("Next").assertDoesNotExist()
        assertEquals(100f, pageHeight("page-a"), .5f)

        compose.runOnIdle { wide = true }
        compose.onNodeWithContentDescription("Next").assertExists()
        assertEquals(140f, pageHeight("page-a"), .5f)
    }

    @Test
    fun pagerControlsReportTheSelectedPageAndScaleTextAwareHeights() {
        var selected by mutableStateOf(0)
        compose.setContent {
            val density = LocalDensity.current
            androidx.compose.runtime.CompositionLocalProvider(
                LocalDensity provides Density(density.density, 1.5f),
            ) {
                DomposableMaterialTheme {
                    Box(Modifier.requiredWidth(720.dp)) {
                        DomposableCarousel(
                            items = pages(),
                            label = "Featured pages",
                            modifier = DomposableModifier.fillMaxWidth(),
                            currentItem = selected,
                            onCurrentItemChange = { selected = it },
                            pagedLayout = layout(compactHeight = 100, wideHeight = 120, scalesWithText = true),
                        )
                    }
                }
            }
        }

        compose.onNodeWithContentDescription("Next").performClick()
        compose.runOnIdle { assertEquals(1, selected) }
        compose.onNodeWithText("Page b").assertExists()
        assertEquals(180f, pageHeight("page-b"), .5f)
    }

    @Test
    fun singleItemOmitsPagingControls() {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableCarousel(
                    items = pages().take(1),
                    label = "One page",
                    pagedLayout = layout(compactHeight = null, wideHeight = null),
                )
            }
        }
        compose.onNodeWithContentDescription("Next").assertDoesNotExist()
        compose.onNodeWithContentDescription("Previous").assertDoesNotExist()
    }

    private fun pageHeight(tag: String): Float {
        val bounds = compose.onNodeWithTag(tag).getUnclippedBoundsInRoot()
        return bounds.bottom.value - bounds.top.value
    }

    private fun pages() =
        listOf("a", "b", "c").map { key ->
            DomposableLazyItem(key) {
                Box(Modifier.fillMaxSize().testTag("page-$key")) {
                    androidx.compose.material3.Text("Page $key")
                }
            }
        }

    private fun layout(
        compactHeight: Int?,
        wideHeight: Int?,
        scalesWithText: Boolean = false,
    ) = DomposablePagedCarouselLayout(
        compactHeight = compactHeight?.portableDp,
        wideHeight = wideHeight?.portableDp,
        heightTextScaleReference = if (scalesWithText) 14.portableDp else null,
        labels = DomposableCarouselLabels("Previous", "Next") { position, total -> "Slide $position of $total" },
    )
}
