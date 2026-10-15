package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.ui.DomposableComponents
import io.github.samuolis.domposable.ui.DomposableTheme

@Composable
expect fun rememberDomposableMaterialComponents(): DomposableComponents

/**
 * Provides the standard Material component set and its tokens without rendering a theme container.
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
    tokens: DomposableTokens = DomposableTokens(),
    content: @Composable () -> Unit,
) {
    DomposableTheme(tokens, rememberDomposableMaterialComponents(), content)
}

internal val LocalMaterialButtonTextAppearance = staticCompositionLocalOf<DomposableTextAppearance?> { null }
