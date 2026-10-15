package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableProgressAppearance
import io.github.samuolis.domposable.foundation.DomposableProgressSemantics
import io.github.samuolis.domposable.foundation.DomposableProgressVariant
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableLoadingIndicatorProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableModifierOperation
import io.github.samuolis.domposable.ui.DomposableProgressProps
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.domposableAttributes
import kotlinx.browser.document
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.TagElement
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.math.PI

private const val SVG_NAMESPACE = "http://www.w3.org/2000/svg"
private const val PROGRESS_SCALE = 100
private const val DEFAULT_INDICATOR_SIZE = 48f
private const val DEFAULT_WAVE_SPEED = 120f
private const val HALF_ROTATION_DEGREES = 180f

@Composable
internal fun HtmlProgress(
    props: DomposableProgressProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.progress
    if (props.variant == DomposableProgressVariant.Wavy) {
        HtmlWavyProgress(props, modifier, appearance)
    } else {
        HtmlStandardProgress(props, modifier, appearance)
    }
}

@Composable
private fun HtmlStandardProgress(
    props: DomposableProgressProps,
    modifier: DomposableModifier,
    appearance: DomposableProgressAppearance,
) {
    val width =
        modifierDimension(modifier, width = true)
            ?: appearance.indicatorSize?.value
            ?: DEFAULT_INDICATOR_SIZE
    val height =
        modifierDimension(modifier, width = false)
            ?: appearance.indicatorSize?.value
            ?: DEFAULT_INDICATOR_SIZE
    val status = props.semantics == DomposableProgressSemantics.Status
    DomposableHtmlModifiers(modifier) {
        TagElement<HTMLElement>(if (status) "div" else "progress", applyAttrs = {
            domposableAttributes(modifier)
            progressSemantics(props, status)
            if (!status) {
                attr("max", "1")
                props.value?.let { attr("value", it.toString()) }
            }
            classes("d-progress", "d-progress-standard")
            if (props.circular) classes("d-progress-circular")
            style {
                appearance.color?.let { property("accent-color", it.css()) }
                appearance.color?.let { property("--d-progress-color", it.css()) }
                appearance.track?.let { property("--d-progress-track", it.css()) }
                property("--d-progress-stroke", "${appearance.strokeWidth?.value ?: 4f}px")
                if (props.circular) {
                    property("width", "${width}px")
                    property("height", "${height}px")
                    property("appearance", "none")
                    property("border", "0")
                    property("border-radius", "50%")
                    property("background", progressRing(props.value))
                    val mask = progressMask(appearance.strokeWidth?.value ?: 4f)
                    property("-webkit-mask", mask)
                    property("mask", mask)
                    if (props.value == null) property("animation", "d-progress-spin 1s linear infinite")
                } else {
                    property("width", "100%")
                    property("height", "${appearance.strokeWidth?.value ?: 4f}px")
                }
            }
        }) {}
    }
}

