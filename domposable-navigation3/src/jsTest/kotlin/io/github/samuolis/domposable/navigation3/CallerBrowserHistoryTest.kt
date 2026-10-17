package io.github.samuolis.domposable.navigation3

import kotlinx.browser.window
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CallerBrowserHistoryTest {
    @Test
    fun browserBindingFlushesRapidPushesAndRestoresWithoutWritingOnPop() {
        withBrowserHistory {
            val authQuery = "?code=one-time-code&auth_flow=flow-42"
            window.history.replaceState(null, "", "/auth/callback$authQuery")
            val codec = stringCodec()
            val state = DomposableBrowserHistoryState("home")
            val binding = CallerBrowserHistoryBinding(state, codec)
            val restored = mutableListOf<DomposableBrowserHistoryRecord<String>?>()
            binding.initialize { restored += it }
            val initialLength = window.history.length
            assertEquals("0:home", window.history.state)
            assertEquals("/auth/callback$authQuery", window.location.pathname + window.location.search)
            assertEquals(authQuery, window.location.search)

            state.push("events", "/events")
            state.push("detail", "/detail?tab=notes#draft")
            binding.flush()

            assertEquals(initialLength + 2, window.history.length)
            assertEquals(
                "/detail?tab=notes#draft",
                window.location.pathname + window.location.search + window.location.hash,
            )
            assertEquals(
                DomposableBrowserHistoryRecord(2, "detail"),
                decodeHistoryRecord(window.history.state as? String, codec),
            )

            // Model the browser selecting its earlier entry and dispatching popstate.
            val earlierState = codec.encode(1, "events")
            window.history.replaceState(earlierState, "", "/events")
            var outgoing = ""
            binding.pop(
                onBeforeRestore = { outgoing = "${state.position}:${state.current}" },
                onRestore = { restored += it },
            )

            assertEquals("2:detail", outgoing)
            assertEquals(1, state.position)
            assertEquals("events", state.current)
            assertEquals(earlierState, window.history.state)
            assertEquals(DomposableBrowserHistoryRecord(1, "events"), restored.last())
        }
    }

    @Test
    fun invalidPopStateFallsBackWithoutRewritingAndUnsafeUrlsAreRejectedBeforeMutation() {
        withBrowserHistory {
            window.history.replaceState("tampered", "", "/fallback")
            val codec = stringCodec()
            val state = DomposableBrowserHistoryState("home")
            val binding = CallerBrowserHistoryBinding(state, codec)
            var fallbackCalled = false

            binding.initialize { error("Invalid initial state must not invoke restore") }
            assertEquals("tampered", window.history.state)
            assertEquals("/fallback", window.location.pathname)

            window.history.replaceState(js("({tampered: true})"), "", "/object-fallback")
            binding.initialize { error("Foreign initial state must not invoke restore") }
            assertEquals(true, js("window.history.state.tampered") as Boolean)
            assertEquals("/object-fallback", window.location.pathname)

            binding.pop(onBeforeRestore = {}, onRestore = { fallbackCalled = it == null })

            assertTrue(fallbackCalled)
            assertEquals(true, js("window.history.state.tampered") as Boolean)
            assertEquals("/object-fallback", window.location.pathname)

            val invalidUrlState = DomposableBrowserHistoryState("home")
            val unsafeCodec =
                object : DomposableBrowserHistoryCodec<String> by codec {
                    override fun url(value: String) = "javascript:alert(1)"
                }
            val invalidUrlBinding = CallerBrowserHistoryBinding(invalidUrlState, unsafeCodec)
            invalidUrlState.push("external")
            val previousState = window.history.state
            val previousUrl = window.location.href
            val previousLength = window.history.length

            assertFailsWith<IllegalArgumentException> { invalidUrlBinding.flush() }

            assertEquals(previousState, window.history.state)
            assertEquals(previousUrl, window.location.href)
            assertEquals(previousLength, window.history.length)
            assertEquals(1, invalidUrlState.pendingIntents.size)
        }
    }
}

private fun stringCodec() =
    object : DomposableBrowserHistoryCodec<String> {
        override fun encode(
            position: Int,
            value: String,
        ) = "$position:$value"

        override fun decode(snapshot: String): DomposableBrowserHistoryRecord<String>? {
            val separator = snapshot.indexOf(':')
            if (separator <= 0) return null
            val position = snapshot.substring(0, separator).toIntOrNull() ?: return null
            return DomposableBrowserHistoryRecord(position, snapshot.substring(separator + 1))
        }

        override fun url(value: String) = "/$value"
    }

private fun withBrowserHistory(block: () -> Unit) {
    val previousUrl = window.location.href
    val previousState = window.history.state
    try {
        block()
    } finally {
        window.history.replaceState(previousState, "", previousUrl)
    }
}
