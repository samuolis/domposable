package example.shop.checkout.api

import example.shop.catalog.api.*
import io.github.samuolis.domposable.screens.DomposableScreenRegistryBuilder
import kotlinx.coroutines.flow.StateFlow

sealed interface CheckoutRoute : ShopRoute {
    data object Cart : CheckoutRoute
    data object Checkout : CheckoutRoute
    data object Confirmation : CheckoutRoute
}
data class ShopOrder(val totalCents: Int, val itemCount: Int)
data class CartState(
    val products: List<ShopProduct>, val quantities: Map<String, Int> = emptyMap(), val notice: String = "",
    val email: String = "", val name: String = "", val address: String = "", val postalCode: String = "",
    val showErrors: Boolean = false, val lastOrder: ShopOrder? = null, val orderSequence: Int = 0,
) {
    val count get() = quantities.values.sum()
    val subtotal get() = products.sumOf { it.cents * (quantities[it.id] ?: 0) }
    val shipping get() = if (count == 0 || subtotal >= 10000) 0 else 500
    val total get() = subtotal + shipping
    val checkoutValid get() = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim()) && name.isNotBlank() && address.isNotBlank() && postalCode.isNotBlank()
}
sealed interface CheckoutAction {
    data class Add(val id: String) : CheckoutAction
    data class Quantity(val id: String, val delta: Int) : CheckoutAction
    data class Email(val value: String) : CheckoutAction
    data class Name(val value: String) : CheckoutAction
    data class Address(val value: String) : CheckoutAction
    data class PostalCode(val value: String) : CheckoutAction
    data object PlaceOrder : CheckoutAction
}
/** Session state shared across feature entries. The implementation lives in checkout:impl. */
interface ShoppingCart {
    val state: StateFlow<CartState>
    fun dispatch(action: CheckoutAction)
}
interface CheckoutFeature {
    fun register(builder: DomposableScreenRegistryBuilder, artwork: ShopArtwork, onBrowse: () -> Unit, onCart: () -> Unit, onCheckout: () -> Unit, onOrdered: () -> Unit)
}
