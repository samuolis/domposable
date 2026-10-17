package io.github.samuolis.domposable.material

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.graphics.shapes.Morph
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class MaterialLoadingParityTest {
    @Test
    fun portableLoadingMorphMatchesInstalledMaterialShapesAtEveryProgress() {
        val native = LoadingIndicatorDefaults.DeterminateIndicatorPolygons
        val expected = Morph(native[0].normalized(), native[1].normalized())
        val actual = Morph(MaterialLoadingPolygons[0].normalized(), MaterialLoadingPolygons[1].normalized())
        listOf(0f, 0.25f, 0.6f, 1f).forEach { progress ->
            val expectedCubics = expected.asCubics(progress)
            val actualCubics = actual.asCubics(progress)
            assertEquals(expectedCubics.size, actualCubics.size)
            expectedCubics.zip(actualCubics).forEach { (reference, portable) ->
                listOf(
                    reference.anchor0X to portable.anchor0X,
                    reference.anchor0Y to portable.anchor0Y,
                    reference.control0X to portable.control0X,
                    reference.control0Y to portable.control0Y,
                    reference.control1X to portable.control1X,
                    reference.control1Y to portable.control1Y,
                    reference.anchor1X to portable.anchor1X,
                    reference.anchor1Y to portable.anchor1Y,
                ).forEach { (a, b) ->
                    assertEquals(a, b, 0.00001f)
                }
            }
        }
    }
}
