# Validation and visual review

The required **CI passed** check combines workflow hygiene, Kotlin quality/coverage, Android/desktop/browser validation, iOS compilation/published-consumer validation and the dedicated **Binary API compatibility** job. Fork PRs run with read-only permissions and no release secrets. Tests exercise behavior, API variants and rendering separately.

- Required ktlint, detekt and Android lint checks enforce source formatting and correctness. Kover line/branch gates and individual core package floors prevent coverage regressions; [test coverage](coverage.md) explains scope and reports.
- Shared JVM tests cover adaptive rules, modifier ordering, entry lifetime/saveables and optional ViewModels.
- Android native-graphics Robolectric tests exercise feature input/back navigation and capture the workspace, component menus, storefront and collection layouts.
- iOS device and simulator compilation, binary API compatibility and published-consumer integration remain required. Automatic CI does not boot an iOS simulator or run XCUITest/screenshot regression, including release validation. The existing iOS UI suite remains available for optional local investigation; iOS-specific runtime and visual regressions are consequently outside the automatic gate.
- Browser behavior and axe accessibility checks run in Chromium, Firefox and WebKit. They cover wrapping, semantic landmarks/links, modal focus, tab keyboard behavior, adaptive drafts, hash history, path deep links/reload under a base path, and feature switching on the generated comparison page. Wasm fixture switching is checked in Chromium.
- CI compares **Android, JVM desktop, semantic HTML and Wasm** screenshots. HTML fixtures include phone/tablet/wide views, list/detail and a dialog. Wasm checks startup/errors and phone/wide rendering.
- Kotlin ABI validation checks 22 baselines across all eight published modules: Android/JVM signatures, JS/Wasm/iOS Klibs and the JVM KSP processor. macOS CI compiles all targets with inference disabled, fails on missing/incomplete inventories, and retains compiled dumps plus PR-base/release diffs for 30 days. Two published consumers also compile every runtime artifact's six target variants. Optional Metro/Koin graphs compile on every target; Kobweb exports and browser routes run separately.
- Strict MkDocs/Dokka and both production browser distributions build before website packaging. Version/release/Central recovery tests protect publishing behavior.

## Parallel CI

CI builds the development browser preview once in a dedicated job, without waiting for Android tests, Dokka, integration exports or production packaging. Eight Ubuntu runners split the complete browser suite: six test-level Chromium shards handle the heavier HTML/Wasm comparison matrix, while Firefox and WebKit each run in their own job. Each runner uses two workers. Sharding is weighted by browser because most visual fixtures run only in Chromium; equal shards across all browsers concentrate those expensive tests in a single job. A failed shard does not cancel the others, so diagnostic captures remain available. Their blob reports are merged into one HTML report, and all captures are collected before the complete screenshot inventory is compared.

iOS validation is limited to compilation, ABI and packaged-consumer checks on standard macOS runners. Demo and integration consumers compile for both device and simulator targets. CI does not link the demo simulator framework or launch Xcode UI tests. Kotlin quality, ABI and consumer jobs run independently. The normal PR budget is at most 20 minutes, targeting 10–15 minutes; actual wall time depends on runner availability and cold builds, and must be measured after this change.

The final **CI passed** gate requires every remaining build, shard, evidence comparison, consumer and ABI job to succeed. Recording runs cannot satisfy it. PR inline previews cover the four automatically captured renderers. Same-repository PRs save branch-scoped Gradle caches; fork PRs remain read-only. Intermediate preview artifacts expire after one day; review evidence remains available for 30 days in `android-html-evidence`. The unsigned Maven bundle is retained separately in `published-consumer-evidence`.

Reproduce an individual browser shard after building the preview:

```bash
npx playwright test --project=chromium --shard=1/6 --workers=2 --reporter=list,blob
# Collect blob-report/*.zip from all eight runners before merging:
npx playwright merge-reports --reporter=html blob-report
```

## Where images live

Reviewed golden PNGs live in `screenshots/<platform>/` in **Git**, with an explicit inventory in `screenshots/manifest.json`. They remain available across branches and releases without an expiring artifact or third-party service. PR checks do not overwrite them.

Each run uploads actual images, expected images, pink diff overlays and JSON comparison reports in its evidence artifact. Browser traces/HTML reports, Android/desktop test reports and the APK are also retained by GitHub Actions for **30 days**. Job summaries list each capture and result. Release assets have separate retention rules.

Comparison rejects missing captures/baselines and dimension changes. It tolerates at most 0.05% differing pixels after ignoring channel differences of 12 or less, to handle small renderer rounding. On the pinned iPad's portrait/landscape dimensions, only the system date label at pixels `(150, 16)`–`(340, 48)` is excluded: historical captures retain the runner's calendar date. The clock, battery, app content and safe-area layout remain checked. Other dimensions and platforms have no date exclusion. A failure needs a reviewed fix or baseline update; increasing the threshold to conceal a layout regression is not acceptable.

