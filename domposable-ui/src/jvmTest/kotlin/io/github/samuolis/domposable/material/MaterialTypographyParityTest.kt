package io.github.samuolis.domposable.material

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import io.github.samuolis.domposable.foundation.DomposableTypography
import kotlin.test.Test
import kotlin.test.assertEquals

class MaterialTypographyParityTest {
    @Test
    fun htmlDefaultsMatchTheInstalledNativeMaterialTypeScale() {
        listOf(
            DomposableTypography(),
            DomposableTypography(bodySize = 18f, titleSize = 28f, labelSize = 15f, lineHeight = 1.6f),
        ).forEach { tokens ->
            val native = tokens.nativeTypography()
            for (emphasized in listOf(false, true)) {
                tokens.materialTypeScale(emphasized).forEach { (role, html) ->
                    val expected =
                        if (emphasized) {
                            native.nativeEmphasizedRoleStyle(
                                role,
                            )
                        } else {
                            native.nativeRoleStyle(role)
                        }
                    assertEquals(expected.fontSize.value, html.fontSize!!, 0.0001f, "$role size")
                    assertEquals(expected.lineHeight.value, html.lineHeight!!, 0.0001f, "$role line height")
                    assertEquals(expected.letterSpacing.value, html.letterSpacing!!, 0.0001f, "$role letter spacing")
                    assertEquals(expected.fontWeight, html.fontWeight!!.native(), "$role weight")
                    if (emphasized) {
                        val material = Typography(fontFamily = FontFamily.Default).nativeEmphasizedRoleStyle(role)
                        assertEquals(
                            material.fontWeight,
                            html.fontWeight!!.native(),
                            "$role Material emphasized weight",
                        )
                    }
                }
            }
        }
    }
}