@Composable
private fun HtmlWavyProgress(
    props: DomposableProgressProps,
    modifier: DomposableModifier,
    appearance: DomposableProgressAppearance,
) {
    val svgElement = remember { mutableStateOf<Element?>(null) }
    val width =
        modifierDimension(modifier, width = true)
            ?: appearance.indicatorSize?.value
            ?: if (props.circular) DEFAULT_INDICATOR_SIZE else 240f
    val height =
        modifierDimension(modifier, width = false)
            ?: appearance.indicatorSize?.value
            ?: if (props.circular) DEFAULT_INDICATOR_SIZE else 12f
    val fillsWidth =
        !props.circular && modifier.elements.any { it == DomposableModifierOperation.FillWidth }

    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            progressSemantics(props, props.semantics == DomposableProgressSemantics.Status)
            classes(
                "d-progress",
                "d-progress-wavy",
                if (props.circular) "d-progress-wavy-circular" else "d-progress-wavy-linear",
            )
            if (appearance.waveSpeed?.value == 0f) classes("d-wave-static")
            style {
                if (!fillsWidth) property("width", "${width}px")
                property("height", "${height}px")
                appearance.color?.let { property("--d-progress-color", it.css()) }
                appearance.track?.let { property("--d-progress-track", it.css()) }
                appearance.strokeWidth?.let { property("--d-progress-stroke", "${it.value}px") }
                waveDuration(appearance.waveSpeed, width, props.circular, props.value == null)?.let {
                    property("--d-wave-duration", it)
                }
            }
            ref { host ->
                val svg = document.createElementNS(SVG_NAMESPACE, "svg")
                svg.setAttribute("viewBox", if (props.circular) "0 0 100 100" else "0 0 100 12")
                if (!props.circular) svg.setAttribute("preserveAspectRatio", "none")
                svg.setAttribute("width", "100%")
                svg.setAttribute("height", "100%")
                svg.setAttribute("aria-hidden", "true")
                svg.setAttribute("focusable", "false")
                host.appendChild(svg)
                svgElement.value = svg
                onDispose {
                    host.removeChild(svg)
                    if (svgElement.value == svg) svgElement.value = null
                }
            }
        }) {}
    }
    SideEffect {
        svgElement.value?.let { svg ->
            while (svg.firstChild != null) svg.removeChild(svg.firstChild!!)
            val circular = props.circular
            val trackPath = if (circular) circularWavePath() else linearWavePath()
            appendSvgPath(svg, if (circular) trackPath else "M0,6 L100,6", "d-wave-track", PROGRESS_SCALE.toString())
            val activePath = appendSvgPath(svg, trackPath, "d-wave-active", PROGRESS_SCALE.toString())
            val fraction = props.value?.coerceIn(0f, 1f)
            if (fraction == null) {
                activePath.setAttribute("class", "d-wave-active d-wave-indeterminate")
            } else if (circular) {
                activePath.setAttribute("stroke-dasharray", "${fraction * PROGRESS_SCALE} $PROGRESS_SCALE")
                activePath.setAttribute("stroke-dashoffset", "0")
            } else {
                activePath.setAttribute("style", "clip-path:inset(-50% ${(1f - fraction) * PROGRESS_SCALE}% -50% 0)")
            }
        }
    }
}

@Composable
internal fun HtmlLoadingIndicator(
    props: DomposableLoadingIndicatorProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.loadingIndicator
    val progress = props.progress
    val svgElement = remember { mutableStateOf<Element?>(null) }
    val width =
        modifierDimension(modifier, width = true)
            ?: appearance.indicatorSize?.value
            ?: DEFAULT_INDICATOR_SIZE
    val height =
        modifierDimension(modifier, width = false)
            ?: appearance.indicatorSize?.value
            ?: DEFAULT_INDICATOR_SIZE

    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            classes("d-loading-indicator", if (props.contained) "d-loading-contained" else "d-loading-uncontained")
            attr("role", "progressbar")
            attr("aria-label", props.label)
            progress?.let {
                attr("aria-valuemin", "0")
                attr("aria-valuemax", "100")
                attr("aria-valuenow", (it * PROGRESS_SCALE).toInt().toString())
            } ?: attr("aria-valuetext", props.label)
            style {
                property("width", "${width}px")
                property("height", "${height}px")
                property("min-width", "24px")
                property("min-height", "24px")
                appearance.color?.let { property("--d-progress-color", it.css()) }
                appearance.track?.let { property("--d-progress-track", it.css()) }
                appearance.strokeWidth?.let { property("--d-progress-stroke", "${it.value}px") }
                props.containerColor?.let { property("--d-loading-container", it.css()) }
            }
            ref { host ->
                val svg = document.createElementNS(SVG_NAMESPACE, "svg")
                svg.setAttribute("viewBox", "0 0 100 100")
                svg.setAttribute("width", "100%")
                svg.setAttribute("height", "100%")
                svg.setAttribute("aria-hidden", "true")
                svg.setAttribute("focusable", "false")
                host.appendChild(svg)
                svgElement.value = svg
                onDispose {
                    host.removeChild(svg)
                    if (svgElement.value == svg) svgElement.value = null
                }
            }
        }) {}
    }
    SideEffect {
        svgElement.value?.let { svg ->
            while (svg.firstChild != null) svg.removeChild(svg.firstChild!!)
            if (props.contained) appendSvgCircle(svg, "d-loading-container-shape")
            if (progress == null) {
                appendLoadingMorph(svg, props.contained)
            } else {
                appendDeterminateLoadingShape(svg, progress)
            }
        }
    }
}

