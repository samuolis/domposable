# Design systems

`DomposableTheme` supplies semantic tokens. Built-in components use direct platform implementations. Applications own their component names and can wrap the library controls or provide their own native and HTML implementations.

## Build your own UI

Domposable shares feature UI across native Compose and semantic HTML. Its core provides portable layout, modifiers, component contracts and renderer extension points. Your design system owns the visual language, composition of controls and platform integrations.

Choose the level that fits each component:

- Compose a custom layout from existing shared primitives and controls.
- Configure built-in controls through theme styles and per-call appearance values.
- Wrap a built-in control, or introduce a custom shared component with native and HTML implementations.

Feature code uses the same shared API in each case. A custom control may need platform implementations, but its callers do not need separate native and web screens. The Material catalog is a ready-made design system, not the limit of what a Domposable application can render. Expressive components and theme settings are optional choices within the theme.

## Adopt inside an existing design system

Keep your component names, tokens and feature signatures. Start with [theme defaults and component overrides](#theme-defaults-and-component-overrides) behind those names, delegating ordinary control rendering to the library. Use your own native and HTML implementations at the design-system boundary when a custom component needs a different structure or behavior. A component does not need generated props, a registry or a renderer provider merely to call its own platform implementation.

For standard controls, keep the implementation in `commonMain` too. The surrounding `DomposableMaterialTheme` supplies colors, typography, shapes and component styles to both renderers:

```kotlin
@Composable
fun AppButton(
    label: String,
    onClick: () -> Unit,
    modifier: DomposableModifier = DomposableModifier,
) {
    DomposableButton(onClick = onClick, modifier = modifier) {
        DomposableText(label)
    }
}
```

Import `DomposableButton`, `DomposableText` and `DomposableModifier` from `io.github.samuolis.domposable.ui`. This wrapper needs no application CSS or platform actuals. Compose business components, such as a product summary or account form, from these shared controls in a business or feature module. Package and module ownership distinguish them from the design system; no runtime annotation or registration is required. Business content does not require a custom renderer.

CSS still implements the library's HTML renderer. Application CSS is an extension for a specialized visual or behavior that the shared API cannot express, rather than a second implementation of every standard control. If the missing behavior is generally useful, prefer extending the shared component contract so both renderers can support it.

When an existing text adapter already owns typography, use `DomposableTextStyle.Inherit` rather than another fixed preset. Native text inherits the surrounding Material text style; HTML leaves host classes and CSS typography variables in control, including on semantic headings. Explicit text appearance values still override inherited values. Ordinary text keeps its existing Body, Title or Label default.

For example, this application-owned account summary belongs in a business or feature package and composes the same controls on every target:

```kotlin
@Composable
fun AccountSummary(name: String, onEdit: () -> Unit) {
    DomposableCard(contentPadding = DomposableInsets(16.dp)) {
        DomposableColumn(gap = 8.dp) {
            DomposableText(name)
            AppButton(label = "Edit account", onClick = onEdit)
        }
    }
}
```

Import the component calls from `io.github.samuolis.domposable.ui` and `DomposableInsets`/`dp` from `io.github.samuolis.domposable.foundation`. Localize the action label in the application. No account-specific component or model is added to Domposable.

The following separate native/HTML implementations are an escape hatch for a control that needs its own renderer. Its shared contract lives in `commonMain`:

```kotlin
import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.ui.DomposableModifier

typealias AppModifier = DomposableModifier

@Composable
expect fun AppButton(
    label: String,
    onClick: () -> Unit,
    modifier: AppModifier = AppModifier,
)
```

Native implementation, in a source set shared by Android and iOS:

```kotlin
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.ui.toComposeModifier

@Composable
actual fun AppButton(label: String, onClick: () -> Unit, modifier: AppModifier) {
    Button(onClick = onClick, modifier = modifier.toComposeModifier()) { Text(label) }
}
```

Use your existing branded button body in place of Material's `Button`. The native modifier conversion stays at this boundary. The same contract's `jsMain` implementation emits HTML:

```kotlin
import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.ui.DomposableHtmlModifiers
import io.github.samuolis.domposable.ui.domposableAttributes
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
actual fun AppButton(label: String, onClick: () -> Unit, modifier: AppModifier) {
    DomposableHtmlModifiers(modifier) {
        Button(attrs = {
            domposableAttributes(modifier)
            attr("type", "button")
            classes("app-button")
            onClick { onClick() }
        }) { Text(label) }
    }
}
```

The `app-button` CSS belongs to your design system. Your common feature continues to call `AppButton`, with its existing state, callbacks or injected model. Extend the contract for the states and slots your component already supports, including disabled state and accessibility labels. Keep native-only widgets inside native implementations.

Adopt library form, scroll, layout and pane helpers where they replace generic infrastructure. Keep product-specific editor, diagram, camera and routing behaviour in the app. The [platform modifier callbacks](#extend-portable-modifiers) handle your own portable modifier elements without copying the library's built-in operation model.

## Customize components through your app API

Use your own `AppButton` throughout features and delegate to `DomposableButton`
with your theme styles and per-call appearance values. If a component needs a
different implementation, change your wrapper or its platform implementation.
There is no built-in provider setup and no requirement to implement the whole
catalog. The demo's `DemoButton` shows an app-owned choice between a custom button
and the standard control.

Tokens cover colors, typography, spacing, radius and motion duration. Theme scope maps values to native Material defaults or CSS variables; nested themes remain local. Product-specific dimensions can be supplied through your own composition locals. Typography roles map to the native Material type scale and HTML styles. Native font assets remain supplied by the host; CSS font-family names do not register platform fonts.

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
DomposableTheme(shopTokens) {
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

Import `Button` from `org.jetbrains.compose.web.dom` and `disabled` from `org.jetbrains.compose.web.attributes`. Keep accessible state and portable modifier semantics when replacing a renderer. A DOM renderer must only compose HTML nodes. The [component-wrapper fixture](https://github.com/samuolis/domposable/tree/main/component-wrapper-fixture) demonstrates an ordinary app-owned wrapper.


## Add typed product components

For custom components that need runtime replacement, a product can opt into typed
provider bindings. Define props in shared code. The modifier and callbacks remain
portable. The optional processor emits a provider in your package. This is independent
of built-in rendering and is unnecessary for ordinary app wrappers.

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

See the [modifier guide](modifiers.md) for supported operations, complete native/HTML decoration examples and platform boundaries.

An ordinary Kotlin extension can combine existing operations. It works on every renderer without additional platform code:

```kotlin
fun DomposableModifier.contentGutter(inset: DomposableLength = 16.dp): DomposableModifier =
    fillMaxWidth().padding(inset)

DomposableColumn(DomposableModifier.contentGutter(24.dp)) {
    FeatureContent()
}
```

Import these types and operations from `ui` and `foundation`, as in the theme examples. Parameters are typed values, and chaining returns a new modifier. Reusing a base modifier does not change its previous consumers. Compose built-in operations rather than inspecting or rewriting an existing chain.

Use built-in operations for constraints, shape, border, child weight/alignment and controlled
scrolling. A product-specific operation can implement `DomposableModifierElement` and be appended
with `modifier.then(element)`; the single ordered `elements` chain keeps it in place between
built-in operations.

```kotlin
data class BrandElevation(val level: Int) : DomposableModifierElement {
    init { require(level in 0..3) }
}

fun DomposableModifier.brandElevation(level: Int): DomposableModifier =
    then(BrandElevation(level))
```

`BrandElevation` belongs to the application's design system. Its native and HTML handlers translate each level into the appropriate shadow; the common feature only calls `.brandElevation(2)`.

Native providers consume `toComposeModifier(operationRenderer = ...)`. HTML content decorators use `LocalDomposableHtmlModifierDecorationRenderer` or the explicit `decorationRenderer` parameter to compose markup around the remaining chain. Attribute-only HTML providers consume
`DomposableHtmlModifiers(modifier, operationRenderer = ...)`. Providers can also install
`LocalDomposableNativeModifierRenderer` or `LocalDomposableHtmlModifierRenderer` for their
children. Each custom element needs native and HTML handling. An unhandled element fails visibly;
it is never silently discarded. Keep product token roles in that provider, outside feature code.

Keep the extension boundary small: portable modifiers describe layout, drawing, input and semantics. Component-specific parameters, such as a button's selected colors or a field's error styling, belong in that component's props or appearance. Do not expose mutable internal fields, arbitrary property-name setters or DOM/CSS objects in the shared modifier API. A missing portable behavior can become a typed custom element without changing the framework's built-in operation set.

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
Material control defaults also apply in this mode: buttons inherit the host font family while
using the configured control type scale, dimensions and focus styling. Select controls inherit the
host typography too, instead of the browser's default form font. No `.d-theme` class is needed.
Use `DomposableMaterialTheme` for a standalone application that wants the default full theme.

Custom HTML text hosts can use the same typed appearance as `DomposableText`, including a host
that mixes text with an inline expand/collapse button. No application stylesheet is required:

```kotlin
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTypographyRole
import io.github.samuolis.domposable.material.domposableTextAppearance

Span(attrs = {
    domposableTextAppearance(DomposableTextAppearance(role = DomposableTypographyRole.BodyMedium))
}) {
    Text(description)
    Button(attrs = { onClick { expanded = !expanded } }) { Text("More") }
}
```

The role reads the nearest theme's typography and follows theme variable updates. Explicit
appearance values override that role. Apply the same appearance to any hidden measurement probe
so wrapping and overflow detection use identical metrics. The helper styles the existing element;
the custom component still owns its events, content, measurement and accessibility semantics.

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

## Theme defaults and component overrides

Theme settings are scoped to the composables beneath the theme, rather than mutable global branding. `DomposableMaterialTheme()` supplies Material defaults. Override only the brand values and component styles you need; nested themes inherit their current values.

Unspecified semantic color roles use the same Material baseline in HTML and native rendering. Changing `primary` does not generate a complete palette: provide container, content and surface roles for a full branded or dark scheme. A host using `ProvideDomposableMaterialComponents` should bridge its complete native color scheme into the shared colors, as in the adapter example below.

```kotlin
val brandStyles = DomposableComponentStyles(
    button = DomposableButtonAppearance(
        shape = DomposableShape.Circle,
        minHeight = 48.dp,
        indicatorTrackColor = DomposableColor(0x40FFFFFF),
        contentPadding = DomposableInsets(start = 20.dp, end = 20.dp),
    ),
    textField = DomposableTextFieldAppearance(
        colors = DomposableTextFieldColors(
            outline = DomposableStateColors(
                unfocused = DomposableColor(0xFF74798C),
                focused = DomposableColor(0xFF77503B),
                error = DomposableColor(0xFFBA1A1A),
            ),
        ),
        shape = DomposableShape.Rounded(4.dp),
        floatingLabel = true,
    ),
)

DomposableMaterialTheme(
    colors = DomposableColors(
        primary = DomposableColor(0xFF77503B),
        onPrimary = DomposableColor(0xFFFFFFFF),
    ),
    styles = brandStyles,
) {
    DomposableButton(
        onClick = model::save,
        loading = saving,
        loadingLabel = "Saving changes",
    ) { DomposableText("Save") }

    // Copy the theme default when overriding one value for this control.
    DomposableButton(
        onClick = model::delete,
        appearance = DomposableTheme.styles.button.copy(
            container = DomposableColor(0xFFBA1A1A),
            content = DomposableColor(0xFFFFFFFF),
        ),
    ) { DomposableText("Delete") }
}
```

`indicatorTrackColor` sets the ring track used while a button is loading in both renderers. Leave
it unset to keep the platform renderer's default.

The types above live in `io.github.samuolis.domposable.foundation`; component calls and the current `DomposableTheme` values live in `io.github.samuolis.domposable.ui`. Import `foundation.dp` for portable dimensions and `material.DomposableMaterialTheme` for the standard renderers.

The ordinary theme API accepts `colors`, `typography`, `shapes`, `styles` and `designLanguage`, all optional. `DomposableTheme.colors`, `.typography`, `.shapes` and `.styles` expose the nearest theme. Tokens are the underlying design values; the `DomposableTokens` bundle remains available for custom provider and host integrations, rather than being required for ordinary branding.

Copy the current styles when a section changes just one component family. Its colors, typography, shapes and other component defaults stay inherited:

```kotlin
DomposableMaterialTheme(
    styles = DomposableTheme.styles.copy(
        button = DomposableTheme.styles.button.copy(minHeight = 56.dp),
    ),
) {
    CheckoutActions()
}
```

The enclosing theme resumes after this section. An application adapter can centralize these mappings while feature code keeps its existing component calls.

A null appearance parameter uses the theme's component defaults. Supplying an appearance replaces those defaults for that call; use `copy` to inherit the settings you want to keep. Unspecified values inside an appearance use the renderer's default. Button colors include separate disabled container, content and border values. Text-field palettes group text, label, placeholder, container, outline, cursor, icon and supporting colors by unfocused, focused, disabled and error state. Field appearance also configures slot typography, inner padding, minimum height and optional multiline autosizing. Loading prevents activation, keeps the content's measured size, and shows the provided accessible loading label without switching to disabled colors.

Checkbox, switch and radio appearances configure label and description typography, including text color, control placement, label spacing and `descriptionSpacing`. Use `showLabel = false` when an app-owned row supplies the visible label; the control retains its accessible label without introducing a nested HTML label. Supply a shared `groupName` for related radio buttons to enable native browser arrow-key selection.

Field padding and outline-width overrides apply to the inner decoration on HTML and Compose UI. When no overrides are supplied, native fields keep Material's standard decoration path.

These defaults style the standard Material renderers. A custom provider owns its rendering and must explicitly honor the appearance fields. Native rendering uses Compose UI and HTML rendering uses real browser controls; the renderers share configuration without requiring identical DOM and native widget trees.


The [Material catalog](material-catalog.md) lists the standard and expressive variants and provides examples for selection controls, queued messages, badge anchors and screen chrome.

### Aligning custom control content

Buttons and button-styled links support the same `DomposableButtonAppearance`. A wide drawer link
can align its content to the logical start, while an ordinary action keeps the centered default:

```kotlin
val drawerAction = DomposableButtonAppearance(
    contentAlignment = DomposableAlignment.Start,
    minHeight = 48.dp,
    contentPadding = DomposableInsets(start = 12.dp, end = 12.dp),
    contentSpacing = 12.dp,
)

DomposableLink(
    label = localizedSettings,
    href = "/settings",
    modifier = DomposableModifier.fillMaxWidth(),
    buttonStyle = DomposableButtonStyle.Text,
    buttonAppearance = drawerAction,
) {
    DomposableRow(gap = 12.dp, alignment = DomposableAlignment.Center) {
        SettingsIcon()
        DomposableText(localizedSettings)
    }
}
```

`Start` and `End` follow the layout direction on native and HTML. Custom button content can use a
full-width row and weighted children when it needs a trailing counter or disclosure icon. Loading
keeps the content's measured space and centers the progress indicator independently of content
alignment. Set this appearance once in your app's design-system adapter; features need no CSS.

## Table track spacing

`DomposableTableAppearance.rowSpacing` and `columnSpacing` optionally set the gaps between table rows and columns.
They default to `null`, preserving each renderer's existing size-preset layout. In Compact mode, `rowSpacing` sets
the gap between visible row tracks (including a rendered header), card rows and fields within each card, while
`columnSpacing` sets the label-to-value gap inside each card.

```kotlin
val tableAppearance = DomposableTableAppearance(
    rowSpacing = 0.dp,
    columnSpacing = 8.dp,
    cellPadding = DomposableInsets(top = 4.dp, bottom = 4.dp),
)

DomposableDataTable(
    columns = listOf(DomposableDataTableColumn("name", "Name")),
    rows = listOf(DomposableDataTableRow("Ada Lovelace")),
    rowHeaderColumnKey = "name",
    appearance = tableAppearance,
) { row, _ ->
    DomposableText(row.key)
}
```
