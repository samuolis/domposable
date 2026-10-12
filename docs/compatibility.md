# Compatibility and tested boundaries

Domposable uses plain semantic versions. Until 1.0, minor releases may change APIs; the table describes this repository's CI coverage. Both browser demos use the **same `DemoApp` feature sources**.

| Target | Renderer | UI/behavior checks | Visual baselines | URL owner |
| --- | --- | --- | --- | --- |
| Android, API 24+ | Compose UI + Material3 | Robolectric API 36 interaction tests; APK build | Robolectric native graphics, 390 × 844 logical pixels | Application |
| iOS arm64 | Compose UI + Material3 | Device compilation; simulator input/rotation tests | iPad mini A17 Pro, iOS 26.5, portrait/landscape | Application |
| iOS simulator arm64 | Same native renderer | XCUITest, unsigned app artifact | Same simulator fixtures | Application |
| JVM desktop | Compose UI + Material3 | JVM tests and compiled consumer | Compose desktop test scene on Ubuntu | Application |
| Kotlin/JS HTML | Native DOM + CSS + browser controls | Chromium, Firefox, WebKit; axe; inputs/focus/history/URLs | Chromium on Ubuntu, phone/tablet/wide | Hash or path history; or host router |
| Kotlin/Wasm JS | Compose UI + Material3 | Canvas startup; navigation, back/forward and saveable restoration in Chromium | Chromium on Ubuntu, phone/wide | Hash or path history; or host router |

Desktop and Wasm support are **experimental**. HTML and Wasm share the optional browser-history/saveable binding. Android, iOS and desktop leave URL ownership to the application. Wasm screenshots do not establish screen-reader parity. Native platform accessibility still needs manual assistive-technology testing.

## Pinned build versions

| Dependency | Version |
| --- | --- |
| Kotlin / Compose compiler | 2.4.21 |
| Compose Multiplatform plugin / runtime / UI / HTML | 1.13.0-alpha02 |
| Material3 provider | 1.13.0-alpha02 |
| Navigation 3 runtime / Compose UI display | 1.1.7 / 1.1.2 |
| KSP | 2.3.12 |
| Android Gradle plugin / Gradle | 9.4.1 / 9.7.1 |
| Metro example | 1.4.5 |
| Kobweb application/export example | 0.25.1 |
| Koin integration example | 4.2.2 |
| Playwright | 1.64.0 |

These are the versions verified together. Applications adopting older/newer Compose, Kotlin or Kobweb versions must compile and test their own dependency graph. All seven runtime libraries publish six target variants; the optional KSP processor runs on JVM and generates shared declarations. CI compiles both a feature consumer and an independent provider against packaged Maven artifacts.

## Rendering differences

HTML uses headings, anchors, input types, landmarks, lists and dialogs. Wasm uses Compose UI rendering and its accessibility layer. They share declarations and behavior intent, not pixels or browser-native text selection. HTML collections are keyed DOM lists, while Compose UI collections are lazy; HTML virtualization remains future work. Native date/time purposes currently use text fields. Hinge/fold information, rich text, drag/reorder and large-table virtualization are not bundled.

Material 3 1.13.0-alpha02 requires Android API 24. The library modules, demo and published-consumer fixtures use the same minimum; applications upgrading must also set `minSdk = 24` or higher.

The Compose plugin, UI and foundation are aligned with Material 3’s dependency version. On Wasm, mixing older compile-time UI dependencies with newer runtime UI can bundle incompatible Skiko JavaScript glue; the startup checks exercise the actual browser bundle.
