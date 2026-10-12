# Migrating a Compose feature

Keep Compose runtime APIs (`remember`, effects, state, composition locals). Move shared UI to `Domposable*` components and `DomposableModifier`.

Migration needs more than changing imports: props, units, layout constraints and renderer behavior are deliberately portable. Replace native `Modifier` chains with supported portable operations. Move unsupported platform widgets and native interop into provider implementations or platform slots.

Keep your existing ViewModels/state machines and DI containers. Pass values/callbacks or app-owned models through screen signatures. Add screen ownership, Navigation 3 or KSP only where useful; replacing an application's navigation architecture is not required to use the components.

Migrate one feature first. Verify semantics, focus/keyboard behavior and measured content breakpoints on each target. Use the [coverage matrix](coverage.md) to identify controls needing a custom renderer.

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

Use the [component recipes](components.md) and [call-path guide](how-it-works.md) to migrate controls. Columns, rows, grids and tables can use [opt-in windowing](performance.md). Rich text editing, column virtualization, complex date pickers and platform camera/maps still require application or provider-specific implementations; this vocabulary does not claim to mirror every Material API.

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


## Product providers and portable layout after 0.3.0

The next minor version adds a typed extension-provider API, consumer KSP generation in an explicit
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