The visual browser reference is Chromium on Ubuntu 24.04 with Playwright 1.64.0. Desktop and Android references use the same Linux job. iOS pins its runtime/device and status-bar clock/battery, waits for stable full-screen captures, and normalizes orientation metadata. A runner/runtime/font update can require a reviewed baseline refresh. Baselines from a different local OS should not replace the CI references casually.

Production packaging has a separate browser check after `build_site.py`. It serves `build/site` under `/domposable/`, verifies content-hashed JavaScript and Wasm assets, reloads nested HTML routes in Chromium/Firefox/WebKit, and starts the production Wasm shop in Chromium. Development previews remain convenient for ordinary feature tests; these production checks exercise the files that Pages receives.

```bash
./gradlew :demo:jsBrowserDistribution :demo:wasmJsBrowserDistribution dokkaGenerate
python scripts/ci/build_site.py
npx playwright test --config playwright.production.config.ts
```

## Update baselines deliberately

1. Push the implementation branch and run **CI** manually on that branch with `record_screenshots=true`.
2. Download `android-html-evidence`. Inspect every changed image and the behavior reports. Recording exports candidates; it does **not** update Git files.
3. Copy approved `build/screenshots/<platform>/*.png` from the downloaded artifacts to `screenshots/<platform>/`. Keep the manifest synchronized with intended fixtures.
4. Commit the reviewed images to the PR and run normal CI again. A normal run must pass before merge/release.

Recording runs deliberately fail **CI passed**, so they cannot satisfy branch protection or release candidate checks. They still produce artifacts for review. No bot automatically blesses a changed rendering.

For local inspection, build both browser development bundles, run `python scripts/ci/browser_preview.py`, then `npx playwright test`. Android and desktop captures run with `:demo:testAndroidHostTest` and `:demo:jvmTest`. For optional local iOS investigation, first run `python scripts/ci/generate_xcode_project.py` and `./gradlew :demo:linkDebugFrameworkIosSimulatorArm64`, then `python scripts/ci/ios_test.py`; this suite is outside automatic CI. Compare a platform with `python scripts/ci/screenshots.py dom`; use `--record` to inspect candidates without a comparison. On Linux, run desktop tests under `xvfb-run -a`.

ABI changes require a reviewed baseline diff, consumer example and an **API compatibility** explanation. Read [binary compatibility](binary-compatibility.md) for commands, version policy and the limits of these checks. Follow [CONTRIBUTING](https://github.com/samuolis/domposable/blob/main/CONTRIBUTING.md) for toolchain setup. Physical screen-reader review remains deferred; screenshot coverage alone is not accessibility certification.

## Inline PR evidence

Ranker's review format is also used here: action title, scoped architecture bullets plus Mermaid, consumer API example, executable build commands, and relevant validation. For visual changes, embed PNG previews in the PR body. Main | Pixel difference | PR comparisons apply when a scenario has a main baseline; new scenarios get an added preview.

Run **Publish visual review** with a completed CI run ID and the open PR number. It validates provenance against the current PR head, reads bounded PNG captures, fetches baseline images at the exact base revision, and uploads comparisons to the `visual-evidence` GitHub prerelease asset bucket. The job summary and `visual-review-markdown` artifact contain content-addressed image URLs to paste into `Visual evidence`. Each filename records the PR, CI run and SHA-256. The publisher verifies the downloaded PNG bytes and never overwrites an existing asset. Buckets rotate at GitHub’s asset limit; they are excluded from library release/version selection. No image branches are created. Reviewed golden inputs remain in main and full CI captures remain in Actions artifacts. Maintainers also inspect the rendered PR body before marking it ready.

Generated comparisons stay outside main and feature branches. Approved golden inputs remain in `screenshots/`; documentation illustrations can be refreshed from them with `python scripts/ci/export_examples.py`. Recording candidates and publishing review evidence are separate from approving baseline updates.

## Collection gallery fixtures

Automatic visual validation covers 154 fixtures: 20 Android, 20 desktop, 62 HTML and 52 Wasm. The inventory also retains 21 iOS fixtures for optional local review. HTML and Wasm each include 32 phone references covering eight themes in resting, hovered, pressed and focused states. The collection gallery has reviewed fixed-input captures for measured columns, horizontal rows, responsive grids and tables on Android, desktop, iOS and HTML. The iOS suite navigates the same app controls and edits a table cell after capturing all four modes. Wasm has a measured-column visual fixture alongside its interaction checks. Live-model diagnostic IDs in navigation policy previews are intentionally outside deterministic golden inputs.

Open **Components → More components → Large collections lab → More collection recipes** on the native demo. The same menu also opens the input/accessibility lab. Golden candidates must be inspected before committing; the normal PR gate still fails missing or changed baselines.
