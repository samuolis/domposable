# Screens and navigation

Start with regular composables. A screen can accept values and callbacks, a plain model, an injected ViewModel, or no parameters. Domposable does not choose your state architecture or DI container.

| Approach | Application owns | Domposable supplies |
| --- | --- | --- |
| Plain Compose / direct Navigation 3 | Screens, back stack, entry provider, decorators and platform display selection | Shared UI components and a DOM display for ordinary `NavEntry` objects |
| Manual registry | Route declarations, registrations, dependencies and back stack | Entry identity, portable saveables and resource ownership |
| KSP + optional navigator | Annotated screens, injection defaults and feature composition | Typed entry factories, feature entry lists and a convenience host |

## A small app with the navigator

```kotlin
val navigator = rememberDomposableNavigator<ShopRoute>(Home)
val entries = remember { domposableEntries() } // generated in your feature package
CompositionLocalProvider(LocalAppNavigator provides navigator) {
    DomposableNavHost(navigator, entries)
}

// In a nested screen:
val navigator = LocalAppNavigator.current
DomposableButton("Open product", { navigator.navigate(Product("mug")) })
```

Define `LocalAppNavigator` with `staticCompositionLocalOf<DomposableNavigator<ShopRoute>> { error("Provide app navigation") }` in your application. A typed local avoids unchecked route casts; callbacks or another router are equally valid.

`navigate` avoids duplicate consecutive routes. `replace` replaces the current entry; `reset` starts a new stack. On HTML and Wasm, `replace` and `reset` replace the current browser-history record; they do not erase earlier browser history. Routes are ordinary application types. Pass a `DomposableRouteCodec` and `DomposableBrowserUrls` to `rememberDomposableNavigator` to opt into HTML/Wasm browser history. Without a codec, navigation stays in memory.

The host creates its registry internally. Entries can come from KSP, hand-written `DomposableDestination` values, or a mixture. The [modular storefront](modular-demo.md) combines catalog and checkout entries without registration calls in the app.

## KSP that removes entry wiring

Annotate the screen itself. Its ViewModel can use your DI container's Compose injection default:

```kotlin
@Composable
@DomposableScreen(ProductRoute::class, viewModel = true)
fun ProductScreen(
    route: ProductRoute,
    model: ProductViewModel = assistedMetroViewModel<ProductViewModel, ProductViewModel.Factory> {
        create(route.id)
    },
) {
    val navigator = LocalAppNavigator.current
    ProductContent(model, onBack = { navigator.goBack() })
}
```

KSP generates `productScreenEntry()` and an explicit `productScreenEntry(model)` overload in the original package's `.generated` package. It supplies `route`, installs `DomposableViewModelEntry` when `viewModel = true`, and calls the original screen. Default expressions, including `metroViewModel()` or `koinViewModel()`, run **inside** that owner. A `DomposableEntryScope<ProductRoute>` parameter is also supplied automatically. KSP never chooses a DI container or creates a ViewModel factory.

The processor also generates `domposableEntries(...)` per package. The full overload combines explicit dependencies; an additional overload omits default parameters. If all non-route parameters have defaults, use `domposableEntries()` with no arguments. Shared dependencies with the same name and type appear once; conflicting types get entry-prefixed names. The app explicitly combines lists from its feature modules.

The default annotation does **not** install AndroidX ViewModel ownership. `viewModel = true` requires `domposable-lifecycle-viewmodel`. State collection remains in your screen or an application helper. The annotation is needed only for generated entries.

For injected screens, provide your DI environment at the application root. [The modular sample](modular-demo.md) demonstrates MetroX's standard factory and composition local. With Koin, provide the Koin application and use `model: YourViewModel = koinViewModel()` instead. Manual constructors, callbacks and explicit models remain supported.

Apply KSP and add the processor to `kspCommonMainMetadata`:

