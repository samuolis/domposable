# Shared components

Use direct composable calls in shared feature code and install a provider at the app root. All **41 renderer contracts** have Compose UI and semantic HTML implementations; Wasm uses the Compose UI provider. [How the API works](how-it-works.md) explains the call path and [the catalog](https://samuolis.github.io/domposable/demo/#/catalog) exercises the controls.

![Shared components on Android and in a browser](assets/examples/components.png)

## Forms and selection

```kotlin
@Composable
fun NewsletterForm(onSubscribe: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var consent by remember { mutableStateOf(false) }
    DomposableColumn(DomposableModifier.padding(16.dp), gap = 12.dp) {
        DomposableText("Stay in the loop", style = DomposableTextStyle.Title)
        DomposableTextField(email, { email = it }, "Email address", purpose = DomposableInputPurpose.Email)
        DomposableCheckbox(consent, { consent = it }, "Send me occasional news")
        DomposableButton("Subscribe", { onSubscribe(email) }, enabled = consent && email.isNotBlank())
    }
}
```

The app owns validation and submission. Pass an `error` message to TextField or Select to display it and associate it with the control. HTML uses labelled browser inputs; native uses Material controls.

```kotlin
DomposableSelect(
    value = delivery,
    onValueChange = { delivery = it },
    label = "Delivery",
    options = listOf(
        DomposableSelectOption("standard", "Standard"),
        DomposableSelectOption("express", "Express"),
        DomposableSelectOption("same-day", "Same day", enabled = false),
    ),
)
DomposableSegmentedButtons(
    labels = listOf("Home", "Office"),
    selectedIndex = addressIndex,
    onSelect = { addressIndex = it },
    label = "Delivery address",
)
DomposableRangeSlider(
    value = priceRange,
    onValueChange = { priceRange = it },
    startLabel = "Minimum price",
    endLabel = "Maximum price",
    range = 0f..100f,
    steps = 9,
)
```

Select values must be unique and include the current value. Use an explicit placeholder option when no choice has been made. Native presents a Material dropdown; HTML emits a `<select>` with disabled options and preserves the app's controlled value. SegmentedButtons provides one selected choice with native selection semantics or an HTML radio group. A range has ordered, finite endpoints inside its bounds; `steps = 9` divides 0–100 into ten intervals. HTML displays two labelled browser sliders; native uses Material's two-thumb slider. Both thumbs remain independently accessible.

## Actions and help

```kotlin
DomposableButton("Apply", onApply, style = DomposableButtonStyle.Outlined)
DomposableChip("Favorites", onToggleFavorites, selected = favoritesOnly)
DomposableIconButton(onFavorite, label = "Favorite") { DomposableIcon("♡", description = null) }
DomposableTooltip("Saves this item to your favorites") {
    DomposableIconButton(onFavorite, label = "Favorite") { DomposableIcon("♡", description = null) }
}
DomposableLink("Help", "https://example.com/help")
```

Tooltips provide supplemental descriptions. The control still needs its own accessible label. HTML shows help on hover or keyboard focus and supports Escape; native delegates hover/long-press behavior to Material. Link emits a browser anchor or opens the native URI handler. Navigation items with `href` preserve browser new-tab behavior while ordinary clicks call `onSelect`.

## Content and collections

```kotlin
DomposableCard(onClick = onOpenProduct) {
    DomposableColumn(gap = 8.dp) {
        DomposableImage(product.imageUrl, description = product.name)
        DomposableText(product.name, style = DomposableTextStyle.Title)
        DomposableBadge("New")
    }
}
DomposableLazyGrid(
    items = products.map { product ->
        DomposableLazyItem(product.id) { ProductCard(product) }
    },
    modifier = DomposableModifier.fillMaxWidth().height(480.dp),
    columns = columnsForContentWidth,
    gap = 16.dp,
)
```

`ProductCard` belongs to the feature. Keys must be stable and unique. Compose UI collections virtualize; HTML currently emits all keyed items as lists/CSS grid. Keep a bounded height when nesting a native lazy collection inside another scroll container. The storefront uses wrapping rows for its small catalogue.

## Shell and feedback

```kotlin
DomposableScaffold(
    topBar = {
        DomposableTopAppBar("My shop", actions = {
            DomposableIconButton(onOpenBag, "Open bag") { DomposableIcon("▣", null) }
        })
    },
    bottomBar = { DomposableLink("Help", "https://example.com/help") },
    floatingAction = {
        DomposableFloatingActionButton(onAdd, "Add item") { DomposableIcon("+", null) }
    },
    snackbar = {
        notice?.let { message ->
            DomposableSnackbar(message, actionLabel = "Undo", onAction = onUndo, onDismiss = onDismissNotice)
        }
    },
) {
    ProductsScreen(model)
}
```

Scaffold owns the full-window shell with header/main/footer landmarks on HTML. Embed columns inside its content rather than another full-window scaffold. The app controls whether a snackbar exists and when it expires; Domposable does not create a queue or timer. Supply both `actionLabel` and `onAction` when offering an action. Use Banner for inline status and Progress for determinate or indeterminate work.

```kotlin
if (confirming) {
    DomposableDialog(
        title = "Remove item?",
        onDismiss = { confirming = false },
        actions = {
            DomposableButton("Remove", { confirming = false; onRemove() })
        },
    ) {
        DomposableText("This removes the item from your list.")
    }
}
```

The feature owns dismissal and application effects. HTML uses a modal dialog with focus restoration; native uses Material's dialog. BottomSheet follows the same controlled presentation pattern.

## Complete vocabulary

| Family | Components |
| --- | --- |
| Theme | Theme |
| Layout | Column, Row, FlowRow, Box, LazyColumn, LazyRow, LazyGrid, Spacer, Divider, Scaffold |
| Actions | Button, IconButton, FloatingActionButton, Link, Chip, Tooltip |
| Forms | TextField, Checkbox, Switch, RadioButton, Slider, RangeSlider, Select, SegmentedButtons |
| Content | Text, Image, Icon, Card, Surface, Avatar, Badge |
| Feedback | Banner, Progress, Snackbar, Menu, Dialog, BottomSheet |
| Navigation | TopAppBar, Tabs, NavigationBar |

Every component has direct arguments and a `Props` overload. Keep props for renderer customization or reusable configuration; ordinary feature code can use the calls above. See [design-system customization](design-systems.md), [adaptive examples](adaptive.md) and [platform boundaries](coverage.md).

The [additional controls demo](https://samuolis.github.io/domposable/demo/#/components) shares the same source for native, HTML and Wasm. These additions are on the next-release branch until merged and published; released 0.1.1 has the earlier 34-contract provider.
