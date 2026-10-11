![Domposable — shared features for native apps and responsive HTML](assets/brand/header.svg)

# Build a feature once

Native apps and responsive DOM websites from the same Compose feature codebase.

Write a feature once using ordinary `@Composable` functions. Supply your own models. Choose a component provider to render it with Compose UI on Android/iOS or semantic HTML in a browser.

Domposable keeps feature contracts independent of dependency injection and state architecture. It adds portable UI primitives, ordered modifiers, semantic tokens, measured adaptive layouts and optional screen/navigation ownership.

Try the [task workspace and component catalog](https://samuolis.github.io/domposable/demo/) and download [native demos](https://github.com/samuolis/domposable/releases). The website and downloads identify their commit in `release.json`.

Until the first Maven release, the site is a tested bootstrap preview at `0.1.0-alpha00`. That version is not available on Maven Central. Native debug downloads are available in the successful [CI run artifacts](https://github.com/samuolis/domposable/actions/workflows/ci.yml). Published releases replace this preview with the website attached to the release.

Android, iOS arm64 and Kotlin/JS are the initial targets. JVM desktop is experimental. Alpha coverage and limitations are listed in [Coverage](coverage.md).


Explore the same shared feature through [HTML/Wasm comparison](compare.md), [Navigation 3 and clean web URLs](recipes.md), and [Metro/Kobweb integration](integrations.md). See [compatibility](compatibility.md) and [remaining release work](roadmap.md).

## Try a real feature

[Open the Fieldwork storefront](https://samuolis.github.io/domposable/shop/) or [compare HTML and Wasm](compare.md). The demo includes real product URLs, search, category filters, a cart and validated demo checkout. It uses ordinary application-owned models and callbacks.

![Fieldwork responsive web storefront](assets/examples/shop-web.png)

## Start small

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

Install a provider around this feature: Android and iOS render Compose UI; Kotlin/JS renders HTML. Your app supplies the state and dependencies. Follow [Get started](getting-started.md), browse [components](components.md), and choose a [design system](design-systems.md).
