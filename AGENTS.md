# Domposable

- Keep feature composables independent of DI, ViewModel inheritance and state architecture.
- Shared public APIs must compile for Android, iOS, JVM and semantic HTML (Kotlin/JS).
- Keep platform rendering inside renderer source sets; never put Compose UI nodes under the DOM applier.
- Verify public API changes, renderer behavior and packaged consumers before releasing.
- Ordinary changes use branches and PRs. Only the approved release workflow may push a version-only commit directly to main.
- Never commit signing credentials or reuse another application's release keys.

## Pull request review

- Use the PR template and a concise action title describing the final behavior. Keep summary bullets concrete. Do not require conventional-commit prefixes; Ranker uses human-readable titles.
- Public API changes need a realistic consumer code sample. Architecture, build/dependency topology, screen contracts, navigation and renderer-boundary changes need mobile-readable `Architecture at a glance` bullets and a Mermaid diagram. Gradle/CI changes need executable validation commands in `Build changes`.
- ABI baseline changes need `API compatibility` fields for classification (additive/breaking/tooling-only), migration and release impact (patch/minor/major). Inspect both PR-base and latest-release diffs. Regenerating a dump does not establish compatibility; pre-1.0 breaks require a minor release and migration notes, stable breaks a major release.
- Visual changes need descriptive inline previews. Existing scenarios show Main | Pixel difference | PR; added scenarios show the new preview. Publish generated review images as content-addressed GitHub release assets in the `visual-evidence` bucket, using absolute HTTPS PNG URLs. Never overwrite an existing asset or create image branches. Verify image bytes and rendered PR previews before declaring review ready.
- Checked-in golden inputs belong in `screenshots/`. CI exports captures and diffs as artifacts; maintainers inspect candidates before updating goldens. Recording runs cannot satisfy `CI passed`.
