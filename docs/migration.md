# Migrating a Compose feature

## Responsive exported pane scaffolds in 0.23

`DomposablePaneScaffold` adds the optional `collapseBelow` breakpoint for exported HTML pages
whose captured `ListDetail` layout should collapse without JavaScript. It defaults to `null`, so
existing layouts keep their behavior. This changes the compiled scaffold signature: recompile
consumers for 0.23. Named-argument calls and trailing content lambdas keep their source form; calls
passing both `contentKey` and `content` positionally should name `content` or move it to a trailing
lambda. Native layout continues to follow the resolved `layout`.

## Bottom-sheet anchors in 0.19

`DomposableBottomSheet` and `DomposableBottomSheetProps` now accept
`skipPartiallyExpanded`, defaulting to `false` on native and HTML. Long compact sheets can
expand from their partial anchor; pass `true` when the sheet should open expanded.
Short sheets continue to size to their content, and desktop dialogs retain their bounds.

Recompile consumers and custom providers when adopting 0.19: the added props field changes
the data-class constructor and generated `copy` signatures, and the convenience composable
has an additional parameter. Existing Kotlin source calls keep their defaults.

HTML rows with a filling weighted child also claim the available width inside start-aligned
boxes, matching native measurement. Remove caller-specific fill-width workarounds added only
to compensate for that renderer difference. Explicit widths, non-filling weights and
horizontal scrolling retain their existing sizing contracts.

## Disabled selected item styling in 0.16

