package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.dp
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class HtmlRawContentBoundsTest {
    @Test
    fun sizedModifiersStretchRawControlsThroughPaddingAndSemanticWrappers() {
        val cases =
            listOf(
                "fill" to (DomposableModifier.fillMaxWidth() to 368.0),
                "prefix" to (DomposableModifier.testId("prefix").padding(8.dp).fillMaxWidth() to 352.0),
                "wrapped-prefix" to (
                    DomposableModifier
                        .testId(
                            "wrap-prefix",
                        ).wrapContentSize()
                        .fillMaxWidth() to 368.0
                ),
                "fixed" to (DomposableModifier.width(120.dp).fillMaxWidth() to 88.0),
                "minimum" to (DomposableModifier.minWidth(120.dp) to 88.0),
                "fraction" to (DomposableModifier.fillMaxWidth(0.5f) to 168.0),
            )
        val root = document.createElement("div")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                cases.forEach { (name, sizing) ->
                    Div(attrs = {
                        style {
                            property("width", "400px")
                            property("display", "flex")
                            property("flex-direction", "column")
                            property("align-items", "flex-start")
                        }
                    }) {
                        DomposableHtmlModifiers(
                            modifier =
                                sizing.first
                                    .padding(16.dp)
                                    .testId(name)
                                    .contentDescription("Action")
                                    .htmlAttributes { attr("data-raw-wrapper", name) },
                            wrapSemantics = true,
                        ) {
                            Button(attrs = {
                                attr("data-raw-button", name)
                                style { property("display", "inline-flex") }
                            }) { Text("Apply") }
                        }
                    }
                }
            }
        try {
            cases.forEach { (name, sizing) ->
                val button = root.querySelector("[data-raw-button='$name']") as HTMLElement
                val actual = button.getBoundingClientRect().width
                assertTrue(abs(actual - sizing.second) < 1, "$name: expected ${sizing.second}px, got ${actual}px")
            }
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun decorationBeforeFillWidthReceivesTheAvailableConstraint() {
        val root = document.createElement("div") as HTMLElement
        root.style.setProperty("width", "400px")
        root.style.setProperty("display", "flex")
        root.style.setProperty("flex-direction", "column")
        root.style.setProperty("align-items", "flex-start")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableHtmlModifiers(
                    modifier = DomposableModifier.then(RawSizingDecoration).fillMaxWidth().padding(16.dp),
                    wrapSemantics = true,
                    decorationRenderer = { element ->
                        if (element == RawSizingDecoration) {
                            { content ->
                                Div(attrs = {
                                    style {
                                        property("box-sizing", "border-box")
                                        property("position", "relative")
                                        property("padding", "8px")
                                    }
                                }) { content() }
                            }
                        } else {
                            null
                        }
                    },
                ) {
                    Button { Text("Apply") }
                }
            }
        try {
            val width = (root.querySelector("button") as HTMLElement).getBoundingClientRect().width
            assertTrue(abs(width - 352.0) < 1, "A decorated full-width button must fill its inset area, got ${width}px")
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun minimumHeightReachesAnEmptyFillSizeChild() {
        val root = document.createElement("div") as HTMLElement
        root.style.setProperty("width", "400px")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableHtmlModifiers(DomposableModifier.fillMaxWidth().minHeight(200.dp), wrapSemantics = true) {
                    DomposableHtmlModifiers(DomposableModifier.fillMaxSize()) {
                        Div(attrs = {
                            attr("data-empty-fill", "true")
                            style {
                                property("width", "100%")
                                property("height", "100%")
                            }
                        }) { }
                    }
                }
            }
        try {
            val bounds = (root.querySelector("[data-empty-fill]") as HTMLElement).getBoundingClientRect()
            assertTrue(
                abs(bounds.height - 200.0) < 1,
                "Minimum height must reach empty content, got ${bounds.height}px",
            )
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun wrapContentReleasesTheWidthConstraintAndCentersTheControl() {
        val root = document.createElement("div") as HTMLElement
        root.style.setProperty("width", "400px")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableHtmlModifiers(
                    DomposableModifier.fillMaxWidth().wrapContentSize().testId("wrapped"),
                    wrapSemantics = true,
                ) {
                    Button { Text("Apply") }
                }
            }
        try {
            val bounds = (root.querySelector("button") as HTMLElement).getBoundingClientRect()
            assertTrue(bounds.width > 0 && bounds.width < 100, "Wrapped content must stay intrinsic")
            val expectedLeft = root.getBoundingClientRect().left + (400 - bounds.width) / 2
            assertTrue(abs(bounds.left - expectedLeft) < 1, "Wrapped content must remain centered")
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }

    @Test
    fun rawControlsWithoutSizingKeepTheirIntrinsicWidth() {
        val root = document.createElement("div") as HTMLElement
        root.style.setProperty("width", "400px")
        root.style.setProperty("display", "flex")
        root.style.setProperty("align-items", "flex-start")
        document.body!!.appendChild(root)
        val composition =
            renderComposable(root) {
                DomposableHtmlModifiers(DomposableModifier.testId("intrinsic"), wrapSemantics = true) {
                    Button { Text("Apply") }
                }
            }
        try {
            val width = (root.querySelector("button") as HTMLElement).getBoundingClientRect().width
            assertTrue(width > 0 && width < 100, "An unsized button must remain intrinsic, got ${width}px")
        } finally {
            composition.dispose()
            root.parentNode?.removeChild(root)
        }
    }
}

private data object RawSizingDecoration : DomposableModifierElement
