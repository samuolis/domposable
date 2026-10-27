# How the API works

**Web rendering:** Domposable uses Compose HTML to render a single-page application (SPA) in the browser. It does not provide server-side rendering (SSR). JavaScript must run to compose feature content. Hash/path URLs and static hosting entry pages do not change this rendering model.

Write a feature once as a regular Compose function. Domposable's component calls describe its controls and layout. Each built-in component calls its platform implementation through `expect` / `actual`.

## 1. Write the feature in commonMain

```kotlin
import androidx.compose.runtime.*
import io.github.samuolis.domposable.foundation.*
import io.github.samuolis.domposable.ui.*

@Composable
fun DeliveryPreferences(onSave: (String) -> Unit) {
    var delivery by remember { mutableStateOf("standard") }
    DomposableColumn(
        modifier = DomposableModifier.padding(16.dp),
        gap = 12.dp,
    ) {
        DomposableText("Delivery", style = DomposableTextStyle.Title)
        DomposableSelect(
            value = delivery,
            onValueChange = { delivery = it },
            label = "Delivery speed",
            options = listOf(
                DomposableSelectOption("standard", "Standard"),
                DomposableSelectOption("express", "Express"),
            ),
        )
        DomposableButton("Save", { onSave(delivery) })
    }
}
```

The function returns `Unit`. It reads Compose state and receives an application callback. Selecting an option changes `delivery`. Compose recomposes the controls that read it. Pressing Save invokes your callback. The option's `value` is the stable application identifier. The `label` is display text that can be translated or reordered.

Use direct arguments in feature code. Each component also has a `Props` overload for reusable configuration. You do not need to build `DomposableSelectProps` to display a selection field.

## 2. Set your theme

```kotlin
import io.github.samuolis.domposable.material.DomposableMaterialTheme

@Composable
fun App() {
    DomposableMaterialTheme {
        DeliveryPreferences(onSave = { delivery -> savePreferences(delivery) })
    }
}
```

`savePreferences` is your application's function. `DomposableMaterialTheme` supplies theme values through composition locals. A nested theme can customize one part of the app. Component implementations are selected at compile time for each target.

Choose a standard host at the platform entry point:

| Host | What the same feature produces |
| --- | --- |
| Android `setContent` | Compose layouts and Material controls |
| iOS `ComposeUIViewController` | Compose layouts and Material controls |
| JVM Compose window | Compose UI, experimental desktop target |
| Kotlin/JS `renderComposable` | HTML elements and CSS. Select becomes a labelled `<select>` |
| Kotlin/Wasm `ComposeViewport` | Compose UI rendered through the browser canvas, experimental Wasm target |

The [getting started guide](getting-started.md#platform-entry-points) shows each host. The host's applier determines which kind of nodes can be composed. Keep native `Row`, Material controls and native `Modifier` inside native renderers. HTML renderers compose DOM elements. Shared feature functions call `DomposableRow`, `DomposableButton` and `DomposableModifier`.

## 3. Understand the call path

For `DomposableSelect(value, onValueChange, label, options)`, the library:

1. Creates the portable `DomposableSelectProps` value.
2. Calls the internal `expect fun RenderDomposableSelect`.
3. Uses the target's `actual` implementation to emit the control and connect events.

The declarations live in `domposable-ui` under `material/ComponentRenderers.kt`.
`HtmlMaterial.kt` implements Kotlin/JS rendering and `NativeMaterial.kt` implements
Compose UI rendering. These are ordinary compile-time platform boundaries.
There is no built-in component registry to construct or install.

```text
Shared feature → DomposableSelect → RenderDomposableSelect
                                      ├─ native actual: Material dropdown
                                      └─ JS actual: labelled HTML select
```

The public props forwarding functions are generated when **Domposable itself** is
built. Applications do not need KSP to use components. Direct calls allow the
production Kotlin/JS compiler to discard unused component implementations.
Theme values customize appearance without retaining every renderer.

## 4. Customize the theme, wrap components, and adapt layout

Use the theme's named arguments to establish your brand. Every argument is optional. Omitted values inherit the nearest theme or the library defaults:

```kotlin
DomposableMaterialTheme(
    colors = DomposableColors(primary = DomposableColor(0xFF354C3C)),
    shapes = DomposableShapes(medium = DomposableShape.Rounded(8.dp)),
    styles = DomposableComponentStyles(
        card = DomposableSurfaceAppearance(shape = DomposableShape.Rounded(8.dp)),
    ),
) {
    DeliveryPreferences(onSave)
}
```

Read the current values through `DomposableTheme.colors`, `.typography`, `.shapes` and `.styles`. For a partial component override, copy its current style, such as `DomposableTheme.styles.card.copy(...)`. [Material configuration](material-catalog.md#configuration) explains per-call overrides and standard/expressive choices. `DomposableTokens` is a theme configuration bundle. Ordinary app themes can use the named arguments above.

Keep your design system's component names and delegate to Domposable:

```kotlin
@Composable
fun AppButton(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    DomposableButton(label, onClick, enabled = enabled)
}
```

Your features call `AppButton`. Add the styling and slots your app needs. For a
control with different structure or behavior, give your own component an
`expect` / `actual` contract. [Design systems](design-systems.md) shows native and
HTML implementations, including modifier translation. Built-in component calls
are not replaced globally by a provider.

Modifiers are an ordered chain of portable operations. `padding(16.dp).background(color)` paints inside the outer padding. `background(color).padding(16.dp)` paints behind it. Native adapts that chain into Compose modifiers. HTML uses ordered wrapper elements and CSS. Use `fillMaxSize()`, `size(width, height)` and `padding(horizontal, vertical)` for common combinations.

Adapt to the space a feature actually receives:

```kotlin
DomposableMeasure { size ->
    val adaptive = rememberDomposableAdaptiveInfo(size)
    if (adaptive.widthClass == DomposableWidthClass.Compact) {
        DomposableColumn(gap = 12.dp) {
            Summary()
            Details()
        }
    } else {
        DomposableRow(gap = 24.dp) {
            Summary()
            Details()
        }
    }
}
```

Import adaptive helpers from `io.github.samuolis.domposable.adaptive`. `Summary` and `Details` are your composables. [Adaptive layouts](adaptive.md) covers pane recipes and content widths.

## Add screens and navigation when your app needs them

A screen is an ordinary composable. It can receive your existing model or obtain one through your DI container's Compose API. Component rendering does not create models or require a state architecture.

For navigation, choose [direct Navigation 3, a manual registry, or generated entries](screens.md). With generated entries, `@DomposableScreen(Route::class)` connects a route to a function, and KSP creates the entry list. `viewModel = true` installs entry ownership before the original screen evaluates its injection default. KSP does not build a DI graph.

Use the [modular app walkthrough](modular-demo.md) to see feature API/implementation boundaries, Metro injection and route ownership together. Start with [component examples](components.md) when your feature only needs shared UI.


## Code sharing and responsive layouts

Shared components, modifiers, sizing, wrapping and scrolling work on native and HTML targets. The [adaptive APIs](adaptive.md) measure the available feature width and provide breakpoints and pane recipes. Put the responsive choice in the shared composable once. Both renderers use it. Applications still choose which content belongs side by side or behind navigation, and platform-only integrations or custom renderers affect the final shared-code ratio.
