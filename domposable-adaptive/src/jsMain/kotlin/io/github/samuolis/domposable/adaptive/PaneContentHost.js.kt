package io.github.samuolis.domposable.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.ui.DomposableBox
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.htmlAttributes
import io.github.samuolis.domposable.ui.region
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLStyleElement

internal actual fun domposablePaneScaffoldRootModifier(
    modifier: DomposableModifier,
    responsiveBreakpoint: DomposableLength?,
): DomposableModifier =
    if (responsiveBreakpoint != null) {
        modifier.htmlAttributes {
            attr("data-domposable-pane-scaffold", responsiveBreakpoint.value.cssNumber())
            style {
                property("container-type", "inline-size")
                property("container-name", "d-domposable-pane-scaffold")
            }
        }
    } else {
        modifier
    }

@Composable
internal actual fun DomposablePaneContentHost(
    pane: DomposablePaneScaffoldPane,
    expanded: Boolean,
    collapseBelow: DomposableLength?,
    visible: Boolean,
    label: String,
    widthFraction: Float,
    minListWidth: DomposableLength,
    maxListWidth: DomposableLength?,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    if (expanded && collapseBelow != null) InstallPaneScaffoldResponsiveStyles(collapseBelow)
    // Keep responsive sizing on this host so pane descendants never move between modifier wrappers.
    Div(attrs = {
        attr("data-domposable-visibility", "")
        attr("data-domposable-pane-host", pane.name.lowercase())
        if (expanded && collapseBelow != null) attr("data-domposable-pane-responsive", "true")
        if (!visible) attr("aria-hidden", "true")
        style {
            property("box-sizing", "border-box")
            property("min-width", "0")
            property("min-height", "0")
            if (visible) {
                property("display", "grid")
                property("grid-template-columns", "minmax(0,1fr)")
                property("grid-template-rows", "minmax(0,1fr)")
            } else {
                property("display", "none")
            }
            if (expanded) {
                property("align-self", "stretch")
                when (pane) {
                    DomposablePaneScaffoldPane.List -> {
                        property("width", "${widthFraction * 100}%")
                        property("min-width", "min(${minListWidth.value}px, 100%)")
                        maxListWidth?.let { property("max-width", "min(${it.value}px, 100%)") }
                        property("flex", "0 1 auto")
                    }

                    DomposablePaneScaffoldPane.Detail -> {
                        property("flex", "1 1 0%")
                        property("min-width", "0")
                        property("min-height", "0")
                    }
                }
            } else {
                property("width", "100%")
                property("flex", "0 1 auto")
            }
        }
    }) {
        DomposableBox(
            modifier = DomposableModifier.fillMaxHeight().region(label).then(modifier),
            content = content,
        )
    }
}

private val paneScaffoldResponsiveStyles = mutableMapOf<String, HTMLStyleElement>()
private val paneScaffoldResponsiveStyleUsersByBreakpoint = mutableMapOf<String, Int>()

@Composable
private fun InstallPaneScaffoldResponsiveStyles(collapseBelow: DomposableLength) {
    val threshold = collapseBelow.value.cssNumber()
    DisposableEffect(threshold) {
        val style =
            paneScaffoldResponsiveStyles[threshold] ?: (document.createElement("style") as HTMLStyleElement).also {
                it.textContent = paneScaffoldResponsiveCss(threshold)
                document.head!!.appendChild(it)
                paneScaffoldResponsiveStyles[threshold] = it
            }
        paneScaffoldResponsiveStyleUsersByBreakpoint[threshold] =
            (paneScaffoldResponsiveStyleUsersByBreakpoint[threshold] ?: 0) + 1
        onDispose {
            val users = paneScaffoldResponsiveStyleUsersByBreakpoint.getValue(threshold) - 1
            if (users == 0) {
                style.remove()
                paneScaffoldResponsiveStyles.remove(threshold)
                paneScaffoldResponsiveStyleUsersByBreakpoint.remove(threshold)
            } else {
                paneScaffoldResponsiveStyleUsersByBreakpoint[threshold] = users
            }
        }
    }
}

private fun paneScaffoldResponsiveCss(threshold: String): String =
    """
    @container d-domposable-pane-scaffold (width < ${threshold}px){
        [data-domposable-pane-scaffold="$threshold"]>[data-domposable-pane-responsive="true"][data-domposable-pane-host="list"]{display:none!important}
        [data-domposable-pane-scaffold="$threshold"]>[data-domposable-pane-responsive="true"][data-domposable-pane-host="detail"]{width:100%!important;flex:0 1 auto!important}
    }
    """.trimIndent()

private fun Float.cssNumber(): String = if (this % 1f == 0f) toLong().toString() else toString()
