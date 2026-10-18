package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextStyle

/** An inline run; unspecified values inherit the surrounding text's theme and appearance. */
data class DomposableTextRun(
    val text: String,
    val fontWeight: DomposableFontWeight? = null,
    val color: DomposableColor? = null,
    val contentAlpha: Float = 1f,
) {
    init {
        require(contentAlpha.isFinite() && contentAlpha in 0f..1f)
    }
}

/** One wrapping, selectable text value with individually styled runs on every renderer. */
@Composable
fun DomposableText(
    runs: List<DomposableTextRun>,
    modifier: DomposableModifier = DomposableModifier,
    style: DomposableTextStyle = DomposableTextStyle.Body,
    color: DomposableColor? = null,
    headingLevel: Int? = null,
    appearance: DomposableTextAppearance? = null,
) = DomposableText(
    DomposableTextProps(runs.joinToString("") { it.text }, style, color, headingLevel, appearance, runs),
    modifier,
)
