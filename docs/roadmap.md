# Release readiness and product follow-ups

## Release validation

- Public demo downloads contain the signed Android APK. Apple signing and public iOS downloads are deferred; iOS library compilation and simulator tests remain in CI.
- Require platform behavior, visual comparisons, public ABI and published-consumer checks before merging a release input. Deploy the website from the same candidate as the Maven artifacts and APK.
- Use **Bump and release**, verify Maven Central coordinates and signed APK identity/version, and install the demo on a real Android device. `0.0.0` is reserved for the unpublished bootstrap baseline.
- Keep dedicated credential backups secure and track publishing-token, certificate and developer-membership expiry dates privately.

## Product follow-ups

- Decide which APIs to stabilize after using the library in a real feature. Gather migration feedback from a team using its own DI/state architecture.
- Extend Wasm accessibility and input interaction coverage beyond the tested startup, URL navigation and saveable restoration scenarios.
- Test a complete Kobweb application/export, beyond the compiled page wrapper, before claiming full Kobweb-version support.
- Evaluate independent root back stacks, richer Navigation 3 scene strategies, HTML list virtualization and large-data performance.
- Manually review native assistive technology, browser screen readers, keyboard/touch behavior, dark themes and real-device rendering.

These are explicit remaining boundaries, not prerequisites for every application that wants to use Domposable. Additional platforms should be added through renderer/source-set implementations and published-consumer checks rather than a larger mandatory screen abstraction.

Public demo downloads currently include Android APKs. iOS download/signing configuration is deferred; iOS library compilation and simulator regression checks remain required.
