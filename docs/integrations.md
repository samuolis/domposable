# Application integrations

Domposable renders UI. Your application chooses its dependencies, models and state architecture. A screen is an ordinary `@Composable` function returning `Unit`; it can accept a model, values/callbacks, a store, or nothing. Injection belongs at the application boundary.

## Metro: a compiled example

[`metro-example`](https://github.com/samuolis/domposable/tree/main/metro-example) uses Metro **1.4.5** with Kotlin **2.4.21**. CI compiles the graph on Android, iOS, JVM, JS and Wasm. Its JVM test checks that repositories are shared, models belong to individual entries, and popping one entry disposes only its model.

```kotlin
@Inject @SingleIn(AppScope::class)
class ContactsRepository

@Inject
class ContactsModel(val repository: ContactsRepository) {
    fun close() { /* application cleanup */ }
}

@DependencyGraph(AppScope::class)
abstract class AppGraph {
    abstract val newContactsModel: () -> ContactsModel
}

fun contactsDestination(graph: AppGraph) =
    DomposableDestination(ContactsRoute::class) {
        val model = remember(this, graph) {
            retain("contacts.model", ContactsModel::close, graph.newContactsModel)
        }
        ContactsScreen(model)
    }
```

Apply `id("dev.zacsweers.metro") version "1.4.5"` in modules containing injected implementations, contributions or the application graph. The library has no Metro dependency. Create an application graph once. This small plain-model example passes a factory explicitly; the ViewModel storefront uses MetroX injection defaults instead. Do not make an entry-owned model an application singleton. Use a portable `Saver` or a saveable-state provider for restorable model data; `retain` owns a live instance and does not serialize it.

Metro is a compiler plugin; Domposable's optional KSP processor generates registration factories. They have separate responsibilities. Check Metro's [compiler compatibility](https://zacsweers.github.io/metro/latest/compatibility/) and [multiplatform guide](https://zacsweers.github.io/metro/latest/multiplatform/) when changing Kotlin or declaring platform contributions.

See the [modular storefront](modular-demo.md) for two feature API/implementation pairs, cross-module Metro contributions, assisted ViewModel creation and KSP-generated entries.

## MetroX and Koin ViewModel injection

The [modular storefront](modular-demo.md) uses MetroX's standard `ViewModelGraph`, ViewModel map contributions and `LocalMetroViewModelFactory`. Screens declare `model: CatalogViewModel = metroViewModel()`; route-dependent models use `assistedMetroViewModel`. The application provides the factory once and generated `domposableEntries()` needs no factories or navigator arguments. CI compiles this arrangement on all six targets.

Koin can follow the same screen declaration:

```kotlin
@Composable
@DomposableScreen(ContactsRoute::class, viewModel = true)
fun ContactsScreen(model: ContactsViewModel = koinViewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    ContactsContent(state, model::dispatch)
}
```

Provide your Koin application at the root and declare the ViewModel in its modules. KSP calls the screen under the entry's AndroidX owner before evaluating the default parameter, so DI uses the current entry store. A dedicated Koin integration is not tested here. See [Koin Compose injection](https://insert-koin.io/docs/reference/koin-compose/compose/) and [Metro ViewModel Compose](https://zacsweers.github.io/metro/latest/metrox-viewmodel-compose/).

`DomposableMviScreen(model)` optionally replaces repeated state collection/action forwarding when a model implements `DomposableMvi<State, Action>`. It is independent of Metro and Koin; MVVM screens can collect their own flows and call ViewModel methods directly.

## Kobweb: the Compose HTML boundary

Kobweb builds on Compose HTML. A Kotlin/JS Domposable feature can therefore render **inside a Kobweb page**, using the HTML provider. The [`kobweb-example`](https://github.com/samuolis/domposable/tree/main/kobweb-example) compiles this boundary against **kobweb-core 0.25.1**:

```kotlin
@Page("/contacts")
@Composable
fun ContactsPage(ctx: PageContext) {
    val model = remember { ContactsPageModel() }
    DomposableTheme(components = rememberDomposableMaterialComponents()) {
        ContactsFeature(model, onOpen = {
            ctx.router.navigateTo("/contacts/$it")
        })
    }
}
```

Let **Kobweb own page routes and browser history**. Pass navigation callbacks into the feature. Do not install `DomposableBrowserHistory` on the same page: two history owners can disagree about the current URL and back button. Domposable's internal in-memory Navigation 3 host can still manage a feature-local flow when its entries are independent of page navigation.

The example checks page API compilation, not a complete Kobweb server/export pipeline. In a real Kobweb application, register the page with Kobweb's own Gradle/KSP setup, supply destination pages, and verify its [routing](https://kobweb.varabyte.com/docs/concepts/foundation/routing) and [export](https://kobweb.varabyte.com/docs/concepts/foundation/exporting) configuration. Static exports of dynamic routes require explicit route coverage. Rendering a shared feature does not automatically provide server rendering, pre-rendered content or SEO guarantees.

Use `DomposableColumn` for an embedded feature rather than the demo's full-window scaffold. Theme CSS variables are scoped. Silk modifiers/components and Compose UI nodes remain platform-specific: they are not interchangeable with `DomposableModifier`, and Compose UI nodes cannot run under the HTML applier. Wasm is a separate Compose UI entry point, not the Kobweb HTML provider.

## Other application choices

| Integration | How to use it | Evidence |
| --- | --- | --- |
| Manual/plain Compose state | Construct the model in the app; pass it to a screen | Shared demo and published-consumer fixture |
| Metro | Use MetroX injection defaults or explicitly pass a plain-model factory | Compiled six-target example; ownership test |
| AndroidX ViewModel | Use the optional `domposable-lifecycle-viewmodel` adapter | JVM ownership tests; compiled published consumers |
| MVI/reducer/store | Use the optional model wrapper or collect a store yourself | Modular shopping demo: StateFlow, reducers, ViewModels and checkout effects |
| Koin/Dagger/custom DI | Use a Compose injection default or resolve an explicit model | Architecture-compatible; dedicated integrations not tested |
| Kobweb | Render a JS feature in `@Page`; let its router own URLs | Page wrapper compiles; full export not tested |

KSP does not infer injection or enforce MVI. Injection defaults remain in ordinary Kotlin composables; explicit models and factories are available when needed.
