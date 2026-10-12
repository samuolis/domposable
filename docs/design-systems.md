# Design systems

`DomposableTheme` installs semantic tokens and a complete `DomposableComponents` provider. The Material artifact is optional: a product can supply every renderer without depending on Material.

```kotlin
DomposableTheme(tokens = productTokens, components = productComponents) {
    FeatureScreen(model)
}
```

For incremental customization, copy a provider and replace one renderer:

```kotlin
val defaults = rememberDomposableMaterialComponents()
val product = defaults.copy(button = { props, modifier, content ->
    ProductButton(props, modifier, content)
})
DomposableTheme(components = product) { FeatureScreen(model) }
```

`ProductButton` is a platform implementation. It receives the same enabled state, click handler, modifier and composable content. It can replace the element structure, interaction and styling. The demo supplies foundation-based native buttons and dashed DOM buttons to show this.

Tokens cover colors, typography, spacing, radius and motion duration. Theme scope maps values to native Material defaults or CSS variables; nested themes remain local. Product-specific dimensions can be supplied through your own composition locals. Current native typography maps sizes/line height; custom fonts need a provider-specific font mapping.

Modifiers preserve operation order. `padding().background()` and `background().padding()` are different. Shared modifiers contain portable operations; use `toComposeModifier()` in native renderer code and `DomposableHtmlModifiers`/`domposableAttributes` in JS renderers. Native `Modifier` remains outside shared feature signatures.

## Product tokens

The storefront changes the same semantic colors and typography for native and HTML:

```kotlin
val shopTokens = DomposableTokens(
    colors = DomposableColors(
        primary = DomposableColor(0xFF354C3C),
        surface = DomposableColor(0xFFFAF8F3),
        onSurface = DomposableColor(0xFF292E28),
        surfaceVariant = DomposableColor(0xFFEAE7DE),
    ),
    typography = DomposableTypography(titleSize = 30f),
    radius = 8.dp,
)
DomposableTheme(shopTokens, rememberDomposableMaterialComponents()) {
    ShopFeature(model, openProduct)
}
```

Native maps semantic surfaces and navigation selection to the product palette. HTML scopes CSS variables to the theme container. Nested themes allow one feature to have its own identity without changing the surrounding app.

## A DOM button implementation

Place this renderer in `jsMain`; implement its native counterpart in a Compose UI source set.

```kotlin
@Composable
fun ProductButton(props: DomposableButtonProps, modifier: DomposableModifier, content: @Composable () -> Unit) {
    DomposableHtmlModifiers(modifier) {
        Button(attrs = {
            domposableAttributes(modifier)
            attr("type", "button")
            if (!props.enabled) disabled()
            onClick { props.onClick() }
            style {
                property("border", "2px solid var(--d-primary)")
                property("padding", "12px 20px")
            }
        }) { content() }
    }
}
```

