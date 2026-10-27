package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.samuolis.domposable.foundation.DomposableColors
import io.github.samuolis.domposable.foundation.DomposableComponentStyles
import io.github.samuolis.domposable.foundation.DomposableDesignLanguage
import io.github.samuolis.domposable.foundation.DomposableShapes
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.DomposableTypography
import io.github.samuolis.domposable.ui.DomposableTheme
import io.github.samuolis.domposable.ui.LocalDomposableTokens

/**
 * Provides Material theme values without rendering a theme container.
 *
 * Hosts that already own their native theme or DOM root can install Domposable's reusable controls
 * while keeping their existing theme and layout tree intact.
 */
@Composable
expect fun ProvideDomposableMaterialComponents(
    tokens: DomposableTokens,
    content: @Composable () -> Unit,
)

@Composable
fun DomposableMaterialTheme(
    tokens: DomposableTokens = LocalDomposableTokens.current,
    colors: DomposableColors = tokens.colors,
    typography: DomposableTypography = tokens.typography,
    shapes: DomposableShapes = tokens.shapes,
    styles: DomposableComponentStyles = tokens.components,
    designLanguage: DomposableDesignLanguage = tokens.designLanguage,
    content: @Composable () -> Unit,
) {
    DomposableTheme(
        tokens.copy(
            colors = colors,
            typography = typography,
            shapes = shapes,
            components = styles,
            designLanguage = designLanguage,
        ),
        content,
    )
}

internal val LocalMaterialButtonTextAppearance = staticCompositionLocalOf<DomposableTextAppearance?> { null }
