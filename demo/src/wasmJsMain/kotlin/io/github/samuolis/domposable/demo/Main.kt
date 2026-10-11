@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package io.github.samuolis.domposable.demo

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window

fun main() {
    val path = window.location.hash.removePrefix("#")
    val route = DemoCodec.decode(path) ?: DemoRoute.Tasks
    ComposeViewport(document.getElementById("root")!!) {
        if (path.startsWith("/shop")) {
            ShoppingApp(initialRoute = ShopCodec.decode(path), onArtworkLoaded = {
                document.body!!.setAttribute("data-shop-artwork-ready", "true")
            })
        } else {
            DemoApp(initialRoute = route)
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
