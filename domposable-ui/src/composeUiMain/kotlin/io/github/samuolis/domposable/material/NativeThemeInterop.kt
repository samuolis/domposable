package io.github.samuolis.domposable.material

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.ui.LocalDomposableTokens

internal val LocalDomposableNativeTypography = staticCompositionLocalOf<Typography?> { null }

/** Installs resource-backed native typography while retaining the shared theme and component variants. */
@Composable
fun DomposableMaterialTheme(
    nativeTypography: Typography,
    tokens: DomposableTokens = LocalDomposableTokens.current,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDomposableNativeTypography provides nativeTypography) {
        DomposableMaterialTheme(tokens = tokens, content = content)
    }
}
