![Domposable — native apps, real DOM, responsive by design](docs/assets/brand/header.svg)

# Domposable

**Native apps and responsive DOM websites from the same Compose feature codebase.**

Domposable gives feature teams a small shared UI vocabulary: ordinary composable screens, ordered modifiers, component renderers, theme tokens and adaptive recipes. Android and iOS use Compose UI. Kotlin/JS uses HTML elements, CSS and native browser controls. The same demo also builds for JVM desktop and Kotlin/Wasm; these targets are experimental. It is a Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP) UI library with a semantic DOM renderer, responsive layouts and a replaceable design system.

```kotlin
@Composable
fun ContactsScreen(model: ContactsModel) {
    DomposableColumn(gap = 12.dp) {
        DomposableText("Contacts", style = DomposableTextStyle.Title)
        DomposableButton("Add contact", model::addContact)
    }
}
```

Your application supplies its models and dependencies. Plain Compose state, MVVM, MVI, Metro, Koin, Dagger and manual construction are application choices. Screens return `Unit`; neither state/action wrappers nor dependency injection are mandatory.

## Modules

| Artifact | Purpose |
| --- | --- |
| `domposable-foundation` | Portable values and theme tokens |
| `domposable-ui` | Shared components, renderer contracts and modifiers |
| `domposable-design-material` | Optional native Material and semantic HTML defaults |
| `domposable-adaptive` | Content-sized breakpoints, preferences and pane recipes |
| `domposable-screens` | Typed registration and entry-owned resources/saveables |
| `domposable-navigation3` | Optional Navigation 3 hosts and browser history |
| `domposable-lifecycle-viewmodel` | Optional AndroidX ownership and MVI state/action helper |
| `domposable-ksp` | Optional typed entries and feature entry lists |

Coordinates: `io.github.samuolis.domposable:<artifact>:<released version>`. Choose the published version from [Releases](https://github.com/samuolis/domposable/releases). `0.0.0` is reserved for the unpublished bootstrap baseline.

Releases use plain semantic versions. The HTML and Wasm demos load content-hashed JavaScript; `release.json` and each demo's `web-assets.json` record their independent build hashes and exact assets. See [web build versioning](https://samuolis.github.io/domposable/releasing/#html-and-wasm-build-hashes).

[![CI](https://github.com/samuolis/domposable/actions/workflows/ci.yml/badge.svg)](https://github.com/samuolis/domposable/actions/workflows/ci.yml)

[Guides](https://samuolis.github.io/domposable/) · [Demo](https://samuolis.github.io/domposable/demo/) · [HTML/Wasm comparison](https://samuolis.github.io/domposable/compare/) · [Clean URL example](https://samuolis.github.io/domposable/routes/tasks/welcome/) · [Release downloads](https://github.com/samuolis/domposable/releases)

## See it in action

[Fieldwork shopping app](https://samuolis.github.io/domposable/shop/) · [Component playground](https://samuolis.github.io/domposable/demo/) · [HTML and Wasm comparison](https://samuolis.github.io/domposable/compare/)

The fictional shop shares its catalogue, filters, cart, checkout and feature composables across native apps, semantic HTML and Wasm. HTML uses real header/main/footer landmarks, links and form controls, with product URLs such as `/domposable/shop/products/canvas-weekender/`.

![Fieldwork responsive storefront on the web](docs/assets/examples/shop-web.png)
![The same Fieldwork storefront on Android and iOS](docs/assets/examples/shop-native.png)
![Domposable shared components on mobile and semantic HTML](docs/assets/examples/components.png)

Start with [the installation guide](docs/getting-started.md), then explore [components](docs/components.md), [responsive layouts](docs/adaptive.md), [design providers](docs/design-systems.md) and [Metro/Kobweb integration](docs/integrations.md). The [shopping walkthrough](docs/shopping.md) connects the pieces in one app. There is no mandatory ViewModel base class, MVI contract or DI container.

The [modular shopping example](https://samuolis.github.io/domposable/modular-demo/) has catalog and checkout API/implementation modules. Screens receive ViewModels through MetroX's standard Compose injection, navigate through a typed composition local, and optionally use the MVI helper:

```kotlin
@Composable
@DomposableScreen(CatalogRoute.Collection::class, viewModel = true)
fun CollectionScreen(model: CatalogViewModel = metroViewModel()) {
    val navigator = LocalShopNavigator.current
    DomposableMviScreen(model) { state, onAction ->
        CollectionContent(state, onAction, onProduct = { navigator.navigate(CatalogRoute.Product(it)) })
    }
}
```

KSP generates `domposableEntries()` with no factory arguments. It evaluates injection defaults under each entry's ViewModel owner. DI, the navigator and MVI are optional; [manual registry and direct Navigation 3](docs/screens.md) are supported too.

For local development, tests and release maintenance, see [CONTRIBUTING.md](CONTRIBUTING.md). Current support and limitations are in the [compatibility table](docs/compatibility.md).

Alpha APIs may change. HTML collections currently render keyed DOM lists rather than virtualizing; platform escape hatches remain platform-specific. See [coverage](docs/coverage.md) for current boundaries.

Apache-2.0. Copyright 2026 Samuolis.
