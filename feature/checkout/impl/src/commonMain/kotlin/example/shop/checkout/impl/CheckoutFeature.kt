package example.shop.checkout.impl

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zacsweers.metro.*
import example.shop.catalog.api.*
import example.shop.checkout.api.*
import example.shop.checkout.impl.generated.*
import io.github.samuolis.domposable.lifecycle.DomposableViewModelEntry
import io.github.samuolis.domposable.screens.*

@Inject @ContributesBinding(ShopScope::class)
class DefaultCheckoutFeature(private val newCart: () -> CartViewModel, private val newCheckout: () -> CheckoutViewModel, private val cart: ShoppingCart) : CheckoutFeature {
    override fun register(builder: DomposableScreenRegistryBuilder, artwork: ShopArtwork, onBrowse: () -> Unit, onCart: () -> Unit, onCheckout: () -> Unit, onOrdered: () -> Unit) {
        builder.register(cartScreenDestination {
            DomposableViewModelEntry(this) {
                val model = viewModel { newCart() }
                val state by model.state.collectAsState()
                CartScreen(state, model::dispatch, artwork, onBrowse, onCheckout)
            }
        })
        builder.register(checkoutScreenDestination {
            DomposableViewModelEntry(this) {
                val model = viewModel { newCheckout() }
                val state by model.state.collectAsState()
                val latestOrdered by rememberUpdatedState(onOrdered)
                LaunchedEffect(model) { model.effects.collect { if (it == CheckoutEffect.OrderPlaced) latestOrdered() } }
                CheckoutScreen(state, model::dispatch, onCart)
            }
        })
        builder.register(confirmationScreenDestination {
            val state by cart.state.collectAsState()
            ConfirmationScreen(state.lastOrder, onBrowse)
        })
    }
}
