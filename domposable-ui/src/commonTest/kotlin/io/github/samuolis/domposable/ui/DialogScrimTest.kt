package io.github.samuolis.domposable.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DialogScrimTest {
    @Test
    fun dialogScrimDefaultsToThePlatformAndValidatesOverrides() {
        val props = DomposableDialogProps("Saving", {})
        assertEquals(null, props.scrimOpacity)
        for (opacity in listOf(0f, 0.5f, 1f)) {
            assertEquals(opacity, props.copy(scrimOpacity = opacity).scrimOpacity)
        }
        for (opacity in listOf(-0.1f, 1.1f, Float.NaN, Float.POSITIVE_INFINITY)) {
            assertFailsWith<IllegalArgumentException> { props.copy(scrimOpacity = opacity) }
        }
    }
}
