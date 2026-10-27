![Domposable — shared features for native apps and responsive HTML](assets/brand/header.svg)

# Shared Compose UI. Real HTML.

Build Android, iOS and web interfaces from shared Compose components and layouts. Domposable renders with Compose UI on native platforms and semantic HTML on Kotlin/JS. Desktop and Wasm are available as experimental targets.

Use the included Material 3 theme or create your own design system. Keep your existing state management, dependency injection and navigation. Domposable is used in [Podiumas](https://podiumas.eu), a live sports tournament app.

[Get started](getting-started.md) · [Try the component playground](https://samuolis.github.io/domposable/demo/) · [Open the storefront](https://samuolis.github.io/domposable/shop/) · [Compare HTML and Wasm](compare.md)

The HTML target runs in the browser. The optional [public-page exporter](page-export.md) creates HTML and CSS snapshots at build time for a list of public routes. This is prerendering, not server-side rendering. The exporter is available from version 0.22.0.

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

Wrap your app in `DomposableMaterialTheme` to set colors, typography and spacing. Components use direct platform implementations with no provider setup. Keep your own component names and wrap the library controls. [Follow the component call](how-it-works.md), [customize a design system](design-systems.md), or explore [adaptive layouts](adaptive.md).

## Bring your architecture

Screens return `Unit`. Pass plain state and callbacks, a model, a ViewModel or a store. Your app owns its dependencies. Use manual factories, Metro, Koin or another DI container. [Screen entries](screens.md), Navigation 3 and KSP registration helpers are optional.

The [Metro and Koin examples](integrations.md) compile on all six targets. The runnable Kobweb example exports static index, contacts and contact-detail pages, tested with browser JavaScript disabled and enabled. See the [compatibility table](compatibility.md) for tested versions and platform differences.

**Before 1.0:** APIs may change in minor releases. Use a published version from [Releases](https://github.com/samuolis/domposable/releases) for Maven dependencies and the signed APK demo. The repository also includes runnable HTML, Wasm and native examples.
