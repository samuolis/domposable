package example.shop.checkout.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.metroViewModel
import example.shop.catalog.api.CatalogRoute
import example.shop.catalog.api.LocalShopArtwork
import example.shop.catalog.api.LocalShopNavigator
import example.shop.catalog.api.ShopScope
import example.shop.checkout.api.CheckoutFeature
import example.shop.checkout.api.CheckoutRoute
import example.shop.checkout.impl.generated.domposableEntries
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.lifecycle.DomposableMviScreen
import io.github.samuolis.domposable.screens.DomposableDestination

@Inject
@ContributesBinding(ShopScope::class)
class DefaultCheckoutFeature : CheckoutFeature {
    override fun entries(): List<DomposableDestination<*>> = domposableEntries()
}

@Composable
@DomposableScreen(CheckoutRoute.Cart::class, viewModel = true)
fun CartScreen(model: CartViewModel = metroViewModel()) {
    val navigator = LocalShopNavigator.current
    val artwork = LocalShopArtwork.current
    DomposableMviScreen(model) { state, onAction ->
        CartContent(
            state,
            onAction,
            artwork,
            { navigator.navigate(CatalogRoute.Collection) },
            { navigator.navigate(CheckoutRoute.Checkout) },
        )
    }
}

@Composable
@DomposableScreen(CheckoutRoute.Checkout::class, viewModel = true)
fun CheckoutScreen(model: CheckoutViewModel = metroViewModel()) {
    val navigator = LocalShopNavigator.current
    LaunchedEffect(model, navigator) {
        model.effects.collect { if (it == CheckoutEffect.OrderPlaced) navigator.replace(CheckoutRoute.Confirmation) }
    }
    DomposableMviScreen(model) { state, onAction ->
        CheckoutContent(state, onAction) { navigator.navigate(CheckoutRoute.Cart) }
    }
}

@Composable
@DomposableScreen(CheckoutRoute.Confirmation::class, viewModel = true)
fun ConfirmationScreen(model: CartViewModel = metroViewModel()) {
    val navigator = LocalShopNavigator.current
    DomposableMviScreen(model) { state, _ ->
        ConfirmationContent(state.lastOrder) { navigator.navigate(CatalogRoute.Collection) }
    }
}
