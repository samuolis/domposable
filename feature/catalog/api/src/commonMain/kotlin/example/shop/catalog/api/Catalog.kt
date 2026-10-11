package example.shop.catalog.api

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableImageScale
import io.github.samuolis.domposable.screens.DomposableDestination
import io.github.samuolis.domposable.ui.DomposableModifier

/** Sample application scope marker; no dependency on a DI framework. */
abstract class ShopScope private constructor()
interface ShopRoute
sealed interface CatalogRoute : ShopRoute {
    data object Home : CatalogRoute
    data object Collection : CatalogRoute
    data class Product(val id: String) : CatalogRoute
    data class Missing(val path: String) : CatalogRoute
}
data class ShopProduct(val id: String, val name: String, val category: String, val cents: Int, val image: String, val description: String)
interface ProductCatalog { val products: List<ShopProduct> }
fun shopPrice(cents: Int): String = "€${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"

data class CatalogState(
    val products: List<ShopProduct>, val category: String = "All", val search: String = "",
    val priceAscending: Boolean = false, val notice: String = "",
) {
    val visibleProducts get() = products.filter {
        (category == "All" || it.category == category) && (it.name + " " + it.description).contains(search.trim(), ignoreCase = true)
    }.let { if (priceAscending) it.sortedBy(ShopProduct::cents) else it }
}
sealed interface CatalogAction {
    data class Search(val value: String) : CatalogAction
    data class Category(val value: String) : CatalogAction
    data class Sort(val ascending: Boolean) : CatalogAction
    data class Add(val id: String) : CatalogAction
    data object ClearFilters : CatalogAction
}
data class ProductState(val product: ShopProduct?, val notice: String)
sealed interface ProductAction { data object Add : ProductAction }
/** The application supplies bundled artwork; features have no resource or platform dependency. */
typealias ShopArtwork = @Composable (String, String, DomposableModifier, (() -> Unit)?, DomposableImageScale) -> Unit
interface CatalogFeature {
    fun entries(): List<DomposableDestination<*>>
}
