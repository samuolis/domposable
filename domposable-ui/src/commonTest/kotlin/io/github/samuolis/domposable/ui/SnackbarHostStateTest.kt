package io.github.samuolis.domposable.ui

import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SnackbarHostStateTest {
    @Test
    fun requestsQueueAndCompleteOnlyOnce() =
        runTest {
            val state = DomposableSnackbarHostState()
            val first = async { state.showSnackbar("Saved", "Undo") }
            val second = async { state.showSnackbar("Uploaded") }
            runCurrent()
            val data = state.currentSnackbarData!!
            assertEquals("Saved", data.message)
            data.performAction()
            data.dismiss()
            runCurrent()
            assertEquals(DomposableSnackbarResult.ActionPerformed, first.await())
            assertEquals("Uploaded", state.currentSnackbarData?.message)
            state.currentSnackbarData!!.dismiss()
            assertEquals(DomposableSnackbarResult.Dismissed, second.await())
            assertNull(state.currentSnackbarData)
        }

    @Test
    fun cancellationRemovesCurrentAndQueuedRequests() =
        runTest {
            val state = DomposableSnackbarHostState()
            val first = async { state.showSnackbar("First") }
            val cancelled = async { state.showSnackbar("Cancelled") }
            val last = async { state.showSnackbar("Last") }
            runCurrent()
            cancelled.cancelAndJoin()
            first.cancelAndJoin()
            runCurrent()
            assertEquals("Last", state.currentSnackbarData?.message)
            last.cancelAndJoin()
            assertNull(state.currentSnackbarData)
        }
}
