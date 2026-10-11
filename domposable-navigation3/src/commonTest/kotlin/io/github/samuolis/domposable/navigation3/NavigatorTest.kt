package io.github.samuolis.domposable.navigation3

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class NavigatorTest {
    @Test fun backKeepsRootAndRetainsThePreviousEntryIdentity() {
        val navigator = DomposableNavigator("home")
        val home = navigator.backStack.single()
        navigator.navigate("product")
        val product = navigator.backStack.last()
        navigator.navigate("product")
        assertEquals(2, navigator.backStack.size)
        assertTrue(navigator.goBack())
        assertSame(home, navigator.backStack.single())
        assertFalse(navigator.goBack())
        navigator.navigate("product")
        assertNotEquals(product.id, navigator.backStack.last().id)
    }

    @Test fun replaceAndResetUseFreshEntriesWithoutAnEmptyStack() {
        val navigator = DomposableNavigator("home")
        navigator.navigate("cart")
        val id = navigator.backStack.last().id
        navigator.replace("checkout")
        assertEquals(listOf("home", "checkout"), navigator.backStack.map { it.route })
        assertNotEquals(id, navigator.backStack.last().id)
        navigator.reset("confirmation")
        assertEquals("confirmation", navigator.currentRoute)
        assertFalse(navigator.canGoBack)
    }

    @Test fun anchorsShareTheBrowserHistoryUrlRules() {
        val codec =
            object : DomposableRouteCodec<String> {
                override fun encode(route: String) = route

                override fun decode(path: String) = path
            }
        assertEquals("#/products/mug", codec.urlFor("/products/mug"))
        assertEquals("/app/products/mug/", codec.urlFor("/products/mug/", DomposableBrowserUrls.Path("/app/", true)))
        assertEquals("/app/", codec.urlFor("/", DomposableBrowserUrls.Path("/app", true)))
        assertFailsWith<IllegalArgumentException> { codec.urlFor("//outside.example") }
        assertFailsWith<IllegalArgumentException> { codec.urlFor("products/mug") }
    }
}
