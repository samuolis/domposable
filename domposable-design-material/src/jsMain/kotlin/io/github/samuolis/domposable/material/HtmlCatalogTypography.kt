package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import org.jetbrains.compose.web.css.StyleScope

private const val DEFAULT_FONT_SIZE_PX = 16f

internal fun StyleScope.applyCatalogTextAppearance(appearance: DomposableTextAppearance) {
    applyRoleTextAppearance(appearance)
    appearance.fontSize?.let { property("font-size", "${it}px") }
    appearance.lineHeight?.let { property("line-height", "${it}px") }
    appearance.fontWeight?.let { property("font-weight", it.css()) }
    applyTextAlignment(appearance)
    applyTextDecoration(appearance)
    appearance.letterSpacing?.let { property("letter-spacing", "${it}px") }
    appearance.selectable?.let { property("user-select", if (it) "text" else "none") }
    applyMaxLines(appearance)
}

private fun StyleScope.applyRoleTextAppearance(appearance: DomposableTextAppearance) {
    appearance.role?.let { role ->
        val prefix = "--d-type-${role.name.toKebabCase()}${if (appearance.emphasized) "-emphasized" else ""}"
        property("font-size", appearance.fontSize?.let { "${it}px" } ?: "var($prefix-size,inherit)")
        property("line-height", appearance.lineHeight?.let { "${it}px" } ?: "var($prefix-line-height,inherit)")
        property("font-weight", appearance.fontWeight?.css() ?: "var($prefix-weight,inherit)")
        property("letter-spacing", appearance.letterSpacing?.let { "${it}px" } ?: "var($prefix-letter-spacing,normal)")
    }
}

private fun StyleScope.applyTextAlignment(appearance: DomposableTextAppearance) {
    appearance.alignment?.let {
        property(
            "text-align",
            when (it.name) {
                "Start" -> "start"
                "End" -> "end"
                else -> it.name.lowercase()
            },
        )
    }
}

private fun StyleScope.applyTextDecoration(appearance: DomposableTextAppearance) {
    appearance.decoration?.let {
        property(
            "text-decoration",
            when (it.name) {
                "LineThrough" -> "line-through"
                else -> it.name.lowercase()
            },
        )
    }
}

private fun StyleScope.applyMaxLines(appearance: DomposableTextAppearance) {
    appearance.maxLines?.let { lines ->
        when (appearance.overflow) {
            io.github.samuolis.domposable.foundation.DomposableTextOverflow.Clip -> {
                property(
                    "max-height",
                    "${(appearance.lineHeight ?: appearance.fontSize ?: DEFAULT_FONT_SIZE_PX) * lines}px",
                )
                property("overflow", "hidden")
            }

            io.github.samuolis.domposable.foundation.DomposableTextOverflow.Ellipsis -> {
                property("display", "-webkit-box")
                property("-webkit-line-clamp", lines.toString())
                property("-webkit-box-orient", "vertical")
                property("overflow", "hidden")
            }

            io.github.samuolis.domposable.foundation.DomposableTextOverflow.Visible -> {
                property("overflow", "visible")
            }
        }
    }
}

internal fun String.toKebabCase(): String = replace(Regex("([a-z])([A-Z])"), "\$1-\$2").lowercase()
