# Shared components

Use portable components in feature code and install a provider at the app boundary. The default provider supplies 34 renderers for Compose UI and semantic HTML. [Explore the interactive catalog](https://samuolis.github.io/domposable/demo/#/catalog).

![Shared components on Android and in a browser](assets/examples/components.png)

## Forms and actions

```kotlin
@Composable
fun NewsletterForm(onSubscribe: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var consent by remember { mutableStateOf(false) }
    DomposableColumn(DomposableModifier.padding(16.dp), gap = 12.dp) {
        DomposableText("Stay in the loop", style = DomposableTextStyle.Title)
        DomposableTextField(
            email, { email = it }, "Email address",
            purpose = DomposableInputPurpose.Email,
        )
        DomposableCheckbox(consent, { consent = it }, "Send me occasional news")
        DomposableButton("Subscribe", { onSubscribe(email) }, enabled = consent && email.isNotBlank())
    }
}
```

This sample takes an application callback; Domposable does not send email. In HTML, the field has an associated label and email input type; the button/checkbox use native browser controls. Native uses corresponding Material controls. Put validation in your app and pass `error` to `DomposableTextField`.

## Wrapping controls and links

```kotlin
DomposableFlowRow(gap = 8.dp) {
    categories.forEach { name ->
        DomposableChip(DomposableChipProps(name, { select(name) }, name == selected))
    }
    DomposableLink("Help", "https://example.com/help")
}
```

`FlowRow` wraps on both providers. `Link` opens a URI on native and emits an anchor on HTML. For app navigation, use `DomposableNavigationItem(label, href = url)` with the Navigation 3/history adapter so ordinary clicks stay in the app while new-tab behavior remains available.

## Collections

```kotlin
DomposableLazyGrid(
    DomposableLazyGridProps(
        items = products.map { product ->
            DomposableLazyItem(product.id) {
                DomposableCard {
                    DomposableText(product.name)
                }
            }
        },
        columns = columnsForContentWidth,
        gap = 16.dp,
    ),
    DomposableModifier.fillMaxWidth().height(480.dp),
)
```

Keys must be unique. Native collections virtualize through Compose; the HTML provider currently emits keyed lists/CSS grid without virtualization. Keep a bounded height when nesting a native lazy collection inside another scrolling container. The storefront uses a wrapping row for its small catalogue instead.

## Shell and feedback

`DomposableScaffold` accepts top bar, navigation, content, footer, contextual and overlay slots. HTML emits header/main/footer landmarks with a skip link. Avoid nesting full-window scaffolds inside a page; embed feature columns instead. Use banners for status, dialogs/sheets for focused flows, and tabs for local sections.

| Family | Components |
| --- | --- |
| Layout | Column, Row, FlowRow, Box, LazyColumn, LazyRow, LazyGrid, Spacer, Divider, Scaffold |
| Actions | Button, IconButton, Link, Chip |
| Forms | TextField, Checkbox, Switch, RadioButton, Slider |
| Content | Text, Image, Icon, Card, Surface, Avatar, Badge |
| Feedback | Banner, Progress, Menu, Dialog, BottomSheet |
| Navigation | Tabs, NavigationBar |

See [design-system customization](design-systems.md), [adaptive examples](adaptive.md) and [coverage boundaries](coverage.md).
