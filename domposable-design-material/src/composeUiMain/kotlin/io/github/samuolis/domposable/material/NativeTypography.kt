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
    val roleBase =
        this?.role?.let {
            if (this.emphasized) {
                MaterialTheme.typography.nativeEmphasizedRoleStyle(
                    it,
                )
            } else {
                MaterialTheme.typography.nativeRoleStyle(it)
            }
        } ?: base
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

internal fun androidx.compose.material3.Typography.nativeEmphasizedRoleStyle(
    role: DomposableTypographyRole,
): TextStyle =
    when (role) {
        DomposableTypographyRole.DisplayLarge -> displayLargeEmphasized
        DomposableTypographyRole.DisplayMedium -> displayMediumEmphasized
        DomposableTypographyRole.DisplaySmall -> displaySmallEmphasized
        DomposableTypographyRole.HeadlineLarge -> headlineLargeEmphasized
        DomposableTypographyRole.HeadlineMedium -> headlineMediumEmphasized
        DomposableTypographyRole.HeadlineSmall -> headlineSmallEmphasized
        DomposableTypographyRole.TitleLarge -> titleLargeEmphasized
        DomposableTypographyRole.TitleMedium -> titleMediumEmphasized
        DomposableTypographyRole.TitleSmall -> titleSmallEmphasized
        DomposableTypographyRole.BodyLarge -> bodyLargeEmphasized
        DomposableTypographyRole.BodyMedium -> bodyMediumEmphasized
        DomposableTypographyRole.BodySmall -> bodySmallEmphasized
        DomposableTypographyRole.LabelLarge -> labelLargeEmphasized
        DomposableTypographyRole.LabelMedium -> labelMediumEmphasized
        DomposableTypographyRole.LabelSmall -> labelSmallEmphasized
    }

internal fun DomposableTypography.nativeTypography(defaults: Typography = Typography()): Typography {
    val base =
        defaults.copy(
            bodyLarge = defaults.bodyLarge.copy(fontSize = bodySize.sp, lineHeight = (bodySize * lineHeight).sp),
            titleLarge = defaults.titleLarge.copy(fontSize = titleSize.sp),
            labelLarge = defaults.labelLarge.copy(fontSize = labelSize.sp),
            bodyLargeEmphasized =
                defaults.bodyLargeEmphasized.copy(
                    fontSize = bodySize.sp,
                    lineHeight =
                        (
                            bodySize *
                                lineHeight
                        ).sp,
                ),
            titleLargeEmphasized = defaults.titleLargeEmphasized.copy(fontSize = titleSize.sp),
            labelLargeEmphasized = defaults.labelLargeEmphasized.copy(fontSize = labelSize.sp),
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
        displayLargeEmphasized = emphasizedStyle(DomposableTypographyRole.DisplayLarge, base.displayLargeEmphasized),
        displayMediumEmphasized = emphasizedStyle(DomposableTypographyRole.DisplayMedium, base.displayMediumEmphasized),
        displaySmallEmphasized = emphasizedStyle(DomposableTypographyRole.DisplaySmall, base.displaySmallEmphasized),
        headlineLargeEmphasized = emphasizedStyle(DomposableTypographyRole.HeadlineLarge, base.headlineLargeEmphasized),
        headlineMediumEmphasized =
            emphasizedStyle(
                DomposableTypographyRole.HeadlineMedium,
                base.headlineMediumEmphasized,
            ),
        headlineSmallEmphasized =
            emphasizedStyle(
                DomposableTypographyRole.HeadlineSmall,
                base.headlineSmallEmphasized,
            ),
        titleLargeEmphasized = emphasizedStyle(DomposableTypographyRole.TitleLarge, base.titleLargeEmphasized),
        titleMediumEmphasized = emphasizedStyle(DomposableTypographyRole.TitleMedium, base.titleMediumEmphasized),
        titleSmallEmphasized = emphasizedStyle(DomposableTypographyRole.TitleSmall, base.titleSmallEmphasized),
        bodyLargeEmphasized = emphasizedStyle(DomposableTypographyRole.BodyLarge, base.bodyLargeEmphasized),
        bodyMediumEmphasized = emphasizedStyle(DomposableTypographyRole.BodyMedium, base.bodyMediumEmphasized),
        bodySmallEmphasized = emphasizedStyle(DomposableTypographyRole.BodySmall, base.bodySmallEmphasized),
        labelLargeEmphasized = emphasizedStyle(DomposableTypographyRole.LabelLarge, base.labelLargeEmphasized),
        labelMediumEmphasized = emphasizedStyle(DomposableTypographyRole.LabelMedium, base.labelMediumEmphasized),
        labelSmallEmphasized = emphasizedStyle(DomposableTypographyRole.LabelSmall, base.labelSmallEmphasized),
    )

private fun DomposableTypography.emphasizedStyle(
    role: DomposableTypographyRole,
    base: TextStyle,
): TextStyle {
    // The older Material constructor aliases emphasized styles to regular styles.
    val weight = materialTypeStyle(role, emphasized = true).fontWeight?.native() ?: base.fontWeight
    return emphasizedRoles[role].nativeTextStyleDirect(base.copy(fontWeight = weight))
}

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
