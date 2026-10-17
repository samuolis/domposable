package io.github.samuolis.domposable.demo

import org.junit.Test
import kotlin.test.assertEquals

class CallerHistoryRouteTest {
    @Test
    fun callerHistoryPathsAcceptTheStaticSitesTrailingSlash() {
        assertEquals(DemoRoute.CallerHistory(), DemoCodec.decode("/caller-history/"))
        assertEquals(DemoRoute.CallerHistory("second"), DemoCodec.decode("/caller-history/second/"))
        assertEquals(DemoRoute.CallerHistory("third"), DemoCodec.decode("/caller-history/third"))
    }
}
