package io.github.samuolis.domposable.ui

/**
 * A safe, renderer-neutral node produced by [parseDomposableRichHtml]. The AST contains only
 * text, allowlisted formatting elements, and a validated link destination; it never contains
 * executable markup or arbitrary HTML attributes.
 */
sealed interface DomposableRichHtmlNode {
    data class Text(
        val value: String,
    ) : DomposableRichHtmlNode

    data class Element(
        val tag: String,
        val href: String? = null,
        val children: List<DomposableRichHtmlNode> = emptyList(),
    ) : DomposableRichHtmlNode
}

/**
 * Parses a deliberately small formatting vocabulary. Active-content subtrees, unknown tags,
 * and all attributes other than a validated anchor `href` are dropped. Renderers should build
 * their own text/DOM nodes from the returned AST and must not insert the source with `innerHTML`.
 */
fun parseDomposableRichHtml(html: String): List<DomposableRichHtmlNode> = RichHtmlParser(html).parse()

private class RichHtmlParser(
    private val html: String,
) {
    private val root = MutableElement("root")
    private val stack = mutableListOf(root)
    private val suppressed = mutableListOf<String>()
    private val overflow = mutableListOf<String>()
    private var index = 0

    fun parse(): List<DomposableRichHtmlNode> {
        while (index < html.length) {
            if (html[index] == '<') consumeTag() else consumeText()
        }
        return root.children.map(::freezeNode)
    }

    private fun consumeText() {
        val nextTag = html.indexOf('<', index).let { if (it < 0) html.length else it }
        if (suppressed.isEmpty() && nextTag > index) {
            stack.last().children += MutableText(decodeRichHtmlEntities(html.substring(index, nextTag)))
        }
        index = nextTag
    }

    private fun consumeTag() {
        val end = findTagEnd(html, index)
        if (end < 0) {
            if (suppressed.isEmpty()) stack.last().children += MutableText("<")
            index++
        } else {
            val token = html.substring(index, end + 1)
            index = end + 1
            if (!token.startsWith("<!--")) parseTag(token)?.let(::acceptTag)
        }
    }

    private fun acceptTag(tag: ParsedTag) {
        when {
            suppressed.isNotEmpty() -> updateSuppressed(tag)
            tag.name in SUPPRESSED_TAGS -> if (tag.opensContent()) suppressed += tag.name
            overflow.isNotEmpty() -> updateOverflow(tag)
            tag.closing -> closeElement(tag.name)
            tag.name in ALLOWED_TAGS -> appendElement(tag)
        }
    }

    private fun updateSuppressed(tag: ParsedTag) {
        if (tag.name in SUPPRESSED_TAGS && tag.opensContent()) {
            suppressed += tag.name
        } else if (tag.closing) {
            val openIndex = suppressed.indexOfLast { it == tag.name }
            if (openIndex >= 0) suppressed.subList(openIndex, suppressed.size).clear()
        }
    }

    private fun closeElement(name: String) {
        val openIndex = stack.indexOfLast { it.tag == name }
        if (openIndex > 0) stack.subList(openIndex, stack.size).clear()
    }

    private fun updateOverflow(tag: ParsedTag) {
        if (tag.closing) {
            val openIndex = overflow.indexOfLast { it == tag.name }
            if (openIndex >= 0) overflow.subList(openIndex, overflow.size).clear()
        } else if (tag.name in ALLOWED_TAGS && tag.opensContent()) {
            overflow += tag.name
        }
    }

    private fun appendElement(tag: ParsedTag) {
        if (tag.opensContent() && stack.size >= MAX_RICH_HTML_DEPTH) {
            overflow += tag.name
            return
        }
        if (tag.name in PARAGRAPH_TAGS) closeElement(tag.name)
        val href = if (tag.name == "a") tag.attributes["href"]?.let(::safeDomposableHref) else null
        val node = MutableElement(tag.name, href)
        stack.last().children += node
        // Keep recursive consumers bounded even when source markup is deeply nested.
        if (tag.opensContent()) stack += node
    }
}

private fun ParsedTag.opensContent(): Boolean = !closing && !selfClosing && name !in VOID_TAGS

/**
 * Returns a safe relative, web, email, or telephone link target, or `null` when it is unsafe.
 * The returned value is intended for a renderer-created anchor element, never HTML interpolation.
 */
fun safeDomposableHref(value: String): String? {
    val href = value.trim()
    if (href.isEmpty() || href.any(::isForbiddenHrefCharacter) || href.startsWith("//")) return null
    val scheme =
        SCHEME_PREFIX
            .find(href)
            ?.groupValues
            ?.get(1)
            ?.lowercase()
    return when {
        scheme == null -> href.takeIf { !it.contains(':') }
        scheme !in SAFE_SCHEMES -> null
        scheme == "http" || scheme == "https" -> href.takeIf { it.substringAfter(':').startsWith("//") }
        else -> href
    }
}

