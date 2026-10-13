package example.consumer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import example.consumer.components.generated.ConsumerComponents
import example.consumer.components.generated.DomposableProductCard
import io.github.samuolis.domposable.foundation.DomposableComponent
import io.github.samuolis.domposable.ui.DomposableComponentProvider
import io.github.samuolis.domposable.ui.ProvideDomposableComponentProvider

@DomposableComponent("ProductCard")
data class ProductCardProps(
    val title: String,
    val onOpen: () -> Unit,
)

/** Both a product provider and the shared feature compile against the packaged library. */
@Composable
fun ProductComponents(
    renderCard: @Composable (ProductCardProps) -> Unit,
    content: @Composable () -> Unit,
) {
    val provider: DomposableComponentProvider =
        remember(renderCard) {
            ConsumerComponents(productCard = { props, _, _ -> renderCard(props) }).asDomposableComponentProvider()
        }
    ProvideDomposableComponentProvider(provider, content)
}

@Composable
fun ProductFeature(
    title: String,
    open: () -> Unit,
) {
    DomposableProductCard(ProductCardProps(title, open))
}
