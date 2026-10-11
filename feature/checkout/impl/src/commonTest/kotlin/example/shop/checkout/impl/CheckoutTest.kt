package example.shop.checkout.impl

import example.shop.catalog.api.*
import example.shop.checkout.api.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class CheckoutTest {
    private val products = listOf(
        ShopProduct("bag", "Bag", "Travel", 8900, "bag.png", "Bag"),
        ShopProduct("mug", "Mug", "Home", 2400, "mug.png", "Mug"),
    )
    private val catalog = object : ProductCatalog { override val products = this@CheckoutTest.products }
    @Test fun quantitiesRejectUnknownProductsAndRespectBoundsAndDeliveryThreshold() {
        var state = CartState(products)
        assertEquals(state, reduceCart(state, CheckoutAction.Quantity("unknown", 1)))
        state = reduceCart(state, CheckoutAction.Add("bag"))
        assertEquals(9400, state.total)
        state = reduceCart(state, CheckoutAction.Add("mug"))
        assertEquals(11300, state.total)
        assertEquals(0, state.shipping)
        state = reduceCart(state, CheckoutAction.Quantity("bag", -1))
        assertEquals(2900, state.total)
        state = reduceCart(state, CheckoutAction.Quantity("mug", Int.MAX_VALUE))
        assertEquals(20, state.count)
        state = reduceCart(state, CheckoutAction.Quantity("mug", Int.MIN_VALUE))
        assertEquals(0, state.total)
    }
    @Test fun validationDoesNotClearBagAndSuccessfulOrderClearsPersonalDetails() {
        var state = reduceCart(CartState(products), CheckoutAction.Add("mug"))
        state = reduceCart(state, CheckoutAction.PlaceOrder)
        assertTrue(state.showErrors)
        assertEquals(1, state.count)
        state = state.copy(email = "demo@example.test", name = "Demo", address = "1 Example Street", postalCode = "00000")
        state = reduceCart(state, CheckoutAction.PlaceOrder)
        assertEquals(ShopOrder(2900, 1), state.lastOrder)
        assertEquals(1, state.orderSequence)
        assertEquals(0, state.count)
        assertEquals("", state.email)
        assertEquals("", state.name)
        assertEquals("", state.address)
        assertEquals("", state.postalCode)
        assertEquals(1, reduceCart(state, CheckoutAction.PlaceOrder).orderSequence)
    }
    @Test fun viewModelsShareTheSessionButNavigationEffectIsEmittedOnlyForSuccessfulCheckout() = runTest {
        val cart = InMemoryShoppingCart(catalog)
        val bag = CartViewModel(cart)
        val checkout = CheckoutViewModel(cart)
        bag.dispatch(CheckoutAction.Add("mug"))
        assertEquals(1, checkout.state.value.count)
        checkout.dispatch(CheckoutAction.PlaceOrder)
        assertEquals(0, checkout.state.value.orderSequence)
        checkout.dispatch(CheckoutAction.Email("demo@example.test"))
        checkout.dispatch(CheckoutAction.Name("Demo"))
        checkout.dispatch(CheckoutAction.Address("1 Example Street"))
        checkout.dispatch(CheckoutAction.PostalCode("00000"))
        checkout.dispatch(CheckoutAction.PlaceOrder)
        assertEquals(CheckoutEffect.OrderPlaced, checkout.effects.first())
        assertEquals(0, bag.state.value.count)
        checkout.dispatch(CheckoutAction.PlaceOrder)
        assertEquals(1, checkout.state.value.orderSequence)
    }
}