```kotlin
plugins { alias(libs.plugins.ksp) }

dependencies {
    add("kspCommonMainMetadata", "io.github.samuolis.domposable:domposable-ksp:<version>")
}
kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}
tasks.matching {
    (it.name.startsWith("compile") || it.name.contains("sourcesJar", ignoreCase = true)) &&
        it.name != "kspCommonMainKotlinMetadata"
}.configureEach { dependsOn("kspCommonMainKotlinMetadata") }
tasks.matching {
    it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata"
}.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
    enabled = false
}
```

Supported declarations are visible, non-generic, top-level `@Composable` functions returning `Unit`, without varargs. Generated factories preserve typed callback/generic parameters and composable type aliases. Additional entry and aggregate overloads omit parameters with defaults and lets the original function apply those defaults. The original `*Destination(render)` factory remains available for custom wiring.

## Build a registry yourself

This path needs neither KSP nor `DomposableNavigator`:

```kotlin
val stack = rememberDomposableBackStack<ShopRoute>(Home)
val registry = remember(dependencies) {
    domposableScreenRegistry {
        register(ProductRoute::class) {
            val model = retain("product", dispose = { it.close() }) {
                dependencies.createProduct(route.id)
            }
            ProductScreen(model) { if (stack.size > 1) stack.removeLast() }
        }
    }
}
DomposableNavigation3Host(stack, registry, onBack = {
    if (stack.size > 1) stack.removeLast()
})
```

You can register generated destinations manually as well. Your registry chooses which screens are available. Duplicate registrations fail early.

`DomposableEntryScope` supplies route identity, portable saveables and Created/Resumed/Destroyed lifecycle. `retain` owns a resource until its entry is removed; `remember` owns a value while its composition is present. Avoid creating a new model on every recomposition. Models can also live above navigation.

`rememberSaveable` supports scalar values, lists and string-keyed maps, including Compose mutable state. Supply a Saver for custom objects. HTML/Wasm Back/Forward restores entry saveables and scroll positions and recreates disposed resources. Keep sensitive and transient data out of browser history. Pane-count changes preserve entry IDs and resources.

## Use Navigation 3 directly

A registry is optional. Build standard Navigation 3 entries and decorate them yourself:

```kotlin
val stack = remember { mutableStateListOf<Route>(Home) }
val provider = entryProvider<Route> {
    entry<Home> { HomeScreen(onProduct = { stack.add(Product(it)) }) }
    entry<Product> { route -> ProductScreen(route.id) }
}
val entries = rememberDecoratedNavEntries(
    backStack = stack,
    entryDecorators = yourDecorators,
    entryProvider = provider,
)
```

On native / Compose UI targets, pass `entries` to the published Navigation 3 `NavDisplay` and configure your own scenes, transitions and back handling. In your Kotlin/JS DOM source set, plug the same entries into:

```kotlin
DomposableNavDisplay(
    entries = entries,
    visibleEntries = 1,
    onBack = { if (stack.size > 1) stack.removeLast() },
)
```

The DOM display renders already-decorated entry content into HTML sections. It accepts any `NavEntry<T>` key type, does not change the stack and does not install decorators. Content keys must be stable and unique. Decorations must be compatible with the DOM applier; Compose UI layout/animation wrappers cannot render inside HTML. The DOM display supports a visible tail of the entries; native Navigation 3 scene strategies and transitions remain configurable in native `NavDisplay`.

Applications using their own route back stack also own URL integration and restoration. Domposable's browser-history convenience uses `DomposableEntryScope` stacks; it does not replace an application's router. See [URL recipes](recipes.md) for hash/path deployment and [compatibility](compatibility.md) for the tested browser boundaries.

For a manually built registry, `rememberDomposableNavEntries(stack, registry, entryDecorators)` provides the entry ownership adapter without a host. Pass its result to native `NavDisplay` or the DOM display. This gives you registry-based screen ownership while retaining control of the display and additional decorators.

[Compiled consumer examples](https://github.com/samuolis/domposable/blob/main/consumer-fixture/src/commonMain/kotlin/example/consumer/Consumer.kt) · [Navigation 3 entry decoration](https://developer.android.com/guide/navigation/navigation-3/naventrydecorators)
