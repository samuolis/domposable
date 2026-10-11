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
