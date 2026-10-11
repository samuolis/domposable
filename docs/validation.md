# Validation of the bootstrap implementation

Locally verified on macOS with JDK 21, Xcode 26.6 and Android SDK 37.2:

- Shared modifier ordering, adaptive boundaries, entry saveables/resource disposal and mutation policies.
- Optional AndroidX ViewModel factory, SavedStateHandle restoration into a new model and clearing the old model.
- Native Android task input and Navigation 3 through Robolectric 4.17.
- iOS simulator task detail notes retained after orientation changes, with screenshot attachments.
- Twelve Playwright tests across Chromium, Firefox and WebKit: real HTML controls, keyboard tabs, dialog focus, custom providers, accessibility scans, adaptive panes, history, reload and application Back/browser Forward.
- Checked-in Kotlin ABI baselines on JVM, Android, JS and native variants.
- Forty-three Maven publications across eight artifacts, with POM metadata, sources and generated documentation.
- Independent consumers resolving local Maven artifacts on Android, JVM, JS, iOS device arm64 and iOS simulator arm64; generated KSP registration and a full provider fixture without Material.
- Strict documentation build, Dokka API site and production HTML demo bundle.
- Seventeen Python tests for version progression, candidate/CI guards, immutable asset checks, main races and Central recovery.

The workflows are checked with actionlint 1.7.12, with its unsupported `concurrency.queue` schema diagnostic excluded. GitHub documents `queue: max` for FIFO release serialization; no other workflow diagnostics are excluded.

Live GitHub Actions, Apple/Android release signing, GitHub App protection bypass, Maven Central publication and Pages deployment remain unverified until repository setup and release credentials are supplied. No signed release has been published by the implementation session.
