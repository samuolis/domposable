package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBadge
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableCarousel
import io.github.samuolis.domposable.ui.DomposableCarouselLabels
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableIcon
import io.github.samuolis.domposable.ui.DomposableLazyItem
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposablePagedCarouselLayout
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableSurface
import io.github.samuolis.domposable.ui.DomposableTabItem
import io.github.samuolis.domposable.ui.DomposableTabs
import io.github.samuolis.domposable.ui.DomposableTabsActivation
import io.github.samuolis.domposable.ui.DomposableTabsAppearance
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableVectorIcon
import io.github.samuolis.domposable.ui.fillMaxSize
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.weight

/** Shared custom control slots and responsive pages; no renderer-specific code or application CSS. */
@Composable
internal fun SharedLayoutRecipe() {
    var tab by rememberSaveable { mutableStateOf(0) }
    var page by rememberSaveable { mutableStateOf(0) }
    DomposableTabs(
        items =
            listOf(
                DomposableTabItem(
                    "overview",
                    "Overview",
                    icon = {
                        DomposableIcon(
                            vector =
                                DomposableVectorIcon(
                                    "M12 2L22 12L12 22L2 12Z M12 6L6 12L12 18L18 12Z",
                                    evenOdd = true,
                                ),
                            description = null,
                            modifier = DomposableModifier.size(24.dp),
                        )
                    },
                ),
                DomposableTabItem("saved", "Saved", badgeContent = { DomposableBadge("3") }),
                DomposableTabItem("unavailable", "Unavailable", enabled = false),
            ),
        selectedIndex = tab,
        onSelect = { tab = it },
        label = "Shared layout sections",
        tabsAppearance = DomposableTabsAppearance(activation = DomposableTabsActivation.Manual),
    )
    DomposableText("Selected section: ${tab + 1}")
    DomposableText("Weighted row in a start-aligned padded box")
    DomposableBox(modifier = DomposableModifier.padding(12.dp)) {
        DomposableRow(gap = 12.dp) {
            DomposableText("Ranker club", modifier = DomposableModifier.weight(1f))
            DomposableText("League points 1,250")
        }
    }
    SharedSlotButtons()
    DomposableCarousel(
        items =
            (1..3).map { index ->
                DomposableLazyItem("page-$index") {
                    DomposableSurface(modifier = DomposableModifier.fillMaxSize()) {
                        DomposableColumn(modifier = DomposableModifier.padding(16.dp), gap = 8.dp) {
                            DomposableText("Page $index")
                            DomposableText("One shared composition adapts its page width and controls.")
                        }
                    }
                }
            },
        label = "Responsive shared pages",
        modifier = DomposableModifier.fillMaxWidth().testId("shared-paged-carousel"),
        currentItem = page,
        onCurrentItemChange = { page = it },
        pagedLayout =
            DomposablePagedCarouselLayout(
                compactHeight = 120.dp,
                wideHeight = 160.dp,
                labels =
                    DomposableCarouselLabels(
                        "Previous page",
                        "Next page",
                    ) { position, total -> "Page $position of $total" },
            ),
    )
}

@Composable
private fun SharedSlotButtons() {
    for (alignment in DomposableAlignment.entries) {
        DomposableButton(
            onClick = {},
            modifier = DomposableModifier.fillMaxWidth(),
            style = DomposableButtonStyle.Outlined,
            appearance = DomposableButtonAppearance(contentAlignment = alignment),
        ) { DomposableText("${alignment.name} content") }
    }
    DomposableButton(
        onClick = {},
        modifier = DomposableModifier.fillMaxWidth(),
        appearance = DomposableButtonAppearance(contentPadding = DomposableInsets(start = 16.dp, end = 16.dp)),
    ) {
        DomposableRow(
            modifier = DomposableModifier.fillMaxWidth(),
            gap = 12.dp,
            alignment = DomposableAlignment.Center,
        ) {
            DomposableText("Custom slot with trailing content", modifier = DomposableModifier.weight(1f))
            DomposableText("›", appearance = DomposableTextAppearance(fontWeight = DomposableFontWeight.Bold))
        }
    }
}
