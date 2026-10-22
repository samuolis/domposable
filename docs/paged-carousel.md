# Paged carousel

`DomposableCarousel` keeps its Material carousel variants by default. Set `pagedLayout` when the
content should show one focused page at a time, with a compact next-page peek and full-width pages
at the wide breakpoint. Native Compose uses Foundation Pager; semantic HTML uses a scroll-snap track.

```kotlin
val items = cards.map { card ->
    DomposableLazyItem(card.id) { CardContent(card) }
}

DomposableCarousel(
    items = items,
    label = localizedCarouselLabel,
    currentItem = selectedPage,
    onCurrentItemChange = onPageChanged,
    onApproachingEnd = loadNextPage,
    pagedLayout = DomposablePagedCarouselLayout(
        compactHeight = 200.dp,
        wideHeight = 280.dp,
        contentInset = 16.dp,
        itemSpacing = 12.dp,
        compactTrailingPeek = 14.dp,
        labels = DomposableCarouselLabels(
            previous = localizedPreviousLabel,
            next = localizedNextLabel,
            itemPosition = localizedPositionLabel,
        ),
        controls = DomposableCarouselControlsAppearance(
            navigationButton = themeCarouselButtonAppearance,
            activeIndicator = DomposableTheme.colors.primary,
            inactiveIndicator = DomposableTheme.colors.outline,
        ),
    ),
)
```

`compactHeight` and `wideHeight` may be omitted for content-sized pages. `breakpoint` defaults to
600 dp; compact and wide content insets, spacing, trailing peek, navigation and indicators are
configured in `DomposablePagedCarouselLayout`. Labels are supplied by the consumer so previous,
next and item-position announcements can be localized. Set `heightTextScaleReference` when fixed
page heights should grow with the BodyMedium text size relative to a reference size. A zero compact
trailing peek makes compact pages full width. `onApproachingEnd` fires when a changed
selection reaches the final two items; empty carousels render nothing and do not invoke callbacks.

Stable `DomposableLazyItem.key` values preserve page identity. `currentItem` synchronizes the visible
page after external selection changes; `onCurrentItemChange` reports user changes. Navigation uses
the supplied button appearance and may use consumer-provided icon slots, while indicator colors
are separately configurable. HTML supports left/right keyboard navigation with RTL-aware direction,
native swipe paging, and semantic slide-position labels.

Navigation arrows reserve their space but are omitted from interaction and accessibility at the
first/last boundary. A single page has no trailing peek, arrows or indicators. Arrow placement is
centered on the page area, excluding indicators. `wideInactiveIndicator` optionally overrides the
inactive dot color at the wide breakpoint; otherwise it inherits `inactiveIndicator`.

Keyboard paging is handled when the HTML track itself has focus. Inputs and other interactive
content inside a slide retain their own arrow-key behavior. Programmatic selection and responsive
resizing preserve the selected page without reporting a new user selection.
