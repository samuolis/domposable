# Domposable

- Keep feature composables independent of DI, ViewModel inheritance and state architecture.
- Shared public APIs must compile for Android, iOS, JVM and semantic HTML (Kotlin/JS).
- Keep platform rendering inside renderer source sets; never put Compose UI nodes under the DOM applier.
- Verify public API changes, renderer behavior and packaged consumers before releasing.
- Ordinary changes use branches and PRs. Only the approved release workflow may push a version-only commit directly to main.
- Never commit signing credentials or reuse another application's release keys.
