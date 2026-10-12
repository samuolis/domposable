# Navigation 3 recipes and web URLs

The shared playground contains a working task list/detail feature and a nested inbox/detail/supporting-pane recipe. Open **Recipes**, type a reply, allow three panes, open context and resize. Entry identity and drafts survive pane changes. Going back disposes a removed entry; restorable state can recreate it later.

## One stack, responsive panes

```kotlin
val stack = rememberDomposableBackStack<Route>(Route.Inbox)
val screens = remember { domposableScreenRegistry {
    register(Route.Inbox::class) { InboxScreen(onOpen = {
        stack.add(DomposableEntryScope(Route.Message(it)))
    }) }
    register(Route.Message::class) { MessageScreen(route.id) }
} }
DomposableMeasure { size ->
    val info = rememberDomposableAdaptiveInfo(size)
    val panes = if (DomposablePaneRecipe().isDualPane(info)) 2 else 1
    DomposableNavigation3Host(stack, screens, visibleEntries = panes) {
        if (stack.size > 1) stack.removeLast()
    }
}
```

Measure **feature content**, after the navigation rail and application chrome take their space. `visibleEntries` displays the last N entries; the built-in scene places them in equal-width panes. It is not a metadata-driven Material list/detail scene strategy. The example's three-pane policy requires a large content width and explicitly enables supporting content. Applications own route grouping, pane policy and root navigation.

The demo's main navigation replaces the active stack. The separate `#/roots` recipe demonstrates independent saved root stacks. Model lifetime comes from `entry.retain`; drafts use `rememberSaveable`. See [screens](screens.md) and [integrations](integrations.md) for application factories, optional ViewModels and Metro.

## Independent root stacks

Open `#/roots`, enter an Inbox draft, switch to Archive and open its message, then return. Both root stacks keep their route strings, entry IDs and portable UI saveables. The application-owned `RootStacks` controller snapshots the departing root before changing hosts and recreates fresh entry scopes when it returns. It uses a Compose Saver to restore both roots after reload.

```kotlin
val roots = rememberSaveable(saver = RootStacks.saver) { RootStacks() }
DomposableTabs(listOf("Inbox", "Archive"), selectedIndex, onSelect = {
    roots.select(listOf("Inbox", "Archive")[it])
})
key(roots.selected) {
    val entries = rememberDomposableNavEntries(roots.active, screens)
    RootRecipeDisplay(entries, wide, roots::back)
}
```

`RootStacks` and `RootRecipeDisplay` are application sample code, not mandatory library APIs. See the [controller](https://github.com/samuolis/domposable/blob/main/demo/src/commonMain/kotlin/io/github/samuolis/domposable/demo/RootStacks.kt) and [recipe](https://github.com/samuolis/domposable/blob/main/demo/src/commonMain/kotlin/io/github/samuolis/domposable/demo/RootStackRecipe.kt).

This recipe deliberately disposes an inactive root's live sessions when selecting another root, then the departed host releases its decorators. Keying the host by root identity keeps automatic saveable keys stable when returning. Its drafts survive; its models are recreated through the application's normal DI path. Never keep a destroyed `DomposableEntryScope` and attempt to resume it. If your app needs live models across root switches, retain the owning host/decorator or move that resource to an application-owned scope. Each policy has a different memory/lifetime cost.

Back pops only the selected root and never removes its final home entry. Re-selecting the current root does nothing; opening its current message does not duplicate the route. Root history here is internal application state: the URL identifies `/roots`, while its tab and child stacks are saveables. To expose each nested route as a URL, supply an application codec/history policy that represents that root; do not install competing browser-history owners.

## Caller-owned Navigation 3 scenes

On native Compose and Wasm, `RootRecipeDisplay` passes decorated entries directly to Navigation 3's `NavDisplay`. Its scene strategy recognizes an Inbox/Archive home and detail from the same root, displays a one-third list and two-thirds detail at wide content sizes, and returns `null` for single-pane fallback. `previousEntries` defines back to the list. The data-class scene provides value equality so unchanged scenes do not become new transitions.

```kotlin
// In the application's Compose UI source set:
val strategy = remember(wide) {
    SceneStrategy<DomposableEntryScope<RootRoute>> {
        rootListDetailScene(it, wide)
    }
}
NavDisplay(
    entries = entries,
    sceneStrategies = listOf(strategy),
    onBack = onBack,
)
```

The [native implementation](https://github.com/samuolis/domposable/blob/main/demo/src/composeUiMain/kotlin/io/github/samuolis/domposable/demo/RootListDetailScene.kt) is compiled across all Compose UI targets. HTML uses `DomposableNavDisplay` with one or two equal-width semantic panes; the native scene's ratio and transition policy are not an HTML API. Keep Compose UI scenes out of the DOM applier. Applications can use ordinary Navigation 3 keys, providers and decorators without a Domposable registry, navigator or KSP; [direct integration](screens.md) shows that lower-level path.

For broader scene policies, see the [official Navigation 3 scene guide](https://developer.android.com/guide/navigation/navigation-3/scenes). Dialog/overlay strategies, predictive-back animation tuning and live-model retention across inactive roots remain application decisions.

## Semantic HTML hash routes

The default browser binding produces URLs such as:

- `/domposable/demo/#/tasks`
- `/domposable/demo/#/tasks/welcome`
- `/domposable/demo/#/catalog`
- `/domposable/demo/#/recipes`

Hash URLs need no server rewrite. The application supplies a `DomposableRouteCodec`; route payloads are explicitly encoded as paths, rather than serializing DI objects. Browser back/forward and reload restore portable UI saveables. Live models are recreated after an entry has been disposed.

## Path routes and direct links

The same HTML demo also runs at [the path-routes example](https://samuolis.github.io/domposable/routes/tasks/welcome/). It produces clean URLs:

- `/domposable/routes/tasks`
- `/domposable/routes/tasks/welcome`
- `/domposable/routes/catalog`
- `/domposable/routes/recipes`

```kotlin
DomposableBrowserHistory(
    backStack = stack,
    codec = MyRouteCodec,
    urls = DomposableBrowserUrls.Path(basePath = "/workspace/", trailingSlash = true),
)
```

`DomposableNavigationItem.href` renders a real anchor in HTML. Ordinary clicks call the application navigation callback; Ctrl/Cmd-click and opening a new tab retain normal browser link behavior. Native providers use the navigation callback.

Install one binding for the application's stack. The codec must encode an absolute application path beginning with `/`; encode route parameters safely and validate them while decoding. `basePath` scopes the binding below a host path. Set `trailingSlash = true` for static directory hosting to avoid reload redirects that can discard history state. Query parameters and data loading remain application responsibilities; the current binding routes using the pathname only. Use a query-aware application adapter when queries are part of navigation identity.

### Host configuration

A server should serve the app entry for every path below the base, while serving assets normally. For example, an nginx deployment at `/workspace/` can use `try_files $uri $uri/ /workspace/index.html;` in that location. Use a matching asset base URL so nested pages load bundles from the application root.

GitHub Pages has no general rewrite rule. This repository emits static entry pages for the known demo paths, with a `<base>` for bundles. They return HTTP 200 on direct loads. Its `404.html` redirects other paths inside `/domposable/routes/` through the entry point, then restores the path before composing. That fallback initially returns HTTP 404 and should not be treated as SEO pre-rendering. Arbitrary data-backed routes need appropriate hosting if that distinction matters.

For a Kobweb site, use its page/router API and do not install a second browser history owner. The Wasm comparison currently has in-memory navigation after initial load; path-history parity is still future work.
