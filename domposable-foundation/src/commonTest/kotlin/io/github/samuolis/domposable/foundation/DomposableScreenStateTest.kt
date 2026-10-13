package io.github.samuolis.domposable.foundation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DomposableScreenStateTest {
    @Test
    fun errorStateKeepsRetryOptionalAndInvokesTheProvidedAction() {
        val noRetry = DomposableScreenState.Error("Unavailable")
        assertNull(noRetry.retry)

        var retryCount = 0
        val retryable = DomposableScreenState.Error("Unavailable") { retryCount++ }
        assertEquals("Unavailable", retryable.message)
        retryable.retry?.invoke()
        assertEquals(1, retryCount)
    }

    @Test
    fun loadingAndContentRepresentTheirExpectedStates() {
        val loading: DomposableScreenState<String> = DomposableScreenState.Loading
        val content: DomposableScreenState<String> = DomposableScreenState.Content("ready")
        assertEquals(DomposableScreenState.Loading, loading)
        assertEquals(DomposableScreenState.Content("ready"), content)
    }
}
