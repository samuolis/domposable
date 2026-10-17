package io.github.samuolis.domposable.navigation3

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CallerHistoryTest {
    @Test fun pushesRetainEverySnapshotAndPruneOnlyTheForwardBranch() {
        val state = DomposableBrowserHistoryState("home")
        state.push("events")
        state.push("detail")

        assertEquals(listOf(0 to "home", 1 to "events", 2 to "detail"), state.retainedSnapshots.sortedBy { it.first })
        assertEquals(listOf("events", "detail"), state.takePending().map { it.value })

        state.restore(DomposableBrowserHistoryRecord(1, "events"))
        state.push("profile")

        assertEquals(listOf(0 to "home", 1 to "events", 2 to "profile"), state.retainedSnapshots.sortedBy { it.first })
        assertEquals(listOf("profile"), state.takePending().map { it.value })
    }

    @Test fun replaceAndRestoreKeepThePhysicalPositionAndRestoreEmitsNoWrite() {
        val state = DomposableBrowserHistoryState("home")
        state.push("events")
        state.takePending()
        state.push("detail")
        state.takePending()

        state.replace("event-detail", "#section")
        assertEquals(2, state.position)
        assertEquals("event-detail", state.current)
        assertEquals(listOf("#section"), state.takePending().map { it.url })

        state.restore(DomposableBrowserHistoryRecord(1, "events"))
        assertEquals(
            listOf(0 to "home", 1 to "events", 2 to "event-detail"),
            state.retainedSnapshots.sortedBy { it.first },
        )
        state.restore(DomposableBrowserHistoryRecord(4, "settings"))
        assertEquals(4, state.position)
        assertEquals("settings", state.current)
        assertEquals(emptyList(), state.takePending())
    }

    @Test fun restoreCapturesOutgoingStateBeforePositionChangesAndDoesNotQueueWrites() {
        val state = DomposableBrowserHistoryState("home")
        state.push("events")
        state.takePending()
        val order = mutableListOf<String>()

        restoreBrowserHistoryRecord(
            state,
            DomposableBrowserHistoryRecord(0, "home"),
            onBeforeRestore = {
                order += "before:${state.position}:${state.current}"
            },
            onRestore = { record ->
                order += "restore:${record?.position}:${state.position}:${state.current}"
            },
        )

        assertEquals(listOf("before:1:events", "restore:0:0:home"), order)
        assertEquals(emptyList(), state.takePending())
    }

    @Test fun absentOrInvalidRecordStillInvokesUrlFallbackWithoutChangingHistory() {
        val state = DomposableBrowserHistoryState("current")
        state.push("pending")
        val order = mutableListOf<String>()

        restoreBrowserHistoryRecord(
            state,
            null,
            onBeforeRestore = { order += "before:${state.position}" },
            onRestore = { order += "url-fallback:${it == null}:${state.position}" },
        )

        assertEquals(listOf("before:1", "url-fallback:true:1"), order)
        assertEquals(emptyList(), state.takePending())
    }

    @Test fun resetDropsRetainedLifetimesAtTheCurrentPosition() {
        val state = DomposableBrowserHistoryState("home")
        state.push("events")
        state.takePending()
        state.push("detail")
        state.takePending()

        state.reset("signed-out")

        assertEquals(2, state.position)
        assertEquals(listOf(2 to "signed-out"), state.retainedSnapshots)
        assertEquals(listOf(true), state.takePending().map { it.replace })
    }

    @Test fun positionsMustBeNonNegative() {
        assertFailsWith<IllegalArgumentException> { DomposableBrowserHistoryState("home", -1) }
        val state = DomposableBrowserHistoryState("home")
        assertFailsWith<IllegalArgumentException> { state.restore(DomposableBrowserHistoryRecord(-1, "invalid")) }
    }

    @Test fun unsafeExplicitUrlsAreRejectedBeforeChangingTheCallerHistory() {
        val state = DomposableBrowserHistoryState("home")

        assertFailsWith<IllegalArgumentException> { state.push("external", "https://outside.invalid/") }
        assertFailsWith<IllegalArgumentException> { state.replace("script", "javascript:alert(1)") }
        assertFailsWith<IllegalArgumentException> { state.reset("protocol-relative", "//outside.invalid/") }

        assertEquals(0, state.position)
        assertEquals("home", state.current)
        assertEquals(listOf(0 to "home"), state.retainedSnapshots)
        assertEquals(emptyList(), state.pendingIntents)
    }

    @Test fun invalidOrTamperedSnapshotsAreIgnored() {
        val codec =
            object : DomposableBrowserHistoryCodec<String> {
                override fun encode(
                    position: Int,
                    value: String,
                ) = "$position:$value"

                override fun decode(snapshot: String): DomposableBrowserHistoryRecord<String>? {
                    val (position, value) = snapshot.split(":", limit = 2)
                    return DomposableBrowserHistoryRecord(position.toInt(), value)
                }

                override fun url(value: String) = "/$value"
            }

        assertEquals(null, decodeHistoryRecord("not-a-snapshot", codec))
        assertEquals(null, decodeHistoryRecord("-2:tampered", codec))
        assertEquals(DomposableBrowserHistoryRecord(3, "settings"), decodeHistoryRecord("3:settings", codec))
    }
}
