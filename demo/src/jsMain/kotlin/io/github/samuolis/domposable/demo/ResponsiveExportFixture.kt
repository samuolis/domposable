package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.adaptive.DomposableHtmlResponsiveGrid
import io.github.samuolis.domposable.adaptive.DomposableHtmlResponsiveHeight
import io.github.samuolis.domposable.adaptive.DomposableHtmlResponsiveSplit
import io.github.samuolis.domposable.adaptive.DomposablePaneLayout
import io.github.samuolis.domposable.adaptive.DomposablePaneRole
import io.github.samuolis.domposable.adaptive.DomposablePaneScaffold
import io.github.samuolis.domposable.adaptive.DomposableResponsive
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.clip
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.htmlAttributes
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text

@Composable
internal fun ResponsiveExportFixture() {
    DomposableMaterialTheme {
        Div(attrs = {
            style {
                property("font-family", "Arial,sans-serif")
                property("padding", "16px")
            }
        }) {
            H1 { Text("Responsive export fixture") }
            DomposableHtmlResponsiveGrid(
                columns = DomposableResponsive(compact = 1, medium = 2, expanded = 3, large = 4),
                gap = 12.dp,
                rootAttrs = { attr("id", "responsive-grid") },
            ) {
                repeat(4) { index ->
                    Div(attrs = {
                        attr("data-grid-item", index.toString())
                        style {
                            property("height", "72px")
                            property("background", if (index % 2 == 0) "#dbeafe" else "#fee2e2")
                        }
                    }) { Text("Grid item ${index + 1}") }
                }
            }

            DomposableHtmlResponsiveHeight(
                heights = DomposableResponsive(100.dp, 140.dp, 180.dp, 220.dp),
                useViewport = true,
                rootAttrs = { attr("id", "responsive-height") },
            ) { Div(attrs = { style { property("height", "100%") } }) { Text("Viewport height") } }

            matchedSplit("split-short", mediaFirst = false, textHeight = 80)
            matchedSplit("split-middle", mediaFirst = false, textHeight = 300)
            matchedSplit("split-long", mediaFirst = true, textHeight = 520, mediaWidth = 240.dp)
            intrinsicSplit("split-intrinsic-short", textHeight = 80)
            intrinsicSplit("split-intrinsic-long", textHeight = 800)
            paneScaffolds()
        }
    }
}

@Composable
private fun paneScaffolds() {
    DomposablePaneScaffold(
        layout = DomposablePaneLayout.ListDetail("organization-directory", "organization-details"),
        listLabel = "Organization directory",
        detailLabel = "Organization details",
        modifier = DomposableModifier.fillMaxWidth().htmlAttributes { attr("id", "export-pane-root") },
        listModifier = DomposableModifier.htmlAttributes { attr("id", "export-pane-list") },
        detailModifier = DomposableModifier.htmlAttributes { attr("id", "export-pane-detail") },
        listFraction = 0.32f,
        minListWidth = 260.dp,
        maxListWidth = 360.dp,
        collapseBelow = 768.dp,
    ) { entry ->
        Div(attrs = { attr("data-pane-entry", entry) }) { Text(entry) }
    }
    DomposablePaneScaffold(
        layout = DomposablePaneLayout.ListDetail("unconfigured-directory", "unconfigured-details"),
        listLabel = "Unconfigured directory",
        detailLabel = "Unconfigured details",
        modifier = DomposableModifier.fillMaxWidth().htmlAttributes { attr("id", "export-unconfigured-pane-root") },
        listModifier = DomposableModifier.htmlAttributes { attr("id", "export-unconfigured-pane-list") },
        detailModifier = DomposableModifier.htmlAttributes { attr("id", "export-unconfigured-pane-detail") },
        listFraction = 0.32f,
    ) { entry ->
        Div(attrs = { attr("data-pane-entry", entry) }) { Text(entry) }
    }
    DomposablePaneScaffold(
        layout = DomposablePaneLayout.Single("single-directory"),
        listLabel = "Single directory",
        detailLabel = "Single detail",
        modifier = DomposableModifier.fillMaxWidth().htmlAttributes { attr("id", "export-single-directory") },
        singlePaneRole = DomposablePaneRole.List,
    ) { entry ->
        Div(attrs = { attr("data-pane-entry", entry) }) { Text(entry) }
    }
}

@Composable
private fun matchedSplit(
    id: String,
    mediaFirst: Boolean,
    textHeight: Int,
    mediaWidth: io.github.samuolis.domposable.foundation.DomposableLength? = null,
) {
    DomposableHtmlResponsiveSplit(
        gap = 12.dp,
        mediaWidth = mediaWidth,
        mediaFirst = mediaFirst,
        mediaMatchesText = true,
        mediaMinHeight = 200.dp,
        mediaMaxHeight = 400.dp,
        rootAttrs = { attr("id", id) },
        mediaContent = {
            DomposableHtmlModifiers(DomposableModifier.fillMaxWidth().clip(DomposableShape.Rounded(12.dp))) {
                DomposableHtmlResponsiveHeight(
                    heights = DomposableResponsive(100.dp),
                    useViewport = true,
                    rootAttrs = {
                        attr("id", "$id-media-height")
                        style { property("position", "relative") }
                    },
                ) {
                    Div(attrs = {
                        attr("id", "$id-media-art")
                        style {
                            property("position", "absolute")
                            property("inset", "0")
                            property("height", "100%")
                            property("background", "#dcfce7")
                        }
                    }) { Text("Media") }
                }
            }
        },
        textContent = {
            Div(attrs = {
                attr("id", "$id-text")
                style {
                    property("height", "${textHeight}px")
                    property("background", "#f3f4f6")
                }
            }) { Text("Text panel ".repeat(3)) }
        },
    )
}

/** Intrinsic artwork exceeds the image-height cap and must remain in document flow. */
@Composable
private fun intrinsicSplit(id: String, textHeight: Int) {
    DomposableHtmlResponsiveSplit(
        gap = 12.dp,
        mediaFirst = true,
        mediaMatchesText = true,
        mediaMinHeight = 200.dp,
        mediaMaxHeight = 400.dp,
        rootAttrs = { attr("id", id) },
        mediaContent = {
            Div(attrs = {
                attr("id", "$id-art")
                style {
                    property("width", "100%")
                    property("height", "560px")
                    property("background", "linear-gradient(#bce7d8,#147b65)")
                }
            }) { Text("Intrinsic artwork — full height") }
        },
        textContent = {
            Div(attrs = {
                attr("id", "$id-text")
                style {
                    property("height", "${textHeight}px")
                    property("background", "#f3f4f6")
                }
            }) { Text("Adjacent description") }
        },
    )
}
