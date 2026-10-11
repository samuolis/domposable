# Contributing

Open an issue for substantial API changes. Keep feature APIs independent of application architecture and platform rendering. Add a portable props declaration and implement behavior/semantics in each supported provider. Preserve modifier ordering and stable keys.

Run shared tests and the relevant platform checks. Public API changes update checked-in baselines with `./gradlew updateKotlinAbi`; review those changes alongside the implementation. CI uses `checkKotlinAbi` and compiles a consumer against actual local Maven publications. Run Playwright when HTML interaction, focus or semantics change.

```sh
python3 -m venv .venv
.venv/bin/pip install -r docs/requirements.txt
.venv/bin/python -m unittest discover -s scripts/tests -v
./gradlew jvmTest checkKotlinAbi publishAllPublicationsToFixtureRepository
python3 scripts/ci/package_publications.py --unsigned
./gradlew :consumer-fixture:compileKotlinJs -Pdomposable.consumer.repository="$PWD/build/repository"
```

All implementation changes go through branches and PRs. Only the release workflow may advance `main` directly with an approved, tested version-only commit. Do not commit credentials, provisioning profiles, signing keys or device lists. The Apache-2.0 license applies to contributions.

Visual baselines live in `screenshots/`; follow [the review process](docs/validation.md) to record candidates on CI, inspect them, and commit approved images. Never automatically overwrite them in a PR run. Browser tests require both JS and Wasm development bundles.

Follow the [PR template](.github/pull_request_template.md): concise action title and summary; architecture bullets plus a Mermaid diagram when boundaries change; realistic consumer examples for API changes; build commands for Gradle/CI changes. Visual changes require inline immutable PNG previews, not only artifact links. `PR review contract` validates the sections relevant to the changed files.

After a completed capture run, use **Publish visual review** with its run ID and the PR number. It checks the current head, reads only screenshot data, compares against the PR base, and publishes PNGs on an isolated `visual-review/**` ref. Copy the job's Markdown into the PR's Visual evidence section. The images use immutable commit URLs. The workflow does not update main, baselines, or post comments. Inspect the rendered previews before marking a PR ready.

Maintainers: [validation and golden updates](docs/validation.md), [release setup and recovery](docs/releasing.md), [remaining release work](docs/roadmap.md).
