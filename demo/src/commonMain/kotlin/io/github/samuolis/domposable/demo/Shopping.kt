package io.github.samuolis.domposable.demo

import androidx.compose.runtime.*
import io.github.samuolis.domposable.adaptive.*
import io.github.samuolis.domposable.foundation.*
import io.github.samuolis.domposable.material.*
import io.github.samuolis.domposable.navigation3.*
import io.github.samuolis.domposable.screens.*
import io.github.samuolis.domposable.ui.*
import io.github.samuolis.domposable.demo.generated.resources.Res

import dev.zacsweers.metro.*
import example.shop.catalog.api.*
import example.shop.checkout.api.*

/** Application composition root aggregates contributions from both implementation modules. */
@DependencyGraph(ShopScope::class)
abstract class ShopGraph {
    abstract val catalog: CatalogFeature
    abstract val checkout: CheckoutFeature
    abstract val cart: ShoppingCart
    abstract val products: ProductCatalog
}
fun createShopGraph(): ShopGraph = createGraph<ShopGraph>()

object ShopCodec : DomposableRouteCodec<ShopRoute> {
    override fun encode(route: ShopRoute) = when (route) {
        CatalogRoute.Home -> "/shop"
        CatalogRoute.Collection -> "/shop/collection"
        is CatalogRoute.Product -> "/shop/products/${route.id}"
        CheckoutRoute.Cart -> "/shop/cart"
        CheckoutRoute.Checkout -> "/shop/checkout"
        CheckoutRoute.Confirmation -> "/shop/confirmation"
        is CatalogRoute.Missing -> route.path
        else -> error("Unknown shop route: $route")
    }
    override fun decode(path: String): ShopRoute = when {
        path == "/shop" || path == "/" -> CatalogRoute.Home
        path == "/shop/collection" -> CatalogRoute.Collection
        path.startsWith("/shop/products/") && path.removePrefix("/shop/products/").matches(Regex("[a-z0-9-]+")) -> CatalogRoute.Product(path.removePrefix("/shop/products/"))
        path == "/shop/cart" -> CheckoutRoute.Cart
        path == "/shop/checkout" -> CheckoutRoute.Checkout
        path == "/shop/confirmation" -> CheckoutRoute.Confirmation
        else -> CatalogRoute.Missing(path)
    }
}
private val ShopTokens = DomposableTokens(
    colors = DomposableColors(primary = DomposableColor(0xFF354C3C), surface = DomposableColor(0xFFFAF8F3), onSurface = DomposableColor(0xFF292E28), surfaceVariant = DomposableColor(0xFFEAE7DE), outline = DomposableColor(0xFF74776E)),
    typography = DomposableTypography(titleSize = 30f), radius = 8.dp,
)

@Composable
fun ShoppingApp(graph: ShopGraph = remember { createShopGraph() }, initialRoute: ShopRoute = CatalogRoute.Home, browserUrls: DomposableBrowserUrls = DomposableBrowserUrls.Hash, onExit: (() -> Unit)? = null, onArtworkLoaded: (() -> Unit)? = null) {
    val stack = rememberDomposableBackStack<ShopRoute>(initialRoute)
    fun open(route: ShopRoute) { if (stack.last().route != route) stack.add(DomposableEntryScope(route)) }
    fun href(route: ShopRoute): String = when (val urls = browserUrls) {
        DomposableBrowserUrls.Hash -> "#${ShopCodec.encode(route)}"
        is DomposableBrowserUrls.Path -> urls.basePath.trimEnd('/') + ShopCodec.encode(route) + if (urls.trailingSlash) "/" else ""
    }
    val cart by graph.cart.state.collectAsState()
    val artwork: ShopArtwork = remember { { file, description, modifier, onLoaded, scale -> ShopPhoto(file, description, modifier, onLoaded, scale) } }
    val screens = remember(stack, graph, onArtworkLoaded) { domposableScreenRegistry {
        graph.catalog.register(this, artwork, { open(CatalogRoute.Collection) }, { open(CatalogRoute.Product(it)) }, { open(CheckoutRoute.Cart) }, onArtworkLoaded)
        graph.checkout.register(this, artwork, { open(CatalogRoute.Collection) }, { open(CheckoutRoute.Cart) }, { open(CheckoutRoute.Checkout) }, { open(CheckoutRoute.Confirmation) })
        register(CatalogRoute.Missing::class) { UnavailableScreen(route.path) { open(CatalogRoute.Home) } }
    } }
    DomposableTheme(ShopTokens, rememberDomposableMaterialComponents()) {
        DomposableBrowserHistory(stack, ShopCodec, browserUrls)
        DomposableScaffold(topBar = {
            DomposableSurface(DomposableSurfaceProps(ShopTokens.colors.primary), DomposableModifier.fillMaxWidth()) {
                DomposableText("Everyday essentials. Free delivery over €100.", DomposableModifier.padding(8.dp), style = DomposableTextStyle.Label, color = ShopTokens.colors.onPrimary)
            }
            DomposableFlowRow(DomposableModifier.padding(16.dp).fillMaxWidth(), gap = 16.dp) {
                DomposableText("FIELDWORK", style = DomposableTextStyle.Title, headingLevel = 1)
                val selected = when (stack.last().route) { CatalogRoute.Home -> 0; CheckoutRoute.Cart, CheckoutRoute.Checkout, CheckoutRoute.Confirmation -> 2; else -> 1 }
                DomposableNavigationBar(DomposableNavigationBarProps(
                    listOf(DomposableNavigationItem("Discover", href = href(CatalogRoute.Home)), DomposableNavigationItem("Shop all", href = href(CatalogRoute.Collection)), DomposableNavigationItem("Bag (${cart.count})", href = href(CheckoutRoute.Cart))),
                    selected, { open(when (it) { 0 -> CatalogRoute.Home; 1 -> CatalogRoute.Collection; else -> CheckoutRoute.Cart }) },
                ))
            }
        }, bottomBar = {
            DomposableColumn(DomposableModifier.padding(12.dp).fillMaxWidth(), gap = 4.dp) {
                DomposableText("Fewer things. Better days.", style = DomposableTextStyle.Label)
                DomposableFlowRow(gap = 8.dp) {
                    DomposableLink("About this demo", "https://samuolis.github.io/domposable/shopping/")
                    if (onExit != null) DomposableButton("Playground", onExit) else DomposableLink("Playground", "https://samuolis.github.io/domposable/demo/")
                    DomposableText("Fictional shop · No payments or orders sent", style = DomposableTextStyle.Label)
                }
            }
        }) {
            DomposableNavigation3Host(stack, screens, contentModifier.fillMaxWidth().fillMaxHeight()) { if (stack.size > 1) stack.removeLast() }
        }
    }
}

@Composable
private fun ShopPhoto(file: String, description: String, modifier: DomposableModifier, onLoaded: (() -> Unit)? = null, scale: DomposableImageScale = DomposableImageScale.Fit) {
    val uri = remember(file) { Res.getUri("files/$file") }
    var loaded by remember(uri) { mutableStateOf(false) }
    var failed by remember(uri) { mutableStateOf(false) }
    DomposableImage(DomposableImageProps(uri, description, onLoaded = { loaded = true; onLoaded?.invoke() }, onError = { failed = true }, scale = scale), modifier.testId("$file-${if (loaded) "loaded" else "loading"}"))
    if (failed) DomposableBanner(DomposableBannerProps("Image unavailable: $description", true))
}
