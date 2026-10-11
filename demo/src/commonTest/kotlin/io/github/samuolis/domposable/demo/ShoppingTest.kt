package io.github.samuolis.domposable.demo

import kotlin.test.*

class ShoppingTest {
    @Test fun quantitiesAndDeliveryThreshold() {
        val model = ShopModel()
        model.changeQuantity("unknown", 1)
        assertEquals(0, model.count)
        model.add(ShopProducts[0])
        assertEquals(9400, model.total)
        model.add(ShopProducts[1])
        assertEquals(11300, model.total)
        assertEquals(0, model.shipping)
        model.changeQuantity(ShopProducts[0].id, -1)
        assertEquals(2900, model.total)
        model.changeQuantity(ShopProducts[1].id, -1)
        assertEquals(0, model.total)
    }
    @Test fun checkoutValidatesAndClearsPersonalDetails() {
        val model = ShopModel()
        assertFalse(model.placeDemoOrder())
        model.add(ShopProducts[1])
        model.email = "bad"; model.name = "Demo Customer"; model.address = "1 Example Street"; model.postalCode = "00000"
        assertFalse(model.placeDemoOrder())
        assertEquals(1, model.count)
        model.email = "demo@example.test"
        assertTrue(model.placeDemoOrder())
        assertEquals(ShopOrder(2900, 1), model.lastOrder)
        assertEquals(0, model.count)
        assertEquals("", model.email)
        assertFalse(model.placeDemoOrder())
    }
    @Test fun filtersAndRoutesSurviveIndependentModels() {
        val model = ShopModel()
        model.category = "Home"
        model.priceAscending = true
        assertEquals(listOf("studio-mug", "clay-table-lamp"), model.visibleProducts.map { it.id })
        model.search = "lamp"
        assertEquals(listOf("clay-table-lamp"), model.visibleProducts.map { it.id })
        model.search = "no match"
        assertTrue(model.visibleProducts.isEmpty())
        for (route in listOf(ShopRoute.Home, ShopRoute.Collection, ShopRoute.Product("canvas-weekender"), ShopRoute.Cart, ShopRoute.Checkout, ShopRoute.Confirmation))
            assertEquals(route, ShopCodec.decode(ShopCodec.encode(route)))
        assertIs<ShopRoute.Missing>(ShopCodec.decode("/shop/wrong"))
        assertEquals(0, ShopModel().count)
    }
}
