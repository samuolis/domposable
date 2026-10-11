package example.shop.catalog.impl

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zacsweers.metro.*
import example.shop.catalog.api.*
import example.shop.checkout.api.CheckoutRoute
import example.shop.catalog.impl.generated.domposableEntries
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.navigation3.DomposableNavigator
import io.github.samuolis.domposable.screens.DomposableDestination

@Inject @ContributesBinding(ShopScope::class)
class DefaultCatalogFeature(private val newCatalog: () -> CatalogViewModel, private val newProduct: ProductViewModel.Factory) : CatalogFeature {
    override fun entries(navigator: DomposableNavigator<ShopRoute>, artwork: ShopArtwork, onArtworkLoaded: (() -> Unit)?): List<DomposableDestination<*>> =
        domposableEntries(newCatalog = newCatalog, navigator = navigator, artwork = artwork, onArtworkLoaded = onArtworkLoaded, newProduct = newProduct)
}

@Composable
@DomposableScreen(route = CatalogRoute.Home::class, viewModel = true)
internal fun Discover(newCatalog: () -> CatalogViewModel, navigator: DomposableNavigator<ShopRoute>, artwork: ShopArtwork, onArtworkLoaded: (() -> Unit)? = null) {
    val model = viewModel { newCatalog() }
    val state by model.state.collectAsState()
    val cart by model.cart.state.collectAsState()
    DiscoverScreen(state.copy(notice = cart.notice), model::dispatch, artwork,
        { navigator.navigate(CatalogRoute.Collection) }, { navigator.navigate(CatalogRoute.Product(it)) }, onArtworkLoaded)
}

@Composable
@DomposableScreen(route = CatalogRoute.Collection::class, viewModel = true)
internal fun Collection(newCatalog: () -> CatalogViewModel, navigator: DomposableNavigator<ShopRoute>, artwork: ShopArtwork) {
    val model = viewModel { newCatalog() }
    val state by model.state.collectAsState()
    val cart by model.cart.state.collectAsState()
    CollectionScreen(state.copy(notice = cart.notice), model::dispatch, artwork,
        { navigator.navigate(CatalogRoute.Collection) }, { navigator.navigate(CatalogRoute.Product(it)) })
}

@Composable
@DomposableScreen(route = CatalogRoute.Product::class, viewModel = true)
internal fun Product(route: CatalogRoute.Product, newProduct: ProductViewModel.Factory, navigator: DomposableNavigator<ShopRoute>, artwork: ShopArtwork) {
    val model = viewModel { newProduct.create(route.id) }
    val cart by model.cart.state.collectAsState()
    ProductScreen(ProductState(model.product, cart.notice), artwork, model::add,
        { navigator.navigate(CatalogRoute.Collection) }, { navigator.navigate(CheckoutRoute.Cart) })
}
