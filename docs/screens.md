# Screens and navigation

Plain composables are sufficient. Add `domposable-screens` only when you want route registration and entry ownership.

```kotlin
val registry = domposableScreenRegistry {
    register(ContactsRoute::class) {
        val model = retain("contacts", dispose = { it.close() }) {
            dependencies.createContactsModel(route)
        }
        ContactsScreen(model)
    }
}
```

The factory belongs to your application and may call any DI container. Avoid resolving a new model on every recomposition. Entry `retain` gives resources a stable lifetime; composition `remember` gives resources a composition lifetime. You can also own models above navigation.

`DomposableEntryScope` supplies route identity, portable saveables and Created/Resumed/Destroyed lifecycle. `rememberSaveable` supports scalar values, lists and string-keyed maps, including Compose mutable state. For custom objects, supply a Saver converting them to portable data. App models are not history snapshots. Keep sensitive/transient data out of browser history.

The optional lifecycle module supplies `DomposableViewModelEntry(entry)` with AndroidX LifecycleOwner, ViewModelStoreOwner and SavedStateRegistryOwner. Create ViewModels with your application factory; the library does not infer dependencies.

The optional Navigation 3 host uses published AndroidX entry decoration and native `NavDisplay`. Its DOM host renders entry content directly into HTML. Use `DomposableRouteCodec` to encode your app's routes, `rememberDomposableBackStack`, `DomposableBrowserHistory`, and `DomposableNavigation3Host`; see the demo for the complete shell. The URL uses hashes so GitHub Pages project paths work without rewrite rules.

Back/forward restores entry saveables and scroll positions, then recreates app-owned resources when an entry was disposed. Pane-count changes retain the same entry IDs and resources.

## Optional KSP

Annotate a visible, non-generic top-level `@Composable` function returning `Unit` with `@DomposableScreen(ContactsRoute::class)`. Add `domposable-ksp` to `kspCommonMainMetadata`, and add `build/generated/ksp/metadata/commonMain/kotlin` as a common source directory. Configure compile/source-jar tasks to depend on the common generator, as the demo does.

The generated `contactsScreenDestination` factory accepts your rendering lambda. Required model parameters remain your responsibility. Parameterless/all-default screens get a default rendering lambda. No Metro, MVI or constructor injection code is generated. Without KSP, use `register(Route::class)` directly.
