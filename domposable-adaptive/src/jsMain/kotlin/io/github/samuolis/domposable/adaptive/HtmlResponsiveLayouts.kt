package io.github.samuolis.domposable.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import io.github.samuolis.domposable.foundation.DomposableLength
import kotlinx.browser.document
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLStyleElement

/** Responsive CSS grid for exported HTML. The browser owns every width decision. */
@Composable
fun DomposableHtmlResponsiveGrid(
    columns: DomposableResponsive<Int>,
    gap: DomposableLength,
    rootAttrs: AttrsScope<HTMLElement>.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    require(columns.compact > 0 && columns.medium > 0 && columns.expanded > 0 && columns.large > 0) {
        "Responsive grid column counts must be positive"
    }
    InstallResponsiveLayoutStyles()
    val classColumns = listOf(columns.compact, columns.medium, columns.expanded, columns.large)
    Div(attrs = {
        attr("data-d-html-responsive-grid-root", "")
        style {
            property("width", "100%")
            property("min-width", "0")
            property("container-type", "inline-size")
        }
    }) {
        Div(attrs = {
            style {
                property("--d-responsive-columns-compact", classColumns[0].toString())
                property("--d-responsive-columns-medium", classColumns[1].toString())
                property("--d-responsive-columns-expanded", classColumns[2].toString())
                property("--d-responsive-columns-large", classColumns[3].toString())
                property("--d-responsive-grid-gap", gap.cssPx())
            }
            rootAttrs()
            attr("data-d-html-responsive-grid", "")
        }) { content() }
    }
}

/** A stack-on-small, two-column HTML layout with optional bounded media matching. */
@Composable
fun DomposableHtmlResponsiveSplit(
    breakpoint: DomposableLength = DomposableLength(DomposableBreakpoints().medium),
    gap: DomposableLength = DomposableLength(0f),
    mediaWidth: DomposableLength? = null,
    mediaFirst: Boolean = false,
    mediaMatchesText: Boolean = false,
    mediaMinHeight: DomposableLength? = null,
    mediaMaxHeight: DomposableLength? = null,
    rootAttrs: AttrsScope<HTMLElement>.() -> Unit = {},
    mediaAttrs: AttrsScope<HTMLElement>.() -> Unit = {},
    textAttrs: AttrsScope<HTMLElement>.() -> Unit = {},
    mediaContent: @Composable () -> Unit,
    textContent: @Composable () -> Unit,
) {
    require(mediaMinHeight == null || mediaMaxHeight == null || mediaMinHeight.value <= mediaMaxHeight.value) {
        "Responsive split media minimum height must not exceed its maximum height"
    }
    InstallResponsiveLayoutStyles()
    InstallResponsiveSplitBreakpoint(breakpoint)
    val threshold = breakpoint.value.cssNumber()
    Div(attrs = {
        attr("data-d-html-responsive-split", threshold)
        attr("data-d-html-responsive-split-media-first", mediaFirst.toString())
        attr("data-d-html-responsive-split-media-fixed-width", (mediaWidth != null).toString())
        attr("data-d-html-responsive-split-media-matches-text", mediaMatchesText.toString())
        style {
            property("width", "100%")
            property("min-width", "0")
            property("container-type", "inline-size")
            property("container-name", "d-responsive-split")
        }
    }) {
        Div(attrs = {
            style {
                property("--d-split-gap", gap.cssPx())
                mediaWidth?.let { property("--d-split-media-width", it.cssPx()) }
            }
            rootAttrs()
            attr("data-d-html-responsive-split-layout", "")
        }) {
            Div(attrs = {
                mediaAttrs()
                attr("data-d-html-responsive-split-media", "")
                style {
                    property("--d-split-media-min-height", mediaMinHeight?.cssPx() ?: "0px")
                    property("--d-split-media-max-height", mediaMaxHeight?.cssPx() ?: "none")
                }
            }) {
                Div(attrs = { attr("data-d-html-responsive-split-media-content", "") }) { mediaContent() }
            }
            Div(attrs = {
                textAttrs()
                attr("data-d-html-responsive-split-text", "")
            }) { textContent() }
        }
    }
}

