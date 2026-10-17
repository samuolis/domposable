package io.github.samuolis.domposable.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.samuolis.domposable.demo.generated.resources.Res
import io.github.samuolis.domposable.demo.generated.resources.comparison_medium
import io.github.samuolis.domposable.demo.generated.resources.comparison_regular
import org.jetbrains.compose.resources.Font

@Composable
internal actual fun MaterialComparisonFont(content: @Composable () -> Unit) {
    val family = FontFamily(Font(Res.font.comparison_regular), Font(Res.font.comparison_medium, FontWeight.Medium))
    val base = MaterialTheme.typography
    val typography =
        base.copy(
            displayLarge = base.displayLarge.copy(fontFamily = family),
            displayMedium = base.displayMedium.copy(fontFamily = family),
            displaySmall = base.displaySmall.copy(fontFamily = family),
            headlineLarge = base.headlineLarge.copy(fontFamily = family),
            headlineMedium = base.headlineMedium.copy(fontFamily = family),
            headlineSmall = base.headlineSmall.copy(fontFamily = family),
            titleLarge = base.titleLarge.copy(fontFamily = family),
            titleMedium = base.titleMedium.copy(fontFamily = family),
            titleSmall = base.titleSmall.copy(fontFamily = family),
            bodyLarge = base.bodyLarge.copy(fontFamily = family),
            bodyMedium = base.bodyMedium.copy(fontFamily = family),
            bodySmall = base.bodySmall.copy(fontFamily = family),
            labelLarge = base.labelLarge.copy(fontFamily = family),
            labelMedium = base.labelMedium.copy(fontFamily = family),
            labelSmall = base.labelSmall.copy(fontFamily = family),
        )
    MaterialTheme(typography = typography, content = content)
}
