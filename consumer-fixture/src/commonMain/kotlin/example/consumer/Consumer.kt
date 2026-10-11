package example.consumer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.adaptive.DomposableWidthClass
import io.github.samuolis.domposable.adaptive.rememberDomposableAdaptiveInfo
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.screens.DomposableEntryScope
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableComponents
import io.github.samuolis.domposable.ui.DomposableMeasure
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTheme
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.size

// Public API consumer: a plain model, callbacks, or an injected model all work.
class FeatureModel {
    var label by mutableStateOf("Public artifact consumer")
}

@Composable
fun Feature(
    model: FeatureModel,
    modifier: DomposableModifier = DomposableModifier,
) {
    DomposableColumn(modifier) {
        DomposableText(model.label)
        DomposableButton("Update", { model.label = "Updated" })
    }
}

@Composable
fun App(factory: () -> FeatureModel) {
    val model = remember(factory) { factory() }
    DomposableMaterialTheme {
        DomposableMeasure { size ->
            val adaptive = rememberDomposableAdaptiveInfo(size)
            Feature(
                model,
                DomposableModifier.padding(
                    if (adaptive.widthClass ==
                        DomposableWidthClass.Compact
                    ) {
                        8.dp
                    } else {
                        24.dp
                    },
                ),
            )
        }
    }
}

// The renderer interface is public. No state machine or ViewModel is required.
@Composable
fun BrandedApp(components: DomposableComponents) {
    DomposableTheme(components = components) { DomposableText("Custom design system") }
}

object FixtureRoute

@Composable
@DomposableScreen(FixtureRoute::class)
fun InjectedScreen(model: FeatureModel) {
    Feature(model)
}

fun generatedRegistration(model: FeatureModel) =
    example.consumer.generated.injectedScreenDestination {
        InjectedScreen(model)
    }

// Typed dependency and default-argument generation; route and entry scope are supplied by KSP.
data class ProductRoute(
    val id: String,
)
typealias Artwork = @Composable (List<String>) -> Unit

@Composable
@DomposableScreen(ProductRoute::class, viewModel = true)
fun Product(
    route: ProductRoute,
    entry: DomposableEntryScope<ProductRoute>,
    model: FeatureModel,
    artwork: Artwork,
    onSelect: (Map<String, List<Int?>>) -> Unit,
    `when`: String = "default label",
) {
    DomposableText("${route.id}: ${entry.id} ${model.label} ${`when`}")
    artwork(listOf(route.id))
    DomposableButton("Select", { onSelect(mapOf(route.id to listOf(1, null))) })
}

fun typedGeneratedEntry(
    model: FeatureModel,
    artwork: Artwork,
    onSelect: (Map<String, List<Int?>>) -> Unit,
) = example.consumer.generated.productEntry(model, artwork, onSelect)

object DefaultRoute

@Composable
@DomposableScreen(DefaultRoute::class, viewModel = true)
fun DefaultScreen(defaultModel: FeatureModel = remember { FeatureModel() }) {
    Feature(defaultModel)
}

// Aggregate defaults stay at the original composable, under its generated entry owner.
fun generatedRequiredEntries(
    model: FeatureModel,
    artwork: Artwork,
    onSelect: (Map<String, List<Int?>>) -> Unit,
) = example.consumer.generated.domposableEntries(model = model, artwork = artwork, onSelect = onSelect)

// Optional MVI is an interface, not a ViewModel inheritance requirement.
class FeatureStore : io.github.samuolis.domposable.lifecycle.DomposableMvi<String, String> {
    override val state = kotlinx.coroutines.flow.MutableStateFlow("Store consumer")

    override fun dispatch(action: String) {
        state.value = action
    }
}

@Composable
fun MviFeature(model: FeatureStore) {
    io.github.samuolis.domposable.lifecycle.DomposableMviScreen(model) { label, onAction ->
        DomposableText(label)
        DomposableButton("Update", { onAction("Updated") })
    }
}

// Direct Navigation 3: no registry, annotation, navigator or Domposable entry scope is required.
@Composable
fun DirectNavigation3() {
    val stack = remember { mutableStateListOf("home") }
    val provider =
        androidx.navigation3.runtime.entryProvider<String> {
            entry<String> { route ->
                DomposableText(route)
                DomposableButton("Open", { stack.add("detail") })
            }
        }
    val entries = androidx.navigation3.runtime.rememberDecoratedNavEntries(stack, entryProvider = provider)
    io.github.samuolis.domposable.navigation3.DomposableNavDisplay(entries = entries, onBack = {
        if (stack.size >
            1
        ) {
            stack.removeAt(stack.lastIndex)
        }
    })
}
