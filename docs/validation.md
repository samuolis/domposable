# Validation and visual review

The required **CI passed** check combines Android/desktop/browser validation and native/published-consumer validation. Fork PRs run with read-only permissions and no release secrets. Tests exercise behavior, API variants and rendering separately.

- Shared JVM tests cover adaptive rules, modifier ordering, entry lifetime/saveables and optional ViewModels.
- Android native-graphics Robolectric tests exercise feature input/back navigation and capture three scenes.
- iOS XCUITest verifies native input, rotation and recipe navigation, and captures portrait/landscape scenes on iPad mini A17 Pro with **iOS 26.5**.
- Browser behavior and axe accessibility checks run in Chromium, Firefox and WebKit. They cover wrapping, semantic landmarks/links, modal focus, tab keyboard behavior, adaptive drafts, hash history and path deep links/reload under a base path.
- Visual comparisons cover **Android, iOS, JVM desktop, semantic HTML and Wasm**. HTML fixtures include phone/tablet/wide views, list/detail and a dialog. Wasm checks startup/errors and phone/wide rendering.
- Kotlin ABI baselines and two published consumers cover every runtime artifact's six target variants. Optional Metro graphs compile on every target; the Kobweb page wrapper compiles on JS.
- Strict MkDocs/Dokka and both production browser distributions build before website packaging. Version/release/Central recovery tests protect publishing behavior.

## Where images live

Reviewed golden PNGs live in `screenshots/<platform>/` in **Git**, with an explicit inventory in `screenshots/manifest.json`. They remain available across branches and releases without an expiring artifact or third-party service. PR checks do not overwrite them.

Each run uploads actual images, expected images, pink diff overlays and JSON comparison reports in its evidence artifact. Browser traces/HTML reports, native test reports/xcresults, the APK and the unsigned simulator app are also retained by GitHub Actions for **30 days**. Job summaries list each capture and result. Release assets have separate retention rules.

Comparison rejects missing captures/baselines and dimension changes. It tolerates at most 0.05% differing pixels after ignoring channel differences of 12 or less, to handle small renderer rounding. A failure needs a reviewed fix or baseline update; increasing the threshold to conceal a layout regression is not acceptable.

The visual browser reference is Chromium on Ubuntu 24.04 with Playwright 1.64.0. Desktop and Android references use the same Linux job. iOS pins its runtime/device and status-bar time/battery. A runner/runtime/font update can require a reviewed baseline refresh. Baselines from a different local OS should not replace the CI references casually.

## Update baselines deliberately

1. Push the implementation branch and run **CI** manually on that branch with `record_screenshots=true`.
2. Download `android-html-evidence` and `native-evidence`. Inspect every changed image and the behavior reports. Recording exports candidates; it does **not** update Git files.
3. Copy approved `build/screenshots/<platform>/*.png` from the downloaded artifacts to `screenshots/<platform>/`. Keep the manifest synchronized with intended fixtures.
4. Commit the reviewed images to the PR and run normal CI again. A normal run must pass before merge/release.

Recording runs deliberately fail **CI passed**, so they cannot satisfy branch protection or release candidate checks. They still produce artifacts for review. No bot automatically blesses a changed rendering.

For local inspection, build both browser development bundles, run `python scripts/ci/browser_preview.py`, then `npx playwright test`. Native captures run with `:demo:testAndroidHostTest`, `:demo:jvmTest` and `python scripts/ci/ios_test.py`. Compare a platform with `python scripts/ci/screenshots.py dom`; use `--record` to inspect candidates without a comparison. On Linux, run desktop tests under `xvfb-run -a`.

ABI changes require `./gradlew updateKotlinAbi` and review of the generated diff. Follow [CONTRIBUTING](https://github.com/samuolis/domposable/blob/main/CONTRIBUTING.md) for toolchain setup. Automated checks supplement manual keyboard, VoiceOver/TalkBack, touch and real-device reviews; screenshot coverage alone is not accessibility certification.