private fun isForbiddenHrefCharacter(char: Char): Boolean =
    char.code < ASCII_SPACE || char.code == ASCII_DELETE || char == '\\'

private fun freezeNode(node: MutableNode): DomposableRichHtmlNode =
    when (node) {
        is MutableText -> DomposableRichHtmlNode.Text(node.value)
        is MutableElement -> DomposableRichHtmlNode.Element(node.tag, node.href, node.children.map(::freezeNode))
    }

private sealed interface MutableNode

private class MutableText(
    val value: String,
) : MutableNode

private class MutableElement(
    val tag: String,
    val href: String? = null,
    val children: MutableList<MutableNode> = mutableListOf(),
) : MutableNode

private data class ParsedTag(
    val name: String,
    val closing: Boolean,
    val selfClosing: Boolean,
    val attributes: Map<String, String>,
)

private fun findTagEnd(
    html: String,
    start: Int,
): Int {
    var quote: Char? = null
    for (position in start + 1 until html.length) {
        val char = html[position]
        if (quote != null) {
            if (char == quote) quote = null
        } else if (char == '\'' || char == '"') {
            quote = char
        } else if (char == '>') {
            return position
        }
    }
    return -1
}

private fun parseTag(token: String): ParsedTag? {
    if (token.startsWith("<!") || token.startsWith("<?")) return null
    val body = token.substring(1, token.length - 1).trim()
    val closing = body.startsWith('/')
    val withoutSlash = if (closing) body.drop(1).trimStart() else body
    val selfClosing = withoutSlash.endsWith('/')
    val attributeSource = if (selfClosing) withoutSlash.dropLast(1) else withoutSlash
    val name = attributeSource.takeWhile { it.isLetterOrDigit() || it == '-' }.lowercase()
    if (name.isEmpty()) return null
    val attributes = if (closing) emptyMap() else parseAttributes(attributeSource.drop(name.length))
    return ParsedTag(name, closing, selfClosing, attributes)
}

private fun parseAttributes(source: String): Map<String, String> = AttributeReader(source).read()

private class AttributeReader(
    private val source: String,
) {
    private var position = 0

    fun read(): Map<String, String> =
        buildMap {
            while (position < source.length) {
                val attribute = readAttribute()
                if (attribute?.first == "href") put(attribute.first, decodeRichHtmlEntities(attribute.second))
            }
        }

    private fun readAttribute(): Pair<String, String>? {
        val name = readName() ?: return null
        skipWhitespace()
        return if (position >= source.length || source[position] != '=') {
            null
        } else {
            position++
            skipWhitespace()
            name to readValue()
        }
    }

    private fun readName(): String? {
        while (position < source.length && (source[position].isWhitespace() || source[position] == '/')) position++
        val start = position
        while (position < source.length && isAttributeNameCharacter(source[position])) position++
        return source.substring(start, position).lowercase().takeIf { it.isNotEmpty() }.also {
            if (it == null) position++
        }
    }

    private fun skipWhitespace() {
        while (position < source.length && source[position].isWhitespace()) position++
    }

    private fun readValue(): String {
        if (position >= source.length) return ""
        val quote = source[position].takeIf { it == '\'' || it == '"' }
        return if (quote == null) {
            val start = position
            while (position < source.length && !source[position].isWhitespace() && source[position] != '>') position++
            source.substring(start, position)
        } else {
            position++
            val start = position
            val end = source.indexOf(quote, start).let { if (it < 0) source.length else it }
            position = (end + 1).coerceAtMost(source.length)
            source.substring(start, end)
        }
    }
}

private fun isAttributeNameCharacter(char: Char): Boolean = char.isLetterOrDigit() || char in "-_:"

private const val MAX_RICH_HTML_DEPTH = 64
private const val ASCII_SPACE = 0x20
private const val ASCII_DELETE = 0x7f

private val ALLOWED_TAGS =
    setOf(
        "p",
        "h1",
        "h2",
        "h3",
        "ul",
        "ol",
        "li",
        "strong",
        "b",
        "em",
        "i",
        "a",
        "br",
        "blockquote",
        "code",
    )
private val PARAGRAPH_TAGS = setOf("p", "h1", "h2", "h3")
private val VOID_TAGS =
    setOf("area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr")
private val SUPPRESSED_TAGS =
    setOf(
        "script",
        "style",
        "iframe",
        "object",
        "embed",
        "svg",
        "math",
        "template",
        "noscript",
        "form",
        "input",
        "button",
        "select",
        "textarea",
        "video",
        "audio",
        "source",
        "canvas",
    )
private val SAFE_SCHEMES = setOf("http", "https", "mailto", "tel")
private val SCHEME_PREFIX = Regex("^([A-Za-z][A-Za-z0-9+.-]*):")
