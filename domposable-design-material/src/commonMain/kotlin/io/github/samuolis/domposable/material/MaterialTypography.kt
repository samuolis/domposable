package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTypography
import io.github.samuolis.domposable.foundation.DomposableTypographyRole

// Material's default type scale; native-oracle tests detect drift when Material is upgraded.
private val MaterialTypeScale =
    mapOf(
        DomposableTypographyRole.DisplayLarge to
            DomposableTextAppearance(fontSize = 57f, lineHeight = 64f, letterSpacing = -0.25f),
        DomposableTypographyRole.DisplayMedium to DomposableTextAppearance(fontSize = 45f, lineHeight = 52f),
        DomposableTypographyRole.DisplaySmall to DomposableTextAppearance(fontSize = 36f, lineHeight = 44f),
        DomposableTypographyRole.HeadlineLarge to DomposableTextAppearance(fontSize = 32f, lineHeight = 40f),
        DomposableTypographyRole.HeadlineMedium to DomposableTextAppearance(fontSize = 28f, lineHeight = 36f),
        DomposableTypographyRole.HeadlineSmall to DomposableTextAppearance(fontSize = 24f, lineHeight = 32f),
        DomposableTypographyRole.TitleLarge to DomposableTextAppearance(fontSize = 22f, lineHeight = 28f),
        DomposableTypographyRole.TitleMedium to
            DomposableTextAppearance(
                fontSize = 16f,
                lineHeight = 24f,
                fontWeight = DomposableFontWeight.Medium,
                letterSpacing = 0.15f,
            ),
        DomposableTypographyRole.TitleSmall to
            DomposableTextAppearance(
                fontSize = 14f,
                lineHeight = 20f,
                fontWeight = DomposableFontWeight.Medium,
                letterSpacing = 0.1f,
            ),
        DomposableTypographyRole.BodyLarge to
            DomposableTextAppearance(fontSize = 16f, lineHeight = 24f, letterSpacing = 0.5f),
        DomposableTypographyRole.BodyMedium to
            DomposableTextAppearance(fontSize = 14f, lineHeight = 20f, letterSpacing = 0.25f),
        DomposableTypographyRole.BodySmall to
            DomposableTextAppearance(fontSize = 12f, lineHeight = 16f, letterSpacing = 0.4f),
        DomposableTypographyRole.LabelLarge to
            DomposableTextAppearance(
                fontSize = 14f,
                lineHeight = 20f,
                fontWeight = DomposableFontWeight.Medium,
                letterSpacing = 0.1f,
            ),
        DomposableTypographyRole.LabelMedium to
            DomposableTextAppearance(
                fontSize = 12f,
                lineHeight = 16f,
                fontWeight = DomposableFontWeight.Medium,
                letterSpacing = 0.5f,
            ),
        DomposableTypographyRole.LabelSmall to
            DomposableTextAppearance(
                fontSize = 11f,
                lineHeight = 16f,
                fontWeight = DomposableFontWeight.Medium,
                letterSpacing = 0.5f,
            ),
    )

internal fun DomposableTypography.materialTypeScale(): Map<DomposableTypographyRole, DomposableTextAppearance> =
    MaterialTypeScale.mapValues { (role, _) -> materialTypeStyle(role) }

internal fun DomposableTypography.materialTypeStyle(role: DomposableTypographyRole): DomposableTextAppearance {
    val base = MaterialTypeScale.getValue(role)
    val size =
        when (role) {
            DomposableTypographyRole.BodyLarge -> bodySize
            DomposableTypographyRole.TitleLarge -> titleSize
            DomposableTypographyRole.LabelLarge -> labelSize
            else -> base.fontSize
        }
    return base.copy(
        fontSize = size,
        lineHeight = if (role == DomposableTypographyRole.BodyLarge) bodySize * lineHeight else base.lineHeight,
        fontWeight = base.fontWeight ?: DomposableFontWeight.Normal,
        letterSpacing = base.letterSpacing ?: 0f,
    )
}
