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

The demo's top-level navigation replaces the active stack. It does not implement independent saved tab back stacks. Model lifetime comes from `entry.retain`; drafts use `rememberSaveable`. See [screens](screens.md) and [integrations](integrations.md) for application factories, optional ViewModels and Metro.

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

Install one binding for the application's stack. The codec must encode an absolute application path beginning with `/`; encode route parameters safely and validate them while decoding. `basePath` scopes the binding below a host path. Set `trailingSlash = true` for static directory hosting to avoid reload redirects that can discard history state. Query parameters and data loading remain application responsibilities; the current binding routes using the pathname only. Use a query-aware application adapter when queries are part of navigation identity.

### Host configuration

A server should serve the app entry for every path below the base, while serving assets normally. For example, an nginx deployment at `/workspace/` can use `try_files $uri $uri/ /workspace/index.html;` in that location. Use a matching asset base URL so nested pages load bundles from the application root.

GitHub Pages has no general rewrite rule. This repository emits static entry pages for the known demo paths, with a `<base>` for bundles. They return HTTP 200 on direct loads. Its `404.html` redirects other paths inside `/domposable/routes/` through the entry point, then restores the path before composing. That fallback initially returns HTTP 404 and should not be treated as SEO pre-rendering. Arbitrary data-backed routes need appropriate hosting if that distinction matters.

For a Kobweb site, use its page/router API and do not install a second browser history owner. The Wasm comparison currently has in-memory navigation after initial load; path-history parity is still future work.
