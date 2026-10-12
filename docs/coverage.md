# Component support and test coverage

| Family | Shared API and native/HTML/Wasm implementations |
| --- | --- |
| Layout | Theme, Column, Row, FlowRow, Box, Scaffold, measure, ordered modifiers |
| Collections | Keyed LazyColumn, LazyRow, LazyGrid |
| Content | Text/headings, Image, Icon, Divider, Spacer, Avatar, Badge |
| Actions | Button, Link, IconButton, FloatingActionButton, Chip, Card, Tooltip |
| Forms | TextField, Checkbox, Switch, RadioButton, Slider, RangeSlider, Select, SegmentedButtons |
| Feedback | Banner, Progress, Snackbar, Surface |
| Navigation | TopAppBar, Tabs, NavigationBar, Menu |
| Overlays | Dialog, BottomSheet |
| Architecture | Route registry, entry resources/saveables, optional Navigation 3, lifecycle and KSP |

The expanded shared API has 41 renderer contracts; the 0.1.x provider has 34. HTML uses native input elements, headings, landmarks, labeled dialog elements, tabs with keyboard navigation and focus restoration. Native providers use Compose UI/Material3 semantics. Browser accessibility checks run in Chromium, Firefox and WebKit. Native demo smoke tests run on an iOS simulator; shared rules are tested on JVM.

HTML keyed collections normally render all items. LazyColumn also supports an optional [fixed-height scroll window](performance.md), with browser tests for bounded DOM size, keyboard focus, keyed drafts and removed rows. Variable-height virtualization, windowed rows/grids, rich text editors, drag/reorder, complex date pickers, fold/hinge APIs, platform camera/maps and product-specific widgets remain outside the provider. Date/time HTML inputs use browser controls; native text fields currently expose text input for those purposes. Scrolling and sizing use each platform's layout model and are not pixel-identical.

The default design provider uses upstream Material3 alpha APIs matching the current Compose release. JVM desktop is experimental. Until 1.0, minor releases may change APIs. KSP generates typed entries and feature entry lists; screen business logic and DI remain application-owned.

## Measured test coverage

Every PR runs `qualityCheck`: ktlint checks handwritten Kotlin and Gradle scripts; detekt checks all source sets; Android lint checks the app and every Android library, including dependencies and tests. Correctness warnings fail the build. Dependency update notices are handled by Dependabot. There are no lint or detekt baselines that hide existing findings.

Kover instruments **JVM and Android host tests** and reports the eight published library modules together. Demo and modular feature tests contribute execution to that report. Demo application classes, generated entry lists and Android-generated resource classes are excluded from the library percentage. Handwritten KSP processor code stays included, so its unmeasured execution is visible.

| Gate | Minimum line coverage | Minimum branch coverage |
| --- | ---: | ---: |
| All library JVM/Android code | 70% | 35% |
| Each core package: foundation, adaptive, screens, navigation3, lifecycle | 80% | 60% |
| KSP processor | 90% | 70% |

The policy lives in [`config/coverage.json`](https://github.com/samuolis/domposable/blob/main/config/coverage.json). Both Kover and the package verifier enforce it. Missing reports, missing required packages, empty measured source and coverage below a floor fail validation. Raise floors as tests improve; lowering a floor requires a reviewed justification.

```bash
./gradlew qualityCheck --continue
# On Linux, use xvfb-run -a ./gradlew qualityCheck --continue
python3 scripts/ci/coverage_report.py build/reports/coverage/coverage.xml --verify config/coverage.json
```

The **Kotlin quality, Android lint and coverage** job summarizes total and per-package percentages. Download its `quality-and-coverage` artifact for `build/reports/coverage/html/index.html`, XML, JSON, test results and lint reports. Artifacts are retained for 30 days. The latest successful CI report is the source of truth; a percentage in documentation would become stale.

## What the percentage does not cover

Kover does not instrument Kotlin/JS, Wasm or iOS. Their behavior, accessibility, screenshots and published consumer builds remain required, separate checks. A native screenshot and a browser interaction test exercise different renderers; neither implies the other platform's branch coverage.

KSP has direct, instrumented JVM tests using the pinned KSP2 analysis engine and the production processor provider. They check route/scope wiring, required and default dependencies, ViewModel-owner placement, aggregate dependency conflicts, component generation and diagnostics for invalid screen declarations. These executions contribute to Kover; the separate generated feature and published-consumer compilations still validate real Compose contracts across all six targets.

Line coverage also does not prove correct behavior. Tests must assert meaningful contracts, including invalid values, state restoration, entry disposal, navigation and API 24 compatibility. See [validation](validation.md) for platform checks and screenshot review.
