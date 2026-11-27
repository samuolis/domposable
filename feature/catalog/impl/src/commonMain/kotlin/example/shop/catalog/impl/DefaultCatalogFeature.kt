package example.shop.catalog.impl

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import example.shop.catalog.api.CatalogFeature
import example.shop.catalog.api.CatalogRoute
import example.shop.catalog.api.LocalShopArtwork
import example.shop.catalog.api.LocalShopArtworkLoaded
import example.shop.catalog.api.LocalShopNavigator
import example.shop.catalog.api.ProductAction
import example.shop.catalog.api.ProductState
import example.shop.catalog.api.ShopScope
import example.shop.catalog.impl.generated.domposableEntries
import example.shop.checkout.api.CheckoutRoute
import io.github.samuolis.domposable.foundation.DomposableMviDestination
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.lifecycle.DomposableMviScreen
import io.github.samuolis.domposable.screens.DomposableDestination

@Inject
@ContributesBinding(ShopScope::class)
class DefaultCatalogFeature : CatalogFeature {
    override fun entries(): List<DomposableDestination<*>> = domposableEntries()
}

@Composable
@DomposableScreen(CatalogRoute.Home::class, viewModel = true)
fun DiscoverScreen(model: CatalogViewModel = metroViewModel()) {
    val navigator = LocalShopNavigator.current
    val artwork = LocalShopArtwork.current
    val onArtworkLoaded = LocalShopArtworkLoaded.current
    DomposableMviScreen(model) { state, onAction ->
        StorefrontContent(
            state,
            onAction,
            artwork,
            true,
            { navigator.navigate(CatalogRoute.Collection) },
            { navigator.navigate(CatalogRoute.Product(it)) },
            onArtworkLoaded,
        )
    }
}

@Composable
@DomposableScreen(CatalogRoute.Collection::class, viewModel = true)
fun CollectionScreen(model: CatalogViewModel = metroViewModel()) {
    val navigator = LocalShopNavigator.current
    val artwork = LocalShopArtwork.current
    DomposableMviScreen(model) { state, onAction ->
        StorefrontContent(
            state,
            onAction,
            artwork,
            false,
            { navigator.navigate(CatalogRoute.Collection) },
            { navigator.navigate(CatalogRoute.Product(it)) },
        )
    }
}

object ProductBinding {
    @Composable
    fun model(route: CatalogRoute.Product): ProductViewModel =
        assistedMetroViewModel<ProductViewModel, ProductViewModel.Factory> { create(route.id) }
}

@Composable
@DomposableMviDestination(CatalogRoute.Product::class, ProductBinding::class)
fun ProductScreen(
    state: ProductState,
    onAction: (ProductAction) -> Unit,
) {
    val navigator = LocalShopNavigator.current
    val artwork = LocalShopArtwork.current
    ProductContent(
        state,
        artwork,
        { onAction(ProductAction.Add) },
        { navigator.navigate(CatalogRoute.Collection) },
        { navigator.navigate(CheckoutRoute.Cart) },
    )
}
