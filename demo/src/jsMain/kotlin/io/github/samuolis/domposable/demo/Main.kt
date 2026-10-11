package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.navigation3.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams
import io.github.samuolis.domposable.ui.*
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.renderComposable

fun main() {
    val base = document.body?.getAttribute("data-route-base")
    val urls = if (base == null) DomposableBrowserUrls.Hash else DomposableBrowserUrls.Path(base, trailingSlash = true)
    URLSearchParams(window.location.search).get("deepLink")?.let { path ->
        if (base != null && path.startsWith("/") && !path.startsWith("//"))
            window.history.replaceState(null, "", base.trimEnd('/') + path)
    }
    renderComposable(rootElementId = "root") { DemoApp(browserUrls = urls) }
}

@Composable
internal actual fun CustomDemoButton(props: DomposableButtonProps, modifier: DomposableModifier, content: @Composable () -> Unit) {
    DomposableHtmlModifiers(modifier) { Button(attrs = {
        domposableAttributes(modifier); attr("type", "button"); attr("data-custom-renderer", "true")
        if (!props.enabled) disabled()
        onClick { props.onClick() }
        style { property("border", "2px dashed var(--d-primary)"); property("background", "transparent"); property("color", "var(--d-primary)"); property("padding", "12px"); property("min-height", "44px") }
    }) { content() } }
}
