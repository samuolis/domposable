package io.github.samuolis.domposable.demo

import androidx.compose.runtime.*
import io.github.samuolis.domposable.adaptive.*
import io.github.samuolis.domposable.foundation.*
import io.github.samuolis.domposable.material.*
import io.github.samuolis.domposable.navigation3.*
import io.github.samuolis.domposable.screens.*
import io.github.samuolis.domposable.ui.*
import io.github.samuolis.domposable.demo.generated.resources.Res

/** Fictional catalogue. Money uses integer cents, never floating point. */
data class ShopProduct(val id: String, val name: String, val category: String, val cents: Int, val image: String, val description: String)
val ShopProducts = listOf(
    ShopProduct("canvas-weekender", "Canvas weekender", "Travel", 8900, "shop-bag.png", "Room for a weekend away. Durable cotton canvas, considered pockets and comfortable carry handles."),
    ShopProduct("studio-mug", "Studio mug", "Home", 2400, "shop-mug.png", "A slower start to your morning. A generous, hand-finished stoneware mug with a softly speckled glaze."),
    ShopProduct("clay-table-lamp", "Clay table lamp", "Home", 12900, "shop-lamp.png", "Warm light for your favourite corner. A sculptural clay base paired with a natural linen shade."),
)
fun shopPrice(cents: Int): String = "€${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
data class ShopOrder(val totalCents: Int, val itemCount: Int)

/** App-owned state: replace this with an injected model, StateFlow, MVVM or MVI. */
class ShopModel {
    val quantities = mutableStateMapOf<String, Int>()
    var category by mutableStateOf("All")
    var search by mutableStateOf("")
    var priceAscending by mutableStateOf(false)
    var notice by mutableStateOf("")
    var email by mutableStateOf("")
    var name by mutableStateOf("")
    var address by mutableStateOf("")
    var postalCode by mutableStateOf("")
    var showErrors by mutableStateOf(false)
    var lastOrder by mutableStateOf<ShopOrder?>(null)
    val count get() = quantities.values.sum()
    val subtotal get() = ShopProducts.sumOf { it.cents * (quantities[it.id] ?: 0) }
    val shipping get() = if (count == 0 || subtotal >= 10000) 0 else 500
    val total get() = subtotal + shipping
    val checkoutValid get() = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim()) && name.isNotBlank() && address.isNotBlank() && postalCode.isNotBlank()
    val visibleProducts get() = ShopProducts.filter {
        (category == "All" || it.category == category) && (it.name + " " + it.description).contains(search.trim(), ignoreCase = true)
    }.let { if (priceAscending) it.sortedBy(ShopProduct::cents) else it }
    fun changeQuantity(id: String, delta: Int) {
        if (ShopProducts.none { it.id == id }) return
        val next = ((quantities[id] ?: 0) + delta).coerceIn(0, 20)
        if (next == 0) quantities.remove(id) else quantities[id] = next
    }
    fun add(product: ShopProduct) { changeQuantity(product.id, 1); notice = "${product.name} added to your bag" }
    fun placeDemoOrder(): Boolean {
        showErrors = true
        if (!checkoutValid || count == 0) return false
        lastOrder = ShopOrder(total, count)
        quantities.clear()
        email = ""; name = ""; address = ""; postalCode = ""; showErrors = false
        notice = ""
        return true
    }
}
sealed interface ShopRoute {
    data object Home : ShopRoute
    data object Collection : ShopRoute
    data class Product(val id: String) : ShopRoute
    data object Cart : ShopRoute
    data object Checkout : ShopRoute
    data object Confirmation : ShopRoute
    data class Missing(val path: String) : ShopRoute
}
object ShopCodec : DomposableRouteCodec<ShopRoute> {
    override fun encode(route: ShopRoute) = when (route) {
        ShopRoute.Home -> "/shop"
        ShopRoute.Collection -> "/shop/collection"
        is ShopRoute.Product -> "/shop/products/${route.id}"
        ShopRoute.Cart -> "/shop/cart"
        ShopRoute.Checkout -> "/shop/checkout"
        ShopRoute.Confirmation -> "/shop/confirmation"
        is ShopRoute.Missing -> route.path
    }
    override fun decode(path: String): ShopRoute = when {
        path == "/shop" || path == "/" -> ShopRoute.Home
        path == "/shop/collection" -> ShopRoute.Collection
        path.startsWith("/shop/products/") && path.removePrefix("/shop/products/").matches(Regex("[a-z0-9-]+")) -> ShopRoute.Product(path.removePrefix("/shop/products/"))
        path == "/shop/cart" -> ShopRoute.Cart
        path == "/shop/checkout" -> ShopRoute.Checkout
        path == "/shop/confirmation" -> ShopRoute.Confirmation
        else -> ShopRoute.Missing(path)
    }
}
private val ShopTokens = DomposableTokens(
    colors = DomposableColors(primary = DomposableColor(0xFF354C3C), surface = DomposableColor(0xFFFAF8F3), onSurface = DomposableColor(0xFF292E28), surfaceVariant = DomposableColor(0xFFEAE7DE), outline = DomposableColor(0xFF74776E)),
    typography = DomposableTypography(titleSize = 30f), radius = 8.dp,
)

