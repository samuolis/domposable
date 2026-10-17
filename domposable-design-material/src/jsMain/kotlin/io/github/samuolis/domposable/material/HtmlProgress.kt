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
import kotlin.math.min

private const val SVG_NAMESPACE = "http://www.w3.org/2000/svg"
private const val PROGRESS_SCALE = 100
private const val DEFAULT_INDICATOR_SIZE = 48f
private const val STANDARD_INDICATOR_SIZE = 40f
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
    if (!props.circular && props.value != null) {
        HtmlExplicitLinearProgress(props, modifier, appearance)
        return
    }
    if (!props.circular) {
        DomposableHtmlModifiers(modifier) {
            TagElement<HTMLElement>("progress", applyAttrs = {
                domposableAttributes(modifier)
                progressSemantics(props, props.semantics == DomposableProgressSemantics.Status)
                classes("d-progress", "d-progress-standard")
                attr("max", "1")
                style {
                    property("width", "100%")
                    appearance.strokeWidth?.let { property("height", "${it.value}px") }
                    appearance.color?.let { property("--d-progress-color", it.css()) }
                    appearance.track?.let { property("--d-progress-track", it.css()) }
                }
            }) {}
        }
        return
    }
    val width = modifierDimension(modifier, width = true) ?: appearance.indicatorSize?.value ?: STANDARD_INDICATOR_SIZE
    val height =
        modifierDimension(modifier, width = false) ?: appearance.indicatorSize?.value ?: STANDARD_INDICATOR_SIZE
    val stroke = appearance.strokeWidth?.value ?: 4f
    val svgElement = remember { mutableStateOf<Element?>(null) }
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            progressSemantics(props, props.semantics == DomposableProgressSemantics.Status)
            classes("d-progress", "d-progress-standard")
            if (props.circular) classes("d-progress-circular")
            style {
                appearance.color?.let { property("--d-progress-color", it.css()) }
                appearance.track?.let { property("--d-progress-track", it.css()) }
                property("--d-progress-stroke", "${stroke}px")
                property("width", if (props.circular) "${width}px" else "100%")
                property("height", if (props.circular) "${height}px" else "${stroke}px")
                if (props.circular) {
                    property("background", "transparent")
                    property("overflow", "visible")
                    if (props.value == null) property("animation", "d-progress-spin 1s linear infinite")
                }
            }
            ref { host ->
                val svg = document.createElementNS(SVG_NAMESPACE, "svg")
                svg.setAttribute("viewBox", "0 0 $width $height")
                svg.setAttribute("width", "100%")
                svg.setAttribute("height", "100%")
                svg.setAttribute("aria-hidden", "true")
                svg.setAttribute("focusable", "false")
                host.appendChild(svg)
                svgElement.value = svg
                onDispose {
                    host.removeChild(svg)
                    svgElement.value = null
                }
            }
        }) {}
    }
    SideEffect {
        svgElement.value?.let { svg ->
            while (svg.firstChild != null) svg.removeChild(svg.firstChild!!)
            if (props.circular) {
                val radius = (min(width, height) - stroke) / 2f
                val circumference = 2f * PI.toFloat() * radius
                val fraction = props.value?.coerceIn(0f, 1f) ?: 0.25f
                val active = fraction * circumference
                val gap = min(active, stroke + 4f)
                appendProgressCircle(
                    svg,
                    width,
                    height,
                    radius,
                    stroke,
                    "d-standard-track",
                    (circumference - active - gap * 2f).coerceAtLeast(0f),
                    -(active + gap),
                )
                appendProgressCircle(svg, width, height, radius, stroke, "d-standard-active", active, 0f)
            }
        }
    }
}

private fun appendProgressCircle(
    svg: Element,
    width: Float,
    height: Float,
    radius: Float,
    stroke: Float,
    cssClass: String,
    length: Float,
    offset: Float,
) {
    document.createElementNS(SVG_NAMESPACE, "circle").also { circle ->
        circle.setAttribute("cx", "${width / 2f}")
        circle.setAttribute("cy", "${height / 2f}")
        circle.setAttribute("r", "$radius")
        circle.setAttribute("class", cssClass)
        circle.setAttribute("fill", "none")
        circle.setAttribute("stroke-width", "$stroke")
        circle.setAttribute("stroke-linecap", "round")
        circle.setAttribute("stroke-dasharray", "$length ${2f * PI.toFloat() * radius}")
        circle.setAttribute("stroke-dashoffset", "$offset")
        circle.setAttribute("transform", "rotate(-90 ${width / 2f} ${height / 2f})")
        if (length == 0f) circle.setAttribute("visibility", "hidden")
        svg.appendChild(circle)
    }
}

