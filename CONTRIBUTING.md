# Contributing

Open an issue for substantial API changes. Keep feature APIs independent of application architecture and platform rendering. Add a portable props declaration and implement behavior/semantics in each supported provider. Preserve modifier ordering and stable keys.

Run shared tests and the relevant platform checks. The required **Binary API compatibility** job validates every published module against reviewed JVM/Android/Klib baselines with all targets compiled on macOS. CI retains PR-base and latest-release diffs alongside the generated dumps. Public baseline updates require a consumer sample and an **API compatibility** section covering classification, migration and release impact; updating a baseline alone does not establish compatibility. Read [binary compatibility](docs/binary-compatibility.md) before changing the public surface. Run Playwright when HTML interaction, focus or semantics change.

```sh
python3 -m venv .venv
.venv/bin/pip install -r docs/requirements.txt
.venv/bin/python -m unittest discover -s scripts/tests -v
./gradlew jvmTest binaryApiCheck -Pdomposable.abi.strict=true publishAllPublicationsToFixtureRepository
python3 scripts/ci/binary_api.py --base origin/main
python3 scripts/ci/package_publications.py --unsigned
./gradlew :consumer-fixture:compileKotlinJs -Pdomposable.consumer.repository="$PWD/build/repository"
```

Run the required quality checks before submitting a PR:

```sh
./gradlew ktlintFormat
./gradlew qualityCheck --continue
# Linux desktop tests need a display: xvfb-run -a ./gradlew qualityCheck --continue
```

`ktlintCheck` checks formatting without editing files. `detekt` analyzes handwritten Kotlin across all source sets. `androidLint` checks every Android app/library with warnings as errors. `koverHtmlReport`, `koverXmlReport`, `koverVerify` and `verifyCoverage` measure JVM/Android host tests and enforce total/per-package floors. Reports live in `build/reports/`; CI retains `quality-and-coverage` for 30 days. Read [test coverage](docs/coverage.md) for the measured scope and remaining gaps.

Compose functions retain their normal naming and declarative layout. Narrow, explained suppressions cover portable-to-native modifier conversion, related DSL functions and existing JVM file facades. Do not add blanket suppressions or baselines to conceal failures. ktlint deliberately excludes generated/build outputs, so checking formatting never runs KSP or formats third-party generated code.

All implementation changes go through branches and PRs. Only the release workflow may advance `main` directly with an approved, tested version-only commit. Do not commit credentials, provisioning profiles, signing keys or device lists. The Apache-2.0 license applies to contributions.

Visual baselines live in `screenshots/`; follow [the review process](docs/validation.md) to record candidates on CI, inspect them, and commit approved images. Never automatically overwrite them in a PR run. Browser tests require both JS and Wasm development bundles.

Follow the [PR template](.github/pull_request_template.md): concise action title and summary; architecture bullets plus a Mermaid diagram when boundaries change; realistic consumer examples for API changes; compatibility/migration/release fields for ABI baseline changes; build commands for Gradle/CI changes. Visual changes require inline immutable PNG previews, not only artifact links. `PR review contract` validates the sections relevant to the changed files. Approved release candidates receive the same named check through an automated version-only diff audit, allowing the release App to advance the already-tested version commit.

After a completed capture run, use **Publish visual review** with its run ID and the PR number. It checks the current head, reads only screenshot data, compares against the PR base, and publishes PNGs as GitHub release assets in the `visual-evidence` bucket. Copy the job's Markdown into the PR's Visual evidence section. Filenames include the PR, CI run and full image SHA-256. The publisher verifies the downloaded bytes and never overwrites an asset. The workflow does not update main, baselines, or post comments. Inspect the rendered previews before marking a PR ready.

Maintainers: [validation and golden updates](docs/validation.md), [release setup and recovery](docs/releasing.md), [remaining release work](docs/roadmap.md).
