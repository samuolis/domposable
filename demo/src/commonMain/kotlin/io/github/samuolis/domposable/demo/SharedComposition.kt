package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.adaptive.DomposablePaneLayout
import io.github.samuolis.domposable.adaptive.DomposablePaneRole
import io.github.samuolis.domposable.adaptive.DomposablePaneScaffold
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableGradientStop
import io.github.samuolis.domposable.foundation.DomposableImageScale
import io.github.samuolis.domposable.foundation.DomposableLayoutAxis
import io.github.samuolis.domposable.foundation.DomposableLinearGradient
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableCard
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableFlowRow
import io.github.samuolis.domposable.ui.DomposableImage
import io.github.samuolis.domposable.ui.DomposableMeasure
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.background
import io.github.samuolis.domposable.ui.boxAlignment
import io.github.samuolis.domposable.ui.fillMaxSize
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.pointerPassthrough
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width

/** The card and pane content are the same Kotlin composition on every renderer. */
@Composable
@DomposableScreen(route = DemoRoute.Composition::class)
fun SharedCompositionScreen() {
    var compact by rememberSaveable { mutableStateOf(false) }
    var detail by rememberSaveable { mutableStateOf(true) }
    var note by rememberSaveable { mutableStateOf("") }
    DomposableColumn(DomposableModifier.fillMaxWidth().padding(16.dp), gap = 16.dp) {
        DomposableText("Shared cards and adaptive panes", headingLevel = 1)
        DomposableText("This screen has no application HTML, CSS or native widget implementations.")
        DomposableSwitch(compact, { compact = it }, "Use compact panes")
        DomposableButton("Show directory", { detail = false })
        DomposableMeasure(DomposableModifier.fillMaxWidth()) { size ->
            val expanded = !compact && size.width >= 720f
            val layout =
                if (expanded) {
                    DomposablePaneLayout.ListDetail("directory", "details")
                } else {
                    DomposablePaneLayout.Single(if (detail) "details" else "directory")
                }
            DomposablePaneScaffold(
                layout = layout,
                listLabel = "Workshop directory",
                detailLabel = "Workshop details",
                modifier = DomposableModifier.testId("composition-panes"),
                listModifier = DomposableModifier.padding(12.dp),
                detailModifier = DomposableModifier.padding(12.dp),
                listFraction = 0.32f,
                minListWidth = 240.dp,
                maxListWidth = 320.dp,
                gap = 16.dp,
                retainedList = "directory",
                singlePaneRole = if (detail) DomposablePaneRole.Detail else DomposablePaneRole.List,
            ) { entry ->
                if (entry == "directory") {
                    DomposableColumn(gap = 12.dp) {
                        DomposableText("Workshops", style = DomposableTextStyle.Title, headingLevel = 2)
                        DomposableTextField(note, { note = it }, "Directory note")
                        DomposableButton("Open workshop", { detail = true })
                    }
                } else {
                    SharedWorkshopCard()
                }
            }
        }
        SharedOverlayAlignments()
    }
}

/** Both axes are configured once, rather than in renderer-specific child selectors. */
@Composable
private fun SharedOverlayAlignments() {
    DomposableText("Overlay alignments", headingLevel = 2)
    DomposableFlowRow(gap = 12.dp) {
        DomposableAlignment.entries.forEach { vertical ->
            DomposableAlignment.entries.forEach { horizontal ->
                val id = "overlay-${horizontal.name}-${vertical.name}"
                DomposableBox(
                    modifier = DomposableModifier.width(100.dp).height(80.dp).testId(id),
                    alignment = horizontal,
                    verticalAlignment = vertical,
                ) {
                    DomposableBox(
                        DomposableModifier
                            .width(20.dp)
                            .height(16.dp)
                            .background(DomposableColor(0xFF4059AD))
                            .testId("$id-child"),
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun SharedWorkshopCard() {
    var imageLoaded by remember { mutableStateOf(false) }
    var mediaActions by rememberSaveable { mutableStateOf(0) }
    var saved by rememberSaveable { mutableStateOf(false) }
    val scrim =
        DomposableLinearGradient(
            axis = DomposableLayoutAxis.Vertical,
            stops =
                listOf(
                    DomposableGradientStop(DomposableColor(0x00000000), 0f),
                    DomposableGradientStop(DomposableColor(0xDD000000), 1f),
                ),
        )
    DomposableCard(
        DomposableModifier.fillMaxWidth().testId(if (imageLoaded) "shared-card-loaded" else "shared-card-loading"),
    ) {
        DomposableBox(DomposableModifier.fillMaxWidth().height(240.dp).testId("shared-card-hero")) {
            DomposableImage(
                DEMO_ARTWORK,
                null,
                DomposableModifier.fillMaxSize().testId("shared-card-image"),
                scale = DomposableImageScale.Crop,
                onLoaded = { imageLoaded = true },
            )
            DomposableButton(
                "Media action",
                { mediaActions++ },
                DomposableModifier.boxAlignment(DomposableAlignment.Start, DomposableAlignment.Start).padding(12.dp),
            )
            DomposableBox(
                DomposableModifier
                    .fillMaxSize()
                    .background(scrim)
                    .pointerPassthrough()
                    .testId("shared-card-scrim"),
            ) {}
            DomposableBox(DomposableModifier.fillMaxSize().pointerPassthrough()) {
                DomposableColumn(
                    DomposableModifier.boxAlignment(DomposableAlignment.Start, DomposableAlignment.End).padding(20.dp),
                    gap = 8.dp,
                ) {
                    val lightText = DomposableTextAppearance(color = DomposableColor(0xFFFFFFFF))
                    DomposableText("Design workshop", headingLevel = 2, appearance = lightText)
                    DomposableText("Saturday · 10:00 · Studio 4", appearance = lightText)
                    DomposableButton(if (saved) "Workshop saved" else "Save workshop", { saved = !saved })
                }
            }
        }
        DomposableColumn(DomposableModifier.padding(20.dp), gap = 12.dp) {
            DomposableText("Media actions: $mediaActions")
            DomposableText("Compose the card once", style = DomposableTextStyle.Title)
            DomposableText("Images, gradient scrims, overlays, typography and spacing use shared APIs.")
        }
    }
}
