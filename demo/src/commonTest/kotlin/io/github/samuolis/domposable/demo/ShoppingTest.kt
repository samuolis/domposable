package io.github.samuolis.domposable.demo

import example.shop.catalog.api.*
import example.shop.checkout.api.*
import kotlin.test.*

class ShoppingTest {
    @Test fun routesRoundTripAndUnknownPathsRemainExplicit() {
        for (route in listOf(CatalogRoute.Home, CatalogRoute.Collection, CatalogRoute.Product("canvas-weekender"), CheckoutRoute.Cart, CheckoutRoute.Checkout, CheckoutRoute.Confirmation))
            assertEquals(route, ShopCodec.decode(ShopCodec.encode(route)))
        assertIs<CatalogRoute.Missing>(ShopCodec.decode("/shop/wrong"))
    }
    @Test fun applicationGraphsOwnIndependentShoppingSessions() {
        val first = createShopGraph()
        first.cart.dispatch(CheckoutAction.Add(first.products.products.first().id))
        assertEquals(1, first.cart.state.value.count)
        assertEquals(0, createShopGraph().cart.state.value.count)
    }
}
