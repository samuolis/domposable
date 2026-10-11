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

Apply `id("dev.zacsweers.metro") version "1.4.5"` in the **application module**. The library has no Metro dependency. Create an application graph once, then pass factories into destinations. Do not make an entry-owned model an application singleton. Use a portable `Saver` or a saveable-state provider for restorable model data; `retain` owns a live instance and does not serialize it.

Metro is a compiler plugin; Domposable's optional KSP processor generates registration factories. They have separate responsibilities. Check Metro's [compiler compatibility](https://zacsweers.github.io/metro/latest/compatibility/) and [multiplatform guide](https://zacsweers.github.io/metro/latest/multiplatform/) when changing Kotlin or declaring platform contributions.

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
| Metro | Pass an application graph's factory into an entry | Compiled six-target example; ownership test |
| AndroidX ViewModel | Use the optional `domposable-lifecycle-viewmodel` adapter | JVM ownership tests; compiled published consumers |
| MVI/reducer/store | Pass a store or values/callbacks from the app | API accepts ordinary arguments; no bundled store integration |
| Koin/Dagger/custom DI | Resolve at the app boundary and supply a factory | Architecture-compatible; dedicated integrations not tested |
| Kobweb | Render a JS feature in `@Page`; let its router own URLs | Page wrapper compiles; full export not tested |

KSP does not infer injection or enforce MVI. Prefer explicit factories that feature teams can read and test.
