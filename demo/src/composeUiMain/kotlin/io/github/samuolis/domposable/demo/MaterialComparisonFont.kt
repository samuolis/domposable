package io.github.samuolis.domposable.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.samuolis.domposable.demo.generated.resources.Res
import io.github.samuolis.domposable.demo.generated.resources.comparison_bold
import io.github.samuolis.domposable.demo.generated.resources.comparison_medium
import io.github.samuolis.domposable.demo.generated.resources.comparison_regular
import org.jetbrains.compose.resources.Font

@Composable
internal actual fun MaterialComparisonFont(content: @Composable () -> Unit) {
    val family =
        FontFamily(
            Font(Res.font.comparison_regular),
            Font(Res.font.comparison_medium, FontWeight.Medium),
            Font(Res.font.comparison_bold, FontWeight.Bold),
        )
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
            displayLargeEmphasized = base.displayLargeEmphasized.copy(fontFamily = family),
            displayMediumEmphasized = base.displayMediumEmphasized.copy(fontFamily = family),
            displaySmallEmphasized = base.displaySmallEmphasized.copy(fontFamily = family),
            headlineLargeEmphasized = base.headlineLargeEmphasized.copy(fontFamily = family),
            headlineMediumEmphasized = base.headlineMediumEmphasized.copy(fontFamily = family),
            headlineSmallEmphasized = base.headlineSmallEmphasized.copy(fontFamily = family),
            titleLargeEmphasized = base.titleLargeEmphasized.copy(fontFamily = family),
            titleMediumEmphasized = base.titleMediumEmphasized.copy(fontFamily = family),
            titleSmallEmphasized = base.titleSmallEmphasized.copy(fontFamily = family),
            bodyLargeEmphasized = base.bodyLargeEmphasized.copy(fontFamily = family),
            bodyMediumEmphasized = base.bodyMediumEmphasized.copy(fontFamily = family),
            bodySmallEmphasized = base.bodySmallEmphasized.copy(fontFamily = family),
            labelLargeEmphasized = base.labelLargeEmphasized.copy(fontFamily = family),
            labelMediumEmphasized = base.labelMediumEmphasized.copy(fontFamily = family),
            labelSmallEmphasized = base.labelSmallEmphasized.copy(fontFamily = family),
        )
    MaterialTheme(typography = typography, content = content)
}