@Composable
fun ShoppingApp(model: ShopModel = remember { ShopModel() }, initialRoute: ShopRoute = ShopRoute.Home, browserUrls: DomposableBrowserUrls = DomposableBrowserUrls.Hash, onExit: (() -> Unit)? = null, onArtworkLoaded: (() -> Unit)? = null) {
    val stack = rememberDomposableBackStack<ShopRoute>(initialRoute)
    fun open(route: ShopRoute) { if (stack.last().route != route) stack.add(DomposableEntryScope(route)) }
    fun href(route: ShopRoute): String = when (val urls = browserUrls) {
        DomposableBrowserUrls.Hash -> "#${ShopCodec.encode(route)}"
        is DomposableBrowserUrls.Path -> urls.basePath.trimEnd('/') + ShopCodec.encode(route) + if (urls.trailingSlash) "/" else ""
    }
    val screens = remember(stack, model, onArtworkLoaded) { domposableScreenRegistry {
        register(ShopRoute.Home::class) { StorefrontScreen(model, true, { open(ShopRoute.Collection) }, { open(ShopRoute.Product(it)) }, onArtworkLoaded) }
        register(ShopRoute.Collection::class) { StorefrontScreen(model, false, { open(ShopRoute.Collection) }, { open(ShopRoute.Product(it)) }) }
        register(ShopRoute.Product::class) { ProductScreen(model, route.id, { open(ShopRoute.Collection) }, { open(ShopRoute.Cart) }) }
        register(ShopRoute.Cart::class) { CartScreen(model, { open(ShopRoute.Collection) }, { open(ShopRoute.Checkout) }) }
        register(ShopRoute.Checkout::class) { CheckoutScreen(model, { open(ShopRoute.Cart) }, { open(ShopRoute.Confirmation) }) }
        register(ShopRoute.Confirmation::class) { ConfirmationScreen(model.lastOrder) { open(ShopRoute.Collection) } }
        register(ShopRoute.Missing::class) { UnavailableScreen(route.path) { open(ShopRoute.Home) } }
    } }
    DomposableTheme(ShopTokens, rememberDomposableMaterialComponents()) {
        DomposableBrowserHistory(stack, ShopCodec, browserUrls)
        DomposableScaffold(topBar = {
            DomposableSurface(DomposableSurfaceProps(ShopTokens.colors.primary), DomposableModifier.fillMaxWidth()) {
                DomposableText("Everyday essentials. Free delivery over €100.", DomposableModifier.padding(8.dp), style = DomposableTextStyle.Label, color = ShopTokens.colors.onPrimary)
            }
            DomposableFlowRow(DomposableModifier.padding(16.dp).fillMaxWidth(), gap = 16.dp) {
                DomposableText("FIELDWORK", style = DomposableTextStyle.Title, headingLevel = 1)
                val selected = when (stack.last().route) { ShopRoute.Home -> 0; ShopRoute.Cart, ShopRoute.Checkout, ShopRoute.Confirmation -> 2; else -> 1 }
                DomposableNavigationBar(DomposableNavigationBarProps(
                    listOf(DomposableNavigationItem("Discover", href = href(ShopRoute.Home)), DomposableNavigationItem("Shop all", href = href(ShopRoute.Collection)), DomposableNavigationItem("Bag (${model.count})", href = href(ShopRoute.Cart))),
                    selected, { open(when (it) { 0 -> ShopRoute.Home; 1 -> ShopRoute.Collection; else -> ShopRoute.Cart }) },
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

@Composable
fun StorefrontScreen(model: ShopModel, showHero: Boolean, onBrowse: () -> Unit, onProduct: (String) -> Unit, onArtworkLoaded: (() -> Unit)? = null) {
    DomposableMeasure(DomposableModifier.fillMaxWidth().fillMaxHeight()) { size ->
        // Wait for real constraints before starting size-dependent image requests.
        if (size.width <= 0f) return@DomposableMeasure
        val wide = size.width >= 800
        val inset = if (wide) 32 else 16
        val columns = when { size.width >= 1000 -> 3; size.width >= 600 -> 2; else -> 1 }
        val cardWidth = ((size.width - inset * 2 - 16 * (columns - 1)) / columns).coerceAtLeast(1f).dp
        DomposableColumn(DomposableModifier.fillMaxWidth().fillMaxHeight().verticalScroll().padding(inset.dp), gap = 24.dp) {
            if (showHero) {
                val copy: @Composable () -> Unit = {
                    DomposableColumn(DomposableModifier.width((if (wide) (size.width - 80) * .38f else size.width - inset * 2).coerceAtLeast(1f).dp), gap = 16.dp) {
                        DomposableText("THE EVERYDAY EDIT", style = DomposableTextStyle.Label)
                        DomposableText("Made for the way you live.", style = DomposableTextStyle.Title, headingLevel = 2)
                        DomposableText("Thoughtful objects for home, work and the places in between. Find your next everyday favourite.")
                        DomposableButton("Explore the collection", onBrowse)
                        DomposableText("Considered materials · Useful by design", style = DomposableTextStyle.Label)
                    }
                }
                val photo: @Composable () -> Unit = { ShopPhoto("shop-hero.png", "Canvas bag, linen, ceramics and warm studio light", DomposableModifier.width((if (wide) (size.width - 80) * .62f else size.width - inset * 2).coerceAtLeast(1f).dp).height((if (wide) 330 else 240).dp), onArtworkLoaded, DomposableImageScale.Crop) }
                if (wide) DomposableRow(gap = 16.dp) { copy(); photo() } else DomposableColumn(gap = 16.dp) { photo(); copy() }
            }
            DomposableText(if (showHero) "Everyday favourites" else "Shop the collection", style = DomposableTextStyle.Title, headingLevel = 2)
            DomposableFlowRow(gap = 12.dp) {
                DomposableTextField(model.search, { model.search = it }, "Search products", modifier = DomposableModifier.width(240.dp).testId("shop-search"), purpose = DomposableInputPurpose.Search)
                DomposableSwitch(DomposableSwitchProps(model.priceAscending, { model.priceAscending = it }, "Price: low to high"))
            }
            DomposableFlowRow(gap = 8.dp) { listOf("All", "Home", "Travel").forEach { category -> DomposableChip(DomposableChipProps(category, { model.category = category }, model.category == category)) } }
            if (model.notice.isNotEmpty()) DomposableBanner(DomposableBannerProps(model.notice))
            DomposableText("${model.visibleProducts.size} thoughtfully chosen pieces", style = DomposableTextStyle.Label)
            if (model.visibleProducts.isEmpty()) {
                DomposableText("No products match your search.")
                DomposableButton("Clear filters", { model.search = ""; model.category = "All" })
            }
            DomposableFlowRow(gap = 16.dp) { model.visibleProducts.forEach { product -> key(product.id) {
                DomposableCard(DomposableModifier.width(cardWidth)) {
                    DomposableColumn(DomposableModifier.padding(16.dp).fillMaxWidth(), gap = 8.dp) {
                        ShopPhoto(product.image, product.name, DomposableModifier.fillMaxWidth().height(220.dp))
                        DomposableText(product.category.uppercase(), style = DomposableTextStyle.Label)
                        DomposableText(product.name, style = DomposableTextStyle.Title, headingLevel = 3)
                        DomposableText(shopPrice(product.cents))
                        DomposableFlowRow(gap = 8.dp) {
                            DomposableButton("View details", { onProduct(product.id) }, DomposableModifier.contentDescription("View ${product.name}"))
                            DomposableButton("Add to bag", { model.add(product) }, DomposableModifier.contentDescription("Add ${product.name} to bag"))
                        }
                    }
                }
            } } }
        }
    }
}

@Composable
fun ProductScreen(model: ShopModel, id: String, onBrowse: () -> Unit, onCart: () -> Unit) {
    val product = ShopProducts.firstOrNull { it.id == id }
    DomposableColumn(DomposableModifier.fillMaxWidth().fillMaxHeight().verticalScroll().padding(24.dp), gap = 16.dp) {
        DomposableButton("Back to collection", onBrowse)
        if (product == null) DomposableText("This product is unavailable.") else {
            DomposableText(product.category.uppercase(), style = DomposableTextStyle.Label)
            DomposableText(product.name, style = DomposableTextStyle.Title, headingLevel = 2)
            ShopPhoto(product.image, product.name, DomposableModifier.fillMaxWidth().height(300.dp))
            DomposableText(shopPrice(product.cents), style = DomposableTextStyle.Title)
            DomposableText(product.description)
            DomposableText("In stock · Dispatches in 2–3 working days", style = DomposableTextStyle.Label)
            DomposableFlowRow(gap = 12.dp) { DomposableButton("Add to bag", { model.add(product) }); DomposableButton("View bag", onCart) }
            if (model.notice.isNotEmpty()) DomposableBanner(DomposableBannerProps(model.notice))
            DomposableDivider()
            DomposableText("The details", style = DomposableTextStyle.Title, headingLevel = 3)
            DomposableText("Designed to be used every day. Carefully chosen materials and simple, useful details. This is a fictional product in an open source example.")
        }
    }
}

@Composable
private fun OrderSummary(model: ShopModel) {
    DomposableCard(DomposableModifier.fillMaxWidth()) {
        DomposableColumn(DomposableModifier.padding(16.dp), gap = 8.dp) {
            DomposableText("Order summary", style = DomposableTextStyle.Title, headingLevel = 3)
            DomposableText("Subtotal  ${shopPrice(model.subtotal)}")
            DomposableText("Delivery  ${if (model.shipping == 0) "Free" else shopPrice(model.shipping)}")
            DomposableDivider()
            DomposableText("Total  ${shopPrice(model.total)}", style = DomposableTextStyle.Title)
        }
    }
}

@Composable
fun CartScreen(model: ShopModel, onBrowse: () -> Unit, onCheckout: () -> Unit) {
    DomposableColumn(DomposableModifier.fillMaxWidth().fillMaxHeight().verticalScroll().padding(24.dp), gap = 16.dp) {
        DomposableText("Your bag", style = DomposableTextStyle.Title, headingLevel = 2)
        DomposableButton("Continue shopping", onBrowse)
        if (model.count == 0) DomposableText("Your bag is empty. Find something for your everyday.") else {
            ShopProducts.filter { (model.quantities[it.id] ?: 0) > 0 }.forEach { product -> key(product.id) {
                DomposableCard(DomposableModifier.fillMaxWidth()) {
                    DomposableFlowRow(DomposableModifier.padding(16.dp), gap = 16.dp) {
                        ShopPhoto(product.image, product.name, DomposableModifier.width(96.dp).height(96.dp))
                        DomposableColumn(gap = 8.dp) {
                            DomposableText(product.name, style = DomposableTextStyle.Title, headingLevel = 3)
                            DomposableText(shopPrice(product.cents))
                            DomposableFlowRow(gap = 8.dp) {
                                DomposableButton("−", { model.changeQuantity(product.id, -1) }, DomposableModifier.contentDescription("Decrease ${product.name}"))
                                DomposableText("Quantity: ${model.quantities[product.id]}")
                                DomposableButton("+", { model.changeQuantity(product.id, 1) }, DomposableModifier.contentDescription("Increase ${product.name}"), enabled = (model.quantities[product.id] ?: 0) < 20)
                            }
                        }
                    }
                }
            } }
            OrderSummary(model)
            DomposableButton("Continue to checkout", onCheckout)
        }
    }
}

@Composable
fun CheckoutScreen(model: ShopModel, onCart: () -> Unit, onOrdered: () -> Unit) {
    DomposableColumn(DomposableModifier.fillMaxWidth().fillMaxHeight().verticalScroll().padding(24.dp), gap = 16.dp) {
        DomposableText("Checkout", style = DomposableTextStyle.Title, headingLevel = 2)
        DomposableButton("Back to bag", onCart)
        DomposableBanner(DomposableBannerProps("Demo checkout. Use fictional details. No payment is collected and nothing is sent."))
        if (model.count == 0) DomposableText("Add something to your bag before checking out.") else {
            OrderSummary(model)
            DomposableTextField(model.email, { model.email = it }, "Email address", modifier = DomposableModifier.testId("shop-email").fillMaxWidth(), purpose = DomposableInputPurpose.Email, error = if (model.showErrors && !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(model.email.trim())) "Enter a valid email" else null)
            DomposableTextField(model.name, { model.name = it }, "Full name", modifier = DomposableModifier.fillMaxWidth(), error = if (model.showErrors && model.name.isBlank()) "Enter your name" else null)
            DomposableTextField(model.address, { model.address = it }, "Street address", modifier = DomposableModifier.fillMaxWidth(), error = if (model.showErrors && model.address.isBlank()) "Enter an address" else null)
            DomposableTextField(model.postalCode, { model.postalCode = it }, "Postal code", modifier = DomposableModifier.fillMaxWidth(), error = if (model.showErrors && model.postalCode.isBlank()) "Enter a postal code" else null)
            DomposableButton("Place demo order", { if (model.placeDemoOrder()) onOrdered() })
        }
    }
}

@Composable
fun ConfirmationScreen(order: ShopOrder?, onBrowse: () -> Unit) {
    DomposableColumn(DomposableModifier.fillMaxWidth().padding(24.dp), gap = 16.dp) {
        DomposableText(if (order == null) "No order to show" else "Thanks for exploring Fieldwork.", style = DomposableTextStyle.Title, headingLevel = 2)
        if (order != null) DomposableText("Demo order: ${order.itemCount} pieces · ${shopPrice(order.totalCents)}. Nothing was purchased or sent.")
        DomposableButton("Keep exploring", onBrowse)
    }
}
