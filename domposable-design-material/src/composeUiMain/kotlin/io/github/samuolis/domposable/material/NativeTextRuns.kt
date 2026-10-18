package io.github.samuolis.domposable.material

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import io.github.samuolis.domposable.ui.DomposableTextProps

internal fun DomposableTextProps.annotatedText(inheritedColor: Color): AnnotatedString =
    if (runs.isEmpty()) {
        AnnotatedString(text)
    } else {
        buildAnnotatedString {
            runs.forEach { run ->
                val foreground = run.color?.let { Color(it.argb) } ?: color?.let { Color(it.argb) } ?: inheritedColor
                withStyle(
                    SpanStyle(
                        fontWeight = run.fontWeight?.let { FontWeight(it.weight) },
                        color =
                            if (run.contentAlpha == 1f) {
                                run.color?.let { Color(it.argb) } ?: Color.Unspecified
                            } else {
                                foreground.copy(alpha = foreground.alpha * run.contentAlpha)
                            },
                    ),
                ) { append(run.text) }
            }
        }
    }
