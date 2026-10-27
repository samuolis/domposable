package io.github.samuolis.domposable.material

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import io.github.samuolis.domposable.foundation.DomposableIconAppearance
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.toComposeModifier

/** Native artwork interoperability; sizing remains controlled by the shared modifier. */
@Composable
fun DomposableIcon(
    painter: Painter,
    description: String?,
    modifier: DomposableModifier = DomposableModifier,
    appearance: DomposableIconAppearance? = null,
) {
    val resolved = appearance ?: LocalDomposableTokens.current.components.icon
    Icon(
        painter = painter,
        contentDescription = description,
        modifier = modifier.toComposeModifier(),
        tint = resolved.color?.nativeColor() ?: LocalContentColor.current,
    )
}

/** Native vector interoperability without serializing paths or losing multi-path artwork. */
@Composable
fun DomposableIcon(
    imageVector: ImageVector,
    description: String?,
    modifier: DomposableModifier = DomposableModifier,
    appearance: DomposableIconAppearance? = null,
) = DomposableIcon(rememberVectorPainter(imageVector), description, modifier, appearance)
