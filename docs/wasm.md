# Wasm input and accessibility

The Wasm demo renders the same shared feature sources through Compose UI. Open the input lab as [semantic HTML](https://samuolis.github.io/domposable/demo/#/inputs) or [Wasm](https://samuolis.github.io/domposable/wasm/#/inputs) to compare a labelled form, validation, disabled controls and a help dialog.

```kotlin
var name by rememberSaveable { mutableStateOf("") }
var privatePhrase by remember { mutableStateOf("") }

DomposableTextField(name, { name = it }, "Display name")
DomposableTextField(
    privatePhrase,
    { privatePhrase = it },
    "Private phrase",
    purpose = DomposableInputPurpose.Password,
)
```

The example retains ordinary drafts through browser restoration. Its sample password stays in memory and is never registered with browser-history saveables; a page refresh clears it. The purpose controls native keyboard input and password masking, while HTML uses a native password input.

## Automated guarantees

Chromium Wasm checks exercise the real Compose text-input bridge and accessibility action delegates. They verify Unicode text entry, masked password semantics, private values absent from saved browser history, field error/disabled state, checkbox callbacks, preview feedback, modal background isolation, and action/Escape dismissal. Native JVM tests exercise the same shared form's validation, disabled state and modal actions. HTML runs the native-control flow and a zero-violation WCAG 2 A/AA axe scan in Chromium, Firefox and WebKit.

The host explicitly enables Compose's semantics mirror:

```kotlin
ComposeViewport(document.getElementById("root")!!, configure = {
    isA11YEnabled = true
}) {
    DemoApp()
}
```

The mirror lives in the canvas container's open shadow root. Compose publishes it asynchronously after rendering, so browser tests wait for the control's semantics rather than a fixed sleep or the first canvas frame. Text editing waits for the real backing editor to receive focus and the correct input mode before sending keyboard input. Assistive activation dispatches the mirror's DOM click, which the Compose bridge maps to the control's `OnClick` action; pointer rendering and keyboard input remain separate behavior checks.

## Known bridge limitations

The pinned Compose UI **1.13.0-alpha02** bridge currently emits these WCAG axe findings on the otherwise empty input lab:

| Rule | Concrete finding |
| --- | --- |
| `aria-required-attr` | Two heading nodes omit `aria-level`. |
| `aria-required-parent` | The three navigation tabs have no `tablist` ancestor. |
| `aria-allowed-attr` | The scroll container has `aria-orientation` without a supporting role. |
| `aria-roles` | The canvas exposes `role="generic"`, rejected by the pinned axe role inventory. |

These are observed outputs of the [pinned Compose UI sources](https://repo.maven.apache.org/maven2/org/jetbrains/compose/ui/ui-wasm-js/1.13.0-alpha02/ui-wasm-js-1.13.0-alpha02-sources.jar), including `ComposeWebSemanticsListener`, `A11YImplementationUtils` and the viewport host. The regression audit runs every WCAG 2 A/AA rule without disabling rules and asserts the exact rule/node descriptors above. A new rule, node, label or count fails the check; a dependency update must review this evidence rather than copy an uninspected audit result. The full report is retained at `build/accessibility/wasm-input-lab.json` in the browser Actions artifact for 30 days.

The bridge also exposes supporting validation text in a field's accessible name but currently omits `aria-invalid`. The form test records that omission explicitly and verifies the actual error message. HTML exports `aria-invalid` and the associated error description. Axe cannot discover every missing state automatically, which is why role/state and interaction assertions run separately.

This is a regression audit with documented failures, not a clean accessibility scan or conformance certification. Wasm remains experimental. Physical screen-reader review is deferred, and automated checks do not establish parity with semantic HTML or native assistive technology. No stable 1.0 release is scheduled.

## Reproduce

```sh
./gradlew :demo:jsBrowserDevelopmentWebpack :demo:wasmJsBrowserDevelopmentWebpack
python scripts/ci/browser_preview.py
npx playwright test tests/browser/input-lab.spec.ts
./gradlew :demo:jvmTest --tests '*InputLabTest'
```
