package example.shop.catalog.impl

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zacsweers.metro.*
import example.shop.catalog.api.*
import example.shop.catalog.impl.generated.*
import io.github.samuolis.domposable.lifecycle.DomposableViewModelEntry
import io.github.samuolis.domposable.screens.*

@Inject @ContributesBinding(ShopScope::class)
class DefaultCatalogFeature(private val newCatalog: () -> CatalogViewModel, private val newProduct: ProductViewModel.Factory) : CatalogFeature {
    override fun register(builder: DomposableScreenRegistryBuilder, artwork: ShopArtwork, onBrowse: () -> Unit, onProduct: (String) -> Unit, onCart: () -> Unit, onArtworkLoaded: (() -> Unit)?) {
        builder.register(discoverScreenDestination {
            DomposableViewModelEntry(this) {
                val model = viewModel { newCatalog() }
                val state by model.state.collectAsState()
                val cart by model.cart.state.collectAsState()
                DiscoverScreen(state.copy(notice = cart.notice), model::dispatch, artwork, onBrowse, onProduct, onArtworkLoaded)
            }
        })
        builder.register(collectionScreenDestination {
            DomposableViewModelEntry(this) {
                val model = viewModel { newCatalog() }
                val state by model.state.collectAsState()
                val cart by model.cart.state.collectAsState()
                CollectionScreen(state.copy(notice = cart.notice), model::dispatch, artwork, onBrowse, onProduct)
            }
        })
        builder.register(productScreenDestination {
            val id = route.id
            DomposableViewModelEntry(this) {
                val model = viewModel { newProduct.create(id) }
                val cart by model.cart.state.collectAsState()
                ProductScreen(ProductState(model.product, cart.notice), artwork, model::add, onBrowse, onCart)
            }
        })
    }
}
