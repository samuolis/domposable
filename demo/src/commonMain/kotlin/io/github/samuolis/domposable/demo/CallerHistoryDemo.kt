package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.navigation3.DomposableBrowserHistory
import io.github.samuolis.domposable.navigation3.DomposableBrowserHistoryCodec
import io.github.samuolis.domposable.navigation3.DomposableBrowserHistoryRecord
import io.github.samuolis.domposable.navigation3.DomposableBrowserHistoryState
import io.github.samuolis.domposable.navigation3.DomposableBrowserUrls
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.testId

@Composable
internal fun CallerHistoryDemo(
    initialPage: String,
    fallbackPageFromUrl: () -> String,
    browserUrls: DomposableBrowserUrls,
) {
    val history = remember(initialPage) { DomposableBrowserHistoryState(initialPage) }
    val codec =
        remember {
            object : DomposableBrowserHistoryCodec<String> {
                override fun encode(
                    position: Int,
                    value: String,
                ) = "$position:$value"

                override fun decode(snapshot: String): DomposableBrowserHistoryRecord<String>? {
                    val separator = snapshot.indexOf(':')
                    if (separator <= 0) return null
                    val position = snapshot.substring(0, separator).toIntOrNull() ?: return null
                    val page = snapshot.substring(separator + 1)
                    if (position < 0 || page !in allowedPages) return null
                    return DomposableBrowserHistoryRecord(position, page)
                }

                override fun url(value: String) = callerHistoryUrl(value, browserUrls)
            }
        }
    var rejectedUnsafeUrl by remember { mutableStateOf(false) }

    DomposableBrowserHistory(history, codec) { record ->
        if (record == null) {
            history.restore(DomposableBrowserHistoryRecord(history.position, fallbackPageFromUrl()))
        }
    }

    DomposableColumn(DomposableModifier.padding(24.dp), gap = 12.dp) {
        DomposableText("Caller-owned browser history", headingLevel = 1)
        DomposableText("Current page: ${history.current}", DomposableModifier.testId("caller-history-page"))
        DomposableText("Physical position: ${history.position}", DomposableModifier.testId("caller-history-position"))
        DomposableText(
            "Rejected external URL: $rejectedUnsafeUrl",
            DomposableModifier.testId("caller-history-url-rejected"),
        )
        DomposableButton(
            "Push two routes",
            {
                history.push("second", callerHistoryUrl("second", browserUrls, "?source=fixture#notes"))
                history.push("third", callerHistoryUrl("third", browserUrls, "?source=fixture#summary"))
            },
        )
        DomposableButton(
            "Replace current route",
            { history.replace("replaced", callerHistoryUrl("replaced", browserUrls, "?mode=replace#details")) },
        )
        DomposableButton(
            "Reject external URL",
            {
                rejectedUnsafeUrl =
                    runCatching {
                        history.push("external", "https://outside.invalid/")
                    }.isFailure
            },
        )
    }
}

private val allowedPages = setOf("home", "second", "third", "replaced", "fallback")

private fun callerHistoryUrl(
    page: String,
    browserUrls: DomposableBrowserUrls,
    suffix: String = "",
): String {
    val route = "/caller-history" + if (page == "home") "" else "/$page"
    return when (browserUrls) {
        DomposableBrowserUrls.Hash -> {
            "#$route$suffix"
        }

        is DomposableBrowserUrls.Path -> {
            browserUrls.basePath.trimEnd('/') + route +
                if (browserUrls.trailingSlash) "/$suffix" else suffix
        }
    }
}
