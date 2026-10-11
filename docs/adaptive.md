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
