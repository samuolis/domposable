package io.github.samuolis.domposable.ui

import kotlin.test.Test
import kotlin.test.assertFailsWith

class DomposableTextRunTest {
    @Test
    fun textContentAndRunOpacityAreValidated() {
        assertFailsWith<IllegalArgumentException> { DomposableTextRun("Text", contentAlpha = Float.NaN) }
        assertFailsWith<IllegalArgumentException> { DomposableTextRun("Text", contentAlpha = -0.1f) }
        assertFailsWith<IllegalArgumentException> { DomposableTextRun("Text", contentAlpha = 1.1f) }
        assertFailsWith<IllegalArgumentException> {
            DomposableTextProps(text = "Different", runs = listOf(DomposableTextRun("Text")))
        }
    }
}
