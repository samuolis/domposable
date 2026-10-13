package io.github.samuolis.domposable.ui

/** Renderer-neutral column roles for responsive matrix layouts. */
enum class DomposableMatrixColumnRole {
    RowHeader,
    MatrixValue,
    Summary,
}

data class DomposableMatrixTableColumn(
    val key: String,
    val compactLabel: String,
    val expandedLabel: String = compactLabel,
    val role: DomposableMatrixColumnRole,
    val accessibleLabel: String = expandedLabel,
)

/** Semantic cell fills; renderers map these roles to their own palette. */
enum class DomposableMatrixCellSurface {
    Empty,
    Muted,
    Accent,
    AccentEmphasis,
}

/** Semantic foreground roles; renderers map these roles to their own palette. */
enum class DomposableMatrixCellTone {
    Disabled,
    Neutral,
    Positive,
}

/**
 * A renderer-neutral matrix cell. The icon type is supplied by the consuming design system so
 * this model does not depend on a particular icon catalogue.
 */
data class DomposableMatrixTableCell<Icon>(
    val text: String,
    val surface: DomposableMatrixCellSurface = DomposableMatrixCellSurface.Empty,
    val tone: DomposableMatrixCellTone = DomposableMatrixCellTone.Neutral,
    val emphasized: Boolean = false,
    val enabled: Boolean = false,
    val overlayIcon: Icon? = null,
    val overlayDescription: String? = null,
    val overlayTestTag: String? = null,
)

/**
 * A keyed matrix row. The icon type is supplied by the consuming design system so this model
 * stays independent of any product icon source.
 */
data class DomposableMatrixTableRow<Icon>(
    val key: String,
    val accessibleLabel: String?,
    val compactHeader: String,
    val expandedHeader: String = compactHeader,
    val accentIndex: Int = 0,
    val trailingIcon: Icon? = null,
    val trailingIconDescription: String? = null,
    val trailingIconTestTag: String? = null,
    val cellsByColumn: Map<String, DomposableMatrixTableCell<Icon>>,
)
