package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableCard
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableFlowRow
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.constrainContentWidth
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.minHeight
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width

internal data class NestedColumnWidthExpectation(
    val testId: String,
    val expectedWidthDp: Int,
    val label: String,
    val measurePaddingAncestor: Boolean = false,
)

internal val nestedColumnWidthExpectations =
    listOf(
        NestedColumnWidthExpectation("wide-host", 578, "bounded host width"),
        NestedColumnWidthExpectation("wide-fill-card", 578, "nested fill-width card"),
        NestedColumnWidthExpectation("auto-box", 578, "automatic Box passes fill demand to its ancestor"),
        NestedColumnWidthExpectation("auto-card", 578, "automatic Card passes fill demand to its ancestor"),
        NestedColumnWidthExpectation("finite-flow-row", 200, "FlowRow preserves finite capped demand"),
        NestedColumnWidthExpectation(
            "padded-column",
            232,
            "padding outside child max-width preserves outer inset",
            measurePaddingAncestor = true,
        ),
        NestedColumnWidthExpectation(
            "padded-max-column",
            232,
            "padding-before-max-width preserves modifier order",
            measurePaddingAncestor = true,
        ),
        NestedColumnWidthExpectation("fractional-column", 289, "fractional fill uses half of the bounded parent"),
        NestedColumnWidthExpectation("fixed-host", 200, "fixed-width boundary"),
        NestedColumnWidthExpectation("fixed-fill-card", 200, "descendant fill stops at the fixed-width boundary"),
        NestedColumnWidthExpectation("max-200-outer-column", 200, "max-width 200 caps the automatic ancestor"),
        NestedColumnWidthExpectation("max-200-fill-card", 200, "fill remains bounded by max-width 200"),
        NestedColumnWidthExpectation("max-1000-outer-column", 578, "max-width 1000 respects available width"),
        NestedColumnWidthExpectation("max-1000-fill-card", 578, "fill within max-width 1000 uses available width"),
        NestedColumnWidthExpectation(
            "sibling-outer-column",
            300,
            "wider intrinsic sibling is not capped by child fill demand",
        ),
        NestedColumnWidthExpectation("capped-row", 266, "row sums capped fill children and the gap"),
        NestedColumnWidthExpectation("capped-max-row", 500, "row max-width caps its summed child demand"),
        NestedColumnWidthExpectation(
            "long-sibling-column",
            578,
            "long wrapping text sibling can grow beyond capped fill demand",
        ),
        NestedColumnWidthExpectation("narrow-host", 100, "narrow parent width"),
        NestedColumnWidthExpectation("narrow-auto-column", 100, "a 200px cap remains bounded by a 100px parent"),
        NestedColumnWidthExpectation("narrow-fill-card", 100, "fill uses the narrower available width"),
    )

internal val intrinsicColumnWidthTestIds = listOf("compact-button", "intrinsic-column")

