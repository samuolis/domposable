package example.consumer

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneStrategy
import io.github.samuolis.domposable.navigation3.DomposableNavDisplay

/** The packaged native API exposes Nav3 configuration without requiring a registry or navigator. */
@Composable
fun <T : Any> NativeNavigationConsumer(
    entries: List<NavEntry<T>>,
    scenes: List<SceneStrategy<T>>,
    chrome: List<SceneDecoratorStrategy<T>>,
    onBack: () -> Unit,
) {
    DomposableNavDisplay(
        entries = entries,
        sceneStrategies = scenes,
        sceneDecoratorStrategies = chrome,
        onBack = onBack,
    )
}
