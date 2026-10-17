package io.github.samuolis.domposable.demo

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MaterialComparisonRouteTest {
    @Test
    fun everyComparisonThemeAndFamilyRoundTrips() {
        ComparisonThemes.forEach { theme ->
            ComparisonFamilies.forEach { family ->
                val path = "/material/compare/$theme/$family"
                val route = DemoRoute.Material("compare/$theme/$family")
                assertEquals(route, DemoCodec.decode(path))
                assertEquals(path, DemoCodec.encode(route))
            }
        }
    }

    @Test
    fun incompleteOrUnknownComparisonsAreRejected() {
        listOf(
            "compare",
            "compare/light",
            "compare/light/unknown",
            "compare/unknown/states",
            "compare/light/states/extra",
        ).forEach {
            assertFalse(materialComparisonPath(it))
        }
    }
}
