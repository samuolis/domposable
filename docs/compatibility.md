# Compatibility and tested boundaries

**Web rendering:** Domposable uses Compose HTML to render a single-page application (SPA) in the browser. It does not provide server-side rendering (SSR). JavaScript composes feature content. The optional [public-page exporter](page-export.md) runs that renderer at build time and writes HTML/CSS for explicit public routes, so those snapshots can show content before JavaScript starts. Export is not SSR; hash/path URLs and ordinary static hosting entry pages alone do not prerender content.

Domposable uses plain semantic versions. Until 1.0, minor releases may change APIs; the table describes this repository's CI coverage. Both browser demos use the **same `DemoApp` feature sources**.

| Target | Renderer | UI/behavior checks | Visual baselines | URL owner |
| --- | --- | --- | --- | --- |
| Android, API 24+ | Compose UI + Material3 | Robolectric API 36 interaction tests; APK build | Robolectric native graphics, 390 × 844 logical pixels | Application |
| iOS arm64 | Compose UI + Material3 | Device compilation; simulator input/rotation tests | iPad mini A17 Pro, iOS 26.5, portrait/landscape | Application |
| iOS simulator arm64 | Same native renderer | XCUITest, unsigned app artifact | Same simulator fixtures | Application |
| JVM desktop | Compose UI + Material3 | JVM tests and compiled consumer | Compose desktop test scene on Ubuntu | Application |
| Kotlin/JS HTML | Native DOM + CSS + browser controls | Chromium, Firefox, WebKit; axe; inputs/focus/history/URLs | Chromium on Ubuntu, phone/tablet/wide | Hash or path history; or host router |
| Kotlin/Wasm JS | Compose UI + Material3 | Chromium startup, history/restoration, Unicode input, password privacy, disabled canvas interactions and modal actions; documented stable-bridge omissions and bounded accessibility regression audit | Chromium on Ubuntu, phone/wide | Hash or path history; or host router |

Desktop and Wasm support are **experimental**. HTML and Wasm share the optional browser-history/saveable binding. Android, iOS and desktop leave URL ownership to the application. The [Wasm input guide](wasm.md) records the pinned Compose bridge's accessibility findings; its regression audit is not a clean WCAG scan. Physical screen-reader review is deferred.

## Pinned build versions

| Dependency | Version |
| --- | --- |
| Kotlin / Compose compiler | 2.4.21 |
| Compose Multiplatform plugin / runtime / UI / HTML | 1.12.2 (stable) |
| AndroidX Compose core / host tests | 1.12.1 (stable) |
| Material3 provider | 1.12.0-alpha03 (separately versioned) |
| Navigation 3 runtime / Compose UI display | 1.1.7 / 1.1.2 |
| KSP | 2.3.12 |
| Android Gradle plugin / Gradle | 9.4.1 / 9.7.1 |
| Metro example | 1.4.5 |
| Kobweb application/export example | 0.25.1 |
| Koin integration example | 4.2.2 |
| Playwright | 1.64.0 |

This is the supported build baseline. Domposable builds against stable Compose core and raises that minimum only when a necessary feature requires it. Material is an optional provider with its own release track; the compatible prerelease Material APIs do not make Compose core alpha. CI checks the resolved packaged-consumer graphs on all six targets and rejects prerelease Runtime, UI, Foundation or Animation artifacts.

Applications may select a compatible newer Compose version or install their own component and modifier renderers. The library does not install global dependency-resolution rules in consumer builds. A version outside this baseline needs application-level verification.

Domposable 0.7.0 was built against Compose 1.13.0-alpha02. Upgrade the library to a build targeting this stable baseline before downgrading the consumer graph; forcing an older runtime beneath the existing 0.7.0 binaries can cause linkage failures.

These are the versions verified together. Applications adopting older/newer Compose, Kotlin or Kobweb versions must compile and test their own dependency graph. All seven runtime libraries publish six target variants; the optional KSP processor runs on JVM and generates shared declarations. CI compiles both a feature consumer and an independent provider against packaged Maven artifacts.

## Rendering differences

HTML menus use the browser Popover API to appear above application stacking
contexts, so the Material provider requires a browser that implements that API.
CI exercises the Chromium, Firefox and WebKit versions bundled with the pinned
Playwright release. HTML menus currently use a viewport-positioned popup;
trigger-anchored placement is not yet shared with native Material menus.

HTML uses headings, anchors, input types, landmarks, lists and dialogs. Wasm uses Compose UI rendering and its accessibility layer. The Material comparison checks geometry, palettes, typography and control states from the same declarations. Text rasterization, browser-native text selection and platform input behavior can vary. HTML collections are keyed DOM lists; columns, rows, grids and tables offer optional [bounded scroll windows](performance.md). Unwindowed collections render all items. Compose UI collections are lazy. Date/time text-field purposes remain text fields; the dedicated date, range and time picker APIs use Material pickers on Compose UI and HTML controls on Kotlin/JS. Application-specific rich-text editors, hinge/fold integrations, drag/reorder and column virtualization remain host-owned.

Domposable supports Android API 24 and newer. The library modules, demo and published-consumer fixtures use the same minimum; applications upgrading must also set `minSdk = 24` or higher.

The Compose plugin, Runtime, UI and Foundation use a stable compatible baseline. The optional Material provider is compiled against that baseline; it must not raise the core to a prerelease version. On Wasm, mixing older compile-time UI dependencies with newer runtime UI can bundle incompatible Skiko JavaScript glue; the startup checks exercise the actual browser bundle.

### Migration to 0.24.0

Native styled buttons again reserve Material's 48dp interaction layout space around
smaller visual faces, restoring the spacing before 0.23.1. A 32dp face stays 32dp;
its parent reserves 48dp. Explicit fixed parent constraints still take precedence.
The `minHeight` appearance property remains a visual minimum, rather than an opt-out
from Material interaction spacing. Semantic HTML button layout is unchanged.

Dialogs now accept an optional `scrimOpacity` from `0f` (clear) to `1f` (black).
Leave it `null` to keep the platform default; other dialog and sheet defaults are unchanged.

```kotlin
DomposableDialog(
    title = "Saving changes",
    onDismiss = {},
    dismissOnBackPress = false,
    dismissOnClickOutside = false,
    scrimOpacity = 0.5f,
) {
    DomposableProgress(label = "Saving changes")
}
```

Adding the property changes the binary signatures of `DomposableDialogProps`
(including its generated `copy`) and the `DomposableDialog` convenience function.
Rebuild all consumers together against 0.24.0; do not mix precompiled 0.23.x callers
with the new UI library. Calls using a trailing content lambda remain source compatible.
If content was passed positionally inside parentheses, use the named `content` argument.
This pre-1.0 binary break requires a minor release.

### Migration from 0.7.0

The stable-core baseline removes `DomposableTimePickerMode.Scroll` and `DomposableDialogVariant.VibrantTimePicker`, which mapped to newer upstream APIs. Use `DomposableTimePickerMode.Dial` or `Input`, and `DomposableDialogVariant.TimePicker`. Other component families remain available on the compatible Material baseline. This is a breaking change and requires a pre-1.0 minor release; rebuild consumers rather than mixing the old and new binaries.

The Wasm comparison retains the stable upstream accessibility bridge with [documented limits](wasm.md). Stable core compatibility does not imply browser accessibility parity; semantic HTML remains the primary web path.
