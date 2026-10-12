package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.ui.LocalDomposableComponents
import io.github.samuolis.domposable.ui.LocalDomposableTokens

@Composable
actual fun ProvideDomposableMaterialComponents(
    tokens: DomposableTokens,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalDomposableTokens provides tokens,
        LocalDomposableComponents provides rememberDomposableMaterialComponents(),
        content = content,
    )
}