private fun AttrsScope<HTMLElement>.progressSemantics(
    props: DomposableProgressProps,
    status: Boolean,
) {
    props.label?.takeIf(String::isNotEmpty)?.let { attr("aria-label", it) }
    if (status) {
        attr("role", "status")
        props.liveRegion?.let { attr("aria-live", it.name.lowercase()) }
    } else {
        attr("role", "progressbar")
        attr("aria-valuemin", "0")
        attr("aria-valuemax", "100")
        props.value?.let { attr("aria-valuenow", (it * PROGRESS_SCALE).toInt().toString()) }
        if (props.value == null) attr("aria-valuetext", props.label ?: "Loading")
    }
}

private fun modifierDimension(
    modifier: DomposableModifier,
    width: Boolean,
): Float? =
    if (width) {
        modifier.elements
            .filterIsInstance<DomposableModifierOperation.Width>()
            .lastOrNull()
            ?.length
            ?.value
    } else {
        modifier.elements
            .filterIsInstance<DomposableModifierOperation.Height>()
            .lastOrNull()
            ?.length
            ?.value
    }

private fun waveDuration(
    speed: DomposableLength?,
    width: Float,
    circular: Boolean,
    animated: Boolean,
): String? {
    if (!animated || speed?.value == 0f) return null
    val pixelsPerSecond = speed?.value ?: DEFAULT_WAVE_SPEED
    val distance = if (circular) width * PI.toFloat() else width
    val seconds = (distance / pixelsPerSecond).coerceAtLeast(0.1f)
    return "${seconds}s"
}

private fun appendSvgPath(
    parent: Element,
    pathData: String,
    cssClass: String,
    pathLength: String,
): Element =
    document.createElementNS(SVG_NAMESPACE, "path").also { path ->
        path.setAttribute("d", pathData)
        path.setAttribute("class", cssClass)
        path.setAttribute("pathLength", pathLength)
        parent.appendChild(path)
    }

private fun appendSvgCircle(
    parent: Element,
    cssClass: String,
) {
    document.createElementNS(SVG_NAMESPACE, "circle").also { circle ->
        circle.setAttribute("cx", "50")
        circle.setAttribute("cy", "50")
        circle.setAttribute("r", "45")
        circle.setAttribute("class", cssClass)
        parent.appendChild(circle)
    }
}

private fun appendLoadingMorph(
    parent: Element,
    contained: Boolean,
) {
    val fill = if (contained) "d-loading-morph-contained" else "d-loading-morph-outline"
    val paths =
        listOf(
            "M50 9 C73 9 91 27 91 50 C91 73 73 91 50 91 C27 91 9 73 9 50 C9 27 27 9 50 9Z",
            "M50 10 C55 10 58 17 62 20 C67 24 77 20 81 25 C86 30 80 39 82 45 " +
                "C84 51 92 55 90 62 C88 69 77 70 73 76 C69 82 72 91 66 94 " +
                "C60 97 53 88 47 88 C41 88 34 96 28 93 C22 90 25 80 20 75 " +
                "C15 70 5 70 4 63 C3 56 12 51 13 45 C14 39 8 30 13 25 " +
                "C18 20 27 24 33 20 C39 16 42 10 50 10Z",
            "M50 7 L60 20 L77 15 L79 33 L95 43 L83 56 L88 74 L69 75 " +
                "L59 92 L47 79 L29 86 L27 67 L10 58 L22 44 L16 26 L35 26Z",
        )
    paths.forEachIndexed { index, pathData ->
        document.createElementNS(SVG_NAMESPACE, "path").also { path ->
            path.setAttribute("d", pathData)
            path.setAttribute("class", "$fill d-loading-morph-shape d-loading-morph-$index")
            parent.appendChild(path)
        }
    }
}

private fun appendDeterminateLoadingShape(
    parent: Element,
    progress: Float,
) {
    val shape = document.createElementNS(SVG_NAMESPACE, "path")
    val boundedProgress = progress.coerceIn(0f, 1f)
    shape.setAttribute("class", "d-loading-determinate")
    shape.setAttribute("d", determinateLoadingPath(boundedProgress))
    shape.setAttribute("data-progress", boundedProgress.toString())
    val rotationDegrees = -boundedProgress * HALF_ROTATION_DEGREES
    shape.setAttribute("transform", "rotate($rotationDegrees 50 50)")
    parent.appendChild(shape)
}
