package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import example.shop.catalog.api.CatalogFeature
import example.shop.catalog.api.CatalogRoute
import example.shop.catalog.api.LocalShopArtwork
import example.shop.catalog.api.LocalShopArtworkLoaded
import example.shop.catalog.api.LocalShopNavigator
import example.shop.catalog.api.ProductCatalog
import example.shop.catalog.api.ShopArtwork
import example.shop.catalog.api.ShopRoute
import example.shop.catalog.api.ShopScope
import example.shop.checkout.api.CheckoutFeature
import example.shop.checkout.api.CheckoutRoute
import example.shop.checkout.api.ShoppingCart
import io.github.samuolis.domposable.demo.generated.resources.Res
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableColors
import io.github.samuolis.domposable.foundation.DomposableImageScale
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.DomposableTypography
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.material.rememberDomposableMaterialComponents
import io.github.samuolis.domposable.navigation3.DomposableBrowserUrls
import io.github.samuolis.domposable.navigation3.DomposableNavHost
import io.github.samuolis.domposable.navigation3.DomposableRouteCodec
import io.github.samuolis.domposable.navigation3.rememberDomposableNavigator
import io.github.samuolis.domposable.navigation3.urlFor
import io.github.samuolis.domposable.screens.DomposableDestination
import io.github.samuolis.domposable.ui.DomposableBanner
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableFlowRow
import io.github.samuolis.domposable.ui.DomposableImage
import io.github.samuolis.domposable.ui.DomposableLink
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableNavigationBar
import io.github.samuolis.domposable.ui.DomposableNavigationItem
import io.github.samuolis.domposable.ui.DomposableScaffold
import io.github.samuolis.domposable.ui.DomposableSurface
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTheme
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.testId

/** Application composition root aggregates contributions from both implementation modules. */
@DependencyGraph(ShopScope::class)
abstract class ShopGraph : ViewModelGraph {
    abstract val catalog: CatalogFeature
    abstract val checkout: CheckoutFeature
    abstract val cart: ShoppingCart
    abstract val products: ProductCatalog
}

fun createShopGraph(): ShopGraph = createGraph<ShopGraph>()

object ShopCodec : DomposableRouteCodec<ShopRoute> {
    override fun encode(route: ShopRoute) =
        when (route) {
            CatalogRoute.Home -> "/shop"
            CatalogRoute.Collection -> "/shop/collection"
            is CatalogRoute.Product -> "/shop/products/${route.id}"
            CheckoutRoute.Cart -> "/shop/cart"
            CheckoutRoute.Checkout -> "/shop/checkout"
            CheckoutRoute.Confirmation -> "/shop/confirmation"
            is CatalogRoute.Missing -> route.path
            else -> error("Unknown shop route: $route")
        }

    override fun decode(path: String): ShopRoute =
        when {
            path == "/shop" || path == "/" -> CatalogRoute.Home

            path == "/shop/collection" -> CatalogRoute.Collection

            path.startsWith(
                "/shop/products/",
            ) &&
                path
                    .removePrefix(
                        "/shop/products/",
                    ).matches(Regex("[a-z0-9-]+"))
            -> CatalogRoute.Product(path.removePrefix("/shop/products/"))

            path == "/shop/cart" -> CheckoutRoute.Cart

            path == "/shop/checkout" -> CheckoutRoute.Checkout

            path == "/shop/confirmation" -> CheckoutRoute.Confirmation

            else -> CatalogRoute.Missing(path)
        }
}

private val ShopTokens =
    DomposableTokens(
        colors =
            DomposableColors(
                primary = DomposableColor(0xFF354C3C),
                surface = DomposableColor(0xFFFAF8F3),
                onSurface = DomposableColor(0xFF292E28),
                surfaceVariant = DomposableColor(0xFFEAE7DE),
                outline = DomposableColor(0xFF74776E),
            ),
        typography = DomposableTypography(titleSize = 30f),
        radius = 8.dp,
    )

