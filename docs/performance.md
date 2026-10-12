# Large collections

Use stable keys with `DomposableLazyColumn`, `DomposableLazyRow`, `DomposableLazyGrid` or `DomposableTable`. Compose UI and Wasm use native lazy layouts. HTML normally renders every item; opt into a bounded scroll window for a large in-memory dataset. Your app still owns loading, paging and business state.

## Fixed or measured column heights

```kotlin
DomposableLazyColumn(
    products,
    key = { it.id },
    gap = 8.dp,
    window = DomposableListWindow(
        rowHeight = 144.dp,
        viewportHeight = 360.dp,
        measureRows = true,
    ),
) { product -> ProductRow(product) }
```

With `measureRows = true`, `rowHeight` is an initial HTML estimate. A `ResizeObserver` measures mounted rows; a keyed prefix-sum index updates offsets and preserves the first visible item's position when preceding rows resize. Native Compose uses natural item heights. With the default `false`, each row has the supplied height and overflowing content is clipped. Both modes keep a bounded viewport.

Measurements are retained by key while the collection is composed. Dataset changes rebuild the index; measured-height updates and offset lookup take logarithmic time. Offscreen, never-measured rows retain their estimate, so the scrollbar extent can change as you visit them. This is not an exact pre-layout measurement of the whole dataset.

## Horizontal rows and responsive grids

```kotlin
DomposableLazyRow(
    products,
    key = { it.id },
    gap = 8.dp,
    window = DomposableRowWindow(
        itemWidth = 240.dp,
        itemHeight = 240.dp,
        viewportWidth = 720.dp,
    ),
) { product -> ProductCard(product) }

DomposableMeasure(DomposableModifier.fillMaxWidth()) { size ->
    DomposableLazyGrid(
        products,
        key = { it.id },
        columns = if (size.width >= 700) 4 else 2,
        gap = 8.dp,
        window = DomposableGridWindow(rowHeight = 240.dp, viewportHeight = 360.dp),
    ) { product -> ProductCard(product) }
}
```

Row cells have fixed width/height; the HTML viewport shrinks to its available width. Grid cells share the available width and have a fixed row height. The application chooses the column count from measured content space. HTML observes viewport resizing and uses logical inline offsets for right-to-left rows. These windows clip content that exceeds the cell dimensions.

## Semantic tables

```kotlin
val columns = listOf(
    DomposableTableColumn("name", "Product"),
    DomposableTableColumn("stock", "Available"),
)
DomposableTable(
    caption = "Warehouse stock",
    columns = columns,
    items = products,
    key = { it.id },
    window = DomposableListWindow(64.dp, 360.dp, measureRows = true),
) { product, column ->
    when (column.key) {
        "name" -> DomposableText(product.name)
        "stock" -> DomposableText(product.available.toString())
    }
}
```

HTML emits `table`, `caption`, column-scoped `th` headers and cells. Windowed rows keep full `aria-rowcount`/`aria-colcount` and their actual `aria-rowindex`; hidden spacer rows preserve scroll geometry. Headers remain mounted and sticky. Native renders labelled columns and row/column collection semantics with a lazy body. Sorting, selection and row actions belong to your feature. Column virtualization and spreadsheet editing are outside this API.

![The shared inventory table on Android and semantic HTML](assets/examples/collection-table.png)

The same table declaration uses a native collection on Android and a real table on the web.

## Keys, focus and state

Use unique, stable string keys and positive dimensions. At least one overscan item/row lets keyboard focus reach the next window. A focused HTML item stays mounted outside the visible range. `rememberSaveable` drafts follow item keys through scrolling and reordering; removed keys discard their saved state. Ordinary `remember` values can be lost when an item unmounts. A new layout mode starts a new collection owner in the demo; business drafts that must survive changing modes should live in your model.

Keep `window = null` for small collections, full-document browser find, printing or accessibility browsing of every item. Windowed HTML omits offscreen items from the DOM; list positions and table coordinates describe the full dataset but do not make absent content searchable. Complete custom providers must honor and document each window contract themselves.

## Run the gallery and checks

Open [collection recipes](https://samuolis.github.io/domposable/demo/#/collection-recipes) or choose **More collection recipes** from the fixed-height lab. Switch layouts, scroll 10,000 items, edit a draft, reverse/reduce the dataset and resize the viewport. Expanded descriptions exercise measured-height updates.

```bash
./gradlew :demo:jsBrowserDevelopmentWebpack :demo:wasmJsBrowserDevelopmentWebpack :demo:jvmTest
python3 scripts/ci/browser_preview.py
npx playwright test tests/browser/collections.spec.ts tests/browser/advanced-collections.spec.ts
```

PR CI runs HTML behavior in Chromium, Firefox and WebKit. Tests gate bounded DOM size, keyed drafts, focus, keyboard traversal, resizing, RTL offsets and table semantics. `build/benchmarks/` records mounted counts and diagnostic timings; `android-html-evidence` retains them for 30 days. Timings include browser scheduling and do not establish a universal speed guarantee. Automated checks do not establish physical screen-reader parity.

See [migration](migration.md) for the next minor release's provider/props ABI changes.
