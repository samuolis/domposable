# Validation and visual review

The required **CI passed** check combines workflow hygiene, Kotlin quality/coverage, Android/desktop/browser validation and native/published-consumer validation. Fork PRs run with read-only permissions and no release secrets. Tests exercise behavior, API variants and rendering separately.

- Required ktlint, detekt and Android lint checks enforce source formatting and correctness. Kover line/branch gates and individual core package floors prevent coverage regressions; [test coverage](coverage.md) explains scope and reports.
- Shared JVM tests cover adaptive rules, modifier ordering, entry lifetime/saveables and optional ViewModels.
- Android native-graphics Robolectric tests exercise feature input/back navigation and capture four scenes, including the storefront.
- iOS XCUITest verifies native input, rotation and recipe navigation, and captures portrait/landscape scenes on iPad mini A17 Pro with **iOS 26.5**.
- Browser behavior and axe accessibility checks run in Chromium, Firefox and WebKit. They cover wrapping, semantic landmarks/links, modal focus, tab keyboard behavior, adaptive drafts, hash history, path deep links/reload under a base path, and feature switching on the generated comparison page. Wasm fixture switching is checked in Chromium.
- Visual comparisons cover **Android, iOS, JVM desktop, semantic HTML and Wasm**. HTML fixtures include phone/tablet/wide views, list/detail and a dialog. Wasm checks startup/errors and phone/wide rendering.
- Kotlin ABI baselines and two published consumers cover every runtime artifact's six target variants. Optional Metro graphs compile on every target; the Kobweb page wrapper compiles on JS.
- Strict MkDocs/Dokka and both production browser distributions build before website packaging. Version/release/Central recovery tests protect publishing behavior.

## Where images live

Reviewed golden PNGs live in `screenshots/<platform>/` in **Git**, with an explicit inventory in `screenshots/manifest.json`. They remain available across branches and releases without an expiring artifact or third-party service. PR checks do not overwrite them.

Each run uploads actual images, expected images, pink diff overlays and JSON comparison reports in its evidence artifact. Browser traces/HTML reports, native test reports/xcresults, the APK and the unsigned simulator app are also retained by GitHub Actions for **30 days**. Job summaries list each capture and result. Release assets have separate retention rules.

Comparison rejects missing captures/baselines and dimension changes. It tolerates at most 0.05% differing pixels after ignoring channel differences of 12 or less, to handle small renderer rounding. A failure needs a reviewed fix or baseline update; increasing the threshold to conceal a layout regression is not acceptable.

The visual browser reference is Chromium on Ubuntu 24.04 with Playwright 1.64.0. Desktop and Android references use the same Linux job. iOS pins its runtime/device and status-bar date/time/battery, waits for stable full-screen captures, and normalizes orientation metadata. A runner/runtime/font update can require a reviewed baseline refresh. Baselines from a different local OS should not replace the CI references casually.

Production packaging has a separate browser check after `build_site.py`. It serves `build/site` under `/domposable/`, verifies content-hashed JavaScript and Wasm assets, reloads nested HTML routes in Chromium/Firefox/WebKit, and starts the production Wasm shop in Chromium. Development previews remain convenient for ordinary feature tests; these production checks exercise the files that Pages receives.

```bash
./gradlew :demo:jsBrowserDistribution :demo:wasmJsBrowserDistribution dokkaGenerate
python scripts/ci/build_site.py
npx playwright test --config playwright.production.config.ts
```

## Update baselines deliberately

1. Push the implementation branch and run **CI** manually on that branch with `record_screenshots=true`.
2. Download `android-html-evidence` and `native-evidence`. Inspect every changed image and the behavior reports. Recording exports candidates; it does **not** update Git files.
3. Copy approved `build/screenshots/<platform>/*.png` from the downloaded artifacts to `screenshots/<platform>/`. Keep the manifest synchronized with intended fixtures.
4. Commit the reviewed images to the PR and run normal CI again. A normal run must pass before merge/release.

Recording runs deliberately fail **CI passed**, so they cannot satisfy branch protection or release candidate checks. They still produce artifacts for review. No bot automatically blesses a changed rendering.

For local inspection, build both browser development bundles, run `python scripts/ci/browser_preview.py`, then `npx playwright test`. Native captures run with `:demo:testAndroidHostTest`, `:demo:jvmTest` and `python scripts/ci/ios_test.py`. Compare a platform with `python scripts/ci/screenshots.py dom`; use `--record` to inspect candidates without a comparison. On Linux, run desktop tests under `xvfb-run -a`.

ABI changes require `./gradlew updateKotlinAbi` and review of the generated diff. Follow [CONTRIBUTING](https://github.com/samuolis/domposable/blob/main/CONTRIBUTING.md) for toolchain setup. Automated checks supplement manual keyboard, VoiceOver/TalkBack, touch and real-device reviews; screenshot coverage alone is not accessibility certification.

## Inline PR evidence

Ranker's review format is also used here: action title, scoped architecture bullets plus Mermaid, consumer API example, executable build commands, and relevant validation. For visual changes, embed PNG previews in the PR body. Main | Pixel difference | PR comparisons apply when a scenario has a main baseline; new scenarios get an added preview.

Run **Publish visual review** with a completed CI run ID and the open PR number. It validates provenance against the current PR head, reads bounded PNG captures, fetches baseline images at the exact base revision, and uploads comparisons to the `visual-evidence` GitHub prerelease asset bucket. The job summary and `visual-review-markdown` artifact contain content-addressed image URLs to paste into `Visual evidence`. Each filename records the PR, CI run and SHA-256. The publisher verifies the downloaded PNG bytes and never overwrites an existing asset. Buckets rotate at GitHub’s asset limit; they are excluded from library release/version selection. No image branches are created. Reviewed golden inputs remain in main and full CI captures remain in Actions artifacts. Maintainers also inspect the rendered PR body before marking it ready.

Generated comparisons stay outside main and feature branches. Approved golden inputs remain in `screenshots/`; documentation illustrations can be refreshed from them with `python scripts/ci/export_examples.py`. Recording candidates and publishing review evidence are separate from approving baseline updates.

Use the [physical-device checklist](accessibility-review.md) to record TalkBack, VoiceOver, keyboard, touch, large-text and theme reviews. The issue form keeps untested checks explicit; results are tied to the reviewed build and device.
