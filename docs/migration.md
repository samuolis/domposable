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

Use the [component recipes](components.md) and [call-path guide](how-it-works.md) to migrate controls. Fixed-height columns can use [opt-in windowing](performance.md). Rich text editing, variable-height and large-table virtualization, complex date pickers and platform camera/maps still require application or provider-specific implementations; this vocabulary does not claim to mirror every Material API.

## Fixed-height columns after 0.2.0

The optional `window` parameter keeps existing source calls unchanged, and the existing direct-call binary overloads remain available. It adds a field to `DomposableLazyColumnProps`, changing that data class's constructor and generated `copy` signatures and adding another destructuring component. Consumers compiled against the old props type must recompile when upgrading to the next minor release. Complete custom providers should inspect `props.window` and document whether they honor it. The Material provider implements it on native Compose, HTML and Wasm.
