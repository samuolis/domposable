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
