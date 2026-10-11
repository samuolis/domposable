package io.github.samuolis.domposable.adaptive

import androidx.compose.runtime.*
import io.github.samuolis.domposable.foundation.DomposableSize

enum class DomposableWidthClass { Compact, Medium, Expanded, Large }

data class DomposableBreakpoints(val medium: Float = 600f, val expanded: Float = 840f, val large: Float = 1200f) {
    init { require(medium > 0 && expanded > medium && large > expanded) }
    fun classify(width: Float): DomposableWidthClass {
        require(width.isFinite() && width >= 0)
        return when {
            width >= large -> DomposableWidthClass.Large
            width >= expanded -> DomposableWidthClass.Expanded
            width >= medium -> DomposableWidthClass.Medium
            else -> DomposableWidthClass.Compact
        }
    }
}

data class DomposableResponsive<T>(val compact: T, val medium: T = compact, val expanded: T = medium, val large: T = expanded) {
    fun resolve(widthClass: DomposableWidthClass): T = when (widthClass) {
        DomposableWidthClass.Compact -> compact
        DomposableWidthClass.Medium -> medium
        DomposableWidthClass.Expanded -> expanded
        DomposableWidthClass.Large -> large
    }
}

data class DomposableAdaptiveInfo(
    val size: DomposableSize,
    val widthClass: DomposableWidthClass,
    val reducedMotion: Boolean = false,
    val precisePointer: Boolean = false,
)

/** Supply measured feature-content size here, independently of the application shell. */
@Composable
fun rememberDomposableAdaptiveInfo(size: DomposableSize, breakpoints: DomposableBreakpoints = DomposableBreakpoints(), reducedMotion: Boolean? = null, precisePointer: Boolean? = null): DomposableAdaptiveInfo =
    DomposableAdaptiveInfo(size, breakpoints.classify(size.width), reducedMotion ?: reducedMotionPreference(), precisePointer ?: precisePointerPreference())

@Composable internal expect fun reducedMotionPreference(): Boolean
@Composable internal expect fun precisePointerPreference(): Boolean

enum class DomposablePaneRole { List, Detail, Supporting }
data class DomposablePaneRecipe(val dualPaneFrom: DomposableWidthClass = DomposableWidthClass.Expanded) {
    fun isDualPane(info: DomposableAdaptiveInfo): Boolean = info.widthClass.ordinal >= dualPaneFrom.ordinal
}
