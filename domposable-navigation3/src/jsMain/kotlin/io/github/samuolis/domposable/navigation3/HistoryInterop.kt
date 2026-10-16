package io.github.samuolis.domposable.navigation3

import kotlinx.browser.window

internal actual fun browserHistoryState(): String? =
    window.history.state.let { if (jsTypeOf(it) == "string") it as String else null }

internal actual fun browserHistoryStateIsNull(): Boolean = js("window.history.state === null") as Boolean

internal actual fun replaceBrowserHistoryState(
    value: String,
    url: String?,
) {
    if (url == null) window.history.replaceState(value, "") else window.history.replaceState(value, "", url)
}

internal actual fun pushBrowserHistoryState(
    value: String,
    url: String,
) {
    window.history.pushState(value, "", url)
}
