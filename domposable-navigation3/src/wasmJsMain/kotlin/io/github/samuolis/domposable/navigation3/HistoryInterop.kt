package io.github.samuolis.domposable.navigation3

import kotlin.JsFun

@JsFun("() => typeof window.history.state === 'string' ? window.history.state : null")
private external fun readHistoryState(): String?

@JsFun(
    """(value, url) => {
        if (url === null) window.history.replaceState(value, '');
        else window.history.replaceState(value, '', url);
    }""",
)
private external fun replaceHistoryState(
    value: String,
    url: String?,
)

@JsFun("(value, url) => window.history.pushState(value, '', url)")
private external fun pushHistoryState(
    value: String,
    url: String,
)

internal actual fun browserHistoryState(): String? = readHistoryState()

@JsFun("() => window.history.state === null")
private external fun isHistoryStateNull(): Boolean

internal actual fun browserHistoryStateIsNull(): Boolean = isHistoryStateNull()

internal actual fun replaceBrowserHistoryState(
    value: String,
    url: String?,
) = replaceHistoryState(value, url)

internal actual fun pushBrowserHistoryState(
    value: String,
    url: String,
) = pushHistoryState(value, url)
