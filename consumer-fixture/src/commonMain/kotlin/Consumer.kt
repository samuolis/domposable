package example.consumer

import androidx.compose.runtime.*
import io.github.samuolis.domposable.foundation.*
import io.github.samuolis.domposable.ui.*
import io.github.samuolis.domposable.material.*
import io.github.samuolis.domposable.screens.*
import io.github.samuolis.domposable.adaptive.*

// Public API consumer: a plain model, callbacks, or an injected model all work.
class FeatureModel { var label by mutableStateOf("Public artifact consumer") }

@Composable
fun Feature(model: FeatureModel, modifier: DomposableModifier = DomposableModifier) {
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
            Feature(model, DomposableModifier.padding(if (adaptive.widthClass == DomposableWidthClass.Compact) 8.dp else 24.dp))
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
fun InjectedScreen(model: FeatureModel) { Feature(model) }
fun generatedRegistration(model: FeatureModel) = example.consumer.generated.injectedScreenDestination { InjectedScreen(model) }
