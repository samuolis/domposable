![Domposable — shared features for native apps and responsive HTML](assets/brand/header.svg)

# One feature. Many screens.

Build native apps and responsive DOM websites from the same Compose feature codebase. Write ordinary `@Composable` functions, supply your own models, and choose a design provider.

Android and iOS render Compose UI. Kotlin/JS emits semantic HTML with browser links, form controls and layout. Desktop and Wasm use Compose UI as experimental targets.

[Get started](getting-started.md) · [Try the component playground](https://samuolis.github.io/domposable/demo/) · [Open the storefront](https://samuolis.github.io/domposable/shop/) · [Compare HTML and Wasm](compare.md)

## See a complete feature

Fieldwork is a familiar shopping app with search, category filters, product pages, a bag and validated demo checkout. The same feature code adapts from a phone to a wide browser. It uses fictional products and sends no payments or orders.

![Fieldwork responsive web storefront](assets/examples/shop-web.png)

Follow the [shopping walkthrough](shopping.md), explore [components](components.md), or try the [navigation recipes](recipes.md).

## Keep the API simple

```kotlin
@Composable
fun ProductActions(name: String, price: String, onAdd: () -> Unit) {
    DomposableColumn(gap = 12.dp) {
        DomposableText(name, style = DomposableTextStyle.Title)
        DomposableText(price)
        DomposableButton("Add to bag", onAdd)
    }
}
```

[Follow the call from shared code to its platform renderer](how-it-works.md). Install a provider around the feature. Theme tokens set your product's colors, type and spacing; replace individual component renderers or supply a complete [design system](design-systems.md). Use measured content widths for [adaptive layouts](adaptive.md).

## Bring your architecture

Screens return `Unit`. Pass plain state and callbacks, a model, a ViewModel or a store. Your app owns dependencies: manual factories, Metro, Koin and other DI choices can stay at its boundary. [Screen entries](screens.md) and Navigation 3 are optional; KSP generates registration helpers without creating business logic or injection rules.

The [Metro example](integrations.md) compiles on all six targets. The Kobweb example checks the Compose HTML page boundary; a complete export is a documented follow-up. See the [compatibility table](compatibility.md) for tested versions and platform differences.

**Before 1.0:** APIs may change in minor releases. Use a published version from [Releases](https://github.com/samuolis/domposable/releases) for Maven dependencies and the signed APK demo. `0.0.0` is the unpublished bootstrap baseline. The repository also includes runnable HTML, Wasm and native examples.
