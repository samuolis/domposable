# Getting started

Add `mavenCentral()` and `google()` to your repositories. Use the current version from [Releases](https://github.com/samuolis/domposable/releases); `alpha00` is an unpublished development baseline.

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
import io.github.samuolis.domposable.material.*

@Composable
fun Greeting() {
    var name by remember { mutableStateOf("") }
    DomposableMaterialTheme {
        DomposableColumn(gap = 16.dp) {
            DomposableTextField(name, { name = it }, "Name")
            DomposableText("Hello $name")
        }
    }
}
```

Call the shared composable from Android `setContent`, iOS `ComposeUIViewController`, or JS `renderComposable(rootElementId = "root")`. The JS entry point uses Compose HTML; add `org.jetbrains.compose.html:html-core` matching the catalog version to the application host. Android/iOS hosts use standard Compose UI.

Use the [consumer fixture](https://github.com/samuolis/domposable/tree/main/consumer-fixture) and demo as runnable integration examples. No runtime reflection registry or DI adapter is required.
