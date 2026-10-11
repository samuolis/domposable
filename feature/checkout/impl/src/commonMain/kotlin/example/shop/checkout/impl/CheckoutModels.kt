package example.shop.checkout.impl

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import example.shop.catalog.api.ProductCatalog
import example.shop.catalog.api.ShopScope
import example.shop.checkout.api.CartState
import example.shop.checkout.api.CheckoutAction
import example.shop.checkout.api.ShopOrder
import example.shop.checkout.api.ShoppingCart
import io.github.samuolis.domposable.lifecycle.DomposableMvi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.none
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

@Inject
@SingleIn(ShopScope::class)
@ContributesBinding(ShopScope::class)
class InMemoryShoppingCart(
    catalog: ProductCatalog,
) : ShoppingCart {
    private val mutableState = MutableStateFlow(CartState(catalog.products))
    override val state = mutableState.asStateFlow()

    override fun dispatch(action: CheckoutAction) {
        mutableState.update { reduceCart(it, action) }
    }
}

fun reduceCart(
    state: CartState,
    action: CheckoutAction,
): CartState =
    when (action) {
        is CheckoutAction.Add -> {
            val product = state.products.firstOrNull { it.id == action.id }
            if (product ==
                null
            ) {
                state
            } else {
                reduceCart(
                    state,
                    CheckoutAction.Quantity(product.id, 1),
                ).copy(notice = "${product.name} added to your bag")
            }
        }

        is CheckoutAction.Quantity -> {
            if (state.products.none { it.id == action.id }) {
                state
            } else {
                val next = ((state.quantities[action.id] ?: 0).toLong() + action.delta).coerceIn(0, 20).toInt()
                state.copy(
                    quantities =
                        if (next ==
                            0
                        ) {
                            state.quantities - action.id
                        } else {
                            state.quantities + (action.id to next)
                        },
                )
            }
        }

        is CheckoutAction.Email -> {
            state.copy(email = action.value)
        }

        is CheckoutAction.Name -> {
            state.copy(name = action.value)
        }

        is CheckoutAction.Address -> {
            state.copy(address = action.value)
        }

        is CheckoutAction.PostalCode -> {
            state.copy(postalCode = action.value)
        }

        CheckoutAction.PlaceOrder -> {
            if (!state.checkoutValid || state.count == 0) {
                state.copy(showErrors = true)
            } else {
                CartState(
                    state.products,
                    lastOrder = ShopOrder(state.total, state.count),
                    orderSequence =
                        state.orderSequence + 1,
                )
            }
        }
    }

@Inject @ViewModelKey
@ContributesIntoMap(ShopScope::class, binding<ViewModel>())
class CartViewModel(
    val cart: ShoppingCart,
) : ViewModel(),
    DomposableMvi<CartState, CheckoutAction> {
    override val state = cart.state

    override fun dispatch(action: CheckoutAction) {
        cart.dispatch(action)
    }
}

sealed interface CheckoutEffect {
    data object OrderPlaced : CheckoutEffect
}

@Inject @ViewModelKey
@ContributesIntoMap(ShopScope::class, binding<ViewModel>())
class CheckoutViewModel(
    val cart: ShoppingCart,
) : ViewModel(),
    DomposableMvi<CartState, CheckoutAction> {
    override val state = cart.state
    private val events = Channel<CheckoutEffect>(Channel.BUFFERED)
    val effects = events.receiveAsFlow()

    override fun dispatch(action: CheckoutAction) {
        val before = state.value.orderSequence
        cart.dispatch(action)
        if (action == CheckoutAction.PlaceOrder &&
            state.value.orderSequence != before
        ) {
            events.trySend(CheckoutEffect.OrderPlaced)
        }
    }

    override fun onCleared() {
        events.close()
    }
}
