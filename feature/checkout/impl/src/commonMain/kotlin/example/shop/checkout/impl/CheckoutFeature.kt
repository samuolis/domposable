package example.shop.checkout.impl

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zacsweers.metro.*
import example.shop.catalog.api.*
import example.shop.checkout.api.*
import example.shop.checkout.impl.generated.domposableEntries
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.navigation3.DomposableNavigator
import io.github.samuolis.domposable.screens.DomposableDestination

@Inject @ContributesBinding(ShopScope::class)
class DefaultCheckoutFeature(private val newCart: () -> CartViewModel, private val newCheckout: () -> CheckoutViewModel, private val cart: ShoppingCart) : CheckoutFeature {
    override fun entries(navigator: DomposableNavigator<ShopRoute>, artwork: ShopArtwork): List<DomposableDestination<*>> =
        domposableEntries(newCart = newCart, navigator = navigator, artwork = artwork, newCheckout = newCheckout, cart = cart)
}

@Composable
@DomposableScreen(route = CheckoutRoute.Cart::class, viewModel = true)
internal fun Cart(newCart: () -> CartViewModel, navigator: DomposableNavigator<ShopRoute>, artwork: ShopArtwork) {
    val model = viewModel { newCart() }
    val state by model.state.collectAsState()
    CartScreen(state, model::dispatch, artwork,
        { navigator.navigate(CatalogRoute.Collection) }, { navigator.navigate(CheckoutRoute.Checkout) })
}

@Composable
@DomposableScreen(route = CheckoutRoute.Checkout::class, viewModel = true)
internal fun Checkout(newCheckout: () -> CheckoutViewModel, navigator: DomposableNavigator<ShopRoute>) {
    val model = viewModel { newCheckout() }
    val state by model.state.collectAsState()
    LaunchedEffect(model, navigator) {
        model.effects.collect { if (it == CheckoutEffect.OrderPlaced) navigator.navigate(CheckoutRoute.Confirmation) }
    }
    CheckoutScreen(state, model::dispatch) { navigator.navigate(CheckoutRoute.Cart) }
}

@Composable
@DomposableScreen(route = CheckoutRoute.Confirmation::class)
internal fun Confirmation(cart: ShoppingCart, navigator: DomposableNavigator<ShopRoute>) {
    val state by cart.state.collectAsState()
    ConfirmationScreen(state.lastOrder) { navigator.navigate(CatalogRoute.Collection) }
}
