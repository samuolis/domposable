# Caller-owned browser history

`DomposableBrowserHistoryState` provides physical browser-history positions and retains the caller's snapshot at each position. The caller keeps ownership of route types, stack transactions, URL policy, and restoration side effects.

```kotlin
@Composable
fun App() {
    var stack by remember { mutableStateOf(listOf(Route.Home)) }
    val history = remember { DomposableBrowserHistoryState(stack) }
    val codec = remember {
        object : DomposableBrowserHistoryCodec<List<Route>> {
            override fun encode(position: Int, value: List<Route>) = encodeSnapshot(position, value)
            override fun decode(snapshot: String) = decodeSnapshot(snapshot)
            override fun url(value: List<Route>) = canonicalUrl(value.last())
        }
    }

    DomposableBrowserHistory(
        history,
        codec,
        onBeforeRestore = ::captureCurrentSaveables,
    ) { record ->
        val restored = record?.value ?: stackFromCurrentUrl()
        stack = restored
        if (record == null) history.restore(DomposableBrowserHistoryRecord(history.position, restored))
    }

    DomposableButton("Settings", onClick = {
        stack = stack + Route.Settings
        history.push(stack)
    })
}
```

Use `replace(value, url)` for a same-position update, including a fragment URL. Use `reset(value, url)` when the app ends the retained entry lifetimes at the current browser position. A push advances one position and drops retained snapshots ahead of the current position. Before a browser Back or Forward restore, `onBeforeRestore` can capture outgoing state. `onRestore` receives valid state on initialization and each valid pop, or `null` for a pop with absent or invalid state so the app can restore from the current URL. Popstate restoration does not write another history entry. Pass root-relative paths or fragments; explicit cross-origin and script URLs are rejected before the caller state changes. The demo's `/#/caller-history` route exercises rapid pushes, Back/Forward, refresh, URL fallback, and URL validation. The existing `DomposableBrowserHistory(backStack, routeCodec, urls)` overload remains available for the library-owned stack API.
