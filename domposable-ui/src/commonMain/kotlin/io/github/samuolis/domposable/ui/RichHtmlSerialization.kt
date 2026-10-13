package io.github.samuolis.domposable.ui

/** Serializes the parser's fixed safe vocabulary for native document engines and editor boundaries. */
fun sanitizeDomposableRichHtml(html: String): String =
    parseDomposableRichHtml(html).joinToString("", transform = ::serialize)

private fun serialize(node: DomposableRichHtmlNode): String =
    when (node) {
        is DomposableRichHtmlNode.Text -> {
            node.value.escapeText()
        }

        is DomposableRichHtmlNode.Element -> {
            buildString {
                append('<').append(node.tag)
                val href = node.href
                if (node.tag == "a" && href != null) append(" href=\"").append(href.escapeAttribute()).append('"')
                append('>')
                node.children.forEach { append(serialize(it)) }
                if (node.tag != "br") append("</").append(node.tag).append('>')
            }
        }
    }

private fun String.escapeText(): String = replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

private fun String.escapeAttribute(): String = escapeText().replace("\"", "&quot;")
