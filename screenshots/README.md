# Reviewed visual inputs

The 31 scenarios in `manifest.json` cover Android, iOS, desktop, semantic HTML and Wasm. These PNGs are regression inputs, not automatically generated PR output. Changes require inspecting the captures and documenting the reason in the PR.

Android, desktop, HTML and Wasm references come from [CI run 38131233383](https://github.com/samuolis/domposable/actions/runs/38131233383), recording source commit `adacb77e8737579e041a0aa6b98da273af6a78cb`. iOS references come from [CI run 38132931021](https://github.com/samuolis/domposable/actions/runs/38132931021), source commit `6687294ff27c09304aa37b6ae5a4bc6de96b15bf`, after fixing the landscape capture:

| Platform | Reference environment |
| --- | --- |
| Android | Ubuntu 24.04, Robolectric native graphics, API 36 |
| Desktop | Ubuntu 24.04, Compose TestScene under Xvfb |
| HTML / Wasm | Ubuntu 24.04, Playwright 1.64.0 Chromium |
| iOS | macOS 26, iOS 26.5, iPad mini A17 Pro simulator |

iOS pins the status-bar clock and battery, waits for stable display frames, and normalizes orientation metadata. Historical iPad captures contain the runner's date, so comparison excludes only that system date label on the pinned portrait/landscape dimensions; app content and the rest of the status bar remain checked. Browser captures wait for fonts and stable frames; storefront captures also wait for artwork. Captures from a different local OS should not replace hosted references without review.

See [the contributor workflow](../docs/validation.md) for recording, comparison tolerances, artifacts and immutable inline PR previews. Recording runs deliberately cannot satisfy the merge/release gate. Guide illustrations are derived from these reviewed inputs with `python scripts/ci/export_examples.py`.
