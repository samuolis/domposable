package io.github.samuolis.domposable.material

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableTableProps
import io.github.samuolis.domposable.ui.DomposableTableRow
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.toComposeModifier

@Composable
internal fun NativeTable(
    props: DomposableTableProps,
    modifier: DomposableModifier,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.table
    val padding = appearance.cellPadding?.nativePadding() ?: PaddingValues(8.dp)
    Column(modifier.toComposeModifier().fillMaxWidth()) {
        Text(props.caption, style = appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.titleMedium))
        Row(
            Modifier
                .fillMaxWidth()
                .then(appearance.headerContainer?.let { Modifier.background(it.nativeColor()) } ?: Modifier)
                .drawBottomBorder(appearance.border?.nativeColor()),
        ) {
            props.columns.forEach { column ->
                key(column.key) {
                    Text(
                        column.label,
                        Modifier.weight(1f).padding(padding).semantics { heading() },
                        color = appearance.headerContent?.nativeColor() ?: LocalContentColor.current,
                        style =
                            appearance.headerTextAppearance.nativeTextStyle(
                                appearance.textAppearance.nativeTextStyle(MaterialTheme.typography.labelLarge),
                            ),
                    )
                }
            }
        }
        val semantics = Modifier.semantics { collectionInfo = CollectionInfo(props.rows.size, props.columns.size) }
        val window = props.window
        if (window == null) {
            Column(semantics, verticalArrangement = Arrangement.spacedBy(0.dp)) {
                props.rows.forEachIndexed { index, row -> key(row.key) { NativeTableRow(props, row, index) } }
            }
        } else {
            LazyColumn(semantics.height(window.viewportHeight.value.dp)) {
                itemsIndexed(props.rows, key = {
                    _,
                    row,
                    ->
                    row.key
                }) { index, row -> NativeTableRow(props, row, index) }
            }
        }
    }
}

@Composable
private fun NativeTableRow(
    props: DomposableTableProps,
    row: DomposableTableRow,
    index: Int,
) {
    val appearance = props.appearance ?: LocalDomposableTokens.current.components.table
    val padding = appearance.cellPadding?.nativePadding() ?: PaddingValues(8.dp)
    val fixed = props.window?.takeUnless { it.measureRows }
    val rowModifier =
        (fixed?.let { Modifier.height(it.rowHeight.value.dp).clipToBounds() } ?: Modifier)
            .fillMaxWidth()
            .then(appearance.rowContainer?.let { Modifier.background(it.nativeColor()) } ?: Modifier)
            .drawBottomBorder(appearance.border?.nativeColor())
    CompositionLocalProvider(
        LocalTextStyle provides appearance.textAppearance.nativeTextStyle(LocalTextStyle.current),
        LocalContentColor provides (appearance.rowContent?.nativeColor() ?: LocalContentColor.current),
    ) {
        Row(rowModifier) {
            row.cells.forEachIndexed { column, cell ->
                key(props.columns[column].key) {
                    Box(
                        Modifier.weight(1f).padding(padding).semantics {
                            collectionItemInfo = CollectionItemInfo(index, 1, column, 1)
                        },
                    ) { cell() }
                }
            }
        }
    }
}

private fun Modifier.drawBottomBorder(color: Color?): Modifier =
    if (color == null) {
        this
    } else {
        drawBehind {
            drawLine(color, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
        }
    }
