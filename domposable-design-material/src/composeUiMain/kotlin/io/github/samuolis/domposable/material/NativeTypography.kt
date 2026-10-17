package io.github.samuolis.domposable.material

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import io.github.samuolis.domposable.foundation.DomposableTextAlignment
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextDecoration
import io.github.samuolis.domposable.foundation.DomposableTextOverflow
import io.github.samuolis.domposable.foundation.DomposableTypography
import io.github.samuolis.domposable.foundation.DomposableTypographyRole
import io.github.samuolis.domposable.ui.LocalDomposableTokens

@Composable
internal fun DomposableTextAppearance?.nativeTextStyle(base: TextStyle): TextStyle {
    val tokens = LocalDomposableTokens.current.typography
    val roleStyle =
        this?.role?.let { role ->
            if (this.emphasized) tokens.emphasizedRoles[role] ?: tokens.roles[role] else tokens.roles[role]
        }
    val roleBase = this?.role?.let { MaterialTheme.typography.nativeRoleStyle(it) } ?: base
    return this.nativeTextStyleDirect(roleStyle?.nativeTextStyleDirect(roleBase) ?: roleBase)
}

internal fun DomposableTextAppearance?.nativeTextStyleDirect(base: TextStyle): TextStyle =
    base.copy(
        color = this?.color?.nativeColor() ?: base.color,
        fontSize = this?.fontSize?.sp ?: base.fontSize,
        lineHeight = this?.lineHeight?.sp ?: base.lineHeight,
        letterSpacing = this?.letterSpacing?.sp ?: base.letterSpacing,
        fontWeight = this?.fontWeight?.native() ?: base.fontWeight,
        textAlign = this?.alignment?.nativeAppearanceTextAlign() ?: base.textAlign,
        textDecoration = this?.decoration?.nativeAppearanceTextDecoration() ?: base.textDecoration,
    )

internal fun androidx.compose.material3.Typography.nativeRoleStyle(role: DomposableTypographyRole): TextStyle =
    when (role) {
        DomposableTypographyRole.DisplayLarge -> displayLarge
        DomposableTypographyRole.DisplayMedium -> displayMedium
        DomposableTypographyRole.DisplaySmall -> displaySmall
        DomposableTypographyRole.HeadlineLarge -> headlineLarge
        DomposableTypographyRole.HeadlineMedium -> headlineMedium
        DomposableTypographyRole.HeadlineSmall -> headlineSmall
        DomposableTypographyRole.TitleLarge -> titleLarge
        DomposableTypographyRole.TitleMedium -> titleMedium
        DomposableTypographyRole.TitleSmall -> titleSmall
        DomposableTypographyRole.BodyLarge -> bodyLarge
        DomposableTypographyRole.BodyMedium -> bodyMedium
        DomposableTypographyRole.BodySmall -> bodySmall
        DomposableTypographyRole.LabelLarge -> labelLarge
        DomposableTypographyRole.LabelMedium -> labelMedium
        DomposableTypographyRole.LabelSmall -> labelSmall
    }

internal fun DomposableTypography.nativeTypography(defaults: Typography = Typography()): Typography {
    val base =
        defaults.copy(
            bodyLarge = defaults.bodyLarge.copy(fontSize = bodySize.sp, lineHeight = (bodySize * lineHeight).sp),
            titleLarge = defaults.titleLarge.copy(fontSize = titleSize.sp),
            labelLarge = defaults.labelLarge.copy(fontSize = labelSize.sp),
        )
    return withConfiguredEmphasizedRoles(withConfiguredRoles(base))
}

private fun DomposableTypography.withConfiguredRoles(base: Typography): Typography =
    base.copy(
        displayLarge = roles[DomposableTypographyRole.DisplayLarge].nativeTextStyleDirect(base.displayLarge),
        displayMedium = roles[DomposableTypographyRole.DisplayMedium].nativeTextStyleDirect(base.displayMedium),
        displaySmall = roles[DomposableTypographyRole.DisplaySmall].nativeTextStyleDirect(base.displaySmall),
        headlineLarge = roles[DomposableTypographyRole.HeadlineLarge].nativeTextStyleDirect(base.headlineLarge),
        headlineMedium = roles[DomposableTypographyRole.HeadlineMedium].nativeTextStyleDirect(base.headlineMedium),
        headlineSmall = roles[DomposableTypographyRole.HeadlineSmall].nativeTextStyleDirect(base.headlineSmall),
        titleLarge = roles[DomposableTypographyRole.TitleLarge].nativeTextStyleDirect(base.titleLarge),
        titleMedium = roles[DomposableTypographyRole.TitleMedium].nativeTextStyleDirect(base.titleMedium),
        titleSmall = roles[DomposableTypographyRole.TitleSmall].nativeTextStyleDirect(base.titleSmall),
        bodyLarge = roles[DomposableTypographyRole.BodyLarge].nativeTextStyleDirect(base.bodyLarge),
        bodyMedium = roles[DomposableTypographyRole.BodyMedium].nativeTextStyleDirect(base.bodyMedium),
        bodySmall = roles[DomposableTypographyRole.BodySmall].nativeTextStyleDirect(base.bodySmall),
        labelLarge = roles[DomposableTypographyRole.LabelLarge].nativeTextStyleDirect(base.labelLarge),
        labelMedium = roles[DomposableTypographyRole.LabelMedium].nativeTextStyleDirect(base.labelMedium),
        labelSmall = roles[DomposableTypographyRole.LabelSmall].nativeTextStyleDirect(base.labelSmall),
    )

