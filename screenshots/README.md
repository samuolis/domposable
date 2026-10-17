# Reviewed visual inputs

The scenarios in `manifest.json` cover Android, iOS, desktop, semantic HTML and Wasm. These PNGs are regression inputs, not automatically generated PR output. Changes require inspecting the captures and documenting the reason in the PR.

Initial Android, desktop, HTML and Wasm references come from [CI run 38131233383](https://github.com/samuolis/domposable/actions/runs/38131233383), recording source commit `adacb77e8737579e041a0aa6b98da273af6a78cb`. Initial iOS references come from [CI run 38132931021](https://github.com/samuolis/domposable/actions/runs/38132931021), source commit `6687294ff27c09304aa37b6ae5a4bc6de96b15bf`, after fixing the landscape capture:

| Platform | Reference environment |
| --- | --- |
| Android | Ubuntu 24.04, Robolectric native graphics, API 36 |
| Desktop | Ubuntu 24.04, Compose TestScene under Xvfb |
| HTML / Wasm | Ubuntu 24.04, Playwright 1.64.0 Chromium |
| iOS | macOS 26, iOS 26.5, iPad mini A17 Pro simulator |

iOS pins the status-bar clock and battery, waits for stable display frames, and normalizes orientation metadata. Historical iPad captures contain the runner's date, so comparison excludes only that system date label on the pinned portrait/landscape dimensions; app content and the rest of the status bar remain checked. Browser captures wait for fonts and stable frames; storefront captures also wait for artwork. Captures from a different local OS should not replace hosted references without review.

Later reviewed changes are recorded in Git history and their PR visual evidence.

See [the contributor workflow](../docs/validation.md) for recording, comparison tolerances, artifacts and immutable inline PR previews. Recording runs deliberately cannot satisfy the merge/release gate. Guide illustrations are derived from these reviewed inputs with `python scripts/ci/export_examples.py`.

## Material renderer review

PR #38 updates 101 inspected references from [CI 39021929272](https://github.com/samuolis/domposable/actions/runs/39021929272), source `30681d21a5679a4eb32259fb91cbd8cc076d7cf6`: Material typography/color roles and control geometry, progress and loading shapes, and 64 new HTML/Wasm checkbox interaction references across eight themes. The same fixture also exports slider, switch and radio interaction comparisons at phone and desktop widths. Native product-form references are reviewed separately after correcting intrinsic segmented-label measurement.

Five inspected Android/desktop/iOS/Wasm references from [CI 39024340884](https://github.com/samuolis/domposable/actions/runs/39024340884), source `5bf1a307d0ca0589ef06f4d686a9dbd5943a5c7b`, record intrinsic segmented labels remaining on one line without equal-width measurement. The Wasm navigation capture with missing fallback symbols was rejected.

Eight inspected HTML pressed-state references from [CI 39026857287](https://github.com/samuolis/domposable/actions/runs/39026857287), source `beb25c8e222cd57122cd012f82f7ea7177eeac12`, record the composed pointer-hover and press layer across light/dark, brand, component-override and expressive themes. All other 167 references passed unchanged.
