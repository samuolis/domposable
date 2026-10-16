@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package io.github.samuolis.domposable.demo

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window

fun main() {
    val path = window.location.hash.removePrefix("#")
    val normalizedPath = path.substringBefore('?').substringBefore('#')
    val route = DemoCodec.decode(normalizedPath) ?: DemoRoute.Tasks
    ComposeViewport(document.getElementById("root")!!, configure = { isA11YEnabled = true }) {
        if (path.startsWith("/shop")) {
            ShoppingApp(initialRoute = ShopCodec.decode(path), onArtworkLoaded = {
                document.body!!.setAttribute("data-shop-artwork-ready", "true")
            })
        } else {
            DemoApp(
                initialRoute = route,
                callerHistoryFallback = {
                    val current =
                        window.location.hash
                            .removePrefix("#")
                            .substringBefore('?')
                            .substringBefore('#')
                    val page = current.removePrefix("/caller-history/")
                    if (page == current) "home" else page.ifEmpty { "home" }
                },
            )
            LaunchedEffect(route) {
                document.body!!.setAttribute("data-domposable-initial-route", DemoCodec.encode(route))
            }
        }
        LaunchedEffect(Unit) {
            withFrameNanos { }
            withFrameNanos { }
            document.body!!.setAttribute("data-domposable-ready", "true")
        }
    }
}