`DomposableItemColors` adds optional `disabledSelectedContainer`, `disabledSelectedContent` and
`disabledSelectedBorder` overrides. Leaving them null preserves existing component styling. Kotlin
source calls keep their defaults, but the data-class constructor and `copy` binary signatures change:
recompile consumers and custom providers when upgrading to 0.16. See the
[component appearance example](material-catalog.md#component-defaults-and-local-overrides).

## Button indicator track in 0.14

`DomposableButtonAppearance.indicatorTrackColor` optionally sets the loading ring's track on
native and HTML renderers. `null` keeps each renderer's existing default. Since this adds a field
to a public data-class constructor and generated `copy`, previously compiled consumers must be
rebuilt with 0.14; Kotlin source calls keep their defaults.

Keep Compose runtime APIs (`remember`, effects, state, composition locals). Move shared UI to `Domposable*` components and `DomposableModifier`.

Migration needs more than changing imports: props, units, layout constraints and renderer behavior are deliberately portable. Replace native `Modifier` chains with supported portable operations. Move unsupported platform widgets and native interop into provider implementations or platform slots.

Keep your existing ViewModels/state machines and DI containers. Pass values/callbacks or app-owned models through screen signatures. Add screen ownership, Navigation 3 or KSP only where useful; replacing an application's navigation architecture is not required to use the components.

If your app already has shared component names, start with [native/HTML design-system adapters](design-systems.md#adopt-inside-an-existing-design-system). Keep the feature API and shared brand configuration, alias the portable modifier behind your own modifier name, and delegate ordinary rendering and generic infrastructure to the library. Retain platform renderer bodies for specialized controls that the shared API cannot express. A second props/provider layer around every application component is unnecessary for this path.

Use Kotlin/JS with Compose HTML for the web host. Wasm is an optional separate Compose UI renderer, not a prerequisite for HTML and not a conversion step for existing native widgets.

Migrate one feature first. Verify semantics, focus/keyboard behavior and measured content breakpoints on each target. Use the [coverage matrix](coverage.md) to identify controls needing a custom renderer.

## Variable-font weights

`DomposableFontWeight` is now a validated value type rather than an enum, so shared text and
component appearances can use exact variable-font weights such as `DomposableFontWeight(750)`
on native and HTML. The named `Thin` through `Black` values remain available. Recompile consumers;
replace enum `entries`/`valueOf` usage with your app's explicit preset list and handle arbitrary
weights in any previous exhaustive `when`. This is an intentional pre-1.0 breaking change requiring
a minor release. Brand typography should configure weight through the theme rather than CSS.

## Independent overlay alignment

`DomposableBox` now accepts `verticalAlignment`, defaulting to `alignment`. Existing calls keep their
previous top-start, centre or bottom-end placement. Specify the two axes for the other six combinations.
The added `DomposableBoxProps` field changes constructor and `copy` binary signatures: recompile consumers
and use a minor pre-1.0 release. The existing direct composable overload is retained, including positional content arguments.

## Shared interaction appearance

`DomposableButtonAppearance` adds optional `disabledOpacity` and `interaction` values. Null keeps
previous rendering. The interaction model configures state colors, state-layer opacity and focus
ring geometry for buttons, icon buttons and button-styled links. Its added data-class fields change
constructor/copy signatures: recompile consumers as part of this pre-1.0 minor release.

## Styled links and custom modal headers

`DomposableLink` can opt into shared button geometry and select a `DomposableButtonStyle` while retaining
anchor navigation on HTML. Dialog and bottom-sheet props can replace the default title/close pair with
`headerContent`; apps that provide it own the heading semantics and localized dismiss label. The new
optional fields preserve source calls after recompilation, but change props constructor/copy signatures.
Recompile consumers and custom providers and include this pre-1.0 ABI change in a minor release.

`DomposableModalLayout` adds explicit surface bounds, viewport insets, alignment and sheet swipe
control to both renderers. Existing defaults retain the library's normal presentation; apps should
set only the layout values required by their design. Its new optional `sectionSpacing` field keeps
the HTML default when omitted and controls section gaps in HTML and native Basic/FullScreen dialogs
and sheet content. Native sheet drag handles and Material alert/picker variants retain their
platform-defined spacing. This data-class field is source-compatible through its default, but changes constructor and generated
`copy` signatures; precompiled consumers must be rebuilt. Treat the API change as a minor release
before 1.0. HTML modal dismissal now checks both pointer start and end positions, and its fallback path blocks background interaction and traps focus when
native `showModal()` is unavailable.

## Layout arrangements

`DomposableTextStyle.Inherit` is an optional addition for adapters that already own text styling. Existing presets are unchanged. Custom providers with an exhaustive text-style mapping must add the new enum case: inherit native ambient typography or HTML host typography, then apply explicit appearance overrides. Inherited HTML headings retain their heading semantics without forcing title typography.

The shared `DomposableColumnProps`, `DomposableRowProps` and `DomposableFlowRowProps` models now include arrangement options, and FlowRow adds line spacing and cross-axis alignment. Existing Row/Column calls keep their layout defaults after recompilation. HTML FlowRow now defaults to top alignment rather than stretching, matching native; specify a different `alignment` when needed. The added data-class fields change constructor and generated `copy` signatures. Rebuild previously compiled consumers; this is an intentional pre-1.0 binary break and belongs in a minor release, not a patch. Direct composable overloads retain their prior positional parameters and trailing-lambda calls.

## Data-table track spacing

`DomposableTableAppearance` adds optional `rowSpacing` and `columnSpacing` values for shared data-table renderers. Null values keep the existing size-preset layout. Set them to tune gaps between rows and columns; Compact mode also applies `rowSpacing` between fields within each card, while `columnSpacing` controls each card's label-to-value gap. `cellPadding` continues to control content insets.

```kotlin
val appearance = DomposableTableAppearance(
    rowSpacing = 0.dp,
    columnSpacing = 8.dp,
    cellPadding = DomposableInsets(top = 4.dp, bottom = 4.dp),
)

DomposableDataTable(
    columns = listOf(DomposableDataTableColumn("name", "Name")),
    rows = listOf(DomposableDataTableRow("Ada Lovelace")),
    rowHeaderColumnKey = "name",
    appearance = appearance,
) { row, _ ->
    DomposableText(row.key)
}
```

The new data-class fields preserve source calls after recompilation but change constructor and generated `copy` signatures. Rebuild previously compiled consumers; this pre-1.0 binary break requires a minor release.

## HTML navigation sizing and adaptive sheets

HTML `DomposableNavDisplay` relays its host's allocated flex space through each entry while honoring explicit height modifiers. Entry content owns its scrolling; callers that relied on the display's previous implicit scroll area should add `verticalScroll()` to that content. This layout correction does not change the public API.

`BottomOnCompact` sheets now center in expanded view and hide their drag handle there. Compact view retains bottom placement and swipe dismissal. Header dragging is disabled whenever the handle is hidden.

## Anchored and rich-text menus

The menu API now accepts an optional `anchor` slot, `contentSpacing`, and `anchorOffset`; menu items also accept optional `textContent`. These fields extend `DomposableMenuProps` and `DomposableMenuItem`, changing their data-class constructor and generated `copy` signatures. Existing source calls retain their defaults after recompilation; previously compiled consumers must rebuild. This is an intentional pre-1.0 binary break and requires a minor release. The rich text slot has a separate overload. HTML adapters that already own a trigger element can use the JS-only `htmlMenuAnchor` modifier to resolve an existing trigger from the rendered menu element without wrapping or moving that trigger.

Menu content items now leave dismissal to the caller on HTML, matching native Material. If an HTML content action relied on implicit dismissal, set `expanded = false` inside its `onClick`. The `items` convenience list still dismisses before invoking an action. Outside interactions request dismissal once; leaving the menu with keyboard focus keeps the next control focused.

Elevated Filter, Assist and Suggestion chips now keep Material's border-free default on native. Explicit appearance borders remain honored. HTML Input chip padding follows its icon slots, and Assist/Suggestion chips expose action semantics rather than `aria-pressed` toggle state. Explicit per-call padding and selectable Filter/Input semantics remain available.

## Direct calls and the expanded provider

Feature code can replace `DomposableChip(DomposableChipProps(label, onClick, selected))` with `DomposableChip(label, onClick, selected = selected)`. Existing props overloads remain available. The demo and modular shop use direct calls throughout; props stay at custom renderer boundaries.

Upgrading from the 0.1.x provider expands it from 34 to 41 required contracts. A provider built entirely by your application must implement `topAppBar`, `floatingActionButton`, `snackbar`, `select`, `segmentedButtons`, `rangeSlider` and `tooltip`. This changes the `DomposableComponents` constructor, copy and generated destructuring ABI; rebuild consumers and update complete custom providers when upgrading. Access renderers by their named properties rather than destructuring the provider. A provider copied from `rememberDomposableMaterialComponents()` inherits the new defaults. Component users do not need KSP.

| Native Compose | Shared call | HTML implementation |
| --- | --- | --- |
| TopAppBar | DomposableTopAppBar | Heading with navigation/action slots |
| FloatingActionButton | DomposableFloatingActionButton | Labelled button |
| Snackbar | DomposableSnackbar | Status message with action/dismiss buttons |
| DropdownMenu plus trigger | DomposableSelect | Labelled select/options |
| SingleChoiceSegmentedButtonRow | DomposableSegmentedButtons | Native radio group |
| RangeSlider | DomposableRangeSlider | Two labelled range inputs |
| TooltipBox / PlainTooltip | DomposableTooltip | Hover/focus help with Escape dismissal |

Use the [component recipes](components.md) and [call-path guide](how-it-works.md) to migrate controls. Columns, rows, grids and tables can use [opt-in windowing](performance.md). Rich text editing, column virtualization and platform camera/maps remain application or provider-specific implementations. The current [Material catalog](material-catalog.md) adds shared date, date-range and time picker families; it maps component variants without copying every upstream overload.

## Collections and tables after 0.2.0

Existing direct calls keep their defaults and old binary overloads remain available. Version 0.3.0 adds `Table` as the 42nd required renderer and expands collection props: `DomposableLazyColumnProps.window`, `DomposableLazyRowProps.window`, `DomposableLazyGridProps.window` and `DomposableListWindow.measureRows`. Their constructors and generated `copy` signatures change. Recompile consumers when upgrading; this is a pre-1.0 ABI break even though ordinary source calls still compile.

Complete custom providers must add a `table` renderer and decide how to honor the row/grid/list window contracts. A provider copied from `rememberDomposableMaterialComponents()` inherits the new implementation. The Material provider supports all four collection modes on native Compose, HTML and Wasm. Access provider slots by name, rather than destructuring generated `componentN` functions.

```kotlin
val defaults = rememberDomposableMaterialComponents()
val branded = remember(defaults) {
    defaults.copy(button = MyButtonRenderer) // Table and collection windows retain the defaults.
}
DomposableTheme(components = branded) { ProductScreen(model) }
```


## Product providers and portable layout in 0.4.0

The 0.4 API adds a typed extension-provider API, consumer KSP generation in an explicit
package, expanded text/input props, and portable layout/shape/scroll operations. Keep existing
component names and branded rendering while moving feature modifiers to `DomposableModifier`.
Product renderer implementations may still use native `Modifier` internally; shared feature
signatures must use the portable type.

`DomposableModifier.elements` is the authoritative ordered view, including application elements.
`operations` remains a legacy view of built-in operations. Custom providers that iterate the old
view must migrate to `elements`; otherwise they would omit product behavior. Handle custom
elements explicitly with the platform adapter callback. The library's adapters reject unsupported
custom elements.

New optional fields change data-class constructor/copy signatures in text and input props.
Existing Kotlin calls retain defaults after recompilation, but previously compiled consumers must
rebuild. This is an intentional pre-1.0 ABI change requiring a minor release; do not publish it as
a patch. Complete providers and packaged consumers are compiled as separate verification gates.

Complete 0.4 providers must add `form`, `disclosure`, `richHtml`, `lineChart`, `valueChart` and
`dataTable`. Copying the Material defaults inherits these renderers. Constructor, `copy` and
component indexes of `DomposableComponents` change; rebuild providers and access slots by name.
`DomposableSegmentedButtonsProps` now carries per-option enabled state/test tags, a group name,
selected-icon behavior and reselect policy. Its selected index may be `-1` when no value matches.
Prefer the typed option overload when your feature uses an enum, ID or another typed value.

`toComposeModifier` now accepts an optional application-element renderer, and the HTML adapter
has the equivalent callback. Both have changed compiled signatures. Recompile native/HTML custom
providers, install scoped Row/Column/Box handling for child weight/alignment, and install product
modifier callbacks at the theme boundary. Unsupported custom elements fail explicitly.


Custom HTML scroll renderers should install `domposableScrollState(state, orientation)` in their
attributes. This JS helper preserves the host's element/CSS and supplies the library's animation,
position observation and disposal behavior. Replace renderer code that mirrors `state.value`
into element offsets or calls `scrollTo` from browser scroll events. See the complete
[custom HTML scroll example](design-systems.md#scroll-state-in-custom-html-renderers).

## Material catalog and theme defaults

Version 0.7 expands the Material 3 and expressive catalog, groups component defaults in `DomposableComponentStyles`, and adds optional per-call overrides. Existing Kotlin calls retain defaults. The props/token/text-appearance data-class constructors and `copy` methods change their binary signatures; recompile consumers and custom providers together when upgrading. See [theme defaults and component overrides](design-systems.md#theme-defaults-and-component-overrides).

Keep application component names as thin adapters. Map brand tokens into `DomposableButtonAppearance` or `DomposableTextFieldAppearance`, and call the standard control instead of maintaining another HTML button/input implementation. Preserve custom editor, diagram and camera renderers; they are separate from ordinary control styling.

HTML controls now use Material default geometry, including switch tracks, chip surfaces and floating field labels. Set `floatingLabel = false` explicitly for a separate HTML field label. Existing per-call appearances still override family defaults; keep application-specific geometry and colors in those adapters.


The expanded catalog also adds per-family defaults under `DomposableTokens.components`, the Material color/type/shape scales and `designLanguage`. Per-call appearance overrides replace a family default; copy that default when changing only one field. Existing explicit providers must implement the new slots or inherit them from the Material provider. KSP remains optional for component consumers.

This is a pre-1.0 **breaking ABI change**, requiring a minor release and recompilation. Props constructors, `copy`, generated provider signatures and several direct-call signatures changed. Prefer named optional arguments and named provider properties. For example, pass `cellContent = { ... }` by name when constructing data-table props positionally; the direct composable still accepts its content as the trailing lambda. Do not mix old compiled adapters with the new artifacts.

`DomposableScreenScaffold` now has a stateless overload for caller-owned state and chrome. Keep an existing application scaffold when it already handles insets and ownership correctly; delegate the library pieces it needs. The optional loading/error overload is a convenience, not a required screen contract.


Theme configuration now groups **all** component defaults, including buttons and text fields, in `DomposableComponentStyles`. Replace `DomposableTokens(button = ..., textField = ...)` with `DomposableMaterialTheme(styles = DomposableComponentStyles(button = ..., textField = ...))`. Custom host integrations can use `DomposableTokens(components = DomposableComponentStyles(...))`. Access defaults through `DomposableTheme.styles`; the former `tokens.button` and `tokens.textField` fields move to `tokens.components`.

## App-owned modifiers and text sizing

Existing three-argument `DomposableHtmlModifiers(modifier, operationRenderer, content)` calls remain supported. Use the new overload with a named `decorationRenderer` only when an app-owned modifier needs HTML/SVG or other markup around its content. CSS and attribute changes on a wrapper can keep using the attribute handler. See [custom modifier examples](modifiers.md#render-custom-html-and-css).

`DomposableTextAppearance(minLines = ...)` reserves a minimum number of text lines on native and HTML. It defaults to one; existing HTML text does not acquire a minimum-height override. If you also set `maxLines`, it must be at least `minLines`. Shared scale, rotation, shadow and min/max dimension helpers are optional additions; transformed content still needs room around it in its parent.

## Portable composition and caller-owned history in 0.9

Image cards can use the same `DomposableBox`, `DomposableImage`, gradient background and overlay modifiers on native and HTML. Remove application grid-area, absolute scrim and pointer passthrough CSS when replacing those renderers with the shared composition. Keep business content and callback policy in the app. `DomposablePaneScaffold` likewise replaces generic pane sizing, labelled regions and hidden-list retention; callers retain route identity, screen access and ViewModel lifetime.

An existing router can adopt the [caller-owned browser-history binding](caller-owned-browser-history.md) without adopting `DomposableNavigator`, the screen registry or KSP. Provide a snapshot codec and URL policy, then restore application state from its callback. The library handles physical history positions and retained branches; app-specific asynchronous scroll restoration stays with the app.

Row, badge, avatar and segmented-button props gain optional layout/content fields. Ordinary source calls retain defaults after recompilation. Their constructor/copy signatures and affected convenience overloads change, so this is a pre-1.0 **breaking ABI change** requiring the 0.9 minor release. Recompile applications and custom providers together. Custom providers must honor supplied content slots, equal-width segments and stretched rows or inherit the Material defaults. New modifier operations require custom exhaustive renderer mappings to be updated; the stock native and HTML adapters already support them.

Text with independently styled runs can use `DomposableText(runs = ...)`. Unspecified run values
inherit the surrounding text style, and `contentAlpha` changes only that run's foreground. Direct
`DomposableTextProps` construction must supply `text` equal to the concatenated run text, keeping
renderer content and accessibility text consistent. Existing plain text calls remain unchanged.

`DomposableTabs(labels = ...)` remains source-compatible. Migrate to `DomposableTabs(items = ...)`
when tabs need stable keys, per-tab enabled state, accessible names, test tags, icons, badges, or
individual text appearance. Supply a localized `label` for the tab list; keyboard navigation wraps
among enabled tabs and supports Home and End.

The next minor release adds optional paged carousel layout, keyed tab items, viewport-height
modifiers and button content alignment. Constructors/copy methods and affected composable
signatures change: recompile consumers and custom providers together. Existing label-only tabs,
Material carousel variants and centered button defaults remain available. New modifier operations
must be handled by custom exhaustive adapters; stock Compose UI and HTML adapters handle them.

Use `contentAlignment = DomposableAlignment.Start` in shared button/link appearance instead of
HTML `justify-content` overrides. Custom content slots inherit the button text appearance, and
full-width rows can lay out weighted labels and trailing content on both renderers. See the
[design-system example](design-systems.md#aligning-custom-control-content).
