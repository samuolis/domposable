package io.github.samuolis.domposable.material

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.ui.DomposableComponents
import io.github.samuolis.domposable.ui.DomposableTheme

@Composable
expect fun rememberDomposableMaterialComponents(): DomposableComponents

@Composable
fun DomposableMaterialTheme(
    tokens: DomposableTokens = DomposableTokens(),
    content: @Composable () -> Unit,
) {
    DomposableTheme(tokens, rememberDomposableMaterialComponents(), content)
}
