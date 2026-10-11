package example.shop.catalog.impl

import example.shop.catalog.api.CatalogAction
import example.shop.catalog.api.CatalogState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogTest {
    private val products = InMemoryProductCatalog().products

    @Test fun filtersAndOrderingAreIndependentOfRendering() {
        var state = CatalogState(products)
        state = reduceCatalog(state, CatalogAction.Category("Home"))
        state = reduceCatalog(state, CatalogAction.Sort(true))
        assertEquals(listOf("studio-mug", "clay-table-lamp"), state.visibleProducts.map { it.id })
        state = reduceCatalog(state, CatalogAction.Search("lamp"))
        assertEquals(listOf("clay-table-lamp"), state.visibleProducts.map { it.id })
        state = reduceCatalog(state, CatalogAction.Search("no match"))
        assertTrue(state.visibleProducts.isEmpty())
        assertEquals(products.size, reduceCatalog(state, CatalogAction.ClearFilters).visibleProducts.size)
    }
}
