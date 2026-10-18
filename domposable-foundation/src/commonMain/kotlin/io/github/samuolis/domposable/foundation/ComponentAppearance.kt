package io.github.samuolis.domposable.foundation

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline

/**
 * Neutral text styling shared by native Material and semantic HTML renderers.
 * Sizes use sp on native and px on HTML.
 */
@Immutable
data class DomposableTextAppearance(
    val fontSize: Float? = null,
    val lineHeight: Float? = null,
    val fontWeight: DomposableFontWeight? = null,
    val alignment: DomposableTextAlignment? = null,
    val decoration: DomposableTextDecoration? = null,
    val maxLines: Int? = null,
    val overflow: DomposableTextOverflow = DomposableTextOverflow.Clip,
    /** Null preserves each renderer's existing selection behavior. */
    val selectable: Boolean? = null,
    val liveRegion: DomposableLiveRegion? = null,
    val letterSpacing: Float? = null,
    val role: DomposableTypographyRole? = null,
    val emphasized: Boolean = false,
    val color: DomposableColor? = null,
    val minLines: Int = 1,
) {
    init {
        require(fontSize == null || (fontSize.isFinite() && fontSize > 0f))
        require(lineHeight == null || (lineHeight.isFinite() && lineHeight > 0f))
        require(minLines > 0)
        require(maxLines == null || maxLines > 0)
        require(maxLines == null || minLines <= maxLines)
        require(letterSpacing == null || letterSpacing.isFinite())
    }
}

/** A variable-font weight, matching the 1–1000 range accepted by native and HTML text. */
@Immutable
@JvmInline
value class DomposableFontWeight(
    val weight: Int,
) {
    init {
        require(weight in 1..1000) { "Font weight must be between 1 and 1000" }
    }

    companion object {
        val Thin = DomposableFontWeight(100)
        val ExtraLight = DomposableFontWeight(200)
        val Light = DomposableFontWeight(300)
        val Normal = DomposableFontWeight(400)
        val Medium = DomposableFontWeight(500)
        val SemiBold = DomposableFontWeight(600)
        val Bold = DomposableFontWeight(700)
        val ExtraBold = DomposableFontWeight(800)
        val Black = DomposableFontWeight(900)
    }
}

enum class DomposableTextAlignment {
    Start,
    Center,
    End,
    Justify,
}

enum class DomposableTextDecoration {
    None,
    Underline,
    LineThrough,
}

enum class DomposableTextOverflow {
    Clip,
    Ellipsis,
    Visible,
}

enum class DomposableLiveRegion {
    Polite,
    Assertive,
}

/** Browser autocomplete hints with consistent names across platforms. */
enum class DomposableAutocomplete {
    Off,
    Email,
    Phone,
    Username,
    CurrentPassword,
    NewPassword,
    OneTimeCode,
}

/** The action shown by virtual keyboards for a text input. */
enum class DomposableImeAction {
    Default,
    Done,
    Go,
    Next,
    Previous,
    Search,
    Send,
}