@Composable
fun ShoppingApp(
    graph: ShopGraph = remember { createShopGraph() },
    initialRoute: ShopRoute = CatalogRoute.Home,
    browserUrls: DomposableBrowserUrls = DomposableBrowserUrls.Hash,
    onExit: (() -> Unit)? = null,
    onArtworkLoaded: (() -> Unit)? = null,
) {
    val navigator = rememberDomposableNavigator(initialRoute, ShopCodec, browserUrls)

    fun href(route: ShopRoute): String = ShopCodec.urlFor(route, browserUrls)
    val cart by graph.cart.state.collectAsState()
    val artwork: ShopArtwork =
        remember {
            {
                file,
                description,
                modifier,
                onLoaded,
                scale,
                ->
                ShopPhoto(file, description, modifier, onLoaded, scale)
            }
        }
    val entries =
        remember(graph) {
            graph.catalog.entries() + graph.checkout.entries() +
                DomposableDestination(CatalogRoute.Missing::class) {
                    UnavailableScreen(route.path) { navigator.navigate(CatalogRoute.Home) }
                }
        }
    CompositionLocalProvider(
        LocalMetroViewModelFactory provides graph.metroViewModelFactory,
        LocalShopNavigator provides navigator,
        LocalShopArtwork provides artwork,
        LocalShopArtworkLoaded provides onArtworkLoaded,
    ) {
        DomposableTheme(ShopTokens, rememberDomposableMaterialComponents()) {
            DomposableScaffold(topBar = {
                DomposableSurface(color = ShopTokens.colors.primary, modifier = DomposableModifier.fillMaxWidth()) {
                    DomposableText(
                        "Everyday essentials. Free delivery over €100.",
                        DomposableModifier.padding(8.dp),
                        style = DomposableTextStyle.Label,
                        color = ShopTokens.colors.onPrimary,
                    )
                }
                DomposableFlowRow(DomposableModifier.padding(16.dp).fillMaxWidth(), gap = 16.dp) {
                    DomposableText("FIELDWORK", style = DomposableTextStyle.Title, headingLevel = 1)
                    val selected =
                        when (navigator.currentRoute) {
                            CatalogRoute.Home -> 0
                            CheckoutRoute.Cart, CheckoutRoute.Checkout, CheckoutRoute.Confirmation -> 2
                            else -> 1
                        }
                    DomposableNavigationBar(
                        items =
                            listOf(
                                DomposableNavigationItem("Discover", href = href(CatalogRoute.Home)),
                                DomposableNavigationItem("Shop all", href = href(CatalogRoute.Collection)),
                                DomposableNavigationItem("Bag (${cart.count})", href = href(CheckoutRoute.Cart)),
                            ),
                        selectedIndex = selected,
                        onSelect = {
                            navigator.navigate(
                                when (it) {
                                    0 -> CatalogRoute.Home
                                    1 -> CatalogRoute.Collection
                                    else -> CheckoutRoute.Cart
                                },
                            )
                        },
                    )
                }
            }, bottomBar = {
                DomposableColumn(DomposableModifier.padding(12.dp).fillMaxWidth(), gap = 4.dp) {
                    DomposableText("Fewer things. Better days.", style = DomposableTextStyle.Label)
                    DomposableFlowRow(gap = 8.dp) {
                        DomposableLink("About this demo", "https://samuolis.github.io/domposable/shopping/")
                        if (onExit !=
                            null
                        ) {
                            DomposableButton("Playground", onExit)
                        } else {
                            DomposableLink("Playground", "https://samuolis.github.io/domposable/demo/")
                        }
                        DomposableText("Fictional shop · No payments or orders sent", style = DomposableTextStyle.Label)
                    }
                }
            }) {
                DomposableNavHost(navigator, entries, contentModifier.fillMaxWidth().fillMaxHeight())
            }
        }
    }
}

@Composable
private fun ShopPhoto(
    file: String,
    description: String,
    modifier: DomposableModifier,
    onLoaded: (() -> Unit)? = null,
    scale: DomposableImageScale = DomposableImageScale.Fit,
) {
    val uri = remember(file) { Res.getUri("files/$file") }
    var loaded by remember(uri) { mutableStateOf(false) }
    var failed by remember(uri) { mutableStateOf(false) }
    DomposableImage(source = uri, description = description, onLoaded = {
        loaded = true
        onLoaded?.invoke()
    }, onError = {
        failed =
            true
    }, scale = scale, modifier = modifier.testId("$file-${if (loaded) "loaded" else "loading"}"))
    if (failed) DomposableBanner(message = "Image unavailable: $description", isError = true)
}
