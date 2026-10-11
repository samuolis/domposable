package example.shop.catalog.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import dev.zacsweers.metro.map
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import example.shop.catalog.api.CatalogAction
import example.shop.catalog.api.CatalogState
import example.shop.catalog.api.ProductAction
import example.shop.catalog.api.ProductCatalog
import example.shop.catalog.api.ProductState
import example.shop.catalog.api.ShopProduct
import example.shop.catalog.api.ShopScope
import example.shop.checkout.api.CheckoutAction
import example.shop.checkout.api.ShoppingCart
import io.github.samuolis.domposable.lifecycle.DomposableMvi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@Inject
@SingleIn(ShopScope::class)
@ContributesBinding(ShopScope::class)
class InMemoryProductCatalog : ProductCatalog {
    override val products =
        listOf(
            ShopProduct(
                "canvas-weekender",
                "Canvas weekender",
                "Travel",
                8900,
                "shop-bag.png",
                "Room for a weekend away. Durable cotton canvas, considered pockets and comfortable carry handles.",
            ),
            ShopProduct(
                "studio-mug",
                "Studio mug",
                "Home",
                2400,
                "shop-mug.png",
                "A slower start to your morning. A generous, hand-finished stoneware mug with a softly speckled glaze.",
            ),
            ShopProduct(
                "clay-table-lamp",
                "Clay table lamp",
                "Home",
                12900,
                "shop-lamp.png",
                "Warm light for your favourite corner. A sculptural clay base paired with a natural linen shade.",
            ),
        )
}

@Inject @ViewModelKey
@ContributesIntoMap(ShopScope::class, binding<ViewModel>())
class CatalogViewModel(
    val catalog: ProductCatalog,
    val cart: ShoppingCart,
) : ViewModel(),
    DomposableMvi<CatalogState, CatalogAction> {
    private val mutableState = MutableStateFlow(CatalogState(catalog.products))
    override val state: StateFlow<CatalogState> =
        combine(mutableState, cart.state) { catalog, cart ->
            catalog.copy(notice = cart.notice)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, mutableState.value.copy(notice = cart.state.value.notice))

    override fun dispatch(action: CatalogAction) {
        when (action) {
            is CatalogAction.Add -> cart.dispatch(CheckoutAction.Add(action.id))
            else -> mutableState.update { reduceCatalog(it, action) }
        }
    }
}

fun reduceCatalog(
    state: CatalogState,
    action: CatalogAction,
): CatalogState =
    when (action) {
        is CatalogAction.Search -> state.copy(search = action.value)
        is CatalogAction.Category -> state.copy(category = action.value)
        is CatalogAction.Sort -> state.copy(priceAscending = action.ascending)
        CatalogAction.ClearFilters -> state.copy(search = "", category = "All")
        is CatalogAction.Add -> state
    }

@AssistedInject
class ProductViewModel(
    @Assisted val id: String,
    val catalog: ProductCatalog,
    val cart: ShoppingCart,
) : ViewModel(),
    DomposableMvi<ProductState, ProductAction> {
    private val product = catalog.products.firstOrNull { it.id == id }
    override val state: StateFlow<ProductState> =
        cart.state
            .map { ProductState(product, it.notice) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, ProductState(product, cart.state.value.notice))

    override fun dispatch(action: ProductAction) {
        when (action) {
            ProductAction.Add -> cart.dispatch(CheckoutAction.Add(id))
        }
    }

    @AssistedFactory @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(ShopScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(id: String): ProductViewModel
    }
}
