@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
package io.github.samuolis.domposable.demo

import androidx.compose.ui.window.ComposeViewport
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import kotlinx.browser.document
import kotlinx.browser.window

fun main() {
    val route = when (window.location.hash.removePrefix("#")) {
        "/catalog" -> DemoRoute.Catalog
        "/recipes" -> DemoRoute.Recipes
        else -> DemoRoute.Tasks
    }
    ComposeViewport(document.getElementById("root")!!) {
        DemoApp(initialRoute = route)
        LaunchedEffect(Unit) {
            withFrameNanos { }; withFrameNanos { }
            document.body!!.setAttribute("data-domposable-ready", "true")
        }
    }
}