@Composable
internal fun NestedColumnWidthFixture(showFill: Boolean) {
    DomposableMaterialTheme {
        DomposableBox(modifier = DomposableModifier.width(DomposableLength(578f)).testId("wide-host")) {
            DomposableColumn(alignment = DomposableAlignment.Start) {
                if (showFill) {
                    DomposableColumn(alignment = DomposableAlignment.Start) {
                        DomposableCard(modifier = DomposableModifier.fillMaxWidth().testId("wide-fill-card")) {
                            DomposableText("Wide card")
                        }
                    }
                }
                DomposableButton(
                    text = "Sign in",
                    onClick = {},
                    modifier = DomposableModifier.minHeight(DomposableLength(40f)).testId("compact-button"),
                )
                DomposableBox(modifier = DomposableModifier.testId("auto-box")) {
                    DomposableColumn(alignment = DomposableAlignment.Start) {
                        DomposableCard(modifier = DomposableModifier.fillMaxWidth()) { DomposableText("Auto Box") }
                    }
                }
                DomposableCard(modifier = DomposableModifier.testId("auto-card")) {
                    DomposableColumn(alignment = DomposableAlignment.Start) {
                        DomposableCard(modifier = DomposableModifier.fillMaxWidth()) { DomposableText("Auto Card") }
                    }
                }
                DomposableFlowRow(modifier = DomposableModifier.testId("finite-flow-row")) {
                    DomposableBox(modifier = DomposableModifier.constrainContentWidth(DomposableLength(200f))) {
                        DomposableColumn(alignment = DomposableAlignment.Start) {
                            DomposableCard(modifier = DomposableModifier.fillMaxWidth()) { DomposableText("Flow") }
                        }
                    }
                }
                DomposableColumn(
                    modifier = DomposableModifier.testId("padded-column").padding(DomposableLength(16f)),
                    alignment = DomposableAlignment.Start,
                ) {
                    DomposableBox(modifier = DomposableModifier.constrainContentWidth(DomposableLength(200f))) {
                        DomposableColumn(alignment = DomposableAlignment.Start) {
                            DomposableCard(modifier = DomposableModifier.fillMaxWidth()) { DomposableText("Padded") }
                        }
                    }
                }
                DomposableColumn(
                    modifier =
                        DomposableModifier
                            .testId("padded-max-column")
                            .padding(DomposableLength(16f))
                            .constrainContentWidth(DomposableLength(200f)),
                    alignment = DomposableAlignment.Start,
                ) {
                    DomposableColumn(alignment = DomposableAlignment.Start) {
                        DomposableCard(modifier = DomposableModifier.fillMaxWidth()) { DomposableText("Padded max") }
                    }
                }
                DomposableColumn(
                    modifier = DomposableModifier.fillMaxWidth(0.5f).testId("fractional-column"),
                    alignment = DomposableAlignment.Start,
                ) { DomposableText("Half width") }
                DomposableColumn(
                    modifier = DomposableModifier.testId("intrinsic-column"),
                    alignment = DomposableAlignment.Start,
                ) {
                    DomposableText("i")
                }
                DomposableBox(modifier = DomposableModifier.width(DomposableLength(200f)).testId("fixed-host")) {
                    DomposableColumn(alignment = DomposableAlignment.Start) {
                        DomposableColumn(alignment = DomposableAlignment.Start) {
                            DomposableCard(modifier = DomposableModifier.fillMaxWidth().testId("fixed-fill-card")) {
                                DomposableText("Bounded card")
                            }
                        }
                    }
                }
                DomposableColumn(
                    modifier = DomposableModifier.testId("max-200-outer-column"),
                    alignment = DomposableAlignment.Start,
                ) {
                    DomposableBox(modifier = DomposableModifier.constrainContentWidth(DomposableLength(200f))) {
                        DomposableColumn(alignment = DomposableAlignment.Start) {
                            DomposableCard(
                                modifier = DomposableModifier.fillMaxWidth().testId("max-200-fill-card"),
                            ) { DomposableText("Capped 200") }
                        }
                    }
                }
                DomposableColumn(
                    modifier = DomposableModifier.testId("max-1000-outer-column"),
                    alignment = DomposableAlignment.Start,
                ) {
                    DomposableBox(modifier = DomposableModifier.constrainContentWidth(DomposableLength(1000f))) {
                        DomposableColumn(alignment = DomposableAlignment.Start) {
                            DomposableCard(
                                modifier = DomposableModifier.fillMaxWidth().testId("max-1000-fill-card"),
                            ) { DomposableText("Capped 1000") }
                        }
                    }
                }
                DomposableColumn(
                    modifier = DomposableModifier.testId("sibling-outer-column"),
                    alignment = DomposableAlignment.Start,
                ) {
                    DomposableBox(modifier = DomposableModifier.constrainContentWidth(DomposableLength(200f))) {
                        DomposableColumn(alignment = DomposableAlignment.Start) {
                            DomposableCard(modifier = DomposableModifier.fillMaxWidth()) {
                                DomposableText("Capped sibling")
                            }
                        }
                    }
                    DomposableBox(modifier = DomposableModifier.width(DomposableLength(300f))) {
                        DomposableText("Wide sibling")
                    }
                }
                DomposableColumn(
                    modifier = DomposableModifier.testId("long-sibling-column"),
                    alignment = DomposableAlignment.Start,
                ) {
                    DomposableBox(modifier = DomposableModifier.constrainContentWidth(DomposableLength(200f))) {
                        DomposableColumn(alignment = DomposableAlignment.Start) {
                            DomposableCard(modifier = DomposableModifier.fillMaxWidth()) {
                                DomposableText("Capped long-text sibling")
                            }
                        }
                    }
                    DomposableText(
                        "A long wrapping text sibling that is deliberately wider than the capped fill card. "
                            .repeat(12),
                    )
                }
                DomposableBox(modifier = DomposableModifier.width(DomposableLength(100f)).testId("narrow-host")) {
                    DomposableColumn(
                        modifier = DomposableModifier.testId("narrow-auto-column"),
                        alignment = DomposableAlignment.Start,
                    ) {
                        DomposableBox(modifier = DomposableModifier.constrainContentWidth(DomposableLength(200f))) {
                            DomposableColumn(alignment = DomposableAlignment.Start) {
                                DomposableCard(
                                    modifier = DomposableModifier.fillMaxWidth().testId("narrow-fill-card"),
                                ) { DomposableText("Narrow fill") }
                            }
                        }
                    }
                }
                DomposableRow(
                    modifier = DomposableModifier.testId("capped-row"),
                    gap = DomposableLength(16f),
                ) {
                    DomposableBox(modifier = DomposableModifier.constrainContentWidth(DomposableLength(100f))) {
                        DomposableColumn(alignment = DomposableAlignment.Start) {
                            DomposableCard(modifier = DomposableModifier.fillMaxWidth()) { DomposableText("A") }
                        }
                    }
                    DomposableBox(modifier = DomposableModifier.constrainContentWidth(DomposableLength(150f))) {
                        DomposableColumn(alignment = DomposableAlignment.Start) {
                            DomposableCard(modifier = DomposableModifier.fillMaxWidth()) { DomposableText("B") }
                        }
                    }
                }
                DomposableRow(
                    modifier =
                        DomposableModifier
                            .constrainContentWidth(
                                DomposableLength(500f),
                            ).testId("capped-max-row"),
                    gap = DomposableLength(16f),
                ) {
                    DomposableBox(modifier = DomposableModifier.constrainContentWidth(DomposableLength(200f))) {
                        DomposableColumn(alignment = DomposableAlignment.Start) {
                            DomposableCard(modifier = DomposableModifier.fillMaxWidth()) { DomposableText("C") }
                        }
                    }
                    DomposableBox(modifier = DomposableModifier.constrainContentWidth(DomposableLength(300f))) {
                        DomposableColumn(alignment = DomposableAlignment.Start) {
                            DomposableCard(modifier = DomposableModifier.fillMaxWidth()) { DomposableText("D") }
                        }
                    }
                }
            }
            DomposableBox(modifier = DomposableModifier.width(DomposableLength(578f)).testId("dynamic-host")) {
                DomposableColumn(
                    modifier = DomposableModifier.testId("dynamic-demand-column"),
                    alignment = DomposableAlignment.Start,
                ) {
                    if (showFill) {
                        DomposableColumn(alignment = DomposableAlignment.Start) {
                            DomposableCard(
                                modifier = DomposableModifier.fillMaxWidth().testId("dynamic-fill-card"),
                            ) { DomposableText("Dynamic fill") }
                        }
                    }
                }
            }
        }
    }
}
