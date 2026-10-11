# Fieldwork: a shared shopping app

A familiar ecommerce layout: announcement bar, shop header, editorial hero, product cards, search/category filters, product details, bag, order summary and validated demo checkout. All product artwork is bundled locally. The catalogue is fictional; no payment is collected and no data is sent.

[Open HTML storefront](https://samuolis.github.io/domposable/shop/) · [Open Wasm storefront](https://samuolis.github.io/domposable/wasm/#/shop) · [Compare renderers](compare.md)

![Fieldwork on a wide browser](assets/examples/shop-web.png)
![Fieldwork on Android and iOS](assets/examples/shop-native.png)

## Feature teams own their state

The [complete shared implementation](https://github.com/samuolis/domposable/blob/main/demo/src/commonMain/kotlin/io/github/samuolis/domposable/demo/Shopping.kt) contains a plain `ShopModel`. It uses integer cents for prices, derives cart totals, and validates checkout before clearing the cart. Construct it manually, inject it, or adapt the screen arguments to your store.

```kotlin
@Composable
fun ShopFeature(model: ShopModel, openProduct: (String) -> Unit) {
    StorefrontScreen(
        model = model,
        showHero = true,
        onBrowse = { model.category = "All" },
        onProduct = openProduct,
    )
}
```

`ShoppingApp` composes the theme, shell and Navigation 3 destinations. The feature screens receive models and callbacks, without a required base class or action protocol.

## Layout follows available space

The storefront measures **feature content**. Narrow widths stack hero artwork and copy; wide widths arrange them alongside each other. Product cards change from one column to two and three. Search and filter controls wrap rather than overflow. Native renders Material controls while HTML uses browser elements; adaptive structure is shared, platform styling remains visible.

## Real product URLs

| Page | HTML URL below the project base |
| --- | --- |
| Discover | `/domposable/shop/` |
| Collection | `/domposable/shop/collection/` |
| Product | `/domposable/shop/products/canvas-weekender/` |
| Bag | `/domposable/shop/cart/` |
| Checkout | `/domposable/shop/checkout/` |

```kotlin
ShoppingApp(
    browserUrls = DomposableBrowserUrls.Path(
        basePath = "/domposable/",
        trailingSlash = true,
    ),
)
```

`ShopCodec` owns the route-to-URL mapping. Navigation items carry actual `href` values, preserving copy-link and open-in-new-tab behavior. Normal clicks update application navigation and browser history. Reloading product pages returns the same product. Static entry pages cover the known catalogue; a GitHub Pages fallback resumes other dynamic paths. Unknown products show an unavailable state. See [routing recipes](recipes.md) for hosting tradeoffs.

The cart is an in-memory demo session and resets on a full reload. Browser back/forward within the app preserves the cart and checkout draft. Personal details are not written to history, storage or the network. Wasm starts from a hash route and uses in-memory navigation; browser-history parity is pending.

## Bundled artwork

The demo uses Compose resources for the same product files on every target:

```kotlin
val source = Res.getUri("files/shop-bag.png")
DomposableImage(
    DomposableImageProps(source, "Canvas weekender", onError = { /* show fallback */ }, scale = DomposableImageScale.Fit),
    DomposableModifier.fillMaxWidth().height(220.dp),
)
```

Enable `implementation(compose.components.resources)` in the app and put the file in `src/commonMain/composeResources/files/`. DOM emits an image element; native/Wasm use Coil. Image callbacks let the feature handle successful loading and errors. Artwork and [generation prompts](assets/brand/storefront-prompts.md) are included in the source repository.

For clean HTML paths, the host also calls `configureWebResources { resourcePathMapping { path -> URL(path, document.baseURI).href } }` before composition. This maps bundled resources to the deployment base instead of the current product URL. JetBrains documents the [resource-path configuration](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources-setup.html). The repository's small generated Xcode project includes a build phase copying these files to the app's Compose resource bundle; a standard Compose Xcode integration can use its resource synchronization tasks instead.
