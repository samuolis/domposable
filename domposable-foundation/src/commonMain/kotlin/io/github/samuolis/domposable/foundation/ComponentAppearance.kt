package io.github.samuolis.domposable.foundation

import androidx.compose.runtime.Immutable

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
) {
    init {
        require(fontSize == null || (fontSize.isFinite() && fontSize > 0f))
        require(lineHeight == null || (lineHeight.isFinite() && lineHeight > 0f))
        require(maxLines == null || maxLines > 0)
        require(letterSpacing == null || letterSpacing.isFinite())
    }
}

enum class DomposableFontWeight {
    Thin,
    ExtraLight,
    Light,
    Normal,
    Medium,
    SemiBold,
    Bold,
    ExtraBold,
    Black,
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
