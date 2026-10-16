package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.navigation3.DomposableBrowserUrls
import io.github.samuolis.domposable.ui.DomposableButtonProps
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.background
import io.github.samuolis.domposable.ui.domposableAttributes
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.url.URLSearchParams

fun main() {
    org.jetbrains.compose.resources.configureWebResources {
        resourcePathMapping { path ->
            org.w3c.dom.url
                .URL(path, document.baseURI)
                .href
        }
    }
    val base = document.body?.getAttribute("data-route-base")
    val urls = if (base == null) DomposableBrowserUrls.Hash else DomposableBrowserUrls.Path(base, trailingSlash = true)
    URLSearchParams(window.location.search).get("deepLink")?.let { path ->
        if (base != null && path.startsWith("/") && !path.startsWith("//")) {
            window.history.replaceState(null, "", base.trimEnd('/') + path)
        }
    }
    val shop =
        document.body?.getAttribute("data-app") == "shop" ||
            window.location.hash
                .removePrefix("#")
                .startsWith("/shop")
    val initialPath = currentDemoPath(base)
    val initialRoute = DemoCodec.decode(initialPath) ?: DemoRoute.Tasks
    renderComposable(
        rootElementId = "root",
    ) {
        if (shop) {
            ShoppingApp(browserUrls = urls)
        } else {
            DemoApp(
                initialRoute = initialRoute,
                browserUrls = urls,
                callerHistoryFallback = {
                    val current = currentDemoPath(base)
                    val page = current.removePrefix("/caller-history/")
                    if (page == current) "home" else page.substringBefore('?').substringBefore('#').ifEmpty { "home" }
                },
            )
        }
    }
}

private fun currentDemoPath(base: String?): String {
    val fragmentPath = window.location.hash.removePrefix("#")
    val path = fragmentPath.takeIf { it.startsWith("/") } ?: window.location.pathname
    val withoutBase = if (base == null) path else path.removePrefix(base.trimEnd('/')).ifEmpty { "/" }
    return withoutBase.substringBefore('?').substringBefore('#')
}

@Composable
internal actual fun CustomDemoButton(
    props: DomposableButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
) {
    DomposableHtmlModifiers(modifier) {
        Button(attrs = {
            domposableAttributes(modifier)
            attr("type", "button")
            attr("data-custom-renderer", "true")
            if (!props.enabled) disabled()
            onClick { props.onClick() }
            style {
                property("border", "2px dashed var(--d-primary)")
                property("background", "transparent")
                property("color", "var(--d-primary)")
                property("padding", "12px")
                property("min-height", "44px")
            }
        }) { content() }
    }
}

internal actual fun openShopDemo(onOpen: () -> Unit) {
    window.location.assign(
        org.w3c.dom.url
            .URL("../shop/", document.baseURI)
            .href,
    )
}