Import `Button` from `org.jetbrains.compose.web.dom` and `disabled` from `org.jetbrains.compose.web.attributes`. Keep accessible state and portable modifier semantics when replacing a renderer. A DOM renderer must only compose HTML nodes. The [custom-provider fixture](https://github.com/samuolis/domposable/tree/main/custom-provider-fixture) demonstrates a complete provider without Material.


## Add typed product components

A product can add renderer contracts without changing the published default provider. Define the
props in shared code; the modifier and callbacks remain portable. The optional processor emits a
provider in your package, so it cannot overwrite Domposable's own `DomposableComponents`.

```kotlin
@DomposableComponent("ProductCard")
data class ProductCardProps(val title: String, val onOpen: () -> Unit)
```

Configure that module's processor:

```kotlin
ksp {
    arg("domposable.components.package", "example.design.generated")
    arg("domposable.components.provider", "ProductComponents")
}
```

Implement the generated renderer in each platform provider and install its typed bindings:

```kotlin
val provider = remember {
    ProductComponents(productCard = { props, modifier, _ ->
        ProductCardRenderer(props, modifier)
    }).asDomposableComponentProvider()
}
ProvideDomposableComponentProvider(provider) {
    DomposableProductCard(ProductCardProps("Canvas bag", openProduct))
}
```

`ProductCardRenderer` is your native or HTML implementation. It consumes the library's modifier
and renders the same values and callbacks; it must never compose native UI nodes into an HTML
composition. This path supports application-owned appearance and richer product slots without
passing a DI container or state architecture to the component framework.

The annotated props must be a named, non-generic class accessible to the generated package.
Set `domposable.components.visibility` to `internal` when an application's props are internal.
For generic props or another hand-written contract, use the manual typed key API below.

The manual equivalent uses `DomposableComponentKey<Props>`, `domposableComponentProvider` and
`DomposableComponent(key, props, modifier)`. Keys have identity, so separate design systems may
use the same display name. A duplicate binding within one provider is rejected. Nested providers
inherit parent bindings and override only their registered keys. Missing renderers fail with the
component's name instead of dropping content.

## Extend portable modifiers

Use built-in operations for constraints, shape, border, child weight/alignment and controlled
scrolling. A product-specific operation can implement `DomposableModifierElement` and be appended
with `modifier.then(element)`; the single ordered `elements` chain keeps it in place between
built-in operations.

Native providers consume `toComposeModifier(operationRenderer = ...)`. HTML providers consume
`DomposableHtmlModifiers(modifier, operationRenderer = ...)`. Providers can also install
`LocalDomposableNativeModifierRenderer` or `LocalDomposableHtmlModifierRenderer` for their
children. Each custom element needs native and HTML handling. An unhandled element fails visibly;
it is never silently discarded. Keep product token roles in that provider, outside feature code.

Inside native Row, Column and Box renderer content, install the respective
`ProvideDomposableRowModifierScope`, `ProvideDomposableColumnModifierScope` or
`ProvideDomposableBoxModifierScope` helper. The library then resolves child weight and alignment
against the actual Compose layout scope. HTML places child layout operations on the outer item
so padding and background wrappers do not detach them from their flex or grid parent.

## Installing controls inside an existing host theme

A host can retain its native Material theme or HTML root and install the library's component set
without a second theme container:

```kotlin
ProvideDomposableMaterialComponents(tokens) {
    ProvideDomposableComponentProvider(provider) {
        ProductScreen(model)
    }
}
```

Native keeps the host's current `MaterialTheme`. HTML installs the Material stylesheet and
component/token locals without adding a layout element. Apply `domposableTokenVariables(tokens)`
to the existing HTML host's attrs so descendant controls receive scoped `--d-*` variables.
The helper sets custom properties only; the host keeps its classes, typography and layout.
Use `DomposableMaterialTheme` for a standalone application that wants the default full theme.

Matrix schemas are portable library models. A matrix may use a generic application icon value;
the provider resolves it. Specialized diagrams and rich-text editors stay application-owned typed
extension components. A basic read-only RichHtml control and shared sanitizer are reusable library
capabilities; diagram layout engines and editor toolbars are outside the default provider.


## Scroll state in custom HTML renderers

Use the same `rememberDomposableScrollState()` in feature code on every target. `scrollTo` moves
immediately; `animateScrollTo` waits for the scroll animation and honors reduced-motion settings
on HTML. A newer scroll request, coroutine cancellation, user input or disposal interrupts the
active animation. The state reports the current position while the user or an animation scrolls.
Targets are clamped to the mounted region's available content, including an empty region. For
asynchronously loaded content, issue the scroll request after those items have been rendered.

A custom HTML renderer can keep its own element and CSS while installing the library's binding:

```kotlin
import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.ui.domposableScrollState
import io.github.samuolis.domposable.ui.rememberDomposableScrollState
import org.jetbrains.compose.web.dom.Div

@Composable
fun ProductScrollArea(content: @Composable () -> Unit) {
    val state = rememberDomposableScrollState()
    Div(attrs = {
        style {
            property("max-height", "320px")
            property("overflow-y", "auto")
        }
        domposableScrollState(state, DomposableOrientation.Vertical)
    }) { content() }
}
```

`domposableScrollState` is a Kotlin/JS renderer helper. It owns the element binding, scroll
observation and cleanup; the renderer owns layout constraints and overflow styles. Avoid copying
`state.value` back to `scrollTop` during recomposition or calling `scrollTo` from an `onScroll`
listener: those paths would interrupt animation. Standard Domposable scroll modifiers already
install this binding. Native renderers use the portable modifier adapter.