@Composable
private fun HtmlExplicitLinearProgress(
    props: DomposableProgressProps,
    modifier: DomposableModifier,
    appearance: DomposableProgressAppearance,
) {
    val progress = props.value!!.coerceIn(0f, 1f)
    val strokeWidth = appearance.strokeWidth?.value ?: 4f
    val stopSize = min(4f, strokeWidth)
    DomposableHtmlModifiers(modifier) {
        Div(attrs = {
            domposableAttributes(modifier)
            progressSemantics(props, props.semantics == DomposableProgressSemantics.Status)
            classes("d-progress", "d-progress-standard")
            style {
                property("position", "relative")
                property("width", "100%")
                property("height", "${strokeWidth}px")
                property("--d-progress-stroke", "${strokeWidth}px")
                property("overflow", "hidden")
                property("border-radius", "999px")
                property("background", "transparent")
                appearance.color?.let { property("--d-progress-color", it.css()) }
                appearance.track?.let { property("--d-progress-track", it.css()) }
            }
        }) {
            Div(attrs = {
                classes("d-progress-linear-track")
                attr("aria-hidden", "true")
                style {
                    property("position", "absolute")
                    property("inset-block", "0")
                    property(
                        "inset-inline-start",
                        "min(100%, calc(${progress * 100f}% + min(${progress * 100f}%, ${strokeWidth + 4f}px)))",
                    )
                    property("inset-inline-end", "0")
                    property("border-radius", "inherit")
                    property("background", "var(--d-progress-track,var(--d-secondary-container))")
                }
            }) {}
            Div(attrs = {
                attr("aria-hidden", "true")
                style {
                    property("position", "absolute")
                    property("inset-block", "0")
                    property("inset-inline-start", "0")
                    property("width", "${progress * 100f}%")
                    property("border-radius", "inherit")
                    property("background-color", "var(--d-progress-color,var(--d-primary))")
                }
            }) {}
            if (appearance.showStopIndicator != false) {
                Div(attrs = {
                    classes("d-progress-stop-indicator")
                    attr("aria-hidden", "true")
                    style {
                        property("position", "absolute")
                        property("inset-inline-end", "0")
                        property("top", "50%")
                        property("width", "${stopSize}px")
                        property("height", "${stopSize}px")
                        property("transform", "translateY(-50%)")
                        property("border-radius", "50%")
                        property("background-color", "var(--d-progress-color,var(--d-primary))")
                    }
                }) {}
            }
        }
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
            ?: if (props.circular) DEFAULT_INDICATOR_SIZE else 10f
    val fillsWidth =
        !props.circular &&
            modifier.elements.any {
                it == DomposableModifierOperation.FillWidth || it is DomposableModifierOperation.FillWidthFraction
            }

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
            val fraction = props.value?.coerceIn(0f, 1f)
            val actualWidth =
                svg.parentElement
                    ?.getBoundingClientRect()
                    ?.width
                    ?.takeIf { it > 0 } ?: width.toDouble()
            if (!circular) svg.setAttribute("viewBox", "0 0 $actualWidth $height")
            val trackPath =
                if (circular) {
                    circularWavePath()
                } else {
                    linearWavePath(
                        actualWidth,
                        height.toDouble(),
                        fraction,
                    )
                }
            val track =
                appendSvgPath(
                    svg,
                    if (circular) {
                        "M92.5,50 A42.5,42.5 0 1,1 7.5,50 A42.5,42.5 0 1,1 92.5,50"
                    } else {
                        "M0,${height / 2f} L$actualWidth,${height / 2f}"
                    },
                    "d-wave-track",
                    PROGRESS_SCALE.toString(),
                )
            val activePath = appendSvgPath(svg, trackPath, "d-wave-active", PROGRESS_SCALE.toString())
            if (circular) {
                // Normalized dash lengths must scale with the circle, unlike linear pixel strokes.
                val stroke = (appearance.strokeWidth?.value ?: 4f) * PROGRESS_SCALE / width
                listOf(track, activePath).forEach {
                    it.setAttribute("style", "vector-effect:none;stroke-width:$stroke")
                }
            }
            if (fraction != null) {
                if (circular) {
                    val gap = 6f
                    track.setAttribute(
                        "stroke-dasharray",
                        "${((1f - fraction) * PROGRESS_SCALE - gap * 2f).coerceAtLeast(0f)} $PROGRESS_SCALE",
                    )
                    track.setAttribute("stroke-dashoffset", "${-(fraction * PROGRESS_SCALE + gap)}")
                    track.setAttribute("transform", "rotate(-90 50 50)")
                } else {
                    track.setAttribute(
                        "style",
                        "clip-path:inset(-50% 0 -50% calc(${fraction * PROGRESS_SCALE}% + 8px))",
                    )
                }
            }
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
