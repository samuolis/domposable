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

The example retains ordinary drafts through browser restoration. Its sample password stays in memory and is never registered with browser-history saveables; a page refresh clears it. The purpose controls native keyboard input and password masking, while HTML uses a native password input. Domposable also masks the stable Wasm accessibility mirror and sets its focused password backing editor to `type="password"`; CMP 1.12 otherwise leaves that editor as a plain text input.

## Supported baseline and test boundaries

The Wasm comparison uses **Compose Multiplatform 1.12.2**. Its accessibility mirror is experimental and substantially more limited than the bridge used by Domposable 0.7.0, which depended on CMP 1.13 alpha. Semantic HTML is the primary web renderer; use it for browser forms and accessible web applications.

Chromium checks exercise native keyboard text input, Unicode, password masking and exclusion from browser history, controlled callbacks, disabled canvas interactions, navigation draft retention and action/Escape dismissal. Screenshot checks cover the shared layouts. These are functional and visual checks, not a guarantee of accessible Wasm controls.

HTML retains native labelled controls, checked/disabled/error state, modal focus isolation and zero-violation WCAG 2 A/AA checks in Chromium, Firefox and WebKit. Native host tests retain their own form validation, state and dialog assertions.

The comparison host explicitly enables the upstream mirror:

```kotlin
ComposeViewport(document.getElementById("root")!!, configure = {
    isA11YEnabled = true
}) {
    DemoApp()
}
```

## Known stable-bridge limitations

The [CMP 1.12.2 bridge source](https://repo.maven.apache.org/maven2/org/jetbrains/compose/ui/ui-wasm-js/1.12.2/ui-wasm-js-1.12.2-sources.jar), `ComposeWebSemanticsListener`, exports only part of Compose semantics:

| Area | Stable Wasm comparison | Semantic HTML |
| --- | --- | --- |
| Field labels and validation | Field labels and errors are not merged into the editable node's accessible name; `aria-invalid` is absent | Native labels, associated error descriptions and `aria-invalid` |
| Toggles and tabs | Clickable nodes become buttons, overriding their checkbox, switch, radio or tab role | Native controls and appropriate roles |
| Checked and disabled state | `aria-checked` and `aria-disabled` are not exported | Checked and disabled states follow shared values |
| Dialogs | No dialog role, `aria-modal` or inert background; the mirror can retain a dismissed dialog owner. Pointer/Escape dismissal is verified against settled rendered frames | Modal semantics, focus isolation and restoration |
| Assistive click delegates | Handlers and the mirrored tree can remain stale after overlay replacement; dismissal checks use native canvas pointer/keyboard input and rendered-frame comparisons | Native browser event handlers |
| Canvas role | Upstream canvas publishes `role="generic"`, rejected by axe | No canvas mirror needed |
| Headings | Heading role without `aria-level` | Native heading elements |

Tests record these omissions explicitly. They locate unnamed Wasm fields by existing test IDs or a unique enclosing form and verify real input/callback behavior. Pointer checks target the canvas where the old accessibility delegate cannot represent disabled state. Tests do not inject replacement roles, labels or ARIA state into the mirror.

The WCAG audit runs all WCAG 2 A/AA axe rules without disabling any rule. Its exact node descriptors and counts are pinned; a changed or new finding fails CI and requires inspection. The report is written **before** the assertion, including on failure, to `build/accessibility/wasm-input-lab.json` in the Actions artifact. It is a regression audit with known failures, not a clean accessibility scan or conformance certification.

The expected stable input-lab findings are two unnamed editable nodes (`aria-input-field-name`) two headings without levels (`aria-required-attr`) and the upstream canvas role (`aria-roles`). State and modal omissions are checked separately because axe cannot detect every missing semantic. Findings from the 0.7.0 alpha bridge must not be used as evidence for this baseline.

Wasm remains an experimental comparison. Physical screen-reader review is deferred, and these checks do not establish parity with HTML or native assistive technology. No stable 1.0 release is scheduled.

## Reproduce

```sh
./gradlew :demo:jsBrowserDevelopmentWebpack :demo:wasmJsBrowserDevelopmentWebpack
python scripts/ci/browser_preview.py
npx playwright test tests/browser/input-lab.spec.ts
./gradlew :demo:jvmTest --tests '*InputLabTest'
```
