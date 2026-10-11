# Getting started

Add `mavenCentral()` and `google()` to your repositories. Use the current version from [Releases](https://github.com/samuolis/domposable/releases); `0.0.0` is an unpublished development baseline.

```kotlin
commonMain.dependencies {
    implementation("io.github.samuolis.domposable:domposable-design-material:<version>")
    implementation("io.github.samuolis.domposable:domposable-adaptive:<version>")
}
```

```kotlin
import androidx.compose.runtime.*
import io.github.samuolis.domposable.foundation.*
import io.github.samuolis.domposable.ui.*

@Composable
fun Greeting() {
    var name by remember { mutableStateOf("") }
    DomposableColumn(gap = 16.dp) {
        DomposableTextField(name, { name = it }, "Name")
        DomposableText("Hello $name")
    }
}
```

Install a provider at the app root and call the shared composable from Android `setContent`, iOS `ComposeUIViewController`, or JS `renderComposable(rootElementId = "root")`. Import `DomposableMaterialTheme` from `io.github.samuolis.domposable.material` for the default provider shown below. The feature itself does not choose its renderer. The JS entry point uses Compose HTML; add `org.jetbrains.compose.html:html-core` matching the catalog version to the application host. Android/iOS hosts use standard Compose UI.

Use the [consumer fixture](https://github.com/samuolis/domposable/tree/main/consumer-fixture) and demo as runnable integration examples. No runtime reflection registry or DI adapter is required.

## Platform entry points

The feature stays in `commonMain`; only the host belongs in a platform source set.

Android (inside an activity):

```kotlin
setContent {
    DomposableMaterialTheme { Greeting() }
}
```

Kotlin/iOS (export this controller to your Swift app):

```kotlin
fun MainViewController() = ComposeUIViewController {
    DomposableMaterialTheme { Greeting() }
}
```

Kotlin/JS with an HTML `<div id="root"></div>`:

```kotlin
fun main() {
    renderComposable(rootElementId = "root") {
        DomposableMaterialTheme { Greeting() }
    }
}
```

Kotlin/Wasm uses Compose UI:

```kotlin
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.getElementById("root")!!) {
        DomposableMaterialTheme { Greeting() }
    }
}
```

Use standard Compose hosts and compiler plugins matching the [tested toolchain](compatibility.md). `renderComposable` comes from `org.jetbrains.compose.web`; `ComposeUIViewController` from `androidx.compose.ui.window`; `ComposeViewport` from `androidx.compose.ui.window`; `document` from `kotlinx.browser`.

These snippets illustrate host boundaries; runnable target configurations are in [`demo/build.gradle.kts`](https://github.com/samuolis/domposable/blob/main/demo/build.gradle.kts) and its source sets. Do not call Compose UI layout or Material composables from the HTML host. Within shared feature code, use the `Domposable` primitives so the selected provider controls rendering.

## Supply your dependencies

```kotlin
@Composable
fun App(dependencies: AppDependencies) {
    val model = remember(dependencies) { dependencies.createContactsModel() }
    DomposableMaterialTheme {
        ContactsScreen(model)
    }
}
```

`AppDependencies` and `ContactsModel` belong to your app. For a model tied to navigation rather than the whole app, use [entry ownership](screens.md). For a compiled dependency graph, see [Metro integration](integrations.md). A ViewModel is optional and a screen returns `Unit`.