/** Responsive-height wrapper whose height can inherit a matched split's bounded media height. */
@Composable
fun DomposableHtmlResponsiveHeight(
    heights: DomposableResponsive<DomposableLength>,
    useViewport: Boolean = false,
    rootAttrs: AttrsScope<HTMLElement>.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    InstallResponsiveLayoutStyles()
    Div(attrs = {
        attr("data-d-html-responsive-height-root", "")
        style {
            property("width", "100%")
            property("min-width", "0")
            property("container-type", "inline-size")
        }
    }) {
        Div(attrs = {
            style {
                property("--d-height-compact", heights.compact.cssPx())
                property("--d-height-medium", heights.medium.cssPx())
                property("--d-height-expanded", heights.expanded.cssPx())
                property("--d-height-large", heights.large.cssPx())
            }
            rootAttrs()
            attr("data-d-html-responsive-height", "")
            attr("data-d-html-responsive-height-viewport", useViewport.toString())
        }) { content() }
    }
}

@Composable
private fun InstallResponsiveLayoutStyles() {
    DisposableEffect(Unit) {
        val style =
            responsiveLayoutStyle ?: (document.createElement("style") as HTMLStyleElement).also {
                it.textContent = responsiveLayoutCss()
                document.head!!.appendChild(it)
                responsiveLayoutStyle = it
            }
        responsiveLayoutStyleUsers++
        onDispose {
            responsiveLayoutStyleUsers--
            if (responsiveLayoutStyleUsers == 0) {
                style.remove()
                responsiveLayoutStyle = null
            }
        }
    }
}

@Composable
private fun InstallResponsiveSplitBreakpoint(breakpoint: DomposableLength) {
    val threshold = breakpoint.value.cssNumber()
    DisposableEffect(threshold) {
        val style =
            responsiveSplitStyles[threshold]
                ?: (document.createElement("style") as HTMLStyleElement).also {
                    it.textContent = responsiveSplitCss(threshold)
                    document.head!!.appendChild(it)
                    responsiveSplitStyles[threshold] = it
                }
        responsiveSplitStyleUsers[threshold] = (responsiveSplitStyleUsers[threshold] ?: 0) + 1
        onDispose {
            val users = responsiveSplitStyleUsers.getValue(threshold) - 1
            if (users == 0) {
                style.remove()
                responsiveSplitStyles.remove(threshold)
                responsiveSplitStyleUsers.remove(threshold)
            } else {
                responsiveSplitStyleUsers[threshold] = users
            }
        }
    }
}

private var responsiveLayoutStyle: HTMLStyleElement? = null
private var responsiveLayoutStyleUsers = 0
private val responsiveSplitStyles = mutableMapOf<String, HTMLStyleElement>()
private val responsiveSplitStyleUsers = mutableMapOf<String, Int>()