private fun DomposableTypography.withConfiguredEmphasizedRoles(base: Typography): Typography =
    base.copy(
        displayLargeEmphasized =
            emphasizedRoles[DomposableTypographyRole.DisplayLarge]
                .nativeTextStyleDirect(base.displayLargeEmphasized),
        displayMediumEmphasized =
            emphasizedRoles[DomposableTypographyRole.DisplayMedium]
                .nativeTextStyleDirect(base.displayMediumEmphasized),
        displaySmallEmphasized =
            emphasizedRoles[DomposableTypographyRole.DisplaySmall]
                .nativeTextStyleDirect(base.displaySmallEmphasized),
        headlineLargeEmphasized =
            emphasizedRoles[DomposableTypographyRole.HeadlineLarge]
                .nativeTextStyleDirect(base.headlineLargeEmphasized),
        headlineMediumEmphasized =
            emphasizedRoles[DomposableTypographyRole.HeadlineMedium]
                .nativeTextStyleDirect(base.headlineMediumEmphasized),
        headlineSmallEmphasized =
            emphasizedRoles[DomposableTypographyRole.HeadlineSmall]
                .nativeTextStyleDirect(base.headlineSmallEmphasized),
        titleLargeEmphasized =
            emphasizedRoles[DomposableTypographyRole.TitleLarge]
                .nativeTextStyleDirect(base.titleLargeEmphasized),
        titleMediumEmphasized =
            emphasizedRoles[DomposableTypographyRole.TitleMedium]
                .nativeTextStyleDirect(base.titleMediumEmphasized),
        titleSmallEmphasized =
            emphasizedRoles[DomposableTypographyRole.TitleSmall]
                .nativeTextStyleDirect(base.titleSmallEmphasized),
        bodyLargeEmphasized =
            emphasizedRoles[DomposableTypographyRole.BodyLarge]
                .nativeTextStyleDirect(base.bodyLargeEmphasized),
        bodyMediumEmphasized =
            emphasizedRoles[DomposableTypographyRole.BodyMedium]
                .nativeTextStyleDirect(base.bodyMediumEmphasized),
        bodySmallEmphasized =
            emphasizedRoles[DomposableTypographyRole.BodySmall]
                .nativeTextStyleDirect(base.bodySmallEmphasized),
        labelLargeEmphasized =
            emphasizedRoles[DomposableTypographyRole.LabelLarge]
                .nativeTextStyleDirect(base.labelLargeEmphasized),
        labelMediumEmphasized =
            emphasizedRoles[DomposableTypographyRole.LabelMedium]
                .nativeTextStyleDirect(base.labelMediumEmphasized),
        labelSmallEmphasized =
            emphasizedRoles[DomposableTypographyRole.LabelSmall]
                .nativeTextStyleDirect(base.labelSmallEmphasized),
    )

internal fun DomposableTextAlignment.nativeAppearanceTextAlign(): TextAlign =
    when (this) {
        DomposableTextAlignment.Start -> TextAlign.Start
        DomposableTextAlignment.Center -> TextAlign.Center
        DomposableTextAlignment.End -> TextAlign.End
        DomposableTextAlignment.Justify -> TextAlign.Justify
    }

internal fun DomposableTextDecoration.nativeAppearanceTextDecoration(): TextDecoration =
    when (this) {
        DomposableTextDecoration.None -> TextDecoration.None
        DomposableTextDecoration.Underline -> TextDecoration.Underline
        DomposableTextDecoration.LineThrough -> TextDecoration.LineThrough
    }

internal fun DomposableTextOverflow.nativeOverflow(): TextOverflow =
    when (this) {
        DomposableTextOverflow.Clip -> TextOverflow.Clip
        DomposableTextOverflow.Ellipsis -> TextOverflow.Ellipsis
        DomposableTextOverflow.Visible -> TextOverflow.Visible
    }
