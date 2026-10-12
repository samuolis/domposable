# Large collections

Native Compose and Wasm use Compose's lazy collections. HTML normally renders every keyed item, which supports variable heights, browser find and printing. For a large fixed-height column, opt into a bounded scroll window:

```kotlin
DomposableLazyColumn(
    products,
    key = { it.id },
    gap = 4.dp,
    window = DomposableListWindow(
        rowHeight = 88.dp,
        viewportHeight = 352.dp,
        overscan = 3,
    ),
) { product ->
    ProductRow(product)
}
```

The same declaration works on all targets. The default Material provider fixes each row to `rowHeight` and the list viewport to `viewportHeight`; content exceeding the row height is clipped. HTML mounts the visible rows and neighboring overscan rows in a semantic list, using `aria-posinset` and `aria-setsize` for their positions in the full collection. A focused row remains mounted when it leaves the viewport. Native providers keep their existing Compose lazy layout with the same fixed dimensions.

Use unique, stable string keys and at least one overscan row so keyboard focus can reach the next window. `rememberSaveable` drafts follow keys when scrolling and reordering; HTML evicts saved state when a key leaves the dataset. Ordinary `remember` values are lost when their row is unmounted. Data loading, paging and business state remain application-owned. This is a rendering window over an in-memory list, not a remote-data paging API.

## Choose the right layout

Windowing is optional and currently applies only to `DomposableLazyColumn`. Leave `window = null` for variable-height content, small lists, documents, browser find across every row, or printing the full collection. Offscreen rows are absent from the HTML tree, so browser find, printing and accessibility browsing cannot enumerate them all at once. Automated keyboard and axe checks cover the demo; physical screen-reader review is not part of CI.

Rows and grids retain their existing keyed HTML rendering. Custom providers receive the optional window in `DomposableLazyColumnProps` and decide how to implement it. Do not claim windowing support for a custom provider that ignores this property.

## Reproduce the measurements

Open the playground's `#/collections` route. Compare 1,000 plain rows with a windowed column, scroll 10,000 rows, edit a note, reverse the dataset and reduce its size. The tests record elapsed browser time and mounted row count in `build/benchmarks/collections-{browser}.json`, with screenshots alongside them.

```bash
./gradlew :demo:jsBrowserDevelopmentWebpack :demo:jvmTest
python3 scripts/ci/browser_preview.py
npx playwright test tests/browser/collections.spec.ts
```

PR CI runs this in Chromium, Firefox and WebKit and retains the measurements in `android-html-evidence` for 30 days. Timing includes interaction and browser scheduling; it is diagnostic evidence, not a hardware-independent performance guarantee. CI gates bounded DOM size, keyed state, focus, keyboard traversal and valid list semantics rather than an arbitrary millisecond threshold.

This API is unreleased on main after 0.2.0. Existing source calls keep their defaults. Its added function/props parameters change the compiled ABI, so consumers must recompile when upgrading to the next minor release.
