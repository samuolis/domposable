package io.github.samuolis.domposable.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TextFieldLineModeTest {
    @Test
    fun multilineTakesPrecedenceEvenWhenCopiedFromSingleLine() {
        val field = DomposableTextFieldProps(value = "", onValueChange = {}, label = "Notes")
        assertTrue(field.effectiveSingleLine)
        assertFalse(field.copy(multiline = true).effectiveSingleLine)
        assertFalse(field.copy(singleLine = false).effectiveSingleLine)
        assertFalse(field.copy(multiline = true, singleLine = true).effectiveSingleLine)
    }
}
