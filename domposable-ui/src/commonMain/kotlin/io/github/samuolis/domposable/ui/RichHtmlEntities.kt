package io.github.samuolis.domposable.ui

internal fun decodeRichHtmlEntities(value: String): String = ENTITY_PATTERN.replace(value, ::decodeEntity)

private fun decodeEntity(match: MatchResult): String {
    val entity = match.groupValues[1]
    return NAMED_ENTITIES[entity] ?: CONTROL_ENTITIES[entity.lowercase()]
        ?: when {
            entity.startsWith("#x", ignoreCase = true) -> entity.drop(2).toIntOrNull(HEX_RADIX)?.toUnicodeString()
            entity.startsWith('#') -> entity.drop(1).toIntOrNull()?.toUnicodeString()
            else -> null
        } ?: match.value
}

private fun Int.toUnicodeString(): String {
    if (this !in 1..UNICODE_MAX || this in HIGH_SURROGATE_START..LOW_SURROGATE_END) return "\uFFFD"
    if (this < SUPPLEMENTARY_START) return toChar().toString()
    val codePoint = this - SUPPLEMENTARY_START
    return charArrayOf(
        (HIGH_SURROGATE_START + (codePoint shr SURROGATE_BITS)).toChar(),
        (LOW_SURROGATE_START + (codePoint and SURROGATE_MASK)).toChar(),
    ).concatToString()
}

private const val HEX_RADIX = 16
private const val UNICODE_MAX = 0x10ffff
private const val SUPPLEMENTARY_START = 0x10000
private const val HIGH_SURROGATE_START = 0xd800
private const val LOW_SURROGATE_START = 0xdc00
private const val LOW_SURROGATE_END = 0xdfff
private const val SURROGATE_BITS = 10
private const val SURROGATE_MASK = 0x3ff
private val NAMED_ENTITIES =
    mapOf(
        "amp" to "&",
        "lt" to "<",
        "gt" to ">",
        "quot" to "\"",
        "apos" to "'",
        "#39" to "'",
        "nbsp" to "\u00a0",
    )
private val CONTROL_ENTITIES = mapOf("colon" to ":", "tab" to "\t", "newline" to "\n")

private val ENTITY_PATTERN = Regex("&(#(?:x[0-9a-fA-F]+|[0-9]+)|[A-Za-z]+);")
