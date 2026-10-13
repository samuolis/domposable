package io.github.samuolis.domposable.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RichHtmlDocumentTest {
    @Test
    fun safeHrefAllowsRelativeAndExplicitSafeSchemes() {
        assertEquals("/help?topic=one&amp;other=two", safeDomposableHref("/help?topic=one&amp;other=two"))
        assertEquals("../guide", safeDomposableHref("../guide"))
        assertEquals("https://example.test/path", safeDomposableHref("https://example.test/path"))
        assertEquals("mailto:help@example.test", safeDomposableHref("mailto:help@example.test"))
        assertEquals("tel:+15551234567", safeDomposableHref("tel:+15551234567"))
    }

    @Test
    fun safeHrefRejectsExecutableSchemesControlsAndAmbiguousAuthorities() {
        listOf(
            "javascript:alert(1)",
            "JaVaScRiPt:alert(1)",
            "java&#x73;cript:alert(1)",
            "java&colon;script:alert(1)",
            "java&Tab;script:alert(1)",
            "java&NewLine;script:alert(1)",
            "data:text/html,<script>alert(1)</script>",
            "//example.test/path",
            "https:example.test/path",
            "https://example.test/a\nb",
            "https://example.test/a\\b",
        ).forEach { assertNull(safeDomposableHref(it), "Expected unsafe href to be rejected: $it") }
    }

    @Test
    fun parserDropsActiveSubtreesAndUnknownAttributesWithoutLosingSafeText() {
        val nodes =
            parseDomposableRichHtml(
                "before<script>alert(1)<b>hidden</b></script>" +
                    "<iframe>frame text</iframe><form><button>submit</button></form>" +
                    "<svg><text>vector text</text></svg>" +
                    "<img src=x onerror=alert(1)>after<a href=\"/safe\" onclick=\"steal()\">link</a>",
            )

        assertEquals(
            listOf(
                DomposableRichHtmlNode.Text("before"),
                DomposableRichHtmlNode.Text("after"),
                DomposableRichHtmlNode.Element(
                    "a",
                    href = "/safe",
                    children = listOf(DomposableRichHtmlNode.Text("link")),
                ),
            ),
            nodes,
        )
    }

    @Test
    fun parserKeepsFormattingAndSafeLinkCallbackPayloadAsData() {
        val nodes =
            parseDomposableRichHtml(
                "<p><strong>Important</strong> <em>note</em> " +
                    "<a href=\"https://example.test/a?x=1&amp;y=2\" onclick=\"run()\">open</a></p>",
            )

        assertEquals(
            listOf(
                DomposableRichHtmlNode.Element(
                    "p",
                    children =
                        listOf(
                            DomposableRichHtmlNode.Element(
                                "strong",
                                children = listOf(DomposableRichHtmlNode.Text("Important")),
                            ),
                            DomposableRichHtmlNode.Text(" "),
                            DomposableRichHtmlNode.Element(
                                "em",
                                children = listOf(DomposableRichHtmlNode.Text("note")),
                            ),
                            DomposableRichHtmlNode.Text(" "),
                            DomposableRichHtmlNode.Element(
                                "a",
                                href = "https://example.test/a?x=1&y=2",
                                children = listOf(DomposableRichHtmlNode.Text("open")),
                            ),
                        ),
                ),
            ),
            nodes,
        )
        val callbackPayload =
            (nodes.single() as DomposableRichHtmlNode.Element)
                .children
                .filterIsInstance<DomposableRichHtmlNode.Element>()
                .single { it.tag == "a" }
                .href
        assertEquals("https://example.test/a?x=1&y=2", callbackPayload)
        assertTrue(nodes.none { it is DomposableRichHtmlNode.Element && it.tag == "script" })
    }

    @Test fun sanitizationPreservesApprovedMarkupAndIsIdempotent() {
        val html =
            "<h1 onclick='run()'>Terms</h1><p>A <strong>bold</strong> &amp; more.</p>" +
                "<a href='https://example.test/?q=1&amp;v=2'>Read</a><script>unsafe</script>"
        val safe = sanitizeDomposableRichHtml(html)
        assertEquals(
            "<h1>Terms</h1><p>A <strong>bold</strong> &amp; more.</p>" +
                "<a href=\"https://example.test/?q=1&amp;v=2\">Read</a>",
            safe,
        )
        assertEquals(safe, sanitizeDomposableRichHtml(safe))
    }

    @Test fun deeplyNestedInputCannotOverflowRecursiveRenderers() {
        val safe = sanitizeDomposableRichHtml("<b>".repeat(10000) + "content" + "</b>".repeat(10000))
        assertTrue("content" in safe)
        assertEquals(safe, sanitizeDomposableRichHtml(safe))
    }
}