private fun responsiveLayoutCss(): String {
    val breakpoints = DomposableBreakpoints()
    val medium = "${breakpoints.medium.cssNumber()}px"
    val expanded = "${breakpoints.expanded.cssNumber()}px"
    val large = "${breakpoints.large.cssNumber()}px"
    return """
        [data-d-html-responsive-grid-root],[data-d-html-responsive-height-root],[data-d-html-responsive-split]{box-sizing:border-box}
        [data-d-html-responsive-grid-root]>[data-d-html-responsive-grid]{display:grid;min-width:0;grid-template-columns:repeat(var(--d-responsive-columns),minmax(0,1fr));gap:var(--d-responsive-grid-gap)}
        [data-d-html-responsive-grid-root]>[data-d-html-responsive-grid]{--d-responsive-columns:var(--d-responsive-columns-compact)}
        [data-d-html-responsive-height-root]{height:var(--d-split-media-height,auto);max-height:var(--d-split-media-max-height,none)}
        [data-d-html-responsive-height-root]>[data-d-html-responsive-height]{height:var(--d-split-media-height,var(--d-responsive-height));min-width:0}
        [data-d-html-responsive-height-root]>[data-d-html-responsive-height]{--d-responsive-height:var(--d-height-compact)}
        [data-d-html-responsive-split-layout]{display:flex;min-width:0;flex-direction:column;gap:var(--d-split-gap,0px)}
        [data-d-html-responsive-split-media],[data-d-html-responsive-split-text]{flex:1 1 0%;min-width:0}
        [data-d-html-responsive-split-media-content]{width:100%;min-width:0}
        [data-d-html-responsive-split-media]{order:1}
        [data-d-html-responsive-split-text]{order:0}
        [data-d-html-responsive-split][data-d-html-responsive-split-media-first=true]>[data-d-html-responsive-split-layout]>[data-d-html-responsive-split-media]{order:0}
        [data-d-html-responsive-split][data-d-html-responsive-split-media-first=true]>[data-d-html-responsive-split-layout]>[data-d-html-responsive-split-text]{order:1}
        @container (min-width:$medium){[data-d-html-responsive-grid-root]>[data-d-html-responsive-grid]{--d-responsive-columns:var(--d-responsive-columns-medium)}[data-d-html-responsive-height-root]>[data-d-html-responsive-height][data-d-html-responsive-height-viewport=false]{--d-responsive-height:var(--d-height-medium)}}
        @container (min-width:$expanded){[data-d-html-responsive-grid-root]>[data-d-html-responsive-grid]{--d-responsive-columns:var(--d-responsive-columns-expanded)}[data-d-html-responsive-height-root]>[data-d-html-responsive-height][data-d-html-responsive-height-viewport=false]{--d-responsive-height:var(--d-height-expanded)}}
        @container (min-width:$large){[data-d-html-responsive-grid-root]>[data-d-html-responsive-grid]{--d-responsive-columns:var(--d-responsive-columns-large)}[data-d-html-responsive-height-root]>[data-d-html-responsive-height][data-d-html-responsive-height-viewport=false]{--d-responsive-height:var(--d-height-large)}}
        @media (min-width:$medium){[data-d-html-responsive-height-root]>[data-d-html-responsive-height][data-d-html-responsive-height-viewport=true]{--d-responsive-height:var(--d-height-medium)}}
        @media (min-width:$expanded){[data-d-html-responsive-height-root]>[data-d-html-responsive-height][data-d-html-responsive-height-viewport=true]{--d-responsive-height:var(--d-height-expanded)}}
        @media (min-width:$large){[data-d-html-responsive-height-root]>[data-d-html-responsive-height][data-d-html-responsive-height-viewport=true]{--d-responsive-height:var(--d-height-large)}}
        """.trimIndent()
}

private fun responsiveSplitCss(threshold: String): String =
    """
    @container d-responsive-split (min-width:${threshold}px){
        [data-d-html-responsive-split="$threshold"]>[data-d-html-responsive-split-layout]{flex-direction:row}
        [data-d-html-responsive-split="$threshold"][data-d-html-responsive-split-media-fixed-width=true]>[data-d-html-responsive-split-layout]>[data-d-html-responsive-split-media]{flex:0 0 min(var(--d-split-media-width),100%)}
        [data-d-html-responsive-split="$threshold"][data-d-html-responsive-split-media-matches-text=true]>[data-d-html-responsive-split-layout]>[data-d-html-responsive-split-media]{--d-split-media-height:100%;contain:size;container-name:d-responsive-split-media;container-type:size;align-self:stretch;display:flex;align-items:center;min-height:var(--d-split-media-min-height)}
        [data-d-html-responsive-split="$threshold"][data-d-html-responsive-split-media-matches-text=true]>[data-d-html-responsive-split-layout]>[data-d-html-responsive-split-media]>[data-d-html-responsive-split-media-content]{flex:1 1 0%;align-self:center;height:100%;min-height:0;max-height:var(--d-split-media-max-height);display:flex;justify-content:center;align-items:center}
    }
    """.trimIndent()

private fun DomposableLength.cssPx(): String = "${value.cssNumber()}px"

private fun Float.cssNumber(): String = if (this % 1f == 0f) toLong().toString() else toString()
