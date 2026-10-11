# Adaptive UI

Measure feature content after shell navigation/insets with `DomposableMeasure` or the scaffold's `availableContentSize`. Classify that width with `rememberDomposableAdaptiveInfo`. Feature breakpoints remain independent from the full window.

Defaults: Compact below 600, Medium from 600, Expanded from 840, Large from 1200 logical units. Override `DomposableBreakpoints` for your product. `DomposableResponsive` inherits smaller-class values unless explicitly overridden.

```kotlin
DomposableMeasure { size ->
    val info = rememberDomposableAdaptiveInfo(size)
    val recipe = DomposablePaneRecipe()
    FeaturePanes(dualPane = recipe.isDualPane(info))
}
```

Recipes change presentation, not route identity or model ownership. The demo keeps list/detail entries when changing between a bottom navigation shell and a rail, or one/two feature panes.

Reduced motion follows browser media queries, Android animator duration settings and iOS accessibility notifications. Pointer precision follows browser media queries and Android connected mouse/touchpad devices. iOS defaults to touch and desktop to precise; applications may override either preference through the adaptive API when they have more precise input information. Preference information is advisory; providers/features choose animations and targets.

## Responsive dimensions and wrapping

```kotlin
DomposableMeasure(DomposableModifier.fillMaxWidth()) { size ->
    if (size.width <= 0f) return@DomposableMeasure
    val info = rememberDomposableAdaptiveInfo(size)
    val spacing = DomposableResponsive(12.dp, medium = 16.dp, expanded = 24.dp)
        .resolve(info.widthClass)
    DomposableFlowRow(gap = spacing) {
        DomposableButton("Save", onSave)
        DomposableButton("Share", onShare)
    }
}
```

Measurement initially reports zero until the platform lays out the feature. Wait for a positive size before starting requests that depend on dimensions, such as image decoding or pagination. The storefront does this before choosing hero/card widths.

## A feature-specific pane recipe

```kotlin
DomposableScaffold(topBar = { AppHeader() }) {
    val info = rememberDomposableAdaptiveInfo(availableContentSize)
    val recipe = DomposablePaneRecipe(dualPaneFrom = DomposableWidthClass.Expanded)
    DomposableNavigation3Host(
        stack, registry,
        modifier = contentModifier.fillMaxWidth().fillMaxHeight(),
        visibleEntries = if (recipe.isDualPane(info)) 2 else 1,
    ) { if (stack.size > 1) stack.removeLast() }
}
```

Keep stack/model ownership outside the layout decision. Shrinking from two panes to one hides an entry without popping it. The [navigation recipes](recipes.md) demonstrate drafts surviving width changes and a supporting third pane.

![Shared shopping UI on mobile and a wide browser](assets/examples/shop-web.png)
